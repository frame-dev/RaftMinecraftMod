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
    /** Opt-in inputs connect at the boundary of the rotated occupied footprint. */
    default java.util.Set<RaftUtility> utilityInputs() { return java.util.Set.of(); }
    /** Positive FE demand wakes connected generators at their normal production rate. */
    default int powerDemand(RaftMachineContext context) { return 0; }
    /** Empty by default, preserving existing addons' sneak-use behavior. */
    default java.util.Set<Integer> quickHarvestSlots() { return java.util.Set.of(); }
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
