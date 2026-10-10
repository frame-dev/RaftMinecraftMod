package dev.raftocean.story;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import dev.raftocean.world.OceanChunkGenerator;

/** Native chunk-bounded template structures; generation never writes into existing chunks. */
public final class IslandStructure extends Structure {
    public static final MapCodec<IslandStructure> CODEC = RecordCodecBuilder.mapCodec(i->i.group(
            settingsCodec(i),Identifier.CODEC.fieldOf("template").forGetter(s->s.template)).apply(i,IslandStructure::new));
    private final Identifier template;
    public IslandStructure(StructureSettings settings,Identifier template) { super(settings);this.template=template; }
    @Override protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if(!(context.chunkGenerator() instanceof OceanChunkGenerator)){return Optional.empty();}
        int x=context.chunkPos().getMinBlockX()+8,z=context.chunkPos().getMinBlockZ()+8;
        if((long)x*x+(long)z*z<160L*160){return Optional.empty();}
        // New flooded sites must never carve through an island from another placement set.
        String name=template.getPath().substring(template.getPath().lastIndexOf('/')+1);
        int route=java.util.Arrays.asList(Exploration.ROUTES).indexOf(name);
        if(route>=0) {
            var sets=context.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET);
            for(String set:route<3?java.util.List.of("islands","ordinary_islands"):
                    java.util.List.of("islands","ordinary_islands","exploration_islands")) {
                var placement=sets.getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE_SET,
                        Identifier.fromNamespaceAndPath("raft_ocean",set))).value().placement();
                if(placement instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement spread) {
                    int cx=context.chunkPos().x(),cz=context.chunkPos().z();
                    for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++) {
                        var candidate=spread.getPotentialStructureChunk(context.seed(),cx+dx,cz+dz);
                        if(Math.abs(candidate.x()-cx)<=4 && Math.abs(candidate.z()-cz)<=4)return Optional.empty();
                    }
                }
            }
        }
        BlockPos center=new BlockPos(x,65,z);
        var size=context.structureTemplateManager().getOrCreate(template).getSize();
        if(size.getX()<1 || size.getZ()<1){return Optional.empty();}
        return Optional.of(new GenerationStub(center,builder->builder.addPiece(
                new IslandPiece(context.structureTemplateManager(),template,new BlockPos(x-size.getX()/2,49,z-size.getZ()/2)))));
    }
    @Override public StructureType<?> type() { return IslandContent.ISLAND.get(); }
}
