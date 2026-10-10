package dev.raftocean.entity;

import java.util.OptionalDouble;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Geometry shared by raft-movement carry and post-player-movement landing checks. */
public final class RaftContact {
    public static final double CLEARANCE = 0.001;
    private static final double CONTACT_TOLERANCE = 0.10;
    private static final double CROSSING_TOLERANCE = 0.025;

    private RaftContact() {
    }

    public static boolean overlapsDeck(AABB body, AABB deck) {
        return body.maxX > deck.minX + 1.0E-5 && body.minX < deck.maxX - 1.0E-5
                && body.maxZ > deck.minZ + 1.0E-5 && body.minZ < deck.maxZ - 1.0E-5;
    }

    public static boolean isSupported(AABB body, AABB deck) {
        double gap = body.minY - deck.maxY;
        return gap >= -CONTACT_TOLERANCE && gap <= CONTACT_TOLERANCE && overlapsDeck(body, deck);
    }

    public static Vec3 carryDisplacement(AABB body, AABB currentDeck, Vec3 platformDelta, double verticalVelocity) {
        if (platformDelta.lengthSqr() >= 0.25 || verticalVelocity > 0.05) {
            return Vec3.ZERO;
        }
        AABB previousDeck = currentDeck.move(platformDelta.scale(-1.0));
        return isSupported(body, previousDeck) ? platformDelta : Vec3.ZERO;
    }

    public static OptionalDouble landingHeight(double startFeet, AABB startDeck,
            AABB endBody, AABB endDeck, double verticalVelocity) {
        if (verticalVelocity > 0.05 || !overlapsDeck(endBody, endDeck)) {
            return OptionalDouble.empty();
        }
        // Compare feet to the moving surface at both ends, rather than just testing overlap.
        // This catches a fall that traverses the entire thin collider during one tick.
        if (startFeet >= startDeck.maxY - CROSSING_TOLERANCE
                && endBody.minY <= endDeck.maxY + CROSSING_TOLERANCE) {
            return OptionalDouble.of(endDeck.maxY + CLEARANCE);
        }
        return OptionalDouble.empty();
    }
}
