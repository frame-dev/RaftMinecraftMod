package dev.raftocean.salvage;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.registry.ModContent;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;

/** Bounded, server-owned salvage with persisted rolled loot and an exclusive live hook claim. */
public final class DebrisEntity extends Entity {
    public enum Kind { PLANK, LOG, BARREL, CRATE, SCRAP, LEAVES, BOTTLE }
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(DebrisEntity.class, EntityDataSerializers.INT);
    private final SimpleContainer loot = new SimpleContainer(16);
    private boolean rolled;
    private int age;
    private long born;
    private boolean driftAssigned;
    private Vec3 drift = Vec3.ZERO;
    private UUID streamRaft;
    public Vec3 drift() { return drift; }
    public void assignStream(RaftEntity raft, Vec3 forward) {
        drift = DebrisStream.drift(forward); driftAssigned = true; streamRaft = raft.getUUID();
    }
    private int dryTicks;
    private SalvageHookEntity hook;

    public DebrisEntity(EntityType<? extends DebrisEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static DebrisEntity create(ServerLevel level, Kind kind, Vec3 pos) {
        DebrisEntity entity = new DebrisEntity(ModContent.DEBRIS.get(), level);
        entity.getEntityData().set(KIND, kind.ordinal());
        entity.setPos(pos);
        entity.born = level.getGameTime();
        return entity;
    }

    public Kind kind() { return Kind.values()[Math.floorMod(getEntityData().get(KIND), Kind.values().length)]; }
    public int age() { return age; }
    public List<ItemStack> loot() {
        rollLoot();
        return loot.getItems().stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
    }

    private void rollLoot() {
        if (rolled || !(level() instanceof ServerLevel server)) { return; }
        rolled = true;
        var key = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("raft_ocean", "salvage/" + kind().name().toLowerCase(java.util.Locale.ROOT)));
        var params = new LootParams.Builder(server).withParameter(LootContextParams.ORIGIN, position()).create(LootContextParamSets.CHEST);
        var results = server.getServer().reloadableRegistries().getLootTable(key).getRandomItems(params);
        // Data packs may produce more loot than the bounded payload can hold; preserve excess as world items.
        for (ItemStack stack : results) {
            ItemStack remainder = loot.addItem(stack.copy());
            if (!remainder.isEmpty()) { spawnAtLocation(server, remainder); }
        }
    }

    public boolean claimed() {
        if (hook != null && hook.isRemoved()) { hook = null; }
        return hook != null;
    }
    public boolean claim(SalvageHookEntity owner) {
        if (isRemoved() || claimed()) { return false; }
        hook = owner;
        return true;
    }
    public void release(UUID owner) { if (hook != null && owner.equals(hook.getUUID())) { hook = null; } }

    public boolean collect(Container destination) {
        if (level().isClientSide() || isRemoved() || claimed()) { return false; }
        if (!SalvageTransfer.deposit(destination, loot())) { return false; }
        loot.clearContent();
        discard();
        return true;
    }

    public boolean collect(Player player, UUID claimant) {
        if (!(level() instanceof ServerLevel server) || isRemoved() || player.level() != level()
                || !player.isAlive() || player.isSpectator() || player.distanceToSqr(this) > 16
                || (claimed() && !hook.getUUID().equals(claimant))) { return false; }
        for (ItemStack stack : loot()) {
            player.getInventory().add(stack);
            if (!stack.isEmpty()) { player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY); }
        }
        loot.clearContent();
        discard();
        return true;
    }

    @Override public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (level().isClientSide()) { return InteractionResult.SUCCESS; }
        return collect(player, null) ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }
    @Override public boolean isPickable() { return !isRemoved(); }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(KIND, 0); }
    @Override protected InterpolationHandler createInterpolationHandler() { return LinearInterpolationHandler.create(this, 3); }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) { return; }
        age = (int) Math.min(dev.raftocean.config.RaftConfig.DEBRIS_LIFETIME.get(), Math.max(age + 1L, server.getGameTime() - born));
        if (age >= dev.raftocean.config.RaftConfig.DEBRIS_LIFETIME.get()) { discard(); return; }
        if (claimed()) { return; }
        double surface = DebrisSpawning.surface(server, getX(), getY(), getZ());
        if (!Double.isFinite(surface)) {
            if (++dryTicks > 200) { discard(); return; }
            return;
        }
        dryTicks = 0;
        if (!driftAssigned) {
            RaftEntity raft = server.getEntitiesOfClass(RaftEntity.class, getBoundingBox().inflate(48), candidate -> !candidate.isRemoved())
                    .stream().min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(this))).orElse(null);
            Vec3 course = raft == null ? new Vec3(.02, 0, .008) : DebrisStream.horizontal(raft.getDeltaMovement());
            if (course.lengthSqr() < 1.0E-6 && raft != null) { course = new Vec3(raft.getCurrentX(), 0, raft.getCurrentZ()); }
            if (course.lengthSqr() < 1.0E-6) { course = new Vec3(.02, 0, .008); }
            drift = DebrisStream.drift(course); driftAssigned = true;
            if (raft != null) { streamRaft = raft.getUUID(); }
        }
        if (age % 40 == 0 && streamRaft != null && server.getEntity(streamRaft) instanceof RaftEntity source) {
            double behind = DebrisStream.horizontal(position().subtract(source.position())).dot(drift.normalize().scale(-1));
            if (behind < -24) { discard(); return; }
        }
        boolean anchored = streamRaft != null && server.getEntity(streamRaft) instanceof RaftEntity source && source.navigation().anchored();
        Vec3 motion = anchored ? Vec3.ZERO : drift;
        Vec3 next = position().add(motion.x, Mth.clamp((surface + .025 - getY()) * .3, -.08, .08), motion.z);
        if (server.hasChunkAt(BlockPos.containing(next)) && server.getWorldBorder().isWithinBounds(BlockPos.containing(next))) {
            setDeltaMovement(next.subtract(position()));
            setPos(next);
        }
        if (age % 200 == 0 && server.players().stream().noneMatch(player -> player.distanceToSqr(this) < 128 * 128)) { discard(); }
    }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        output.putString("Kind", kind().name()); output.putInt("Age", age); output.putBoolean("Rolled", rolled); output.putLong("Born", born);
        output.putBoolean("DriftAssigned", driftAssigned); output.putDouble("DriftX", drift.x); output.putDouble("DriftZ", drift.z);
        if (streamRaft != null) { output.store("StreamRaft", net.minecraft.core.UUIDUtil.CODEC, streamRaft); }
        ContainerHelper.saveAllItems(output, loot.getItems());
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        try { getEntityData().set(KIND, Kind.valueOf(input.getStringOr("Kind", "PLANK")).ordinal()); }
        catch (IllegalArgumentException invalid) { getEntityData().set(KIND, 0); }
        age = Mth.clamp(input.getIntOr("Age", 0), 0, dev.raftocean.config.RaftConfig.DEBRIS_LIFETIME.get());
        born = input.getLongOr("Born", level().getGameTime() - age);
        rolled = input.getBooleanOr("Rolled", false);
        loot.clearContent(); ContainerHelper.loadAllItems(input, loot.getItems());
        driftAssigned = input.getBooleanOr("DriftAssigned", false);
        double dx = input.getDoubleOr("DriftX", 0), dz = input.getDoubleOr("DriftZ", 0);
        drift = Double.isFinite(dx) && Double.isFinite(dz) ? new Vec3(dx, 0, dz) : Vec3.ZERO;
        if (drift.lengthSqr() > DebrisStream.DRIFT_SPEED * DebrisStream.DRIFT_SPEED * 1.01) { drift = drift.normalize().scale(DebrisStream.DRIFT_SPEED); }
        streamRaft = input.read("StreamRaft", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        hook = null;
    }
}
