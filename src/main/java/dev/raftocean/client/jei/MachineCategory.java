package dev.raftocean.client.jei;

import dev.raftocean.entity.RaftPiece;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.ProcessingRecipe;
import java.util.List;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Searchable real datapack inputs, products, returned cups and processing conditions. */
public final class MachineCategory implements IRecipeCategory<ProcessingRecipe> {
    private final RaftPiece.Kind kind;
    private final IRecipeType<ProcessingRecipe> type;
    private final IDrawable icon;
    private final List<ItemStack> fuels;
    public MachineCategory(RaftPiece.Kind kind,IRecipeType<ProcessingRecipe> type,IGuiHelper gui){
        this.kind=kind;this.type=type;
        icon=gui.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ModContent.structuralItem(kind)));
        fuels=BuiltInRegistries.ITEM.stream().map(item->item.getDefaultInstance()).filter(stack->stack.has(DataComponents.COOKING_FUEL)).toList();
    }
    @Override public IRecipeType<ProcessingRecipe> getRecipeType(){return type;}
    @Override public Component getTitle(){return Component.translatable("item.raft_ocean.raft_"+kind.id());}
    @Override public int getWidth(){return 174;}
    @Override public int getHeight(){return 98;}
    @Override public IDrawable getIcon(){return icon;}
    @Override public void setRecipe(IRecipeLayoutBuilder builder,ProcessingRecipe recipe,IFocusGroup focuses){
        var ingredients=recipe.placementInfo().ingredients();
        for(int i=0;i<ingredients.size();i++){
            builder.addSlot(RecipeIngredientRole.INPUT,8+i*21,19).setStandardSlotBackground().add(ingredients.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT,116,19).setOutputSlotBackground().add(recipe.ingredients().result());
        for(int i=0;i<recipe.byproducts().size();i++){
            builder.addSlot(RecipeIngredientRole.OUTPUT,138+i*19,19).setStandardSlotBackground().add(recipe.byproducts().get(i));
        }
        if(kind.purifier() || kind==RaftPiece.Kind.COOKING_POT){
            var fuel=builder.addSlot(RecipeIngredientRole.INPUT,8,49).setStandardSlotBackground();
            fuels.forEach(fuel::add);
        }
    }
    @Override public void draw(ProcessingRecipe recipe,IRecipeSlotsView slots,GuiGraphicsExtractor graphics,double x,double y){
        var font=Minecraft.getInstance().font;
        graphics.fill(0,0,174,2,0xFF70C8D1);
        graphics.fill(4,3,170,16,0xFF152831);
        graphics.fill(29,44,170,68,0xFF152831);
        graphics.fill(4,69,170,97,0xFF152831);
        graphics.text(font,Component.translatable("raft_ocean.jei.inputs"),7,5,0xFFABC4C4);
        graphics.text(font,Component.literal("→"),96,23,0xFF70C8D1);
        int time=(int)(recipe.duration()/dev.raftocean.config.RaftConfig.PROCESSING.get()/20);
        graphics.text(font,Component.translatable("raft_ocean.jei.duration",time),32,48,0xFFE7A455);
        int lineY=71;
        for(var line:font.split(Component.translatable("raft_ocean.jei.condition."+kind.id()),158)){
            graphics.text(font,line,7,lineY,0xFFECE9D5);lineY+=9;
        }
        double cycle=(System.nanoTime()/1_000_000_000.0)%3/3;
        graphics.fill(32,61,166,64,0xFF213B43);graphics.fill(32,61,32+(int)(134*cycle),64,0xFF70C8D1);
    }
}
