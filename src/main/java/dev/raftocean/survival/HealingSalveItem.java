package dev.raftocean.survival;

import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** One salve restores three hearts after its use animation; healthy players retain every item. */
public final class HealingSalveItem extends Item {
    public HealingSalveItem(Properties properties){super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(player.getHealth()>=player.getMaxHealth()) {
            if(!level.isClientSide()){player.sendOverlayMessage(Component.translatable("raft_ocean.salve.healthy"));}
            return InteractionResult.FAIL;
        }
        return super.use(level,player,hand);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity consumer) {
        if(!level.isClientSide() && !stack.isEmpty()){consumer.heal(6);}
        return super.finishUsingItem(stack,level,consumer);
    }
}
