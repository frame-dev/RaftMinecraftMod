package dev.raftocean.story;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Actual native generation and chest loot, including branches after the main story. */
public final class ExplorationCheck {
    private static int checks;
    private static void require(boolean value,String message) {if(!value)throw new IllegalStateException(message);checks++;}
    private static Identifier id(String path) {return Identifier.fromNamespaceAndPath("raft_ocean",path);}
    public static int run(ServerLevel level) {
        checks=0;
        var registry=level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var logs=List.of(Exploration.SURVEY.get(),Exploration.REFUGE.get(),Exploration.ORCHARD.get());
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"ExpeditionCheck"));
        player.setData(IslandContent.JOURNEY,ReceiverItem.DESTINATIONS.length);
        for(String set:List.of("dive_sites","exploration_islands"))require(level.getChunkSource().getGeneratorState()
                .possibleStructureSets().stream().anyMatch(h->h.is(ResourceKey.create(Registries.STRUCTURE_SET,id(set)))),"Old ocean settings include "+set);
        for(int i=0;i<7;i++) {
            String name=Exploration.ROUTES[i];int size=i<3?97:65;
            var template=level.getStructureTemplateManager().getOrCreate(id("islands/"+name));
            require(template.getSize().equals(new Vec3i(size,i<3?42:14,size)),"Native exploration template dimensions: "+name);
            var settings=new StructurePlaceSettings();
            var chest=template.filterBlocks(BlockPos.ZERO,settings,Blocks.CHEST).getFirst();
            require(chest.nbt()!=null,"Exploration loot reference exists: "+name);
            if(i>=3)require(!template.filterBlocks(BlockPos.ZERO,settings,Blocks.WATER).isEmpty(),"Underwater sites contain flooded entrances: "+name);
            else require(!template.filterBlocks(BlockPos.ZERO,settings,Blocks.KELP).isEmpty(),"New island reef has kelp: "+name);
            var holder=registry.getOrThrow(ResourceKey.create(Registries.STRUCTURE,id(name)));
            var located=level.findNearestMapStructure(HolderSet.direct(holder),new BlockPos(0,65,0),12,false);
            require(located!=null,"Native locator resolves optional branch: "+name);
            var center=ReceiverItem.islandCenter(located);
            BlockPos origin=new BlockPos(center.getX()-size/2,49,center.getZ()-size/2),pos=origin.offset(chest.pos());
            // Template spans several chunks; force the whole local footprint through native generation.
            for(int x=(origin.getX()>>4);x<=((origin.getX()+size-1)>>4);x++)
                for(int z=(origin.getZ()>>4);z<=((origin.getZ()+size-1)>>4);z++)level.getChunk(x,z);
            require(level.getBlockEntity(pos) instanceof ChestBlockEntity,"Generated accessible expedition chest: "+name);
            var container=(ChestBlockEntity)level.getBlockEntity(pos);boolean found=false;
            for(int slot=0;slot<container.getContainerSize();slot++)if(container.getItem(slot).is(logs.get(i%3)))found=true;
            require(found,"Native loot grants expedition journal: "+name);
            require(i<3 || pos.getY()<63 && level.getFluidState(pos.above()).is(net.minecraft.tags.FluidTags.WATER),"Dive treasure is submerged: "+name);
            player.setData(Exploration.CHANNEL,i);player.setPos(0,65,0);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(IslandContent.LONG_RECEIVER.get()));
            require(IslandContent.LONG_RECEIVER.get().use(level,player,InteractionHand.MAIN_HAND).consumesAction(),"Optional receiver remains usable after final chapter: "+name);
            logs.get(i%3).use(level,player,InteractionHand.MAIN_HAND);
            require(player.getData(IslandContent.JOURNEY)==ReceiverItem.DESTINATIONS.length,"Branch journal leaves main story open-ended: "+name);
        }
        require(player.getData(Exploration.DISCOVERIES)==7,"All three independent journal discoveries persist");
        player.setData(Exploration.CHANNEL,Exploration.ROUTES.length-1);player.setShiftKeyDown(true);
        IslandContent.LONG_RECEIVER.get().use(level,player,InteractionHand.MAIN_HAND);
        require(player.getData(Exploration.CHANNEL)==-1,"Receiver cycles back to main story");
        player.discard();return checks;
    }
    private ExplorationCheck() { }
}
