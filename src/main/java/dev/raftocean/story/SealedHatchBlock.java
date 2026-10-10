package dev.raftocean.story;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The Echo Vault's two-block seal opens with a relay key and the local ON/OFF/ON lever pattern. */
public final class SealedHatchBlock extends Block {
    public SealedHatchBlock(Properties properties) { super(properties); }
    public static boolean solved(Level level,BlockPos base) {
        for(int i=0;i<3;i++) {
            var state=level.getBlockState(base.offset((i-1)*2,0,-2));
            if(!state.is(Blocks.LEVER) || state.getValue(LeverBlock.POWERED)!=(i!=1)){return false;}
        }
        return true;
    }
    public static InteractionResult unlock(Level level,BlockPos clicked,Player player,ItemStack key) {
        if(!level.getBlockState(clicked).is(IslandContent.HATCH.get())){return InteractionResult.PASS;}
        BlockPos base=level.getBlockState(clicked.below()).is(IslandContent.HATCH.get())?clicked.below():clicked;
        if(!key.is(IslandContent.KEY.get()) || !solved(level,base)) {
            if(!level.isClientSide()){player.sendOverlayMessage(Component.translatable("raft_ocean.story.sealed"));}
            return InteractionResult.FAIL;
        }
        if(!level.isClientSide()) {
            level.setBlock(base,Blocks.AIR.defaultBlockState(),3);
            if(level.getBlockState(base.above()).is(IslandContent.HATCH.get())){level.setBlock(base.above(),Blocks.AIR.defaultBlockState(),3);}
            if(!player.getAbilities().instabuild){key.shrink(1);}
            player.sendSystemMessage(Component.translatable("raft_ocean.story.open"));
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        return unlock(level,pos,player,stack);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        return unlock(level,pos,player,ItemStack.EMPTY);
    }
}
