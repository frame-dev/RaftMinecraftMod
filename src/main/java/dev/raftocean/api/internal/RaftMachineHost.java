package dev.raftocean.api.internal;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import dev.raftocean.api.*;
import dev.raftocean.entity.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.*;

/** Internal lifecycle/persistence bridge. Addons use RaftEntity.machineContext/machineView. */
public final class RaftMachineHost {
    private record Installed(String id,RaftMachineState state) { }
    private final RaftEntity raft;
    private boolean destroyed;
    private final Set<BlockPos> removing=new HashSet<>();
    private final Map<BlockPos,Installed> states=new LinkedHashMap<>();
    private final Map<BlockPos,RaftMachineView> views=new LinkedHashMap<>();
    public RaftMachineHost(RaftEntity raft) { this.raft=raft; }
    public Optional<RaftMachineContext> context(BlockPos pos) {
        if(raft.level().isClientSide())throw new IllegalStateException("Client machine inventory is private; read machineView");
        if(destroyed || removing.contains(pos))return Optional.empty();
        var piece=raft.pieces().get(pos);
        if(piece==null || piece.addonId().isEmpty())return Optional.empty();
        var behavior=RaftBuildables.machine(piece.addonId());if(behavior.isEmpty())return Optional.empty();
        var installed=states.computeIfAbsent(pos.immutable(),key->new Installed(piece.addonId(),new RaftMachineState(behavior.get().slots())));
        if(!installed.id.equals(piece.addonId()))throw new IllegalStateException("Stale addon machine owner");
        return Optional.of(new RaftMachineContext(raft,pos,piece.addon(),installed.state));
    }
    public RaftMachineView view(BlockPos pos) { return views.getOrDefault(pos,RaftMachineView.IDLE); }
    public void placed(BlockPos pos,Player player) {
        context(pos).ifPresent(ctx->RaftBuildables.machine(ctx.type().id().toString()).orElseThrow().placed(ctx,player));sync();
    }
    public void tick() {
        if(destroyed)return;
        for(var entry:raft.pieces().entrySet()) {
            if(removing.contains(entry.getKey()))continue;
            var behavior=RaftBuildables.machine(entry.getValue().addonId());
            if(behavior.isPresent() && raft.tickCount%behavior.get().tickInterval()==0)behavior.get().tick(context(entry.getKey()).orElseThrow());
        }
        if(raft.tickCount%5==0)sync();
    }
    public void detach(BlockPos pos,RaftPiece piece) {
        var installed=states.remove(pos);views.remove(pos);
        if(installed!=null && raft.level() instanceof ServerLevel level) {
            removing.add(pos.immutable());
            try {
            var type=RaftBuildables.find(installed.id).orElseThrow();
            var ctx=new RaftMachineContext(raft,pos,type,installed.state);
            RaftBuildables.machine(installed.id).orElseThrow().removed(ctx);
            Containers.dropContents(level,BlockPos.containing(ctx.worldPosition()),installed.state);
            installed.state.clearContent();
            } finally { removing.remove(pos); }
        }
    }
    public void destroyed() {
        destroyed=true;
        for(var pos:new ArrayList<>(states.keySet()))detach(pos,raft.pieces().get(pos));
    }
    public void sync() {
        if(raft.level().isClientSide())return;
        if(destroyed){views.clear();raft.publishAddonMachines("[]");return;}
        views.clear();var encoded=new ArrayList<List<Integer>>();
        for(var entry:raft.pieces().entrySet()) {
            if(removing.contains(entry.getKey()))continue;
            var behavior=RaftBuildables.machine(entry.getValue().addonId());if(behavior.isEmpty())continue;
            var view=Objects.requireNonNull(behavior.get().view(context(entry.getKey()).orElseThrow()));var pos=entry.getKey();
            views.put(pos,view);encoded.add(List.of(pos.getX(),pos.getY(),pos.getZ(),view.active()?1:0,view.progress(),view.duration(),view.variant()));
        }
        var value=new Gson().toJson(encoded);
        if(value.length()>32767)throw new IllegalStateException("Addon presentation snapshot exceeds native string limit");
        raft.publishAddonMachines(value);
    }
    public void accept(String value) {
        if(!raft.level().isClientSide())return;
        views.clear();if(value.isEmpty())return;
        var array=JsonParser.parseString(value).getAsJsonArray();
        if(array.size()>RaftStructure.MAX_PIECES)throw new IllegalArgumentException("Too many addon machine views");
        for(var element:array) {
            var v=element.getAsJsonArray();if(v.size()!=7)throw new IllegalArgumentException("Invalid addon view");
            var pos=new BlockPos(v.get(0).getAsInt(),v.get(1).getAsInt(),v.get(2).getAsInt());
            views.put(pos,new RaftMachineView(v.get(3).getAsInt()!=0,v.get(4).getAsInt(),v.get(5).getAsInt(),v.get(6).getAsInt()));
        }
    }
    public void save(ValueOutput output) {
        var entries=output.childrenList("AddonMachines");
        for(var pos:raft.pieces().keySet())context(pos).ifPresent(ctx->{
            var entry=entries.addChild();entry.putInt("X",pos.getX());entry.putInt("Y",pos.getY());entry.putInt("Z",pos.getZ());
            entry.putString("Type",ctx.type().id().toString());ctx.state().save(entry);
        });
    }
    public void load(ValueInput input) {
        destroyed=false;
        states.clear();views.clear();removing.clear();
        for(var entry:input.childrenListOrEmpty("AddonMachines")) {
            var pos=new BlockPos(entry.getIntOr("X",0),entry.getIntOr("Y",0),entry.getIntOr("Z",0));
            var ctx=context(pos);if(ctx.isEmpty() || !ctx.get().type().id().toString().equals(entry.getStringOr("Type","")))continue;
            ctx.get().state().load(entry);
        }
        for(var pos:raft.pieces().keySet())context(pos).ifPresent(ctx->RaftBuildables.machine(ctx.type().id().toString()).orElseThrow().loaded(ctx));
        sync();
    }
}
