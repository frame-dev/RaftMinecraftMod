package dev.raftocean.mixin;

import dev.raftocean.entity.RaftSpatialIndex;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep distant deck collision, player contact and picking in the same broad phase. */
@Mixin(Level.class)
public abstract class RaftSpatialQueryMixin {
    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
            at = @At("RETURN"))
    private void raftOcean$wideHullEntities(Entity except, AABB area, Predicate<? super Entity> predicate,
            CallbackInfoReturnable<List<Entity>> callback) {
        List<Entity> output = callback.getReturnValue();
        for (var raft : RaftSpatialIndex.intersecting((Level) (Object) this, area)) {
            if (raft != except && !output.contains(raft) && predicate.test(raft)) { output.add(raft); }
        }
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V",
            at = @At("TAIL"))
    private <T extends Entity> void raftOcean$wideHullTypedEntities(EntityTypeTest<Entity, T> type,
            AABB area, Predicate<? super T> predicate, List<? super T> output, int maxResults,
            CallbackInfo callback) {
        if (output.size() >= maxResults) { return; }
        for (var raft : RaftSpatialIndex.intersecting((Level) (Object) this, area)) {
            T cast = type.tryCast(raft);
            if (cast != null && !output.contains(cast) && predicate.test(cast)) {
                output.add(cast);
                if (output.size() >= maxResults) { return; }
            }
        }
    }

    @Inject(method = "hasEntities", at = @At("RETURN"), cancellable = true)
    private <T extends Entity> void raftOcean$hasWideHull(EntityTypeTest<Entity, T> type,
            AABB area, Predicate<? super T> predicate, CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue()) { return; }
        for (var raft : RaftSpatialIndex.intersecting((Level) (Object) this, area)) {
            T cast = type.tryCast(raft);
            if (cast != null && predicate.test(cast)) { callback.setReturnValue(true); return; }
        }
    }
}
