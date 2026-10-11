package dev.raftocean;

import com.mojang.logging.LogUtils;
import dev.raftocean.command.RaftCommands;
import dev.raftocean.entity.RaftPlayerSupport;
import dev.raftocean.registry.ModContent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(RaftOcean.MOD_ID)
public final class RaftOcean {
    public static final String MOD_ID = "raft_ocean";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static int smokeShutdownTicks = -1;

    public RaftOcean(IEventBus modEventBus, net.neoforged.fml.ModContainer container) {
        if(Boolean.getBoolean("raftOcean.oceanSmoke") || Boolean.getBoolean("raftOcean.carrySmoke")) {
            NeoForge.EVENT_BUS.addListener(RaftOcean::finishSmoke);
        }
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SYNCED, dev.raftocean.config.RaftConfig.SPEC);
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, dev.raftocean.config.RaftConfig.CLIENT_SPEC);
        dev.raftocean.story.IslandContent.register(modEventBus);
        dev.raftocean.story.Exploration.initialize();
        dev.raftocean.trading.OutpostTrading.initialize();
        NeoForge.EVENT_BUS.addListener(dev.raftocean.trading.OutpostTrading::onStop);
        dev.raftocean.crew.CrewRescue.initialize();
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewAccess::onLogin);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewAccess::onTick);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onDeath);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onDamage);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onInteract);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onUse);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onBlock);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onAttack);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onBreak);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onTick);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onLogout);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.crew.CrewRescue::onStop);
        dev.raftocean.creature.CreatureContent.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.creature.CreatureSpawning::onTick);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanMobRules::onJoin);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanMobRules::onTick);
        dev.raftocean.survival.SurvivalContent.ATTACHMENTS.register(modEventBus);
        dev.raftocean.survival.CraftingExpansion.initialize();
        dev.raftocean.survival.DivingContent.initialize();
        dev.raftocean.survival.Automation.initialize();
        dev.raftocean.survival.Batteries.initialize();
        dev.raftocean.archery.ArcheryContent.initialize();
        dev.raftocean.progression.Research.initialize();
        dev.raftocean.menu.ResearchMenu.TYPES.register(modEventBus);
        dev.raftocean.registry.ModSounds.SOUNDS.register(modEventBus);
        ModContent.BLOCKS.register(modEventBus);
        ModContent.ITEMS.register(modEventBus);
        ModContent.ENTITY_TYPES.register(modEventBus);
        ModContent.CREATIVE_TABS.register(modEventBus);
        ModContent.CHUNK_GENERATORS.register(modEventBus);
        dev.raftocean.survival.ProcessingRecipe.TYPES.register(modEventBus);
        dev.raftocean.survival.ProcessingRecipe.SERIALIZERS.register(modEventBus);
        dev.raftocean.menu.SurvivalMenu.TYPES.register(modEventBus);
        dev.raftocean.menu.NavigationMenu.TYPES.register(modEventBus);
        modEventBus.addListener(dev.raftocean.compat.EnergyItems::register);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(dev.raftocean.network.RaftMovementSync::register);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.survival.Thirst::onTick);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.progression.ResearchRecipes::onTick);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.survival.ProcessingRecipe::onSync);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.survival.RaftBeds::onTick);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.salvage.DebrisSpawning::onTick);
        NeoForge.EVENT_BUS.addListener(RaftCommands::register);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.command.RaftCrewCommands::register);
        NeoForge.EVENT_BUS.addListener(RaftPlayerSupport::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(RaftPlayerSupport::onPlayerTickPost);
        NeoForge.EVENT_BUS.addListener(RaftPlayerSupport::beforeEntityTick);
        NeoForge.EVENT_BUS.addListener(RaftPlayerSupport::afterEntityTick);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.entity.RaftSpatialIndex::onJoin);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.entity.RaftSpatialIndex::onLeave);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.entity.RaftSpatialIndex::onUnload);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanSpawn::onServerStarting);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanSpawn::onStopped);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanSpawn::onLogin);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanSpawn::onLogout);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanSpawn::onRespawn);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanSpawn::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(dev.raftocean.world.OceanSpawn::onRaftTick);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> dev.raftocean.archery.ArcheryContent.ARROWS.forEach(
                arrow -> net.minecraft.world.level.block.DispenserBlock.registerProjectileBehavior(arrow.get())));
        LOGGER.info("Raft Ocean: common setup complete.");
    }

    private void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Raft Ocean: server starting; common entry point loaded.");
        if (Boolean.getBoolean("raftOcean.carrySmoke")) {
            try {
                dev.raftocean.entity.RaftModelExport.write();
                int collisionChecks = dev.raftocean.entity.RaftCollisionCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime collision assertions passed.", collisionChecks);
                int nativeMovementChecks = dev.raftocean.entity.RaftMovementValidationCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} native movement validation assertions passed.",nativeMovementChecks);
                int assertions = dev.raftocean.command.RaftCarryCheck.run(event.getServer().overworld(),
                        net.minecraft.world.phys.Vec3.ZERO);
                LOGGER.info("Raft Ocean: {} runtime carry assertions passed.", assertions);
                int constructionChecks = dev.raftocean.entity.RaftConstructionCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime construction assertions passed.", constructionChecks);
                LOGGER.info("Raft Ocean: {} runtime building expansion/API assertions passed.",dev.raftocean.entity.RaftExpansionCheck.run(event.getServer().overworld()));
                LOGGER.info("Raft Ocean: {} runtime machine API assertions passed.",dev.raftocean.entity.RaftMachineApiCheck.run(event.getServer().overworld()));
                LOGGER.info("Raft Ocean: {} runtime coastal utility assertions passed.",dev.raftocean.survival.CoastalUtilityCheck.run(event.getServer().overworld()));
                int craftingChecks = dev.raftocean.entity.RaftCraftingCheck.run(event.getServer().overworld());
                craftingChecks += dev.raftocean.entity.RecipeAuditCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime crafting assertions passed.", craftingChecks);
                int stationChecks = dev.raftocean.entity.RaftStationCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime station assertions passed.", stationChecks);
                int researchChecks=dev.raftocean.progression.ResearchCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime research assertions passed.", researchChecks);
                LOGGER.info("Raft Ocean: {} runtime crew/rescue assertions passed.",dev.raftocean.crew.CrewCheck.run(event.getServer().overworld()));
                LOGGER.info("Raft Ocean: {} runtime outpost trading assertions passed.",dev.raftocean.trading.OutpostCheck.run(event.getServer().overworld()));
                int salvageChecks = dev.raftocean.salvage.SalvageCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime salvage assertions passed.", salvageChecks);
                int survivalChecks = dev.raftocean.survival.SurvivalCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime survival assertions passed.", survivalChecks);
                int navigationChecks = dev.raftocean.navigation.NavigationCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime navigation assertions passed.", navigationChecks);
                int creatureChecks=dev.raftocean.creature.CreatureCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime creature/config assertions passed.", creatureChecks);
            } finally {
                smokeShutdownTicks = 40;
            }
        }
        if (Boolean.getBoolean("raftOcean.oceanSmoke")) {
            try {
                dev.raftocean.world.OceanSpawn.onServerStarting(event);
                int assertions = dev.raftocean.world.OceanWorldCheck.run(event.getServer().overworld());
                LOGGER.info("Raft Ocean: {} runtime ocean-world assertions passed.", assertions);
            } finally {
                smokeShutdownTicks = 40;
            }
        }
    }

    private static void finishSmoke(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        // Let queued generation and lighting work drain before the test server saves.
        if(smokeShutdownTicks > 0 && --smokeShutdownTicks == 0)event.getServer().halt(false);
    }
}
