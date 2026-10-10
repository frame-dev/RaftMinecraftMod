package dev.raftocean.archery;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Native arrow entity preserves the original custom pickup stack, including save/load. */
public final class RaftArrowItem extends ArrowItem {
    private final double damage;
    public RaftArrowItem(Properties properties, double damage) { super(properties); this.damage = damage; }
    public double damage() { return damage; }
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<net.minecraft.network.chat.Component> output,
            net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, output, flag);
        output.accept(net.minecraft.network.chat.Component.translatable("raft_ocean.archery.arrow_damage", damage)
                .withStyle(net.minecraft.ChatFormatting.AQUA));
    }
    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity owner, @Nullable ItemStack weapon) {
        var arrow = super.createArrow(level, ammo, owner, weapon);
        arrow.setBaseDamage(damage); return arrow;
    }
    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack ammo, Direction direction) {
        var arrow = (AbstractArrow) super.asProjectile(level, position, ammo, direction);
        arrow.setBaseDamage(damage); return arrow;
    }
}
