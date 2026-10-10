package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.registry.ModContent;
import dev.raftocean.network.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Runs actual native packet collision validation, which FakePlayer's normal connection skips. */
public final class RaftMovementValidationCheck {
    private static final class WalkingPlayer extends FakePlayer {
        Runnable movement;
        WalkingPlayer(ServerLevel level) { super(level,new GameProfile(UUID.randomUUID(),"NativeRaftMove")); }
        @Override public void doTick() { if(movement!=null)movement.run(); }
    }
    private static final class Handler extends ServerGamePacketListenerImpl {
        int corrections;
        Handler(ServerLevel level,ServerPlayer player) {
            super(level.getServer(),new Connection(PacketFlow.SERVERBOUND),player,CommonListenerCookie.createInitial(player.getGameProfile(),false));markClientLoaded();
        }
        @Override public void send(Packet<?> packet) { if(packet instanceof ClientboundPlayerPositionPacket)corrections++; }
    }
    public static int run(ServerLevel level) {
        boolean forced=level.setChunkForced(0,0,true);level.getChunkSource().tick(() -> true,false);level.waitForEntities(new net.minecraft.world.level.ChunkPos(0,0),0);
        var player=new WalkingPlayer(level);
        var handler=new Handler(level,player);int checks=0;
        try {
            for(var kind:List.of(RaftPiece.Kind.CRAFTING_TABLE,RaftPiece.Kind.CHEST,RaftPiece.Kind.SPRINKLER,RaftPiece.Kind.BATTERY_CHARGER,RaftPiece.Kind.STAIRS)) {
                var structure=new RaftStructure();structure.add(new BlockPos(0,1,0),new RaftPiece(kind,0,false),RaftLayout.starter());
                var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);seed.putString("Structure",structure.encode());
                var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
                try {
                    raft.setPos(8,level.getMaxY()+28,8);require(level.addFreshEntity(raft),"Register movement fixture");
                    for(double gap:new double[]{.02,-.02}) {
                    player.setPos(raft.toWorld(new Vec3(-1,RaftEntity.DECK_HEIGHT+gap,-1)));
                    player.setDeltaMovement(new Vec3(0,-.02,0));handler.resetPosition();
                    Vec3[] landed={null};
                    player.movement=() -> {
                        RaftPlayerSupport.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                        player.setPos(player.position().add(0,-.03,0));
                        RaftPlayerSupport.onPlayerTickPost(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
                        landed[0]=player.position();
                    };
                    try {
                        var tick=ServerGamePacketListenerImpl.class.getDeclaredMethod("tickPlayer");tick.setAccessible(true);tick.invoke(handler);
                    } catch(ReflectiveOperationException failure){throw new IllegalStateException("Native player tick failed",failure);}
                    player.movement=null;
                    require(player.position().distanceToSqr(landed[0])<1e-12,"Native tick restored pre-landing position: landed="+landed[0]+" actual="+player.position());checks++;
                    }
                    for(float yaw:new float[]{0,37,90,143,217,323}) {
                        raft.setYRot(yaw);raft.refreshBounds();
                        player.setPos(raft.toWorld(new Vec3(kind==RaftPiece.Kind.STAIRS?0:-1.1,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,kind==RaftPiece.Kind.STAIRS?-1.1:.95)));
                        player.setDeltaMovement(Vec3.ZERO);player.setOnGround(true);player.verticalCollisionBelow=true;
                        for(int step=0;step<20;step++) {
                            Vec3 motion=new Vec3(.04,Math.sin(step*.4)*.002,.025);
                            raft.setPos(raft.position().add(motion));raft.setYRot(yaw+.1F);raft.refreshBounds();
                            RaftPlayerSupport.carryPlayer(raft,player,motion,yaw);yaw+=.1F;
                            // Resolve client walking against its interpolated hull, then submit that packet to the real server.
                            Vec3 start=player.position(), serverOrigin=raft.position();
                            Vec3 clientOrigin=serverOrigin.subtract(.35,.006,.12);float clientYaw=yaw-1.5F;
                            Vec3 localStart=RaftTransform.local(start,serverOrigin,yaw);
                            raft.setPos(clientOrigin);raft.setYRot(clientYaw);raft.refreshBounds();
                            player.setPos(RaftTransform.world(localStart,clientOrigin,clientYaw));
                            Vec3 walk=RaftTransform.world(kind==RaftPiece.Kind.STAIRS?new Vec3(0,-.02,.09):new Vec3(.09,-.02,0),Vec3.ZERO,clientYaw);
                            player.move(MoverType.PLAYER,walk);Vec3 raw=player.position();
                            raft.setPos(serverOrigin);raft.setYRot(yaw);raft.refreshBounds();
                            player.setPos(start);player.setOnGround(true);player.verticalCollisionBelow=true;player.setDeltaMovement(Vec3.ZERO);handler.resetPosition();player.setLastSectionPos(net.minecraft.core.SectionPos.of(player));
                            Vec3 expected=RaftTransform.world(RaftTransform.local(raw,clientOrigin,clientYaw),serverOrigin,yaw);
                            var frame=new RaftMovementFrame(raft.getId(),clientOrigin,raw,clientYaw);
                            RaftMovementSync.accept(player,frame);int before=handler.corrections;
                            handler.handleMovePlayer(new ServerboundMovePlayerPacket.Pos(raw,true,false));
                            require(handler.corrections==before,"Native server teleported a legal walk near "+kind+" yaw="+yaw+" step="+step+" from="+start+" to="+expected);checks++;
                            require(player.position().distanceToSqr(start)<=2.25 && level.noCollision(player,player.getBoundingBox().deflate(1e-6)),"Native server failed to retain a legal walking position near "+kind+" yaw="+yaw+" step="+step+" expected="+expected+" got="+player.position());checks++;
                            handler.handleClientTickEnd(new ServerboundClientTickEndPacket());
                        }
                    }
                } finally {raft.discard();}
            }
            return checks;
        } finally {player.discard();if(forced)level.setChunkForced(0,0,false);}
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    private RaftMovementValidationCheck() { }
}
