package example.raftaddon;

import dev.raftocean.api.*;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Example: one sand + ten FE + one cup of network water becomes one clay ball. */
public final class SedimentWasher implements RaftMachineBehavior {
    private final Supplier<Block> idle,working;
    public SedimentWasher(Supplier<Block> idle,Supplier<Block> working) { this.idle=idle;this.working=working; }
    public int tickInterval() { return 20; }
    public Set<RaftUtility> utilityInputs() { return Set.of(RaftUtility.POWER,RaftUtility.FRESH_WATER); }
    public Set<Integer> quickHarvestSlots() { return Set.of(1); }
    public boolean canInsert(RaftMachineContext ctx,int slot,ItemStack stack) { return slot==0 && stack.is(Items.SAND); }
    public void placed(RaftMachineContext ctx,Player player) { initialize(ctx); }
    public void loaded(RaftMachineContext ctx) { initialize(ctx); }
    private static void initialize(RaftMachineContext ctx) {
        if(!ctx.state().hasInt("schema")) { ctx.state().setInt("enabled",1);ctx.state().setInt("schema",1); }
        ctx.state().setInt("duration",20);
    }
    private static Map<Integer,ItemStack> input() { return Map.of(0,new ItemStack(Items.SAND)); }
    private static Map<Integer,ItemStack> output() { return Map.of(1,new ItemStack(Items.CLAY_BALL)); }
    public int powerDemand(RaftMachineContext ctx) {
        return ctx.state().getInt("enabled")!=0 && RaftMachineTransactions.canProcess(ctx,input(),output(),new RaftUtilityCost(0,1,0))?10:0;
    }
    public void tick(RaftMachineContext ctx) {
        ctx.state().setInt("active",0);
        if(ctx.state().getInt("enabled")==0)return;
        if(RaftMachineTransactions.tryProcess(ctx,input(),output(),new RaftUtilityCost(10,1,0))) {
            ctx.state().setInt("active",1);ctx.state().setInt("progress",20);
        } else { ctx.state().setInt("progress",0); }
    }
    public BlockState visual(RaftBuildable type,RaftMachineView view) { return (view.active()?working:idle).get().defaultBlockState(); }
}
