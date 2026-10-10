package dev.raftocean.entity;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RaftTransformTest {
    public static void run() {
        int checks = 0;
        AABB foundation = new AABB(-.5, 0, -.5, .5, .25, .5);
        Vec3 origin = new Vec3(80, 63, -40);
        for (int yaw = 0; yaw < 360; yaw += 5) {
            var strips = RaftTransform.colliders(foundation, yaw);
            require(strips.size() <= 23, "Collider growth stays bounded per foundation"); checks++;
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    Vec3 local = new Vec3(x*.1, .125, z*.1);
                    Vec3 rotated = RaftTransform.rotate(local, yaw);
                    require(strips.stream().anyMatch(box -> box.inflate(1E-8).contains(rotated)), "Rotated deck has no interior holes"); checks++;
                    require(RaftTransform.local(RaftTransform.world(local, origin, yaw), origin, yaw).distanceToSqr(local)<1E-12,
                            "Coordinate conversion round trip"); checks++;
                }
            }
            Vec3 outside = RaftTransform.rotate(new Vec3(.65,.125,.65), yaw);
            require(strips.stream().noneMatch(box -> box.contains(outside)), "Corners beyond the deck remain empty"); checks++;
        }
        require(RaftTransform.rotate(new Vec3(0,0,1),90).distanceToSqr(new Vec3(-1,0,0))<1E-12,"Minecraft yaw 90 points west");checks++;
        require(RaftTransform.difference(1,359)==2 && RaftTransform.difference(359,1)==-2,"Shortest turn crosses north wrap");checks++;
        require(RaftTransform.colliders(foundation,90).size()==1,"Quarter turns retain exact collider");checks++;
        System.out.println(checks + " raft rotation/coordinate regression checks passed.");
    }
    private static void require(boolean condition,String message) { if(!condition){throw new AssertionError(message);} }
}
