package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.registry.ModContent;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Exercise the actual interaction path, including snapping and inventory transactions. */
public final class RaftBuildingUpgradeCheck {
    private RaftBuildingUpgradeCheck() { }
    public static int run(ServerLevel level) {
        var raft=new RaftEntity(ModContent.RAFT.get(),level);
        raft.setPos(.5,level.getMaxY()+16,.5);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"EdgeBuildCheck"));
        player.setPos(raft.position().add(0,.626,0));
        int checks=previews(level)+cornerJoins(level);
        try {
            require(level.addFreshEntity(raft),"Edge placement raft registered");checks++;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.COLLECTION_NET),2));
            aim(player,raft,new Vec3(1.48,.24,0));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.pieces().containsKey(new BlockPos(2,0,0)) && player.getMainHandItem().getCount()==1,
                    "Net snaps from foundation edge and consumes exactly one");checks++;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.FOUNDATION.get(),2));
            aim(player,raft,new Vec3(1.48,.24,0));
            require(!raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && player.getMainHandItem().getCount()==2,"Foundation cannot overwrite edge net");checks++;
            player.setPos(raft.toWorld(new Vec3(1.2,.626,0)));
            aim(player,raft,new Vec3(2.48,.24,0));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.getBlocks().containsKey(new BlockPos(3,0,0)) && player.getMainHandItem().getCount()==1,
                    "Foundation extends beyond a net by selecting its complete mesh surface");checks++;
            require(!raft.removeStructuralPiece(new BlockPos(2,0,0)),"Removing a required net bridge is rejected");checks++;
            player.setPos(raft.position().add(0,.626,0));
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.COLLECTION_NET)));
            player.setShiftKeyDown(true);aim(player,raft,new Vec3(1.48,.24,-1));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.pieces().containsKey(new BlockPos(2,0,-1)),"Crouching net placement still snaps flush to foundation edge");checks++;
            player.setShiftKeyDown(false);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.WALL),2));
            aim(player,raft,new Vec3(-1,.24,-1.48));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.pieces().get(new BlockPos(-1,1,-1)).rotation()==0,"Wall snaps to clicked north edge");checks++;
            aim(player,raft,new Vec3(0,.24,-1.48));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.pieces().get(new BlockPos(0,1,-1)).rotation()==0,"Neighbouring wall extends same edge without overlap");checks++;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.WALL),2));
            aim(player,raft,new Vec3(.44,1.1,-1.46));
            var continuation=raft.placement(player,player.getMainHandItem(),raft.pick(player));
            require(continuation!=null && continuation.valid() && continuation.cell().equals(new BlockPos(1,1,-1))
                    && continuation.piece().rotation()==0,"Selecting a wall face extends along the existing wall");checks++;
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.pieces().get(continuation.cell()).equals(continuation.piece()),"Wall continuation matches preview");checks++;
            Vec3 feet=new Vec3(2,RaftEntity.DECK_HEIGHT+RaftContact.CLEARANCE,0);
            player.setPos(raft.toWorld(feet));player.setDeltaMovement(Vec3.ZERO);
            Vec3 delta=new Vec3(.02,0,.008);
            raft.setPos(raft.position().add(delta));
            RaftPlayerSupport.carryPlayer(raft,player,delta);
            require(player.position().distanceToSqr(raft.toWorld(feet))<1E-12,"Walking surface of edge net carries player with hull");checks++;
            return checks;
        } finally {player.getInventory().clearContent();raft.discard();}
    }
    private static int cornerJoins(ServerLevel level){
        int checks=0;
        Vec3[] edges={new Vec3(0,.24,-.48),new Vec3(.48,.24,0),new Vec3(0,.24,.48),new Vec3(-.48,.24,0)};
        for(int edge=0;edge<4;edge++){
            for(int turn:new int[]{1,3}){
                var raft=new RaftEntity(ModContent.RAFT.get(),level);
                raft.setPos(8.5,level.getMaxY()+16,8.5);raft.setYRot(37);
                var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CornerJoinCheck"));
                player.setPos(raft.toWorld(new Vec3(0,.626,0)));
                try{
                    require(level.addFreshEntity(raft),"Corner fixture registered");
                    player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.WALL),3));
                    aim(player,raft,edges[edge]);
                    require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction(),"First corner face placed");checks++;
                    aim(player,raft,edges[(edge+turn)%4]);
                    var plan=raft.placement(player,player.getMainHandItem(),raft.pick(player));
                    require(plan!=null && plan.valid() && plan.cell().equals(new BlockPos(0,1,0))
                            && plan.piece().kind()==RaftPiece.Kind.CORNER_WALL,"Perpendicular wall preview joins occupied support cell");checks++;
                    require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                            && raft.pieces().get(plan.cell()).equals(plan.piece()) && raft.getStructuralCount()==1
                            && player.getMainHandItem().getCount()==1,"Two walls become one corner and consume exactly two walls");checks++;
                    var encoded="0,1,0,CORNER_WALL,"+plan.piece().rotation()+",0";
                    require(RaftStructure.decode(encoded,RaftLayout.starter()).pieces().get(plan.cell()).equals(plan.piece()),"Joined corner survives structural save format");checks++;
                    aim(player,raft,new Vec3(edges[edge].x*.5,.24,edges[edge].z*.5));
                    require(!raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                            && player.getMainHandItem().getCount()==1,"Duplicate corner face placement is rejected without consuming");checks++;
                }finally{player.getInventory().clearContent();raft.discard();}
            }
        }
        return checks;
    }
    private static int previews(ServerLevel level){
        int checks=0;
        for(var kind:RaftPiece.Kind.values()){
            if(kind==RaftPiece.Kind.ADDON)continue;
            var raft=new RaftEntity(ModContent.RAFT.get(),level);
            raft.setPos(8.5,level.getMaxY()+16,8.5);
            var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"GhostCheck"));
            player.setPos(raft.toWorld(new Vec3(0,.626,0)));
            try{
                require(level.addFreshEntity(raft),"Preview raft registered");
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.structuralItem(kind),2));
                aim(player,raft,new Vec3(1.48,.24,0));
                int before=raft.getStructuralCount();
                var plan=raft.placement(player,player.getMainHandItem(),raft.pick(player));
                require(plan!=null && !plan.boxes().isEmpty() && raft.getStructuralCount()==before
                        && player.getMainHandItem().getCount()==2,"Every buildable has a non-mutating ghost: "+kind);checks++;
                require(plan.valid(),"Unobstructed starter raft supports preview: "+kind+" "+plan.failure());checks++;
                boolean placed=raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction();
                require(placed==plan.valid(),"Server accepts precisely the preview result: "+kind);checks++;
                require(!placed || raft.pieces().get(plan.cell()).equals(plan.piece()),"Preview location and rotation match build: "+kind);checks++;
                require(player.getMainHandItem().getCount()==(placed?1:2),"Preview/build consume only successful placement: "+kind);checks++;
            }finally{player.getInventory().clearContent();raft.discard();}
        }
        return checks;
    }
    private static void aim(FakePlayer player,RaftEntity raft,Vec3 point) {
        Vec3 delta=raft.toWorld(point).subtract(player.getEyePosition());
        player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
        player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));
    }
    private static void require(boolean condition,String message) {
        if(!condition){throw new IllegalStateException(message);}
    }
}
