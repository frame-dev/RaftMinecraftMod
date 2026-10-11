package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.registry.ModContent;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Actual player-sized support, furniture headroom and collision-cache invalidation. */
public final class RaftFurnitureCollisionCheck {
    private RaftFurnitureCollisionCheck() { }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}

    public static int run(ServerLevel level) {
        int checks=0;
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"FurnitureCollision"));
        var anchor=new BlockPos(0,1,0);
        try {
            for(var kind:new RaftPiece.Kind[]{RaftPiece.Kind.CABIN_BED,RaftPiece.Kind.DOUBLE_BED,
                    RaftPiece.Kind.WOODEN_CHAIR,RaftPiece.Kind.SOFA,RaftPiece.Kind.POTTED_PALM}) {
                var structure=new RaftStructure();
                require(structure.add(anchor,new RaftPiece(kind,0,false),RaftLayout.starter()),"Furniture collision fixture support");
                var data=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
                data.putString("Structure",structure.encode());
                var raft=new RaftEntity(ModContent.RAFT.get(),level);
                raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),data.buildResult()));
                raft.setPos(8,level.getMaxY()+28,8);
                try {
                    require(level.addFreshEntity(raft),"Register furniture collision fixture");
                    for(float yaw:new float[]{0,90,180,270}) {
                        raft.setYRot(yaw);raft.refreshBounds();
                        Vec3 local=switch(kind) {
                            case CABIN_BED,DOUBLE_BED -> new Vec3(0,.25+.535+RaftContact.CLEARANCE,.5);
                            case WOODEN_CHAIR -> new Vec3(0,.25+.55+RaftContact.CLEARANCE,-.12);
                            case SOFA -> new Vec3(.45,.25+.50+RaftContact.CLEARANCE,-.12);
                            case POTTED_PALM -> new Vec3(.35,.25+.44+RaftContact.CLEARANCE,0);
                            default -> throw new IllegalStateException("Unexpected furniture fixture");
                        };
                        player.setPos(raft.toWorld(local));player.setDeltaMovement(Vec3.ZERO);
                        require(raft.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox().deflate(1e-6))),
                                "Empty space above mattress/seat or beside palm trunk stays open: "+kind+" yaw="+yaw);checks++;
                        double feet=player.getY();
                        player.move(MoverType.SELF,new Vec3(0,-.08,0));
                        require(Math.abs(player.getY()-feet+RaftContact.CLEARANCE)<1e-6,
                                "Actual physical surface supports standing player: "+kind);checks++;
                        player.setPos(raft.toWorld(local));player.setDeltaMovement(Vec3.ZERO);
                        Vec3 start=player.position(),shift=new Vec3(.025,.002,-.01);
                        raft.setPos(raft.position().add(shift));
                        RaftPlayerSupport.carryPlayer(raft,player,shift,yaw);
                        require(player.position().distanceToSqr(start.add(shift))<1e-10,
                                "Furniture surface follows raft without a side shove: "+kind);checks++;
                        require(raft.getCollisionBoxes()==raft.getCollisionBoxes(),"Same pose reuses immutable collision geometry");checks++;
                        var previous=raft.getCollisionBoxes();raft.setPos(raft.position().add(.1,0,0));
                        require(raft.getCollisionBoxes()!=previous && raft.getCollisionBoxes().getFirst().minX>previous.getFirst().minX,
                                "Translated collision cache updates immediately");checks++;
                        previous=raft.getCollisionBoxes();raft.setYRot(yaw+13);
                        require(raft.getCollisionBoxes()!=previous,"Turning invalidates world collision geometry");checks++;
                    }
                    var previous=raft.getCollisionBoxes();
                    require(raft.removeStructuralPiece(anchor),"Reclaim furniture fixture");
                    require(raft.getCollisionBoxes()!=previous,"Reclaim removes cached furniture colliders");checks++;
                } finally {raft.discard();}
            }
        } finally {player.discard();}
        return checks;
    }
}
