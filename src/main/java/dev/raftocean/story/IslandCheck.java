package dev.raftocean.story;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.registry.ModContent;
import dev.raftocean.entity.RaftEntity;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import java.util.*;

/** Native generation, template loot, progression persistence, puzzle and placement-cap checks. */
public final class IslandCheck {
    private static int checks;
    private static void check(boolean value,String message) { if(!value){throw new IllegalStateException(message);}checks++; }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("raft_ocean",path); }
    private IslandCheck() { }
    public static int run(ServerLevel level) {
        checks=0;
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"IslandCheck"));
        var stages=level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var logs=List.of(IslandContent.RADIO_LOG.get(),IslandContent.WATCH_LOG.get(),IslandContent.GARDEN_LOG.get(),
                IslandContent.VAULT_LOG.get(),IslandContent.BEACON_LOG.get(),IslandContent.HAVEN_LOG.get(),IslandContent.STARFALL_LOG.get());
        int limit=RaftConfig.FOUNDATION_LIMIT.get();
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.setPos(.5,62.8,.5);
        try {
            check(level.getChunkSource().getGeneratorState().possibleStructureSets().stream()
                    .anyMatch(holder->holder.is(ResourceKey.create(Registries.STRUCTURE_SET,id("islands")))),"Old ocean settings gain the native island structure set");
            for(String name:List.of("drift_atoll","radio_tower","glasswatch","garden_relay","echo_vault")) {
                var template=level.getStructureTemplateManager().getOrCreate(id("islands/"+name));
                int width=name.equals("drift_atoll")?65:name.equals("radio_tower")?113:145;
                check(template.getSize().equals(new Vec3i(width,name.equals("drift_atoll")?29:48,width)),"Island template loads with native NBT schema: "+name);
                check(!template.filterBlocks(BlockPos.ZERO,new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings(),Blocks.KELP).isEmpty(),
                        "Story shoreline includes harvestable underwater kelp: "+name);
            }
            check(level.getChunkSource().getGeneratorState().possibleStructureSets().stream()
                    .anyMatch(holder->holder.is(ResourceKey.create(Registries.STRUCTURE_SET,id("ordinary_islands")))),
                    "Existing Ocean settings also gain ordinary islands without replacing story placement");
            String[] ordinary={"shell_sandbar","tidal_grove","flower_meadow","twin_cove","stone_rise","broadleaf_island","crescent_reef","split_grove","longshore","mossy_arch"};
            int[] sizes={33,49,65,97,129,161,97,129,65,161};
            var placement=new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings();
            var ordinarySet=level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)
                    .getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,id("ordinary_islands"))).value();
            check(ordinarySet.structures().size()==10,"Native ordinary set loads ten weighted variants");
            for(int variant=0;variant<ordinary.length;variant++){
                String name=ordinary[variant];int size=sizes[variant];
                var template=level.getStructureTemplateManager().getOrCreate(id("islands/"+name));
                check(template.getSize().equals(new Vec3i(size,42,size)),"Variable-size native island template: "+name);
                var kelp=template.filterBlocks(BlockPos.ZERO,placement,Blocks.KELP,false);
                check(kelp.size()>=6,"Ordinary island has multiple kelp stands: "+name);
                for(var resource:List.of(Blocks.CLAY,Blocks.GRAVEL,Blocks.COPPER_ORE,Blocks.IRON_ORE,Blocks.COAL_ORE)){
                    check(!template.filterBlocks(BlockPos.ZERO,placement,resource,false).isEmpty(),"Native island includes underwater gathering material: "+name+" "+resource);
                }
                check(template.filterBlocks(BlockPos.ZERO,placement,Blocks.CHEST).isEmpty()
                        && template.filterBlocks(BlockPos.ZERO,placement,IslandContent.HATCH.get()).isEmpty(),
                        "Ordinary island contains no story chest or puzzle: "+name);
                var holder=stages.getOrThrow(ResourceKey.create(Registries.STRUCTURE,id(name)));
                BlockPos located=level.findNearestMapStructure(HolderSet.direct(holder),new BlockPos(0,65,0),10,false);
                check(located!=null,"Native locator finds ordinary island: "+name);
                BlockPos center=ReceiverItem.islandCenter(located);
                level.getChunk(center.getX()>>4,center.getZ()>>4);
                BlockPos kelpPos=new BlockPos(center.getX()-size/2,49,center.getZ()-size/2).offset(kelp.getFirst().pos());
                level.getChunk(kelpPos.getX()>>4,kelpPos.getZ()>>4);
                var kelpState=level.getBlockState(kelpPos);
                check(kelpState.is(Blocks.KELP) && kelpState.canSurvive(level,kelpPos)
                        && level.getFluidState(kelpPos).is(net.minecraft.tags.FluidTags.WATER),"Generated kelp is supported underwater and survives native placement: "+name);
                check(Block.getDrops(kelpState,level,kelpPos,null).stream().anyMatch(stack->stack.is(Items.KELP)),"Native kelp harvest provides planter input: "+name);
                check(level.getBlockState(new BlockPos(center.getX(),49,center.getZ())).is(Blocks.STONE),
                        "Variable-size island is centered correctly and joins the seabed: "+name);
                // Check a template surface outside the center chunk, including the large island footprint.
                var sand=template.filterBlocks(BlockPos.ZERO,placement,Blocks.SAND,false);
                var sample=sand.stream().filter(info->info.pos().getY()>=14)
                        .max(Comparator.comparingInt(info->Math.abs(info.pos().getX()-size/2)+Math.abs(info.pos().getZ()-size/2))).orElseThrow();
                BlockPos shore=new BlockPos(center.getX()-size/2,49,center.getZ()-size/2).offset(sample.pos());
                level.getChunk(shore.getX()>>4,shore.getZ()>>4);
                check(level.getBlockState(shore).is(Blocks.SAND),"Island shoreline places across its variable chunk footprint: "+name);
                if(variant>0){
                    check(!template.filterBlocks(BlockPos.ZERO,placement,Blocks.OAK_LOG).isEmpty()
                            || !template.filterBlocks(BlockPos.ZERO,placement,Blocks.BIRCH_LOG).isEmpty()
                            || !template.filterBlocks(BlockPos.ZERO,placement,Blocks.SPRUCE_LOG).isEmpty(),
                            "Wooded ordinary island contains harvestable trees: "+name);
                }
            }
            var storySet=level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)
                    .getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET,id("islands"))).value();
            check(storySet.structures().size()==8,"Story placement includes seven destinations and the original atoll");
            for(int stage=0;stage<ReceiverItem.DESTINATIONS.length;stage++) {
                String name=ReceiverItem.DESTINATIONS[stage];
                var template=level.getStructureTemplateManager().getOrCreate(id("islands/"+name));
                int size=stage==0?113:stage<4?145:stage==4?161:193;
                check(template.getSize().equals(new Vec3i(size,48,size)),"Expanded story template dimensions: "+name);
                Block landmark=switch(stage){
                    case 0 -> Blocks.OAK_LOG;
                    case 1 -> Blocks.DARK_OAK_LOG;
                    case 2 -> Blocks.WATER_CAULDRON;
                    case 3 -> Blocks.CARTOGRAPHY_TABLE;
                    case 4 -> Blocks.COPPER_BLOCK.weathering().oxidized();
                    case 5 -> Blocks.ANVIL;
                    default -> Blocks.CHISELED_STONE_BRICKS;
                };
                check(!template.filterBlocks(BlockPos.ZERO,placement,landmark,false).isEmpty(),
                        "Authored story landmark detail exists: "+name);
                check(!template.filterBlocks(BlockPos.ZERO,placement,Blocks.LANTERN,false).isEmpty(),
                        "Story location has authored lighting: "+name);
                if(stage>=4){
                    check(template.filterBlocks(BlockPos.ZERO,placement,Blocks.KELP,false).size()>=6,"Story shallows contain kelp: "+name);
                    for(var resource:List.of(Blocks.CLAY,Blocks.GRAVEL,Blocks.COPPER_ORE,Blocks.IRON_ORE,Blocks.COAL_ORE)){
                        check(!template.filterBlocks(BlockPos.ZERO,placement,resource,false).isEmpty(),"Story shallows gathering resource: "+name+" "+resource);
                    }
                }
                var holder=stages.getOrThrow(ResourceKey.create(Registries.STRUCTURE,id(name)));
                BlockPos target=level.findNearestMapStructure(HolderSet.direct(holder),new BlockPos(0,65,0),6,false);
                check(target!=null,"Native structure locator finds "+name);
                target=ReceiverItem.islandCenter(target);
                int cx=target.getX()>>4,cz=target.getZ()>>4;
                int radius=(size+15)/32;
                for(int x=cx-radius;x<=cx+radius;x++){for(int z=cz-radius;z<=cz+radius;z++){level.getChunk(x,z);}}
                checks+=StoryExplorationCheck.run(level,name,target,size);
                check(level.getBlockState(new BlockPos(target.getX(),49,target.getZ())).is(Blocks.STONE),"Island terrain connects to the seabed: "+name);
                int chestY=stage<=1?73:stage==4?78:66,zOffset=stage<=1||stage==4?0:stage==2||stage==5?4:stage==3?7:8;
                BlockPos chestPos=new BlockPos(target.getX(),chestY,target.getZ()+zOffset);
                check(level.getBlockEntity(chestPos) instanceof ChestBlockEntity,"Native chunk generation places story chest: "+name);
                var chest=(ChestBlockEntity)level.getBlockEntity(chestPos);
                boolean found=false;for(int slot=0;slot<chest.getContainerSize();slot++){if(chest.getItem(slot).is(logs.get(stage))){found=true;}}
                check(found,"Story chest native loot contains original journal: "+name);
                var detail=template.filterBlocks(BlockPos.ZERO,placement,landmark,false).getFirst();
                BlockPos detailPos=new BlockPos(target.getX()-size/2,49,target.getZ()-size/2).offset(detail.pos());
                check(level.getBlockState(detailPos).is(landmark),"New decorative landmark survives actual chunk generation: "+name);
                for(var lamp:template.filterBlocks(BlockPos.ZERO,placement,Blocks.LANTERN,false)){
                    BlockPos lampPos=new BlockPos(target.getX()-size/2,49,target.getZ()-size/2).offset(lamp.pos());
                    check(level.getBlockState(lampPos).is(Blocks.LANTERN) && level.getBlockState(lampPos).canSurvive(level,lampPos),
                            "Story lantern stays supported: "+name);
                }
                if(stage==2||stage==5) {
                    boolean key=false;for(int slot=0;slot<chest.getContainerSize();slot++){if(chest.getItem(slot).is(IslandContent.KEY.get())){key=true;}}
                    check(key,"Preceding chapter loot grants the bunker key: "+name);
                }
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(logs.get(stage)));
                logs.get(stage).use(level,player,InteractionHand.MAIN_HAND);
                check(player.getData(IslandContent.JOURNEY)==stage+1 && player.getMainHandItem().getCount()==1,"Reading journal advances persistent journey without consuming it");
                if(stage==3) { testSeal(level,player,new BlockPos(target.getX(),66,target.getZ()-3)); }
                if(stage==6) { testSeal(level,player,new BlockPos(target.getX(),66,target.getZ()-2)); }
                if(stage==3){
                    var oldSave=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());player.saveWithoutId(oldSave);
                    var oldPlayer=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"OldJourneyReload"));
                    oldPlayer.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),oldSave.buildResult()));
                    check(oldPlayer.getData(IslandContent.JOURNEY)==4,"Existing completed four-stage saves continue at Ember Beacon");
                }
                if(stage>=4){
                    var kelp=template.filterBlocks(BlockPos.ZERO,placement,Blocks.KELP,false).getFirst();
                    BlockPos kelpPos=new BlockPos(target.getX()-size/2,49,target.getZ()-size/2).offset(kelp.pos());
                    check(level.getBlockState(kelpPos).is(Blocks.KELP) && level.getBlockState(kelpPos).canSurvive(level,kelpPos),
                            "Actual story island places supported kelp across chunk bounds: "+name);
                    var sand=template.filterBlocks(BlockPos.ZERO,placement,Blocks.SAND,false).stream()
                            .filter(info->info.pos().getY()>=13).max(Comparator.comparingInt(info->Math.abs(info.pos().getX()-size/2)+Math.abs(info.pos().getZ()-size/2))).orElseThrow();
                    BlockPos shore=new BlockPos(target.getX()-size/2,49,target.getZ()-size/2).offset(sand.pos());
                    check(level.getBlockState(shore).is(Blocks.SAND),"New story shoreline places beyond its origin chunk: "+name);
                    for(var ladder:template.filterBlocks(BlockPos.ZERO,placement,Blocks.LADDER,false)){
                        BlockPos ladderPos=new BlockPos(target.getX()-size/2,49,target.getZ()-size/2).offset(ladder.pos());
                        check(level.getBlockState(ladderPos).is(Blocks.LADDER) && level.getBlockState(ladderPos).canSurvive(level,ladderPos),"Beacon ladder remains attached after native generation");
                    }
                }
            }
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());player.saveWithoutId(saved);
            var restored=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"JourneyReload"));
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            check(restored.getData(IslandContent.JOURNEY)==ReceiverItem.DESTINATIONS.length,"All seven story stages survive native player serialization");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(IslandContent.WATCH_LOG.get()));
            IslandContent.WATCH_LOG.get().use(level,player,InteractionHand.MAIN_HAND);
            check(player.getData(IslandContent.JOURNEY)==ReceiverItem.DESTINATIONS.length,"Reading an earlier journal cannot regress the extended journey");
            var input=CraftingInput.of(2,1,List.of(new ItemStack(IslandContent.RECEIVER.get()),new ItemStack(IslandContent.ANTENNA.get())));
            check(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow().value().assemble(input).is(IslandContent.LONG_RECEIVER.get()),"Native antenna upgrade recipe assembles");
            check(level.addFreshEntity(raft),"Foundation-cap fixture registered");
            player.setPos(raft.position().add(0,.626,0));player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.FOUNDATION.get(),2));
            Vec3 aim=raft.toWorld(new Vec3(1.45,.24,0)).subtract(player.getEyePosition());
            player.setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));player.setXRot((float)-Math.toDegrees(Math.atan2(aim.y,aim.horizontalDistance())));
            RaftConfig.FOUNDATION_LIMIT.set(9);
            check(!raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && raft.getBlocks().size()==9
                    && player.getMainHandItem().getCount()==2,"Configured foundation cap refuses placement before consumption");
            RaftConfig.FOUNDATION_LIMIT.set(10);
            check(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && raft.getBlocks().size()==10
                    && player.getMainHandItem().getCount()==1,"Raising cap permits actual native foundation placement");
            RaftConfig.FOUNDATION_LIMIT.set(9);
            saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());raft.saveWithoutId(saved);
            var copy=new RaftEntity(ModContent.RAFT.get(),level);copy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            check(copy.getBlocks().size()==10,"Lowering cap preserves larger saved rafts");copy.discard();
        } finally {
            raft.discard();player.getInventory().clearContent();RaftConfig.FOUNDATION_LIMIT.set(limit);
        }
        return checks;
    }
    private static void testSeal(ServerLevel level,FakePlayer player,BlockPos base) {
        // Restore the gate after the test so repeated isolated runs exercise the same transaction.
        var before=level.getBlockState(base);var above=level.getBlockState(base.above());
        var switches=new ArrayList<net.minecraft.world.level.block.state.BlockState>();
        for(int i=0;i<3;i++){switches.add(level.getBlockState(base.offset((i-1)*2,0,-2)));}
        try {
            level.setBlock(base,IslandContent.HATCH.get().defaultBlockState(),3);level.setBlock(base.above(),IslandContent.HATCH.get().defaultBlockState(),3);
            var key=new ItemStack(IslandContent.KEY.get(),2);
            for(int i=0;i<3;i++){level.setBlock(base.offset((i-1)*2,0,-2),switches.get(i).setValue(LeverBlock.POWERED,false),3);}
            check(!SealedHatchBlock.unlock(level,base,player,key).consumesAction() && key.getCount()==2,"Wrong lever pattern preserves key and seal");
            for(int i=0;i<3;i++){level.setBlock(base.offset((i-1)*2,0,-2),switches.get(i).setValue(LeverBlock.POWERED,i!=1),3);}
            check(SealedHatchBlock.solved(level,base),"Original ON/OFF/ON puzzle resolves");
            check(!SealedHatchBlock.unlock(level,base,player,ItemStack.EMPTY).consumesAction(),"Correct pattern still requires the correct key");
            check(SealedHatchBlock.unlock(level,base.above(),player,key).consumesAction() && key.getCount()==1
                    && level.getBlockState(base).isAir() && level.getBlockState(base.above()).isAir(),"Either seal half opens both blocks and consumes one key");
            check(SealedHatchBlock.unlock(level,base,player,key)==InteractionResult.PASS && key.getCount()==1,"Repeat unlock cannot consume a second key");
        } finally {
            level.setBlock(base,before,3);level.setBlock(base.above(),above,3);
            for(int i=0;i<3;i++){level.setBlock(base.offset((i-1)*2,0,-2),switches.get(i),3);}
        }
    }
}
