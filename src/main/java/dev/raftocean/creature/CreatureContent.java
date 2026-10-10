package dev.raftocean.creature;

import dev.raftocean.RaftOcean;
import dev.raftocean.registry.ModContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class CreatureContent {
    public static final DeferredBlock<Block> SHARK_SKIN=ModContent.BLOCKS.registerSimpleBlock("shark_skin");
    public static final DeferredBlock<Block> SHARK_FIN=ModContent.BLOCKS.registerSimpleBlock("shark_fin");
    public static final DeferredBlock<Block> SHARK_BELLY=ModContent.BLOCKS.registerSimpleBlock("shark_belly");
    public static final DeferredBlock<Block> CREATURE_EYE=ModContent.BLOCKS.registerSimpleBlock("creature_eye");
    public static final DeferredBlock<Block> CREATURE_TOOTH=ModContent.BLOCKS.registerSimpleBlock("creature_tooth");
    public static final DeferredBlock<Block> BIRD_WING=ModContent.BLOCKS.registerSimpleBlock("bird_wing");
    public static final DeferredBlock<Block> BIRD_BEAK=ModContent.BLOCKS.registerSimpleBlock("bird_beak");
    public static final DeferredBlock<Block> GRAZER_HOOF=ModContent.BLOCKS.registerSimpleBlock("grazer_hoof");
    public static final DeferredBlock<Block> BIRD_SKIN=ModContent.BLOCKS.registerSimpleBlock("bird_skin");
    public static final DeferredBlock<Block> GRAZER_SKIN=ModContent.BLOCKS.registerSimpleBlock("grazer_skin");
    public static final DeferredBlock<Block> DAMAGED_DECK=ModContent.BLOCKS.registerSimpleBlock("damaged_deck");
    public static final DeferredItem<Item> SPEAR=ModContent.ITEMS.registerSimpleItem("timber_spear",p->p.sword(ToolMaterial.WOOD,3,-2.8F));
    public static final DeferredItem<ChumItem> BAIT=ModContent.ITEMS.registerItem("shark_bait",ChumItem::new);
    public static final DeferredHolder<EntityType<?>,EntityType<OceanCreature>> SHARK=ModContent.ENTITY_TYPES.register("reef_shark",id->
            EntityType.Builder.<OceanCreature>of(OceanCreature::new,MobCategory.MONSTER).sized(1.6F,.65F).clientTrackingRange(8).updateInterval(2)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,id)));
    public static final DeferredHolder<EntityType<?>,EntityType<OceanCreature>> BIRD=ModContent.ENTITY_TYPES.register("crop_skimmer",id->
            EntityType.Builder.<OceanCreature>of(OceanCreature::new,MobCategory.CREATURE).sized(.65F,.5F).clientTrackingRange(8).updateInterval(2)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,id)));
    public static final DeferredHolder<EntityType<?>,EntityType<IslandGrazer>> GRAZER=ModContent.ENTITY_TYPES.register("tide_grazer",id->
            EntityType.Builder.<IslandGrazer>of(IslandGrazer::new,MobCategory.CREATURE).sized(.85F,1.1F).clientTrackingRange(8).updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,id)));
    public static final DeferredHolder<EntityType<?>,EntityType<ChumEntity>> CHUM=ModContent.ENTITY_TYPES.register("chum_float",id->
            EntityType.Builder.<ChumEntity>of(ChumEntity::new,MobCategory.MISC).sized(.5F,.25F).clientTrackingRange(8).updateInterval(3)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,id)));
    public static void register(IEventBus bus) {
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent event)-> {
            event.put(SHARK.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.MOVEMENT_SPEED,.3).build());
            event.put(BIRD.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,8).add(Attributes.MOVEMENT_SPEED,.3).build());
            event.put(GRAZER.get(),animalAttributes());
        });
    }
    private static net.minecraft.world.entity.ai.attributes.AttributeSupplier animalAttributes() {
        return net.minecraft.world.entity.animal.Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH,16).add(Attributes.MOVEMENT_SPEED,.2).build();
    }
    private CreatureContent() { }
}
