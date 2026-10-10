package dev.raftocean.client.render;

import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.raftocean.entity.RaftTransform;

/** Shared normalized anchors for the vessel meshes and their animated contents. */
public final class MachineVisualLayout {
    public static final double VESSEL_FLOOR = .18;
    public static final double VESSEL_INNER_RADIUS = .425;
    public static final double FILL_RADIUS = .405;
    private MachineVisualLayout() { }
    public static AABB canonicalBox(AABB mounted,Vec3 pivot,float mount){
        return RaftTransform.bounds(mounted.move(-pivot.x,-pivot.y,-pivot.z),-mount).move(pivot);
    }
    public static AABB envelope(List<AABB> boxes) {
        AABB bounds=boxes.getFirst();
        for(var box:boxes){bounds=bounds.minmax(box);}
        return bounds;
    }
    public static double waterHeight(AABB bounds,float fill,double wave) {
        double level=Math.clamp(fill,0,1);
        double y=VESSEL_FLOOR+.025+.68*level+Math.clamp(wave,-.008,.008);
        return bounds.minY+y*bounds.getYsize();
    }
    public static AABB water(AABB bounds,float fill,double wave) {
        double x=bounds.getCenter().x,z=bounds.getCenter().z;
        double rx=FILL_RADIUS*bounds.getXsize(),rz=FILL_RADIUS*bounds.getZsize();
        double surface=waterHeight(bounds,fill,wave);
        return new AABB(x-rx,surface,z-rz,x+rx,surface+.012*bounds.getYsize(),z+rz);
    }
}
