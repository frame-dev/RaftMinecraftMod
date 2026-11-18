package dev.raftocean.api;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftEntity;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;

/** Immutable platform snapshots for addon diagnostics, read on the owning game thread. */
public final class RaftPlatforms {
    private RaftPlatforms() { }

    public record Status(BlockPos cell, boolean collectionNet, int health, boolean armored,
            boolean protectionActive, boolean exposed) {
        public Status {
            cell=cell.immutable();
        }
    }

    /** Returns empty for removed rafts and cells without a sea-level foundation or net. */
    public static Optional<Status> inspect(RaftEntity raft, BlockPos cell) {
        if(raft.isRemoved() || !raft.seaLevelPlatform(cell))return Optional.empty();
        boolean armored=raft.reinforcedFoundation(cell);
        return Optional.of(new Status(cell, !raft.getBlocks().containsKey(cell),
                raft.foundationHealth(cell), armored, armored && RaftConfig.REINFORCEMENT.get(),
                raft.edgeFoundation(cell)));
    }

    /** A detached, immutable snapshot. Later raft changes require a new call. */
    public static List<Status> snapshot(RaftEntity raft) {
        if(raft.isRemoved())return List.of();
        return raft.seaLevelCells().stream().map(cell->inspect(raft,cell).orElseThrow()).toList();
    }
}
