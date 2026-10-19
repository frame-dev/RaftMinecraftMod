package dev.raftocean.survival;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Native shapeless four-food recipes, synchronized to clients and JEI. */
public record JuicingRecipe(ShapelessRecipe ingredients,int duration) implements Recipe<CraftingInput> {
    public static final DeferredHolder<RecipeType<?>,RecipeType<JuicingRecipe>> TYPE=ProcessingRecipe.TYPES.register("juicing",()->RecipeType.simple(Identifier.fromNamespaceAndPath("raft_ocean","juicing")));
    private static final MapCodec<JuicingRecipe> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
            ShapelessRecipe.MAP_CODEC.forGetter(JuicingRecipe::ingredients),Codec.intRange(20,24000).fieldOf("duration").forGetter(JuicingRecipe::duration)).apply(i,JuicingRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<JuicingRecipe>> SERIALIZER=ProcessingRecipe.SERIALIZERS.register("juicing",()->new RecipeSerializer<>(CODEC,
            StreamCodec.composite(ShapelessRecipe.STREAM_CODEC,JuicingRecipe::ingredients,ByteBufCodecs.VAR_INT,JuicingRecipe::duration,JuicingRecipe::new)));
    public static void initialize() { }
    @Override public boolean matches(CraftingInput input,Level level){return input.ingredientCount()==4 && ingredients.placementInfo().ingredients().size()==4 && ingredients.matches(input,level);}
    @Override public ItemStack assemble(CraftingInput input){return ingredients.assemble(input);}
    @Override public boolean showNotification(){return false;}
    @Override public String group(){return ingredients.group();}
    @Override public RecipeSerializer<JuicingRecipe> getSerializer(){return SERIALIZER.get();}
    @Override public RecipeType<JuicingRecipe> getType(){return TYPE.get();}
    @Override public PlacementInfo placementInfo(){return ingredients.placementInfo();}
    @Override public RecipeBookCategory recipeBookCategory(){return RecipeBookCategories.CRAFTING_MISC;}
}
