package dev.raftocean.client.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RaftRenderState extends EntityRenderState {
    public final List<DeckCell> cells = new ArrayList<>();
    public float hullYaw;
    public dev.raftocean.entity.RaftEntity.Placement ghost;
    public record DeckCell(AABB localBox, MovingBlockRenderState block, Vec3 pivot,
            float mountYaw, float turn, float spin, float flutter) { }
}
