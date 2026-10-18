package dev.raftocean.story;

import com.mojang.serialization.Codec;
import dev.raftocean.survival.SurvivalContent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Saves original note IDs, never arbitrary book text or client-supplied discoveries. */
public final class ExpeditionNotebook {
    public static final int NOTE_COUNT=28;
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> NOTES=SurvivalContent.ATTACHMENTS.register(
            "expedition_notes",()->AttachmentType.builder(()->0)
                    .serialize(Codec.intRange(0,(1<<NOTE_COUNT)-1).fieldOf("notes")).copyOnDeath().build());
    public static void initialize() { }
    public static String noteKey(int index){
        if(index<0 || index>=NOTE_COUNT)throw new IllegalArgumentException("Unknown field note");
        return "raft_ocean.story.field."+(index/4)+"."+(index%4);
    }
    public static int noteIndex(ItemStack stack){
        if(!stack.is(Items.WRITTEN_BOOK))return -1;
        var book=stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if(book==null || book.pages().size()!=2)return -1;
        if(!(book.pages().get(0).raw().getContents() instanceof TranslatableContents page)
                || !(book.pages().get(1).raw().getContents() instanceof TranslatableContents context))return -1;
        for(int i=0;i<NOTE_COUNT;i++)if(page.getKey().equals(noteKey(i)+".page")
                && context.getKey().equals(noteKey(i)+".context"))return i;
        return -1;
    }
    public static boolean record(Player player,ItemStack stack){
        if(player.level().isClientSide() || player.isSpectator())return false;
        int index=noteIndex(stack);if(index<0)return false;
        int old=player.getData(NOTES);if((old&1<<index)!=0)return false;
        player.setData(NOTES,old|1<<index);
        player.sendOverlayMessage(Component.translatable("raft_ocean.notebook.recorded",Component.translatable(noteKey(index)+".title")));
        return true;
    }
    public static void collect(Player player){
        if(player.level().isClientSide() || player.isSpectator())return;
        for(int i=0;i<player.getInventory().getContainerSize();i++)record(player,player.getInventory().getItem(i));
        record(player,player.containerMenu.getCarried());
    }
    public static void onTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){
        if(event.getEntity() instanceof ServerPlayer player && player.tickCount%20==0)collect(player);
    }
    public static void onPickup(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post event){
        record(event.getPlayer(),event.getOriginalStack());
    }
    public static void onUse(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem event){
        record(event.getEntity(),event.getItemStack());
    }
    public static void open(Player player){
        if(!(player instanceof ServerPlayer server) || player.isSpectator())return;
        collect(player);
        server.openMenu(new SimpleMenuProvider((id,inventory,p)->new dev.raftocean.menu.NotebookMenu(id,inventory),
                Component.translatable("raft_ocean.notebook.title")));
    }
    public static void registerCommand(net.neoforged.neoforge.event.RegisterCommandsEvent event){
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("raftnotebook").executes(c->{
            open(c.getSource().getPlayerOrException());return 1;
        }));
    }
    private ExpeditionNotebook() { }
}
