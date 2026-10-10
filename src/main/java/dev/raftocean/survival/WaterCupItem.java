package dev.raftocean.survival;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.HitResult;

public final class WaterCupItem extends Item {
    private final boolean drinkable;
    public WaterCupItem(Properties properties, boolean drinkable) { super(properties); this.drinkable = drinkable; }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (drinkable) { return super.use(level, player, hand); }
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), dev.raftocean.registry.ModSounds.CUP_FILL.get(), net.minecraft.sounds.SoundSource.PLAYERS, .5F, 1F);
            return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(CupTransfers.replaceOne(player, hand,
                    new ItemStack(SurvivalContent.SALT_WATER.get())));
        }
        return InteractionResult.SUCCESS;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && drinkable && !stack.isEmpty() && entity instanceof Player player) {
            Thirst.drink(player);
            if (!level.isClientSide()) { level.playSound(null, player.blockPosition(), dev.raftocean.registry.ModSounds.CUP_DRINK.get(), net.minecraft.sounds.SoundSource.PLAYERS, .45F, 1F); }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
