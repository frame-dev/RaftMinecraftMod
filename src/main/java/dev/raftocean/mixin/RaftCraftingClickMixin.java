package dev.raftocean.mixin;

import dev.raftocean.progression.ResearchRecipes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Validate all native result-click actions before any inventory mutation. */
@Mixin(AbstractContainerMenu.class)
public abstract class RaftCraftingClickMixin {
    @Inject(method="clicked",at=@At("HEAD"),cancellable=true)
    private void raftOcean$validateResult(int slot,int button,ContainerInput action,Player player,CallbackInfo callback) {
        if(slot==0 && (Object)this instanceof AbstractCraftingMenu crafting
                && ResearchRecipes.rejectStaleResult(player,crafting)){callback.cancel();}
    }
}
