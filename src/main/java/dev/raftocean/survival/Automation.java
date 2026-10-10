package dev.raftocean.survival;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.menu.RaftStorage;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;

/** Loaded, server-owned raft-local utility networks. Resources are committed only after capacity checks. */
public final class Automation {
    public static final int ENERGY_CAPACITY = 2000;
    public static final DeferredItem<Item> BIOFUEL = ModContent.ITEMS.registerSimpleItem("biofuel");
    private static final Comparator<BlockPos> ORDER = Comparator.comparingInt((BlockPos p)->p.getY())
            .thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ);
    public static void initialize() { }
    private Automation() { }
    public static int waterCapacity(RaftPiece.Kind kind) { return kind == RaftPiece.Kind.WATER_TANK ? 64 : 4; }
    public enum Network { WATER, FUEL, POWER }
    public static boolean member(RaftPiece.Kind kind, Network network) {
        return switch(network) {
            case WATER -> switch(kind) {
                case WATER_PIPE, WATER_TANK, ELECTRIC_PURIFIER, PURIFIER, RAIN_COLLECTOR, SPRINKLER,
                     PLANTER, TREE_PLANTER, COOKING_POT -> true;
                default -> false;
            };
            case FUEL -> kind == RaftPiece.Kind.FUEL_PIPE || kind == RaftPiece.Kind.FUEL_TANK
                    || kind == RaftPiece.Kind.ENGINE || kind == RaftPiece.Kind.BIOFUEL_GENERATOR
                    || kind == RaftPiece.Kind.BATTERY_CHARGER;
            case POWER -> kind == RaftPiece.Kind.POWER_CABLE || kind == RaftPiece.Kind.BIOFUEL_GENERATOR
                    || kind == RaftPiece.Kind.ELECTRIC_PURIFIER || kind == RaftPiece.Kind.SPRINKLER
                    || kind == RaftPiece.Kind.BATTERY_CHARGER;
        };
    }
    /** Each cell is visited once; unrelated machines and other conduit types cannot bridge a network. */
    public static List<List<BlockPos>> components(Map<BlockPos,RaftPiece> pieces, Network network) {
        Set<BlockPos> remaining = new HashSet<>();
        pieces.forEach((p,piece)->{if(member(piece.kind(),network)){remaining.add(p);}});
        List<List<BlockPos>> result = new ArrayList<>();
        for(BlockPos seed : remaining.stream().sorted(ORDER).toList()) {
            if(!remaining.remove(seed)){continue;}
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();queue.add(seed);
            List<BlockPos> group = new ArrayList<>();
            while(!queue.isEmpty()) {
                BlockPos cell=queue.removeFirst();group.add(cell);
                for(Direction direction:Direction.values()) {
                    BlockPos neighbor=cell.relative(direction);
                    if(remaining.remove(neighbor)){queue.addLast(neighbor);}
                }
            }
            group.sort(ORDER);result.add(group);
        }
        return result;
    }
    public static void tick(RaftEntity raft) { tick(raft,raft.level().getGameTime()/20); }

    /** A rotating recipient order avoids permanent starvation on a busy, under-supplied network. */
    static List<BlockPos> priority(List<BlockPos> recipients,long second) {
        List<BlockPos> ordered=new ArrayList<>(recipients);
        if(!ordered.isEmpty()){Collections.rotate(ordered,-(int)Math.floorMod(second,(long)ordered.size()));}
        return ordered;
    }
    static void tick(RaftEntity raft,long second) {
        if(!(raft.level() instanceof ServerLevel level)){return;}
        var pieces=raft.pieces();
        boolean enabled=RaftConfig.AUTOMATION.get();
        for(var entry:pieces.entrySet()) {
            var kind=entry.getValue().kind();
            if(kind.automation() && kind!=RaftPiece.Kind.BIOREFINER) {
                var storage=raft.storage(entry.getKey());
                storage.status=!enabled || storage.paused?"off":"network";
                if(enabled && !storage.paused){exchangeCup(storage,kind);}
            }
        }
        if(!enabled){return;}
        for(var group:components(pieces,Network.FUEL)) {
            var recipients=priority(group.stream().filter(p->{
                var kind=pieces.get(p).kind();
                if(kind!=RaftPiece.Kind.ENGINE && kind!=RaftPiece.Kind.BIOFUEL_GENERATOR && kind!=RaftPiece.Kind.BATTERY_CHARGER){return false;}
                var receiver=raft.storage(p);var fuel=receiver.getItem(kind==RaftPiece.Kind.ENGINE?1:0);
                return !receiver.paused && (fuel.isEmpty() || fuel.is(BIOFUEL.get()) && fuel.getCount()<fuel.getMaxStackSize());
            }).toList(),second);
            Set<BlockPos> supplied=new HashSet<>();
            for(var source:group) {
                if(pieces.get(source).kind()!=RaftPiece.Kind.FUEL_TANK || raft.storage(source).paused){continue;}
                var tank=raft.storage(source);
                for(var target:recipients) {
                    if(supplied.contains(target)){continue;}
                    if(transferFuel(tank,raft.storage(target),pieces.get(target).kind()==RaftPiece.Kind.ENGINE?1:0)){
                        supplied.add(target);
                    }
                }
            }
        }
        for(var group:components(pieces,Network.POWER)) {
            List<RaftStorage> generators=group.stream().filter(p->pieces.get(p).kind()==RaftPiece.Kind.BIOFUEL_GENERATOR
                    && !raft.storage(p).paused).map(raft::storage).toList();
            List<BlockPos> ready=new ArrayList<>();
            for(var pos:group) {
                var kind=pieces.get(pos).kind();
                if(kind!=RaftPiece.Kind.ELECTRIC_PURIFIER && kind!=RaftPiece.Kind.SPRINKLER && kind!=RaftPiece.Kind.BATTERY_CHARGER){continue;}
                var storage=raft.storage(pos);
                if(storage.paused){continue;}
                if(kind==RaftPiece.Kind.BATTERY_CHARGER) {
                    if(Batteries.missing(storage)==0){Batteries.tickCharger(storage,List.of(),second);continue;}
                } else if(kind==RaftPiece.Kind.ELECTRIC_PURIFIER) {
                    storage.duration=RaftConfig.ELECTRIC_PURIFY_TICKS.get();
                    var point=raft.stationPosition(pos,kind);
                    boolean water=Double.isFinite(dev.raftocean.salvage.DebrisSpawning.surface(level,point.x,raft.getY(),point.z));
                    if(storage.cleanWater>=4){storage.status="full";continue;}
                    if(!water){storage.status="water";continue;}
                } else {
                    if(irrigationTarget(raft,pos,second)==null){continue;}
                    if(storage.cleanWater==0){storage.status="water";continue;}
                }
                ready.add(pos);
            }
            boolean demand=!ready.isEmpty();
            for(var generator:generators) {
                if(demand && generator.energy < ENERGY_CAPACITY) {
                    if(generator.fuelTicks==0 && generator.getItem(0).is(BIOFUEL.get())) {
                        generator.removeItem(0,1);generator.fuelTicks=generator.fuelCapacity=RaftConfig.GENERATOR_ENERGY.get();
                    }
                    int advance=Math.min(20,Math.min(generator.fuelTicks,ENERGY_CAPACITY-generator.energy));
                    generator.energy+=advance;generator.fuelTicks-=advance;
                    generator.status=advance>0?"working":"fuel";
                } else {generator.status=generator.energy==ENERGY_CAPACITY?"full":"network";}
                generator.duration=generator.fuelCapacity;generator.progress=generator.fuelCapacity-generator.fuelTicks;
                generator.setChanged();
            }
            for(var pos:priority(ready,second)) {
                var kind=pieces.get(pos).kind();var storage=raft.storage(pos);
                if(kind==RaftPiece.Kind.BATTERY_CHARGER) {
                    Batteries.tickCharger(storage,generators,second);
                } else if(kind==RaftPiece.Kind.ELECTRIC_PURIFIER) {
                    if(!Batteries.spend(generators,storage,RaftConfig.PURIFIER_ENERGY.get())){storage.status="power";continue;}
                    storage.status="working";storage.progress+=Math.max(1,(int)(20*RaftConfig.PROCESSING.get()));
                    if(storage.progress>=storage.duration){storage.progress=0;storage.cleanWater++;}
                    storage.setChanged();
                } else {
                    BlockPos target=irrigationTarget(raft,pos,second);
                    if(target==null){continue;}
                    if(!Batteries.spend(generators,storage,RaftConfig.SPRINKLER_ENERGY.get())){storage.status="power";continue;}
                    storage.cleanWater--;raft.storage(target).cleanWater++;raft.storage(target).setChanged();
                    storage.status="working";storage.setChanged();
                }
            }
        }
        for(var group:components(pieces,Network.WATER)) {
            var recipients=priority(group.stream().filter(p->{var kind=pieces.get(p).kind();return
                    (kind==RaftPiece.Kind.WATER_TANK || waterConsumer(kind)) && !raft.storage(p).paused
                    && raft.storage(p).cleanWater<waterCapacity(kind);}).toList(),second);
            Set<BlockPos> supplied=new HashSet<>();
            for(var source:group) {
                var kind=pieces.get(source).kind();
                if(kind!=RaftPiece.Kind.WATER_TANK && kind!=RaftPiece.Kind.PURIFIER
                        && kind!=RaftPiece.Kind.RAIN_COLLECTOR && kind!=RaftPiece.Kind.ELECTRIC_PURIFIER){continue;}
                var reservoir=raft.storage(source);
                if(reservoir.paused){continue;}
                for(var target:recipients) {
                    if(reservoir.cleanWater==0){break;}
                    if(supplied.contains(target) || kind==RaftPiece.Kind.WATER_TANK
                            && pieces.get(target).kind()==RaftPiece.Kind.WATER_TANK){continue;}
                    if(transferWater(reservoir,raft.storage(target),waterCapacity(pieces.get(target).kind()))>0){supplied.add(target);}
                }
            }
        }
    }
    private static boolean waterConsumer(RaftPiece.Kind kind) {
        return kind==RaftPiece.Kind.SPRINKLER || kind==RaftPiece.Kind.PLANTER
                || kind==RaftPiece.Kind.TREE_PLANTER || kind==RaftPiece.Kind.COOKING_POT;
    }
    public static int transferWater(RaftStorage source,RaftStorage target,int capacity) {
        if(source==target || source.paused || target.paused || source.cleanWater<=0 || target.cleanWater>=capacity){return 0;}
        source.cleanWater--;target.cleanWater++;source.setChanged();target.setChanged();return 1;
    }
    public static boolean transferFuel(RaftStorage source,RaftStorage target,int slot) {
        ItemStack fuel=source.getItem(0),destination=target.getItem(slot);
        if(source==target || source.paused || target.paused || !fuel.is(BIOFUEL.get()) || !destination.isEmpty()
                && (!ItemStack.isSameItemSameComponents(fuel,destination) || destination.getCount()>=destination.getMaxStackSize())){return false;}
        if(destination.isEmpty()){target.setItem(slot,fuel.copyWithCount(1));}else{destination.grow(1);}
        source.removeItem(0,1);source.setChanged();target.setChanged();return true;
    }
    public static boolean spend(List<RaftStorage> generators,int amount) {
        if(amount<=0 || generators.stream().filter(g->!g.paused).mapToLong(g->g.energy).sum()<amount){return false;}
        for(var generator:generators) {
            if(generator.paused){continue;}
            int take=Math.min(amount,generator.energy);generator.energy-=take;amount-=take;generator.setChanged();
            if(amount==0){break;}
        }
        return true;
    }
    private static BlockPos irrigationTarget(RaftEntity raft,BlockPos sprinkler,long second) {
        int radius=RaftConfig.SPRINKLER_RADIUS.get();
        var targets=raft.pieces().entrySet().stream().filter(e->e.getKey().getY()==sprinkler.getY()
                && (e.getValue().kind()==RaftPiece.Kind.PLANTER || e.getValue().kind()==RaftPiece.Kind.TREE_PLANTER)
                && Math.abs(e.getKey().getX()-sprinkler.getX())<=radius && Math.abs(e.getKey().getZ()-sprinkler.getZ())<=radius
                && !raft.storage(e.getKey()).paused && raft.storage(e.getKey()).cleanWater<4)
                .map(Map.Entry::getKey).sorted(ORDER).toList();
        var ordered=priority(targets,second);return ordered.isEmpty()?null:ordered.getFirst();
    }
    private static void exchangeCup(RaftStorage storage,RaftPiece.Kind kind) {
        if(kind!=RaftPiece.Kind.WATER_TANK && kind!=RaftPiece.Kind.ELECTRIC_PURIFIER && kind!=RaftPiece.Kind.SPRINKLER){return;}
        ItemStack cup=storage.getItem(0);
        boolean draw=cup.is(SurvivalContent.EMPTY_CUP.get()) && storage.cleanWater>0;
        boolean pour=cup.is(SurvivalContent.FRESH_WATER.get()) && storage.cleanWater<waterCapacity(kind);
        if(!draw && !pour){return;}
        ItemStack result=new ItemStack(draw?SurvivalContent.FRESH_WATER.get():SurvivalContent.EMPTY_CUP.get());
        int slot=draw?2:3;ItemStack existing=storage.getItem(slot);
        if(!existing.isEmpty() && (!ItemStack.isSameItemSameComponents(existing,result) || existing.getCount()>=existing.getMaxStackSize())){storage.status="cup";return;}
        storage.removeItem(0,1);storage.cleanWater+=draw?-1:1;
        if(existing.isEmpty()){storage.setItem(slot,result);}else{existing.grow(1);}
        storage.setChanged();
    }
}
