package dev.raftocean.network;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class RaftBuildRotationSync {
    // Respawn replaces the player entity but keeps the play connection.
    private static final Map<ServerGamePacketListenerImpl, Integer> ROTATIONS =
            Collections.synchronizedMap(new WeakHashMap<>());

    private RaftBuildRotationSync() { }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("raft-build-rotation-11.41")
                .playToServer(RaftBuildRotationPayload.TYPE, RaftBuildRotationPayload.STREAM_CODEC,
                        (payload, context) -> {
                            if (context.player() instanceof ServerPlayer player) {
                                context.enqueueWork(() -> acceptRotation(player, payload.rotation()));
                            }
                        });
    }

    static void acceptRotation(ServerPlayer player, int rotation) {
        if (rotation >= 0 && rotation <= 3 && player.connection != null) {
            ROTATIONS.put(player.connection, rotation);
        }
    }

    public static int rotation(ServerPlayer player) {
        return ROTATIONS.getOrDefault(player.connection, 0);
    }
}
