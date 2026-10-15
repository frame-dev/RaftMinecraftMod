package dev.raftocean.api;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.RaftStorage;
import dev.raftocean.survival.Automation;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Server-only, preflighted draws from explicitly declared neighbouring utility inputs. */
public final class RaftMachineUtilities {
    private record Topology(long revision,Map<RaftUtility,List<List<BlockPos>>> groups,Map<BlockPos,Set<RaftUtility>> inputs) { }
    private static final Map<RaftEntity,Topology> CACHE=new WeakHashMap<>();
    private static final Comparator<BlockPos> ORDER=Comparator.comparingInt((BlockPos pos)->pos.getY()).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ);
    private RaftMachineUtilities() { }
    private static synchronized Topology topology(RaftEntity raft) {
        var previous=CACHE.get(raft);
        if(previous!=null && previous.revision()==raft.layoutRevision())return previous;
        Map<RaftUtility,List<List<BlockPos>>> groups=new EnumMap<>(RaftUtility.class);
        groups.put(RaftUtility.POWER,Automation.components(raft.pieces(),Automation.Network.POWER));
        groups.put(RaftUtility.FRESH_WATER,Automation.components(raft.pieces(),Automation.Network.WATER));
        groups.put(RaftUtility.BIOFUEL,Automation.components(raft.pieces(),Automation.Network.FUEL));
        Map<BlockPos,Set<RaftUtility>> inputs=new HashMap<>();
        for(var entry:raft.pieces().entrySet()) {
            var behavior=RaftBuildables.machine(entry.getValue().addonId());
            if(behavior.isEmpty())continue;
            var declared=Set.copyOf(behavior.get().utilityInputs());
            if(!declared.isEmpty())for(var cell:entry.getValue().occupiedCells(entry.getKey()))inputs.put(cell,declared);
        }
        var next=new Topology(raft.layoutRevision(),groups,Map.copyOf(inputs));CACHE.put(raft,next);return next;
    }
    /** Client-safe declaration lookup, including non-anchor cells of wide/rotated machines. */
    public static boolean acceptsAt(RaftEntity raft,BlockPos occupiedCell,RaftUtility input) {
        Objects.requireNonNull(input);
        return topology(raft).inputs().getOrDefault(occupiedCell,Set.of()).contains(input);
    }
    private static Set<BlockPos> boundary(RaftMachineContext ctx) {
        var piece=ctx.raft().pieces().get(ctx.anchor());
        if(piece==null)return Set.of();
        var cells=piece.occupiedCells(ctx.anchor());Set<BlockPos> boundary=new HashSet<>();
        for(var cell:cells)for(var direction:Direction.values()) {
            var next=cell.relative(direction);if(!cells.contains(next))boundary.add(next);
        }
        return boundary;
    }
    private static List<BlockPos> connected(RaftMachineContext ctx,RaftUtility resource) {
        var inputs=RaftBuildables.machine(ctx.type().id().toString()).orElseThrow().utilityInputs();
        if(!inputs.contains(resource))return List.of();
        var boundary=boundary(ctx);Set<BlockPos> connected=new HashSet<>();
        for(var group:topology(ctx.raft()).groups().get(resource))if(group.stream().anyMatch(boundary::contains))connected.addAll(group);
        return connected.stream().sorted(ORDER).toList();
    }
    private static List<RaftStorage> sources(RaftMachineContext ctx,RaftUtility resource) {
        List<RaftStorage> sources=new ArrayList<>();
        for(var pos:connected(ctx,resource)) {
            var kind=ctx.raft().pieces().get(pos).kind();
            boolean source=switch(resource) {
                case POWER->kind==RaftPiece.Kind.BIOFUEL_GENERATOR;
                case FRESH_WATER->kind==RaftPiece.Kind.WATER_TANK || kind.purifier() || kind==RaftPiece.Kind.RAIN_COLLECTOR || kind==RaftPiece.Kind.ELECTRIC_PURIFIER;
                case BIOFUEL->kind==RaftPiece.Kind.FUEL_TANK || kind==RaftPiece.Kind.BIOREFINER;
            };
            if(source) { var storage=ctx.raft().storage(pos);if(storage!=null && !storage.paused)sources.add(storage); }
        }
        return sources;
    }
    private static int fuelSlot(RaftStorage source) { return source.kind()==RaftPiece.Kind.BIOREFINER?2:0; }
    private static int available(RaftStorage source,RaftUtility resource) {
        return switch(resource) {
            case POWER->Math.max(0,source.energy);
            case FRESH_WATER->Math.max(0,source.cleanWater);
            case BIOFUEL->{var item=source.getItem(fuelSlot(source));yield item.is(Automation.BIOFUEL.get())?item.getCount():0;}
        };
    }
    private static Map<RaftUtility,List<RaftStorage>> plan(RaftMachineContext ctx,RaftUtilityCost cost) {
        Objects.requireNonNull(cost);
        if(!ctx.isInstalled())return null;
        Map<RaftUtility,List<RaftStorage>> plan=new EnumMap<>(RaftUtility.class);
        for(var resource:RaftUtility.values())if(cost.amount(resource)>0) {
            if(!RaftConfig.AUTOMATION.get())return null;
            var sources=sources(ctx,resource);
            if(sources.stream().mapToLong(source->available(source,resource)).sum()<cost.amount(resource))return null;
            plan.put(resource,sources);
        }
        return plan;
    }
    public static boolean canConsume(RaftMachineContext ctx,RaftUtilityCost cost) { return plan(ctx,cost)!=null; }
    /** Call on the server tick thread. Failed preflight spends nothing in any network. */
    public static boolean consume(RaftMachineContext ctx,RaftUtilityCost cost) {
        var plan=plan(ctx,cost);if(plan==null)return false;
        for(var entry:plan.entrySet()) {
            int remaining=cost.amount(entry.getKey());
            for(var source:entry.getValue()) {
                int take=Math.min(remaining,available(source,entry.getKey()));if(take==0)continue;
                switch(entry.getKey()) {
                    case POWER->source.energy-=take;
                    case FRESH_WATER->source.cleanWater-=take;
                    case BIOFUEL->source.removeItem(fuelSlot(source),take);
                }
                source.setChanged();remaining-=take;if(remaining==0)break;
            }
        }
        return true;
    }
    /** Internal producer bridge; addons advertise readiness through powerDemand(). */
    public static boolean hasPowerDemand(RaftEntity raft,List<BlockPos> network) {
        if(!RaftConfig.AUTOMATION.get())return false;
        for(var entry:raft.pieces().entrySet()) {
            var behavior=RaftBuildables.machine(entry.getValue().addonId());
            if(behavior.isEmpty() || !behavior.get().utilityInputs().contains(RaftUtility.POWER))continue;
            var ctx=raft.machineContext(entry.getKey()).orElseThrow();
            if(boundary(ctx).stream().anyMatch(network::contains) && behavior.get().powerDemand(ctx)>0)return true;
        }
        return false;
    }
}
