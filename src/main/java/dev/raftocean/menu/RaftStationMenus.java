package dev.raftocean.menu;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Native screens and processing with moving server-side reach checks. */
public final class RaftStationMenus {
    private RaftStationMenus() { }

    public static boolean valid(RaftEntity raft, BlockPos pos, RaftPiece.Kind kind, Player player) {
        Vec3 center = raft.stationPosition(pos, kind);
        return player.isAlive() && !raft.isRemoved() && player.level() == raft.level()
                && center != null && player.position().distanceToSqr(center) <= 64;
    }

    public static ContainerLevelAccess access(RaftEntity raft, BlockPos pos, RaftPiece.Kind kind) {
        BlockPos local = pos.immutable();
        return new ContainerLevelAccess() {
            @Override
            public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> action) {
                Vec3 center = raft.stationPosition(local, kind);
                return Optional.ofNullable(action.apply(raft.level(), BlockPos.containing(center != null ? center : raft.position())));
            }
        };
    }

    public static AbstractContainerMenu create(int id, Inventory inventory, RaftEntity raft, BlockPos pos, RaftPiece.Kind kind) {
        ContainerLevelAccess access = access(raft, pos, kind);
        return switch (kind) {
            case RESEARCH_TABLE -> new ResearchMenu(id, inventory, raft, pos);
            case CRAFTING_TABLE, WIDE_CRAFTING_TABLE -> new RaftCraftingMenu(id, inventory, raft, pos);
            case CHEST -> RaftChests.menu(id, inventory, raft, pos);
            case ENDER_CHEST -> new ChestMenu(MenuType.GENERIC_9x3,id,inventory,inventory.player.getEnderChestInventory(),3){
                @Override public boolean stillValid(Player player){return valid(raft,pos,kind,player);}
            };
            case ANVIL -> new RaftAnvilMenu(id,inventory,raft,pos);
            case ENCHANTING_TABLE -> new RaftEnchantingMenu(id,inventory,raft,pos);
            case BARREL, COLLECTION_NET -> ChestMenu.threeRows(id, inventory, raft.storage(pos));
            case BASIC_GRILL, BASIC_PURIFIER, DRYING_RACK, PURIFIER, RAIN_COLLECTOR, GRILL, COOKING_POT, PLANTER, TREE_PLANTER, SMELTER, WATER_TANK, FUEL_TANK, BIOREFINER, BIOFUEL_GENERATOR, ELECTRIC_PURIFIER, SPRINKLER, BATTERY_CHARGER -> new SurvivalMenu(id, inventory, kind, raft.storage(pos));
            case SAIL, STEERING_WHEEL, ENGINE, DISPOSABLE_ANCHOR, ANCHOR -> new NavigationMenu(id, inventory, raft, pos, kind);
            case STONECUTTER -> new StonecutterMenu(id, inventory, access) {
                @Override public boolean stillValid(Player player) { return valid(raft, pos, kind, player); }
            };
            case SMITHING_TABLE -> new SmithingMenu(id, inventory, access) {
                @Override public boolean stillValid(Player player) { return valid(raft, pos, kind, player); }
            };
            case GRINDSTONE -> new GrindstoneMenu(id, inventory, access) {
                @Override public boolean stillValid(Player player) { return valid(raft, pos, kind, player); }
            };
            case LOOM -> new LoomMenu(id, inventory, access) {
                @Override public boolean stillValid(Player player) { return valid(raft, pos, kind, player); }
            };
            case CARTOGRAPHY_TABLE -> new CartographyTableMenu(id, inventory, access) {
                @Override public boolean stillValid(Player player) { return valid(raft, pos, kind, player); }
            };
            default -> throw new IllegalArgumentException("Not a raft station: " + kind);
        };
    }
}
