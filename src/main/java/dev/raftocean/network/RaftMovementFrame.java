package dev.raftocean.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Client raft origin paired with the next vanilla position packet, not a teleport request. */
public record RaftMovementFrame(int raftId, Vec3 origin, Vec3 playerPosition, float yaw) implements CustomPacketPayload {
    public RaftMovementFrame(int raftId, Vec3 origin, Vec3 playerPosition) { this(raftId, origin, playerPosition, 0); }

    public static final Type<RaftMovementFrame> TYPE = new Type<>(Identifier.fromNamespaceAndPath("raft_ocean", "movement_frame"));
    public static final StreamCodec<FriendlyByteBuf, RaftMovementFrame> STREAM_CODEC =
            CustomPacketPayload.codec(RaftMovementFrame::write, RaftMovementFrame::read);

    private void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(raftId);
        writeVector(buffer, origin);
        writeVector(buffer, playerPosition);
        buffer.writeFloat(yaw);
    }

    private static RaftMovementFrame read(FriendlyByteBuf buffer) {
        return new RaftMovementFrame(buffer.readVarInt(), readVector(buffer), readVector(buffer), buffer.readFloat());
    }

    private static void writeVector(FriendlyByteBuf buffer, Vec3 vector) {
        buffer.writeDouble(vector.x);
        buffer.writeDouble(vector.y);
        buffer.writeDouble(vector.z);
    }

    private static Vec3 readVector(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    @Override
    public Type<RaftMovementFrame> type() { return TYPE; }
}
