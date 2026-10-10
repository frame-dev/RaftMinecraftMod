package dev.raftocean.mixin;

import dev.raftocean.network.RaftMovementSync;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/** Client-only: enqueue the raft frame before the matching vanilla position packet. */
@Mixin(LocalPlayer.class)
public abstract class RaftLocalPlayerMixin {
    @ModifyArg(method = "sendPosition", at = @At(value="INVOKE",
            target="Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"),index=0)
    private Packet<?> raftOcean$sendMovementFrame(Packet<?> packet) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if(packet instanceof ServerboundMovePlayerPacket move && move.hasPosition()){
            player.connection.send(RaftMovementSync.clientFrame(player).toVanillaServerbound());
        }
        return packet;
    }
}
