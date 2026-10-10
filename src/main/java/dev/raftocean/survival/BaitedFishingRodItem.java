package dev.raftocean.survival;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.level.Level;

public final class BaitedFishingRodItem extends FishingRodItem {
    public BaitedFishingRodItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.fishing == null && !player.getAbilities().instabuild) {
            var bait = player.getInventory().getNonEquipmentItems().stream().filter(stack -> stack.is(SurvivalContent.BAIT.get()))
                    .findFirst().orElse(null);
            if (bait == null) {
                if (!level.isClientSide()) { player.sendOverlayMessage(Component.translatable("raft_ocean.fishing.needs_bait")); }
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide()) { bait.shrink(1); }
        }
        return super.use(level, player, hand);
    }
}
