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
        BlockPos center=new BlockPos(x,65,z);
        var size=context.structureTemplateManager().getOrCreate(template).getSize();
        if(size.getX()<1 || size.getZ()<1){return Optional.empty();}
        return Optional.of(new GenerationStub(center,builder->builder.addPiece(
                new IslandPiece(context.structureTemplateManager(),template,new BlockPos(x-size.getX()/2,49,z-size.getZ()/2)))));
    }
    @Override public StructureType<?> type() { return IslandContent.ISLAND.get(); }
}
