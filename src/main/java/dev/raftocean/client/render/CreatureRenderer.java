package dev.raftocean.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.raftocean.creature.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import java.util.*;

/** Original geometric creature meshes: swimming tail, flapping wings and walking legs. */
public final class CreatureRenderer<T extends Entity> extends EntityRenderer<T,CreatureRenderer.State> {
    private final Map<T,VisualMotion> motions = new WeakHashMap<>();
    public static final class State extends EntityRenderState {
        float yaw,scale=1,pitch,bank,bob,thrust;final List<Part> parts=new ArrayList<>();
    }
    private record Part(AABB box,String mesh,float pivotX,float pivotY,float pivotZ,float angleX,float angleY,float angleZ){ }
    public CreatureRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=.3F;}
    @Override public State createRenderState(){return new State();}
    @Override public void extractRenderState(T entity,State state,float partial){
        super.extractRenderState(entity,state,partial);state.parts.clear();state.yaw=Mth.rotLerp(partial,entity.yRotO,entity.getYRot());state.scale=1;
        double time=entity.tickCount+partial;
        var motion=motions.computeIfAbsent(entity,key->new VisualMotion());
        var velocity=entity.getDeltaMovement();
        double measured=Math.hypot(entity.getX()-entity.xo,entity.getZ()-entity.zo);
        motion.update(time,Math.max(velocity.horizontalDistance(),measured),velocity.y,
                Mth.wrapDegrees(entity.getYRot()-entity.yRotO),entity instanceof OceanCreature creature && creature.phase()==2,
                entity instanceof OceanCreature creature && !creature.shark() && (creature.phase()==1 || creature.phase()==2));
        state.pitch=motion.pitch;state.bank=motion.bank;state.bob=0;state.thrust=0;
        if(entity instanceof OceanCreature mob && mob.shark()) {
            Block skin=CreatureContent.SHARK_SKIN.get();
            double impactAge=mob.biteImpact()<0?-1:Math.max(0,entity.level().getGameTime()+partial-mob.biteImpact());
            var bite=BitePose.sample(mob.interpolatedBiteWindup(partial),impactAge);
            state.thrust=bite.thrust();state.pitch+=bite.pitch();
            boolean foundation=mob.foundationBite();
            if(foundation){
                float reach=mob.biteWindup()>0 || impactAge<0?1:(float)(1-Math.clamp(impactAge/BitePose.RECOVERY_TICKS,0,1));
                state.thrust=(float)FoundationBite.MODEL_REACH*reach;
                state.pitch=0;state.bank=0;
            }
            double swim=motion.cycle+entity.getId()*.73;
            float tail=(float)Math.sin(swim)* (12+motion.speed*55)*bite.tailBoost();
            add(entity,state,skin,new AABB(-.33,.16,-.8,.33,.61,1));
            float jaw=bite.jaw();
            part(entity,state,CreatureContent.SHARK_BELLY.get(),new AABB(-.24,.21,.5,.24,.34,1),0,.34F,.5F,jaw,0,0);
            add(entity,state,CreatureContent.SHARK_FIN.get(),new AABB(-.05,.5,-.18,.05,1,.28));
            for(int side:new int[]{-1,1}){
                double x=side*.285;
                add(entity,state,CreatureContent.CREATURE_EYE.get(),new AABB(x-.025,.34,.72,x+.025,.405,.79));
            }
            for(int tooth=0;tooth<5;tooth++){
                double x=-.15+tooth*.075;
                part(entity,state,CreatureContent.CREATURE_TOOTH.get(),new AABB(x-.021,.31,.91,x+.021,.37,.96),
                        (float)x,.34F,.935F,180,0,0);
                part(entity,state,CreatureContent.CREATURE_TOOTH.get(),new AABB(x-.02,.285,.91,x+.02,.345,.96),0,.34F,.5F,jaw,0,0);
            }
            float fin= (float)Math.sin(swim*.6)*4;
            part(entity,state,CreatureContent.SHARK_FIN.get(),new AABB(-.9,.13,-.3,-.28,.22,.35),-.28F,.18F,0,0,0,fin-8);
            part(entity,state,CreatureContent.SHARK_FIN.get(),new AABB(.28,.13,-.3,.9,.22,.35),.28F,.18F,0,0,0,8-fin);
            part(entity,state,CreatureContent.SHARK_FIN.get(),new AABB(-.14,.18,-1.15,.14,.5,-.6),0,.3F,-.6F,0,tail,0);
            // The tail tip follows the same joint, with delayed flex along the spine.
            float tip=tail+(float)Math.sin(swim-.65)*12;
            double tailRadians=Math.toRadians(tail);
            float jointX=(float)(-.48*Math.sin(tailRadians)),jointZ=(float)(-.6-.48*Math.cos(tailRadians));
            part(entity,state,CreatureContent.SHARK_FIN.get(),new AABB(-.08,-.02,-1.4,.08,.75,-1.08).move(jointX,0,jointZ+1.08),jointX,.3F,jointZ,0,tip,0);
            state.bob=foundation?0:(float)Math.sin(swim*.5)*.018F;
        } else if(entity instanceof OceanCreature) {
            Block skin=CreatureContent.BIRD_SKIN.get();
            add(entity,state,skin,new AABB(-.2,.1,-.3,.2,.4,.3));
            float peck=(float)Math.sin(time*1.2)*18*motion.action;
            part(entity,state,skin,new AABB(-.15,.3,.18,.15,.58,.46),0,.32F,.18F,peck,0,0);
            part(entity,state,CreatureContent.BIRD_BEAK.get(),new AABB(-.07,.35,.4,.07,.45,.65),0,.32F,.18F,peck,0,0);
            for(int side:new int[]{-1,1}){
                double x=side*.15;
                part(entity,state,CreatureContent.CREATURE_EYE.get(),new AABB(x-.012,.43,.33,x+.012,.475,.375),0,.32F,.18F,peck,0,0);
            }
            float amplitude=32-motion.glide*23;
            float flap=(float)Math.sin(time*.6+entity.getId())*amplitude;
            for(int side:new int[]{-1,1}){
                float root=side*.15F;
                AABB inner=side<0?new AABB(-.55,.2,-.18,-.15,.28,.3):new AABB(.15,.2,-.18,.55,.28,.3);
                AABB outer=side<0?new AABB(-.95,.21,-.12,-.53,.26,.23):new AABB(.53,.21,-.12,.95,.26,.23);
                float wing=-side*flap;
                part(entity,state,CreatureContent.BIRD_WING.get(),inner,root,.25F,0,0,0,wing);
                double wingRadians=Math.toRadians(wing);
                float elbowX=root+(float)(side*.38*Math.cos(wingRadians));
                float elbowY=.25F+(float)(side*.38*Math.sin(wingRadians));
                part(entity,state,CreatureContent.BIRD_WING.get(),outer.move(elbowX-side*.53,elbowY-.25,0),elbowX,elbowY,0,0,0,
                        wing-side*(float)Math.sin(time*.6+entity.getId()-.45)*10);
            }
            part(entity,state,skin,new AABB(-.12,.2,-.55,.12,.29,-.25),0,.24F,-.25F,8+(float)Math.sin(time*.18)*5,0,0);
            state.bob=(float)Math.sin(time*.6+entity.getId())*(.035F-motion.glide*.03F);
        } else if(entity instanceof IslandGrazer animal) {
            state.scale=animal.isBaby()?.5F:1;
            Block skin=CreatureContent.GRAZER_SKIN.get();
            add(entity,state,skin,new AABB(-.35,.35,-.4,.35,.9,.4));
            state.bank=0;state.pitch=0;
            float idle=1-(float)Math.clamp(motion.speed*15,0,1);
            float graze=(float)Math.max(0,Math.sin(time*.045+entity.getId()))*35*idle;
            float glance=Mth.wrapDegrees(Mth.rotLerp(partial,animal.yHeadRotO,animal.yHeadRot)-state.yaw);
            part(entity,state,skin,new AABB(-.25,.7,.32,.25,1.15,.7),0,.75F,.32F,graze,-glance*.6F,0);
            for(int side:new int[]{-1,1}){
                double x=side*.251;
                part(entity,state,CreatureContent.CREATURE_EYE.get(),new AABB(x-.012,.99,.54,x+.012,1.04,.59),0,.75F,.32F,graze,-glance*.6F,0);
            }
            for(int side:new int[]{-1,1}){
                AABB ear=side<0?new AABB(-.32,1,.4,-.2,1.3,.55):new AABB(.2,1,.4,.32,1.3,.55);
                part(entity,state,skin,ear,0,.75F,.32F,graze,-glance*.6F,(float)Math.pow(Math.max(0,Math.sin(time*.19+side*2)),12)*side*12);
            }
            float stride=(float)(Math.sin(motion.cycle*2)*Math.min(1,motion.speed*15)*28);
            state.bob=(float)Math.abs(Math.sin(motion.cycle*2))*Math.min(.035F,motion.speed*.3F);
            part(entity,state,skin,new AABB(-.06,.55,-.64,.06,.65,-.36),0,.6F,-.36F,0,(float)Math.sin(time*.12)*18,0);
            for(int side:new int[]{-1,1}){for(int end:new int[]{-1,1}){
                float x=side*.24F,z=end*.26F;
                part(entity,state,skin,new AABB(x-.08,0,z-.08,x+.08,.45,z+.08),x,.4F,z,stride*side*end,0,0);
                part(entity,state,CreatureContent.GRAZER_HOOF.get(),new AABB(x-.085,0,z-.085,x+.085,.12,z+.085),x,.4F,z,stride*side*end,0,0);
            }}
        } else {
            state.pitch=(float)Math.sin(time*.11)*4;state.bank=(float)Math.sin(time*.09)*5;
            state.bob=(float)Math.sin(time*.12)*.035F;
            add(entity,state,CreatureContent.DAMAGED_DECK.get(),new AABB(-.24,0,-.24,.24,.15,.24));
            add(entity,state,CreatureContent.BIRD_SKIN.get(),new AABB(-.13,.1,-.13,.13,.3,.13));
        }
        if(entity instanceof LivingEntity living){
            state.bank+=(float)Math.sin((living.hurtTime-partial)*1.2)*living.hurtTime*.7F;
            if(living.deathTime>0){state.bank=90*Math.min(1,(living.deathTime+partial)/18);}
        }
    }
    private void add(T entity,State state,Block skin,AABB box){part(entity,state,skin,box,0,0,0,0,0,0);}
    private void part(T entity,State state,Block skin,AABB box,float px,float py,float pz,float ax,float ay,float az) {
        String mesh=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(skin).getPath();
        if(mesh.equals("shark_skin")){mesh="shark_body";}
        if(mesh.equals("shark_fin")){
            mesh=box.getXsize()>.5?"shark_pectoral_"+(box.maxX<0?"left":"right")
                    :box.minZ<-.6?"shark_tail":"shark_dorsal";
        }
        if(mesh.equals("bird_wing")){mesh="bird_wing_"+(box.maxX<0?"left":"right");}
        state.parts.add(new Part(box,mesh,px,py,pz,ax,ay,az));
    }
    @Override public void submit(State state,PoseStack pose,SubmitNodeCollector collector,CameraRenderState camera) {
        pose.pushPose();pose.translate(0,state.bob,0);pose.rotateDegrees(Axis.YP,-state.yaw);
        pose.translate(0,0,state.thrust);
        pose.rotateDegrees(Axis.ZP,state.bank);pose.rotateDegrees(Axis.XP,state.pitch);pose.scale(state.scale,state.scale,state.scale);
        for(var part:state.parts){pose.pushPose();pose.translate(part.pivotX,part.pivotY,part.pivotZ);
            pose.rotateDegrees(Axis.XP,part.angleX);pose.rotateDegrees(Axis.YP,part.angleY);pose.rotateDegrees(Axis.ZP,part.angleZ);
            pose.translate(-part.pivotX,-part.pivotY,-part.pivotZ);var b=part.box;
            pose.translate(b.minX,b.minY,b.minZ);pose.scale((float)b.getXsize(),(float)b.getYsize(),(float)b.getZsize());
            EntityMeshes.submit(part.mesh,pose,collector,state.lightCoords,state.outlineColor);pose.popPose();}
        pose.popPose();super.submit(state,pose,collector,camera);
    }
}
