package dev.raftocean.world;

import com.mojang.authlib.GameProfile;
import dev.raftocean.entity.RaftContact;
import dev.raftocean.entity.RaftEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Isolated Ocean World boot checks; never run against the user's client/server worlds. */
public final class OceanWorldCheck {
    private OceanWorldCheck() { }

    public static int run(ServerLevel level) {
        require(OceanSpawn.isOcean(level), "Dedicated server did not select Ocean World");
        int checks = 1;
        var drowned=new net.minecraft.world.entity.monster.zombie.Drowned(net.minecraft.world.entity.EntityTypes.DROWNED,level);
        drowned.setPos(8,61,8);
        require(!level.addFreshEntity(drowned),"Ocean World rejects fresh drowned before installation");
        checks++;
        var loaded=new net.neoforged.neoforge.event.entity.EntityJoinLevelEvent(drowned,level,true);
        OceanMobRules.onJoin(loaded);
        require(loaded.isCanceled(),"Ocean World rejects saved drowned on chunk load");
        checks++;
        var drownedTick=new EntityTickEvent.Pre(drowned);
        OceanMobRules.onTick(drownedTick);
        require(drowned.isRemoved() && drownedTick.isCanceled(),"Already loaded drowned discard without death loot");
        checks++;
        var generator=level.getChunkSource().getGenerator();
        var monsters=generator.getMobsAt(level,level.structureManager(),net.minecraft.world.entity.MobCategory.MONSTER,BlockPos.ZERO);
        require(monsters.unwrap().stream().noneMatch(entry->entry.value().type()==net.minecraft.world.entity.EntityTypes.DROWNED),
                "Natural Ocean World spawn table excludes drowned");
        checks++;
        var waterMobs=generator.getMobsAt(level,level.structureManager(),net.minecraft.world.entity.MobCategory.WATER_CREATURE,BlockPos.ZERO);
        require(!waterMobs.isEmpty(),"Removing drowned preserves other aquatic spawn entries");
        checks++;
        require(level.getChunkSource().getGenerator().getSeaLevel() == 63, "Generator sea level must be 63");
        checks++;
        require(level.getRespawnData().pos().getY() == 63, "Default ocean spawn must be at water surface");
        checks++;
        int[][] samples = {{0, 0}, {31, -31}, {-64, 64}, {1024, -512}, {-1024, 512}};
        for (int[] sample : samples) {
            level.getChunk(sample[0] >> 4, sample[1] >> 4);
            require(level.getBlockState(new BlockPos(sample[0], -64, sample[1])).is(Blocks.BEDROCK), "Bedrock layer");
            require(level.getBlockState(new BlockPos(sample[0], 48, sample[1])).is(Blocks.SAND), "Sand seabed");
            require(level.getFluidState(new BlockPos(sample[0], 49, sample[1])).is(net.minecraft.tags.FluidTags.WATER),
                    "Bottom water layer remains flooded beneath ocean features");
            require(level.getFluidState(new BlockPos(sample[0], 62, sample[1])).is(net.minecraft.tags.FluidTags.WATER),
                    "Top water layer remains flooded beneath ocean features");
            require(level.getBlockState(new BlockPos(sample[0], 63, sample[1])).isAir(), "Air above sea");
            require(level.getBiome(new BlockPos(sample[0], 63, sample[1])).is(Biomes.OCEAN), "Ocean biome");
            require(Math.abs(dev.raftocean.salvage.DebrisSpawning.surface(level, sample[0] + .5, 63, sample[1] + .5) - (62 + 8.0 / 9)) < 1.0E-5,
                    "Salvage detects exposed water surface");
            checks += 7;
        }
        RaftEntity streamSource = new RaftEntity(dev.raftocean.registry.ModContent.RAFT.get(), level);
        streamSource.setPos(.5, 62.9, .5);
        var passing = dev.raftocean.salvage.DebrisEntity.create(level, dev.raftocean.salvage.DebrisEntity.Kind.PLANK,
                new Vec3(2, 62 + 8.0 / 9 + .025, 2));
        passing.assignStream(streamSource, new Vec3(1, 0, 0));
        for (int tick = 0; tick < 10; tick++) { passing.tick(); }
        require(passing.getX() < 2 && passing.getZ() == 2 && !passing.isRemoved(), "Actual water tick keeps fixed stream drift");
        checks++;
        streamSource.setPos(5, 62.9, 2); streamSource.setCurrent(0, -.05);
        double previousX = passing.getX();
        for (int tick = 0; tick < 10; tick++) { passing.tick(); }
        require(passing.getX() < previousX && passing.getZ() == 2 && !passing.isRemoved(), "Passed salvage continues behind without following changed raft");
        checks++;
        passing.discard(); streamSource.discard();
        UUID owner = UUID.randomUUID();
        FakePlayer player = new TestPlayer(level, owner);
        OceanSpawn.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
        tick(player, 2);
        OceanRaftData data = OceanRaftData.get(level);
        OceanRaftData.Home home = data.home(owner);
        require(home != null, "First join must remember starter ownership");
        checks++;
        require(level.getEntity(home.raft()) instanceof RaftEntity, "Starter raft must be a registered saved entity");
        checks++;
        RaftEntity raft = (RaftEntity) level.getEntity(home.raft());
        require(raft.getBlocks().size() == 9 && supported(player, raft), "First join must board a 3x3 raft");
        checks++;
        require(player.getInventory().countItem(Items.OAK_PLANKS) == 16
                && player.getInventory().countItem(Items.BREAD) == 8, "Starter supplies granted once");
        checks++;
        Vec3 before = player.position();
        OceanSpawn.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
        tick(player, 3);
        require(data.home(owner).raft().equals(home.raft()) && before.equals(player.position())
                && player.getInventory().countItem(Items.BREAD) == 8, "Reconnect must not teleport, duplicate raft, or refill kit");
        checks++;
        // Keep this synchronous test inside the loaded entity section; live raft
        // movement cannot cross into an unloaded section and is tested separately.
        raft.setPos(raft.position().add(.4, 0, .4));
        data.update(raft.getUUID(), raft.position());
        var encoded = OceanRaftData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        OceanRaftData decoded = OceanRaftData.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        require(decoded.home(owner).equals(data.home(owner)), "Moving raft ownership and position persist through codec");
        checks++;
        FakePlayer respawned = new TestPlayer(level, owner);
        OceanSpawn.onRespawn(new PlayerEvent.PlayerRespawnEvent(respawned, false));
        tick(respawned, 2);
        require(supported(respawned, raft) && data.home(owner).raft().equals(home.raft()),
                "Default respawn returns to original moving raft: player=" + respawned.position()
                        + ", raft=" + raft.position() + ", resolved=" + level.getEntity(home.raft()));
        checks++;
        require(respawned.getInventory().countItem(Items.BREAD) == 0, "Death must not refill starter supplies");
        checks++;
        var bedCell = new BlockPos(1, 1, 0);
        var bedStructure = new dev.raftocean.entity.RaftStructure();
        require(bedStructure.add(bedCell, new dev.raftocean.entity.RaftPiece(dev.raftocean.entity.RaftPiece.Kind.BED, 0, false),
                dev.raftocean.entity.RaftLayout.starter()), "Bed supported on raft"); checks++;
        var bedSave = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
        raft.saveWithoutId(bedSave); bedSave.putString("Structure", bedStructure.encode());
        raft.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), bedSave.buildResult()));
        dev.raftocean.survival.RaftBeds.use(raft, bedCell, respawned);
        require(respawned.getData(dev.raftocean.survival.SurvivalContent.BED_HOME).raft().equals(raft.getUUID()), "Raft bed sets persisted moving home"); checks++;
        raft.setPos(raft.position().add(.3, 0, .2)); data.update(raft.getUUID(), raft.position());
        var bedRespawn = new TestPlayer(level, owner); bedRespawn.copyAttachmentsFrom(respawned, true);
        OceanSpawn.onRespawn(new PlayerEvent.PlayerRespawnEvent(bedRespawn, false)); tick(bedRespawn, 2);
        var bedTarget = dev.raftocean.survival.RaftBeds.spawnPosition(bedRespawn, raft);
        require(bedTarget != null && bedRespawn.position().distanceToSqr(bedTarget) < 1.0E-12, "Respawn follows bed cell on moved raft"); checks++;
        require(raft.removeStructuralPiece(bedCell), "Bed can be removed"); checks++;
        OceanSpawn.onRespawn(new PlayerEvent.PlayerRespawnEvent(bedRespawn, false)); tick(bedRespawn, 2);
        require(supported(bedRespawn, raft), "Removed bed falls back to safe raft deck"); checks++;
        checks += survivalEnvironment(level);
        checks += dev.raftocean.navigation.NavigationCheck.ocean(level);
        raft.discard();
        OceanSpawn.onRespawn(new PlayerEvent.PlayerRespawnEvent(respawned, false));
        tick(respawned, 62);
        OceanRaftData.Home recovery = data.home(owner);
        require(!recovery.raft().equals(home.raft()) && level.getEntity(recovery.raft()) instanceof RaftEntity,
                "Missing raft receives a bounded recovery after entity-loading grace period");
        checks++;
        RaftEntity replacement = (RaftEntity) level.getEntity(recovery.raft());
        require(supported(respawned, replacement), "Recovery raft must have a safe supported deck");
        checks++;
        boolean sharedSetting=dev.raftocean.config.RaftConfig.SHARED_STARTER.get();
        try {
            dev.raftocean.config.RaftConfig.SHARED_STARTER.set(true);
            // Use the existing loaded recovery raft as the designated shared home.
            var oldShared=data.sharedHome();
            if(oldShared!=null){data.replaceRaft(oldShared.raft(),replacement.getUUID(),replacement.position());}
            var crewId=UUID.randomUUID();var crew=new TestPlayer(level,crewId);
            int savedHomes=data.size();
            OceanSpawn.onLogin(new PlayerEvent.PlayerLoggedInEvent(crew));tick(crew,2);
            require(data.home(crewId).raft().equals(replacement.getUUID()) && supported(crew,replacement),"Co-op first join boards the same saved raft");checks++;
            require(data.size()==savedHomes+1 && crew.getInventory().countItem(Items.BREAD)==8,"Co-op entry has one home and one starter supply grant");checks++;
            OceanSpawn.onLogin(new PlayerEvent.PlayerLoggedInEvent(crew));tick(crew,2);
            require(crew.getInventory().countItem(Items.BREAD)==8,"Co-op reconnect cannot duplicate starter supplies");checks++;
            boolean grants=dev.raftocean.config.RaftConfig.STARTER_SUPPLIES.get(),hammer=dev.raftocean.config.RaftConfig.STARTER_HAMMER.get();
            int foundations=dev.raftocean.config.RaftConfig.STARTER_FOUNDATIONS.get(),planks=dev.raftocean.config.RaftConfig.STARTER_PLANKS.get(),bread=dev.raftocean.config.RaftConfig.STARTER_FOOD.get();
            try {
                dev.raftocean.config.RaftConfig.STARTER_HAMMER.set(false);dev.raftocean.config.RaftConfig.STARTER_FOUNDATIONS.set(0);
                dev.raftocean.config.RaftConfig.STARTER_PLANKS.set(128);dev.raftocean.config.RaftConfig.STARTER_FOOD.set(3);
                var customCrew=new TestPlayer(level,UUID.randomUUID());
                OceanSpawn.onLogin(new PlayerEvent.PlayerLoggedInEvent(customCrew));tick(customCrew,2);
                require(customCrew.getInventory().countItem(Items.OAK_PLANKS)==128 && customCrew.getInventory().countItem(Items.BREAD)==3
                        && customCrew.getInventory().countItem(dev.raftocean.registry.ModContent.FOUNDATION.get())==0
                        && customCrew.getInventory().countItem(dev.raftocean.registry.ModContent.BUILDING_HAMMER.get())==0,"Custom starter supplies split large stacks and omit zero counts/hammer");checks++;
                customCrew.discard();dev.raftocean.config.RaftConfig.STARTER_SUPPLIES.set(false);
                var emptyCrew=new TestPlayer(level,UUID.randomUUID());OceanSpawn.onLogin(new PlayerEvent.PlayerLoggedInEvent(emptyCrew));tick(emptyCrew,2);
                require(emptyCrew.getInventory().isEmpty() && supported(emptyCrew,replacement),"Disabling supplies still safely boards a new co-op player");checks++;
                emptyCrew.discard();
            } finally {
                dev.raftocean.config.RaftConfig.STARTER_SUPPLIES.set(grants);dev.raftocean.config.RaftConfig.STARTER_HAMMER.set(hammer);
                dev.raftocean.config.RaftConfig.STARTER_FOUNDATIONS.set(foundations);dev.raftocean.config.RaftConfig.STARTER_PLANKS.set(planks);dev.raftocean.config.RaftConfig.STARTER_FOOD.set(bread);
            }
            var coSave=OceanRaftData.CODEC.encodeStart(NbtOps.INSTANCE,data).getOrThrow();
            var coLoad=OceanRaftData.CODEC.parse(NbtOps.INSTANCE,coSave).getOrThrow();
            require(coLoad.sharedHome().raft().equals(replacement.getUUID()) && coLoad.home(crewId).equals(data.home(crewId)),"Shared raft and crew homes persist through native codec");checks++;
            var recruit=new TestPlayer(level,UUID.randomUUID());recruit.setPos(replacement.position().add(0,1,0));
            require(dev.raftocean.command.RaftCrewCommands.joinRaft(recruit,replacement) && data.home(recruit.getUUID()).raft().equals(replacement.getUUID()),"Public crew join selects nearby raft as moving home");checks++;
            recruit.setPos(replacement.position().add(100,0,0));
            require(!dev.raftocean.command.RaftCrewCommands.joinRaft(recruit,replacement),"Crew command rejects distant boarding");checks++;
            var crewRespawn=new TestPlayer(level,crewId);OceanSpawn.onRespawn(new PlayerEvent.PlayerRespawnEvent(crewRespawn,false));tick(crewRespawn,2);
            require(supported(crewRespawn,replacement) && crewRespawn.getInventory().countItem(Items.BREAD)==0,"Co-op death returns to shared raft without supplying more items");checks++;
            // A manually formed crew must also stay together after losing its raft,
            // even when automatic shared-starter entry is disabled.
            dev.raftocean.config.RaftConfig.SHARED_STARTER.set(false);
            replacement.discard();OceanSpawn.onRespawn(new PlayerEvent.PlayerRespawnEvent(crewRespawn,false));tick(crewRespawn,62);
            var recoveredCrew=data.home(crewId);
            require(!recoveredCrew.raft().equals(replacement.getUUID())
                    && data.home(recruit.getUUID()).raft().equals(recoveredCrew.raft()),"Missing crew raft replacement updates offline/other crew homes together");checks++;
            require(level.getEntity(recoveredCrew.raft()) instanceof RaftEntity recovered && supported(crewRespawn,recovered),"Crew recovery creates one shared safe replacement");checks++;
            if(level.getEntity(recoveredCrew.raft()) instanceof RaftEntity recovered){recovered.discard();}
            crew.discard();recruit.discard();crewRespawn.discard();
        } finally { dev.raftocean.config.RaftConfig.SHARED_STARTER.set(sharedSetting); }
        replacement.discard();
        OceanSpawn.onLogout(new PlayerEvent.PlayerLoggedOutEvent(respawned));
        checks += dev.raftocean.creature.CreatureCheck.ocean(level);
        checks += dev.raftocean.story.IslandCheck.run(level);
        checks += dev.raftocean.story.RadioCheck.run(level);
        checks += dev.raftocean.story.ExplorationCheck.run(level);
        checks += dev.raftocean.trading.OutpostIslandCheck.run(level);
        return checks;
    }

    private static int survivalEnvironment(ServerLevel level) {
        int checks = 0;
        var rain = dev.raftocean.survival.SurvivalCheck.create(level, dev.raftocean.entity.RaftPiece.Kind.RAIN_COLLECTOR);
        rain.setPos(6.5, 63, 6.5);
        BlockPos cell = new BlockPos(1, 1, 0);
        var inventory = rain.storage(cell);
        inventory.setItem(0, new net.minecraft.world.item.ItemStack(dev.raftocean.survival.SurvivalContent.EMPTY_CUP.get()));
        float beforeRain = level.getRainLevel(1);
        var waterUser = new TestPlayer(level, UUID.randomUUID());
        waterUser.setPos(10.5, 63.1, 10.5); waterUser.setXRot(90);
        waterUser.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(dev.raftocean.survival.SurvivalContent.EMPTY_CUP.get()));
        waterUser.gameMode.useItem(waterUser, level, waterUser.getMainHandItem(), net.minecraft.world.InteractionHand.MAIN_HAND);
        require(waterUser.getMainHandItem().is(dev.raftocean.survival.SurvivalContent.SALT_WATER.get()), "Cup fills from actual ocean water"); checks++;
        for (var hand : net.minecraft.world.InteractionHand.values()) {
            waterUser.getInventory().clearContent();
            waterUser.setItemInHand(hand, new net.minecraft.world.item.ItemStack(dev.raftocean.survival.SurvivalContent.EMPTY_CUP.get(), 3));
            waterUser.gameMode.useItem(waterUser, level, waterUser.getItemInHand(hand), hand);
            require(waterUser.getItemInHand(hand).getCount() == 2
                    && waterUser.getInventory().countItem(dev.raftocean.survival.SurvivalContent.SALT_WATER.get()) == 1,
                    "Native ocean right-click exchanges one of three cups in either hand"); checks++;
        }
        waterUser.getInventory().clearContent();
        for (int i = 0; i < 36; i++) { waterUser.getInventory().setItem(i, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE, 64)); }
        waterUser.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(dev.raftocean.survival.SurvivalContent.EMPTY_CUP.get()));
        waterUser.gameMode.useItem(waterUser, level, waterUser.getMainHandItem(), net.minecraft.world.InteractionHand.MAIN_HAND);
        require(waterUser.getMainHandItem().is(dev.raftocean.survival.SurvivalContent.SALT_WATER.get())
                && waterUser.getMainHandItem().getCount() == 1, "Full inventory keeps the filled cup in the original hand"); checks++;
        waterUser.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.item.ItemStack(dev.raftocean.survival.SurvivalContent.EMPTY_CUP.get(), 2));
        var existingDrops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, waterUser.getBoundingBox().inflate(4))
                .stream().map(net.minecraft.world.entity.Entity::getId).collect(java.util.stream.Collectors.toSet());
        waterUser.gameMode.useItem(waterUser, level, waterUser.getMainHandItem(), net.minecraft.world.InteractionHand.MAIN_HAND);
        var droppedCups = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, waterUser.getBoundingBox().inflate(4))
                .stream().filter(item -> !existingDrops.contains(item.getId())).toList();
        require(waterUser.getMainHandItem().getCount() == 1 && droppedCups.size() == 1
                && droppedCups.getFirst().getItem().is(dev.raftocean.survival.SurvivalContent.SALT_WATER.get())
                && droppedCups.getFirst().getItem().getCount() == 1, "Full stacked inventory drops the filled cup instead of deleting it"); checks++;
        droppedCups.forEach(net.minecraft.world.entity.Entity::discard);
        try {
            level.setRainLevel(0);
            for (int i=0; i<10; i++) { dev.raftocean.survival.SurvivalMachines.process(rain, cell, dev.raftocean.entity.RaftPiece.Kind.RAIN_COLLECTOR, level); }
            require(inventory.progress == 0 && inventory.getItem(0).getCount() == 1, "Dry collector does not create water"); checks++;
            level.setRainLevel(1);
            require(dev.raftocean.survival.SurvivalMachines.rainReaches(rain, cell, level), "Ocean rain reaches open collector"); checks++;
            for (int i=0; i<10; i++) { dev.raftocean.survival.SurvivalMachines.process(rain, cell, dev.raftocean.entity.RaftPiece.Kind.RAIN_COLLECTOR, level); }
            require(inventory.getItem(0).getCount() == 1 && inventory.cleanWater == 1, "Rain collector stores water without consuming a cup"); checks++;
            dev.raftocean.survival.SurvivalMachines.process(rain, cell, dev.raftocean.entity.RaftPiece.Kind.RAIN_COLLECTOR, level);
            require(inventory.getItem(0).isEmpty() && inventory.getItem(2).is(dev.raftocean.survival.SurvivalContent.FRESH_WATER.get())
                    && inventory.cleanWater == 0, "Inserted cup draws stored water once"); checks++;
            inventory.setItem(2, net.minecraft.world.item.ItemStack.EMPTY);
            for (int i=0; i<50; i++) { dev.raftocean.survival.SurvivalMachines.process(rain, cell, dev.raftocean.entity.RaftPiece.Kind.RAIN_COLLECTOR, level); }
            require(inventory.cleanWater == 4 && inventory.progress == 0, "Cup-free collector fills its reservoir and pauses at four"); checks++;
            var save = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
            rain.saveWithoutId(save);
            var structure = dev.raftocean.entity.RaftStructure.decode(save.buildResult().getStringOr("Structure", ""), dev.raftocean.entity.RaftLayout.starter());
            require(structure.add(cell.above(), new dev.raftocean.entity.RaftPiece(dev.raftocean.entity.RaftPiece.Kind.ROOF, 0, false),
                    dev.raftocean.entity.RaftLayout.starter()), "Collector roof supported"); checks++;
            save.putString("Structure", structure.encode());
            rain.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), save.buildResult()));
            require(!dev.raftocean.survival.SurvivalMachines.rainReaches(rain, cell, level), "Owned moving roof blocks rain collection"); checks++;
        } finally {
            level.setRainLevel(beforeRain); rain.storage(cell).clearContent(); rain.discard();
        }
        var bed = dev.raftocean.survival.SurvivalCheck.create(level, dev.raftocean.entity.RaftPiece.Kind.BED); bed.setPos(12.5, 63, 12.5);
        var first = new TestPlayer(level, UUID.randomUUID()); var second = new TestPlayer(level, UUID.randomUUID());
        OceanRaftData.get(level).remember(first.getUUID(),bed.getUUID(),bed.position());
        OceanRaftData.get(level).remember(second.getUUID(),bed.getUUID(),bed.position());
        first.setPos(bed.position().add(0, .26, 0)); second.setPos(bed.position().add(0, .26, .5));
        level.addNewPlayer(first); level.addNewPlayer(second);
        var clock = level.dimensionType().defaultClock().orElseThrow();
        long oldTime = level.clockManager().getInstance(clock).totalTicks();
        try {
            level.clockManager().setTotalTicks(clock, 13000); level.updateSkyBrightness();
            dev.raftocean.survival.RaftBeds.use(bed, cell, first);
            dev.raftocean.survival.RaftBeds.tickRest(level, level.getGameTime() + 100);
            require(level.clockManager().getInstance(clock).totalTicks() == 13000, "One resting player cannot skip night for both"); checks++;
            dev.raftocean.survival.RaftBeds.use(bed, cell, second);
            dev.raftocean.survival.RaftBeds.tickRest(level, level.getGameTime() + 99);
            require(level.clockManager().getInstance(clock).totalTicks() == 13000, "Night rest needs five seconds"); checks++;
            dev.raftocean.survival.RaftBeds.tickRest(level, level.getGameTime() + 100);
            require(level.clockManager().getInstance(clock).totalTicks() > 13000, "Cooperative raft rest advances actual world clock to morning"); checks++;
        } finally {
            level.clockManager().setTotalTicks(clock, oldTime); level.updateSkyBrightness();
            level.removePlayerImmediately(first, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            level.removePlayerImmediately(second, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED); bed.discard();
        }
        return checks;
    }

    private static boolean supported(FakePlayer player, RaftEntity raft) {
        return raft.getCollisionBoxes().stream().anyMatch(box -> RaftContact.isSupported(player.getBoundingBox(), box));
    }

    private static void tick(FakePlayer player, int count) {
        for (int i = 0; i < count; i++) { OceanSpawn.onPlayerTick(new PlayerTickEvent.Post(player)); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new IllegalStateException(message); }
    }

    /** NeoForge's fake connection drops teleports; this records server position for the check. */
    private static final class TestPlayer extends FakePlayer {
        TestPlayer(ServerLevel level, UUID owner) { super(level, new GameProfile(owner, "OceanSpawnCheck")); }
        @Override
        public void teleportTo(double x, double y, double z) { setPos(x, y, z); }
    }
}
