package dev.raftocean.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.raftocean.salvage.DebrisEntity;
import dev.raftocean.salvage.SalvageHookEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Original polygon models for salvage, a curved hook and continuous rope segments. */
public final class SalvageRenderer<T extends Entity> extends EntityRenderer<T, SalvageRenderer.State> {
    public static final class State extends EntityRenderState {
        final List<Cell> cells = new ArrayList<>();
    }
    private record Cell(AABB box,String mesh,Vec3 direction) { }
    public SalvageRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0; }
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(T entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick); state.cells.clear();
        if (entity instanceof DebrisEntity debris) {
            var kind = debris.kind();
            switch (kind) {
                case PLANK -> add(entity, state, kind, new AABB(-.45, 0, -.1, .45, .07, .1));
                case LOG -> add(entity, state, kind, new AABB(-.45, 0, -.18, .45, .24, .18));
                case BARREL -> {
                    add(entity, state, kind, new AABB(-.3, 0, -.3, .3, .4, .3));
                    add(entity, state, kind, new AABB(-.34, .08, -.34, .34, .13, .34));
                    add(entity, state, kind, new AABB(-.34, .29, -.34, .34, .34, .34));
                }
                case CRATE -> add(entity, state, kind, new AABB(-.35, 0, -.35, .35, .35, .35));
                case SCRAP -> add(entity, state, kind, new AABB(-.22, 0, -.18, .22, .09, .18));
                case LEAVES -> add(entity, state, kind, new AABB(-.35, 0, -.12, .35, .035, .12));
                case BOTTLE -> {
                    add(entity, state, kind, new AABB(-.12, 0, -.25, .12, .16, .15));
                    add(entity, state, kind, new AABB(-.055, .025, .15, .055, .13, .3));
                }
            }
        } else if (entity instanceof SalvageHookEntity hook) {
            add(entity, state, DebrisEntity.Kind.SCRAP, new AABB(-.12, 0, -.12, .12, .25, .12),"hook_body");
            Entity owner = entity.level().getEntity(hook.ownerId());
            if (owner != null) {
                Vec3 rope = owner.getEyePosition(partialTick).subtract(state.x, state.y, state.z).add(0, -.2, 0);
                if(rope.lengthSqr()<.000001){return;}
                int links = Math.min(64, Math.max(1, (int) Math.ceil(rope.length() * 2)));
                for (int i = 0; i < links; i++) {
                    Vec3 point = rope.scale((i + .5) / links);
                    double half=rope.length()/links/2;
                    state.cells.add(new Cell(new AABB(point.x-.016,point.y-half,point.z-.016,point.x+.016,point.y+half,point.z+.016),"hook_rope",rope.normalize()));
                }
            }
        }
    }
    private void add(T entity, State state, DebrisEntity.Kind kind, AABB box) {
        add(entity,state,kind,box,"salvage_"+kind.name().toLowerCase(java.util.Locale.ROOT));
    }
    private void add(T entity,State state,DebrisEntity.Kind kind,AABB box,String mesh){
        state.cells.add(new Cell(box,mesh,null));
    }
    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Cell cell : state.cells) {
            pose.pushPose(); var box = cell.box();
            if(cell.direction()!=null){
                Vec3 center=box.getCenter();pose.translate(center.x,center.y,center.z);
                var direction=cell.direction();
                if(direction.lengthSqr()>.00001){pose.rotate(new org.joml.Quaternionf().rotationTo(new org.joml.Vector3f(0,1,0),new org.joml.Vector3f((float)direction.x,(float)direction.y,(float)direction.z)));}
                pose.scale((float)box.getXsize(),(float)box.getYsize(),(float)box.getZsize());pose.translate(-.5,-.5,-.5);
            }else{pose.translate(box.minX,box.minY,box.minZ);pose.scale((float)box.getXsize(),(float)box.getYsize(),(float)box.getZsize());}
            EntityMeshes.submit(cell.mesh(),pose,collector,state.lightCoords,state.outlineColor);pose.popPose();
        }
        super.submit(state, pose, collector, camera);
    }
}
