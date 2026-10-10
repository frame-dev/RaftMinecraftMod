package dev.raftocean.entity;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Executable geometry regressions in a dedicated source set; no game/display required. */
public final class RaftContactTest {
    private static final AABB DECK = new AABB(-1.5, 62.85, -1.5, 1.5, 63.10, 1.5);

    public static void main(String[] args) {
        // A fast fall traverses a thin deck: overlap at the destination alone would miss it.
        lands("fast fall", 64.0, DECK, body(0, 62.7, 0), DECK, -1.3, 63.101);
        lands("stationary contact", 63.101, DECK, body(0, 63.101, 0), DECK, -0.0784, 63.101);
        AABB raised = DECK.move(0.02, 0.012, 0.008);
        lands("deck rises into feet", 63.101, DECK, body(0, 63.101, 0), raised, -0.0784, 63.113);
        lands("deck moves down", 63.101, DECK, body(0, 63.03, 0), DECK.move(0, -0.012, 0), -0.0784, 63.089);
        misses("jump detaches", 63.101, DECK, body(0, 63.11, 0), DECK, 0.42);
        misses("walk off edge", 63.101, DECK, body(1.81, 63.0, 0), DECK, -0.0784);
        misses("swim under deck", 62.7, DECK, body(0, 62.8, 0), DECK, 0.0);
        misses("fall above deck", 65.0, DECK, body(0, 64.0, 0), DECK, -1.0);
        misses("raft removed from underneath", 63.101, DECK, body(0, 63.0, 0), DECK.move(4, 0, 0), -0.0784);
        if (!RaftContact.isSupported(body(1.6, 63.101, 0), DECK)) {
            throw new AssertionError("A foot partially over the edge must still be supported");
        }
        followsForOneMinute();
        Vec3 drift = new Vec3(0.02, 0.0, 0.008);
        AABB moved = DECK.move(drift);
        assertVector("jumping receives no carry", Vec3.ZERO,
                RaftContact.carryDisplacement(body(0, 63.101, 0), moved, drift, 0.42));
        assertVector("swimmer receives no carry", Vec3.ZERO,
                RaftContact.carryDisplacement(body(0, 62.7, 0), moved, drift, 0.0));
        assertVector("player outside deck receives no carry", Vec3.ZERO,
                RaftContact.carryDisplacement(body(2, 63.101, 0), moved, drift, 0.0));
        assertVector("network teleport must not drag player", Vec3.ZERO,
                RaftContact.carryDisplacement(body(0, 63.101, 0), DECK.move(10, 0, 0), new Vec3(10, 0, 0), 0.0));
        assertVector("reverse current follows actual movement", drift.scale(-1),
                RaftContact.carryDisplacement(body(0, 63.101, 0), DECK.move(drift.scale(-1)), drift.scale(-1), 0.0));
        System.out.println("16 raft contact/carry regression checks passed.");
        RaftLayoutTest.run();
        RaftStructureTest.run();
        RaftMovementSyncTest.run();
        RaftTransformTest.run();
    }

    private static void followsForOneMinute() {
        AABB deck = DECK;
        Vec3 player = new Vec3(0, 63.101, 0);
        double lastWave = 0;
        for (int tick = 1; tick <= 1200; tick++) {
            double wave = Math.sin(tick * 0.075) * 0.012;
            Vec3 delta = new Vec3(0.02, wave - lastWave, 0.008);
            deck = deck.move(delta);
            player = player.add(RaftContact.carryDisplacement(body(player.x, player.y, player.z), deck, delta, 0.0));
            lastWave = wave;
        }
        assertVector("one minute of drift and bobbing preserves relative position",
                new Vec3(24.0, 63.101 + lastWave, 9.6), player);
    }

    private static void assertVector(String name, Vec3 expected, Vec3 actual) {
        if (expected.distanceToSqr(actual) > 1.0E-12) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
    }

    private static AABB body(double x, double feet, double z) {
        return new AABB(x - 0.3, feet, z - 0.3, x + 0.3, feet + 1.8, z + 0.3);
    }

    private static void lands(String name, double start, AABB startDeck, AABB end, AABB endDeck, double velocity, double expected) {
        double actual = RaftContact.landingHeight(start, startDeck, end, endDeck, velocity).orElseThrow(
                () -> new AssertionError(name + " did not land"));
        if (Math.abs(actual - expected) > 1.0E-7) {
            throw new AssertionError(name + ": expected " + expected + ", got " + actual);
        }
    }

    private static void misses(String name, double start, AABB startDeck, AABB end, AABB endDeck, double velocity) {
        if (RaftContact.landingHeight(start, startDeck, end, endDeck, velocity).isPresent()) {
            throw new AssertionError(name + " must not attach to the deck");
        }
    }
}
