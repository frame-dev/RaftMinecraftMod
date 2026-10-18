package dev.raftocean.story;

import com.mojang.authlib.GameProfile;
import dev.raftocean.menu.NotebookMenu;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Actual loot, item conservation, short-safe menus, save/death and personal archive boundaries. */
public final class NotebookCheck {
    private static final class Reader extends FakePlayer {
        private NotebookMenu opened;
        Reader(ServerLevel level){super(level,new GameProfile(UUID.randomUUID(),"NotebookReader"));}
        @Override public OptionalInt openMenu(net.minecraft.world.MenuProvider provider,
                java.util.function.Consumer<net.minecraft.network.RegistryFriendlyByteBuf> writer){
            var created=provider.createMenu(3,getInventory(),this);
            if(!(created instanceof NotebookMenu notebook))throw new IllegalStateException("Notebook provider opened another menu");
            opened=notebook;return OptionalInt.of(3);
        }
    }
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    private static FakePlayer player(ServerLevel level){return new FakePlayer(level,new GameProfile(UUID.randomUUID(),"NotebookCheck"));}
    public static int run(ServerLevel level){
        checks=0;var player=player(level);var stranger=player(level);var menu=new NotebookMenu(1,player.getInventory());
        require(menu.notes()==0 && menu.journey()==0 && menu.branches()==0,"New archive starts empty");
        require(!ExpeditionNotebook.record(player,new ItemStack(Items.BOOK)) && !ExpeditionNotebook.record(player,new ItemStack(Items.WRITTEN_BOOK)),"Ordinary books cannot create discoveries");
        var books=new ArrayList<ItemStack>();
        for(int i=0;i<ExpeditionNotebook.NOTE_COUNT;i++){
            var key=ResourceKey.create(Registries.LOOT_TABLE,Identifier.fromNamespaceAndPath("raft_ocean",
                    "chests/field_notes/"+ReceiverItem.DESTINATIONS[i/4]+"_"+(i%4)));
            var params=new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN,Vec3.ZERO).create(LootContextParamSets.CHEST);
            var loot=level.getServer().reloadableRegistries().getLootTable(key).getRandomItems(params);
            final int expected=i;
            var book=loot.stream().filter(stack->ExpeditionNotebook.noteIndex(stack)==expected).findFirst().orElseThrow(()->new IllegalStateException("Native note loot missing: "+key));
            books.add(book.copy());var before=book.copy();
            require(ExpeditionNotebook.record(player,book),"Every native field note archives its stable ID: "+i);
            require(ItemStack.matches(before,book),"Archiving preserves complete book and components");
            require(!ExpeditionNotebook.record(player,book),"Duplicate notes do not alter archive");
            require(menu.notes()==(1<<(i+1))-1,"Both short-safe data halves reconstruct every note bit");
        }
        require(stranger.getData(ExpeditionNotebook.NOTES)==0,"Nearby players do not receive unread personal notes");
        player.getInventory().clearContent();
        require(menu.notes()==(1<<28)-1,"Losing physical books cannot erase notes");
        var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());player.saveWithoutId(saved);
        var restored=player(level);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
        require(restored.getData(ExpeditionNotebook.NOTES)==(1<<28)-1,"All archive bits survive actual player save/load");
        var respawn=player(level);respawn.copyAttachmentsFrom(player,true);
        require(respawn.getData(ExpeditionNotebook.NOTES)==(1<<28)-1,"Actual death attachment copy retains notes");
        var old=player(level);old.setData(IslandContent.JOURNEY,5);old.setData(Exploration.DISCOVERIES,5);
        var oldMenu=new NotebookMenu(2,old.getInventory());
        require(oldMenu.journey()==5 && oldMenu.branches()==5 && oldMenu.notes()==0,"Existing journey and side logs are readable without inventing collected field notes");
        old.getInventory().setItem(17,books.get(27).copy());ExpeditionNotebook.collect(old);
        require(oldMenu.notes()==1<<27 && old.getInventory().getItem(17).getCount()==1,"Inventory recovery archives old books without consuming them");
        require(old.getData(IslandContent.JOURNEY)==5 && old.getData(Exploration.DISCOVERIES)==5,"Collecting optional notes never advances journey or side routes");
        old.containerMenu.setCarried(books.get(0).copy());ExpeditionNotebook.collect(old);
        require(oldMenu.notes()==((1<<27)|1) && old.containerMenu.getCarried().getCount()==1,"Carried legacy books also archive safely");
        require(menu.slots.isEmpty() && menu.quickMoveStack(player,0).isEmpty() && !menu.clickMenuButton(player,0),"Notebook cannot act as a machine or item storage");
        player.containerMenu.setCarried(new ItemStack(Items.DIAMOND,2));
        menu.clicked(-999,0,ContainerInput.PICKUP,player);
        require(player.containerMenu.getCarried().getCount()==2,"Read-only notebook rejects outside-click item drops");
        require(menu.stillValid(player) && !menu.stillValid(stranger),"Only the owning player can use this archive menu");
        var reader=new Reader(level);
        var source=level.getServer().createCommandSourceStack().withEntity(reader)
                .withPermission(net.minecraft.server.permissions.PermissionSet.NO_PERMISSIONS);
        try{
            require(level.getServer().getCommands().getDispatcher().execute("raftnotebook",source)==1
                    && reader.opened!=null,"Registered notebook command opens its native menu without operator permissions");
        }catch(com.mojang.brigadier.exceptions.CommandSyntaxException error){throw new IllegalStateException("Notebook command failed",error);}
        reader.opened=null;reader.setShiftKeyDown(true);
        IslandContent.STARFALL_LOG.get().use(level,reader,net.minecraft.world.InteractionHand.MAIN_HAND);
        require(reader.opened!=null && reader.getData(IslandContent.JOURNEY)==0,"Sneak journal opens notebook without reading an undiscovered journal");
        reader.opened=null;Exploration.ORCHARD.get().use(level,reader,net.minecraft.world.InteractionHand.MAIN_HAND);
        require(reader.opened!=null && reader.getData(Exploration.DISCOVERIES)==0,"Sneak optional journal opens notebook without acquiring its log");
        reader.setShiftKeyDown(false);IslandContent.RADIO_LOG.get().use(level,reader,net.minecraft.world.InteractionHand.MAIN_HAND);
        require(reader.getData(IslandContent.JOURNEY)==1,"Normal journal use still advances the journey");
        var groundReader=player(level);var entity=new net.minecraft.world.entity.item.ItemEntity(level,0,100,0,books.get(1).copy());
        ExpeditionNotebook.onPickup(new net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post(groundReader,entity,books.get(1)));
        require(groundReader.getData(ExpeditionNotebook.NOTES)==2 && entity.getItem().getCount()==1,"Native successful pickup event archives immediately without mutating entity");
        stranger.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
        require(!ExpeditionNotebook.record(stranger,books.get(0)) && stranger.getData(ExpeditionNotebook.NOTES)==0,"Spectator cannot acquire notes");
        player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
        require(!menu.stillValid(player),"Changing to spectator closes notebook");
        reader.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);reader.opened=null;ExpeditionNotebook.open(reader);
        require(reader.opened==null,"Spectator cannot open archive via command or receiver");
        return checks;
    }
    private NotebookCheck() { }
}
