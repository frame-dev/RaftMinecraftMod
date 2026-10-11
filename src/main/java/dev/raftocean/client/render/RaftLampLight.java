package dev.raftocean.client.render;

import java.util.List;
import dev.raftocean.entity.RaftFurniture;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

/** Light follows moving raft fixtures without writing phantom blocks into chunks. */
public final class RaftLampLight extends MovingBlockRenderState {
    private final List<Vec3> sources;
    public RaftLampLight(List<Vec3> sources){this.sources=sources;}
    @Override public int getBrightness(LightLayer layer,BlockPos pos) {
        int nativeLight=getLightEngine().getLayerListener(layer).getLightValue(pos);
        return layer==LightLayer.BLOCK?Math.max(nativeLight,RaftFurniture.brightness(Vec3.atCenterOf(pos),sources)):nativeLight;
    }
    @Override public int getRawBrightness(BlockPos pos,int darkening) {
        return Math.max(getLightEngine().getRawBrightness(pos,darkening),RaftFurniture.brightness(Vec3.atCenterOf(pos),sources));
    }
}
