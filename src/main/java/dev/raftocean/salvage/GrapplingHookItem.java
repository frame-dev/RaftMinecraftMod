package dev.raftocean.salvage;

import dev.raftocean.registry.ModContent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class GrapplingHookItem extends Item {
    private static final java.util.Map<Player, SalvageHookEntity> ACTIVE = new java.util.WeakHashMap<>();
    private final boolean reinforced;
    public static SalvageHookEntity active(Player player) {
        SalvageHookEntity hook = ACTIVE.get(player);
        if (hook != null && (hook.isRemoved() || hook.level() != player.level())) { hook.discard(); ACTIVE.remove(player); return null; }
        return hook;
    }
    public static void forget(SalvageHookEntity hook) { ACTIVE.values().removeIf(value -> value == hook); }
    public GrapplingHookItem(Properties properties, boolean reinforced) { super(properties); this.reinforced = reinforced; }

    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel server)) { return InteractionResult.SUCCESS; }
        SalvageHookEntity active = active(player);
        if (active != null) {
            active.reel();
            return InteractionResult.SUCCESS_SERVER;
        }
        if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand))) { return InteractionResult.FAIL; }
        SalvageHookEntity hook = new SalvageHookEntity(ModContent.SALVAGE_HOOK.get(), server);
        hook.cast(player, hand, reinforced);
        if (!server.addFreshEntity(hook)) { return InteractionResult.FAIL; }
        ACTIVE.put(player, hook);
        server.playSound(null, player.blockPosition(), dev.raftocean.registry.ModSounds.HOOK_CAST.get(), net.minecraft.sounds.SoundSource.PLAYERS, .5F, 1F);
        player.getItemInHand(hand).hurtAndBreak(1, player, hand);
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 8);
        return InteractionResult.SUCCESS_SERVER;
    }
}
