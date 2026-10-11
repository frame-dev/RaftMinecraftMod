package dev.raftocean.network;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class RaftBuildRotationSync {
    private static final Map<ServerPlayer, Integer> ROTATIONS = Collections.synchronizedMap(new WeakHashMap<>());

    private RaftBuildRotationSync() { }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("raft-build-rotation-11.41")
                .playToServer(RaftBuildRotationPayload.TYPE, RaftBuildRotationPayload.STREAM_CODEC,
                        (payload, context) -> {
                            if (payload.rotation() < 0 || payload.rotation() > 3
                                    || !(context.player() instanceof ServerPlayer player)) {
                                return;
                            }
                            context.enqueueWork(() -> ROTATIONS.put(player, payload.rotation()));
                        });
    }

    public static int rotation(ServerPlayer player) {
        return ROTATIONS.getOrDefault(player, 0);
    }
}
