package dev.raftocean.survival;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.RaftChests;
import dev.raftocean.menu.RaftStationMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Matching-component backpack deposits; hotbar, armor and offhand are never touched. */
public final class QuickStash {
    private QuickStash() { }
    public static int store(RaftEntity raft,BlockPos pos,Player player) {
        if(raft.level().isClientSide() || player.isSpectator() || !dev.raftocean.config.RaftConfig.QUICK_STASH.get())return 0;
        var piece=raft.pieces().get(pos);
        if(piece==null || piece.kind()!=RaftPiece.Kind.CHEST && piece.kind()!=RaftPiece.Kind.BARREL
                || !RaftStationMenus.valid(raft,pos,piece.kind(),player))return 0;
        Container target=raft.storage(pos);
        if(piece.kind()==RaftPiece.Kind.CHEST) {
            var partner=RaftChests.pairs(raft).get(pos);
            if(partner!=null)target=new CompoundContainer(target,raft.storage(partner));
        }
        int moved=0;
        for(int slot=9;slot<36;slot++) {
            var source=player.getInventory().getItem(slot);if(source.isEmpty())continue;
            boolean matching=false;
            for(int cell=0;cell<target.getContainerSize();cell++) {
                if(ItemStack.isSameItemSameComponents(source,target.getItem(cell))){matching=true;break;}
            }
            if(!matching)continue;
            for(int pass=0;pass<2 && !source.isEmpty();pass++) {
                for(int cell=0;cell<target.getContainerSize() && !source.isEmpty();cell++) {
                    var existing=target.getItem(cell);
                    if(pass==0?existing.isEmpty() || !ItemStack.isSameItemSameComponents(source,existing):!existing.isEmpty())continue;
                    int fit=Math.min(source.getCount(),Math.max(0,Math.min(source.getMaxStackSize(),target.getMaxStackSize())-existing.getCount()));
                    if(fit==0)continue;
                    if(existing.isEmpty())target.setItem(cell,source.copyWithCount(fit));else existing.grow(fit);
                    source.shrink(fit);moved+=fit;
                }
            }
        }
        if(moved>0){target.setChanged();player.getInventory().setChanged();player.inventoryMenu.sendAllDataToRemote();}
        return moved;
    }
}
