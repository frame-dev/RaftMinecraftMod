package dev.raftocean.menu;

import dev.raftocean.api.RaftMachineContext;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.salvage.SalvageCompactor;
import dev.raftocean.survival.Batteries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Server-owned role slots and short-safe progress; clients never send batch contents. */
public final class CompactorMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>,MenuType<CompactorMenu>> TYPE=RadioMenu.TYPES.register("salvage_compactor",()->
            net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id,inventory,buffer)->{
                buffer.readVarInt();buffer.readBlockPos();return new CompactorMenu(id,inventory,null);
            }));
    public static void initialize() { }
    private final RaftMachineContext context;
    private final Player owner;
    private final Container inventory;
    private final ContainerData data;
    public CompactorMenu(int id,Inventory playerInventory,RaftMachineContext context){
        super(TYPE.get(),id);this.context=context;owner=playerInventory.player;
        inventory=context==null?new SimpleContainer(9):context.state();
        data=context==null?new SimpleContainerData(7):new ContainerData(){
            public int getCount(){return 7;}
            public void set(int index,int value) { }
            public int get(int index){
                if(!context.isInstalled())return 0;
                var s=context.state();var battery=s.getItem(7);int points=0;
                for(int i=0;i<7;i++)points+=SalvageCompactor.value(s.getItem(i))*s.getItem(i).getCount();
                return switch(index){
                    case 0->s.getInt("progress");case 1->RaftConfig.COMPACTOR_SECONDS.get();case 2->Batteries.charge(battery);
                    case 3->Batteries.capacity(battery);case 4->status();case 5->Math.min(40,points);case 6->s.getInt("paused");default->0;
                };
            }
        };
        for(int i=0;i<9;i++){
            final int cell=i;int x=i<7?12+i*18:i==7?164:198;
            addSlot(new Slot(inventory,i,x,51){
                @Override public boolean mayPlace(ItemStack stack){return cell<7?SalvageCompactor.value(stack)>0:cell==7 && Batteries.capacity(stack)>0;}
                @Override public boolean mayPickup(Player player){return stillValid(player);}
                @Override public int getMaxStackSize(){return cell==7?1:cell==8?4:super.getMaxStackSize();}
            });
        }
        addStandardInventorySlots(playerInventory,35,141);addDataSlots(data);
    }
    private int status(){
        var s=context.state();var out=s.getItem(8);
        if(!RaftConfig.RECYCLING.get())return 4;if(s.getInt("paused")!=0)return 5;
        if(SalvageCompactor.batch(s).isEmpty())return 0;
        if(!out.isEmpty() && (!ItemStack.isSameItemSameComponents(out,new ItemStack(SalvageCompactor.BALE.get())) || out.getCount()>=4))return 2;
        if(Batteries.charge(s.getItem(7))<1)return 1;return 3;
    }
    public int value(int index){return data.get(index);}
    @Override public boolean stillValid(Player player){return owner==player && !player.isSpectator() && (context==null || context.valid(player));}
    @Override public void clicked(int slot,int button,ContainerInput action,Player player){if(stillValid(player))super.clicked(slot,button,action,player);}
    @Override public boolean clickMenuButton(Player player,int button){
        if(context==null || !stillValid(player) || button!=0 || !getCarried().isEmpty())return false;
        var s=context.state();s.setInt("paused",s.getInt("paused")==0?1:0);if(s.getInt("paused")!=0)s.setInt("active",0);broadcastChanges();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player) || index<0 || index>=slots.size())return ItemStack.EMPTY;
        var slot=slots.get(index);if(!slot.hasItem() || !slot.mayPickup(player))return ItemStack.EMPTY;
        var stack=slot.getItem();var before=stack.copy();boolean moved=false;
        if(index<9)moved=moveItemStackTo(stack,9,45,true);
        else{
            // Native merge passes can skip mayPlace: prefilter every destination.
            for(int target=0;target<8 && !stack.isEmpty();target++)if(slots.get(target).mayPlace(stack))moved|=moveItemStackTo(stack,target,target+1,false);
            if(!moved)moved=index<36?moveItemStackTo(stack,36,45,false):moveItemStackTo(stack,9,36,false);
        }
        if(!moved)return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return before;
    }
}
