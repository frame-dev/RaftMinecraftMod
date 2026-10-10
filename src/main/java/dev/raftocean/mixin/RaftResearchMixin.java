package dev.raftocean.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.raftocean.progression.Research;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Applies to native 2x2, 3x3 and mounted crafting before the result is sent or taken. */
@Mixin(CraftingMenu.class)
public abstract class RaftResearchMixin {
    @ModifyExpressionValue(method = "slotChangedCraftingGrid", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/CraftingRecipe;assemble(Lnet/minecraft/world/item/crafting/RecipeInput;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack raftOcean$researchResult(ItemStack output, @Local(argsOnly = true) Player player,
            @Local net.minecraft.world.item.crafting.CraftingInput input) {
        int blocked=Research.blockedCraftingProject(player,output,input);
        if(blocked<0){return output;}
        if(!player.level().isClientSide()){
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.research.craft_locked",
                    net.minecraft.network.chat.Component.translatable("raft_ocean.research.topic."+Research.TOPICS[blocked].id()),
                    output.getHoverName()));
        }
        return ItemStack.EMPTY;
    }
}
