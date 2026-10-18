package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.creature.OceanCreature;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.salvage.NetHarvest;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native consumption and partial transfer regressions for crew-friendly survival features. */
public final class FeaturePolishCheck {
    private FeaturePolishCheck() { }
    private static void require(boolean value,String message){if(!value){throw new IllegalStateException(message);}}
    public static int run(ServerLevel level) {
        int checks=0;var cell=new BlockPos(1,1,0);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"FeaturePolishCheck"));
        var net=SurvivalCheck.create(level,RaftPiece.Kind.COLLECTION_NET);
        var purifier=SurvivalCheck.create(level,RaftPiece.Kind.PURIFIER);
        var planter=SurvivalCheck.create(level,RaftPiece.Kind.PLANTER);
        boolean bonuses=RaftConfig.MEAL_BONUSES.get(),birds=RaftConfig.BIRDS.get();int duration=RaftConfig.MEAL_DURATION.get();var mode=RaftConfig.MODE.get();
        try {
            var enchantments=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            for(String name:List.of("metal_spear","reef_machete")){
                var weapon=new ItemStack(CraftingExpansion.item(name));
                require(weapon.isDamageableItem() && weapon.getMaxDamage()>new ItemStack(dev.raftocean.creature.CreatureContent.SPEAR.get()).getMaxDamage(),
                        "Advanced weapons have greater native durability than the starter spear");checks++;
                require(enchantments.getOrThrow(Enchantments.SHARPNESS).value().canEnchant(weapon)
                        && enchantments.getOrThrow(Enchantments.UNBREAKING).value().canEnchant(weapon),
                        "Advanced weapons support native damage and durability enchantments");checks++;
            }
            RaftConfig.MEAL_BONUSES.set(true);RaftConfig.MEAL_DURATION.set(600);
            var names=List.of("garden_broth","mushroom_chowder","sea_grain_bowl","hearty_fish_pot","reef_stew","orchard_compote","seafarer_supper");
            var effects=List.of(MobEffects.SPEED,MobEffects.WATER_BREATHING,MobEffects.HASTE,MobEffects.ABSORPTION,MobEffects.RESISTANCE,MobEffects.SPEED,MobEffects.NIGHT_VISION);
            for(int i=0;i<names.size();i++) {
                player.getFoodData().setFoodLevel(10);
                var bowl=new ItemStack(CraftingExpansion.item(names.get(i))).finishUsingItem(level,player);
                var effect=player.getEffect(effects.get(i));
                require(bowl.is(Items.BOWL) && player.getFoodData().getFoodLevel()>10 && effect!=null
                        && effect.getAmplifier()==0 && effect.getDuration()==600,"Meal restores hunger, returns bowl and applies configured level-one bonus: "+names.get(i));checks++;
                new ItemStack(CraftingExpansion.item(names.get(i))).finishUsingItem(level,player);
                require(player.getEffect(effects.get(i)).getAmplifier()==0,"Repeated meals refresh without stacking bonus strength");checks++;
            }
            player.removeEffect(MobEffects.SPEED);RaftConfig.MEAL_BONUSES.set(false);
            require(new ItemStack(CraftingExpansion.item("garden_broth")).finishUsingItem(level,player).is(Items.BOWL)
                    && !player.hasEffect(MobEffects.SPEED),"Disabling meal bonuses preserves nutrition and bowl return");checks++;
            player.setPos(net.stationPosition(cell,RaftPiece.Kind.COLLECTION_NET));player.getInventory().clearContent();
            net.storage(cell).setItem(0,new ItemStack(Items.OAK_PLANKS,10));
            require(NetHarvest.collect(net,cell,player)==10 && player.getInventory().countItem(Items.OAK_PLANKS)==10
                    && net.storage(cell).getItem(0).isEmpty(),"Net harvest transfers exact counts without dropping loot");checks++;
            require(NetHarvest.collect(net,cell,player)==0,"Repeated net pickup cannot duplicate cargo");checks++;
            require(NetHarvest.remaining(net,cell)==0,"Empty net feedback reports no cargo");checks++;
            for(int slot=0;slot<36;slot++){player.getInventory().setItem(slot,new ItemStack(Items.STONE,64));}
            player.getInventory().setItem(0,new ItemStack(Items.OAK_PLANKS,63));net.storage(cell).setItem(0,new ItemStack(Items.OAK_PLANKS,10));
            require(NetHarvest.collect(net,cell,player)==1 && net.storage(cell).getItem(0).getCount()==9
                    && player.getInventory().getItem(0).getCount()==64,"Partial pickup leaves all excess cargo in the shared net");checks++;
            for(boolean creative:new boolean[]{false,true}) {
                player.getAbilities().instabuild=creative;
                require(NetHarvest.collect(net,cell,player)==0 && net.storage(cell).getItem(0).getCount()==9,"Full inventory never destroys net cargo in either game mode");checks++;
            }
            require(NetHarvest.remaining(net,cell)==9,"Partial/full pickup feedback retains the exact cargo count");checks++;
            require(NetHarvest.remaining(net,cell.above())==0,"Stale net feedback does not create another inventory");checks++;
            player.getAbilities().instabuild=false;player.setPos(player.position().add(100,0,0));
            require(NetHarvest.collect(net,cell,player)==0 && net.storage(cell).getItem(0).getCount()==9,"Distant net pickup preserves cargo");checks++;
            player.getInventory().clearContent();player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SurvivalContent.SALT_WATER.get(),2));
            require(CupTransfers.use(purifier,cell,RaftPiece.Kind.PURIFIER,player,InteractionHand.MAIN_HAND)==InteractionResult.FAIL
                    && player.getMainHandItem().getCount()==2 && purifier.storage(cell).rawWater==0,"Remote cup use cannot consume cups or alter reservoirs");checks++;
            player.setPos(purifier.stationPosition(cell,RaftPiece.Kind.PURIFIER));
            require(CupTransfers.use(purifier,cell.above(),RaftPiece.Kind.PURIFIER,player,InteractionHand.MAIN_HAND)==InteractionResult.FAIL
                    && player.getMainHandItem().getCount()==2,"Stale station location rejects cup use safely");checks++;
            purifier.discard();
            require(CupTransfers.use(purifier,cell,RaftPiece.Kind.PURIFIER,player,InteractionHand.MAIN_HAND)==InteractionResult.FAIL
                    && player.getMainHandItem().getCount()==2,"Removed station rejects cup use without losing items");checks++;
            checks += qualityChecks(level);
            RaftConfig.MODE.set(RaftConfig.Mode.PEACEFUL);RaftConfig.BIRDS.set(true);planter.storage(cell).setItem(2,new ItemStack(Items.WHEAT,3));
            require(!OceanCreature.stealCrop(planter,cell) && planter.storage(cell).getItem(2).getCount()==3,"Direct theft transaction honors Peaceful");checks++;
        } finally {
            RaftConfig.MEAL_BONUSES.set(bonuses);RaftConfig.MEAL_DURATION.set(duration);RaftConfig.MODE.set(mode);RaftConfig.BIRDS.set(birds);
            for(var raft:List.of(net,purifier,planter)){var storage=raft.storage(cell);if(storage!=null){storage.clearContent();}raft.discard();}
            player.getInventory().clearContent();player.discard();
        }
        return checks;
    }
    private static int qualityChecks(ServerLevel level) {
        int checks = 0;
        var cell = new BlockPos(1, 1, 0);
        var chest = SurvivalCheck.create(level, RaftPiece.Kind.CHEST);
        var grill = SurvivalCheck.create(level, RaftPiece.Kind.GRILL);
        var pot = SurvivalCheck.create(level, RaftPiece.Kind.COOKING_POT);
        try {
            var pairs = dev.raftocean.menu.RaftChests.pairs(chest);
            require(pairs == dev.raftocean.menu.RaftChests.pairs(chest), "Unchanged chest layout reuses its pairing map"); checks++;
            chest.setPos(chest.position().add(10, 0, 0)); chest.setYRot(90);
            require(pairs == dev.raftocean.menu.RaftChests.pairs(chest), "Hull motion does not rebuild raft-local chest pairs"); checks++;
            var seed = net.minecraft.world.level.storage.TagValueOutput.createWithoutContext(net.minecraft.util.ProblemReporter.DISCARDING);
            seed.putString("Structure", "0,1,0,CHEST,0,0;1,1,0,CHEST,0,0");
            chest.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), seed.buildResult()));
            var paired = dev.raftocean.menu.RaftChests.pairs(chest);
            require(cell.equals(paired.get(new BlockPos(0, 1, 0))) && paired != pairs,
                    "Reloading a changed layout rebuilds chest pairs"); checks++;
            require(chest.removeStructuralPiece(cell) && dev.raftocean.menu.RaftChests.pairs(chest).isEmpty(),
                    "Removing one chest invalidates the cached double chest"); checks++;
            var storage = grill.storage(cell);
            storage.setItem(0, new ItemStack(Items.STONE)); storage.setItem(1, new ItemStack(Items.OAK_PLANKS, 3));
            SurvivalMachines.tick(grill);
            require(storage.status.equals("recipe") && storage.getItem(0).getCount() == 1
                    && storage.getItem(1).getCount() == 3 && storage.fuelTicks == 0,
                    "Invalid grill recipe explains blockage without spending items or fuel"); checks++;
            storage.setItem(0, ItemStack.EMPTY); SurvivalMachines.tick(grill);
            require(storage.status.equals("supplies"), "Empty grill requests ingredients instead of reporting recipe failure"); checks++;
            var ingredients = pot.storage(cell); ingredients.cleanWater = 1;
            ingredients.setItem(0, new ItemStack(Items.STONE)); ingredients.setItem(1, new ItemStack(Items.CARROT));
            ingredients.setItem(6, new ItemStack(Items.BOWL)); ingredients.setItem(3, new ItemStack(Items.OAK_PLANKS));
            SurvivalMachines.startCooking(ingredients,level);
            if(!RaftConfig.MANUAL_COOKING.get())SurvivalMachines.tick(pot);
            require(ingredients.status.equals("recipe") && ingredients.cleanWater == 1 && ingredients.fuelTicks == 0
                    && ingredients.getItem(6).is(Items.BOWL), "Invalid multi-input recipe preserves water, bowl and fuel"); checks++;
            var menu = new dev.raftocean.menu.SurvivalMenu(88,
                    new FakePlayer(level, new GameProfile(UUID.randomUUID(), "QualityMenuCheck")).getInventory(), RaftPiece.Kind.COOKING_POT, ingredients);
            require(menu.status().equals("recipe"), "Recipe failure is synchronized to machine UI"); checks++;
        } finally {
            for (var raft : List.of(chest, grill, pot)) {
                raft.pieces().keySet().forEach(pos -> { var inventory = raft.storage(pos); if (inventory != null) { inventory.clearContent(); } });
                raft.discard();
            }
        }
        return checks;
    }

}
