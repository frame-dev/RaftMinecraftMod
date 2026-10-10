package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.progression.Research;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native wear/swap/load and consumed salves, plus real recipe and research integration. */
public final class DivingCheck {
    private DivingCheck() { }
    private static void require(boolean value,String message){if(!value){throw new IllegalStateException(message);}}
    private static final class EquippedPlayer extends FakePlayer {
        private final Map<EquipmentSlot,ItemStack> previous=new EnumMap<>(EquipmentSlot.class);
        EquippedPlayer(ServerLevel level,String name) {
            super(level,new GameProfile(UUID.randomUUID(),name));
            for(var slot:EquipmentSlot.VALUES){previous.put(slot,ItemStack.EMPTY);}
        }
        void updateEquipment() {
            collectEquipmentChanges(previous);
            for(var slot:EquipmentSlot.VALUES){previous.put(slot,getItemBySlot(slot).copy());}
        }
    }
    private static void tick(EquippedPlayer player){player.updateEquipment();}
    public static int run(ServerLevel level) {
        int checks=0;var player=new EquippedPlayer(level,"DivingCheck");player.setPos(.5,level.getMaxY()+16,.5);
        var copy=new EquippedPlayer(level,"SavedDiveCheck");
        try {
            double air=player.getAttributeValue(Attributes.OXYGEN_BONUS),swim=player.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(DivingContent.HOOD.get()));tick(player);
            require(player.getAttributeValue(Attributes.OXYGEN_BONUS)==air,"Holding a hood does not grant its wearable bonus");checks++;
            player.setItemSlot(EquipmentSlot.HEAD,new ItemStack(DivingContent.HOOD.get()));
            player.setItemSlot(EquipmentSlot.FEET,new ItemStack(DivingContent.FLIPPERS.get()));tick(player);
            require(player.getAttributeValue(Attributes.OXYGEN_BONUS)==air+2,"Worn hood applies native oxygen preservation");checks++;
            require(Math.abs(player.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY)-(swim+.65))<1e-9,"Worn flippers apply native water efficiency");checks++;
            require(player.getItemBySlot(EquipmentSlot.HEAD).get(DataComponents.EQUIPPABLE).slot()==EquipmentSlot.HEAD
                    && player.getItemBySlot(EquipmentSlot.FEET).get(DataComponents.EQUIPPABLE).slot()==EquipmentSlot.FEET,"Gear declares correct native equipment slots");checks++;
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());player.saveWithoutId(saved);
            copy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));tick(copy);
            require(copy.getItemBySlot(EquipmentSlot.HEAD).is(DivingContent.HOOD.get()) && copy.getItemBySlot(EquipmentSlot.FEET).is(DivingContent.FLIPPERS.get())
                    && copy.getAttributeValue(Attributes.OXYGEN_BONUS)==air+2,"Equipped gear and bonuses survive native player save/load without duplication");checks++;
            player.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);player.setItemSlot(EquipmentSlot.FEET,ItemStack.EMPTY);tick(player);
            require(player.getAttributeValue(Attributes.OXYGEN_BONUS)==air && player.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY)==swim,"Removing gear removes both native bonuses");checks++;
            var salve=new ItemStack(DivingContent.SALVE.get(),2);player.setItemInHand(InteractionHand.MAIN_HAND,salve);player.setHealth(player.getMaxHealth());
            require(DivingContent.SALVE.get().use(level,player,InteractionHand.MAIN_HAND)==InteractionResult.FAIL && salve.getCount()==2,"Healthy players retain all salves");checks++;
            player.setHealth(10);
            require(DivingContent.SALVE.get().use(level,player,InteractionHand.MAIN_HAND).consumesAction() && player.getHealth()==10 && salve.getCount()==2,"Starting salve animation does not heal or consume early");checks++;
            var remaining=salve.finishUsingItem(level,player);
            require(player.getHealth()==16 && remaining.getCount()==1 && remaining.is(DivingContent.SALVE.get()),"Completed salve heals three hearts and consumes exactly one");checks++;
            player.setHealth(19);remaining.finishUsingItem(level,player);
            require(player.getHealth()==player.getMaxHealth(),"Salve healing clamps to maximum health");checks++;
            List<List<ItemStack>> ingredients=List.of(
                    List.of(new ItemStack(Items.LEATHER),new ItemStack(Items.LEATHER),new ItemStack(Items.GLASS),new ItemStack(dev.raftocean.registry.ModContent.CORDAGE.get()),new ItemStack(CraftingExpansion.item("copper_wire"))),
                    List.of(new ItemStack(Items.LEATHER),new ItemStack(Items.LEATHER),new ItemStack(CraftingExpansion.item("canvas_roll")),new ItemStack(CraftingExpansion.item("binding_paste"))),
                    List.of(new ItemStack(dev.raftocean.registry.ModContent.DRIFT_LEAF.get()),new ItemStack(Items.KELP),new ItemStack(CraftingExpansion.item("binding_paste"))));
            var outputs=List.of(DivingContent.HOOD.get(),DivingContent.FLIPPERS.get(),DivingContent.SALVE.get());
            for(int i=0;i<outputs.size();i++) {
                var input=CraftingInput.of(ingredients.get(i).size(),1,ingredients.get(i));
                require(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow().value().assemble(input).is(outputs.get(i)),"Loaded dive/medical recipe assembles intended output");checks++;
            }
            require(new ItemStack(DivingContent.HOOD.get()).is(Research.tag("research/unlocks/diving"))
                    && new ItemStack(DivingContent.FLIPPERS.get()).is(Research.tag("research/unlocks/diving")),"Diving wearables join the dedicated diving project");checks++;
        } finally {player.discard();copy.discard();}
        return checks;
    }
}
