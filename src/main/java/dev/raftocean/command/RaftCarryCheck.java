package dev.raftocean.command;

import com.mojang.authlib.GameProfile;
import dev.raftocean.entity.RaftContact;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPlayerSupport;
import dev.raftocean.registry.ModContent;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Exercises production carry in a server without inserting entities or changing blocks. */
public final class RaftCarryCheck {
    private RaftCarryCheck() {
    }

    public static int run(ServerLevel level, Vec3 origin) {
        FakePlayer player = new FakePlayer(level,
                new GameProfile(UUID.fromString("d3550f86-55da-49a2-8a47-d448e36876e6"), "RaftCarryCheck"));
        RaftEntity raft = new RaftEntity(ModContent.RAFT.get(), level);
        Vec3 start = new Vec3(origin.x, level.getMaxY() + 16.0, origin.z);
        Vec3[] directions = {
                new Vec3(0.03, 0, 0), new Vec3(-0.03, 0, 0),
                new Vec3(0, 0, 0.03), new Vec3(0, 0, -0.03),
                new Vec3(0.02, 0, 0.008), new Vec3(-0.02, 0, -0.02)
        };
        if (java.util.Arrays.stream(net.minecraft.world.level.EntityGetter.class.getDeclaredMethods())
                .noneMatch(method -> method.getName().contains("raftOcean"))) {
            throw new IllegalStateException("Foundation collision mixin is not active");
        }
        int assertions = 1;
        for (Vec3 direction : directions) {
            raft.setPos(start);
            player.setPos(start.add(0, RaftEntity.DECK_HEIGHT + RaftContact.CLEARANCE, 0));
            player.setDeltaMovement(Vec3.ZERO);
            Vec3 relative = player.position().subtract(raft.position());
            double lastWave = 0;
            for (int tick = 1; tick <= 120; tick++) {
                double wave = Math.sin(tick * 0.075) * 0.012;
                Vec3 delta = direction.add(0, wave - lastWave, 0);
                raft.setPos(raft.position().add(delta));
                RaftPlayerSupport.carryPlayer(raft, player, delta);
                Vec3 actual = player.position().subtract(raft.position());
                if (actual.distanceToSqr(relative) > 1.0E-12) {
                    throw new IllegalStateException("Carry check failed for " + direction
                            + " at tick " + tick + ": relative " + actual + ", expected " + relative);
                }
                assertions++;
                lastWave = wave;
            }
        }
        player.setDeltaMovement(new Vec3(0, 0.42, 0));
        Vec3 beforeJump = player.position();
        Vec3 delta = new Vec3(0.03, 0, 0.02);
        raft.setPos(raft.position().add(delta));
        RaftPlayerSupport.carryPlayer(raft, player, delta);
        if (!player.position().equals(beforeJump)) {
            throw new IllegalStateException("Jumping player must detach from carry");
        }
        assertions++;
        if (java.util.Arrays.stream(net.minecraft.server.network.ServerGamePacketListenerImpl.class.getDeclaredMethods())
                .noneMatch(method -> method.getName().contains("raftOcean$relativePosition"))) {
            throw new IllegalStateException("Server movement frame hook is not active");
        }
        assertions++;
        player.setDeltaMovement(Vec3.ZERO);
        Vec3 relative = new Vec3(.2, RaftEntity.DECK_HEIGHT + RaftContact.CLEARANCE, -.2);
        player.setPos(raft.position().add(relative));
        Vec3 clientOrigin = raft.position().subtract(new Vec3(.12, .006, .05));
        Vec3 raw = clientOrigin.add(relative);
        var frame = new dev.raftocean.network.RaftMovementFrame(raft.getId(), clientOrigin, raw);
        var packet = new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot(raw, 90, 20, true, true);
        var rebased = dev.raftocean.network.RaftMovementSync.rebasePacket(player, packet, frame, raft);
        if (new Vec3(rebased.getX(0), rebased.getY(0), rebased.getZ(0)).distanceToSqr(player.position()) > 1.0E-12
                || rebased.getYRot(0) != 90 || rebased.getXRot(0) != 20 || !rebased.isOnGround() || !rebased.horizontalCollision()) {
            throw new IllegalStateException("Delayed raft packet must preserve relative position, rotation, and flags");
        }
        assertions++;
        var airborneRaw = raw.add(0, .5, 0);
        var airborneFrame = new dev.raftocean.network.RaftMovementFrame(raft.getId(), clientOrigin, airborneRaw);
        var airbornePacket = new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos(airborneRaw, false, false);
        if (dev.raftocean.network.RaftMovementSync.rebasePacket(player, airbornePacket, airborneFrame, raft) != airbornePacket) {
            throw new IllegalStateException("Unsupported airborne packet must retain vanilla coordinates");
        }
        assertions++;
        boolean forced=level.setChunkForced(0,0,true);
        level.getChunkSource().tick(()->true,false);
        level.waitForEntities(new net.minecraft.world.level.ChunkPos(0,0),0);
        raft.setPos(start);player.setPos(start.add(relative));
        if(!level.addFreshEntity(raft)){throw new IllegalStateException("Movement frame fixture raft registration failed");}
        try{
            var stale=new dev.raftocean.network.RaftMovementFrame(raft.getId(),start.subtract(.1,0,0),start.add(relative).subtract(.1,0,0));
            var current=new dev.raftocean.network.RaftMovementFrame(raft.getId(),start.subtract(.2,0,0),start.add(relative).subtract(.2,0,0));
            dev.raftocean.network.RaftMovementSync.accept(player,stale);
            dev.raftocean.network.RaftMovementSync.accept(player,current);
            var rotationOnly=new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(0,0,true,false);
            if(dev.raftocean.network.RaftMovementSync.consumeFrame(player,rotationOnly).isPresent()){throw new IllegalStateException("Rotation packets must not consume movement frames");}assertions++;
            var paired=new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos(current.playerPosition(),true,false);
            var consumed=dev.raftocean.network.RaftMovementSync.consumeFrame(player,paired);
            if(consumed.isEmpty() || !consumed.get().equals(current)){throw new IllegalStateException("A stale frame must not replace the matching position frame");}assertions++;
            if(dev.raftocean.network.RaftMovementSync.consumeFrame(player,paired).isPresent()){throw new IllegalStateException("Movement frames are consumed exactly once");}assertions++;
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(ModContent.structuralItem(dev.raftocean.entity.RaftPiece.Kind.WALL)));
            Vec3 aim=start.add(0,.24,-.48).subtract(player.getEyePosition());
            player.setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));
            player.setXRot((float)-Math.toDegrees(Math.atan2(aim.y,aim.horizontalDistance())));
            if(!raft.interact(player,net.minecraft.world.InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()){throw new IllegalStateException("Wall movement fixture placement failed");}
            player.setPos(start.add(0,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,-1.1));
            Vec3 beyond=start.add(0,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,.1);
            var cross=new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos(beyond,true,false);
            var crossFrame=new dev.raftocean.network.RaftMovementFrame(raft.getId(),start,beyond);
            var stopped=dev.raftocean.network.RaftMovementSync.rebasePacket(player,cross,crossFrame,raft);
            if(stopped==cross || stopped.getZ(0)>start.z-.79 || stopped.getZ(0)<player.getZ()){
                throw new IllegalStateException("Rebased motion must stop at the wall instead of crossing to a free endpoint");
            }assertions++;
        }finally{raft.discard();player.getInventory().clearContent();if(forced){level.setChunkForced(0,0,false);}}
        return assertions;
    }
}
