package dev.raftocean.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** One coordinate convention for physics, interaction, rendering, and movement packets. */
public final class RaftTransform {
    private RaftTransform() { }
    public static Vec3 rotate(Vec3 point, double yaw) {
        double radians = Math.toRadians(yaw), c = Math.cos(radians), s = Math.sin(radians);
        return new Vec3(c * point.x - s * point.z, point.y, s * point.x + c * point.z);
    }
    public static Vec3 world(Vec3 local, Vec3 origin, double yaw) { return origin.add(rotate(local, yaw)); }
    public static Vec3 local(Vec3 world, Vec3 origin, double yaw) { return rotate(world.subtract(origin), -yaw); }
    public static double difference(double target, double current) { return ((target - current) % 360 + 540) % 360 - 180; }
    public static AABB bounds(AABB box, double yaw) {
        Vec3[] corners = corners(box, yaw);
        double minX = Double.POSITIVE_INFINITY, minZ = minX, maxX = -minX, maxZ = -minX;
        for (Vec3 p : corners) { minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x); minZ = Math.min(minZ, p.z); maxZ = Math.max(maxZ, p.z); }
        return new AABB(minX, box.minY, minZ, maxX, box.maxY, maxZ);
    }
    private static Vec3[] corners(AABB b, double yaw) {
        return new Vec3[]{rotate(new Vec3(b.minX, 0, b.minZ), yaw), rotate(new Vec3(b.maxX, 0, b.minZ), yaw),
                rotate(new Vec3(b.maxX, 0, b.maxZ), yaw), rotate(new Vec3(b.minX, 0, b.maxZ), yaw)};
    }
    /** Conservative 1/16-block strips preserve holes without filling a rotated piece's whole bounding box. */
    public static List<AABB> colliders(AABB box, double yaw) {
        AABB bounds = bounds(box, yaw);
        if (Math.abs(Math.sin(Math.toRadians(yaw * 2))) < 1.0E-8) { return List.of(bounds); }
        Vec3[] polygon = corners(box, yaw);
        List<AABB> result = new ArrayList<>();
        int slices = Math.max(1, (int)Math.ceil(bounds.getZsize() * 16));
        double step = bounds.getZsize() / slices;
        for (int i = 0; i < slices; i++) {
            double z0 = bounds.minZ + i * step, z1 = i == slices - 1 ? bounds.maxZ : z0 + step;
            double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
            for (int edge = 0; edge < 4; edge++) {
                Vec3 a = polygon[edge], b = polygon[(edge + 1) % 4];
                if (a.z >= z0 - 1.0E-9 && a.z <= z1 + 1.0E-9) { minX = Math.min(minX, a.x); maxX = Math.max(maxX, a.x); }
                if (Math.abs(b.z - a.z) > 1.0E-10) {
                    for (double z : new double[]{z0, z1}) {
                        double t = (z - a.z) / (b.z - a.z);
                        if (t >= -1.0E-9 && t <= 1 + 1.0E-9) {
                            double x = a.x + Math.clamp(t, 0, 1) * (b.x - a.x);
                            minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                        }
                    }
                }
            }
            if (maxX > minX) { result.add(new AABB(minX, box.minY, z0, maxX, box.maxY, z1)); }
        }
        return result;
    }
}
