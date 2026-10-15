package dev.raftocean.salvage;

import dev.raftocean.entity.RaftEntity;
import java.util.LinkedHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Only loaded water near occupied rafts receives salvage; no chunk tickets or offline simulation. */
public final class DebrisSpawning {
    public static final int PER_RAFT_CAP = 24;
    public static final int LEVEL_CAP = 128;
    public static final int LIFETIME = 6000;
    private static final java.util.Map<RaftEntity, DebrisStream> STREAMS = new java.util.WeakHashMap<>();
    private DebrisSpawning() { }

    public static double surface(ServerLevel level, double x, double nearY, double z) {
        BlockPos column = BlockPos.containing(x, nearY, z);
        if (!level.hasChunkAt(column)) { return Double.NaN; }
        for (int y = Mth.floor(nearY) + 3; y >= Mth.floor(nearY) - 3; y--) {
            BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
            var water = level.getFluidState(pos);
            if (water.is(FluidTags.WATER) && level.getFluidState(pos.above()).isEmpty()
                    && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) {
                return y + water.getHeight(level, pos);
            }
        }
        return Double.NaN;
    }

    public static boolean canSpawn(int inLevel, int nearRaft) { return inLevel < dev.raftocean.config.RaftConfig.DEBRIS_LEVEL_CAP.get() && nearRaft < dev.raftocean.config.RaftConfig.DEBRIS_CAP.get(); }

    public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 20 != 0 || level.players().isEmpty() || !dev.raftocean.config.RaftConfig.DEBRIS.get()) { return; }
        var rafts = new LinkedHashSet<RaftEntity>();
        for (var player : level.players()) {
            if (!player.isSpectator()) {
                rafts.addAll(level.getEntitiesOfClass(RaftEntity.class, player.getBoundingBox().inflate(48), raft -> !raft.isRemoved()));
            }
        }
        var ready = new java.util.LinkedHashMap<RaftEntity, DebrisStream>();
        for (RaftEntity raft : rafts) {
            if (raft.navigation().anchored()) {
                var paused = STREAMS.get(raft);
                if (paused != null) { paused.pause(level.getGameTime()); }
                continue;
            }
            Vec3 course = DebrisStream.horizontal(raft.getDeltaMovement());
            if (course.lengthSqr() < 1.0E-6) { course = new Vec3(raft.getCurrentX(), 0, raft.getCurrentZ()); }
            DebrisStream stream = STREAMS.computeIfAbsent(raft, ignored -> new DebrisStream());
            if (stream.update(raft.position(), course, level.getGameTime())) { ready.put(raft, stream); }
        }
        if (Math.floorDiv(level.getGameTime(),dev.raftocean.config.RaftConfig.DEBRIS_INTERVAL.get()) == Math.floorDiv(level.getGameTime()-20,dev.raftocean.config.RaftConfig.DEBRIS_INTERVAL.get()) || ready.isEmpty()) { return; }
        int count = 0;
        for (var entity : level.getAllEntities()) { if (entity instanceof DebrisEntity && !entity.isRemoved()) { count++; } }
        if (count >= dev.raftocean.config.RaftConfig.DEBRIS_LEVEL_CAP.get()) { return; }
        for (var entry : ready.entrySet()) {
            RaftEntity raft = entry.getKey();
            Vec3 forward = entry.getValue().forward();
            Vec3 side = new Vec3(-forward.z, 0, forward.x);
            double halfWidth = Math.min(20, Math.max(8, 6 + .5 * (raft.getBoundingBox().getXsize() * Math.abs(side.x)
                    + raft.getBoundingBox().getZsize() * Math.abs(side.z))));
            int nearby = (int) level.getEntitiesOfClass(DebrisEntity.class, raft.getBoundingBox().inflate(64), debris -> !debris.isRemoved())
                    .stream().filter(debris -> DebrisStream.matchesDrift(debris.drift(), forward) && DebrisStream.incoming(debris.position().subtract(raft.position()), forward, halfWidth)).count();
            for (int attempt = 0; attempt < dev.raftocean.config.RaftConfig.DEBRIS_ATTEMPTS.get() && canSpawn(count, nearby); attempt++) {
                Vec3 point = DebrisStream.spawnPosition(raft.position(), forward, 18 + level.getRandom().nextDouble() * 12,
                        (level.getRandom().nextDouble() * 2 - 1) * halfWidth);
                double y = surface(level, point.x, raft.getY(), point.z);
                if (!Double.isFinite(y) || !level.isPositionEntityTicking(BlockPos.containing(point.x, y, point.z))
                        || !level.getWorldBorder().isWithinBounds(BlockPos.containing(point.x, y, point.z))) { continue; }
                int pick = level.getRandom().nextInt(20);
                DebrisEntity.Kind kind = pick < 7 ? DebrisEntity.Kind.PLANK : pick < 10 ? DebrisEntity.Kind.LEAVES
                        : pick < 13 ? DebrisEntity.Kind.SCRAP : pick < 15 ? DebrisEntity.Kind.LOG
                        : pick < 17 ? DebrisEntity.Kind.BARREL : pick < 19 ? DebrisEntity.Kind.CRATE : DebrisEntity.Kind.BOTTLE;
                DebrisEntity debris = DebrisEntity.create(level, kind, new Vec3(point.x, y + .025, point.z));
                debris.assignStream(raft, forward);
                if (level.addFreshEntity(debris)) { count++; nearby++; }
            }
        }
    }
}
