package dev.raftocean.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.raftocean.entity.RaftEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

/** A client-only ghost uses the authoritative placement planner and owns no collision or inventory. */
public final class RaftPlacementGhost {
    private RaftPlacementGhost() { }
    public static RaftEntity.Placement last;
    public static long tick=-1;
    public static RaftEntity.Placement extract(RaftEntity raft){
        var game=Minecraft.getInstance();var player=game.player;
        if(player==null || game.gui.screen()!=null || !(game.hitResult instanceof EntityHitResult hit) || hit.getEntity()!=raft){return null;}
        var pick=raft.pick(player);
        var plan=raft.placement(player,player.getMainHandItem(),pick);
        // Vanilla tries the main hand first. A station/door consumes that interaction before the offhand.
        if(plan==null && player.getMainHandItem().isEmpty() && pick!=null && (pick.piece()==null
                || !pick.piece().kind().station() && pick.piece().kind()!=dev.raftocean.entity.RaftPiece.Kind.DOOR)){
            plan=raft.placement(player,player.getOffhandItem(),pick);
        }
        last=plan;tick=game.level.getGameTime();return plan;
    }
    public static void submit(RaftEntity.Placement plan,PoseStack pose,SubmitNodeCollector collector){
        if(plan==null){return;}
        int color=plan.valid()?0x6639E6AA:0x88F16B67;
        var boxes=plan.boxes().stream().map(box->box.inflate(.003)).toList();
        collector.submitCustomGeometry(pose,RenderTypes.debugQuads(),(frame,buffer)->{
            for(AABB box:boxes){box(frame,buffer,box,color);}
        });
    }
    private static void box(PoseStack.Pose pose,VertexConsumer buffer,AABB b,int color){
        float x0=(float)b.minX,x1=(float)b.maxX,y0=(float)b.minY,y1=(float)b.maxY,z0=(float)b.minZ,z1=(float)b.maxZ;
        float[][] corners={{x0,y0,z0},{x1,y0,z0},{x1,y1,z0},{x0,y1,z0},
                {x0,y0,z1},{x1,y0,z1},{x1,y1,z1},{x0,y1,z1}};
        int[][] faces={{0,1,2,3},{5,4,7,6},{4,0,3,7},{1,5,6,2},{3,2,6,7},{4,5,1,0}};
        for(var face:faces){for(int index:face){var p=corners[index];buffer.addVertex(pose,p[0],p[1],p[2]).setColor(color);}}
    }
}
