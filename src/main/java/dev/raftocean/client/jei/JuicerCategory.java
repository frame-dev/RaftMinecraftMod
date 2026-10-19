package dev.raftocean.client.jei;

import dev.raftocean.survival.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** The four foods, reusable serving glass and portable power are distinct recipe roles. */
public final class JuicerCategory implements IRecipeCategory<JuicingRecipe> {
    public static final IRecipeType<JuicingRecipe> TYPE=IRecipeType.create("raft_ocean","juicing",JuicingRecipe.class);
    private final IDrawable icon;
    public JuicerCategory(IGuiHelper helper){icon=helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(RaftJuicer.ITEM.get()));}
    @Override public IRecipeType<JuicingRecipe> getRecipeType(){return TYPE;}
    @Override public Component getTitle(){return Component.translatable("item.raft_ocean.raft_juicer");}
    @Override public int getWidth(){return 184;}
    @Override public int getHeight(){return 98;}
    @Override public IDrawable getIcon(){return icon;}
    @Override public void setRecipe(IRecipeLayoutBuilder builder,JuicingRecipe recipe,IFocusGroup focuses){
        var ingredients=recipe.placementInfo().ingredients();
        for(int i=0;i<ingredients.size();i++)builder.addSlot(RecipeIngredientRole.INPUT,6+i*21,20).setStandardSlotBackground().add(ingredients.get(i));
        builder.addSlot(RecipeIngredientRole.INPUT,104,20).setStandardSlotBackground().add(new ItemStack(RaftJuicer.GLASS.get()));
        builder.addSlot(RecipeIngredientRole.OUTPUT,150,20).setOutputSlotBackground().add(recipe.ingredients().result());
        var power=builder.addSlot(RecipeIngredientRole.CRAFTING_STATION,6,48).setStandardSlotBackground();
        power.add(new ItemStack(Batteries.BASIC.get()));power.add(new ItemStack(Batteries.ADVANCED.get()));
    }
    @Override public void draw(JuicingRecipe recipe,IRecipeSlotsView slots,GuiGraphicsExtractor g,double x,double y){
        var font=Minecraft.getInstance().font;g.fill(0,0,184,2,0xFF70C8D1);g.fill(2,3,182,16,0xFF152831);g.fill(28,44,182,68,0xFF152831);g.fill(2,70,182,98,0xFF152831);
        g.text(font,Component.translatable("raft_ocean.juicer.foods"),6,5,0xFFABC4C4);g.text(font,Component.literal("+"),88,24,0xFF70C8D1);g.text(font,Component.literal("→"),129,24,0xFF70C8D1);
        int seconds=(int)Math.ceil(recipe.duration()/(20*dev.raftocean.config.RaftConfig.PROCESSING.get()*dev.raftocean.config.RaftConfig.JUICER_SPEED.get()));
        g.text(font,Component.translatable("raft_ocean.jei.duration",seconds),32,48,0xFFE7B97C);
        int y0=72;for(var line:font.split(Component.translatable("raft_ocean.juicer.jei_help"),170)){g.text(font,line,6,y0,0xFFECE9D5);y0+=9;}
    }
}
