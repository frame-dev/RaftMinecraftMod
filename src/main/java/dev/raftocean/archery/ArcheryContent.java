package dev.raftocean.archery;

import dev.raftocean.registry.ModContent;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;

/** Original survival archery, using the native ammo and enchantment pipeline. */
public final class ArcheryContent {
    public static final DeferredItem<Item> BOWSTRING = ModContent.ITEMS.registerSimpleItem("bowstring");
    public static final DeferredItem<Item> SHAFT = ModContent.ITEMS.registerSimpleItem("arrow_shaft");
    public static final DeferredItem<RaftBowItem> DRIFT = bow("driftwood_bow", 192, .85F, Items.STICK);
    public static final DeferredItem<RaftBowItem> REEF = bow("reef_laminated_bow", 384, 1.1F, Items.LEATHER);
    public static final DeferredItem<RaftBowItem> STORM = bow("stormsteel_bow", 640, 1.25F, Items.IRON_INGOT);
    public static final DeferredItem<RaftArrowItem> REED = arrow("reed_arrow", 1.5);
    public static final DeferredItem<RaftArrowItem> SCRAP = arrow("scrap_arrow", 2.0);
    public static final DeferredItem<RaftArrowItem> METAL = arrow("steel_arrow", 2.5);
    public static final DeferredItem<RaftArrowItem> BROADHEAD = arrow("broadhead_arrow", 3.0);
    public static final List<DeferredItem<RaftBowItem>> BOWS = List.of(DRIFT, REEF, STORM);
    public static final List<DeferredItem<RaftArrowItem>> ARROWS = List.of(REED, SCRAP, METAL, BROADHEAD);
    private static DeferredItem<RaftBowItem> bow(String name, int durability, float speed, Item repair) {
        return ModContent.ITEMS.registerItem(name, p -> new RaftBowItem(
                p.durability(durability).enchantable(15).repairable(repair), speed));
    }
    private static DeferredItem<RaftArrowItem> arrow(String name, double damage) {
        return ModContent.ITEMS.registerItem(name, p -> new RaftArrowItem(p, damage));
    }
    public static void initialize() { }
    public static void display(Consumer<Item> output) {
        output.accept(BOWSTRING.get()); output.accept(SHAFT.get());
        BOWS.forEach(bow -> output.accept(bow.get()));
        ARROWS.forEach(arrow -> output.accept(arrow.get()));
    }
    private ArcheryContent() { }
}
