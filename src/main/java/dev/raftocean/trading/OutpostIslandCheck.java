package dev.raftocean.trading;

import com.google.gson.JsonParser;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

/** Natural generation plus all marked island routes validated against native templates. */
public final class OutpostIslandCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    private static Identifier id(String path){return Identifier.fromNamespaceAndPath("raft_ocean",path);}
    public static int run(ServerLevel level) {
        checks=0;
        var structures=level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        require(level.getChunkSource().getGeneratorState().possibleStructureSets().stream().anyMatch(h->h.is(ResourceKey.create(Registries.STRUCTURE_SET,id("trading_outposts")))),"Existing Ocean settings gain trading outposts");
        for(String name:List.of("merchant_harbor","lantern_market")) {
            var template=level.getStructureTemplateManager().getOrCreate(id("islands/"+name));
            require(template.getSize().equals(new Vec3i(129,42,129)),"Trading island dimensions: "+name);
            var counter=template.filterBlocks(BlockPos.ZERO,new StructurePlaceSettings(),OutpostTrading.POST.get()).getFirst();
            var target=level.findNearestMapStructure(HolderSet.direct(structures.getOrThrow(ResourceKey.create(Registries.STRUCTURE,id(name)))),new BlockPos(0,65,0),16,false);
            require(target!=null,"Native locator finds optional trading island: "+name);
            var center=dev.raftocean.story.ReceiverItem.islandCenter(target);var origin=new BlockPos(center.getX()-64,49,center.getZ()-64);
            for(int x=(origin.getX()>>4);x<=((origin.getX()+128)>>4);x++)for(int z=(origin.getZ()>>4);z<=((origin.getZ()+128)>>4);z++)level.getChunk(x,z);
            require(level.getBlockState(origin.offset(counter.pos())).is(OutpostTrading.POST.get()),"Natural generation places working trade counter: "+name);
            for(var lamp:template.filterBlocks(BlockPos.ZERO,new StructurePlaceSettings(),Blocks.LANTERN)) {
                var pos=origin.offset(lamp.pos());require(level.getBlockState(pos).is(Blocks.LANTERN) && level.getBlockState(pos).canSurvive(level,pos),"Outpost lantern remains attached: "+name);
            }
        }
        var resource=level.getServer().getResourceManager().getResource(id("island_routes.json")).orElseThrow();
        try(var reader=resource.openAsReader()) {
            var routes=JsonParser.parseReader(reader).getAsJsonObject();require(routes.size()>=18,"Most existing islands receive marked landing routes");
            for(var entry:routes.entrySet()) {
                var template=level.getStructureTemplateManager().getOrCreate(id("islands/"+entry.getKey()));
                var lookup=new java.util.HashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
                for(var block:List.of(Blocks.DIRT_PATH,Blocks.GRAVEL,Blocks.OAK_PLANKS,Blocks.STONE_BRICK_STAIRS))
                    for(var cell:template.filterBlocks(BlockPos.ZERO,new StructurePlaceSettings(),block))lookup.put(cell.pos(),cell.state());
                BlockPos previous=null;
                for(var element:entry.getValue().getAsJsonArray()) {
                    var cell=element.getAsJsonArray();var pos=new BlockPos(cell.get(0).getAsInt(),cell.get(1).getAsInt(),cell.get(2).getAsInt());
                    require(lookup.containsKey(pos),"Authored route uses a native walkable surface: "
                            +entry.getKey()+" at "+pos);
                    if(previous!=null)require(Math.abs(pos.getY()-previous.getY())<=1 && Math.abs(pos.getX()-previous.getX())+Math.abs(pos.getZ()-previous.getZ())==1,"Route has no gaps or two-block steps: "+entry.getKey());
                    previous=pos;
                }
            }
        } catch(java.io.IOException error){throw new IllegalStateException(error);}
        return checks;
    }
    private OutpostIslandCheck() { }
}
