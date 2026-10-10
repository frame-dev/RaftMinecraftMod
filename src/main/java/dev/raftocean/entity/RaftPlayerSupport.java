package dev.raftocean.entity;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.WeakHashMap;
import java.util.Locale;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Adds platform translation without seating players or replacing their normal movement. */
public final class RaftPlayerSupport {
    // Client/server have distinct Player objects. Weak keys release state after logout/respawn.
    private static final Map<Player, Frame> FRAMES = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<RaftEntity, PlatformFrame> CLIENT_ORIGINS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Player, CarryReport> REPORTS = Collections.synchronizedMap(new WeakHashMap<>());

    private static final ThreadLocal<RaftEntity> CARRYING = new ThreadLocal<>();
    public static boolean ignoringCollision(RaftEntity raft) { return CARRYING.get() == raft; }
    public static boolean externalCollisionFree(RaftEntity raft,Player player,AABB body){
        RaftEntity previous=CARRYING.get();CARRYING.set(raft);
        try{return player.level().noCollision(player,body);}
        finally{if(previous==null){CARRYING.remove();}else{CARRYING.set(previous);}}
    }
    public static boolean externalMovementFree(RaftEntity raft,Player player,AABB body,Vec3 movement) {
        RaftEntity previous=CARRYING.get();CARRYING.set(raft);
        try {
            Vec3 clipped=Entity.collideBoundingBox(player,movement,body,player.level(),
                    player.level().getEntityCollisions(player,body.expandTowards(movement)));
            return clipped.distanceToSqr(movement)<1e-12;
        } finally { if(previous==null){CARRYING.remove();}else{CARRYING.set(previous);} }
    }
    public static boolean recoverOverlap(RaftEntity raft,Player player){
        if(!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping()
                || player.getAbilities().flying || player.isFallFlying() || player.getDeltaMovement().y>.05){return false;}
        Vec3 before=player.position();
        var safe=RaftPlayerClearance.position(raft,player,before);
        if(safe.isEmpty() || safe.get().distanceToSqr(before)<1e-12){return false;}
        player.setPos(safe.get());player.recordMovement(MoverType.SHULKER,safe.get().subtract(before));
        if(player instanceof net.minecraft.server.level.ServerPlayer server){server.connection.resetPosition();}
        return true;
    }
    private static void recoverNearby(Player player){
        for(var raft:player.level().getEntitiesOfClass(RaftEntity.class,player.getBoundingBox().inflate(.2),r->!r.isRemoved())){
            recoverOverlap(raft,player);
        }
    }

    private RaftPlayerSupport() {
    }

    public static void beforeEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof RaftEntity raft && raft.level().isClientSide()) {
            // This event precedes commonTick, which performs network interpolation.
            CLIENT_ORIGINS.put(raft, new PlatformFrame(raft.position(), raft.getYRot()));
        }
    }

    public static void afterEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof RaftEntity raft && raft.level().isClientSide()) {
            PlatformFrame origin = CLIENT_ORIGINS.remove(raft);
            if (origin != null) {
                raft.refreshBounds();
                onRaftMoved(raft, raft.position().subtract(origin.position), origin.yaw);
            }
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() && !player.isLocalPlayer()) {
            return; // Other players use Minecraft's authoritative player tracking.
        }
        if (!player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.getAbilities().flying || player.isFallFlying()) {
            FRAMES.remove(player);
            return;
        }

        List<DeckSnapshot> decks = new ArrayList<>();
        recoverNearby(player);
        // Include the full predicted fall path, not just a narrow band around the feet.
        AABB search = player.getBoundingBox().expandTowards(player.getDeltaMovement()).inflate(0.3, 0.3, 0.3);
        for (RaftEntity raft : player.level().getEntitiesOfClass(
                RaftEntity.class, search)) {
            if (!raft.isRemoved()) {
                for (AABB cell : raft.getCollisionBoxes()) {
                    if (cell.intersects(search)) {
                        decks.add(new DeckSnapshot(raft, cell));
                    }
                }
            }
        }
        FRAMES.put(player, new Frame(player.getY(), decks));
    }

    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Frame frame = FRAMES.remove(player);
        if (frame == null || !player.isAlive() || player.isSpectator() || player.isPassenger()
                || player.getAbilities().flying || player.isFallFlying()) {
            return;
        }
        if (frame.decks.stream().anyMatch(deck -> deck.raft.isOnLadder(player.getBoundingBox()))) {
            return; // Vanilla climbing controls vertical movement while on a moving ladder.
        }
        DeckSnapshot landing = null;
        double landingY = Double.NEGATIVE_INFINITY;
        for (DeckSnapshot snapshot : frame.decks) {
            RaftEntity raft = snapshot.raft;
            if (raft.isRemoved() || raft.level() != player.level()) {
                continue;
            }
            // Raft cells retain their local offset while the owning entity translates.
            AABB currentCell = snapshot.bounds.move(raft.position().subtract(snapshot.origin));
            if (Math.abs(RaftTransform.difference(raft.getYRot(), snapshot.yaw)) > 1.0E-5) {
                currentCell = raft.getCollisionBoxes().stream().filter(box -> Math.abs(box.maxY - currentCellHeight(snapshot)) < .001
                        && RaftContact.overlapsDeck(player.getBoundingBox(), box)).findFirst().orElse(null);
                if (currentCell == null) { continue; }
            }
            OptionalDouble height = RaftContact.landingHeight(frame.startFeet, snapshot.bounds,
                    player.getBoundingBox(), currentCell, player.getDeltaMovement().y);
            if (height.isPresent() && height.getAsDouble() > landingY) {
                landing = snapshot;
                landingY = height.getAsDouble();
            }
        }
        if (landing != null) {
            placeOnSurface(player, landingY);
        }
        recoverNearby(player);
    }

    public static void onRaftMoved(RaftEntity raft, Vec3 delta) { onRaftMoved(raft, delta, raft.getYRot()); }

    public static void onRaftMoved(RaftEntity raft, Vec3 delta, float oldYaw) {
        if (raft.isRemoved() || delta.lengthSqr() < 1.0E-12 && Math.abs(RaftTransform.difference(raft.getYRot(), oldYaw)) < 1.0E-5) { return; }
        for (Player player : raft.level().players()) {
            if ((raft.level().isClientSide() && !player.isLocalPlayer()) || !player.isAlive() || player.isSpectator()
                    || player.isPassenger() || player.getAbilities().flying || player.isFallFlying()) { continue; }
            carryPlayer(raft, player, delta, oldYaw);
        }
    }

    public static void carryPlayer(RaftEntity raft, Player player, Vec3 delta) { carryPlayer(raft, player, delta, raft.getYRot()); }

    public static void carryPlayer(RaftEntity raft, Player player, Vec3 delta, float oldYaw) {
        if (delta.lengthSqr() >= .25) { return; }
        Vec3 oldOrigin = raft.position().subtract(delta);
        AABB support = raft.collisionBoxesAt(oldOrigin, oldYaw).stream()
                .filter(box -> RaftContact.isSupported(player.getBoundingBox(), box))
                .max(java.util.Comparator.comparingDouble(box -> box.maxY)).orElse(null);
        Vec3 localFeet = RaftTransform.local(player.position(), oldOrigin, oldYaw);
        AABB oldBody = RaftTransform.bounds(player.getBoundingBox().move(oldOrigin.scale(-1)), -oldYaw);
        boolean climbing = raft.pieces().entrySet().stream().anyMatch(e -> e.getValue().kind().ladder()
                && e.getValue().climbBox(e.getKey()).intersects(oldBody));
        if ((support == null || player.getDeltaMovement().y > .05) && !climbing) {
            REPORTS.put(player, new CarryReport(raft.getId(), raft.level().getGameTime(), delta, Vec3.ZERO, "no deck contact or jumping"));
            return;
        }
        Vec3 before = player.position();
        Vec3 target = raft.toWorld(localFeet);
        double surfaceY = support == null ? target.y : support.maxY + delta.y + RaftContact.CLEARANCE;
        Vec3 desired = new Vec3(target.x - before.x, climbing ? target.y - before.y : surfaceY - before.y, target.z - before.z);
        Vec3 clipped;
        CARRYING.set(raft);
        try {
            clipped = Entity.collideBoundingBox(player, desired, player.getBoundingBox(), player.level(),
                    player.level().getEntityCollisions(player, player.getBoundingBox().expandTowards(desired)));
        } finally { CARRYING.remove(); }
        Vec3 candidate=before.add(clipped);
        var safe=RaftPlayerClearance.position(raft,player,candidate);
        if(safe.isEmpty()){
            REPORTS.put(player,new CarryReport(raft.getId(),raft.level().getGameTime(),desired,Vec3.ZERO,"no supported clear carry destination"));
            recoverOverlap(raft,player);return;
        }
        player.setPos(safe.get());
        player.recordMovement(MoverType.SHULKER, safe.get().subtract(before));
        float turn = (float)RaftTransform.difference(raft.getYRot(), oldYaw);
        player.setYRot(player.getYRot() + turn);
        player.yRotO += turn;
        if (!climbing && raft.getCollisionBoxes().stream().anyMatch(box -> RaftContact.isSupported(player.getBoundingBox(), box))) {
            placeOnSurface(player, surfaceY);
        }
        REPORTS.put(player, new CarryReport(raft.getId(), raft.level().getGameTime(), desired, player.position().subtract(before), "collision-checked rotating carry"));
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) { serverPlayer.connection.resetPosition(); }
    }

    private static double currentCellHeight(DeckSnapshot snapshot) {
        return snapshot.bounds.maxY + snapshot.raft.getY() - snapshot.origin.y;
    }

    public static String supportReport(Player player, RaftEntity raft) {
        CarryReport report = REPORTS.get(player);
        Vec3 relative = player.position().subtract(raft.position());
        boolean supported = raft.getCollisionBoxes().stream()
                .anyMatch(cell -> RaftContact.isSupported(player.getBoundingBox(), cell));
        String latest = report != null && report.raftId == raft.getId()
                ? String.format(Locale.ROOT, "last carry %d ticks ago: requested %s, applied %s (%s)",
                        player.level().getGameTime() - report.tick, report.requested, report.applied, report.reason)
                : "no carry recorded for this raft";
        return String.format(Locale.ROOT,
                "Raft support: contact=%s; flying=%s; feet gap=%.4f; relative X=%.3f Z=%.3f; vertical velocity=%.4f; %s",
                supported, player.getAbilities().flying, player.getY() - raft.getDeckY(),
                relative.x, relative.z, player.getDeltaMovement().y, latest)
                + (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                        ? "; rebased packets=" + dev.raftocean.network.RaftMovementSync.rebasedCount(serverPlayer) : "");
    }

    private static boolean placeOnSurface(Player player, double feetY) {
        AABB destination = player.getBoundingBox().move(0.0, feetY - player.getY(), 0.0);
        if (!player.level().noCollision(player, destination)) {
            return false; // Never lift the player into a ceiling or adjacent solid block.
        }
        Vec3 previousPosition = player.position();
        player.setPos(player.getX(), feetY, player.getZ());
        player.recordMovement(MoverType.SHULKER, player.position().subtract(previousPosition));
        // Native tickPlayer restores firstGood after doTick. Keep that baseline in sync
        // with our post-tick landing so it cannot put the player back inside the deck.
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.resetPosition();
        }
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(new Vec3(velocity.x, 0.0, velocity.z));
        player.verticalCollision = true;
        player.verticalCollisionBelow = true;
        player.setOnGround(true);
        player.resetFallDistance();
        return true;
    }

    private record PlatformFrame(Vec3 position, float yaw) { }

    private record DeckSnapshot(RaftEntity raft, AABB bounds, Vec3 origin, float yaw) {
        private DeckSnapshot(RaftEntity raft, AABB bounds) {
            this(raft, bounds, raft.position(), raft.getYRot());
        }
    }

    private record Frame(double startFeet, List<DeckSnapshot> decks) {
    }

    private record CarryReport(int raftId, long tick, Vec3 requested, Vec3 applied, String reason) {
    }

}
