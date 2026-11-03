package dev.raftocean.building;

import dev.raftocean.api.RaftBuildables;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/** Persistent hammer choice; only ordinary crafted pieces in the backpack/hotbar are spent. */
public final class BuildingHammer extends Item {
    public static final String CHOICE="RaftBuildSelection";
    public record Entry(Item item,int category){public ItemStack stack(){return new ItemStack(item);}}
    public BuildingHammer(Properties properties){super(properties);}
    public static List<Entry> catalog(){
        Map<Item,Entry> items=new HashMap<>();items.put(ModContent.FOUNDATION.get(),new Entry(ModContent.FOUNDATION.get(),0));
        for(var kind:RaftPiece.Kind.values())if(kind!=RaftPiece.Kind.ADDON){
            int category=kind.furniture()?2:kind==RaftPiece.Kind.COLLECTION_NET?0:kind.station() || kind.navigation()?1:0;
            var item=ModContent.structuralItem(kind);items.put(item,new Entry(item,category));
        }
        for(var type:RaftBuildables.definitions().values())items.put(type.item().get(),new Entry(type.item().get(),3));
        var entries=items.values().stream().sorted(Comparator.comparing(e->BuiltInRegistries.ITEM.getKey(e.item()).toString())).toList();
        if(entries.size()>512)throw new IllegalStateException("Hammer palette exceeds its bounded menu capacity");return entries;
    }
    private static boolean buildable(ItemStack stack){
        if(stack.is(ModContent.FOUNDATION.get()) || RaftBuildables.forItem(stack).isPresent())return true;
        var kind=ModContent.structuralKind(stack);
        return kind!=null && kind!=RaftPiece.Kind.ADDON && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("raft_ocean");
    }
    public static ItemStack selection(ItemStack hammer){
        if(!hammer.is(ModContent.BUILDING_HAMMER.get()) || !dev.raftocean.config.RaftConfig.HAMMER_PALETTE.get())return ItemStack.EMPTY;
        var value=hammer.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(CHOICE,"");
        var id=Identifier.tryParse(value);if(id==null || !BuiltInRegistries.ITEM.containsKey(id))return ItemStack.EMPTY;
        var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(id));return buildable(stack)?stack:ItemStack.EMPTY;
    }
    public static void choose(ItemStack hammer,ItemStack selected){
        if(!hammer.is(ModContent.BUILDING_HAMMER.get()) || !selected.isEmpty() && !buildable(selected))throw new IllegalArgumentException("Invalid hammer choice");
        CustomData.update(DataComponents.CUSTOM_DATA,hammer,tag->{
            if(selected.isEmpty())tag.remove(CHOICE);else tag.putString(CHOICE,BuiltInRegistries.ITEM.getKey(selected.getItem()).toString());
        });
    }
    public static ItemStack supply(Player player,ItemStack selected){
        if(selected.isEmpty())return ItemStack.EMPTY;
        return player.getInventory().getNonEquipmentItems().stream().filter(s->!s.isEmpty() && ItemStack.isSameItemSameComponents(s,selected)).findFirst().orElse(ItemStack.EMPTY);
    }
    public static int count(Player player,ItemStack selected){
        if(selected.isEmpty())return 0;
        return player.getInventory().getNonEquipmentItems().stream().filter(s->ItemStack.isSameItemSameComponents(s,selected)).mapToInt(ItemStack::getCount).sum();
    }
    /** Resolve the selected shape for shared ghosts/rotation without moving any held inventory. */
    public static ItemStack preview(Player player,ItemStack held){
        return held.is(ModContent.BUILDING_HAMMER.get())?player.isShiftKeyDown()?ItemStack.EMPTY:selection(held):held;
    }
    public static boolean open(Player player,InteractionHand hand){
        if(!(player instanceof ServerPlayer server) || !player.isAlive() || player.isSpectator()
                || !dev.raftocean.config.RaftConfig.HAMMER_PALETTE.get() || !player.getItemInHand(hand).is(ModContent.BUILDING_HAMMER.get()))return false;
        server.openMenu(new SimpleMenuProvider((id,inventory,user)->new dev.raftocean.menu.HammerMenu(id,inventory,hand),Component.translatable("raft_ocean.hammer.title")),buffer->buffer.writeBoolean(hand==InteractionHand.OFF_HAND));
        return true;
    }
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<Component> builder,TooltipFlag flag){
        super.appendHoverText(stack,context,display,builder,flag);builder.accept(Component.translatable("raft_ocean.hammer.tip"));
        if(dev.raftocean.config.RaftConfig.SPEC.isLoaded()){
            var selected=selection(stack);if(!selected.isEmpty())builder.accept(Component.translatable("raft_ocean.hammer.selected",selected.getHoverName()));
        }
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(!dev.raftocean.config.RaftConfig.HAMMER_PALETTE.get())return InteractionResult.PASS;
        if(!level.isClientSide())return open(player,hand)?InteractionResult.SUCCESS_SERVER:InteractionResult.FAIL;
        return InteractionResult.SUCCESS;
    }
}
