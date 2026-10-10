package dev.raftocean.creature;

import dev.raftocean.config.RaftConfig;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;

public final class ChumEntity extends Entity {
    private long expires=-1;
    public ChumEntity(EntityType<? extends ChumEntity> type,Level level){super(type,level);setNoGravity(true);}
    public void start(ServerLevel level){expires=level.getGameTime()+RaftConfig.BAIT_DURATION.get();}
    public boolean active(){return !isRemoved() && expires>level().getGameTime();}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){ }
    @Override public void tick(){super.tick();if(level() instanceof ServerLevel level){if(expires<0){start(level);}if(!active() || !level.getFluidState(blockPosition()).is(net.minecraft.tags.FluidTags.WATER)){discard();}}}
    @Override protected void addAdditionalSaveData(ValueOutput output){output.putLong("Expires",expires);}
    @Override protected void readAdditionalSaveData(ValueInput input){expires=input.getLongOr("Expires",-1);}
    @Override public boolean hurtServer(ServerLevel level,DamageSource source,float damage){return false;}
}
