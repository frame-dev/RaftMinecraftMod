package dev.raftocean.survival;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.InventoryTransfers;
import dev.raftocean.menu.RaftStationMenus;
import dev.raftocean.menu.SurvivalMenu;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

/** Deliberate empty-hand shortcut; only role-labelled outputs can leave a machine. */
public final class MachineHarvest {
    private MachineHarvest() { }
    public static List<Integer> outputSlots(RaftPiece.Kind kind) {
        if(!kind.machine()) { return List.of(); }
        return SurvivalMenu.layout(kind).entrySet().stream().filter(entry->entry.getValue().output()).map(java.util.Map.Entry::getKey).toList();
    }
    public static InteractionResult use(RaftEntity raft,BlockPos pos,RaftPiece.Kind kind,Player player,InteractionHand hand) {
        if(!RaftConfig.QUICK_HARVEST.get() || !player.isShiftKeyDown() || !player.getItemInHand(hand).isEmpty()) { return InteractionResult.PASS; }
        var slots=outputSlots(kind);
        if(slots.isEmpty()) { return InteractionResult.PASS; }
        if(player.isSpectator() || !RaftStationMenus.valid(raft,pos,kind,player)) { return InteractionResult.FAIL; }
        var storage=raft.storage(pos);if(storage==null) { return InteractionResult.FAIL; }
        if(raft.level().isClientSide()) { return InteractionResult.SUCCESS; }
        int moved=InventoryTransfers.collect(storage,slots,player);
        int remaining=slots.stream().mapToInt(slot->storage.getItem(slot).getCount()).sum();
        String message=moved>0?(remaining>0?"partial":"collected"):(remaining>0?"full":"empty");
        player.sendOverlayMessage(Component.translatable("raft_ocean.harvest."+message,moved,remaining));
        if(moved>0) { raft.machines().sync(); }
        return InteractionResult.SUCCESS_SERVER;
    }
}
