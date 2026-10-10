package dev.raftocean.survival;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Authoritative hydration; serialized attachments synchronize only to their owner. */
public final class Thirst {
    private Thirst() { }
    public static int value(Player player) { return player.getData(SurvivalContent.THIRST); }
    public static void drink(Player player) {
        if (!player.level().isClientSide()) { player.setData(SurvivalContent.THIRST, Math.min(20, value(player) + dev.raftocean.config.RaftConfig.DRINK_RESTORE.get())); }
    }
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) { tick(player, 20); }
    }
    public static void tick(ServerPlayer player, int elapsed) {
        if (!player.isAlive()) { return; }
        if (!dev.raftocean.config.RaftConfig.THIRST.get()
                || !dev.raftocean.config.RaftConfig.PEACEFUL_THIRST.get() && dev.raftocean.config.RaftConfig.effectiveDifficulty(player.level().getDifficulty())==Difficulty.PEACEFUL
                || player.isSpectator() || player.getAbilities().instabuild) {
            if (value(player) != 20) { player.setData(SurvivalContent.THIRST, 20); }
            player.getPersistentData().putInt("RaftThirstTicks", 0);
            return;
        }
        int period = dev.raftocean.config.RaftConfig.THIRST_PERIOD.get();
        int accumulated = Math.clamp(player.getPersistentData().getIntOr("RaftThirstTicks", 0), 0, period-1) + (int)(Math.clamp(elapsed, 0, 1200) * (player.isSprinting() ? dev.raftocean.config.RaftConfig.SPRINT_THIRST.get() : 1));
        int lost = accumulated / period;
        player.getPersistentData().putInt("RaftThirstTicks", accumulated % period);
        if (lost > 0) { player.setData(SurvivalContent.THIRST, Math.max(0, value(player) - lost)); }
        if (value(player) == 0 && player.tickCount % 100 == 0 && dev.raftocean.config.RaftConfig.effectiveDifficulty(player.level().getDifficulty()) != Difficulty.PEACEFUL
                && player.getHealth() > 1) {
            player.hurtServer(player.level(), player.damageSources().starve(), Math.min(1, player.getHealth() - 1));
        }
    }
}
