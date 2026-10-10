package dev.raftocean.api;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Register one stateless controller per type; mutable state belongs to each context. */
public interface RaftMachineBehavior {
    /** Native inventory clicks and shift-clicks enforce these server-side slot rules. */
    default boolean canInsert(RaftMachineContext context,int slot,net.minecraft.world.item.ItemStack stack) { return true; }
    default boolean canExtract(RaftMachineContext context,int slot,Player player) { return true; }
    default int slots() { return 9; }
    default int tickInterval() { return 1; }
    default void placed(RaftMachineContext context,Player player) { }
    default void loaded(RaftMachineContext context) { }
    default void removed(RaftMachineContext context) { }
    void tick(RaftMachineContext context);
    default InteractionResult use(RaftMachineContext context,Player player,InteractionHand hand) {
        RaftMachineMenus.openInventory(context,player);return InteractionResult.SUCCESS_SERVER;
    }
    default RaftMachineView view(RaftMachineContext context) {
        var state=context.state();
        return new RaftMachineView(state.getInt("active")!=0,state.getInt("progress"),state.getInt("duration"),state.getInt("variant"));
    }
    /** Runs during client extraction: use only this immutable view and registry references. */
    default BlockState visual(RaftBuildable type,RaftMachineView view) { return type.visual().get().defaultBlockState(); }
}
