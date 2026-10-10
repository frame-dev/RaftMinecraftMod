package dev.raftocean.mixin;

import dev.raftocean.entity.RaftEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Supply exact deck cells, rather than a solid box spanning holes in a raft. */
@Mixin(EntityGetter.class)
public interface RaftCollisionMixin {
    @Inject(method = "getEntityCollisions", at = @At("RETURN"), cancellable = true)
    private void raftOcean$foundationCollisions(Entity source, AABB area,
            CallbackInfoReturnable<List<VoxelShape>> callback) {
        EntityGetter getter = (EntityGetter) this;
        List<RaftEntity> rafts = getter.getEntitiesOfClass(RaftEntity.class, area.inflate(1.0E-7),
                raft -> raft != source && !raft.isRemoved() && !dev.raftocean.entity.RaftPlayerSupport.ignoringCollision(raft));
        if (rafts.isEmpty()) {
            return;
        }
        List<VoxelShape> shapes = new ArrayList<>(callback.getReturnValue());
        for (RaftEntity raft : rafts) {
            for (AABB cell : raft.getCollisionBoxes()) {
                if (cell.intersects(area.inflate(1.0E-7))) {
                    shapes.add(Shapes.create(cell));
                }
            }
        }
        callback.setReturnValue(shapes);
    }
}
