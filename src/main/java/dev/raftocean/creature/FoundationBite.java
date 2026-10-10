package dev.raftocean.creature;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.navigation.RaftNavigation;
import net.minecraft.world.phys.Vec3;

/** Shared server jaw contact and client foundation-bite pose dimensions. */
public final class FoundationBite {
    public static final double BODY_DISTANCE=1.75;
    public static final double MODEL_REACH=.315;
    public static final double JAW_FORWARD=.935;
    public static final double JAW_HEIGHT=.34;
    public static final double BODY_HEIGHT=RaftEntity.DECK_HEIGHT*.5-JAW_HEIGHT;
    public static Vec3 jaw(Vec3 origin,float yaw) {
        return origin.add(RaftNavigation.direction(yaw).scale(JAW_FORWARD+MODEL_REACH)).add(0,JAW_HEIGHT,0);
    }
    private FoundationBite() { }
}
