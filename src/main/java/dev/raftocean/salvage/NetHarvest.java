package dev.raftocean.salvage;

import dev.raftocean.entity.*;
import dev.raftocean.menu.RaftStationMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Move only the items that fit; leftovers remain in the shared net for the next crew member. */
public final class NetHarvest {
    private NetHarvest() { }
    public static int collect(RaftEntity raft,BlockPos pos,Player player) {
        if(raft.level().isClientSide() || player.isSpectator()
                || !RaftStationMenus.valid(raft,pos,RaftPiece.Kind.COLLECTION_NET,player)) { return 0; }
        var storage=raft.storage(pos);if(storage==null){return 0;}
        int total=0;
        for(int slot=0;slot<storage.getContainerSize();slot++) {
            var remaining=storage.getItem(slot).copy();if(remaining.isEmpty()){continue;}
            int count=remaining.getCount();
            for(int pass=0;pass<2 && !remaining.isEmpty();pass++) {
                for(int target=0;target<Inventory.INVENTORY_SIZE && !remaining.isEmpty();target++) {
                    var existing=player.getInventory().getItem(target);
                    if(pass==0 && existing.isEmpty() || pass==1 && !existing.isEmpty()
                            || !existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing,remaining)){continue;}
                    int fit=Math.min(remaining.getCount(),Math.max(0,remaining.getMaxStackSize()-existing.getCount()));
                    if(fit==0){continue;}
                    if(existing.isEmpty()){player.getInventory().setItem(target,remaining.copyWithCount(fit));}
                    else {existing.grow(fit);}
                    remaining.shrink(fit);
                }
            }
            int moved=count-remaining.getCount();
            if(moved>0){storage.setItem(slot,remaining);total+=moved;}
        }
        if(total>0){storage.setChanged();player.getInventory().setChanged();player.inventoryMenu.sendAllDataToRemote();}
        return total;
    }
}
