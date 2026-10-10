package dev.raftocean.client.render;

/** Frame-rate independent cosmetic motion. Never changes entity position or collision. */
final class VisualMotion {
    private double time = Double.NaN;
    float speed, bank, pitch, action, glide;
    double cycle;

    void update(double now, double horizontalSpeed, double verticalSpeed, float turn, boolean active, boolean diving) {
        if (Double.isNaN(time) || now < time || now - time > 20) {
            time = now;
            speed = (float)Math.clamp(horizontalSpeed, 0, .5);
        }
        double dt = Math.clamp(now - time, 0, 2);
        time = now;
        float blend = (float)(1 - Math.exp(-dt / 3));
        speed += ((float)Math.clamp(horizontalSpeed, 0, .5) - speed) * blend;
        bank += ((float)Math.clamp(-turn * 3, -22, 22) - bank) * blend;
        pitch += ((float)Math.clamp(-verticalSpeed * 100, -25, 25) - pitch) * blend;
        action += ((active ? 1 : 0) - action) * (float)(1 - Math.exp(-dt / (active ? 1.5 : 5)));
        glide += ((diving ? 1 : 0) - glide) * blend;
        cycle += dt * (.12 + speed * 3);
    }
}
