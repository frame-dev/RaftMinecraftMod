package dev.raftocean.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** The client requests a menu for a hand, never a trusted item, recipe or placement position. */
public record HammerMenuPayload(boolean offhand) implements CustomPacketPayload {
    public static final Type<HammerMenuPayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("raft_ocean","hammer_menu"));
    public static final StreamCodec<FriendlyByteBuf,HammerMenuPayload> STREAM_CODEC=CustomPacketPayload.codec((p,b)->b.writeBoolean(p.offhand()),b->new HammerMenuPayload(b.readBoolean()));
    @Override public Type<HammerMenuPayload> type(){return TYPE;}
    public static void register(net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event){
        event.registrar("raft-hammer-13.9").playToServer(TYPE,STREAM_CODEC,(payload,ctx)->ctx.enqueueWork(()->
                dev.raftocean.building.BuildingHammer.open(ctx.player(),payload.offhand()?net.minecraft.world.InteractionHand.OFF_HAND:net.minecraft.world.InteractionHand.MAIN_HAND)));
    }
}
