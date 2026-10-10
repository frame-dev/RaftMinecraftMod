package dev.raftocean.creature;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Server-owned bounded circle/approach/bite and circle/dive/steal state machines. */
public final class OceanCreature extends PathfinderMob {
    public static final double SWIM_DEPTH=1.08;
    public static final float SHARK_TURN_DEGREES=6;
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(OceanCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BITE_WINDUP=SynchedEntityData.defineId(OceanCreature.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> BITE_IMPACT=SynchedEntityData.defineId(OceanCreature.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> FOUNDATION_BITE=SynchedEntityData.defineId(OceanCreature.class,EntityDataSerializers.BOOLEAN);
    private long windupReceivedAt;
    private UUID raftId;
    private RaftEntity cached;
    private int cooldown=200,windup,playerCooldown,fleeTicks,orphanTicks;
    private int raftAttackDelay;
    private boolean raftAttackDelayInitialized;
    private int cropTheftDelay;
    private boolean cropTheftDelayInitialized;
    private BlockPos targetCell;
    private Vec3 attackNormal,retreat;
    private boolean linedUp;
    private int retreatTicks,approachTicks,bitesInAttack,repelHits,bitePause;
    private UUID preyId;
    public OceanCreature(EntityType<? extends OceanCreature> type,Level level){super(type,level);setNoGravity(true);}
    public boolean shark(){return getType()==CreatureContent.SHARK.get();}
    public int phase(){return getEntityData().get(PHASE);}
    public int biteWindup(){return getEntityData().get(BITE_WINDUP);}
    public boolean foundationBite(){return getEntityData().get(FOUNDATION_BITE);}
    public long biteImpact(){return getEntityData().get(BITE_IMPACT);}
    public float interpolatedBiteWindup(float partial){
        int progress=biteWindup();
        return progress==0?0:Math.min(BitePose.WINDUP_TICKS-1,progress+(Math.clamp(level().getGameTime()-windupReceivedAt,0,2)+partial)*(foundationBite()?(float)BitePose.WINDUP_TICKS/RaftConfig.FOUNDATION_WINDUP.get():1));
    }
    public void bind(RaftEntity raft){raftId=raft.getUUID();cached=raft;}
    public UUID raftId(){return raftId;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);builder.define(PHASE,0);builder.define(BITE_WINDUP,0);builder.define(BITE_IMPACT,-1L);builder.define(FOUNDATION_BITE,false);
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){
        super.onSyncedDataUpdated(key);
        if(key.equals(BITE_WINDUP)){windupReceivedAt=level().getGameTime();}
    }
    @Override public boolean canBreatheUnderwater(){return shark();}
    @Override public boolean isPushedByFluid(){return false;}
    @Override public boolean causeFallDamage(double fall,float modifier,DamageSource source){return false;}
    @Override public void travel(Vec3 input){if(!level().isClientSide()){
        if(shark()){clearRaftOverlap();}
        move(MoverType.SELF,getDeltaMovement());
    }resetFallDistance();}

    /** A raft can sweep over a shark between its AI updates. Escape below the actual colliders. */
    private void clearRaftOverlap() {
        AABB body=getBoundingBox();
        double ceiling=Double.POSITIVE_INFINITY;
        boolean overlap=false;
        for(var raft:level().getEntitiesOfClass(RaftEntity.class,body.inflate(1),r->!r.isRemoved())) {
            for(AABB cell:raft.getCollisionBoxes()){
                if(cell.intersects(body.inflate(.02))){overlap=true;}
                if(cell.maxX>body.minX && cell.minX<body.maxX && cell.maxZ>body.minZ && cell.minZ<body.maxZ){
                    ceiling=Math.min(ceiling,Math.min(cell.minY,raft.getY()-SWIM_DEPTH+getBbHeight()+.12));
                }
            }
        }
        if(!overlap || !Double.isFinite(ceiling)){return;}
        double y=ceiling-getBbHeight()-.12;
        AABB safe=body.move(0,y-getY(),0);
        if(level().hasChunkAt(BlockPos.containing(getX(),y,getZ()))
                && level().getFluidState(BlockPos.containing(getX(),y+.25,getZ())).is(net.minecraft.tags.FluidTags.WATER)
                && level().getWorldBorder().isWithinBounds(safe) && level().noCollision(this,safe)) {
            setPos(getX(),y,getZ());
            setDeltaMovement(getDeltaMovement().multiply(1,0,1));
        }
    }
    @Override protected void customServerAiStep(ServerLevel level){think(level);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    private RaftEntity raft(ServerLevel level) {
        if(cached!=null && !cached.isRemoved() && cached.level()==level){return cached;}
        if(raftId!=null && level.getEntity(raftId) instanceof RaftEntity raft && !raft.isRemoved()){cached=raft;return raft;}
        if(tickCount%40==0 || raftId==null) {
            cached=level.getEntitiesOfClass(RaftEntity.class,getBoundingBox().inflate(48),r->!r.isRemoved()).stream()
                    .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if(cached!=null){raftId=cached.getUUID();}
        }
        return cached;
    }
    public void think(ServerLevel level) {
        if(!isAlive()){setDeltaMovement(Vec3.ZERO);getEntityData().set(BITE_WINDUP,0);return;}
        if(phase()!=2 && (biteImpact()<0 || level.getGameTime()-biteImpact()>=BitePose.RECOVERY_TICKS)){getEntityData().set(FOUNDATION_BITE,false);}
        if(playerCooldown>0){playerCooldown--;}
        if(fleeTicks>0){fleeTicks--;}
        if(cooldown>0){cooldown--;}
        if(shark()) {
            if(!raftAttackDelayInitialized){restartRaftAttackDelay();}
            else {raftAttackDelay=Math.min(Math.max(0,raftAttackDelay-1),RaftConfig.RAFT_ATTACK_DELAY.get()*20);}
        } else {
            if(!cropTheftDelayInitialized){restartCropTheftDelay();}
            else {cropTheftDelay=Math.min(Math.max(0,cropTheftDelay-1),RaftConfig.BIRD_THEFT_DELAY.get()*20);}
        }
        RaftEntity raft=raft(level);
        if(raft==null || raft.distanceToSqr(this)>96*96){windup=0;getEntityData().set(BITE_WINDUP,0);if(++orphanTicks>600){discard();}setDeltaMovement(getDeltaMovement().scale(.9));return;}
        orphanTicks=0;
        if(shark()){sharkThink(level,raft);}else{birdThink(level,raft);}
        int animationWindup=targetCell!=null && shark()?Math.clamp(windup*BitePose.WINDUP_TICKS/RaftConfig.FOUNDATION_WINDUP.get(),0,BitePose.WINDUP_TICKS-1):windup;
        getEntityData().set(BITE_WINDUP,shark() && phase()==2?animationWindup:0);
    }
    private boolean hostile(ServerLevel level){return RaftConfig.difficulty(level.getDifficulty())>0;}
    private void sharkThink(ServerLevel level,RaftEntity raft) {
        var bait=level.getEntitiesOfClass(ChumEntity.class,getBoundingBox().inflate(24),ChumEntity::active).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        if(bait!=null){if(targetCell!=null){restartRaftAttackDelay();}getEntityData().set(FOUNDATION_BITE,false);targetCell=null;windup=0;cooldown=Math.max(cooldown,80);getEntityData().set(PHASE,3);
            attackNormal=null;retreatTicks=0;preyId=null;
            double angle=(tickCount+getId()*17)*.035;moveToward(bait.position().add(Math.cos(angle)*1.3,-SWIM_DEPTH,Math.sin(angle)*1.3),.15);return;}
        boolean enabled=RaftConfig.SHARKS.get() && RaftConfig.AGGRESSION.get()>0 && hostile(level) && fleeTicks==0;
        if(enabled && retreatTicks>0 && retreat!=null){
            retreatTicks--;windup=0;getEntityData().set(PHASE,4);moveToward(retreat,.24);return;
        }
        if(enabled && playerCooldown==0) {
            Player prey=level.players().stream().filter(p->p.isAlive() && !p.isSpectator() && !p.getAbilities().instabuild && p.isInWater()
                    && distanceToSqr(p)<144).min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if(prey!=null){
                if(targetCell!=null){restartRaftAttackDelay();}
                getEntityData().set(FOUNDATION_BITE,false);targetCell=null;attackNormal=null;
                if(!prey.getUUID().equals(preyId)){windup=0;preyId=prey.getUUID();}
                getEntityData().set(PHASE,1);moveToward(prey.position().add(0,.2,0),windup>0?.07:.22);
                if(distanceToSqr(prey)<3 && facing(prey.position())<25 && visible(level,prey.position().add(0,.3,0))) {
                    getEntityData().set(PHASE,2);
                    if(++windup>=BitePose.WINDUP_TICKS){
                        if(prey.hurtServer(level,damageSources().mobAttack(this),(float)(3*RaftConfig.difficulty(level.getDifficulty())))){
                            getEntityData().set(BITE_IMPACT,level.getGameTime());
                        }
                        beginRetreat(prey.position());playerCooldown=80;windup=0;preyId=null;
                    }
                }else{windup=0;}return;}
        }
        preyId=null;
        if(enabled && RaftConfig.RAFT_DAMAGE.get() && cooldown==0 && raftAttackDelay==0 && targetCell==null){
            targetCell=raft.getBlocks().keySet().stream().filter(raft::attackableFoundation)
                    .min(Comparator.comparingDouble(p->raft.toWorld(new Vec3(p.getX(),0,p.getZ())).distanceToSqr(position()))).orElse(null);
            if(targetCell==null)cooldown=40;
            if(targetCell!=null){
                restartRaftAttackDelay();
                Vec3 center=raft.toWorld(new Vec3(targetCell.getX(),FoundationBite.BODY_HEIGHT,targetCell.getZ()));
                attackNormal=RaftTransform.rotate(bitePosition(raft,targetCell).subtract(center).normalize(),-raft.getYRot());
                linedUp=position().distanceToSqr(bitePosition(raft,targetCell))<4;approachTicks=0;
                bitesInAttack=0;repelHits=0;bitePause=0;getEntityData().set(BITE_IMPACT,-1L);
            }
        }
        if(!enabled || !RaftConfig.RAFT_DAMAGE.get()){
            if(targetCell!=null){restartRaftAttackDelay();}
            targetCell=null;windup=0;bitePause=0;repelHits=0;bitesInAttack=0;retreatTicks=0;
            getEntityData().set(FOUNDATION_BITE,false);getEntityData().set(BITE_IMPACT,-1L);
        }
        if(targetCell!=null && raft.attackableFoundation(targetCell)) {
            Vec3 normal=RaftTransform.rotate(attackNormal==null?new Vec3(0,0,1):attackNormal,raft.getYRot());
            Vec3 center=raft.toWorld(new Vec3(targetCell.getX(),FoundationBite.BODY_HEIGHT,targetCell.getZ()));
            Vec3 target=center.add(normal.scale(FoundationBite.BODY_DISTANCE));getEntityData().set(PHASE,1);
            if(++approachTicks>400){restartRaftAttackDelay();targetCell=null;windup=0;cooldown=80;return;}
            if(!linedUp){
                Vec3 staging=target.add(normal.scale(3.5));
                staging=new Vec3(staging.x,raft.getY()-SWIM_DEPTH,staging.z);
                if(position().distanceToSqr(staging)<1.5){linedUp=true;}
                else{moveToward(staging,.2);return;}
            }
            if(position().distanceToSqr(target)<.7 && visible(level,target.add(0,.1,0))) {
                approachTicks=0;
                setDeltaMovement(raft.getDeltaMovement().add(target.subtract(position()).scale(.18)));
                Vec3 foundation=raft.toWorld(new Vec3(targetCell.getX(),FoundationBite.BODY_HEIGHT,targetCell.getZ()));
                face(foundation);
                if(facing(foundation)>=8){windup=0;return;}
                getEntityData().set(PHASE,2);getEntityData().set(FOUNDATION_BITE,true);
                if(bitePause>0){bitePause--;windup=0;return;}
                if(++windup>=RaftConfig.FOUNDATION_WINDUP.get()) {
                    Vec3 contact=raft.toWorld(new Vec3(targetCell.getX(),RaftEntity.DECK_HEIGHT*.5,targetCell.getZ())).add(normal.scale(.5));
                    if(FoundationBite.jaw(position(),getYRot()).distanceToSqr(contact)>.15*.15){windup=RaftConfig.FOUNDATION_WINDUP.get()-1;return;}
                    if(raft.damageFoundation(targetCell,Math.max(1,(int)(RaftConfig.BITE_DAMAGE.get()*RaftConfig.difficulty(level.getDifficulty()))))){
                        getEntityData().set(BITE_IMPACT,level.getGameTime());
                    }
                    bitesInAttack++;windup=0;bitePause=RaftConfig.FOUNDATION_PAUSE.get();
                    if(bitesInAttack>=RaftConfig.BITE_BURST.get() || !raft.attackableFoundation(targetCell)){
                        restartRaftAttackDelay();
                        beginRetreat(center);targetCell=null;repelHits=0;
                        cooldown=Math.max(40,(int)(RaftConfig.BITE_INTERVAL.get()/(RaftConfig.AGGRESSION.get()*RaftConfig.attackFrequency(level.getDifficulty()))));
                    }}
            } else {windup=0;moveToward(target.add(raft.getDeltaMovement().scale(3)),.23,true);}
            return;
        }
        if(targetCell!=null){restartRaftAttackDelay();}
        targetCell=null;windup=0;getEntityData().set(PHASE,0);
        double radius=Math.max(5,Math.hypot(raft.getBoundingBox().getXsize(),raft.getBoundingBox().getZsize())*.6+2);
        double angle=(tickCount+getId()*31)*.014;
        moveToward(raft.position().add(Math.cos(angle)*radius,-SWIM_DEPTH,Math.sin(angle)*radius),fleeTicks>0?.23:.16);
        if(!isInWater() && tickCount%40==0){hurtServer(level,damageSources().dryOut(),1);}
    }
    private void beginRetreat(Vec3 bitten) {
        Vec3 away=position().subtract(bitten).multiply(1,0,1);
        if(away.lengthSqr()<.001){away=dev.raftocean.navigation.RaftNavigation.direction(getYRot()+150);}
        Vec3 tangent=new Vec3(-away.z,0,away.x).normalize().scale(2);
        retreat=position().add(away.normalize().scale(5)).add(tangent);
        retreatTicks=RaftConfig.RETREAT_DURATION.get();
    }
    private void restartRaftAttackDelay() {
        raftAttackDelay=RaftConfig.RAFT_ATTACK_DELAY.get()*20;
        raftAttackDelayInitialized=true;
    }
    public Vec3 bitePosition(RaftEntity raft,BlockPos cell) {
        // Choose an exposed cardinal edge, including concave layouts; radial offsets can aim into another cell.
        return List.of(cell.north(),cell.south(),cell.east(),cell.west()).stream()
                .filter(neighbor->!raft.getBlocks().containsKey(neighbor))
                .map(neighbor->raft.toWorld(new Vec3(cell.getX()+(neighbor.getX()-cell.getX())*FoundationBite.BODY_DISTANCE,
                        FoundationBite.BODY_HEIGHT,cell.getZ()+(neighbor.getZ()-cell.getZ())*FoundationBite.BODY_DISTANCE)))
                .min(Comparator.comparingDouble(position()::distanceToSqr))
                .orElseGet(()->raft.toWorld(new Vec3(cell.getX(),FoundationBite.BODY_HEIGHT,cell.getZ())));
    }
    private void birdThink(ServerLevel level,RaftEntity raft) {
        boolean attack=RaftConfig.BIRDS.get() && hostile(level) && fleeTicks==0 && cooldown==0 && cropTheftDelay==0;
        if(attack) {
            var crop=raft.pieces().entrySet().stream().filter(e->e.getValue().kind()==RaftPiece.Kind.PLANTER
                    && stealable(raft,e.getKey())).min(Comparator.comparingDouble(e->raft.stationPosition(e.getKey(),RaftPiece.Kind.PLANTER).distanceToSqr(position()))).orElse(null);
            if(crop!=null){targetCell=crop.getKey();Vec3 point=raft.stationPosition(targetCell,RaftPiece.Kind.PLANTER).add(0,.7,0);
                getEntityData().set(PHASE,1);moveToward(point,.17);
                if(position().distanceToSqr(point)<1 && visible(level,point)) {
                    if(stealCrop(raft,targetCell)){restartCropTheftDelay();cooldown=RaftConfig.BIRD_INTERVAL.get();targetCell=null;getEntityData().set(PHASE,2);}
                }return;}
        }
        if(targetCell!=null){restartCropTheftDelay();}
        targetCell=null;getEntityData().set(PHASE,0);
        double angle=(tickCount+getId()*41)*.021;
        moveToward(raft.position().add(Math.cos(angle)*7,6+Math.sin(angle*2),Math.sin(angle)*7),.19);
    }
    public static boolean stealable(RaftEntity raft,BlockPos pos) {
        Vec3 center=raft.stationPosition(pos,RaftPiece.Kind.PLANTER);
        if(center==null || dev.raftocean.survival.SurvivalMachines.covered(raft,center)){return false;}
        var storage=raft.storage(pos);
        return !storage.getItem(2).isEmpty() || !storage.getItem(0).isEmpty();
    }
    private void restartCropTheftDelay() {
        cropTheftDelay=RaftConfig.BIRD_THEFT_DELAY.get()*20;
        cropTheftDelayInitialized=true;
    }
    public static boolean stealCrop(RaftEntity raft,BlockPos pos) {
        if(raft.level().isClientSide() || raft.isRemoved() || !RaftConfig.BIRDS.get() || RaftConfig.difficulty(raft.level().getDifficulty())==0 || !stealable(raft,pos)){return false;}
        var storage=raft.storage(pos);int slot=storage.getItem(2).isEmpty()?0:2;
        storage.removeItem(slot,1);storage.progress=0;storage.setChanged();return true;
    }
    private boolean visible(ServerLevel level,Vec3 destination) {
        return level.clip(new ClipContext(position().add(0,.3,0),destination,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;
    }
    private void moveToward(Vec3 target,double speed) { moveToward(target,speed,false); }
    private void moveToward(Vec3 target,double speed,boolean foundationApproach) {
        if(!foundationApproach && shark() && cached!=null && !cached.isRemoved()){
            // Keep the complete model below the deck while chasing swimmers or bait near foundations.
            AABB hull=cached.getBoundingBox().inflate(1.5);
            if(target.x>=hull.minX && target.x<=hull.maxX && target.z>=hull.minZ && target.z<=hull.maxZ){
                target=new Vec3(target.x,Math.min(target.y,cached.getY()-SWIM_DEPTH),target.z);
            }
        }
        Vec3 desired=target.subtract(position());if(desired.lengthSqr()>.0001){desired=desired.normalize().scale(Math.min(speed,desired.length()));}
        Vec3 motion=getDeltaMovement().scale(.65).add(desired.scale(.35));
        if(shark() && desired.horizontalDistanceSqr()>.0001){
            // Steer the swimming velocity along the gradual heading, creating an approach arc.
            face(target);
            double speedScale=.2+.8*(1-Math.min(180,facing(target))/180);
            double heading=Math.toRadians(getYRot()),horizontal=motion.horizontalDistance()*speedScale;
            motion=new Vec3(-Math.sin(heading)*horizontal,motion.y,Math.cos(heading)*horizontal);
        }
        if(!level().hasChunkAt(BlockPos.containing(position().add(motion))) || !level().getWorldBorder().isWithinBounds(getBoundingBox().move(motion))){motion=Vec3.ZERO;}
        setDeltaMovement(motion);
        if(!shark() && motion.horizontalDistanceSqr()>.0001){setYRot((float)Math.toDegrees(Math.atan2(-motion.x,motion.z)));setYHeadRot(getYRot());yBodyRot=getYRot();}
    }
    private void face(Vec3 target){
        Vec3 direction=target.subtract(position());
        if(direction.horizontalDistanceSqr()>.0001){
            float heading=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));
            float change=(float)RaftTransform.difference(heading,getYRot());
            setYRot(getYRot()+(float)Math.clamp(change,-SHARK_TURN_DEGREES,SHARK_TURN_DEGREES));
            setYHeadRot(getYRot());yBodyRot=getYRot();
        }
    }
    private double facing(Vec3 target){
        Vec3 direction=target.subtract(position());
        return Math.abs(RaftTransform.difference(Math.toDegrees(Math.atan2(-direction.x,direction.z)),getYRot()));
    }
    @Override public boolean hurtServer(ServerLevel level,DamageSource source,float damage) {
        boolean hurt=super.hurtServer(level,source,damage);
        if(hurt && shark() && targetCell!=null && source.getEntity() instanceof Player){
            if(++repelHits<RaftConfig.REPEL_HITS.get()){return true;}
            beginRetreat(FoundationBite.jaw(position(),getYRot()));
            restartRaftAttackDelay();targetCell=null;windup=0;bitePause=0;repelHits=0;fleeTicks=0;cooldown=Math.max(cooldown,RaftConfig.REPEL_COOLDOWN.get());
            getEntityData().set(PHASE,4);getEntityData().set(BITE_WINDUP,0);
            getEntityData().set(BITE_IMPACT,-1L);getEntityData().set(FOUNDATION_BITE,false);
            return true;
        }
        if(hurt){if(targetCell!=null){if(shark()){restartRaftAttackDelay();}else{restartCropTheftDelay();}}fleeTicks=120;cooldown=Math.max(cooldown,120);windup=0;targetCell=null;retreatTicks=0;preyId=null;getEntityData().set(PHASE,0);
            getEntityData().set(BITE_WINDUP,0);getEntityData().set(BITE_IMPACT,-1L);getEntityData().set(FOUNDATION_BITE,false);}
        return hurt;
    }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);if(raftId!=null){output.putString("Raft",raftId.toString());}
        output.putInt("Cooldown",cooldown);output.putInt("FleeTicks",fleeTicks);
        if(shark()){output.putInt("RaftAttackDelay",!raftAttackDelayInitialized || targetCell!=null?RaftConfig.RAFT_ATTACK_DELAY.get()*20:raftAttackDelay);}
        else {output.putInt("CropTheftDelay",!cropTheftDelayInitialized || targetCell!=null?RaftConfig.BIRD_THEFT_DELAY.get()*20:cropTheftDelay);}
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);cached=null;raftId=null;
        try{String id=input.getStringOr("Raft","");if(!id.isEmpty()){raftId=UUID.fromString(id);}}catch(IllegalArgumentException ignored){ }
        cooldown=Math.clamp(input.getIntOr("Cooldown",200),0,12000);fleeTicks=Math.clamp(input.getIntOr("FleeTicks",0),0,120);
        raftAttackDelay=shark()?Math.clamp(input.getIntOr("RaftAttackDelay",RaftConfig.RAFT_ATTACK_DELAY.get()*20),0,72000):0;
        raftAttackDelayInitialized=true;
        cropTheftDelay=!shark()?Math.clamp(input.getIntOr("CropTheftDelay",RaftConfig.BIRD_THEFT_DELAY.get()*20),0,72000):0;
        cropTheftDelayInitialized=true;
        targetCell=null;windup=0;attackNormal=null;preyId=null;retreatTicks=0;approachTicks=0;
        bitesInAttack=0;repelHits=0;bitePause=0;
        getEntityData().set(BITE_WINDUP,0);getEntityData().set(BITE_IMPACT,-1L);getEntityData().set(FOUNDATION_BITE,false);
    }
}
