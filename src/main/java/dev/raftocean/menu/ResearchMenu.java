package dev.raftocean.menu;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.progression.Research;
import dev.raftocean.progression.ResearchCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.*;

/** Temporary research sample is returned on close, removal, death or lost reach. */
public final class ResearchMenu extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> TYPES=DeferredRegister.create(Registries.MENU,"raft_ocean");
    public static final DeferredHolder<MenuType<?>,MenuType<ResearchMenu>> TYPE=TYPES.register("research",()->
            new MenuType<>((id,inventory)->new ResearchMenu(id,inventory,null,BlockPos.ZERO),FeatureFlags.VANILLA_SET));
    public static final int STUDY_SAMPLE=1000;
    private static final int MATERIAL_START=Research.TOPICS.length;
    private static final int INPUT_STATE=MATERIAL_START+ResearchCatalog.MATERIALS.length;
    private final RaftEntity raft;
    private final BlockPos pos;
    private final Player owner;
    private final ContainerData data;
    private final SimpleContainer input=new SimpleContainer(1);
    public ResearchMenu(int id,Inventory inventory,RaftEntity raft,BlockPos pos){
        super(TYPE.get(),id);this.raft=raft;this.pos=pos.immutable();owner=inventory.player;
        data=raft==null?new SimpleContainerData(INPUT_STATE+1):new ContainerData(){
            public int get(int index){
                if(index<MATERIAL_START){return Research.state(owner,index);}
                if(index<INPUT_STATE){return Research.materialKnown(owner,index-MATERIAL_START)?1:0;}
                int material=Research.materialIndex(input.getItem(0));
                return material<0?0:Research.materialKnown(owner,material)?2:input.getItem(0).getCount()<Research.sampleCost(material)?3:1;
            }
            public void set(int index,int value){}
            public int getCount(){return INPUT_STATE+1;}
        };
        addSlot(new Slot(input,0,46,62){@Override public boolean mayPlace(ItemStack stack){return Research.materialIndex(stack)>=0;}});
        addStandardInventorySlots(inventory,99,211);addDataSlots(data);
    }
    public int state(int topic){return data.get(topic);}
    public boolean materialKnown(int material){return data.get(MATERIAL_START+material)==1;}
    public int inputState(){return data.get(INPUT_STATE);}
    public Player owner(){return owner;}
    @Override public boolean stillValid(Player player){
        return raft==null?player==owner:player==owner && RaftStationMenus.valid(raft,pos,RaftPiece.Kind.RESEARCH_TABLE,player);
    }
    @Override public boolean clickMenuButton(Player player,int button){
        if(raft==null || !stillValid(player)){return false;}
        boolean changed=button==STUDY_SAMPLE?Research.research(player,input.getItem(0)):Research.study(player,button);
        if(changed){input.setChanged();broadcastChanges();}return changed;
    }
    @Override public void clicked(int index,int button,ContainerInput action,Player player){if(stillValid(player)){super.clicked(index,button,action,player);}}
    @Override public void removed(Player player){super.removed(player);if(!player.level().isClientSide()){clearContainer(player,input);}}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(!stillValid(player) || index<0 || index>=slots.size()){return ItemStack.EMPTY;}
        var slot=slots.get(index);if(!slot.hasItem()){return ItemStack.EMPTY;}
        var stack=slot.getItem();var before=stack.copy();
        boolean moved=index==0?moveItemStackTo(stack,1,37,true):Research.materialIndex(stack)>=0 && moveItemStackTo(stack,0,1,false);
        if(!moved && index!=0){moved=index<28?moveItemStackTo(stack,28,37,false):moveItemStackTo(stack,1,28,false);}
        if(!moved){return ItemStack.EMPTY;}
        if(stack.isEmpty()){slot.setByPlayer(ItemStack.EMPTY);}else{slot.setChanged();}slot.onTake(player,stack);return before;
    }
}
