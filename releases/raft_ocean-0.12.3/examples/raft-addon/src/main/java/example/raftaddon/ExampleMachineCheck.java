package example.raftaddon;

import dev.raftocean.api.RaftMachineState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Optional native server verification of the actual example controller. */
public final class ExampleMachineCheck {
    private ExampleMachineCheck() { }
    public static int run(ServerLevel level) {
        var state=new RaftMachineState(9);state.setInt("enabled",1);state.setInt("duration",100);
        state.setItem(0,new ItemStack(Items.KELP,3));state.setItem(2,new ItemStack(Items.STICK));
        for(int tick=0;tick<50;tick++)KelpDryer.process(state);
        require(state.getInt("progress")==50 && state.getInt("fuel")==150 && state.getItem(0).getCount()==3,"Partial process conserves input");
        var reporter=new ProblemReporter.Collector();var output=TagValueOutput.createWithContext(reporter,level.registryAccess());state.save(output);
        var restored=new RaftMachineState(9);restored.load(TagValueInput.create(reporter,level.registryAccess(),output.buildResult()));
        require(reporter.isEmpty() && restored.getInt("fuel")==150 && restored.getInt("progress")==50 && restored.getItem(0).getCount()==3,"Example saves midway");
        restored.setInt("enabled",0);KelpDryer.process(restored);
        require(restored.getInt("fuel")==150 && restored.getInt("progress")==50 && restored.getInt("active")==0,"Pause conserves fuel/progress");
        restored.setInt("enabled",1);restored.setItem(1,new ItemStack(Items.DRIED_KELP,64));KelpDryer.process(restored);
        require(restored.getInt("fuel")==150 && restored.getInt("progress")==50,"Full output conserves fuel/progress");
        restored.setItem(1,new ItemStack(Items.DIAMOND));KelpDryer.process(restored);
        require(restored.getInt("fuel")==150 && restored.getInt("progress")==50,"Incompatible output preserves items");
        restored.setItem(1,ItemStack.EMPTY);
        for(int tick=0;tick<150;tick++)KelpDryer.process(restored);
        require(restored.getItem(0).getCount()==1 && restored.getItem(1).is(Items.DRIED_KELP)
                && restored.getItem(1).getCount()==2 && restored.getInt("fuel")==0 && restored.getInt("progress")==0,"One stick produces exactly two kelp");
        KelpDryer.process(restored);require(restored.getItem(0).getCount()==1 && restored.getItem(1).getCount()==2 && restored.getInt("active")==0,"No free output without fuel");
        restored.setItem(2,new ItemStack(Items.STICK));restored.setItem(0,ItemStack.EMPTY);KelpDryer.process(restored);
        require(restored.getItem(2).getCount()==1 && restored.getInt("fuel")==0,"Idle consumes no fuel");
        return 8;
    }
    private static void require(boolean value,String message) { if(!value)throw new IllegalStateException(message); }
}
