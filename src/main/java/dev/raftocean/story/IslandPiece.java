package dev.raftocean.story;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

public final class IslandPiece extends TemplateStructurePiece {
    public IslandPiece(StructureTemplateManager manager,Identifier template,BlockPos pos) {
        super(IslandContent.PIECE.get(),0,manager,template,template.toString(),new StructurePlaceSettings(),pos);
    }
    public IslandPiece(StructureTemplateManager manager,CompoundTag tag) {
        super(IslandContent.PIECE.get(),tag,manager,id->new StructurePlaceSettings());
    }
    @Override protected void handleDataMarker(String marker,BlockPos pos,ServerLevelAccessor level,RandomSource random,BoundingBox bounds) {
        if(bounds.isInside(pos)){level.setBlock(pos,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);}
    }
}
