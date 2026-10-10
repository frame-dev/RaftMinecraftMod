package dev.raftocean.menu;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;

/** One shared, persisted 27-slot inventory belonging to a particular raft piece. */
public final class RaftStorage extends SimpleContainer {
    private final RaftEntity raft;
    private final BlockPos pos;
    private final RaftPiece.Kind kind;
    public String status = "supplies";
    public int duration;
    public int progress;
    public int fuelTicks;
    public int fuelCapacity;
    public int rawWater;
    public int cleanWater;
    public int energy;
    public RaftPiece.Kind kind() { return kind; }
    public boolean enabled;
    public boolean paused;
    public int trim;
    public String work = "";
    public float fuelSpeed = 1;

    public RaftStorage(RaftEntity raft, BlockPos pos, RaftPiece.Kind kind) {
        super(27);
        this.raft = raft;
        this.pos = pos.immutable();
        this.kind = kind;
    }

    @Override
    public boolean stillValid(Player player) {
        return raft.storage(pos) == this && RaftStationMenus.valid(raft, pos, kind, player);
    }
}
