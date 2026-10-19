package dev.raftocean.client.jei;

import dev.raftocean.client.MachineRecipeCache;
import dev.raftocean.client.SurvivalScreen;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.ProcessingRecipe;
import java.util.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Discovered by JEI only; dedicated servers and clients without JEI do not load this class. */
@JeiPlugin
public final class RaftJeiPlugin implements IModPlugin {
    private static final org.slf4j.Logger LOGGER=com.mojang.logging.LogUtils.getLogger();
    public static final List<RaftPiece.Kind> KINDS=List.of(RaftPiece.Kind.BASIC_PURIFIER,RaftPiece.Kind.DRYING_RACK,RaftPiece.Kind.PURIFIER,RaftPiece.Kind.RAIN_COLLECTOR,
            RaftPiece.Kind.PLANTER,RaftPiece.Kind.TREE_PLANTER,RaftPiece.Kind.COOKING_POT,RaftPiece.Kind.BIOREFINER);
    private final Map<RaftPiece.Kind,IRecipeType<ProcessingRecipe>> types=new EnumMap<>(RaftPiece.Kind.class);
    private final Map<RaftPiece.Kind,List<ProcessingRecipe>> shown=new EnumMap<>(RaftPiece.Kind.class);
    private List<dev.raftocean.survival.JuicingRecipe> shownJuice=List.of();
    private IJeiRuntime runtime;
    private final Set<net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>> hiddenCrafting=new HashSet<>();
    public RaftJeiPlugin() {
        KINDS.forEach(kind->types.put(kind,IRecipeType.create("raft_ocean",kind.id(),ProcessingRecipe.class)));
    }
    @Override public Identifier getPluginUid(){return Identifier.fromNamespaceAndPath("raft_ocean","machines");}
    @Override public void registerCategories(IRecipeCategoryRegistration registration){
        registration.addRecipeCategories(new JuicerCategory(registration.getJeiHelpers().getGuiHelper()));
        for(var kind:KINDS){registration.addRecipeCategories(new MachineCategory(kind,types.get(kind),registration.getJeiHelpers().getGuiHelper()));}
    }
    private List<ProcessingRecipe> recipes(RaftPiece.Kind kind){
        return MachineRecipeCache.recipes.byType(ProcessingRecipe.type(kind)).stream().map(holder->holder.value()).toList();
    }
    private List<dev.raftocean.survival.JuicingRecipe> juices(){return MachineRecipeCache.recipes.byType(dev.raftocean.survival.JuicingRecipe.TYPE.get()).stream().map(h->h.value()).toList();}
    @Override public void registerRecipes(IRecipeRegistration registration){
        shownJuice=juices();registration.addRecipes(JuicerCategory.TYPE,shownJuice);
        registration.addIngredientInfo(dev.raftocean.survival.RaftJuicer.ITEM.get(),Component.translatable("raft_ocean.juicer.jei_help"));
        for(var kind:KINDS){var list=recipes(kind);shown.put(kind,list);registration.addRecipes(types.get(kind),list);}
        for(var kind:RaftPiece.Kind.values()){
            if(kind.machine()){registration.addIngredientInfo(ModContent.structuralItem(kind),Component.translatable("raft_ocean.jei.help."+kind.id()));}
        }
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration){
        registration.addCraftingStation(JuicerCategory.TYPE,dev.raftocean.survival.RaftJuicer.ITEM.get());
        for(var kind:KINDS){registration.addCraftingStation(types.get(kind),ModContent.structuralItem(kind));}
        registration.addCraftingStation(RecipeTypes.CAMPFIRE_COOKING,ModContent.structuralItem(RaftPiece.Kind.GRILL));
        registration.addCraftingStation(RecipeTypes.CAMPFIRE_COOKING,ModContent.structuralItem(RaftPiece.Kind.BASIC_GRILL));
        registration.addCraftingStation(RecipeTypes.SMELTING,ModContent.structuralItem(RaftPiece.Kind.SMELTER));
        registration.addCraftingStation(RecipeTypes.SMELTING_FUEL,ModContent.structuralItem(RaftPiece.Kind.SMELTER));
        registration.addCraftingStation(RecipeTypes.CRAFTING,ModContent.structuralItem(RaftPiece.Kind.CRAFTING_TABLE));
        registration.addCraftingStation(RecipeTypes.CRAFTING,ModContent.structuralItem(RaftPiece.Kind.WIDE_CRAFTING_TABLE));
        registration.addCraftingStation(RecipeTypes.STONECUTTING,ModContent.structuralItem(RaftPiece.Kind.STONECUTTER));
        registration.addCraftingStation(RecipeTypes.SMITHING,ModContent.structuralItem(RaftPiece.Kind.SMITHING_TABLE));
        registration.addCraftingStation(RecipeTypes.ANVIL,ModContent.structuralItem(RaftPiece.Kind.ANVIL));
    }
    @Override public void onRuntimeAvailable(IJeiRuntime runtime){
        this.runtime=runtime;MachineRecipeCache.changed=this::refresh;
        dev.raftocean.client.ResearchRecipeVisibility.changed=this::refreshResearch;
        refresh();
        LOGGER.info("Raft Ocean JEI: nine processing categories and machine recipe links are available.");
    }
    @Override public void registerGuiHandlers(IGuiHandlerRegistration registration){
        registration.addGuiContainerHandler(dev.raftocean.client.JuicerScreen.class,new IGuiContainerHandler<dev.raftocean.client.JuicerScreen>(){
            @Override public Collection<IGuiClickableArea> getGuiClickableAreas(dev.raftocean.client.JuicerScreen screen,double x,double y){return List.of(IGuiClickableArea.createBasic(12,75,206,6,JuicerCategory.TYPE));}
        });
        registration.addGuiContainerHandler(SurvivalScreen.class,new IGuiContainerHandler<SurvivalScreen>() {
            @Override public Collection<IGuiClickableArea> getGuiClickableAreas(SurvivalScreen screen,double x,double y){
                if(screen.getMenu().suppliesPage()){return List.of();}
                var kind=screen.getMenu().kind();
                IRecipeType<?> type=switch(kind){
                    case BASIC_GRILL, GRILL -> RecipeTypes.CAMPFIRE_COOKING;
                    case SMELTER -> RecipeTypes.SMELTING;
                    default -> types.get(kind);
                };
                return type==null?List.of():List.of(IGuiClickableArea.createBasic(64,78,74,21,type));
            }
        });
    }
    private void refresh(){
        if(runtime==null){return;}
        refreshResearch();
        var currentJuice=juices();
        if(!shownJuice.equals(currentJuice)){
            runtime.getRecipeManager().hideRecipes(JuicerCategory.TYPE,shownJuice);
            runtime.getRecipeManager().addRecipes(JuicerCategory.TYPE,currentJuice);
            runtime.getRecipeManager().unhideRecipes(JuicerCategory.TYPE,currentJuice);shownJuice=currentJuice;
        }
        for(var kind:KINDS){
            var previous=shown.getOrDefault(kind,List.of());var current=recipes(kind);
            if(previous.equals(current)){continue;}
            runtime.getRecipeManager().hideRecipes(types.get(kind),previous);
            runtime.getRecipeManager().addRecipes(types.get(kind),current);
            runtime.getRecipeManager().unhideRecipes(types.get(kind),current);
            shown.put(kind,current);
            LOGGER.info("Raft Ocean JEI: synchronized {} {} recipes.",current.size(),kind.id());
        }
    }
    private void refreshResearch(){
        if(runtime==null)return;
        var player=net.minecraft.client.Minecraft.getInstance().player;
        var current=new HashSet<net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe>>();
        for(var holder:MachineRecipeCache.recipes.byType(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
            if(dev.raftocean.progression.ResearchRecipes.gated(holder) && (player==null || !dev.raftocean.progression.ResearchRecipes.allowed(player,holder)))current.add(holder);
        }
        var reveal=hiddenCrafting.stream().filter(h->!current.contains(h)).toList();
        var hide=current.stream().filter(h->!hiddenCrafting.contains(h)).toList();
        if(!reveal.isEmpty())runtime.getRecipeManager().unhideRecipes(RecipeTypes.CRAFTING,reveal);
        if(!hide.isEmpty())runtime.getRecipeManager().hideRecipes(RecipeTypes.CRAFTING,hide);
        hiddenCrafting.clear();hiddenCrafting.addAll(current);
    }
    @Override public void onRuntimeUnavailable(){runtime=null;MachineRecipeCache.changed=()->{};dev.raftocean.client.ResearchRecipeVisibility.changed=()->{};hiddenCrafting.clear();shown.clear();shownJuice=List.of();}
}
