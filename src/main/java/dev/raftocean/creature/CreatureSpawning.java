package dev.raftocean.creature;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.salvage.DebrisSpawning;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Mode-dependent sharks and one pest per occupied raft, nearby land fauna, global caps, and no forced chunks. */
public final class CreatureSpawning {
    private CreatureSpawning(){ }
    public static void onTick(LevelTickEvent.Post event) {
        if(event.getLevel() instanceof ServerLevel level && level.getGameTime()%RaftConfig.CREATURE_INTERVAL.get()==0){spawn(level);}
    }
    public static int spawn(ServerLevel level) {
        if(!RaftConfig.CREATURES.get() || !level.getGameRules().get(GameRules.SPAWN_MOBS) || level.players().isEmpty()){return 0;}
        var rafts=new LinkedHashSet<RaftEntity>();
        for(var player:level.players()){if(player.isAlive() && !player.isSpectator()){rafts.addAll(level.getEntitiesOfClass(RaftEntity.class,player.getBoundingBox().inflate(48),r->!r.isRemoved()));}}
        var existing=new ArrayList<Entity>();
        for(var entity:level.getAllEntities()){if(!entity.isRemoved() && (entity instanceof OceanCreature || entity instanceof IslandGrazer)){existing.add(entity);}}
        int made=0;
        for(var raft:rafts) {
            if(existing.size()>=RaftConfig.CREATURE_CAP.get()){break;}
            long sharks = existing.stream().filter(e->e instanceof OceanCreature c && c.shark() && raft.getUUID().equals(c.raftId())).count();
            for(long number=sharks; RaftConfig.SHARKS.get() && number<RaftConfig.sharkCount(level.getDifficulty()) && existing.size()<RaftConfig.CREATURE_CAP.get();number++) {
                double radius=Math.max(7,raft.getBoundingBox().getXsize()/2+4),angle=level.getRandom().nextDouble()*Math.PI*2;
                Vec3 point=raft.position().add(Math.cos(angle)*radius,0,Math.sin(angle)*radius);
                double surface=DebrisSpawning.surface(level,point.x,raft.getY(),point.z);
                if(Double.isFinite(surface)){
                    var shark=new OceanCreature(CreatureContent.SHARK.get(),level);shark.setPos(point.x,surface-OceanCreature.SWIM_DEPTH,point.z);shark.bind(raft);
                    if(install(level,shark)){existing.add(shark);made++;}
                }
            }
            if(existing.size()<RaftConfig.CREATURE_CAP.get() && RaftConfig.effectiveDifficulty(level.getDifficulty())!=Difficulty.PEACEFUL && RaftConfig.BIRDS.get()
                    && raft.pieces().values().stream().anyMatch(p->p.kind()==dev.raftocean.entity.RaftPiece.Kind.PLANTER)
                    && existing.stream().noneMatch(e->e instanceof OceanCreature c && !c.shark() && raft.getUUID().equals(c.raftId()))) {
                var bird=new OceanCreature(CreatureContent.BIRD.get(),level);bird.setPos(raft.position().add(7,7,0));bird.bind(raft);
                if(install(level,bird)){existing.add(bird);made++;}
            }
            if(!RaftConfig.FAUNA.get() || existing.stream().filter(e->e instanceof IslandGrazer && e.distanceToSqr(raft)<48*48).count()>=2){continue;}
            for(int attempt=0;attempt<8 && existing.size()<RaftConfig.CREATURE_CAP.get();attempt++) {
                double angle=level.getRandom().nextDouble()*Math.PI*2,radius=10+level.getRandom().nextInt(20);
                BlockPos column=BlockPos.containing(raft.getX()+Math.cos(angle)*radius,raft.getY(),raft.getZ()+Math.sin(angle)*radius);
                if(!level.hasChunkAt(column)){continue;}
                int y=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column.getX(),column.getZ());
                BlockPos feet=new BlockPos(column.getX(),y,column.getZ());
                var ground=level.getBlockState(feet.below());
                if(!ground.is(Blocks.GRASS_BLOCK) && !ground.is(Blocks.SAND)){continue;}
                if(!level.getFluidState(feet).isEmpty() || !level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()){continue;}
                var animal=new IslandGrazer(CreatureContent.GRAZER.get(),level);animal.setPos(feet.getX()+.5,y,feet.getZ()+.5);
                if(install(level,animal)){existing.add(animal);made++;break;}
            }
        }
        return made;
    }
    private static boolean install(ServerLevel level,Entity entity) {
        var box=entity.getBoundingBox();
        for(double x:new double[]{box.minX,box.maxX}){for(double z:new double[]{box.minZ,box.maxZ}){
            if(!level.hasChunkAt(BlockPos.containing(x,entity.getY(),z))){return false;}
        }}
        return level.isPositionEntityTicking(entity.blockPosition()) && level.getWorldBorder().isWithinBounds(box)
                && level.noCollision(entity,box) && level.addFreshEntity(entity);
    }
}
