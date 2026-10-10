package dev.raftocean.menu;

import dev.raftocean.entity.RaftEntity;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Vanilla crafting slots, recipes, screen, and item return with a moving server-side source. */
public final class RaftCraftingMenu extends CraftingMenu {
    private final RaftEntity raft;
    private final BlockPos table;

    public RaftCraftingMenu(int id, Inventory inventory, RaftEntity raft, BlockPos table) {
        super(id, inventory, movingAccess(raft, table.immutable()));
        this.raft = raft;
        this.table = table.immutable();
    }

    @Override
    public boolean stillValid(Player player) {
        Vec3 center = raft.craftingTablePosition(table);
        return player.isAlive() && !raft.isRemoved() && player.level() == raft.level()
                && center != null && player.position().distanceToSqr(center) <= 64;
    }

    private static ContainerLevelAccess movingAccess(RaftEntity raft, BlockPos table) {
        return new ContainerLevelAccess() {
            @Override
            public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> action) {
                Vec3 center = raft.craftingTablePosition(table);
                // Closing/removal must still return grid contents even after the piece disappears.
                BlockPos current = BlockPos.containing(center != null ? center : raft.position());
                return Optional.ofNullable(action.apply(raft.level(), current));
            }
        };
    }
}
