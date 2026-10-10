package dev.raftocean.survival;

import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/** Original cooked meals grant one temporary server-owned bonus; repeated meals refresh it. */
public final class PreparedMealItem extends Item {
    private final Holder<MobEffect> bonus;
    private final String name;
    public PreparedMealItem(Properties properties,String name) {
        super(properties);this.name=name;
        bonus=switch(name) {
            case "garden_broth" -> MobEffects.SPEED;
            case "mushroom_chowder" -> MobEffects.WATER_BREATHING;
            case "sea_grain_bowl" -> MobEffects.HASTE;
            case "hearty_fish_pot" -> MobEffects.ABSORPTION;
            case "reef_stew" -> MobEffects.RESISTANCE;
            case "orchard_compote" -> MobEffects.SPEED;
            case "seafarer_supper" -> MobEffects.NIGHT_VISION;
            default -> throw new IllegalArgumentException("Unknown prepared meal: "+name);
        };
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity consumer) {
        if(!level.isClientSide() && !stack.isEmpty() && dev.raftocean.config.RaftConfig.MEAL_BONUSES.get()) {
            consumer.addEffect(new MobEffectInstance(bonus,dev.raftocean.config.RaftConfig.MEAL_DURATION.get(),0));
        }
        return super.finishUsingItem(stack,level,consumer);
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,
            Consumer<Component> builder,TooltipFlag flag) {
        super.appendHoverText(stack,context,display,builder,flag);
        if(dev.raftocean.config.RaftConfig.SPEC.isLoaded() && dev.raftocean.config.RaftConfig.MEAL_BONUSES.get()) {
            builder.accept(Component.translatable("raft_ocean.meal."+name,dev.raftocean.config.RaftConfig.MEAL_DURATION.get()/20));
        }
    }
}
