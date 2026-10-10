package dev.raftocean.progression;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

/** Recipe discovery follows learned projects, including existing saved recipe books. */
public final class ResearchRecipes {
    private record State(int knowledge,boolean enabled,boolean creative,RecipeMap recipes) { }
    private static final Map<ServerPlayer,State> LAST=new WeakHashMap<>();
    public static ItemStack output(RecipeHolder<?> holder) {
        return holder.value() instanceof ShapedRecipe shaped?shaped.assemble(CraftingInput.EMPTY)
                :holder.value() instanceof ShapelessRecipe shapeless?shapeless.assemble(CraftingInput.EMPTY):ItemStack.EMPTY;
    }
    public static boolean allowed(Player player,RecipeHolder<?> holder) {
        return Research.allowed(player,output(holder));
    }
    public static boolean gated(RecipeHolder<?> holder) {
        var output=output(holder);
        for(var topic:Research.TOPICS)if(output.is(Research.tag("research/unlocks/"+topic.id())))return true;
        return false;
    }
    public static void reconcile(ServerPlayer player,boolean notify) {
        var recipes=player.level().recipeAccess().getRecipes();
        var hidden=recipes.stream().filter(h->!allowed(player,h)).toList();
        var discovered=recipes.stream().filter(ResearchRecipes::gated).filter(h->allowed(player,h)).toList();
        var book=player.getRecipeBook();
        if(notify){book.removeRecipes(hidden,player);book.addRecipes(discovered,player);refreshCrafting(player);}
        else {hidden.forEach(h->book.remove(h.id()));discovered.forEach(h->book.add(h.id()));}
    }
    /** Refresh existing inputs when research, configuration or gamemode changes. */
    public static void refreshCrafting(ServerPlayer player) {
        player.inventoryMenu.slotsChanged(player.inventoryMenu.getSlot(1).container);
        if(player.containerMenu instanceof net.minecraft.world.inventory.AbstractCraftingMenu crafting
                && crafting!=player.inventoryMenu){crafting.slotsChanged(crafting.getSlot(1).container);}
        player.containerMenu.broadcastChanges();
    }
    /** Revalidate before taking an old result, including before the periodic refresh. */
    public static boolean rejectStaleResult(Player player,net.minecraft.world.inventory.AbstractCraftingMenu menu) {
        if(player.level().isClientSide() || Research.allowed(player,menu.getSlot(0).getItem()))return false;
        menu.slotsChanged(menu.getSlot(1).container);
        menu.broadcastChanges();
        return true;
    }
    public static void onTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || player.tickCount%20!=0)return;
        var state=new State(player.getData(Research.KNOWLEDGE),dev.raftocean.config.RaftConfig.RESEARCH.get(),
                player.getAbilities().instabuild,player.level().recipeAccess().recipeMap());
        if(!state.equals(LAST.put(player,state)))reconcile(player,true);
    }
    private ResearchRecipes() { }
}
