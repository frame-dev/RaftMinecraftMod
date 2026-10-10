package dev.raftocean.api;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import dev.raftocean.entity.RaftEntity;

/** Common-side extension definition. Geometry is relative to the anchor deck surface. */
public record RaftBuildable(Identifier id, Supplier<Item> item, Supplier<Block> visual,
        int width, int depth, int height, double mass, boolean floorMounted,
        List<AABB> geometry, List<AABB> collision, Interaction interaction) {
    @FunctionalInterface
    public interface Interaction {
        InteractionResult use(RaftEntity raft, BlockPos anchor, Player player, InteractionHand hand);
    }
    public RaftBuildable {
        Objects.requireNonNull(id);Objects.requireNonNull(item);Objects.requireNonNull(visual);
        Objects.requireNonNull(interaction);
        geometry=List.copyOf(geometry);collision=List.copyOf(collision);
        if(width<1 || width>4 || depth<1 || depth>4 || height<1 || height>16
                || !Double.isFinite(mass) || mass<0 || mass>256 || geometry.isEmpty()
                || geometry.size()>128 || collision.size()>128) {
            throw new IllegalArgumentException("Invalid raft buildable dimensions: "+id);
        }
        for(var boxes:List.of(geometry,collision))for(var box:boxes) {
            double[] values={box.minX,box.minY,box.minZ,box.maxX,box.maxY,box.maxZ};
            for(double value:values)if(!Double.isFinite(value))throw new IllegalArgumentException("Nonfinite raft geometry: "+id);
            if(box.minX<-.5 || box.maxX>width-.5 || box.minZ<-.5 || box.maxZ>depth-.5
                    || box.minY<0 || box.maxY>height || box.getXsize()<=0 || box.getYsize()<=0 || box.getZsize()<=0) {
                throw new IllegalArgumentException("Geometry outside reserved footprint: "+id);
            }
        }
    }
}
