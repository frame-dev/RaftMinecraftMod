package dev.raftocean.survival;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Server-owned hydration and a temporary bonus; native consumption returns a reusable glass. */
public final class JuiceDrinkItem extends Item {
    private final Holder<MobEffect> bonus;
    public JuiceDrinkItem(Properties properties,Holder<MobEffect> bonus){super(properties);this.bonus=bonus;}
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity consumer){
        if(!level.isClientSide() && !stack.isEmpty() && consumer instanceof Player player){
            player.setData(SurvivalContent.THIRST,Math.min(20,Thirst.value(player)+dev.raftocean.config.RaftConfig.JUICE_HYDRATION.get()));
            if(dev.raftocean.config.RaftConfig.MEAL_BONUSES.get())player.addEffect(new MobEffectInstance(bonus,dev.raftocean.config.RaftConfig.MEAL_DURATION.get(),0));
        }
        return super.finishUsingItem(stack,level,consumer);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> builder,TooltipFlag flag){
        super.appendHoverText(stack,context,display,builder,flag);
        if(dev.raftocean.config.RaftConfig.SPEC.isLoaded()){
            builder.accept(Component.translatable("raft_ocean.juicer.drink_help",dev.raftocean.config.RaftConfig.JUICE_HYDRATION.get()));
            if(dev.raftocean.config.RaftConfig.MEAL_BONUSES.get())builder.accept(Component.translatable("raft_ocean.juicer.bonus."+net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(this).getPath(),dev.raftocean.config.RaftConfig.MEAL_DURATION.get()/20));
        }
    }
}
