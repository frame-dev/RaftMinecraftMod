package dev.raftocean.mixin;

import dev.raftocean.menu.RaftEnchantingMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Add raft shelves before the native bookshelf loop, costs and NeoForge enchanting hooks. */
@Mixin(EnchantmentMenu.class)
public abstract class RaftEnchantingMixin {
    @ModifyVariable(method="lambda$slotsChanged$0",at=@At(value="STORE",ordinal=0),ordinal=0)
    private float raftOcean$bookshelfPower(float power){
        return (Object)this instanceof RaftEnchantingMenu menu?power+menu.bookshelfPower():power;
    }
}
