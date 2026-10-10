package dev.raftocean.survival;

import com.mojang.serialization.Codec;
import dev.raftocean.registry.ModContent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;

public final class SurvivalContent {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "raft_ocean");
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> THIRST = ATTACHMENTS.register(
            "thirst", () -> AttachmentType.builder(() -> 20).serialize(Codec.intRange(0, 20).fieldOf("value"))
                    .sync((holder, player) -> holder == player, ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<RaftBeds.Home>> BED_HOME = ATTACHMENTS.register(
            "bed_home", () -> AttachmentType.builder(() -> RaftBeds.Home.EMPTY).serialize(RaftBeds.Home.CODEC).copyOnDeath().build());
    public static final DeferredItem<Item> EMPTY_CUP = ModContent.ITEMS.registerItem("empty_cup", p -> new WaterCupItem(p.stacksTo(16), false));
    public static final DeferredItem<Item> SALT_WATER = ModContent.ITEMS.registerSimpleItem("salt_water_cup", p -> p.stacksTo(16));
    public static final DeferredItem<Item> FRESH_WATER = ModContent.ITEMS.registerItem("fresh_water_cup", p -> new WaterCupItem(
            p.stacksTo(16).component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK).usingConvertsTo(EMPTY_CUP.get()), true));
    public static final DeferredItem<Item> BAIT = ModContent.ITEMS.registerSimpleItem("crumb_bait");
    public static final DeferredItem<Item> PLANTING_MIX = ModContent.ITEMS.registerSimpleItem("planting_mix");
    public static final DeferredItem<Item> STEW = ModContent.ITEMS.registerSimpleItem("sea_garden_stew", p -> p.stacksTo(16)
            .food(new FoodProperties.Builder().nutrition(8).saturationModifier(.8F).build()).usingConvertsTo(Items.BOWL));
    public static final DeferredItem<BaitedFishingRodItem> FISHING_ROD = ModContent.ITEMS.registerItem(
            "baited_fishing_rod", p -> new BaitedFishingRodItem(p.durability(128)));
    private SurvivalContent() { }
}
