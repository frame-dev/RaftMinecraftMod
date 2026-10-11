package dev.raftocean.story;

import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.SurvivalContent;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.*;
import net.neoforged.neoforge.attachment.AttachmentType;

public final class IslandContent {
    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE,"raft_ocean");
    public static final DeferredHolder<StructureType<?>, StructureType<IslandStructure>> ISLAND = TYPES.register("island",()->()->IslandStructure.CODEC);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE,"raft_ocean");
    public static final DeferredHolder<StructurePieceType,StructurePieceType> PIECE = PIECES.register("island",()->(context,tag)->new IslandPiece(context.structureTemplateManager(),tag));
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> JOURNEY = SurvivalContent.ATTACHMENTS.register(
            "journey",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,ReceiverItem.DESTINATIONS.length).fieldOf("stage")).copyOnDeath().build());
    public static final DeferredItem<Item> RECEIVER = ModContent.ITEMS.registerItem("radio_receiver",p->new ReceiverItem(p.stacksTo(1),false));
    public static final DeferredItem<Item> ANTENNA = ModContent.ITEMS.registerSimpleItem("antenna_upgrade");
    public static final DeferredItem<Item> LONG_RECEIVER = ModContent.ITEMS.registerItem("long_range_receiver",p->new ReceiverItem(p.stacksTo(1),true));
    public static final DeferredItem<Item> KEY = ModContent.ITEMS.registerSimpleItem("relay_key",p->p.stacksTo(16));
    public static final DeferredItem<Item> RADIO_LOG = ModContent.ITEMS.registerItem("radio_log",p->new JournalItem(p.stacksTo(1),0));
    public static final DeferredItem<Item> WATCH_LOG = ModContent.ITEMS.registerItem("watch_log",p->new JournalItem(p.stacksTo(1),1));
    public static final DeferredItem<Item> GARDEN_LOG = ModContent.ITEMS.registerItem("garden_log",p->new JournalItem(p.stacksTo(1),2));
    public static final DeferredItem<Item> VAULT_LOG = ModContent.ITEMS.registerItem("vault_log",p->new JournalItem(p.stacksTo(1),3));
    public static final DeferredItem<Item> BEACON_LOG = ModContent.ITEMS.registerItem("beacon_log",p->new JournalItem(p.stacksTo(1),4));
    public static final DeferredItem<Item> HAVEN_LOG = ModContent.ITEMS.registerItem("haven_log",p->new JournalItem(p.stacksTo(1),5));
    public static final DeferredItem<Item> STARFALL_LOG = ModContent.ITEMS.registerItem("starfall_log",p->new JournalItem(p.stacksTo(1),6));
    public static final DeferredBlock<SealedHatchBlock> HATCH = ModContent.BLOCKS.registerBlock("sealed_hatch",SealedHatchBlock::new,
            p->p.strength(-1,3600000));
    public static void register(net.neoforged.bus.api.IEventBus bus) { TYPES.register(bus);PIECES.register(bus); }
    private IslandContent() { }
}
