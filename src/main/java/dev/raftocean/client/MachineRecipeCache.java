package dev.raftocean.client;

import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

/** Client recipe data remains available without loading any optional JEI classes. */
@EventBusSubscriber(modid="raft_ocean", value=Dist.CLIENT)
public final class MachineRecipeCache {
    private MachineRecipeCache() { }
    public static RecipeMap recipes=RecipeMap.EMPTY;
    public static Runnable changed=()->{};
    @SubscribeEvent public static void onRecipes(RecipesReceivedEvent event) {
        recipes=event.getRecipeMap(); changed.run();
    }
    @SubscribeEvent public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes=RecipeMap.EMPTY;
    }
}
