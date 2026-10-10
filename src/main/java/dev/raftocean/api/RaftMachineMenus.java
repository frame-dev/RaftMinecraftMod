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
                        @Override public boolean mayPlace(net.minecraft.world.item.ItemStack stack) { return behavior.canInsert(context,cell,stack); }
                        @Override public boolean mayPickup(Player player) { return behavior.canExtract(context,cell,player); }
                    };
                    filtered.index=i;slots.set(i,filtered);
                }
            }
            @Override public boolean stillValid(Player player) { return context.valid(player); }
            @Override public net.minecraft.world.item.ItemStack quickMoveStack(Player player,int index) {
                if(!context.valid(player) || index<0 || index>=slots.size() || !slots.get(index).mayPickup(player))return net.minecraft.world.item.ItemStack.EMPTY;
                return super.quickMoveStack(player,index);
            }
        };
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
