package dev.raftocean.menu;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;

/** Deterministic side-by-side pairs; inventories remain owned by their original halves. */
public final class RaftChests {
    private record Cached(long revision, Map<BlockPos, BlockPos> pairs) { }
    private static final Map<RaftEntity, Cached> CACHE = new WeakHashMap<>();
    private RaftChests() { }
    public static synchronized Map<BlockPos, BlockPos> pairs(RaftEntity raft) {
        var cached = CACHE.get(raft);
        if (cached != null && cached.revision() == raft.layoutRevision()) { return cached.pairs(); }
        Map<BlockPos, BlockPos> result = new HashMap<>();
        var positions = raft.pieces().entrySet().stream().filter(e -> e.getValue().kind() == RaftPiece.Kind.CHEST)
                .map(Map.Entry::getKey).sorted(Comparator.comparingInt((BlockPos cell) -> cell.getY())
                        .thenComparingInt(cell -> cell.getX()).thenComparingInt(cell -> cell.getZ())).toList();
        for (var pos : positions) {
            if (result.containsKey(pos)) { continue; }
            var piece = raft.pieces().get(pos);
            var neighbors = piece.rotation() % 2 == 0 ? List.of(pos.east(), pos.west()) : List.of(pos.south(), pos.north());
            for (var neighbor : neighbors) {
                var other = raft.pieces().get(neighbor);
                if (other != null && other.kind() == RaftPiece.Kind.CHEST && other.rotation() == piece.rotation()
                        && !result.containsKey(neighbor)) {
                    result.put(pos, neighbor); result.put(neighbor, pos); break;
                }
            }
        }
        var pairs = Map.copyOf(result);
        CACHE.put(raft, new Cached(raft.layoutRevision(), pairs));
        return pairs;
    }
    public static ChestMenu menu(int id, Inventory inventory, RaftEntity raft, BlockPos pos) {
        BlockPos partner = pairs(raft).get(pos);
        if (partner == null) { return ChestMenu.threeRows(id, inventory, raft.storage(pos)); }
        boolean first = pos.getX() < partner.getX() || pos.getX() == partner.getX() && pos.getZ() < partner.getZ();
        var contents = new CompoundContainer(raft.storage(first ? pos : partner), raft.storage(first ? partner : pos));
        return new ChestMenu(net.minecraft.world.inventory.MenuType.GENERIC_9x6, id, inventory, contents, 6) {
            @Override public boolean stillValid(Player player) {
                return partner.equals(pairs(raft).get(pos)) && super.stillValid(player);
            }
        };
    }
}
