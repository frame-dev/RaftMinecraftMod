package dev.raftocean.survival;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BaitedFishingRodItem extends FishingRodItem {
    public BaitedFishingRodItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.fishing != null) {
            return super.use(level, player, hand);
        }

        ItemStack bait = ItemStack.EMPTY;
        if (!player.getAbilities().instabuild) {
            bait = player.getInventory().getNonEquipmentItems().stream()
                    .filter(stack -> stack.is(SurvivalContent.BAIT.get()))
                    .findFirst().orElse(ItemStack.EMPTY);
            if (bait.isEmpty()) {
                if (!level.isClientSide()) { player.sendOverlayMessage(Component.translatable("raft_ocean.fishing.needs_bait")); }
                return InteractionResult.FAIL;
            }
        }

        InteractionResult result = super.use(level, player, hand);
        if (level.isClientSide()) {
            return result;
        }

        // The native cast assigns the owner before NeoForge can reject the bobber spawn.
        var hook = player.fishing;
        if (hook == null || hook.isRemoved() || !hook.isAddedToLevel()) {
            if (hook != null) {
                hook.discard();
                player.fishing = null;
            }
            return InteractionResult.FAIL;
        }
        if (!bait.isEmpty()) {
            bait.shrink(1);
            player.getInventory().setChanged();
        }
        return result;
    }
}
