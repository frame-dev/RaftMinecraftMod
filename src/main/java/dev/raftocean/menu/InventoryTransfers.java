package dev.raftocean.menu;

import java.util.Collection;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Exact fit-only collection; no drops, creative deletion or component-blind merges. */
public final class InventoryTransfers {
    private InventoryTransfers() { }
    public static int collect(Container source, Collection<Integer> slots, Player player) {
        if(player.level().isClientSide() || player.isSpectator()) { return 0; }
        int total=0;
        for(int slot:slots) {
            var remaining=source.getItem(slot).copy();
            int original=remaining.getCount();
            for(int pass=0;pass<2 && !remaining.isEmpty();pass++) {
                for(int offset=0;offset<Inventory.INVENTORY_SIZE && !remaining.isEmpty();offset++) {
                    int target=(offset+9)%Inventory.INVENTORY_SIZE;
                    var existing=player.getInventory().getItem(target);
                    if(pass==0 && existing.isEmpty() || pass==1 && !existing.isEmpty()
                            || !existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing,remaining)) { continue; }
                    int limit=Math.min(remaining.getMaxStackSize(),player.getInventory().getMaxStackSize());
                    int fit=Math.min(remaining.getCount(),Math.max(0,limit-existing.getCount()));
                    if(fit==0) { continue; }
                    if(existing.isEmpty()) { player.getInventory().setItem(target,remaining.copyWithCount(fit)); }
                    else { existing.grow(fit); }
                    remaining.shrink(fit);
                }
            }
            int moved=original-remaining.getCount();
            if(moved>0) { source.setItem(slot,remaining);total+=moved; }
        }
        if(total>0) { source.setChanged();player.getInventory().setChanged();player.inventoryMenu.sendAllDataToRemote(); }
        return total;
    }
}
