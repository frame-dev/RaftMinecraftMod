package dev.raftocean.mixin;

import dev.raftocean.entity.RaftEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Extend vanilla climbing to ladders owned by a moving raft. */
@Mixin(LivingEntity.class)
public abstract class RaftLadderMixin {
    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void raftOcean$movingLadders(CallbackInfoReturnable<Boolean> callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!callback.getReturnValue() && !entity.isSpectator()
                && entity.level().getEntitiesOfClass(RaftEntity.class, entity.getBoundingBox().inflate(.2),
                        raft -> raft.isOnLadder(entity.getBoundingBox())).size() > 0) {
            callback.setReturnValue(true);
        }
    }
}
