package example.raftaddon;

import dev.raftocean.api.*;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Slot 0: kelp, slot 1: output, slot 2: sticks. Other slots are storage. */
public final class KelpDryer implements RaftMachineBehavior {
    private final Supplier<Block> idle;
    private final Supplier<Block> working;
    public KelpDryer(Supplier<Block> idle,Supplier<Block> working) { this.idle=idle;this.working=working; }
    @Override public boolean canInsert(RaftMachineContext context,int slot,ItemStack stack) {
        return switch(slot) { case 0->stack.is(Items.KELP);case 1->false;case 2->stack.is(Items.STICK);default->true; };
    }
    @Override public void placed(RaftMachineContext context,Player player) { initialize(context.state()); }
    @Override public void loaded(RaftMachineContext context) { initialize(context.state()); }
    private static void initialize(RaftMachineState state) {
        if(state.getInt("schema")==0) { state.setInt("enabled",1);state.setInt("schema",1); }
        state.setInt("duration",100);
    }
    @Override public void tick(RaftMachineContext context) { process(context.state()); }
    public static void process(RaftMachineState state) {
        state.setInt("active",0);
        var input=state.getItem(0);var output=state.getItem(1);
        boolean room=output.isEmpty() || output.is(Items.DRIED_KELP) && output.getCount()<output.getMaxStackSize();
        if(state.getInt("enabled")==0 || !input.is(Items.KELP) || !room)return;
        if(state.getInt("fuel")<=0) {
            var fuel=state.getItem(2);if(!fuel.is(Items.STICK))return;
            fuel.shrink(1);state.setInt("fuel",200);
        }
        state.setInt("fuel",state.getInt("fuel")-1);state.setInt("active",1);
        int progress=state.getInt("progress")+1;
        if(progress>=100) {
            input.shrink(1);
            if(output.isEmpty())state.setItem(1,new ItemStack(Items.DRIED_KELP));else output.grow(1);
            progress=0;
        }
        state.setInt("progress",progress);state.setChanged();
    }
    @Override public InteractionResult use(RaftMachineContext context,Player player,InteractionHand hand) {
        if(player.isShiftKeyDown()) {
            var state=context.state();state.setInt("enabled",state.getInt("enabled")==0?1:0);
            player.sendOverlayMessage(Component.translatable(state.getInt("enabled")==0?"raft_example.dryer.paused":"raft_example.dryer.running"));
        } else {
            player.sendOverlayMessage(Component.translatable("raft_example.dryer.slots"));
            RaftMachineMenus.openInventory(context,player);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
    @Override public BlockState visual(RaftBuildable type,RaftMachineView view) {
        return (view.active()?working:idle).get().defaultBlockState();
    }
}
