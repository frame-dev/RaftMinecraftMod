package dev.raftocean.survival;

import dev.raftocean.registry.ModContent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;

/** Original intermediate materials and prepared meals, usable through data-pack recipes. */
public final class CraftingExpansion {
    private static final Map<String,DeferredItem<Item>> ITEMS=new LinkedHashMap<>();
    static {
        for(String name:new String[]{"stone_anchor_bundle","damp_brick","dried_brick","fired_brick","metal_plate","copper_wire","binding_paste","canvas_roll",
                "reinforced_brace","root_skewer","fish_skewer"}) {
            ITEMS.put(name,ModContent.ITEMS.registerSimpleItem(name));
        }
        meal("garden_broth",7,.7F,false);meal("mushroom_chowder",9,.8F,false);
        meal("sea_grain_bowl",8,.7F,false);meal("hearty_fish_pot",10,.9F,false);
        meal("roasted_roots",6,.6F,true);meal("charred_fish_skewer",7,.7F,true);
        meal("reef_stew",9,.8F,false);meal("orchard_compote",7,.7F,false);meal("seafarer_supper",10,.9F,false);
        ITEMS.put("metal_spear",ModContent.ITEMS.registerSimpleItem("metal_spear",p->p.sword(net.minecraft.world.item.ToolMaterial.IRON,3,-2.8F)));
        ITEMS.put("reef_machete",ModContent.ITEMS.registerSimpleItem("reef_machete",p->p.sword(net.minecraft.world.item.ToolMaterial.IRON,3,-2.3F)));
    }
    private static void meal(String name,int nutrition,float saturation,boolean skewer) {
        ITEMS.put(name,ModContent.ITEMS.registerItem(name,p -> {
            var properties=p.stacksTo(16).food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build())
                    .usingConvertsTo(skewer?Items.STICK:Items.BOWL);
            return skewer?new Item(properties):new PreparedMealItem(properties,name);
        }));
    }
    public static void initialize() { }
    public static Item item(String name) {return ITEMS.get(name).get();}
    public static void display(Consumer<Item> output) {ITEMS.values().forEach(item->output.accept(item.get()));}
    private CraftingExpansion() { }
}
