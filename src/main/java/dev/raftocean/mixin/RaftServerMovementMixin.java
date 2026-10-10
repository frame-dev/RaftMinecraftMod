package dev.raftocean.mixin;

import dev.raftocean.network.RaftMovementSync;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class RaftServerMovementMixin {
    @Shadow public ServerPlayer player;

    @ModifyVariable(method = "handleMovePlayer", argsOnly = true, ordinal = 0,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"))
    private ServerboundMovePlayerPacket raftOcean$relativePosition(ServerboundMovePlayerPacket packet) {
        return RaftMovementSync.rebase(player, packet);
    }
}
