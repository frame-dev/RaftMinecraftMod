package dev.raftocean.mixin;

import dev.raftocean.progression.ResearchRecipes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Direct shift-transfer cannot take a research result computed under an old state. */
@Mixin({CraftingMenu.class,InventoryMenu.class})
public abstract class RaftCraftingTakeMixin {
    @Inject(method="quickMoveStack",at=@At("HEAD"),cancellable=true)
    private void raftOcean$validateResult(Player player,int slot,CallbackInfoReturnable<ItemStack> callback) {
        if(slot==0 && ResearchRecipes.rejectStaleResult(player,(AbstractCraftingMenu)(Object)this)){
            callback.setReturnValue(ItemStack.EMPTY);
        }
    }
}
