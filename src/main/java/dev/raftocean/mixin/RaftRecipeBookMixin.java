package dev.raftocean.mixin;

import java.util.Collection;
import dev.raftocean.progression.ResearchRecipes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(ServerRecipeBook.class)
public abstract class RaftRecipeBookMixin {
    @ModifyVariable(method="addRecipes",at=@At("HEAD"),argsOnly=true)
    private Collection<RecipeHolder<?>> raftOcean$discoveredRecipes(Collection<RecipeHolder<?>> recipes,
            @Local(argsOnly=true) ServerPlayer player) {
        return recipes.stream().filter(holder->ResearchRecipes.allowed(player,holder)).toList();
    }
    @Inject(method="sendInitialRecipeBook",at=@At("HEAD"))
    private void raftOcean$refreshSavedBook(ServerPlayer player,CallbackInfo callback) {
        ResearchRecipes.reconcile(player,false);
    }
}
