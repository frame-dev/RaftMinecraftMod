package dev.raftocean.creature;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

/** Place one exclusive physical distraction on exposed source water, with no client-side consumption. */
public final class ChumItem extends Item {
    public ChumItem(Properties properties){super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        var hit=getPlayerPOVHitResult(level,player,ClipContext.Fluid.SOURCE_ONLY);
        if(hit.getType()!=HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)
                || !level.getFluidState(hit.getBlockPos().above()).isEmpty()
                || !level.getBlockState(hit.getBlockPos().above()).getCollisionShape(level,hit.getBlockPos().above()).isEmpty()) {return InteractionResult.FAIL;}
        if(level.isClientSide()){return InteractionResult.SUCCESS;}
        ServerLevel server=(ServerLevel)level;
        int count=0;for(var entity:server.getAllEntities()){if(entity instanceof ChumEntity && !entity.isRemoved()){count++;}}
        if(count>=dev.raftocean.config.RaftConfig.BAIT_CAP.get()){return InteractionResult.FAIL;}
        var bait=new ChumEntity(CreatureContent.CHUM.get(),level);
        bait.setPos(hit.getLocation().x,hit.getBlockPos().getY()+.98,hit.getLocation().z);
        bait.start(server);
        if(!level.noCollision(bait,bait.getBoundingBox())
                || !level.getEntitiesOfClass(ChumEntity.class,bait.getBoundingBox().inflate(1),ChumEntity::active).isEmpty()){return InteractionResult.FAIL;}
        if(!level.addFreshEntity(bait)){return InteractionResult.FAIL;}
        if(!player.getAbilities().instabuild){player.getItemInHand(hand).shrink(1);}
        return InteractionResult.SUCCESS_SERVER;
    }
}
