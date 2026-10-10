package dev.raftocean.api;

import dev.raftocean.entity.RaftEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** A server-side handle to one installed machine, addressed by its immutable local anchor. */
public record RaftMachineContext(RaftEntity raft,BlockPos anchor,RaftBuildable type,RaftMachineState state) {
    public RaftMachineContext {
        anchor=anchor.immutable();
        if(raft.level().isClientSide())throw new IllegalArgumentException("Machine contexts are server-only; use view() on clients");
    }
    public Vec3 worldPosition() {
        var piece=raft.pieces().get(anchor);
        if(piece==null)return raft.toWorld(new Vec3(anchor.getX(),RaftEntity.DECK_HEIGHT+anchor.getY()-1,anchor.getZ()));
        var boxes=raft.pieceBoxes(anchor,piece);var envelope=boxes.getFirst();for(var box:boxes)envelope=envelope.minmax(box);
        return raft.toWorld(envelope.getCenter());
    }
    public boolean valid(Player player) {
        var piece=raft.pieces().get(anchor);
        return dev.raftocean.crew.CrewAccess.allowed(player,raft) && !raft.isRemoved() && player.isAlive() && player.level()==raft.level() && piece!=null
                && piece.addonId().equals(type.id().toString())
                && raft.machineContext(anchor).map(current->current.state()==state).orElse(false)
                && player.position().distanceToSqr(worldPosition())<=64;
    }
}
