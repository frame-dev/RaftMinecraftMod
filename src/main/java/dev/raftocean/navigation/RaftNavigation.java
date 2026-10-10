package dev.raftocean.navigation;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.RaftStorage;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;

/** Server-owned wind and propulsion in the rotating hull coordinate frame. */
public final class RaftNavigation {
    public static final double MAX_SPEED = .16;
    private final RaftEntity raft;
    private final Set<BlockPos> clientEnabled = new HashSet<>();
    private final Map<BlockPos, Integer> clientTrims = new HashMap<>();
    private final Set<BlockPos> running = new HashSet<>();
    private Vec3 paddle = Vec3.ZERO;
    private double heading;
    private double windHeading = -60;
    private double windStrength = 1;
    private int engines, sails;
    private boolean anchored;

    public RaftNavigation(RaftEntity raft) { this.raft = raft; }
    public double heading() { return heading; }
    public double windHeading() { return windHeading; }
    public double windStrength() { return windStrength; }
    public int engines() { return engines; }
    public int sails() { return sails; }
    public boolean anchored() {
        return raft.level().isClientSide() ? anchored : raft.pieces().entrySet().stream()
                .anyMatch(e -> e.getValue().kind().anchor() && raft.storage(e.getKey()).enabled);
    }
    public boolean enabled(BlockPos pos) {
        if (raft.level().isClientSide()) { return clientEnabled.contains(pos); }
        var storage = raft.storage(pos);
        return storage != null && storage.enabled;
    }
    public int trim(BlockPos pos) { return raft.level().isClientSide() ? clientTrims.getOrDefault(pos, 0) : raft.storage(pos).trim; }
    public boolean running(BlockPos pos) { return running.contains(pos); }
    public static double normalize(double angle) { return Double.isFinite(angle) ? (angle % 360 + 360) % 360 : 0; }
    public void setHeading(double angle) { if (Double.isFinite(angle)) { heading = normalize(angle); } }
    public void turn(int degrees) { setHeading(heading + degrees); sync(); }
    public static Vec3 direction(double yaw) {
        double radians = Math.toRadians(yaw);
        return new Vec3(-Math.sin(radians), 0, Math.cos(radians));
    }
    public static double sailDrive(double bearing, double windBearing, double strength) {
        double alignment = direction(bearing).dot(direction(windBearing));
        // A broad reach produces useful lateral drive; pointing directly into the wind stalls.
        double drive = alignment <= -.75 ? 0 : (.6 + .4 * alignment) * Math.min(1, (alignment + .75) / .25);
        return .08 * dev.raftocean.config.RaftConfig.SAIL_THRUST.get() * Math.clamp(strength, 0, 3) * drive;
    }
    public boolean stroke(Vec3 look) {
        Vec3 horizontal = new Vec3(look.x, 0, look.z);
        if (dev.raftocean.config.RaftConfig.PADDLE_THRUST.get()==0 || anchored() || horizontal.lengthSqr() < .0001) { return false; }
        double scale = Math.clamp(Math.sqrt(108 / raft.getMass()), .25, 1);
        paddle = cap(paddle.add(horizontal.normalize().scale(.06 * scale * dev.raftocean.config.RaftConfig.PADDLE_THRUST.get())), .09 * dev.raftocean.config.RaftConfig.PADDLE_THRUST.get());
        return true;
    }
    private static Vec3 cap(Vec3 vector, double maximum) {
        return vector.lengthSqr() > maximum * maximum ? vector.normalize().scale(maximum) : vector;
    }

    public Vec3 target(boolean afloat) {
        if (!(raft.level() instanceof ServerLevel level)) { return Vec3.ZERO; }
        windHeading = normalize(-60 + 25 * Math.sin(level.getGameTime() / 6000.0));
        windStrength = (.85 + .15 * Math.sin(level.getGameTime() / 2800.0)) * (level.isRaining() ? 1.15 : 1) * dev.raftocean.config.RaftConfig.WIND.get();
        anchored = anchored(); engines = 0; sails = 0; running.clear();
        Vec3 thrust = Vec3.ZERO;
        if (!anchored && afloat) {
            double massScale = Math.clamp(Math.sqrt(108 / raft.getMass()), .25, 1);
            for (var entry : raft.pieces().entrySet()) {
                var kind = entry.getValue().kind();
                if (!kind.navigation()) { continue; }
                var storage = raft.storage(entry.getKey());
                if (!storage.enabled) { continue; }
                if (kind == RaftPiece.Kind.SAIL) {
                    sails++;
                    double bearing = raft.getYRot() + storage.trim;
                    thrust = thrust.add(direction(bearing).scale(sailDrive(bearing, windHeading, windStrength) * massScale));
                } else if (kind == RaftPiece.Kind.ENGINE && dev.raftocean.config.RaftConfig.ENGINE_THRUST.get()>0 && fuel(level, entry.getKey(), storage)) {
                    engines++; running.add(entry.getKey());
                    double bearing = raft.getYRot() + entry.getValue().rotation() * 90 + storage.trim;
                    double engineScale=Math.min(1,dev.raftocean.config.RaftConfig.ENGINE_FOUNDATIONS.get()/(double)raft.getBlocks().size());
                    thrust = thrust.add(direction(bearing).scale(.08 * dev.raftocean.config.RaftConfig.ENGINE_THRUST.get() * Math.min(2, storage.fuelSpeed) * engineScale));
                }
            }
        }
        Vec3 current = new Vec3(raft.getCurrentX(), 0, raft.getCurrentZ()).scale(dev.raftocean.config.RaftConfig.CURRENT.get());
        Vec3 result = anchored ? Vec3.ZERO : cap(current.add(thrust).add(paddle), dev.raftocean.config.RaftConfig.MAX_SPEED.get());
        paddle = anchored || !afloat ? Vec3.ZERO : paddle.scale(.90);
        if (raft.tickCount % 20 == 0) { sync(); }
        return result;
    }

    private boolean fuel(ServerLevel level, BlockPos pos, RaftStorage storage) {
        if (storage.fuelTicks == 0) {
            ItemStack stack = storage.getItem(1);
            var reader = new FuelReader(BlockPos.containing(raft.stationPosition(pos, RaftPiece.Kind.ENGINE)));
            reader.setLevel(level); reader.setItem(1, stack.copy());
            boolean biofuel = stack.is(dev.raftocean.survival.Automation.BIOFUEL.get());
            int duration = biofuel ? dev.raftocean.config.RaftConfig.BIOFUEL_ENGINE_TICKS.get() : reader.duration(level, stack);
            float speed = biofuel ? 1 : reader.speed(level, stack);
            var remainder = stack.getCraftingRemainder();
            if (duration <= 0 || !Float.isFinite(speed) || speed <= 0 || remainder != null && stack.getCount() != 1) { return false; }
            storage.fuelCapacity = storage.fuelTicks = Math.clamp((int)(duration * dev.raftocean.config.RaftConfig.ENGINE_FUEL.get()),1,200000);
            storage.fuelSpeed = Math.clamp(speed, .01F, 100F);
            stack.shrink(1);
            if (stack.isEmpty() && remainder != null) { storage.setItem(1, remainder.create()); }
        }
        storage.fuelTicks--;
        storage.setChanged();
        return true;
    }

    public void sync() {
        if (raft.level().isClientSide()) { return; }
        String enabled = raft.pieces().entrySet().stream().filter(e -> e.getValue().kind().navigation())
                .map(e -> e.getKey().getX() + "," + e.getKey().getY() + "," + e.getKey().getZ()
                        + "," + trim(e.getKey()) + "," + (enabled(e.getKey()) ? 1 : 0) + "," + (running(e.getKey()) ? 1 : 0))
                .sorted().collect(java.util.stream.Collectors.joining("/"));
        raft.publishNavigation(heading + ";" + windHeading + ";" + windStrength + ";" + engines + ";" + sails + ";"
                + (anchored() ? 1 : 0) + ";" + enabled);
    }
    public void acceptSnapshot(String encoded) {
        if (encoded.length() > 24000) { return; }
        String[] fields = encoded.split(";", -1);
        if (fields.length != 7) { return; }
        try {
            setHeading(Double.parseDouble(fields[0])); windHeading = normalize(Double.parseDouble(fields[1]));
            double strength = Double.parseDouble(fields[2]);
            windStrength = Double.isFinite(strength) ? Math.clamp(strength, 0, 3) : 1;
            engines = Math.clamp(Integer.parseInt(fields[3]), 0, 128); sails = Math.clamp(Integer.parseInt(fields[4]), 0, 128);
            anchored = fields[5].equals("1"); clientEnabled.clear(); clientTrims.clear(); running.clear();
            if (!fields[6].isEmpty()) {
                for (String entry : fields[6].split("/")) {
                    String[] cell = entry.split(",");
                    if (cell.length == 3 || cell.length == 6) {
                        BlockPos pos = new BlockPos(Integer.parseInt(cell[0]), Integer.parseInt(cell[1]), Integer.parseInt(cell[2]));
                        if (cell.length == 3 || cell[4].equals("1")) { clientEnabled.add(pos); }
                        if (cell.length == 6) {
                            clientTrims.put(pos, Math.clamp(Integer.parseInt(cell[3]), -180, 180));
                            if (cell[5].equals("1")) { running.add(pos); }
                        }
                    }
                }
            }
        } catch (NumberFormatException ignored) { clientEnabled.clear(); }
    }
    private static final class FuelReader extends FurnaceBlockEntity {
        FuelReader(BlockPos pos) { super(pos, Blocks.FURNACE.defaultBlockState()); }
        int duration(ServerLevel level, ItemStack stack) { return getBurnDuration(level, stack); }
        float speed(ServerLevel level, ItemStack stack) { return getSpeedMultiplier(level, stack); }
    }
}
