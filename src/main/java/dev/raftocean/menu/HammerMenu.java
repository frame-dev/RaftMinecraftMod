package dev.raftocean.menu;

import dev.raftocean.building.BuildingHammer;
import dev.raftocean.registry.ModContent;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Read-only palette: buttons select, never grant preview stacks or consume materials. */
public final class HammerMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>,MenuType<HammerMenu>> TYPE=RadioMenu.TYPES.register("hammer_palette",()->
            net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id,inventory,buffer)->new HammerMenu(id,inventory,buffer.readBoolean()?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND)));
    public static void initialize() { }
    private final Player owner;
    private final InteractionHand hand;
    private final ItemStack hammer;
    private final List<BuildingHammer.Entry> entries;
    private final ContainerData data;
    public HammerMenu(int id,Inventory inventory,InteractionHand hand){
        super(TYPE.get(),id);owner=inventory.player;this.hand=hand;hammer=owner.getItemInHand(hand);entries=BuildingHammer.catalog();
        data=owner.level().isClientSide()?new SimpleContainerData(1+2*entries.size()):new ContainerData(){
            public int getCount(){return 1+2*entries.size();}
            public void set(int index,int value) { }
            public int get(int index){
                if(index==0){var selected=BuildingHammer.selection(hammer);for(int i=0;i<entries.size();i++)if(selected.is(entries.get(i).item()))return i+1;return 0;}
                int cell=(index-1)%entries.size();var stack=entries.get(cell).stack();
                return index<=entries.size()?Math.min(4096,BuildingHammer.count(owner,stack)):dev.raftocean.progression.Research.allowed(owner,stack)?1:0;
            }
        };addDataSlots(data);
    }
    public List<BuildingHammer.Entry> entries(){return entries;}
    public int selected(){return data.get(0)-1;}
    public int count(int index){return data.get(1+index);}
    public boolean allowed(int index){return data.get(1+entries.size()+index)!=0;}
    @Override public boolean stillValid(Player player){return player==owner && player.isAlive() && !player.isSpectator()
            && dev.raftocean.config.RaftConfig.HAMMER_PALETTE.get() && player.getItemInHand(hand).is(ModContent.BUILDING_HAMMER.get())
            && (player.level().isClientSide() || player.getItemInHand(hand)==hammer);}
    @Override public boolean clickMenuButton(Player player,int button){
        if(!stillValid(player) || !getCarried().isEmpty() || button<0 || button>entries.size())return false;
        var choice=button==0?ItemStack.EMPTY:entries.get(button-1).stack();
        if(!choice.isEmpty() && !dev.raftocean.progression.Research.allowed(player,choice))return false;
        BuildingHammer.choose(hammer,choice);player.getInventory().setChanged();player.inventoryMenu.sendAllDataToRemote();broadcastChanges();return true;
    }
    @Override public void clicked(int index,int button,ContainerInput action,Player player) { }
    @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
}
