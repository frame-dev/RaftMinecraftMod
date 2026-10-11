package dev.raftocean.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RaftBuildRotationPayload(int rotation) implements CustomPacketPayload {
    public static final Type<RaftBuildRotationPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("raft_ocean", "build_rotation"));
    public static final StreamCodec<FriendlyByteBuf, RaftBuildRotationPayload> STREAM_CODEC =
            CustomPacketPayload.codec(RaftBuildRotationPayload::write, RaftBuildRotationPayload::read);

    private void write(FriendlyByteBuf buffer) {
        buffer.writeByte(rotation);
    }

    private static RaftBuildRotationPayload read(FriendlyByteBuf buffer) {
        return new RaftBuildRotationPayload(buffer.readUnsignedByte());
    }

    @Override
    public Type<RaftBuildRotationPayload> type() {
        return TYPE;
    }
}
