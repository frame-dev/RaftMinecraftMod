package dev.raftocean.salvage;

import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** One live cast per owner; hooks retrieve salvage, never attach to mobs or arbitrary world blocks. */
public final class SalvageHookEntity extends Entity {
    private static final EntityDataAccessor<Integer> OWNER = SynchedEntityData.defineId(SalvageHookEntity.class, EntityDataSerializers.INT);
    private UUID owner;
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private DebrisEntity target;
    private int age;
    private boolean reinforced;
    private boolean reeling;
    private boolean floating;

    public SalvageHookEntity(EntityType<? extends SalvageHookEntity> type, Level level) { super(type, level); setNoGravity(true); }
    public boolean ownedBy(Player player) { return !isRemoved() && player.getUUID().equals(owner); }
    public int ownerId() { return getEntityData().get(OWNER); }
    public double range() { return reinforced ? 40 : 24; }
    public void reel() { reeling = true; }
    public void cast(Player player, InteractionHand usedHand, boolean upgraded) {
        owner = player.getUUID(); hand = usedHand; reinforced = upgraded;
        getEntityData().set(OWNER, player.getId());
        Vec3 look = player.getLookAngle();
        setPos(player.getEyePosition().add(look.scale(.6)));
        setDeltaMovement(look.scale(upgraded ? 1.1 : .85).add(0, .15, 0));
    }

    public boolean attach(DebrisEntity debris) {
        if (target != null || debris.level() != level() || !debris.claim(this)) { return false; }
        target = debris; floating = true; setPos(debris.position()); setDeltaMovement(Vec3.ZERO);
        return true;
    }

    @Override public boolean hurtServer(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) { return false; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(OWNER, -1); }
    @Override protected InterpolationHandler createInterpolationHandler() { return LinearInterpolationHandler.create(this, 2); }
    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) { return; }
        Player player = owner == null ? null : server.getPlayerByUUID(owner);
        if (++age > 600 || player == null || !player.isAlive() || player.isSpectator()
                || player.level() != level() || player.distanceToSqr(this) > 64 * 64
                || !(player.getItemInHand(hand).getItem() instanceof GrapplingHookItem)) { discard(); return; }
        DebrisEntity debris = target;
        if (target != null && (debris == null || debris.isRemoved())) { target = null; reeling = true; }
        if (player.distanceToSqr(this) > range() * range()) { reeling = true; }
        if (reeling) {
            Vec3 toward = player.position().add(0, .65, 0).subtract(position());
            if (toward.lengthSqr() < 2.25) {
                if (debris != null) { debris.collect(player, getUUID()); }
                discard(); return;
            }
            Vec3 movement = toward.normalize().scale(reinforced ? .5 : .32);
            var obstacle = server.clip(new ClipContext(position(), position().add(movement), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (obstacle.getType() != HitResult.Type.MISS) { discard(); return; }
            setPos(position().add(movement)); setDeltaMovement(movement);
            if (debris != null) { debris.setPos(position()); }
        } else if (debris != null) {
            setPos(debris.position());
        } else {
            Vec3 movement = floating ? Vec3.ZERO : getDeltaMovement().add(0, -.006, 0);
            var candidates = server.getEntitiesOfClass(DebrisEntity.class, getBoundingBox().expandTowards(movement).inflate(.7), entity -> !entity.isRemoved() && !entity.claimed());
            Vec3 segmentEnd = position().add(movement);
            var nearest = candidates.stream().filter(entity -> entity.getBoundingBox().inflate(.65).clip(position(), segmentEnd).isPresent()
                    || entity.distanceToSqr(this) < 1).min(Comparator.comparingDouble(entity -> entity.distanceToSqr(this))).orElse(null);
            var obstruction = server.clip(new ClipContext(position(), position().add(movement), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (nearest != null && (obstruction.getType() == HitResult.Type.MISS
                    || nearest.distanceToSqr(this) < obstruction.getLocation().distanceToSqr(position()))) { attach(nearest); return; }
            if (obstruction.getType() != HitResult.Type.MISS) { reeling = true; return; }
            Vec3 next = position().add(movement);
            if (!server.hasChunkAt(BlockPos.containing(next))) { discard(); return; }
            double surface = DebrisSpawning.surface(server, next.x, next.y, next.z);
            if (Double.isFinite(surface) && next.y <= surface + .04) {
                next = new Vec3(next.x, surface + .04, next.z); floating = true; movement = Vec3.ZERO;
            }
            setPos(next); setDeltaMovement(movement);
        }
    }

    @Override public void remove(RemovalReason reason) {
        GrapplingHookItem.forget(this);
        if (target != null) { target.release(getUUID()); target = null; }
        super.remove(reason);
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) { }
    @Override protected void readAdditionalSaveData(ValueInput input) { owner = null; }
}
