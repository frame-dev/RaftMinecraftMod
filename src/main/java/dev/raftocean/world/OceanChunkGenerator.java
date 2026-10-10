package dev.raftocean.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

/** Data-driven ocean layers with correct Overworld height and sea-level metadata. */
public final class OceanChunkGenerator extends FlatLevelSource {
    public static final int SEA_LEVEL = 63;
    public static final MapCodec<OceanChunkGenerator> CODEC = FlatLevelGeneratorSettings.CODEC
            .fieldOf("settings").xmap(OceanChunkGenerator::new, OceanChunkGenerator::settings);

    public OceanChunkGenerator(FlatLevelGeneratorSettings settings) { super(settings); }

    @Override
    public net.minecraft.util.random.WeightedList<net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData> getMobsAt(
            net.minecraft.world.level.Level level, net.minecraft.world.level.StructureManager structures,
            net.minecraft.world.entity.MobCategory category, net.minecraft.core.BlockPos pos) {
        var spawns=super.getMobsAt(level,structures,category,pos);
        if(category!=net.minecraft.world.entity.MobCategory.MONSTER){return spawns;}
        return net.minecraft.util.random.WeightedList.of(spawns.unwrap().stream()
                .filter(entry->entry.value().type()!=net.minecraft.world.entity.EntityTypes.DROWNED).toList());
    }

    @Override
    public net.minecraft.world.level.chunk.ChunkGeneratorStructureState createState(
            net.minecraft.core.HolderLookup<net.minecraft.world.level.levelgen.structure.StructureSet> sets,
            net.minecraft.world.level.levelgen.RandomState random,long seed) {
        // Also upgrades old Ocean World generator settings whose structure_overrides was empty.
        var originals=settings().structureOverrides().map(net.minecraft.core.HolderSet::stream).orElseGet(()->java.util.stream.Stream.empty());
        var islands=java.util.stream.Stream.of("islands","ordinary_islands","exploration_islands","dive_sites")
                .map(name->(net.minecraft.core.Holder<net.minecraft.world.level.levelgen.structure.StructureSet>)sets.getOrThrow(
                        net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE_SET,
                                net.minecraft.resources.Identifier.fromNamespaceAndPath("raft_ocean",name))));
        return net.minecraft.world.level.chunk.ChunkGeneratorStructureState.createForFlat(random,seed,getOrigin(random),getBiomeSource(),
                java.util.stream.Stream.concat(originals,islands).distinct());
    }
    @Override
    protected MapCodec<? extends ChunkGenerator> codec() { return CODEC; }

    @Override
    public int getSeaLevel() { return SEA_LEVEL; }

    @Override
    public int getMinY() { return -64; }
}
