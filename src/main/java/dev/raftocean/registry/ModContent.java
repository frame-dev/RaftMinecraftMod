package dev.raftocean.registry;

import dev.raftocean.RaftOcean;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import net.minecraft.world.item.ItemStack;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RaftOcean.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RaftOcean.MOD_ID);
    public static final DeferredItem<Item> FOUNDATION = ITEMS.registerSimpleItem("foundation");
    public static final DeferredItem<Item> BUILDING_HAMMER = ITEMS.registerSimpleItem("building_hammer", p -> p.stacksTo(1));
    public static final DeferredItem<Item> SCRAP = ITEMS.registerSimpleItem("scrap");
    public static final DeferredItem<Item> DRIFT_LEAF = ITEMS.registerSimpleItem("drift_leaf");
    public static final DeferredItem<Item> PLANT_FIBER = ITEMS.registerSimpleItem("plant_fiber");
    public static final DeferredItem<Item> CORDAGE = ITEMS.registerSimpleItem("cordage");
    public static final DeferredItem<Item> WOVEN_MESH = ITEMS.registerSimpleItem("woven_mesh");
    public static final DeferredItem<Item> TIMBER_BRACE = ITEMS.registerSimpleItem("timber_brace");
    public static final DeferredItem<Item> SCRAP_FASTENERS = ITEMS.registerSimpleItem("scrap_fasteners");
    public static final DeferredItem<Item> METAL_HOOK_HEAD = ITEMS.registerSimpleItem("metal_hook_head");
    public static final DeferredItem<dev.raftocean.navigation.PaddleItem> PADDLE = ITEMS.registerItem(
            "paddle", p -> new dev.raftocean.navigation.PaddleItem(p.durability(192)));
    public static final DeferredItem<Item> SEALED_NOTE = ITEMS.registerSimpleItem("sealed_note", properties -> properties.stacksTo(16));
    public static final DeferredItem<dev.raftocean.salvage.GrapplingHookItem> GRAPPLING_HOOK = ITEMS.registerItem(
            "grappling_hook", properties -> new dev.raftocean.salvage.GrapplingHookItem(properties.durability(128), false));
    public static final DeferredItem<dev.raftocean.salvage.GrapplingHookItem> REINFORCED_HOOK = ITEMS.registerItem(
            "reinforced_hook", properties -> new dev.raftocean.salvage.GrapplingHookItem(properties.durability(384), true));
    private static final Map<dev.raftocean.salvage.DebrisEntity.Kind, DeferredBlock<Block>> SALVAGE_VISUALS =
            new EnumMap<>(dev.raftocean.salvage.DebrisEntity.Kind.class);
    static {
        for (var kind : dev.raftocean.salvage.DebrisEntity.Kind.values()) {
            SALVAGE_VISUALS.put(kind, BLOCKS.registerSimpleBlock("salvage_" + kind.name().toLowerCase(java.util.Locale.ROOT)));
        }
    }
    public static Block salvageVisual(dev.raftocean.salvage.DebrisEntity.Kind kind) { return SALVAGE_VISUALS.get(kind).get(); }
    private static final Map<RaftPiece.Kind, DeferredItem<Item>> STRUCTURAL_ITEMS = new EnumMap<>(RaftPiece.Kind.class);
    static {
        for (RaftPiece.Kind kind : RaftPiece.Kind.values()) {
            STRUCTURAL_ITEMS.put(kind, ITEMS.registerSimpleItem("raft_" + kind.id()));
        }
    }

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RaftOcean.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONSTRUCTION_TAB =
            CREATIVE_TABS.register("construction", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.raft_ocean.construction"))
                    .icon(() -> new ItemStack(FOUNDATION.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(FOUNDATION.get());
                        output.accept(BUILDING_HAMMER.get());
                        for (RaftPiece.Kind kind : RaftPiece.Kind.values()) { if(kind!=RaftPiece.Kind.ADDON)output.accept(structuralItem(kind)); }
                    }).build());
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SALVAGE_TAB =
            CREATIVE_TABS.register("salvage", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.raft_ocean.salvage"))
                    .icon(() -> new ItemStack(GRAPPLING_HOOK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(GRAPPLING_HOOK.get()); output.accept(REINFORCED_HOOK.get());
                        output.accept(PADDLE.get());
                        output.accept(dev.raftocean.story.IslandContent.RECEIVER.get());
                        output.accept(dev.raftocean.story.IslandContent.ANTENNA.get());
                        output.accept(dev.raftocean.story.IslandContent.LONG_RECEIVER.get());
                        output.accept(dev.raftocean.story.IslandContent.KEY.get());
                        output.accept(dev.raftocean.story.IslandContent.RADIO_LOG.get());
                        output.accept(dev.raftocean.story.IslandContent.WATCH_LOG.get());
                        output.accept(dev.raftocean.story.IslandContent.GARDEN_LOG.get());
                        output.accept(dev.raftocean.story.IslandContent.VAULT_LOG.get());
                        output.accept(dev.raftocean.story.IslandContent.BEACON_LOG.get());
                        output.accept(dev.raftocean.story.IslandContent.HAVEN_LOG.get());
                        output.accept(dev.raftocean.story.IslandContent.STARFALL_LOG.get());
                        output.accept(dev.raftocean.story.Exploration.SURVEY.get());
                        output.accept(dev.raftocean.story.Exploration.REFUGE.get());
                        output.accept(dev.raftocean.story.Exploration.ORCHARD.get());
                        output.accept(dev.raftocean.creature.CreatureContent.SPEAR.get());
                        output.accept(dev.raftocean.creature.CreatureContent.BAIT.get());
                        output.accept(SCRAP.get()); output.accept(DRIFT_LEAF.get()); output.accept(SEALED_NOTE.get());
                        output.accept(dev.raftocean.survival.SurvivalContent.EMPTY_CUP.get());
                        output.accept(dev.raftocean.survival.SurvivalContent.SALT_WATER.get());
                        output.accept(dev.raftocean.survival.SurvivalContent.FRESH_WATER.get());
                        output.accept(dev.raftocean.survival.SurvivalContent.BAIT.get());
                        output.accept(dev.raftocean.survival.SurvivalContent.PLANTING_MIX.get());
                        output.accept(dev.raftocean.survival.SurvivalContent.STEW.get());
                        output.accept(dev.raftocean.survival.SurvivalContent.FISHING_ROD.get());
                        output.accept(PLANT_FIBER.get()); output.accept(CORDAGE.get()); output.accept(WOVEN_MESH.get());
                        output.accept(TIMBER_BRACE.get()); output.accept(SCRAP_FASTENERS.get()); output.accept(METAL_HOOK_HEAD.get());
                        output.accept(structuralItem(RaftPiece.Kind.COLLECTION_NET));
                        dev.raftocean.survival.CraftingExpansion.display(output::accept);
                        output.accept(dev.raftocean.survival.DivingContent.HOOD.get());
                        output.accept(dev.raftocean.survival.DivingContent.FLIPPERS.get());
                        output.accept(dev.raftocean.survival.DivingContent.SALVE.get());
                        output.accept(dev.raftocean.survival.Automation.BIOFUEL.get());
                        output.accept(dev.raftocean.survival.Batteries.BASIC.get());
                        output.accept(dev.raftocean.survival.Batteries.ADVANCED.get());
                        dev.raftocean.archery.ArcheryContent.display(output::accept);
                    }).build());
    public static final DeferredRegister<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator>>
            CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, RaftOcean.MOD_ID);
    public static final DeferredHolder<com.mojang.serialization.MapCodec<? extends net.minecraft.world.level.chunk.ChunkGenerator>,
            com.mojang.serialization.MapCodec<dev.raftocean.world.OceanChunkGenerator>> OCEAN_GENERATOR =
            CHUNK_GENERATORS.register("ocean", () -> dev.raftocean.world.OceanChunkGenerator.CODEC);

    private static final Map<RaftPiece.Kind, DeferredBlock<Block>> STATION_VISUALS = new EnumMap<>(RaftPiece.Kind.class);
    static {
        for (var kind : RaftPiece.Kind.values()) {
            STATION_VISUALS.put(kind, BLOCKS.registerSimpleBlock("station_" + kind.id()));
        }
    }
    public static final DeferredBlock<Block> TREE_BARK = BLOCKS.registerSimpleBlock("tree_bark");
    public static final DeferredBlock<Block> TREE_LEAVES = BLOCKS.registerSimpleBlock("tree_leaves");
    public static final DeferredBlock<Block> MACHINE_WATER = BLOCKS.registerSimpleBlock("machine_water");
    public static final DeferredBlock<Block> MACHINE_EMBER = BLOCKS.registerSimpleBlock("machine_ember");
    public static final DeferredBlock<Block> MACHINE_STEAM = BLOCKS.registerSimpleBlock("machine_steam");
    public static final DeferredBlock<Block> CROP_STEM = BLOCKS.registerSimpleBlock("crop_stem");
    public static final DeferredBlock<Block> CROP_GRAIN = BLOCKS.registerSimpleBlock("crop_grain");
    public static final DeferredBlock<Block> MACHINE_FOOD = BLOCKS.registerSimpleBlock("machine_food");
    public static Block stationVisual(RaftPiece.Kind kind) { return STATION_VISUALS.get(kind).get(); }
    private static final Map<RaftPiece.Kind, DeferredBlock<Block>> DISPLAY_MODELS = new EnumMap<>(RaftPiece.Kind.class);
    static {
        for (RaftPiece.Kind kind : RaftPiece.Kind.values()) {
            DISPLAY_MODELS.put(kind, BLOCKS.registerSimpleBlock("display_" + kind.id()));
        }
    }
    public static Block displayModel(RaftPiece.Kind kind) { return DISPLAY_MODELS.get(kind).get(); }
    private static final Map<RaftPiece.Kind, java.util.List<DeferredBlock<Block>>> PART_MODELS = new EnumMap<>(RaftPiece.Kind.class);
    static {
        for (RaftPiece.Kind kind : RaftPiece.Kind.values()) {
            var parts = new java.util.ArrayList<DeferredBlock<Block>>();
            int count = new RaftPiece(kind,0,false).boxes(new net.minecraft.core.BlockPos(0,1,0)).size();
            for (int i=0;i<count;i++) { parts.add(BLOCKS.registerSimpleBlock("part_"+kind.id()+"_"+i)); }
            PART_MODELS.put(kind,java.util.List.copyOf(parts));
        }
    }
    public static Block modelPart(RaftPiece.Kind kind,int part) { return PART_MODELS.get(kind).get(part).get(); }

    public static Item structuralItem(RaftPiece.Kind kind) { return STRUCTURAL_ITEMS.get(kind).get(); }
    public static Item structuralItem(RaftPiece piece) { return piece.kind()==RaftPiece.Kind.ADDON && !piece.addonId().isEmpty()?piece.addon().item().get():structuralItem(piece.kind()); }
    public static RaftPiece.Kind structuralKind(ItemStack stack) {
        if(dev.raftocean.api.RaftBuildables.forItem(stack).isPresent())return RaftPiece.Kind.ADDON;
        for (var entry : STRUCTURAL_ITEMS.entrySet()) {
            if (entry.getKey()!=RaftPiece.Kind.ADDON && stack.is(entry.getValue().get())) { return entry.getKey(); }
        }
        for (RaftPiece.Kind kind : RaftPiece.Kind.values()) {
            Item vanilla = vanillaStation(kind);
            if (vanilla != null && stack.is(vanilla)) { return kind; }
        }
        return null;
    }
    public static Item vanillaStation(RaftPiece.Kind kind) {
        return switch (kind) {
            case CRAFTING_TABLE -> net.minecraft.world.item.Items.CRAFTING_TABLE;
            case CHEST -> net.minecraft.world.item.Items.CHEST;
            case BARREL -> net.minecraft.world.item.Items.BARREL;
            case STONECUTTER -> net.minecraft.world.item.Items.STONECUTTER;
            case SMITHING_TABLE -> net.minecraft.world.item.Items.SMITHING_TABLE;
            case GRINDSTONE -> net.minecraft.world.item.Items.GRINDSTONE;
            case LOOM -> net.minecraft.world.item.Items.LOOM;
            case CARTOGRAPHY_TABLE -> net.minecraft.world.item.Items.CARTOGRAPHY_TABLE;
            case ANVIL -> net.minecraft.world.item.Items.ANVIL;
            case ENCHANTING_TABLE -> net.minecraft.world.item.Items.ENCHANTING_TABLE;
            case ENDER_CHEST -> net.minecraft.world.item.Items.ENDER_CHEST;
            case BOOKSHELF -> net.minecraft.world.item.Items.BOOKSHELF;
            default -> null;
        };
    }
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, RaftOcean.MOD_ID);

    // Original full-cube model transformed into each owned structural shape by the renderer.
    public static final DeferredBlock<Block> RAFT_DECK = BLOCKS.registerSimpleBlock(
            "raft_deck", properties -> properties.mapColor(MapColor.WOOD).strength(2.0F));

    public static final DeferredHolder<EntityType<?>, EntityType<RaftEntity>> RAFT = ENTITY_TYPES.register(
            "raft", id -> EntityType.Builder.<RaftEntity>of(RaftEntity::new, MobCategory.MISC)
                    .sized(RaftEntity.DECK_WIDTH, RaftEntity.DECK_HEIGHT)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<dev.raftocean.salvage.DebrisEntity>> DEBRIS = ENTITY_TYPES.register(
            "debris", id -> EntityType.Builder.<dev.raftocean.salvage.DebrisEntity>of(dev.raftocean.salvage.DebrisEntity::new, MobCategory.MISC)
                    .sized(.8F, .35F).clientTrackingRange(8).updateInterval(2)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<dev.raftocean.salvage.SalvageHookEntity>> SALVAGE_HOOK = ENTITY_TYPES.register(
            "salvage_hook", id -> EntityType.Builder.<dev.raftocean.salvage.SalvageHookEntity>of(dev.raftocean.salvage.SalvageHookEntity::new, MobCategory.MISC)
                    .sized(.2F, .2F).clientTrackingRange(8).updateInterval(1).noSave()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    private ModContent() {
    }
}
