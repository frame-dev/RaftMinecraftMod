package dev.raftocean.trading;

import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** A compact counter with matching visible, selection and collision geometry. */
public final class TradingPostBlock extends Block {
    private static final VoxelShape SHAPE=Shapes.or(Block.box(1,0,2,15,13,14),Block.box(0,13,0,16,16,16),Block.box(6,7,1,10,9,2));
    public TradingPostBlock(Properties properties){super(properties);}
    @Override protected VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,CollisionContext context){return SHAPE;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(level.isClientSide())return InteractionResult.SUCCESS;
        return OutpostTrading.open((net.minecraft.server.level.ServerLevel)level,pos,player)?InteractionResult.SUCCESS_SERVER:InteractionResult.FAIL;
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        return useWithoutItem(state,level,pos,player,hit);
    }
}
