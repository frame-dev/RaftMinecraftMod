package dev.raftocean.creature;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Real config consumers, native combat/loot, damage/removal transactions and ocean AI regression checks. */
public final class CreatureCheck {
    private static int checks;
    private static final BlockPos CROP=new BlockPos(0,1,0);
    private CreatureCheck(){ }
    private static void check(boolean value,String message){if(!value){throw new IllegalStateException(message);}checks++;}
    private static TagValueOutput save(Entity entity,ServerLevel level){var output=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());entity.saveWithoutId(output);return output;}
    private static void load(Entity entity,ServerLevel level,TagValueOutput output){entity.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),output.buildResult()));}
    private static RaftEntity raft(ServerLevel level,String layout,String structure){
        var raft=new RaftEntity(ModContent.RAFT.get(),level);var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        seed.putString("Layout",layout);seed.putString("Structure",structure);load(raft,level,seed);raft.setPos(10.5,level.getMaxY()+16,10.5);return raft;
    }
    public static int run(ServerLevel level) {
        checks=0;
        boolean forced=level.setChunkForced(0,0,true);level.getChunkSource().tick(()->true,false);level.waitForEntities(new net.minecraft.world.level.ChunkPos(0,0),0);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CreatureCheck"));
        var entities=new ArrayList<Entity>();
        boolean peacefulThirst=RaftConfig.PEACEFUL_THIRST.get();
        boolean thirst=RaftConfig.THIRST.get(),damage=RaftConfig.RAFT_DAMAGE.get(),birds=RaftConfig.BIRDS.get();
        int period=RaftConfig.THIRST_PERIOD.get(),levelCap=RaftConfig.DEBRIS_LEVEL_CAP.get(),raftCap=RaftConfig.DEBRIS_CAP.get();
        double current=RaftConfig.CURRENT.get(),processing=RaftConfig.PROCESSING.get();
        var mode=RaftConfig.MODE.get();
        try {
            check(RaftConfig.SPEC.isLoaded(),"Server-synchronized config loaded before gameplay");
            check(BitePose.sample(0,-1).jaw()==0 && BitePose.sample(0,20).thrust()==0,"Idle and completed bite poses settle fully");
            check(BitePose.sample(10,-1).jaw()>0 && BitePose.sample(39,-1).jaw()==42,"Foundation anticipation opens jaw progressively");
            check(BitePose.sample(0,0).jaw()==42 && BitePose.sample(0,3).jaw()==0,"Confirmed bite snaps from open to shut in three ticks");
            check(BitePose.sample(0,2).thrust()>0 && BitePose.sample(0,8).thrust()<0,"Bite has forward lunge followed by backward recoil");
            var drowned=new net.minecraft.world.entity.monster.zombie.Drowned(EntityTypes.DROWNED,level);
            var join=new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(drowned,level);
            dev.raftocean.world.OceanMobRules.onJoin(join);
            check(!join.isCanceled(),"Drowned remain available in ordinary Minecraft worlds");
            drowned.discard();
            RaftConfig.MODE.set(RaftConfig.Mode.EASY);
            check(RaftConfig.attackFrequency(Difficulty.NORMAL)==.5 && RaftConfig.sharkCount(Difficulty.HARD)==1,"Easy overrides world mode and halves bite frequency");
            RaftConfig.MODE.set(RaftConfig.Mode.PEACEFUL);
            check(RaftConfig.attackFrequency(Difficulty.HARD)==0 && RaftConfig.difficulty(Difficulty.HARD)==0,"Peaceful overrides hostile damage");
            RaftConfig.MODE.set(RaftConfig.Mode.HARD);
            check(RaftConfig.effectiveDifficulty(Difficulty.PEACEFUL)==Difficulty.PEACEFUL && RaftConfig.attackFrequency(Difficulty.PEACEFUL)==0 && RaftConfig.difficulty(Difficulty.PEACEFUL)==0,"Minecraft Peaceful wins over the mod Hard override");
            check(RaftConfig.sharkCount(Difficulty.NORMAL)==2 && RaftConfig.attackFrequency(Difficulty.NORMAL)==1.5,"Hard targets two sharks and faster bites");
            RaftConfig.MODE.set(RaftConfig.Mode.WORLD);
            check(RaftConfig.sharkCount(Difficulty.HARD)==2 && RaftConfig.attackFrequency(Difficulty.EASY)==.5,"WORLD follows Minecraft difficulty");
            StringJoiner grid=new StringJoiner(";");
            for(int x=-16;x<16;x++){for(int z=-16;z<16;z++){grid.add(x+","+z);}}
            var large=RaftLayout.decode(grid.toString());
            check(large.cells().size()==1024 && !large.add(new BlockPos(16,0,0)),"1024-foundation connected raft loads and enforces its cap");
            var build=new RaftStructure();
            for(int x=-16;x<16;x++){for(int z=-16;z<0;z++){
                check(build.add(new BlockPos(x,1,z),new RaftPiece(RaftPiece.Kind.FLOOR,0,false),large),"Large build accepts supported floor");
            }}
            var big=raft(level,large.encode(),build.encode());entities.add(big);
            var bigCopy=raft(level,"0,0","");entities.add(bigCopy);load(bigCopy,level,save(big,level));
            check(bigCopy.getBlocks().size()==1024 && bigCopy.pieces().size()==512,"Large native raft save retains every foundation and building piece");
            check((large.encode()+"|"+build.encode()).length()<32767,"Large layout fits native synchronized string limit");
            var chests=raft(level,RaftLayout.starter().encode(),"0,1,0,CHEST,0,0;1,1,0,CHEST,0,0;-1,1,0,CHEST,0,0");entities.add(chests);
            BlockPos left=new BlockPos(-1,1,0),middle=new BlockPos(0,1,0),right=new BlockPos(1,1,0);
            var pairs=dev.raftocean.menu.RaftChests.pairs(chests);
            check(pairs.size()==2 && middle.equals(pairs.get(left)) && !pairs.containsKey(right),"Three chests form one reciprocal double and one single");
            player.setPos(chests.stationPosition(left,RaftPiece.Kind.CHEST));
            chests.storage(left).setItem(0,new ItemStack(Items.DIAMOND,3));chests.storage(middle).setItem(26,new ItemStack(Items.CARROT,7));
            var menu=dev.raftocean.menu.RaftChests.menu(31,player.getInventory(),chests,left);
            var other=dev.raftocean.menu.RaftChests.menu(32,player.getInventory(),chests,middle);
            check(menu.getRowCount()==6 && menu.getSlot(53).getItem().getCount()==7 && other.getSlot(0).getItem().getCount()==3,"Either half opens identical shared 54-slot contents");
            menu.quickMoveStack(player,53);
            check(chests.storage(middle).getItem(26).isEmpty() && player.getInventory().countItem(Items.CARROT)==7,"Double chest shift transfer consumes exactly once");
            var chestCopy=raft(level,"0,0","");entities.add(chestCopy);load(chestCopy,level,save(chests,level));
            check(chestCopy.storage(left).getItem(0).getCount()==3 && dev.raftocean.menu.RaftChests.pairs(chestCopy).size()==2,"Double chest pairing and inventory survive native reload");
            check(chests.removeStructuralPiece(middle) && !menu.stillValid(player) && chests.storage(left).getItem(0).getCount()==3,"Removing half closes old double menu and preserves surviving contents");
            RaftConfig.THIRST.set(false);player.setData(SurvivalContent.THIRST,0);Thirst.tick(player,1200);
            check(Thirst.value(player)==20,"Disabling thirst restores hydration and prevents starvation");
            RaftConfig.THIRST.set(true);RaftConfig.THIRST_PERIOD.set(100);Thirst.tick(player,100);
            check(Thirst.value(player)==19,"Configured hydration period controls actual drain");
            RaftConfig.MODE.set(RaftConfig.Mode.PEACEFUL);RaftConfig.PEACEFUL_THIRST.set(true);
            player.setData(SurvivalContent.THIRST,0);float peacefulHealth=player.getHealth();Thirst.tick(player,100);
            check(player.getHealth()==peacefulHealth,"Mod Peaceful prevents dehydration damage in a non-Peaceful world");
            RaftConfig.PEACEFUL_THIRST.set(false);Thirst.tick(player,100);
            check(Thirst.value(player)==20,"Optional Peaceful thirst toggle restores hydration");
            RaftConfig.MODE.set(RaftConfig.Mode.WORLD);RaftConfig.PEACEFUL_THIRST.set(peacefulThirst);
            RaftConfig.DEBRIS_LEVEL_CAP.set(2);RaftConfig.DEBRIS_CAP.set(1);
            check(dev.raftocean.salvage.DebrisSpawning.canSpawn(1,0) && !dev.raftocean.salvage.DebrisSpawning.canSpawn(2,0)
                    && !dev.raftocean.salvage.DebrisSpawning.canSpawn(0,1),"Natural salvage respects configured caps");
            var plain=raft(level,RaftLayout.starter().encode(),"");entities.add(plain);plain.setCurrent(.02,0);RaftConfig.CURRENT.set(0.0);
            check(plain.navigation().target(true).lengthSqr()==0,"Zero current multiplier disables actual current force");
            RaftConfig.CURRENT.set(2.0);check(Math.abs(plain.navigation().target(true).x-.04)<1E-9,"Current force uses configured multiplier");
            var purifier=raft(level,RaftLayout.starter().encode(),"0,1,0,PURIFIER,0,0");entities.add(purifier);
            purifier.storage(CROP).rawWater=1;purifier.storage(CROP).setItem(1,new ItemStack(Items.OAK_PLANKS));RaftConfig.PROCESSING.set(2.0);
            SurvivalMachines.tick(purifier);
            check(purifier.storage(CROP).progress==40 && purifier.storage(CROP).fuelCapacity-purifier.storage(CROP).fuelTicks==20,
                    "Machine speed config advances processing twice while burning one real second of fuel");
            check(net.minecraft.world.entity.ai.attributes.DefaultAttributes.hasSupplier(CreatureContent.SHARK.get())
                    && net.minecraft.world.entity.ai.attributes.DefaultAttributes.hasSupplier(CreatureContent.BIRD.get())
                    && net.minecraft.world.entity.ai.attributes.DefaultAttributes.hasSupplier(CreatureContent.GRAZER.get()),"All native creature attributes registered");
            var spear=CraftingInput.of(1,3,List.of(new ItemStack(ModContent.SCRAP_FASTENERS.get()),new ItemStack(ModContent.TIMBER_BRACE.get()),new ItemStack(ModContent.CORDAGE.get())));
            check(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,spear,level).orElseThrow().value().assemble(spear).is(CreatureContent.SPEAR.get()),"Loaded original spear recipe assembles");
            var bait=CraftingInput.of(3,2,List.of(new ItemStack(Items.COD),ItemStack.EMPTY,new ItemStack(Items.SALMON),ItemStack.EMPTY,new ItemStack(ModContent.CORDAGE.get()),ItemStack.EMPTY));
            check(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,bait,level).orElseThrow().value().assemble(bait).is(CreatureContent.BAIT.get()),"Loaded chum recipe accepts mixed native raw fish");
            RaftConfig.RAFT_DAMAGE.set(true);BlockPos edge=new BlockPos(1,0,1);plain.setYRot(37);
            check(plain.edgeFoundation(edge) && !plain.edgeFoundation(BlockPos.ZERO),"Sharks select exposed foundation edges");
            check(plain.damageFoundation(edge,25) && plain.foundationHealth(edge)==75,"Bite damages foundation without removing it prematurely");
            plain.damageFoundation(edge,25);check(plain.getBlocks().get(edge).is(CreatureContent.DAMAGED_DECK.get()),"Damaged foundation uses cracked original material");
            var restored=raft(level,RaftLayout.starter().encode(),"");entities.add(restored);load(restored,level,save(plain,level));
            check(restored.foundationHealth(edge)==50 && restored.getYRot()==37,"Foundation health and hull yaw persist together");
            player.getInventory().add(new ItemStack(Items.OAK_PLANKS,2));
            check(plain.repairFoundation(edge,player) && plain.foundationHealth(edge)==75 && player.getInventory().countItem(Items.OAK_PLANKS)==1,"Repair consumes one plank for 25 health");
            check(plain.repairFoundation(edge,player) && plain.foundationHealth(edge)==100 && !plain.repairFoundation(edge,player),"Full-health repair refuses without consuming extras");
            RaftConfig.RAFT_DAMAGE.set(false);check(!plain.damageFoundation(edge,100) && plain.foundationHealth(edge)==100,"Raft damage switch is enforced");RaftConfig.RAFT_DAMAGE.set(true);
            var bridge=raft(level,"0,0;1,0;2,0","2,1,0,CHEST,0,0");entities.add(bridge);bridge.storage(new BlockPos(2,1,0)).setItem(0,new ItemStack(Items.OAK_PLANKS,7));
            Set<UUID> before=new HashSet<>();for(var e:level.getAllEntities()){if(e instanceof ItemEntity){before.add(e.getUUID());}}
            check(bridge.damageFoundation(new BlockPos(1,0,0),100),"Breaking a bridge foundation succeeds");
            check(bridge.getBlocks().size()==1 && bridge.getStructuralCount()==0,"Damage keeps a valid connected buoyant remnant");
            var drops=new ArrayList<ItemEntity>();for(var e:level.getAllEntities()){if(e instanceof ItemEntity item && !before.contains(e.getUUID())){drops.add(item);entities.add(item);}}
            check(drops.stream().filter(e->e.getItem().is(Items.OAK_PLANKS)).mapToInt(e->e.getItem().getCount()).sum()==7
                    && drops.stream().filter(e->e.getItem().is(ModContent.structuralItem(RaftPiece.Kind.CHEST))).count()==1
                    && drops.stream().filter(e->e.getItem().is(ModContent.FOUNDATION.get())).count()==1,"Detached cargo, chest and float drop exactly once");
            load(restored,level,save(bridge,level));check(restored.getBlocks().size()==1,"Broken bridge raft reloads without graph errors");
            var last=raft(level,"0,0","0,1,0,CHEST,0,0");entities.add(last);last.storage(CROP).setItem(0,new ItemStack(Items.CARROT,3));
            check(last.damageFoundation(BlockPos.ZERO,100) && last.isRemoved() && !last.damageFoundation(BlockPos.ZERO,100),"Last foundation destruction cannot run twice");
            var planter=raft(level,RaftLayout.starter().encode(),"0,1,0,PLANTER,0,0");entities.add(planter);
            planter.storage(CROP).setItem(0,new ItemStack(Items.WHEAT_SEEDS,5));planter.storage(CROP).setItem(2,new ItemStack(Items.WHEAT,3));planter.storage(CROP).progress=100;
            RaftConfig.BIRDS.set(true);check(OceanCreature.stealCrop(planter,CROP) && planter.storage(CROP).getItem(2).getCount()==2
                    && planter.storage(CROP).getItem(0).getCount()==5 && planter.storage(CROP).progress==0,"Bird steals one harvest and resets growth without emptying storage");
            RaftConfig.BIRDS.set(false);check(!OceanCreature.stealCrop(planter,CROP) && planter.storage(CROP).getItem(2).getCount()==2,"Bird config blocks theft");RaftConfig.BIRDS.set(true);
            var cover=save(planter,level);cover.putString("Structure","0,1,0,PLANTER,0,0;0,2,0,ROOF,0,0");load(planter,level,cover);
            check(!OceanCreature.stealCrop(planter,CROP),"A roof protects crops from bird theft");
            var grazer=new IslandGrazer(CreatureContent.GRAZER.get(),level);entities.add(grazer);
            check(grazer.getHealth()==16 && grazer.isFood(new ItemStack(Items.WHEAT)) && !grazer.isFood(new ItemStack(Items.STONE))
                    && grazer.getBreedOffspring(level,grazer)!=null,"Friendly grazer has native health, wheat feeding and offspring");
            check(grazer.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.TEMPT_RANGE)==10,
                    "Grazer inherits the native animal temptation range");
            grazer.setPos(8.5,level.getMaxY()+16,8.5);grazer.setNoGravity(true);
            var tempt=new net.minecraft.world.entity.ai.goal.TemptGoal(grazer,1,item->item.is(Items.WHEAT),false);
            tempt.canUse();
            for(int i=0;i<120;i++){tick(grazer);}
            check(grazer.isAlive() && grazer.tickCount>=120,"Grazer runs native goal selection without missing-attribute crashes");
            var grazerCopy=new IslandGrazer(CreatureContent.GRAZER.get(),level);entities.add(grazerCopy);
            load(grazerCopy,level,save(grazer,level));
            new net.minecraft.world.entity.ai.goal.TemptGoal(grazerCopy,1,item->item.is(Items.WHEAT),false).canUse();
            for(int i=0;i<60;i++){tick(grazerCopy);}
            check(grazerCopy.isAlive() && grazerCopy.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.TEMPT_RANGE)==10,
                    "Saved grazers reload with working native temptation AI");
            var calf=grazer.getBreedOffspring(level,grazer);entities.add(calf);
            calf.setPos(grazer.position());calf.setNoGravity(true);calf.setAge(-24000);
            for(int i=0;i<60;i++){tick(calf);}
            check(calf.isAlive() && calf.isBaby() && calf.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.TEMPT_RANGE)==10,
                    "Bred grazer offspring have complete native animal attributes");
            var shark=new OceanCreature(CreatureContent.SHARK.get(),level);entities.add(shark);shark.bind(plain);
            check(shark.getHealth()==30 && shark.hurtServer(level,player.damageSources().playerAttack(player),4) && shark.getHealth()==26,"Native player combat damages shark and interrupts attacks");
            var saved=save(shark,level);var sharkCopy=new OceanCreature(CreatureContent.SHARK.get(),level);entities.add(sharkCopy);load(sharkCopy,level,saved);
            check(plain.getUUID().equals(sharkCopy.raftId()) && sharkCopy.getHealth()==26,"Creature health and raft assignment persist");
            var lootKey=net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,net.minecraft.resources.Identifier.fromNamespaceAndPath("raft_ocean","entities/reef_shark"));
            var loot=level.getServer().reloadableRegistries().getLootTable(lootKey);
            var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(level).withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY,shark)
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN,shark.position())
                    .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE,level.damageSources().generic()).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
            check(loot.getRandomItems(params).stream().anyMatch(stack->stack.is(Items.COD) && stack.getCount()==3),"Loaded data-driven shark drops use current entity loot schema");
        } finally {
            RaftConfig.PEACEFUL_THIRST.set(peacefulThirst);RaftConfig.THIRST.set(thirst);RaftConfig.THIRST_PERIOD.set(period);RaftConfig.RAFT_DAMAGE.set(damage);RaftConfig.BIRDS.set(birds);RaftConfig.MODE.set(mode);RaftConfig.CURRENT.set(current);RaftConfig.PROCESSING.set(processing);
            RaftConfig.DEBRIS_LEVEL_CAP.set(levelCap);RaftConfig.DEBRIS_CAP.set(raftCap);
            for(var entity:entities){
                if(entity instanceof RaftEntity raft && !raft.isRemoved()){raft.pieces().keySet().forEach(pos->{var storage=raft.storage(pos);if(storage!=null){storage.clearContent();}});}
                entity.discard();
            }
            player.getInventory().clearContent();
            if(forced){level.setChunkForced(0,0,false);}
        }
        return checks + dev.raftocean.config.ExpandedConfigCheck.run(level);
    }
    private static void tick(Entity entity){entity.commonTick();entity.tick();}
    public static int ocean(ServerLevel level) {
        int oldAttackDelay=RaftConfig.RAFT_ATTACK_DELAY.get();
        int oldBirdDelay=RaftConfig.BIRD_THEFT_DELAY.get();
        RaftConfig.BIRD_THEFT_DELAY.set(0);
        RaftConfig.RAFT_ATTACK_DELAY.set(0);
        int oldRetreat=RaftConfig.RETREAT_DURATION.get(),oldRepelCooldown=RaftConfig.REPEL_COOLDOWN.get();
        int oldBiteDamage=RaftConfig.BITE_DAMAGE.get(),oldBurst=RaftConfig.BITE_BURST.get(),oldRepel=RaftConfig.REPEL_HITS.get();
        RaftConfig.BITE_DAMAGE.set(25);RaftConfig.BITE_BURST.set(1);
        checks=0;var forced=new ArrayList<net.minecraft.world.level.ChunkPos>();
        for(int x=11;x<=13;x++){for(int z=11;z<=13;z++){if(level.setChunkForced(x,z,true)){forced.add(new net.minecraft.world.level.ChunkPos(x,z));}level.getChunk(x,z);}}
        level.getChunkSource().tick(()->true,false);
        for(int x=11;x<=13;x++){for(int z=11;z<=13;z++){level.waitForEntities(new net.minecraft.world.level.ChunkPos(x,z),0);}}
        var raft=raft(level,RaftLayout.starter().encode(),"0,1,0,PLANTER,0,0");raft.setPos(200.5,62.8,200.5);raft.setYRot(37);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CreatureOceanCheck"));player.setPos(raft.position().add(0,.626,0));level.addNewPlayer(player);
        Difficulty oldDifficulty=level.getDifficulty();boolean natural=RaftConfig.CREATURES.get(),sharks=RaftConfig.SHARKS.get(),birds=RaftConfig.BIRDS.get();
        var mode=RaftConfig.MODE.get();
        int cap=RaftConfig.CREATURE_CAP.get(),baitCap=RaftConfig.BAIT_CAP.get();double aggression=RaftConfig.AGGRESSION.get(),damage=RaftConfig.DIFFICULTY.get();
        var swimmer=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"SharkSwimCheck"));swimmer.setPermanentlyInvulnerable(false);
        var cleanup=new ArrayList<Entity>();cleanup.add(raft);
        try {
            RaftConfig.MODE.set(RaftConfig.Mode.WORLD);level.getServer().setDifficulty(Difficulty.NORMAL,true);RaftConfig.CREATURES.set(true);RaftConfig.SHARKS.set(true);RaftConfig.BIRDS.set(true);RaftConfig.CREATURE_CAP.set(32);RaftConfig.AGGRESSION.set(1.0);RaftConfig.DIFFICULTY.set(1.0);
            check(level.addFreshEntity(raft),"Creature ocean raft registered");
            check(CreatureSpawning.spawn(level)==2,"Loaded occupied ocean spawns one shark and one planter pest");
            var creatures=level.getEntitiesOfClass(OceanCreature.class,raft.getBoundingBox().inflate(32),c->raft.getUUID().equals(c.raftId()));cleanup.addAll(creatures);
            check(creatures.size()==2 && creatures.stream().anyMatch(OceanCreature::shark),"Natural creatures attach to authoritative raft");
            RaftConfig.RAFT_ATTACK_DELAY.set(600);
            var delayed=new OceanCreature(CreatureContent.SHARK.get(),level);cleanup.add(delayed);delayed.bind(raft);
            delayed.setPos(raft.position().add(5,-OceanCreature.SWIM_DEPTH,0));
            RaftConfig.MODE.set(RaftConfig.Mode.HARD);RaftConfig.AGGRESSION.set(4.0);
            delayed.think(level);
            check(delayed.phase()==0 && save(delayed,level).buildResult().getIntOr("RaftAttackDelay",-1)==12000,
                    "New sharks receive ten loaded minutes before the first raft attack");
            var delayState=save(delayed,level);delayState.putInt("Cooldown",0);load(delayed,level,delayState);
            boolean protectedDelay=true;
            for(int i=0;i<11999;i++){delayed.think(level);protectedDelay&=delayed.phase()==0 && !delayed.foundationBite();}
            check(protectedDelay && save(delayed,level).buildResult().getIntOr("RaftAttackDelay",-1)==1,
                    "Hard difficulty and maximum aggression cannot shorten the raft attack delay");
            var remaining=save(delayed,level);load(delayed,level,remaining);
            check(save(delayed,level).buildResult().getIntOr("RaftAttackDelay",-1)==1,
                    "Saving and loading preserve the remaining attack delay");
            delayed.think(level);check(delayed.phase()==1,"Raft approach begins after the configured delay expires");
            RaftConfig.RAFT_ATTACK_DELAY.set(3600);
            var legacy=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);legacy.putInt("Cooldown",0);
            load(delayed,level,legacy);
            check(save(delayed,level).buildResult().getIntOr("RaftAttackDelay",-1)==72000,
                    "Legacy shark saves receive configured grace, including the one-hour maximum");
            load(delayed,level,save(delayed,level));
            check(save(delayed,level).buildResult().getIntOr("RaftAttackDelay",-1)==72000,
                    "Long configured delays are not truncated by the old cooldown save limit");
            delayed.discard();RaftConfig.RAFT_ATTACK_DELAY.set(0);RaftConfig.AGGRESSION.set(1.0);
            RaftConfig.MODE.set(RaftConfig.Mode.HARD);
            check(CreatureSpawning.spawn(level)==1,"Hard mode naturally adds a second shark to occupied raft");
            var hardSharks=level.getEntitiesOfClass(OceanCreature.class,raft.getBoundingBox().inflate(32),c->c.shark() && raft.getUUID().equals(c.raftId()));
            cleanup.addAll(hardSharks);
            check(hardSharks.size()==2 && CreatureSpawning.spawn(level)==0,"Hard mode maintains two sharks without duplicates");
            int hardCount=RaftConfig.HARD_SHARKS.get();
            try {
                RaftConfig.HARD_SHARKS.set(3);
                check(CreatureSpawning.spawn(level)==1,"Configured Hard shark count adds a third natural shark");
                var extraSharks=level.getEntitiesOfClass(OceanCreature.class,raft.getBoundingBox().inflate(32),c->c.shark() && raft.getUUID().equals(c.raftId()) && !hardSharks.contains(c));
                cleanup.addAll(extraSharks);extraSharks.forEach(Entity::discard);
            } finally {RaftConfig.HARD_SHARKS.set(hardCount);}
            hardSharks.stream().filter(c->!creatures.contains(c)).forEach(Entity::discard);
            RaftConfig.MODE.set(RaftConfig.Mode.PEACEFUL);
            hardSharks.stream().filter(c->!c.isRemoved()).forEach(c->{c.commonTick();c.baseTick();c.think(level);});
            check(hardSharks.stream().filter(c->!c.isRemoved()).allMatch(c->c.phase()==0),"Peaceful mode leaves existing sharks non-attacking");
            RaftConfig.MODE.set(RaftConfig.Mode.WORLD);
            check(CreatureSpawning.spawn(level)==0,"Second spawn cannot duplicate raft shark/pest");RaftConfig.CREATURES.set(false);check(CreatureSpawning.spawn(level)==0,"Natural creature spawn toggle enforced");RaftConfig.CREATURES.set(true);
            var shark=creatures.stream().filter(OceanCreature::shark).findFirst().orElseThrow();
            Vec3 initial=shark.position();for(int i=0;i<80;i++){tick(shark);}
            check(!shark.isRemoved() && shark.position().distanceToSqr(initial)>.2 && shark.getHealth()==30,"Native swimming AI moves live shark without drowning");
            var turningShark=new OceanCreature(CreatureContent.SHARK.get(),level);cleanup.add(turningShark);turningShark.bind(raft);
            turningShark.setPos(raft.position().add(8,-OceanCreature.SWIM_DEPTH,0));turningShark.setYRot(-90);
            var turnState=save(turningShark,level);turnState.putInt("Cooldown",0);load(turningShark,level,turnState);
            double initialZ=turningShark.getZ();
            for(int step=0;step<12;step++){
                float oldYaw=turningShark.getYRot();tick(turningShark);
                check(Math.abs(RaftTransform.difference(turningShark.getYRot(),oldYaw))<=OceanCreature.SHARK_TURN_DEGREES+.001,
                        "Shark attack approach changes heading gradually each native tick");
            }
            check(Math.abs(turningShark.getZ()-initialZ)>.05 && turningShark.phase()==1,
                    "Distant shark follows a visible curved approach before its bite");turningShark.discard();
            shark.setPos(raft.position().add(0,.02,0));shark.setDeltaMovement(Vec3.ZERO);
            check(raft.getCollisionBoxes().stream().anyMatch(box->box.intersects(shark.getBoundingBox())),"Stuck-shark fixture overlaps real rotated deck");
            shark.travel(Vec3.ZERO);
            check(raft.getCollisionBoxes().stream().noneMatch(box->box.intersects(shark.getBoundingBox()))
                    && shark.getY()<raft.getY()-.7,"Shark embedded by raft motion escapes below native colliders");
            Vec3 recovered=shark.position();shark.setDeltaMovement(new Vec3(.1,0,0));shark.travel(Vec3.ZERO);
            check(shark.getX()>recovered.x+.05,"Recovered shark continues swimming instead of staying clipped");
            shark.setPos(raft.position().add(0,.35,0));shark.setDeltaMovement(Vec3.ZERO);
            check(raft.getCollisionBoxes().stream().anyMatch(box->box.intersects(shark.getBoundingBox())),"Raised stuck-shark fixture intersects planter above deck");
            shark.travel(Vec3.ZERO);
            check(raft.getCollisionBoxes().stream().noneMatch(box->box.intersects(shark.getBoundingBox())),
                    "Recovery clears the entire column including underlying deck rather than escaping one station into another collider");
            var concave=raft(level,"0,0;1,0;2,0;0,1;2,1;0,2;1,2;2,2","");cleanup.add(concave);
            concave.setPos(raft.position().add(0,0,12));concave.setYRot(63);
            BlockPos innerEdge=new BlockPos(1,0,0);
            shark.setPos(concave.toWorld(new Vec3(1,-OceanCreature.SWIM_DEPTH,1)));
            Vec3 approach=concave.toLocal(shark.bitePosition(concave,innerEdge));
            check(Math.abs(approach.x-1)<.001 && approach.z>1 && Math.abs(approach.y-FoundationBite.BODY_HEIGHT)<.001,
                    "Concave rotated raft bite approaches its exposed inward edge rather than radial hull center");
            BlockPos edge=new BlockPos(1,0,1);Vec3 target=raft.toWorld(new Vec3(1,-.32,1));Vec3 out=target.subtract(raft.position());out=new Vec3(out.x,0,out.z).normalize();
            shark.setPos(target.add(out.scale(1.05)));shark.setDeltaMovement(Vec3.ZERO);var state=save(shark,level);state.putInt("Cooldown",0);state.putInt("FleeTicks",0);load(shark,level,state);
            int largestWindup=0;
            boolean jawContact=false;
            RaftConfig.RAFT_ATTACK_DELAY.set(600);
            for(int i=0;i<120;i++){
                int oldHealth=raft.foundationHealth(edge);
                tick(shark);largestWindup=Math.max(largestWindup,shark.biteWindup());
                if(raft.foundationHealth(edge)<oldHealth){
                    check(save(shark,level).buildResult().getIntOr("RaftAttackDelay",-1)==12000,
                            "A completed foundation attack starts the full configured delay");
                    Vec3 jaw=raft.toLocal(FoundationBite.jaw(shark.position(),shark.getYRot()));
                    double x=jaw.x-edge.getX(),z=jaw.z-edge.getZ();
                    jawContact=shark.foundationBite() && Math.abs(jaw.y-RaftEntity.DECK_HEIGHT*.5)<.08
                            && Math.min(Math.hypot(x-.5,z),Math.min(Math.hypot(x+.5,z),
                            Math.min(Math.hypot(x,z-.5),Math.hypot(x,z+.5))))<.18;
                    check(jawContact,"Confirmed raft damage places the jaw against the actual foundation side at deck height");
                    check(raft.getCollisionBoxes().stream().noneMatch(box->box.intersects(shark.getBoundingBox())),
                            "Foundation bite keeps the physical shark body outside raft colliders");
                }
            }
            check(jawContact,"Real native shark attack reaches the foundation instead of biting water");
            RaftConfig.RAFT_ATTACK_DELAY.set(0);
            for(float yaw:new float[]{0,37,90,143,217,323}){
                for(var normal:List.of(new Vec3(1,0,0),new Vec3(-1,0,0),new Vec3(0,0,1),new Vec3(0,0,-1))){
                    Vec3 local=new Vec3(1,FoundationBite.BODY_HEIGHT,1).add(normal.scale(FoundationBite.BODY_DISTANCE));
                    Vec3 world=RaftTransform.world(local,raft.position(),yaw);
                    Vec3 inward=RaftTransform.rotate(normal.scale(-1),yaw);
                    float heading=(float)Math.toDegrees(Math.atan2(-inward.x,inward.z));
                    Vec3 expected=RaftTransform.world(new Vec3(1,RaftEntity.DECK_HEIGHT*.5,1).add(normal.scale(.5)),raft.position(),yaw);
                    check(FoundationBite.jaw(world,heading).distanceToSqr(expected)<1e-10,
                            "Rendered jaw dimensions align with the foundation side across rotated hull faces");
                }
            }
            check(raft.foundationHealth(edge)==75,"Actual shark windup bites rotated exposed foundation once");
            check(largestWindup>25 && shark.biteImpact()==level.getGameTime() && shark.biteWindup()==0,
                    "Server publishes bite anticipation and retains confirmed foundation impact after attack phase ends");
            RaftConfig.BITE_DAMAGE.set(10);RaftConfig.BITE_BURST.set(10);RaftConfig.REPEL_HITS.set(3);
            var crewRaft=raft(level,RaftLayout.starter().encode(),"");crewRaft.setPos(raft.position().add(8,0,0));crewRaft.setYRot(37);
            check(level.addFreshEntity(crewRaft),"Repeated-bite raft registered");cleanup.add(crewRaft);
            var persistent=new OceanCreature(CreatureContent.SHARK.get(),level);persistent.bind(crewRaft);cleanup.add(persistent);
            BlockPos crewEdge=new BlockPos(1,0,0);
            persistent.setPos(crewRaft.toWorld(new Vec3(1+FoundationBite.BODY_DISTANCE,FoundationBite.BODY_HEIGHT,0)));
            persistent.setYRot(127);check(level.addFreshEntity(persistent),"Repeated-bite shark registered");
            var attackState=save(persistent,level);attackState.putInt("Cooldown",0);load(persistent,level,attackState);
            int windupSetting=RaftConfig.FOUNDATION_WINDUP.get(),pauseSetting=RaftConfig.FOUNDATION_PAUSE.get();
            try {
                RaftConfig.FOUNDATION_WINDUP.set(80);RaftConfig.FOUNDATION_PAUSE.set(60);
                for(int i=0;i<65;i++){tick(persistent);}
                check(crewRaft.foundationHealth(crewEdge)==100 && persistent.biteWindup()>0,"Longer configured windup delays damage while publishing scaled anticipation");
                for(int i=0;i<60 && crewRaft.foundationHealth(crewEdge)==100;i++){tick(persistent);}
                check(crewRaft.foundationHealth(crewEdge)==90,"Configured windup still reaches physical jaw contact");
                for(int i=0;i<55;i++){tick(persistent);}
                check(crewRaft.foundationHealth(crewEdge)==90 && persistent.biteWindup()==0,"Configured recovery pause delays the next bite");
            } finally {RaftConfig.FOUNDATION_WINDUP.set(windupSetting);RaftConfig.FOUNDATION_PAUSE.set(pauseSetting);}
            int confirmed=1;
            for(int i=0;i<650 && crewRaft.getBlocks().containsKey(crewEdge);i++){
                int hp=crewRaft.foundationHealth(crewEdge);tick(persistent);
                int next=crewRaft.getBlocks().containsKey(crewEdge)?crewRaft.foundationHealth(crewEdge):0;
                if(next<hp){confirmed++;check(hp-next==10,"Every repeated bite applies exactly ten damage");}
            }
            check(confirmed==10 && !crewRaft.getBlocks().containsKey(crewEdge),"Ten contact bites break a full-health foundation");
            check(persistent.phase()==4 || persistent.phase()==2,"Shark ends the completed foundation burst");
            persistent.discard();crewRaft.discard();
            var repelRaft=raft(level,RaftLayout.starter().encode(),"");repelRaft.setPos(raft.position().add(8,0,0));repelRaft.setYRot(37);
            check(level.addFreshEntity(repelRaft),"Cooperative repel fixture registered");cleanup.add(repelRaft);
            var defended=new OceanCreature(CreatureContent.SHARK.get(),level);defended.bind(repelRaft);cleanup.add(defended);
            defended.setPos(repelRaft.toWorld(new Vec3(1+FoundationBite.BODY_DISTANCE,FoundationBite.BODY_HEIGHT,0)));defended.setYRot(127);
            check(level.addFreshEntity(defended),"Defended shark registered");
            var defendState=save(defended,level);defendState.putInt("Cooldown",0);load(defended,level,defendState);
            for(int i=0;i<75 && repelRaft.foundationHealth(crewEdge)==100;i++){tick(defended);}
            check(repelRaft.foundationHealth(crewEdge)==90,"First bite starts a continued foundation attack");
            for(boolean vanillaPeaceful:new boolean[]{false,true}){
                RaftConfig.MODE.set(vanillaPeaceful?RaftConfig.Mode.HARD:RaftConfig.Mode.PEACEFUL);
                level.getServer().setDifficulty(vanillaPeaceful?Difficulty.PEACEFUL:Difficulty.NORMAL,true);
                int peacefulHp=repelRaft.foundationHealth(crewEdge);
                for(int i=0;i<100;i++){tick(defended);}
                check(!defended.isRemoved() && defended.phase()==0 && !defended.foundationBite() && defended.biteWindup()==0
                        && repelRaft.foundationHealth(crewEdge)==peacefulHp,"Switching to Peaceful cancels a live bite burst and keeps the shark passive");
                RaftConfig.MODE.set(RaftConfig.Mode.WORLD);level.getServer().setDifficulty(Difficulty.NORMAL,true);
                var resumeState=save(defended,level);resumeState.putInt("Cooldown",0);load(defended,level,resumeState);
                defended.setPos(repelRaft.toWorld(new Vec3(1+FoundationBite.BODY_DISTANCE,FoundationBite.BODY_HEIGHT,0)));defended.setYRot(127);
                tick(defended);
            }
            var teammate=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"RepelTeammate"));
            int retreatSetting=RaftConfig.RETREAT_DURATION.get(),repelCooldownSetting=RaftConfig.REPEL_COOLDOWN.get();
            RaftConfig.RETREAT_DURATION.set(120);RaftConfig.REPEL_COOLDOWN.set(500);
            RaftConfig.RAFT_ATTACK_DELAY.set(600);
            for(int hit=1;hit<=3;hit++){
                if(hit>1){for(int wait=0;wait<12;wait++){tick(defended);}}
                check(defended.hurtServer(level,(hit==2?teammate:player).damageSources().playerAttack(hit==2?teammate:player),1),"Successful crew hit accepted");
                check(hit<3?defended.phase()!=0 && defended.phase()!=4:defended.phase()==4,"First two hits retain attack; third crew hit triggers swimming retreat");
            }
            check(save(defended,level).buildResult().getIntOr("Cooldown",0)>=500,"Repelling uses the configured minimum attack cooldown");
            check(save(defended,level).buildResult().getIntOr("RaftAttackDelay",-1)==12000,"Repelling starts the full raft attack delay");
            RaftConfig.RAFT_ATTACK_DELAY.set(0);
            RaftConfig.RETREAT_DURATION.set(retreatSetting);RaftConfig.REPEL_COOLDOWN.set(repelCooldownSetting);
            int protectedHp=repelRaft.foundationHealth(crewEdge);
            for(int i=0;i<60;i++){tick(defended);}
            check(repelRaft.foundationHealth(crewEdge)==protectedHp && defended.phase()==4,"Configured long retreat keeps the repelled shark swimming away without damage");
            teammate.discard();defended.discard();repelRaft.discard();
            RaftConfig.BITE_DAMAGE.set(25);RaftConfig.BITE_BURST.set(1);
            var chum=new ChumEntity(CreatureContent.CHUM.get(),level);chum.setPos(raft.position().add(4,.08,0));chum.start(level);check(level.addFreshEntity(chum),"Chum float registered");cleanup.add(chum);
            state=save(shark,level);state.putInt("Cooldown",0);load(shark,level,state);shark.think(level);
            check(shark.phase()==3 && raft.foundationHealth(edge)==75,"Physical bait overrides pending raft attack");
            RaftConfig.AGGRESSION.set(0.0);chum.discard();state=save(shark,level);state.putInt("Cooldown",0);load(shark,level,state);shark.think(level);
            check(shark.phase()==0,"Zero aggression suppresses attack state");RaftConfig.AGGRESSION.set(1.0);
            RaftConfig.DIFFICULTY.set(0.0);shark.think(level);check(shark.phase()==0,"Zero damage difficulty disables hostile attacks");RaftConfig.DIFFICULTY.set(1.0);
            var bird=creatures.stream().filter(c->!c.shark()).findFirst().orElseThrow();raft.storage(CROP).setItem(2,new ItemStack(Items.CARROT,3));
            RaftConfig.BIRD_THEFT_DELAY.set(300);
            var delayedBird=new OceanCreature(CreatureContent.BIRD.get(),level);cleanup.add(delayedBird);delayedBird.bind(raft);
            Vec3 cropPoint=raft.stationPosition(CROP,RaftPiece.Kind.PLANTER).add(0,.7,0);
            delayedBird.setPos(cropPoint);delayedBird.think(level);
            check(delayedBird.phase()==0 && save(delayedBird,level).buildResult().getIntOr("CropTheftDelay",-1)==6000,
                    "New crop pests receive five minutes before their first theft");
            var birdWait=save(delayedBird,level);birdWait.putInt("Cooldown",0);load(delayedBird,level,birdWait);
            for(int i=0;i<5999;i++){delayedBird.think(level);}
            check(raft.storage(CROP).getItem(2).getCount()==3 && delayedBird.phase()==0
                    && save(delayedBird,level).buildResult().getIntOr("CropTheftDelay",-1)==1,
                    "A bird already at a ripe planter cannot steal during the configured wait");
            load(delayedBird,level,save(delayedBird,level));
            check(save(delayedBird,level).buildResult().getIntOr("CropTheftDelay",-1)==1,
                    "Crop theft countdown survives saving and loading");
            delayedBird.think(level);
            check(raft.storage(CROP).getItem(2).getCount()==2 && delayedBird.phase()==2
                    && save(delayedBird,level).buildResult().getIntOr("CropTheftDelay",-1)==6000,
                    "Bird steals exactly one crop after expiry and restarts the full wait");
            birdWait=save(delayedBird,level);birdWait.putInt("Cooldown",0);load(delayedBird,level,birdWait);delayedBird.think(level);
            check(raft.storage(CROP).getItem(2).getCount()==2 && delayedBird.phase()==0,
                    "Reloading cannot bypass the post-theft delay by clearing the legacy cooldown");
            birdWait=save(delayedBird,level);birdWait.putInt("CropTheftDelay",0);birdWait.putInt("Cooldown",0);load(delayedBird,level,birdWait);
            delayedBird.setPos(cropPoint.add(3,0,0));delayedBird.think(level);
            check(delayedBird.phase()==1,"Bird fixture begins a crop approach after the delay");
            check(delayedBird.hurtServer(level,player.damageSources().playerAttack(player),1)
                    && save(delayedBird,level).buildResult().getIntOr("CropTheftDelay",-1)==6000,
                    "Scaring a bird away restarts its configurable crop theft delay");
            delayedBird.discard();RaftConfig.BIRD_THEFT_DELAY.set(0);raft.storage(CROP).setItem(2,new ItemStack(Items.CARROT,3));
            RaftConfig.MODE.set(RaftConfig.Mode.PEACEFUL);
            bird.setPos(raft.stationPosition(CROP,RaftPiece.Kind.PLANTER).add(0,.7,0));tick(bird);
            check(raft.storage(CROP).getItem(2).getCount()==3 && bird.phase()==0,"Peaceful crop pests cannot steal planted crops");
            RaftConfig.MODE.set(RaftConfig.Mode.WORLD);
            bird.setPos(raft.stationPosition(CROP,RaftPiece.Kind.PLANTER).add(0,.7,0));state=save(bird,level);state.putInt("Cooldown",0);load(bird,level,state);tick(bird);
            check(raft.storage(CROP).getItem(2).getCount()==2 && bird.phase()==2,"Native bird dive steals one crop from moving planter inventory");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(CreatureContent.BAIT.get(),2));
            Vec3 aim=new Vec3(raft.getX()+3,62.86,raft.getZ()).subtract(player.getEyePosition());
            player.setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));player.setXRot((float)-Math.toDegrees(Math.atan2(aim.y,aim.horizontalDistance())));
            check(CreatureContent.BAIT.get().use(level,player,InteractionHand.MAIN_HAND).consumesAction() && player.getMainHandItem().getCount()==1,"Native water use places one bait and consumes exactly one item");
            check(!CreatureContent.BAIT.get().use(level,player,InteractionHand.MAIN_HAND).consumesAction()
                    && player.getMainHandItem().getCount()==1,"Overlapping bait placement rejects without item loss");
            RaftConfig.BAIT_CAP.set(1);
            check(!CreatureContent.BAIT.get().use(level,player,InteractionHand.MAIN_HAND).consumesAction()
                    && player.getMainHandItem().getCount()==1,"Configured loaded bait cap refuses placement before consumption");
            RaftConfig.BAIT_CAP.set(baitCap);
            var placed=level.getEntitiesOfClass(ChumEntity.class,raft.getBoundingBox().inflate(8),c->!c.isRemoved());cleanup.addAll(placed);check(placed.size()==1,"Bait item installs exactly one world entity");
            state=save(placed.getFirst(),level);state.putLong("Expires",level.getGameTime());load(placed.getFirst(),level,state);tick(placed.getFirst());check(placed.getFirst().isRemoved(),"Expired bait cleans up after reload without replaying duration");
            check(shark.hurtServer(level,player.damageSources().playerAttack(player),2) && shark.phase()==0,"Combat interrupts a live ocean shark");
            check(shark.biteWindup()==0 && shark.biteImpact()==-1,"Combat interruption clears bite animation data");
            state=save(shark,level);state.putInt("Cooldown",0);state.putInt("FleeTicks",0);load(shark,level,state);
            swimmer.setPos(shark.position().add(.3,0,0));level.addNewPlayer(swimmer);swimmer.connection.markClientLoaded();swimmer.commonTick();swimmer.baseTick();
            check(swimmer.isInWater(),"Native player fluid state detects swimming next to shark");
            float health=swimmer.getHealth();
            for(int step=0;step<80 && swimmer.getHealth()==health;step++){
                float oldYaw=shark.getYRot();shark.think(level);
                check(Math.abs(RaftTransform.difference(shark.getYRot(),oldYaw))<=OceanCreature.SHARK_TURN_DEGREES+.001,
                        "Turning toward swimmer never snaps heading");
            }
            check(swimmer.getHealth()<health && shark.phase()==2,"Shark attacks a real server-side swimmer");
            check(shark.biteImpact()==level.getGameTime(),"Successful swimmer damage publishes impact after visible windup");
            long impact=shark.biteImpact();
            health=swimmer.getHealth();shark.think(level);check(swimmer.getHealth()==health && shark.biteImpact()==impact,"Player attack cooldown prevents repeated damage or animation retriggers");
            raft.pieces().keySet().forEach(pos->{var storage=raft.storage(pos);if(storage!=null){storage.clearContent();}});
            cleanup.forEach(Entity::discard);
            StringJoiner fullGrid=new StringJoiner(";");
            for(int x=-16;x<16;x++){for(int z=-16;z<16;z++){fullGrid.add(x+","+z);}}
            var full=raft(level,fullGrid.toString(),"0,1,0,STEERING_WHEEL,0,0");cleanup.add(full);
            full.setPos(200.5,62.82,200.5);full.setCurrent(.03,0);full.navigation().setHeading(15);
            check(level.addFreshEntity(full),"1024-foundation raft enters loaded ocean");
            Vec3 start=full.position();for(int i=0;i<20;i++){tick(full);}
            check(full.position().distanceToSqr(start)>.001 && full.getYRot()>5 && full.getBlocks().size()==1024,"Full-size raft floats, drifts and physically turns without losing cells");
            var local=full.getCollisionBoxes().stream().map(box->box.move(full.position().scale(-1))).toList();
            check(local.size()<2000,"Merged full-size rotated deck avoids per-foundation strip explosion");
            var trapped=new OceanCreature(CreatureContent.SHARK.get(),level);cleanup.add(trapped);trapped.bind(full);
            trapped.setPos(full.position().add(0,.01,0));trapped.travel(Vec3.ZERO);
            check(full.getCollisionBoxes().stream().noneMatch(box->box.intersects(trapped.getBoundingBox())),
                    "Shark recovery also clears the merged rotated 1024-foundation deck");
        } finally {
            raft.pieces().keySet().forEach(pos->{var storage=raft.storage(pos);if(storage!=null){storage.clearContent();}});
            cleanup.forEach(Entity::discard);level.removePlayerImmediately(player,Entity.RemovalReason.DISCARDED);
            if(level.players().contains(swimmer)){level.removePlayerImmediately(swimmer,Entity.RemovalReason.DISCARDED);}
            RaftConfig.CREATURES.set(natural);RaftConfig.SHARKS.set(sharks);RaftConfig.BIRDS.set(birds);RaftConfig.CREATURE_CAP.set(cap);RaftConfig.BAIT_CAP.set(baitCap);RaftConfig.AGGRESSION.set(aggression);RaftConfig.DIFFICULTY.set(damage);
            RaftConfig.RETREAT_DURATION.set(oldRetreat);RaftConfig.REPEL_COOLDOWN.set(oldRepelCooldown);
            RaftConfig.RAFT_ATTACK_DELAY.set(oldAttackDelay);
            RaftConfig.BIRD_THEFT_DELAY.set(oldBirdDelay);
            RaftConfig.BITE_DAMAGE.set(oldBiteDamage);RaftConfig.BITE_BURST.set(oldBurst);RaftConfig.REPEL_HITS.set(oldRepel);
            RaftConfig.MODE.set(mode);level.getServer().setDifficulty(oldDifficulty,true);forced.forEach(pos->level.setChunkForced(pos.x(),pos.z(),false));
        }
        return checks;
    }
}

