package dev.raftocean.survival;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.world.OceanRaftData;
import dev.raftocean.world.OceanSpawn;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** A moving home and cooperative five-second rest, without fabricating world bed blocks. */
public final class RaftBeds {
    private static final Map<ServerPlayer, Rest> RESTING = new WeakHashMap<>();
    private RaftBeds() { }
    public record Home(UUID raft, BlockPos cell) {
        public static final Home EMPTY = new Home(new UUID(0, 0), BlockPos.ZERO);
        public static final MapCodec<Home> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                UUIDUtil.CODEC.fieldOf("raft").forGetter(Home::raft), BlockPos.CODEC.fieldOf("cell").forGetter(Home::cell)).apply(i, Home::new));
    }
    public static InteractionResult use(RaftEntity raft, BlockPos pos, Player user) {
        if (!(user instanceof ServerPlayer player) || !dev.raftocean.menu.RaftStationMenus.valid(raft, pos, RaftPiece.Kind.BED, player) || player.isSpectator()) { return InteractionResult.FAIL; }
        if (OceanSpawn.isOcean(player.level())) {
            player.setData(SurvivalContent.BED_HOME, new Home(raft.getUUID(), pos.immutable()));
            OceanRaftData.get(player.level()).remember(player.getUUID(), raft.getUUID(), raft.position());
            player.setRespawnPosition(null, false);
            player.sendSystemMessage(Component.translatable("raft_ocean.bed.home"));
        }
        if (!player.level().canSleepThroughNights() || !player.level().isDarkOutside()) {
            player.sendOverlayMessage(Component.translatable("raft_ocean.bed.day"));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!player.level().getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
                player.getBoundingBox().inflate(8, 5, 8), monster -> monster.isAlive()).isEmpty()) {
            player.sendOverlayMessage(Component.translatable("block.minecraft.bed.not_safe"));
            return InteractionResult.FAIL;
        }
        RESTING.put(player, new Rest(raft, pos.immutable(), player.level().getGameTime(), player.getHealth()));
        player.sendSystemMessage(Component.translatable("raft_ocean.bed.rest"));
        return InteractionResult.SUCCESS_SERVER;
    }
    public static Vec3 spawnPosition(ServerPlayer player, RaftEntity raft) {
        Home home = player.getData(SurvivalContent.BED_HOME);
        if (!home.raft().equals(raft.getUUID())) { return null; }
        Vec3 center = raft.stationPosition(home.cell(), RaftPiece.Kind.BED);
        if (center == null) { return null; }
        Vec3 target = center.add(0, .23, 0);
        var body = player.getBoundingBox().move(target.subtract(player.position()));
        return player.level().noCollision(player, body) && raft.getCollisionBoxes().stream().noneMatch(body::intersects) ? target : null;
    }
    public static void onTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 20 != 0) { return; }
        tickRest(level, level.getGameTime());
    }
    public static void tickRest(ServerLevel level, long now) {
        RESTING.entrySet().removeIf(entry -> !valid(entry.getKey(), entry.getValue())
                || now - entry.getValue().since > 600);
        var eligible = level.players().stream().filter(player -> player.isAlive() && !player.isSpectator()).toList();
        int required = Math.max(1, (int)Math.ceil(eligible.size() * level.getGameRules().get(GameRules.PLAYERS_SLEEPING_PERCENTAGE) / 100.0));
        long ready = eligible.stream().filter(player -> player.isSleepingLongEnough() || RESTING.containsKey(player)
                && now - RESTING.get(player).since >= 100).count();
        if (!level.canSleepThroughNights() || !level.isDarkOutside() || ready < required) { return; }
        level.dimensionType().defaultClock().ifPresent(clock -> {
            if (level.getGameRules().get(GameRules.ADVANCE_TIME)) {
                var adjustment = net.neoforged.neoforge.event.EventHooks.onSleepFinished(level,
                        new net.neoforged.neoforge.common.util.ClockAdjustment.Marker(ClockTimeMarkers.WAKE_UP_FROM_SLEEP));
                if (adjustment != null) { adjustment.apply(level.clockManager(), clock); }
            }
        });
        for (ServerPlayer player : eligible) {
            if (RESTING.remove(player) != null) {
                player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
                player.sendOverlayMessage(Component.translatable("raft_ocean.bed.wake"));
            }
            if (player.isSleeping()) { player.stopSleepInBed(false, true); }
        }
    }
    private static boolean valid(ServerPlayer player, Rest rest) {
        Vec3 center = rest.raft.stationPosition(rest.pos, RaftPiece.Kind.BED);
        return player.isAlive() && player.level().players().contains(player) && !player.isSpectator() && !rest.raft.isRemoved() && player.level() == rest.raft.level()
                && center != null && center.distanceToSqr(player.position()) <= 9 && player.getHealth() >= rest.health;
    }
    private record Rest(RaftEntity raft, BlockPos pos, long since, float health) { }
}
