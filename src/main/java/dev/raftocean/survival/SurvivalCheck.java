package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.entity.*;
import dev.raftocean.menu.RaftStationMenus;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Tests real machine recipes, fuel components, transactions, persistence, drinking, and fishing. */
public final class SurvivalCheck {
    private static final BlockPos CELL = new BlockPos(1, 1, 0);
    private SurvivalCheck() { }
    public static int run(ServerLevel level) {
        int checks = 0;
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SurvivalCheck"));
        player.setPos(0, level.getMaxY() - 8, 0); level.addNewPlayer(player);
        try {
            require(Thirst.value(player) == 20, "New player hydration"); checks++;
            Thirst.tick(player, 600);
            require(Thirst.value(player) == 19, "Thirty seconds drains one hydration"); checks++;
            player.setSprinting(true); Thirst.tick(player, 600);
            require(Thirst.value(player) == 17, "Sprinting doubles hydration drain"); checks++;
            player.setSprinting(false); player.setData(SurvivalContent.THIRST, 8);
            var drink = new ItemStack(SurvivalContent.FRESH_WATER.get());
            ItemStack empty = drink.finishUsingItem(level, player);
            require(Thirst.value(player) == 14 && empty.is(SurvivalContent.EMPTY_CUP.get()), "Drinking restores six and returns the cup"); checks++;
            Thirst.drink(player); Thirst.drink(player);
            require(Thirst.value(player) == 20, "Hydration clamps at twenty"); checks++;
            player.setData(SurvivalContent.THIRST, 7);
            var bedHome = new RaftBeds.Home(UUID.randomUUID(), CELL);
            player.setData(SurvivalContent.BED_HOME, bedHome);
            var savedPlayer = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            player.saveWithoutId(savedPlayer);
            var loadedPlayer = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SavedSurvivalCheck"));
            loadedPlayer.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), savedPlayer.buildResult()));
            require(Thirst.value(loadedPlayer) == 7 && loadedPlayer.getData(SurvivalContent.BED_HOME).equals(bedHome),
                    "Hydration and moving bed home persist with player data"); checks++;
            var deathClone = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SurvivalDeathCheck"));
            deathClone.copyAttachmentsFrom(player, true);
            require(deathClone.getData(SurvivalContent.BED_HOME).equals(bedHome) && Thirst.value(deathClone) == 20,
                    "Death preserves bed home and resets hydration"); checks++;
            player.getAbilities().instabuild = true; Thirst.tick(player, 600);
            require(Thirst.value(player) == 20, "Creative is exempt"); checks++;
            player.getAbilities().instabuild = false;

            var rod = SurvivalContent.FISHING_ROD.get();
            player.getInventory().clearContent(); player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(rod));
            require(rod.use(level, player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL && player.fishing == null,
                    "Baited rod refuses unbaited casts"); checks++;
            player.getInventory().setItem(10, new ItemStack(SurvivalContent.BAIT.get(), 2));
            rod.use(level, player, InteractionHand.MAIN_HAND);
            require(player.fishing != null && player.getInventory().countItem(SurvivalContent.BAIT.get()) == 1,
                    "Native bobber casts and consumes one bait"); checks++;
            rod.use(level, player, InteractionHand.MAIN_HAND);
            require(player.fishing == null && player.getInventory().countItem(SurvivalContent.BAIT.get()) == 1,
                    "Native reel does not charge bait again"); checks++;

            for (var kind : RaftPiece.Kind.values()) {
                if (!kind.machine()) { continue; }
                var raft = create(level, kind);
                var menu = RaftStationMenus.create(1, player.getInventory(), raft, CELL, kind);
                player.setPos(raft.position().add(0, RaftEntity.DECK_HEIGHT + .001, 0));
                require(menu.stillValid(player), "Shared survival inventory opens " + kind); checks++;
                var delta = new net.minecraft.world.phys.Vec3(20, .01, -20);
                raft.setPos(raft.position().add(delta)); player.setPos(player.position().add(delta));
                require(menu.stillValid(player), "Survival inventory follows drift " + kind); checks++;
                require(menu.slots.get(0).container == raft.storage(CELL), "Survival menu shares server inventory " + kind); checks++;
                raft.storage(CELL).clearContent(); raft.discard();
                require(!menu.stillValid(player), "Removed survival machine invalidates menu " + kind); checks++;
            }

            var purifier = create(level, RaftPiece.Kind.PURIFIER);
            var water = purifier.storage(CELL);
            water.rawWater = 2; water.cleanWater = 4;
            water.setItem(1, new ItemStack(Items.OAK_PLANKS, 4));
            advance(purifier, RaftPiece.Kind.PURIFIER, 20);
            require(water.rawWater == 2 && water.cleanWater == 4 && water.getItem(1).getCount() == 4 && water.progress == 0,
                    "Full reservoir does not consume fuel or raw water"); checks++;
            water.cleanWater = 0; advance(purifier, RaftPiece.Kind.PURIFIER, 5);
            require(water.progress == 100 && water.fuelTicks > 0 && water.rawWater == 2,
                    "Purifier progresses without early input consumption"); checks++;
            var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess()); purifier.saveWithoutId(saved);
            var restored = new RaftEntity(ModContent.RAFT.get(), level);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
            require(restored.storage(CELL).progress == water.progress && restored.storage(CELL).fuelTicks == water.fuelTicks
                    && restored.storage(CELL).work.equals(water.work) && restored.storage(CELL).rawWater == 2
                    && restored.storage(CELL).cleanWater == 0 && restored.storage(CELL).fuelCapacity == water.fuelCapacity,
                    "Machine progress/fuel/work/reservoirs survive real entity save/load"); checks++;
            advance(restored, RaftPiece.Kind.PURIFIER, 5);
            require(restored.storage(CELL).rawWater == 1 && restored.storage(CELL).cleanWater == 1,
                    "Saved purifier finishes once using remaining duration"); checks++;
            restored.storage(CELL).clearContent(); restored.discard(); water.clearContent(); purifier.discard();

            for (var kind : List.of(RaftPiece.Kind.GRILL, RaftPiece.Kind.SMELTER)) {
                var raft = create(level, kind); var inventory = raft.storage(CELL);
                Item raw = kind == RaftPiece.Kind.GRILL ? Items.COD : ModContent.SCRAP.get();
                Item output = kind == RaftPiece.Kind.GRILL ? Items.COOKED_COD : Items.IRON_INGOT;
                inventory.setItem(0, new ItemStack(raw)); inventory.setItem(1, new ItemStack(Items.OAK_PLANKS, 16));
                advance(raft, kind, 40);
                require(inventory.getItem(0).isEmpty() && inventory.getItem(2).is(output) && inventory.getItem(2).getCount() == 1,
                        "Native fuel and loaded cooking recipe complete exactly once " + kind); checks++;
                inventory.clearContent(); raft.discard();
            }

            var pot = create(level, RaftPiece.Kind.COOKING_POT); var inventory = pot.storage(CELL);
            inventory.setItem(0, new ItemStack(Items.SALMON)); inventory.setItem(1, new ItemStack(Items.CARROT));
            inventory.setItem(2, new ItemStack(SurvivalContent.FRESH_WATER.get())); inventory.setItem(3, new ItemStack(Items.OAK_PLANKS, 8));
            inventory.setItem(6, new ItemStack(Items.BOWL)); inventory.setItem(5, new ItemStack(SurvivalContent.EMPTY_CUP.get(), 16));
            advance(pot, RaftPiece.Kind.COOKING_POT, 30);
            require(inventory.getItem(0).getCount() == 1 && inventory.getItem(3).getCount() == 8 && inventory.getItem(4).isEmpty(),
                    "Blocked cup byproduct prevents partial stew creation"); checks++;
            inventory.setItem(5, ItemStack.EMPTY); advance(pot, RaftPiece.Kind.COOKING_POT, 20);
            require(inventory.getItem(4).is(SurvivalContent.STEW.get()) && inventory.getItem(5).is(SurvivalContent.EMPTY_CUP.get())
                    && inventory.getItem(0).isEmpty() && inventory.getItem(1).isEmpty() && inventory.getItem(2).isEmpty() && inventory.getItem(6).isEmpty(),
                    "Pot consumes food/water/bowl once and returns reusable cup"); checks++;
            var handCraft = CraftingInput.of(2, 2, List.of(new ItemStack(Items.SALMON), new ItemStack(Items.CARROT),
                    new ItemStack(SurvivalContent.FRESH_WATER.get()), new ItemStack(Items.BOWL)));
            require(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, handCraft, level).isEmpty(), "Stew processing cannot be bypassed at workbench"); checks++;
            player.getFoodData().setFoodLevel(10);
            var bowl = inventory.getItem(4).copy().finishUsingItem(level, player);
            require(player.getFoodData().getFoodLevel() == 18 && bowl.is(Items.BOWL), "Stew feeds player and returns bowl"); checks++;
            inventory.clearContent(); pot.discard();

            for (var crop : List.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.CARROT, Items.POTATO)) {
                var planter = create(level, RaftPiece.Kind.PLANTER); var garden = planter.storage(CELL);
                garden.setItem(0, new ItemStack(crop)); garden.setItem(1, new ItemStack(SurvivalContent.PLANTING_MIX.get()));
                garden.setItem(4, new ItemStack(SurvivalContent.FRESH_WATER.get()));
                advance(planter, RaftPiece.Kind.PLANTER, 120);
                require(garden.getItem(0).isEmpty() && garden.getItem(1).isEmpty() && garden.getItem(4).isEmpty()
                        && garden.getItem(2).getCount() == 2 && garden.getItem(3).is(crop) && garden.getItem(3).getCount() == 2
                        && garden.getItem(5).is(SurvivalContent.EMPTY_CUP.get()), "Garden harvest with renewable seeds and cup " + crop); checks++;
                garden.clearContent(); planter.discard();
            }
            for (var sapling : List.of(Items.OAK_SAPLING, Items.BIRCH_SAPLING, Items.SPRUCE_SAPLING,
                    Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING,
                    Items.CHERRY_SAPLING, Items.PALE_OAK_SAPLING, Items.MANGROVE_PROPAGULE)) {
                var tree=create(level,RaftPiece.Kind.TREE_PLANTER);var soil=tree.storage(CELL);
                soil.setItem(0,new ItemStack(sapling));soil.setItem(1,new ItemStack(SurvivalContent.PLANTING_MIX.get()));
                soil.setItem(4,new ItemStack(SurvivalContent.FRESH_WATER.get()));
                advance(tree,RaftPiece.Kind.TREE_PLANTER,90);tree.syncTreeGrowth();
                require(soil.progress==1800 && tree.treeStage(CELL)==2 && soil.getItem(2).isEmpty(),"Tree grows visibly before harvest: "+sapling);checks++;
                require(tree.getBoundingBox().maxY>tree.getY()+3 && tree.getCollisionBoxes().stream()
                        .noneMatch(box->box.maxY>tree.getY()+1),"Visible tree canopy has render bounds without trapping players");checks++;
                var save=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());tree.saveWithoutId(save);
                var copy=new RaftEntity(ModContent.RAFT.get(),level);
                copy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),save.buildResult()));
                require(copy.storage(CELL).progress==1800 && copy.storage(CELL).cleanWater==1,"Growing tree and water survive reload");checks++;
                if(sapling==Items.OAK_SAPLING) {
                    save.putString("Structure",save.buildResult().getStringOr("Structure","")+";1,5,0,ROOF,0,0");
                    copy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),save.buildResult()));
                    advance(copy,RaftPiece.Kind.TREE_PLANTER,10);
                    require(copy.storage(CELL).progress==1800 && copy.storage(CELL).status.equals("light"),"Covered tree pauses without consuming sapling/water");checks++;
                    soil.setItem(2,new ItemStack(Items.OAK_LOG,64));advance(tree,RaftPiece.Kind.TREE_PLANTER,10);
                    require(soil.progress==1800 && soil.cleanWater==1 && !soil.getItem(0).isEmpty() && soil.status.equals("output"),
                            "Blocked tree output preserves pending harvest and inputs");checks++;
                    soil.setItem(2,ItemStack.EMPTY);
                }
                copy.discard();advance(tree,RaftPiece.Kind.TREE_PLANTER,90);tree.syncTreeGrowth();
                require(soil.getItem(2).getCount()==6 && soil.getItem(3).is(sapling) && soil.getItem(3).getCount()==2
                        && soil.getItem(5).is(SurvivalContent.EMPTY_CUP.get()) && soil.cleanWater==0 && tree.treeStage(CELL)==4,
                        "Mature tree yields timber, renewable saplings and reusable cup: "+sapling);checks++;
                soil.clearContent();tree.syncTreeGrowth();
                require(tree.treeStage(CELL)==0,"Collecting timber removes the mature canopy");checks++;
                tree.discard();
            }
            checks += SurvivalInteractionCheck.run(level, player);
            checks += WorkshopCheck.run(level,player);
            checks += FeaturePolishCheck.run(level);
            checks += DivingCheck.run(level);
            checks += dev.raftocean.archery.ArcheryCheck.run(level);
            checks += AutomationCheck.run(level);
            checks += BatteryCheck.run(level);
            checks += StarterSurvivalCheck.run(level);
            return checks;
        } finally {
            if (player.fishing != null) { player.fishing.discard(); }
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }
    public static RaftEntity create(ServerLevel level, RaftPiece.Kind kind) {
        var structure = new RaftStructure();
        require(structure.add(CELL, new RaftPiece(kind, 0, false), RaftLayout.starter()), "Survival piece support " + kind);
        var seed = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING); seed.putString("Structure", structure.encode());
        var raft = new RaftEntity(ModContent.RAFT.get(), level);
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), seed.buildResult()));
        raft.setPos(.5, level.getMaxY() - 8, .5); return raft;
    }
    private static void advance(RaftEntity raft, RaftPiece.Kind kind, int seconds) {
        for (int i = 0; i < seconds; i++) { SurvivalMachines.process(raft, CELL, kind, (ServerLevel)raft.level()); }
    }
    private static void require(boolean condition, String message) { if (!condition) { throw new IllegalStateException(message); } }
}
