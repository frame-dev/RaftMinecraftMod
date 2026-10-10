package dev.raftocean.archery;

import com.mojang.authlib.GameProfile;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.CraftingExpansion;
import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Real native shots, consumption, pickup persistence and loaded recipes. */
public final class ArcheryCheck {
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }
    public static int run(ServerLevel level) {
        int checks = 0;
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ArcheryCheck"));
        player.setPos(.5, level.getMaxY()+24, .5); level.getChunkAt(player.blockPosition()); level.addNewPlayer(player); player.setYRot(0); player.setXRot(0);
        var projectiles = new ArrayList<Entity>();
        try {
            for (var bow : ArcheryContent.BOWS) {
                for (var ammo : ArcheryContent.ARROWS) {
                    player.getInventory().clearContent();
                    var weapon = new ItemStack(bow.get()); var supply = new ItemStack(ammo.get(), 4);
                    player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
                    player.setItemInHand(InteractionHand.OFF_HAND, supply);
                    require(supply.is(ItemTags.ARROWS) && player.getProjectile(weapon)==supply, "Tagged custom arrows selected by native player ammo lookup"); checks++;
                    require(bow.get().use(level,player,InteractionHand.MAIN_HAND).consumesAction(), "Loaded bow starts native drawing"); checks++;
                    require(!bow.get().releaseUsing(weapon,level,player,72000) && supply.getCount()==4 && weapon.getDamageValue()==0, "Zero-length draw consumes no resources"); checks++;
                    var before = new HashSet<>(level.getEntitiesOfClass(Arrow.class, player.getBoundingBox().inflate(8)));
                    require(bow.get().releaseUsing(weapon,level,player,71980), "Fully drawn bow releases"); checks++;
                    var shots = level.getEntitiesOfClass(Arrow.class,player.getBoundingBox().inflate(8)).stream().filter(a -> !before.contains(a)).toList();
                    projectiles.addAll(shots);
                    require(shots.size()==1 && supply.getCount()==3 && weapon.getDamageValue()==1, "One real projectile consumes one arrow and one durability: shots="+shots.size()+" ammo="+supply.getCount()+" wear="+weapon.getDamageValue()); checks++;
                    var shot=shots.getFirst();
                    require(Math.abs(shot.getDeltaMovement().length()-3*bow.get().speedMultiplier())<.08, "Bow tier applies intended native launch speed"); checks++;
                    require(shot.getOwner()==player && shot.getPickupItemStackOrigin().is(ammo.get()) && shot.pickup==AbstractArrow.Pickup.ALLOWED, "Survival shot keeps original owner and recoverable custom ammo"); checks++;
                    var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess()); shot.saveWithoutId(saved);
                    require(saved.buildResult().getDouble("damage").orElseThrow()==ammo.get().damage(), "Arrow tier stores intended base damage"); checks++;
                    var copy=new Arrow(net.minecraft.world.entity.EntityTypes.ARROW,level);projectiles.add(copy);
                    copy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
                    require(copy.getPickupItemStackOrigin().is(ammo.get()) && copy.pickup==AbstractArrow.Pickup.ALLOWED, "Native projectile reload preserves special ammo identity and pickup rules"); checks++;
                    player.stopUsingItem();shot.discard();copy.discard();
                }
                player.getInventory().clearContent(); var weapon=new ItemStack(bow.get());player.setItemInHand(InteractionHand.MAIN_HAND,weapon);
                require(bow.get().use(level,player,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL, "Survival without ammo cannot start drawing"); checks++;
                player.getAbilities().instabuild=true;
                require(bow.get().use(level,player,InteractionHand.MAIN_HAND).consumesAction() && bow.get().releaseUsing(weapon,level,player,71980), "Creative fires default ammo without inventory arrows"); checks++;
                var creative=level.getEntitiesOfClass(Arrow.class,player.getBoundingBox().inflate(8));projectiles.addAll(creative);
                require(creative.size()==1 && creative.getFirst().pickup==AbstractArrow.Pickup.CREATIVE_ONLY && weapon.getDamageValue()==0, "Creative projectile cannot duplicate collectible ammo or wear weapon"); checks++;
                creative.forEach(Entity::discard);player.stopUsingItem();player.getAbilities().instabuild=false;
            }
            for(var ammo:ArcheryContent.ARROWS) {
                require(net.minecraft.world.level.block.DispenserBlock.DISPENSER_REGISTRY.get(ammo.get()) instanceof net.minecraft.core.dispenser.ProjectileDispenseBehavior, "Custom ammo registers a real dispenser projectile behavior"); checks++;
                var projectile=(Arrow)ammo.get().asProjectile(level,new Vec3(.5,level.getMaxY()+24,.5),new ItemStack(ammo.get()),Direction.NORTH);projectiles.add(projectile);
                var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());projectile.saveWithoutId(saved);
                require(saved.buildResult().getDouble("damage").orElseThrow()==ammo.get().damage() && projectile.getPickupItemStackOrigin().is(ammo.get()), "Dispenser route retains special arrow damage and pickup"); checks++;
            }
            List<List<Item>> ingredients=List.of(
                List.of(ModContent.CORDAGE.get(),ModContent.CORDAGE.get(),CraftingExpansion.item("binding_paste")),
                List.of(Items.STICK,Items.STICK,CraftingExpansion.item("binding_paste")),
                List.of(Items.STICK,Items.STICK,Items.STICK,ArcheryContent.BOWSTRING.get(),CraftingExpansion.item("binding_paste")),
                List.of(ArcheryContent.DRIFT.get(),ModContent.TIMBER_BRACE.get(),ArcheryContent.BOWSTRING.get(),CraftingExpansion.item("canvas_roll"),CraftingExpansion.item("binding_paste")),
                List.of(ArcheryContent.REEF.get(),CraftingExpansion.item("metal_plate"),CraftingExpansion.item("metal_plate"),CraftingExpansion.item("copper_wire"),ArcheryContent.BOWSTRING.get()));
            var outputs=List.of(ArcheryContent.BOWSTRING.get(),ArcheryContent.SHAFT.get(),ArcheryContent.DRIFT.get(),ArcheryContent.REEF.get(),ArcheryContent.STORM.get());
            for(int i=0;i<ingredients.size();i++) {
                var input=CraftingInput.of(3,3,padded(ingredients.get(i)));
                var result=level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow().value().assemble(input);
                require(result.is(outputs.get(i)) && result.getCount()==(i==0?2:i==1?8:1), "Loaded material and bow upgrade recipe assembles intended count"); checks++;
            }
            var heads=List.of(ModContent.DRIFT_LEAF.get(),ModContent.SCRAP.get(),Items.IRON_INGOT,CraftingExpansion.item("metal_plate"));
            for(int i=0;i<4;i++)for(var fletching:List.of(ModContent.DRIFT_LEAF.get(),Items.FEATHER)) {
                var input=CraftingInput.of(3,3,padded(List.of(ArcheryContent.SHAFT.get(),ArcheryContent.SHAFT.get(),ArcheryContent.SHAFT.get(),ArcheryContent.SHAFT.get(),heads.get(i),fletching)));
                var result=level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow().value().assemble(input);
                require(result.is(ArcheryContent.ARROWS.get(i).get()) && result.getCount()==4,"Arrow recipes support renewable leaves or feathers with correct batch size");checks++;
            }
        } finally {projectiles.forEach(Entity::discard);level.removePlayerImmediately(player,Entity.RemovalReason.DISCARDED);}
        return checks;
    }
    private static List<ItemStack> padded(List<Item> items) {
        var grid=new ArrayList<ItemStack>();items.forEach(i -> grid.add(new ItemStack(i)));
        while(grid.size()<9)grid.add(ItemStack.EMPTY);return grid;
    }
    private ArcheryCheck() { }
}
