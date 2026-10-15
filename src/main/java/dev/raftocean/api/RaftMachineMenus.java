package dev.raftocean.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;

/** Native inventory menus and a custom-menu opener carrying raft ID/local anchor. */
public final class RaftMachineMenus {
    private RaftMachineMenus() { }
    public interface Factory { AbstractContainerMenu create(int id,Inventory inventory,RaftMachineContext context); }
    public static AbstractContainerMenu inventory(int id,Inventory inventory,RaftMachineContext context) {
        int rows=context.state().getContainerSize()/9;
        MenuType<?> type=switch(rows) {
            case 1->MenuType.GENERIC_9x1;case 2->MenuType.GENERIC_9x2;case 3->MenuType.GENERIC_9x3;
            case 4->MenuType.GENERIC_9x4;case 5->MenuType.GENERIC_9x5;default->MenuType.GENERIC_9x6;
        };
        return new ChestMenu(type,id,inventory,context.state(),rows) {
            {
                var behavior=RaftBuildables.machine(context.type().id().toString()).orElseThrow();
                for(int i=0;i<context.state().getContainerSize();i++) {
                    final int cell=i;var previous=slots.get(i);
                    var filtered=new Slot(context.state(),i,previous.x,previous.y) {
                        @Override public boolean mayPlace(net.minecraft.world.item.ItemStack stack) { return !behavior.quickHarvestSlots().contains(cell) && behavior.canInsert(context,cell,stack); }
                        @Override public boolean mayPickup(Player player) { return !player.isSpectator() && context.valid(player) && behavior.canExtract(context,cell,player); }
                    };
                    filtered.index=i;slots.set(i,filtered);
                }
            }
            @Override public boolean stillValid(Player player) { return context.valid(player); }
            @Override public void clicked(int index,int button,ContainerInput input,Player player) {
                if(player.isSpectator() || !context.valid(player))return;
                super.clicked(index,button,input,player);
            }
            @Override public net.minecraft.world.item.ItemStack quickMoveStack(Player player,int index) {
                if(player.isSpectator() || !context.valid(player) || index<0 || index>=slots.size())return net.minecraft.world.item.ItemStack.EMPTY;
                var slot=slots.get(index);if(!slot.hasItem() || !slot.mayPickup(player))return net.minecraft.world.item.ItemStack.EMPTY;
                var stack=slot.getItem();var before=stack.copy();int machine=context.state().getContainerSize();boolean moved=false;
                if(index<machine) { moved=moveItemStackTo(stack,machine,slots.size(),true); }
                else {
                    // Native merge passes do not invoke mayPlace; filter each destination first.
                    for(int target=0;target<machine && !stack.isEmpty();target++) {
                        if(slots.get(target).mayPlace(stack))moved|=moveItemStackTo(stack,target,target+1,false);
                    }
                    if(!moved)moved=index<machine+27?moveItemStackTo(stack,machine+27,slots.size(),false):moveItemStackTo(stack,machine,machine+27,false);
                }
                if(!moved)return net.minecraft.world.item.ItemStack.EMPTY;
                if(stack.isEmpty())slot.setByPlayer(net.minecraft.world.item.ItemStack.EMPTY);else slot.setChanged();
                slot.onTake(player,stack);return before;
            }
        };
    }
    /** Opt-in shortcut; canExtract still protects locked outputs. Legacy controllers default to PASS. */
    public static net.minecraft.world.InteractionResult quickHarvest(RaftMachineContext context,Player player,net.minecraft.world.InteractionHand hand) {
        if(!dev.raftocean.config.RaftConfig.QUICK_HARVEST.get() || !player.isShiftKeyDown() || !player.getItemInHand(hand).isEmpty())
            return net.minecraft.world.InteractionResult.PASS;
        var behavior=RaftBuildables.machine(context.type().id().toString()).orElseThrow();
        if(behavior.quickHarvestSlots().isEmpty())return net.minecraft.world.InteractionResult.PASS;
        if(player.isSpectator() || !context.valid(player))return net.minecraft.world.InteractionResult.FAIL;
        var slots=behavior.quickHarvestSlots().stream().sorted().filter(slot->behavior.canExtract(context,slot,player)).toList();
        int moved=dev.raftocean.menu.InventoryTransfers.collect(context.state(),slots,player);
        int remaining=slots.stream().mapToInt(slot->context.state().getItem(slot).getCount()).sum();
        String message=moved>0?(remaining>0?"partial":"collected"):(remaining>0?"full":"empty");
        player.sendOverlayMessage(Component.translatable("raft_ocean.harvest."+message,moved,remaining));
        return net.minecraft.world.InteractionResult.SUCCESS_SERVER;
    }
    public static void openInventory(RaftMachineContext context,Player player) {
        if(!context.valid(player))return;
        player.openMenu(new SimpleMenuProvider((id,inventory,user)->inventory(id,inventory,context),new net.minecraft.world.item.ItemStack(context.type().item().get()).getHoverName()));
    }
    /** The addon's IContainerFactory must read readVarInt(), then readBlockPos(). */
    public static void openCustom(RaftMachineContext context,Player player,Component title,Factory factory) {
        if(!(player instanceof ServerPlayer server) || !context.valid(player))return;
        server.openMenu(new SimpleMenuProvider((id,inventory,user)->factory.create(id,inventory,context),title),
                buffer->{buffer.writeVarInt(context.raft().getId());buffer.writeBlockPos(context.anchor());});
    }
}
