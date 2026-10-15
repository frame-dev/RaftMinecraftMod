package dev.raftocean.client.render;

/** Integrates cosmetic rotation without jumping when activity fades or frames are skipped. */
public final class MachineRotor {
    private double previous = Double.NaN, angle, speed;
    public float update(double now, double activity, double degreesPerTick) {
        if (!Double.isFinite(now) || !Double.isFinite(activity) || !Double.isFinite(degreesPerTick)) { return (float)angle; }
        if (Double.isNaN(previous) || now < previous || now - previous > 20) { previous = now; }
        double dt = Math.clamp(now - previous, 0, 2);
        previous = now;
        double before = speed;
        speed += (degreesPerTick * Math.clamp(activity, 0, 1) - speed) * (1 - Math.exp(-dt / 4));
        angle = (angle + (before + speed) * .5 * dt) % 360;
        return (float)angle;
    }
}
