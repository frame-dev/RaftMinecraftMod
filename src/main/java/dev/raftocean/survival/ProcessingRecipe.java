package dev.raftocean.survival;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import dev.raftocean.entity.RaftPiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.*;

/** Data-pack recipes for machines; these cannot be assembled at a crafting table. */
public record ProcessingRecipe(ShapelessRecipe ingredients, int duration, List<ItemStackTemplate> byproducts,
                               RaftPiece.Kind kind) implements Recipe<CraftingInput> {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "raft_ocean");
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "raft_ocean");
    private static final java.util.Map<RaftPiece.Kind, DeferredHolder<RecipeType<?>, RecipeType<ProcessingRecipe>>> TYPES_BY_KIND =
            new java.util.EnumMap<>(RaftPiece.Kind.class);
    private static final java.util.Map<RaftPiece.Kind, DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ProcessingRecipe>>> SERIALIZERS_BY_KIND =
            new java.util.EnumMap<>(RaftPiece.Kind.class);
    static {
        for (var kind : List.of(RaftPiece.Kind.BASIC_PURIFIER, RaftPiece.Kind.DRYING_RACK, RaftPiece.Kind.PURIFIER, RaftPiece.Kind.RAIN_COLLECTOR, RaftPiece.Kind.PLANTER, RaftPiece.Kind.TREE_PLANTER, RaftPiece.Kind.COOKING_POT, RaftPiece.Kind.BIOREFINER)) {
            TYPES_BY_KIND.put(kind, TYPES.register(kind.id(), () -> RecipeType.simple(Identifier.fromNamespaceAndPath("raft_ocean", kind.id()))));
            MapCodec<ProcessingRecipe> codec = RecordCodecBuilder.mapCodec(i -> i.group(
                    ShapelessRecipe.MAP_CODEC.forGetter(ProcessingRecipe::ingredients),
                    Codec.intRange(20, 72000).fieldOf("duration").forGetter(ProcessingRecipe::duration),
                    ItemStackTemplate.CODEC.listOf(0, 2).optionalFieldOf("byproducts", List.of()).forGetter(ProcessingRecipe::byproducts))
                    .apply(i, (recipe, ticks, extra) -> new ProcessingRecipe(recipe, ticks, extra, kind)));
            StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipe> stream = StreamCodec.composite(
                    ShapelessRecipe.STREAM_CODEC, ProcessingRecipe::ingredients, ByteBufCodecs.VAR_INT, ProcessingRecipe::duration,
                    ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()), ProcessingRecipe::byproducts,
                    (recipe, ticks, extra) -> new ProcessingRecipe(recipe, ticks, extra, kind));
            SERIALIZERS_BY_KIND.put(kind, SERIALIZERS.register(kind.id(), () -> new RecipeSerializer<>(codec, stream)));
        }
    }
    public static RecipeType<ProcessingRecipe> type(RaftPiece.Kind kind) { return TYPES_BY_KIND.get(kind).get(); }
    public static void onSync(net.neoforged.neoforge.event.OnDatapackSyncEvent event) {
        event.sendRecipes(JuicingRecipe.TYPE.get());
        for(var type:TYPES_BY_KIND.values()){event.sendRecipes(type.get());}
    }
    @Override public boolean matches(CraftingInput input, Level level) { return ingredients.matches(input, level); }
    @Override public ItemStack assemble(CraftingInput input) { return ingredients.assemble(input); }
    @Override public boolean showNotification() { return false; }
    @Override public String group() { return ingredients.group(); }
    @Override public RecipeSerializer<ProcessingRecipe> getSerializer() { return SERIALIZERS_BY_KIND.get(kind).get(); }
    @Override public RecipeType<ProcessingRecipe> getType() { return type(kind); }
    @Override public PlacementInfo placementInfo() { return ingredients.placementInfo(); }
    @Override public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }
}
