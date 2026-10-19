package dev.raftocean.menu;

import dev.raftocean.api.RaftMachineContext;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.survival.RaftJuicer;
import dev.raftocean.survival.Batteries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Server-owned role slots and short-safe progress; clients never send batch contents. */
public final class JuicerMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>,MenuType<JuicerMenu>> TYPE=RadioMenu.TYPES.register("raft_juicer",()->
            net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id,inventory,buffer)->{
                buffer.readVarInt();buffer.readBlockPos();return new JuicerMenu(id,inventory,null);
            }));
    public static void initialize() { }
    private final RaftMachineContext context;
    private final Player owner;
    private final Container inventory;
    private final ContainerData data;
    public JuicerMenu(int id,Inventory playerInventory,RaftMachineContext context){
        super(TYPE.get(),id);this.context=context;owner=playerInventory.player;
        inventory=context==null?new SimpleContainer(9):context.state();
        data=context==null?new SimpleContainerData(8):new ContainerData(){
            public int getCount(){return 8;}
            public void set(int index,int value) { }
            public int get(int index){
                if(!context.isInstalled())return 0;
                var s=context.state();var battery=s.getItem(4);
                return switch(index){
                    case 0->s.getInt("progress");case 1->s.getInt("duration");case 2->Batteries.charge(battery);
                    case 3->Batteries.capacity(battery);case 4->status();case 5->s.getInt("running");
                    case 6->s.getInt("paused");case 7->s.getItem(8).isEmpty()?0:1;default->0;
                };
            }
        };
        for(int i=0;i<7;i++){
            final int cell=i;int x=i<4?18+i*22:i==4?18:i==5?122:190;int y=i<4?50:100;
            addSlot(new Slot(inventory,i,x,y){
                @Override public boolean mayPlace(ItemStack stack){return cell<4?RaftJuicer.isIngredient(stack):cell==4?Batteries.capacity(stack)>0:cell==5 && stack.is(RaftJuicer.GLASS.get());}
                @Override public boolean mayPickup(Player player){return stillValid(player);}
                @Override public int getMaxStackSize(){return cell==4?1:super.getMaxStackSize();}
            });
        }
        addStandardInventorySlots(playerInventory,35,166);addDataSlots(data);
    }
    private int status(){
        var s=context.state();
        if(!RaftConfig.JUICING.get())return 4;
        if(!s.getItem(8).isEmpty()){
            var out=s.getItem(6);if(!out.isEmpty() && (!ItemStack.isSameItemSameComponents(out,s.getItem(8)) || out.getCount()>=out.getMaxStackSize()))return 8;
            return 2;
        }
        if(s.getInt("paused")!=0)return 5;

        if(Batteries.charge(s.getItem(4))<1)return 1;
        if(s.getInt("running")!=0)return 3;
        return RaftJuicer.recipe(context).isPresent()?7:s.getInt("changed")!=0?9:0;
    }
    public int value(int index){return data.get(index);}
    @Override public boolean stillValid(Player player){return owner==player && !player.isSpectator() && (context==null || context.valid(player));}
    @Override public void clicked(int slot,int button,ContainerInput action,Player player){if(stillValid(player))super.clicked(slot,button,action,player);}
    @Override public boolean clickMenuButton(Player player,int button){
        if(context==null || !stillValid(player) || !getCarried().isEmpty())return false;
        if(button==0)return RaftJuicer.start(context);
        if(button==2)return RaftJuicer.serve(context);
        if(button!=1)return false;
        var s=context.state();s.setInt("paused",s.getInt("paused")==0?1:0);if(s.getInt("paused")!=0)s.setInt("active",0);broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player) || index<0 || index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem() || !slot.mayPickup(player))return ItemStack.EMPTY;
        var stack=slot.getItem();var before=stack.copy();boolean moved=false;
        if(index<7)moved=moveItemStackTo(stack,7,43,true);
        else{
            // Native merge passes can skip mayPlace: prefilter every destination.
            for(int target=0;target<6 && !stack.isEmpty();target++)if(slots.get(target).mayPlace(stack))moved|=moveItemStackTo(stack,target,target+1,false);
            if(!moved)moved=index<34?moveItemStackTo(stack,34,43,false):moveItemStackTo(stack,7,34,false);
        }
        if(!moved)return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
}
