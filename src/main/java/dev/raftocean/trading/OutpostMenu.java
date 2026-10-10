package dev.raftocean.trading;

import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

/** Native merchant packets/UI, without vanilla's shift-trade Entity cast. */
public final class OutpostMenu extends MerchantMenu {
    private final OutpostTrading merchant;
    public OutpostMenu(int id,Inventory inventory,OutpostTrading merchant){super(id,inventory,merchant);this.merchant=merchant;}
    public OutpostTrading merchant(){return merchant;}
    @Override public void clicked(int slot,int button,ContainerInput input,Player player){
        if(!stillValid(player)){player.closeContainer();return;}
        super.clicked(slot,button,input,player);
    }
    @Override public void tryMoveItems(int index){
        var player=merchant.getTradingPlayer();if(player!=null && stillValid(player))super.tryMoveItems(index);
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player) || index<0 || index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem() || !slot.mayPickup(player))return ItemStack.EMPTY;
        var stack=slot.getItem();var original=stack.copy();
        if(index==2){
            if(!moveItemStackTo(stack,3,39,true))return ItemStack.EMPTY;
            slot.onQuickCraft(stack,original);
        } else if(index==0 || index==1){
            if(!moveItemStackTo(stack,3,39,false))return ItemStack.EMPTY;
        } else if(index<30){
            if(!moveItemStackTo(stack,30,39,false))return ItemStack.EMPTY;
        } else if(!moveItemStackTo(stack,3,30,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        if(stack.getCount()==original.getCount())return ItemStack.EMPTY;
        slot.onTake(player,stack);return original;
    }
}
