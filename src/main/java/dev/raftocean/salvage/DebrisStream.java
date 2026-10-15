package dev.raftocean.salvage;

import net.minecraft.world.phys.Vec3;

/** A forward corridor with a settling delay after course changes, never a homing target. */
public final class DebrisStream {
    public static final int SETTLE_TICKS = 200;
    public static final double DRIFT_SPEED = .018;
    private static final double TURN_COSINE = Math.cos(Math.toRadians(25));
    private Vec3 forward;
    private Vec3 corridorCourse;
    private Vec3 lastPosition;
    private long readyAt;
    private long lastUpdate = Long.MIN_VALUE;

    public void pause(long now) {
        readyAt = now + SETTLE_TICKS;
        lastUpdate = now;
        lastPosition = null;
    }

    public boolean update(Vec3 position, Vec3 course, long now) {
        Vec3 desired = horizontal(course);
        if (desired.lengthSqr() < 1.0E-6) {
            desired = forward == null ? horizontal(new Vec3(.02, 0, .008)).normalize() : forward;
        } else { desired = desired.normalize(); }
        if (forward == null || lastPosition == null || now < lastUpdate || now - lastUpdate > 40
                || desired.dot(corridorCourse) < TURN_COSINE
                || horizontal(position.subtract(lastPosition)).lengthSqr() > 64) {
            readyAt = now + SETTLE_TICKS;
            forward = desired; corridorCourse = desired;
        }
        // Small turns track the current heading without repeatedly restarting the delay.
        forward = desired;
        lastPosition = position;
        lastUpdate = now;
        return now >= readyAt;
    }

    public Vec3 forward() { return forward; }
    public static Vec3 horizontal(Vec3 vector) { return new Vec3(vector.x, 0, vector.z); }
    public static Vec3 spawnPosition(Vec3 origin, Vec3 forward, double ahead, double sideways) {
        Vec3 side = new Vec3(-forward.z, 0, forward.x);
        return origin.add(forward.scale(ahead)).add(side.scale(sideways));
    }
    public static Vec3 drift(Vec3 forward) { return horizontal(forward).normalize().scale(-DRIFT_SPEED); }
    public static boolean matchesDrift(Vec3 drift, Vec3 forward) {
        return drift.lengthSqr() > 1.0E-9 && horizontal(drift).normalize().dot(forward) < -.9;
    }
    public static boolean incoming(Vec3 relative, Vec3 forward, double halfWidth) {
        double along = horizontal(relative).dot(forward);
        double across = horizontal(relative).dot(new Vec3(-forward.z, 0, forward.x));
        return along >= -4 && along <= 64 && Math.abs(across) <= halfWidth + 4;
    }
}
