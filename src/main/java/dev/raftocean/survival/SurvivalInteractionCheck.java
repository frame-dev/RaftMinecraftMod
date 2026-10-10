package dev.raftocean.survival;

import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.SurvivalMenu;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;

/** Regressions exercise native right-click/shift-click rather than just reading slot contents. */
public final class SurvivalInteractionCheck {
    private static final BlockPos CELL = new BlockPos(1, 1, 0);
    private SurvivalInteractionCheck() { }
    public static int run(ServerLevel level, ServerPlayer player) {
        int checks = 0;
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SurvivalContent.EMPTY_CUP.get(), 3));
        var before = player.getMainHandItem().copy();
        player.setXRot(-90);
        player.gameMode.useItem(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND);
        require(ItemStack.matches(before, player.getMainHandItem()), "Right-click away from water preserves all cups"); checks++;

        var purifier = SurvivalCheck.create(level, RaftPiece.Kind.PURIFIER);
        var water = purifier.storage(CELL);
        player.setPos(purifier.stationPosition(CELL,RaftPiece.Kind.PURIFIER));
        try {
            for (var hand : InteractionHand.values()) {
                player.getInventory().clearContent();
                player.setItemInHand(hand, new ItemStack(SurvivalContent.SALT_WATER.get()));
                var result = CupTransfers.use(purifier, CELL, RaftPiece.Kind.PURIFIER, player, hand);
                require(result.consumesAction() && player.getItemInHand(hand).is(SurvivalContent.EMPTY_CUP.get()) && water.rawWater == 1,
                        "Pouring returns the same cup in either hand"); checks++;
                water.rawWater = 0; water.cleanWater = 1;
                CupTransfers.use(purifier, CELL, RaftPiece.Kind.PURIFIER, player, hand);
                require(player.getItemInHand(hand).is(SurvivalContent.FRESH_WATER.get()) && water.cleanWater == 0,
                        "Collecting fresh water transforms either held cup"); checks++;
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SurvivalContent.EMPTY_CUP.get()));
            require(CupTransfers.use(purifier, CELL, RaftPiece.Kind.PURIFIER, player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL
                    && player.getMainHandItem().is(SurvivalContent.EMPTY_CUP.get()), "Empty tank cannot delete an empty cup"); checks++;
            water.rawWater = 4;
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SurvivalContent.SALT_WATER.get(), 3));
            require(CupTransfers.use(purifier, CELL, RaftPiece.Kind.PURIFIER, player, InteractionHand.MAIN_HAND) == InteractionResult.FAIL
                    && player.getMainHandItem().getCount() == 3, "Full tank does not consume any stacked cups"); checks++;
            water.rawWater = 0;
            CupTransfers.use(purifier, CELL, RaftPiece.Kind.PURIFIER, player, InteractionHand.MAIN_HAND);
            require(player.getMainHandItem().getCount() == 2 && player.getInventory().countItem(SurvivalContent.EMPTY_CUP.get()) == 1
                    && water.rawWater == 1, "One pour exchanges exactly one cup in an existing stack"); checks++;
            player.getInventory().clearContent();
            for (int i = 0; i < 36; i++) { player.getInventory().setItem(i, new ItemStack(Items.STONE, 64)); }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SurvivalContent.SALT_WATER.get()));
            CupTransfers.use(purifier, CELL, RaftPiece.Kind.PURIFIER, player, InteractionHand.MAIN_HAND);
            require(player.getMainHandItem().is(SurvivalContent.EMPTY_CUP.get()) && water.rawWater == 2,
                    "Full inventory still returns an unstacked cup directly to the hand"); checks++;
            player.getInventory().clearContent();
            water.setItem(0, new ItemStack(SurvivalContent.SALT_WATER.get(), 2));
            water.setItem(3, new ItemStack(SurvivalContent.EMPTY_CUP.get(), 16));
            SurvivalMachines.process(purifier, CELL, RaftPiece.Kind.PURIFIER, level);
            require(water.getItem(0).getCount() == 2 && water.getItem(3).getCount() == 16 && water.rawWater == 2,
                    "Inserted water cannot erase its cup when cup-return slot is full"); checks++;
            water.setItem(3, ItemStack.EMPTY);
            SurvivalMachines.process(purifier, CELL, RaftPiece.Kind.PURIFIER, level);
            require(water.getItem(0).getCount() == 1 && water.getItem(3).getCount() == 1 && water.rawWater == 3,
                    "Legacy input slot returns its cup immediately, before processing"); checks++;
        } finally { water.clearContent(); purifier.discard(); }

        for (var kind : List.of(RaftPiece.Kind.PLANTER, RaftPiece.Kind.COOKING_POT)) {
            var raft = SurvivalCheck.create(level, kind);
            try {
                player.setPos(raft.stationPosition(CELL,kind));
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(SurvivalContent.FRESH_WATER.get()));
                CupTransfers.use(raft, CELL, kind, player, InteractionHand.MAIN_HAND);
                require(raft.storage(CELL).cleanWater == 1 && player.getMainHandItem().is(SurvivalContent.EMPTY_CUP.get()),
                        "Watering returns cup before growing/cooking " + kind); checks++;
            } finally { raft.storage(CELL).clearContent(); raft.discard(); }
        }

        for (var kind : RaftPiece.Kind.values()) {
            if (!kind.machine()) { continue; }
            var raft = SurvivalCheck.create(level, kind);
            var storage = raft.storage(CELL);
            player.setPos(raft.position().add(0, .26, 0)); player.getInventory().clearContent();
            var menu = new SurvivalMenu(2, player.getInventory(), kind, storage);
            try {
                Item valid = switch (kind) {
                    case BASIC_PURIFIER, PURIFIER, RAIN_COLLECTOR, WATER_TANK, ELECTRIC_PURIFIER, SPRINKLER -> SurvivalContent.EMPTY_CUP.get();
                    case FUEL_TANK, BIOFUEL_GENERATOR, BATTERY_CHARGER -> Automation.BIOFUEL.get();
                    case BIOREFINER -> Items.POTATO;
                    case DRYING_RACK -> CraftingExpansion.item("damp_brick");
                    case PLANTER -> Items.WHEAT_SEEDS;
                    case TREE_PLANTER -> Items.OAK_SAPLING;
                    case COOKING_POT -> Items.COD;
                    case BASIC_GRILL, GRILL -> Items.COD;
                    default -> dev.raftocean.registry.ModContent.SCRAP.get();
                };
                menu.setCarried(new ItemStack(valid, 2));
                menu.clicked(0, 1, ContainerInput.PICKUP, player);
                require(storage.getItem(0).getCount() == 1 && menu.getCarried().getCount() == 1,
                        "Native right-click deposits exactly one, without deleting the rest " + kind); checks++;
                require(!menu.clickMenuButton(player, 0), "Tab switch refuses a carried stack " + kind); checks++;
                require(!menu.clickMenuButton(player,1) && !storage.paused,"Pause refuses a carried stack "+kind);checks++;
                menu.setCarried(ItemStack.EMPTY);
                menu.clicked(0, 1, ContainerInput.PICKUP, player);
                require(storage.getItem(0).isEmpty() && menu.getCarried().getCount() == 1,
                        "Native right-click retrieves the input " + kind); checks++;
                menu.setCarried(ItemStack.EMPTY);
                int output = menu.layout().entrySet().stream().filter(entry -> entry.getValue().output()).map(java.util.Map.Entry::getKey).findFirst().orElse(-1);
                if(output>=0) {
                storage.setItem(output, new ItemStack(Items.COOKED_COD, 9));
                menu.clicked(output, 1, ContainerInput.PICKUP, player);
                require(menu.getCarried().getCount() == 5 && storage.getItem(output).getCount() == 4,
                        "Right-click on ready items splits the stack safely " + kind); checks++;
                menu.setCarried(new ItemStack(Items.STONE, 3));
                menu.clicked(output, 1, ContainerInput.PICKUP, player);
                require(menu.getCarried().getCount() == 3 && storage.getItem(output).getCount() == 4,
                        "Output slot cannot swallow manually inserted items " + kind); checks++;
                }
                menu.setCarried(ItemStack.EMPTY);
                player.getInventory().setItem(9, new ItemStack(valid, 5));
                menu.clicked(27, 0, ContainerInput.QUICK_MOVE, player);
                require(storage.getItem(0).getCount() == 5 && player.getInventory().getItem(9).isEmpty()
                        && (output<0 || storage.getItem(output).getCount() == 4), "Shift-click routes to input, never merges into output " + kind); checks++;
                int spare = java.util.stream.IntStream.range(0, 27).filter(i -> !menu.layout().containsKey(i)).findFirst().orElseThrow();
                storage.setItem(spare, new ItemStack(Items.DIAMOND, 3));
                menu.clicked(spare, 0, ContainerInput.PICKUP, player);
                require(menu.getCarried().isEmpty() && storage.getItem(spare).getCount() == 3, "Hidden spare slot rejects clicks " + kind); checks++;
                require(menu.clickMenuButton(player, 0) && menu.suppliesPage(), "Supplies tab opens on server " + kind); checks++;
                menu.clicked(spare, 1, ContainerInput.PICKUP, player);
                require(menu.getCarried().getCount() == 2 && storage.getItem(spare).getCount() == 1, "Spare items from old saves remain retrievable " + kind); checks++;
                menu.setCarried(ItemStack.EMPTY);
                var second = new SurvivalMenu(3, player.getInventory(), kind, storage);
                require(!second.suppliesPage() && (output<0 || second.slots.get(output).getItem().getCount() == 4),
                        "Separate viewers share items but keep independent tabs " + kind); checks++;
                require(menu.clickMenuButton(player,1) && storage.paused && second.paused(),
                        "Co-op viewers share pause state "+kind);checks++;
                require(second.clickMenuButton(player,1) && !menu.paused() && !storage.paused,
                        "Co-op viewer resumes the same machine "+kind);checks++;
                require(!menu.clickMenuButton(player,99),"Unknown machine control is rejected "+kind);checks++;
                storage.progress = 60000; storage.duration = 72000; storage.fuelTicks = 100000; storage.fuelCapacity = 200000;
                require(menu.progress() == 833 && second.progress() == 833 && menu.fuel() == 500,
                        "Large recipe and fuel times fit vanilla short-sized synchronization " + kind); checks++;
            } finally { menu.setCarried(ItemStack.EMPTY); storage.clearContent(); raft.discard(); }
        }
        player.getInventory().clearContent();
        return checks;
    }
    private static void require(boolean value, String message) { if (!value) { throw new IllegalStateException(message); } }
}
