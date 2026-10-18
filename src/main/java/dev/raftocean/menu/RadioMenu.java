package dev.raftocean.menu;

import dev.raftocean.api.RaftMachineContext;
import dev.raftocean.story.*;
import dev.raftocean.survival.Batteries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.*;

/** Short-safe native data slots; no client-supplied position or target is trusted. */
public final class RadioMenu extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> TYPES=DeferredRegister.create(Registries.MENU,"raft_ocean");
    public static final DeferredHolder<MenuType<?>,MenuType<RadioMenu>> TYPE=TYPES.register("survey_receiver",()->
            net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id,inventory,buffer)->{
                buffer.readVarInt();buffer.readBlockPos();return new RadioMenu(id,inventory,null);
            }));
    private final RaftMachineContext context;
    private final Player owner;
    private final Container inventory;
    private final ContainerData data;
    public RadioMenu(int id,Inventory playerInventory,RaftMachineContext context){
        super(TYPE.get(),id);this.context=context;owner=playerInventory.player;
        inventory=context==null?new SimpleContainer(9):context.state();
        data=context==null?new SimpleContainerData(11):new ContainerData(){
            public int getCount(){return 11;}
            public void set(int index,int value) { }
            public int get(int index){
                if(!context.isInstalled())return 0;
                var s=context.state();var battery=s.getItem(0);var relative=RaftRadio.relative(context);
                return switch(index){
                    case 0->s.getInt("enabled");case 1->s.getInt("status");case 2->s.getInt("antennas");
                    case 3->Batteries.charge(battery);case 4->Batteries.capacity(battery);case 5->s.getInt("channel");
                    case 6->(int)Math.round(relative.x);case 7->(int)Math.round(relative.z);
                    case 8->(int)Math.round(relative.horizontalDistance());
                    case 9->Math.clamp(owner.getData(IslandContent.JOURNEY),0,ReceiverItem.DESTINATIONS.length);
                    case 10->(int)dev.raftocean.navigation.RaftNavigation.normalize(context.raft().getYRot()+context.raft().pieces().get(context.anchor()).rotation()*90);
                    default->0;
                };
            }
        };
        addSlot(new Slot(inventory,0,150,57){
            @Override public boolean mayPlace(ItemStack stack){return Batteries.capacity(stack)>0;}
            @Override public boolean mayPickup(Player player){return stillValid(player) && !player.isSpectator();}
            @Override public int getMaxStackSize(){return 1;}
        });
        addStandardInventorySlots(playerInventory,59,181);addDataSlots(data);
    }
    public int value(int index){return data.get(index);}
    @Override public boolean stillValid(Player player){return owner==player && !player.isSpectator() && (context==null || context.valid(player));}
    @Override public void broadcastChanges(){if(context!=null && stillValid(owner))RaftRadio.refresh(context);super.broadcastChanges();}
    @Override public void clicked(int slot,int button,ContainerInput action,Player player){if(stillValid(player))super.clicked(slot,button,action,player);}
    @Override public boolean clickMenuButton(Player player,int button){
        if(context==null || !stillValid(player) || button<0 || button>5)return false;
        if(button==5){ExpeditionNotebook.open(player);return true;}
        var s=context.state();int old=Math.clamp(s.getInt("channel"),0,ReceiverItem.DESTINATIONS.length);
        int max=Math.clamp(player.getData(IslandContent.JOURNEY),0,ReceiverItem.DESTINATIONS.length);
        if(button==0)s.setInt("enabled",s.getInt("enabled")==0?1:0);
        else if(button==1){if(old>max)return false;RaftRadio.scan(context);}
        else{
            int channel=button==4?max:button==2?Math.max(0,Math.min(max,old-1)):Math.min(max,old+1);
            if(channel==old)return false;s.setInt("channel",channel);s.setInt("found",0);
        }
        RaftRadio.refresh(context);broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player) || index<0 || index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem() || !slot.mayPickup(player))return ItemStack.EMPTY;
        var stack=slot.getItem();var before=stack.copy();boolean moved=index==0?moveItemStackTo(stack,1,37,true):
                slots.get(0).mayPlace(stack) && moveItemStackTo(stack,0,1,false);
        if(!moved && index!=0)moved=index<28?moveItemStackTo(stack,28,37,false):moveItemStackTo(stack,1,28,false);
        if(!moved)return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
}
