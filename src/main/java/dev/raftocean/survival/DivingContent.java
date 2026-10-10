package dev.raftocean.survival;

import dev.raftocean.registry.ModContent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.equipment.*;
import net.neoforged.neoforge.registries.DeferredItem;

/** Native wearable components synchronize, save and remove their bonuses with the equipment slot. */
public final class DivingContent {
    public static final DeferredItem<Item> HOOD=ModContent.ITEMS.registerItem("tidal_diving_hood",p->new Item(p.durability(192)
            .component(DataComponents.EQUIPPABLE,Equippable.builder(EquipmentSlot.HEAD)
                    .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID,Identifier.fromNamespaceAndPath("raft_ocean","tidal_hood"))).build())
            .component(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.builder()
                    .add(Attributes.OXYGEN_BONUS,new AttributeModifier(Identifier.fromNamespaceAndPath("raft_ocean","hood_air"),2,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.HEAD)
                    .build())));
    public static final DeferredItem<Item> FLIPPERS=ModContent.ITEMS.registerItem("reef_flippers",p->new Item(p.durability(192)
            .component(DataComponents.EQUIPPABLE,Equippable.builder(EquipmentSlot.FEET)
                    .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID,Identifier.fromNamespaceAndPath("raft_ocean","reef_flippers"))).build())
            .component(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.builder()
                    .add(Attributes.WATER_MOVEMENT_EFFICIENCY,new AttributeModifier(Identifier.fromNamespaceAndPath("raft_ocean","flipper_swim"),.65,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.FEET)
                    .build())));
    public static final DeferredItem<Item> SALVE=ModContent.ITEMS.registerItem("shoreleaf_salve",p->new HealingSalveItem(p.stacksTo(16)
            .component(DataComponents.CONSUMABLE,Consumables.DEFAULT_DRINK)));
    public static void initialize() { }
    private DivingContent() { }
}
