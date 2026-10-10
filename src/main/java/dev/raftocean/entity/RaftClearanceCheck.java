package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.registry.ModContent;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native player-sized collisions for embedded stations, rotated carry, bounded escape and blocked rooms. */
public final class RaftClearanceCheck {
    private RaftClearanceCheck() { }
    private static void check(boolean value,String message){if(!value){throw new IllegalStateException(message);}}
    private static RaftEntity raft(ServerLevel level,String layout,String pieces){
        var entity=new RaftEntity(ModContent.RAFT.get(),level);
        var data=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        data.putString("Layout",layout);data.putString("Structure",pieces);
        entity.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),data.buildResult()));
        entity.setPos(8.5,level.getMaxY()+16,8.5);return entity;
    }
    public static int run(ServerLevel level){
        int checks=0;
        boolean forced=level.setChunkForced(0,0,true);
        level.getChunkSource().tick(()->true,false);level.waitForEntities(new net.minecraft.world.level.ChunkPos(0,0),0);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"RaftClearanceCheck"));
        try{
            // A low addon surface and the foundation both lie within contact tolerance.
            // Carry must choose the actual upper surface rather than the first collider.
            var lowId=net.minecraft.resources.Identifier.fromNamespaceAndPath("raft_test","low_carry_surface");
            var lowBox=new net.minecraft.world.phys.AABB(-.5,0,-.5,.5,.05,.5);
            dev.raftocean.api.RaftBuildables.register(new dev.raftocean.api.RaftBuildable(lowId,
                    ()->net.minecraft.world.item.Items.AMETHYST_SHARD,()->net.minecraft.world.level.block.Blocks.OAK_SLAB,
                    1,1,1,1,true,java.util.List.of(lowBox),java.util.List.of(lowBox),
                    (owner,anchor,user,hand)->net.minecraft.world.InteractionResult.PASS));
            var lowCarry=raft(level,RaftLayout.starter().encode(),"0,1,0,ADDON,0,0,"+lowId);
            try {
                check(level.addFreshEntity(lowCarry),"Low addon carry fixture is registered");checks++;
                player.setPos(lowCarry.toWorld(new Vec3(0,.30+RaftContact.CLEARANCE,0)));
                player.setDeltaMovement(Vec3.ZERO);
                check(lowCarry.getCollisionBoxes().stream().filter(box->RaftContact.isSupported(player.getBoundingBox(),box)).count()>=2,
                        "Low surface fixture offers both lower foundation and upper addon contact");checks++;
                for(int step=0;step<40;step++) {
                    Vec3 drift=new Vec3(.02,.002,.007);float yaw=lowCarry.getYRot();
                    lowCarry.setPos(lowCarry.position().add(drift));lowCarry.setYRot(yaw+.75F);lowCarry.refreshBounds();
                    player.setOnGround(false);
                    RaftPlayerSupport.carryPlayer(lowCarry,player,drift,yaw);
                    check(Math.abs(player.getY()-lowCarry.getY()-.30-RaftContact.CLEARANCE)<1e-6
                            && player.onGround() && lowCarry.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox())),
                            "Rotating carry keeps low addon contact grounded without dragging feet into the foundation");checks++;
                }
            } finally {lowCarry.discard();}
            for(var kind:new RaftPiece.Kind[]{RaftPiece.Kind.CHEST,RaftPiece.Kind.CRAFTING_TABLE,RaftPiece.Kind.SMELTER,
                    RaftPiece.Kind.SHORT_PILLAR,RaftPiece.Kind.WALL,RaftPiece.Kind.DOOR,RaftPiece.Kind.STAIRS}){
                var raft=raft(level,RaftLayout.starter().encode(),"0,1,0,"+kind.name()+",0,0");
                var piece=raft.pieces().entrySet().iterator().next();
                Vec3 center=raft.pieceBoxes(piece.getKey(),piece.getValue()).getFirst().getCenter();
                player.setPos(raft.position().add(center.x,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,center.z));
                player.setDeltaMovement(Vec3.ZERO);
                check(raft.getCollisionBoxes().stream().anyMatch(box->box.intersects(player.getBoundingBox())),"Embedded fixture must intersect "+kind);checks++;
                Vec3 before=player.position();
                check(RaftPlayerSupport.recoverOverlap(raft,player),"Recover embedded player from "+kind);checks++;
                check(raft.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox()))
                        && player.position().distanceToSqr(before)<=2.25 && player.getY()>=before.y && player.getY()-before.y<=.6,
                        "Recovery stays on deck and clears "+kind);checks++;
                check(!RaftPlayerSupport.recoverOverlap(raft,player),"Clear player must not be corrected again: "+kind);checks++;
                raft.discard();
            }
            // A rotated physical object is represented by many adjoining strips.
            // Recovery must be able to leave that object without treating the next
            // strip as a separate wall, and must stay stable once clear.
            for(var kind:new RaftPiece.Kind[]{RaftPiece.Kind.CHEST,RaftPiece.Kind.CRAFTING_TABLE,RaftPiece.Kind.SMELTER,RaftPiece.Kind.WALL}) {
                for(float yaw:new float[]{7,23,37,53,71,127,217,323}) {
                    var rotated=raft(level,RaftLayout.starter().encode(),"0,1,0,"+kind.name()+",0,0");
                    rotated.setYRot(yaw);
                    var entry=rotated.pieces().entrySet().iterator().next();
                    Vec3 center=rotated.pieceBoxes(entry.getKey(),entry.getValue()).getFirst().getCenter();
                    player.setPos(rotated.toWorld(new Vec3(center.x,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,center.z)));
                    player.setDeltaMovement(Vec3.ZERO);
                    check(RaftPlayerSupport.recoverOverlap(rotated,player),"Rotated embedded recovery: "+kind+" at "+yaw);checks++;
                    check(rotated.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox())),"Rotated recovery clears all strips");checks++;
                    check(!RaftPlayerSupport.recoverOverlap(rotated,player),"Rotated recovery must settle without repeat pushes");checks++;
                    rotated.discard();
                }
            }
            var low=raft(level,RaftLayout.starter().encode(),"0,1,0,FLOOR,0,0");
            player.setPos(low.position().add(0,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,0));
            double feet=player.getY();
            check(RaftPlayerSupport.recoverOverlap(low,player) && player.getY()>feet && player.getY()-feet<=.6,
                    "Low ledge recovery steps upward rather than pushing the player sideways");checks++;
            check(low.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox())),"Stepped player has full clearance");checks++;
            low.discard();
            var body=new net.minecraft.world.phys.AABB(0,0,0,.6,1.8,.6);
            check(RaftPlayerClearance.sweeps(body,new net.minecraft.world.phys.AABB(.71,0,0,.72,2,.6),new Vec3(1,0,0)),"Continuous escape detects thin wall between sampled positions");checks++;
            check(!RaftPlayerClearance.sweeps(body,new net.minecraft.world.phys.AABB(.71,0,2,.72,2,3),new Vec3(1,0,0)),"Unrelated wall does not block escape");checks++;
            check(!RaftPlayerClearance.sweeps(body,new net.minecraft.world.phys.AABB(.6,0,0,.7,2,.6),new Vec3(-1,0,0)),"Moving away from a touching face stays free");checks++;
            var turning=raft(level,RaftLayout.starter().encode(),"0,1,0,CRAFTING_TABLE,0,0");
            player.setPos(turning.position().add(.81,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,0));
            check(turning.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox())),"Rotating-corner fixture starts clear");checks++;
            Vec3 delta=new Vec3(.02,0,.01);turning.setYRot(45);turning.setPos(turning.position().add(delta));
            RaftPlayerSupport.carryPlayer(turning,player,delta,0);
            check(turning.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox()))
                    && turning.getCollisionBoxes().stream().anyMatch(box->RaftContact.isSupported(player.getBoundingBox(),box)),
                    "Rotation carry clears player corners against the station while preserving support");checks++;
            for(int tick=0;tick<30;tick++){
                float old=turning.getYRot();turning.setYRot(old+.75F);turning.setPos(turning.position().add(delta));
                RaftPlayerSupport.carryPlayer(turning,player,delta,old);
                check(turning.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox())),"Repeated station-adjacent carry remains clear");checks++;
            }
            turning.discard();
            var upper=raft(level,RaftLayout.starter().encode(),"0,1,0,PILLAR,0,0;0,4,0,FLOOR,0,0;-1,4,0,FLOOR,0,0;1,4,0,FLOOR,0,0;0,5,0,CHEST,0,0");
            double upperY=upper.getY()+3.375+RaftContact.CLEARANCE;
            player.setPos(upper.getX(),upperY,upper.getZ());
            check(RaftPlayerSupport.recoverOverlap(upper,player) && player.getY()==upperY
                    && upper.getCollisionBoxes().stream().noneMatch(box->box.intersects(player.getBoundingBox())),
                    "Upper-floor chest recovery uses actual flush-mounted geometry without dropping floors");checks++;
            upper.discard();
            var sealed=raft(level,RaftLayout.starter().encode(),"0,1,0,CHEST,0,0;1,1,0,WALL,3,0;-1,1,0,WALL,1,0;0,1,1,WALL,0,0;0,1,-1,WALL,2,0");
            player.setPos(sealed.position().add(0,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,0));
            check(RaftPlayerClearance.position(sealed,player,player.position()).isEmpty(),"Recovery must not pass through enclosing walls or invent deck space");checks++;
            sealed.discard();
            var narrow=raft(level,"0,0;1,0;2,0","1,1,0,CHEST,0,0");
            player.setPos(narrow.position().add(1,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,0));
            check(RaftPlayerSupport.recoverOverlap(narrow,player) && Math.abs(player.getZ()-narrow.getZ())<.01,
                    "A narrow raft recovers along the supported deck instead of pushing the center into water");checks++;
            narrow.discard();
        }finally{player.discard();if(forced){level.setChunkForced(0,0,false);}}
        return checks;
    }
}
