package dev.raftocean.archery;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/** Only modifies launch speed; native release handles ammo, hooks, wear and enchantments. */
public final class RaftBowItem extends BowItem {
    private final float speedMultiplier;
    public RaftBowItem(Properties properties, float speedMultiplier) {
        super(properties); this.speedMultiplier = speedMultiplier;
    }
    public float speedMultiplier() { return speedMultiplier; }
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<net.minecraft.network.chat.Component> output,
            net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, output, flag);
        output.accept(net.minecraft.network.chat.Component.translatable("raft_ocean.archery.bow_speed", Math.round(speedMultiplier * 100))
                .withStyle(net.minecraft.ChatFormatting.AQUA));
    }
    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index,
            float power, float uncertainty, float angle, @Nullable LivingEntity target) {
        super.shootProjectile(shooter, projectile, index, power * speedMultiplier, uncertainty, angle, target);
    }
    @Override
    public ItemStack getDefaultCreativeAmmo(@Nullable Player player, ItemStack weapon) {
        return ArcheryContent.REED.get().getDefaultInstance();
    }
}
