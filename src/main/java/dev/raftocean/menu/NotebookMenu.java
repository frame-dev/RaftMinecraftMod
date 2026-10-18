package dev.raftocean.menu;

import dev.raftocean.story.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Read-only, personal archive. Split masks fit native signed-short menu synchronization. */
public final class NotebookMenu extends AbstractContainerMenu {
    public static final DeferredHolder<MenuType<?>,MenuType<NotebookMenu>> TYPE=RadioMenu.TYPES.register("expedition_notebook",()->
            new MenuType<>(NotebookMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    private final Player owner;
    private final ContainerData data;
    public static void initialize() { }
    public NotebookMenu(int id,Inventory inventory){
        super(TYPE.get(),id);owner=inventory.player;
        data=owner.level().isClientSide()?new SimpleContainerData(4):new ContainerData(){
            public int getCount(){return 4;}
            public void set(int index,int value) { }
            public int get(int index){return switch(index){
                case 0->owner.getData(ExpeditionNotebook.NOTES)&0x3fff;
                case 1->owner.getData(ExpeditionNotebook.NOTES)>>>14;
                case 2->Math.clamp(owner.getData(IslandContent.JOURNEY),0,ReceiverItem.DESTINATIONS.length);
                case 3->owner.getData(Exploration.DISCOVERIES);
                default->0;
            };}
        };addDataSlots(data);
    }
    public int notes(){return data.get(0)|(data.get(1)<<14);}
    public int journey(){return data.get(2);}
    public int branches(){return data.get(3);}
    @Override public boolean stillValid(Player player){return player==owner && player.isAlive() && !player.isSpectator();}
    @Override public void clicked(int slot,int button,ContainerInput action,Player player) { }
    @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
}
