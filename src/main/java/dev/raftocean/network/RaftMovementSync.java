package dev.raftocean.network;

import dev.raftocean.entity.RaftContact;
import dev.raftocean.entity.RaftEntity;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Reconcile client-interpolated raft coordinates before vanilla validates movement. */
public final class RaftMovementSync {
    private static final Map<ServerPlayer, java.util.ArrayDeque<Pending>> PENDING = new WeakHashMap<>();
    private static final Map<ServerPlayer, Integer> REBASED = Collections.synchronizedMap(new WeakHashMap<>());
    private RaftMovementSync() { }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("raft-movement-13.2").executesOn(net.neoforged.neoforge.network.registration.HandlerThread.NETWORK)
                .playToServer(RaftMovementFrame.TYPE, RaftMovementFrame.STREAM_CODEC,
                (frame, context) -> {
                    if (context.player() instanceof ServerPlayer player) { accept(player, frame); }
                });
    }

    public static void accept(ServerPlayer player, RaftMovementFrame frame) {
        // The network thread only buffers immutable data. Entity/world validation happens on the server thread.
        if (frame.raftId() >= 0 && finite(frame.origin()) && finite(frame.playerPosition()) && Float.isFinite(frame.yaw())) {
            synchronized(PENDING){
                var queue=PENDING.computeIfAbsent(player,key->new java.util.ArrayDeque<>());
                while(queue.size()>=32){queue.removeFirst();}
                queue.addLast(new Pending(frame,System.nanoTime()));
            }
        }
    }

    public static ServerboundMovePlayerPacket rebase(ServerPlayer player, ServerboundMovePlayerPacket packet) {
        if(!packet.hasPosition() || !eligible(player)){return packet;}
        var frame=consumeFrame(player,packet);
        if(frame.isEmpty() || !(player.level().getEntity(frame.get().raftId()) instanceof RaftEntity raft) || raft.isRemoved()){
            return packet;
        }
        return rebasePacket(player,packet,frame.get(),raft);
    }

    /** Match and consume only a positional packet's immutable frame, before inspecting world state. */
    public static Optional<RaftMovementFrame> consumeFrame(ServerPlayer player,ServerboundMovePlayerPacket packet){
        if(!packet.hasPosition()){return Optional.empty();}
        Pending pending=null;
        Vec3 raw=new Vec3(packet.getX(0),packet.getY(0),packet.getZ(0));
        synchronized(PENDING){
            var queue=PENDING.get(player);
            if(queue!=null){
                while(!queue.isEmpty()){
                    var candidate=queue.removeFirst();
                    if(System.nanoTime()-candidate.tick>1_000_000_000L){continue;}
                    if(raw.distanceToSqr(candidate.frame.playerPosition())<1E-12){pending=candidate;break;}
                }
                if(queue.isEmpty()){PENDING.remove(player);}
            }
        }
        return pending==null?Optional.empty():Optional.of(pending.frame);
    }

    public static ServerboundMovePlayerPacket rebasePacket(ServerPlayer player, ServerboundMovePlayerPacket packet,
            RaftMovementFrame frame, RaftEntity raft) {
        if (!eligible(player) || !packet.hasPosition() || raft.isRemoved() || raft.level() != player.level()
                || frame.raftId() != raft.getId()) { return packet; }
        Vec3 raw = new Vec3(packet.getX(0), packet.getY(0), packet.getZ(0));
        Optional<Vec3> rebased = rebasePosition(raw, frame, raft.position(), raft.getYRot());
        if (rebased.isEmpty() || rebased.get().distanceToSqr(player.position()) > 2.25) { return packet; }
        Vec3 target = rebased.get();
        AABB body = player.getBoundingBox().move(target.subtract(player.position()));
        // The actual server raft must support the reported feet or provide a ladder.
        if (!raft.isOnLadder(body) && raft.getCollisionBoxes().stream().noneMatch(box -> RaftContact.isSupported(body, box))) {
            return packet;
        }
        // A free endpoint can lie on the other side of a wall. Clip the complete path before vanilla checks it.
        Vec3 movement=target.subtract(player.position());
        var collisions=new java.util.ArrayList<net.minecraft.world.phys.shapes.VoxelShape>(
                player.level().getEntityCollisions(player,player.getBoundingBox().expandTowards(movement)));
        // Include the selected raft explicitly, including during chunk tracking transitions.
        raft.getCollisionBoxes().stream().filter(box->box.intersects(player.getBoundingBox().expandTowards(movement)))
                .map(net.minecraft.world.phys.shapes.Shapes::create).forEach(collisions::add);
        Vec3 clipped=net.minecraft.world.entity.Entity.collideBoundingBox(player,movement,player.getBoundingBox(),player.level(),collisions);
        target=player.position().add(clipped);
        var clear=dev.raftocean.entity.RaftPlayerClearance.position(raft,player,target);
        if(clear.isEmpty() || clear.get().distanceToSqr(player.position())>2.25){return packet;}
        target=clear.get();
        REBASED.merge(player, 1, Integer::sum);
        // Rotation, on-ground state, collision checks, speed checks, and teleport
        // acknowledgement still go through the unchanged vanilla handler.
        return packet.hasRotation()
                ? new ServerboundMovePlayerPacket.PosRot(target, packet.getYRot(0) + (float)dev.raftocean.entity.RaftTransform.difference(raft.getYRot(), frame.yaw()), packet.getXRot(0), packet.isOnGround(), packet.horizontalCollision())
                : new ServerboundMovePlayerPacket.Pos(target, packet.isOnGround(), packet.horizontalCollision());
    }

    public static Optional<Vec3> rebasePosition(Vec3 raw, RaftMovementFrame frame, Vec3 serverOrigin) {
        return rebasePosition(raw, frame, serverOrigin, 0);
    }

    public static Optional<Vec3> rebasePosition(Vec3 raw, RaftMovementFrame frame, Vec3 serverOrigin, float serverYaw) {
        if (!Float.isFinite(frame.yaw()) || !Float.isFinite(serverYaw)
                || Math.abs(dev.raftocean.entity.RaftTransform.difference(serverYaw, frame.yaw())) > 15) { return Optional.empty(); }
        if (!finite(raw) || !finite(frame.origin()) || !finite(frame.playerPosition()) || !finite(serverOrigin)
                || raw.distanceToSqr(frame.playerPosition()) > 1.0E-12
                || serverOrigin.distanceToSqr(frame.origin()) > 4.0) { return Optional.empty(); }
        return Optional.of(dev.raftocean.entity.RaftTransform.world(
                dev.raftocean.entity.RaftTransform.local(raw, frame.origin(), frame.yaw()), serverOrigin, serverYaw));
    }

    public static RaftMovementFrame clientFrame(Player player) {
        if (eligible(player)) {
            RaftEntity raft = player.level().getEntitiesOfClass(RaftEntity.class, player.getBoundingBox().inflate(.2),
                    candidate -> !candidate.isRemoved() && (candidate.isOnLadder(player.getBoundingBox())
                            || candidate.getCollisionBoxes().stream().anyMatch(box -> RaftContact.isSupported(player.getBoundingBox(), box))))
                    .stream().min(java.util.Comparator.comparingDouble(candidate -> candidate.distanceToSqr(player))).orElse(null);
            if (raft != null) { return new RaftMovementFrame(raft.getId(), raft.position(), player.position(), raft.getYRot()); }
        }
        return new RaftMovementFrame(-1, Vec3.ZERO, player.position());
    }

    public static int rebasedCount(ServerPlayer player) { return REBASED.getOrDefault(player, 0); }

    private static boolean eligible(Player player) {
        return player.isAlive() && !player.isSpectator() && !player.isPassenger()
                && !player.getAbilities().flying && !player.isFallFlying() && !player.isSleeping();
    }

    private static boolean finite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    private record Pending(RaftMovementFrame frame, long tick) { }
}

