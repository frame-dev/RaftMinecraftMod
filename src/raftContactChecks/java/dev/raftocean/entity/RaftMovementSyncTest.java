package dev.raftocean.entity;

import dev.raftocean.network.RaftMovementFrame;
import dev.raftocean.network.RaftMovementSync;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public final class RaftMovementSyncTest {
    public static void run() {
        Vec3 clientOrigin = new Vec3(100, 63, -80);
        Vec3 relative = new Vec3(.4, 2.251, -.3);
        Vec3 raw = clientOrigin.add(relative);
        RaftMovementFrame frame = new RaftMovementFrame(42, clientOrigin, raw);
        for (Vec3 delay : new Vec3[] { Vec3.ZERO, new Vec3(.06, .006, .024), new Vec3(-.15, -.003, -.06) }) {
            Vec3 serverOrigin = clientOrigin.add(delay);
            Vec3 target = RaftMovementSync.rebasePosition(raw, frame, serverOrigin).orElseThrow();
            require(target.subtract(serverOrigin).distanceToSqr(relative) < 1.0E-12, "Interpolation delay must preserve local coordinates");
        }
        require(RaftMovementSync.rebasePosition(raw.add(.01, 0, 0), frame, clientOrigin).isEmpty(), "Mismatched position packet rejected");
        require(RaftMovementSync.rebasePosition(raw, frame, clientOrigin.add(3, 0, 0)).isEmpty(), "Large origin correction rejected");
        require(RaftMovementSync.rebasePosition(new Vec3(Double.NaN, 0, 0), frame, clientOrigin).isEmpty(), "Non-finite position rejected");
        require(RaftMovementSync.rebasePosition(raw, new RaftMovementFrame(42, new Vec3(0, Double.POSITIVE_INFINITY, 0), raw), clientOrigin).isEmpty(), "Non-finite origin rejected");
        for (float yaw : new float[]{0, 45, 179, 359}) {
            Vec3 rotatedRaw = RaftTransform.world(relative, clientOrigin, yaw);
            var rotatedFrame = new RaftMovementFrame(42, clientOrigin, rotatedRaw, yaw);
            Vec3 serverOrigin = clientOrigin.add(.12,.006,-.03);
            Vec3 result = RaftMovementSync.rebasePosition(rotatedRaw, rotatedFrame, serverOrigin, yaw+3).orElseThrow();
            require(RaftTransform.local(result, serverOrigin, yaw+3).distanceToSqr(relative)<1E-12,
                    "Rotating movement frame preserves local feet across yaw wrap");
        }
        require(RaftMovementSync.rebasePosition(raw,frame,clientOrigin,16).isEmpty(),"Excessive angle correction rejected");
        require(RaftMovementSync.rebasePosition(raw,new RaftMovementFrame(42,clientOrigin,raw,Float.NaN),clientOrigin,0).isEmpty(),"Non-finite yaw rejected");
        frame = new RaftMovementFrame(42, clientOrigin, raw, 359);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            RaftMovementFrame.STREAM_CODEC.encode(buffer, frame);
            require(RaftMovementFrame.STREAM_CODEC.decode(buffer).equals(frame), "Movement frame wire format round trip");
            require(buffer.readableBytes() == 0, "Codec consumes the whole frame");
            RaftMovementFrame detached = new RaftMovementFrame(-1, Vec3.ZERO, raw);
            buffer.clear();
            RaftMovementFrame.STREAM_CODEC.encode(buffer, detached);
            require(RaftMovementFrame.STREAM_CODEC.decode(buffer).equals(detached), "Detachment marker round trip");
        } finally { buffer.release(); }
        System.out.println("16 raft movement-frame/codec regression checks passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
