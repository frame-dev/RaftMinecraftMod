package dev.raftocean.salvage;

import dev.raftocean.entity.*;
import dev.raftocean.menu.RaftStationMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/** Move only the items that fit; leftovers remain in the shared net for the next crew member. */
public final class NetHarvest {
    private NetHarvest() { }
    public static int remaining(RaftEntity raft,BlockPos pos) {
        var piece=raft.pieces().get(pos);
        if(piece==null || piece.kind()!=RaftPiece.Kind.COLLECTION_NET)return 0;
        var storage=raft.storage(pos);int count=0;
        for(int slot=0;slot<storage.getContainerSize();slot++)count+=storage.getItem(slot).getCount();
        return count;
    }
    public static int collect(RaftEntity raft,BlockPos pos,Player player) {
        if(raft.level().isClientSide() || player.isSpectator()
                || !RaftStationMenus.valid(raft,pos,RaftPiece.Kind.COLLECTION_NET,player)) { return 0; }
        var storage=raft.storage(pos);if(storage==null){return 0;}
        return dev.raftocean.menu.InventoryTransfers.collect(storage,
                java.util.stream.IntStream.range(0,storage.getContainerSize()).boxed().toList(),player);
    }
}
