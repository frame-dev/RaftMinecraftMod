package dev.raftocean.client;

import dev.raftocean.RaftOcean;
import dev.raftocean.client.render.RaftRenderer;
import dev.raftocean.registry.ModContent;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPlayerSupport;
import java.util.Comparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

// NeoForge discovers this subscriber only on the physical client.
@EventBusSubscriber(modid = RaftOcean.MOD_ID, value = Dist.CLIENT)
public final class RaftOceanClient {
    private static final KeyMapping.Category BUILD_CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(RaftOcean.MOD_ID,"building"));
    private static final KeyMapping ROTATE_BUILD = new KeyMapping(
            "key.raft_ocean.rotate_build",InputConstants.KEY_R,BUILD_CATEGORY);

    private RaftOceanClient() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(BUILD_CATEGORY);
        event.register(ROTATE_BUILD);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        var game=Minecraft.getInstance();
        while(ROTATE_BUILD.consumeClick()) {
            var player=game.player;
            if(player==null || game.level==null || game.gui.screen()!=null){continue;}
            var kind=ModContent.structuralKind(player.getMainHandItem());
            if(kind==null){kind=ModContent.structuralKind(player.getOffhandItem());}
            if(kind==null || !kind.rotatable()){continue;}
            int rotation=dev.raftocean.client.render.RaftPlacementGhost.rotateBuild();
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
                    new dev.raftocean.network.RaftBuildRotationPayload(rotation));
        }
    }

    @SubscribeEvent
    public static void onClientLogout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        dev.raftocean.client.render.RaftPlacementGhost.resetRotation();
    }

    @SubscribeEvent
    public static void onMeshReload(net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event){
        event.addListener(net.minecraft.resources.Identifier.fromNamespaceAndPath(RaftOcean.MOD_ID,"entity_meshes"),new dev.raftocean.client.render.EntityMeshes());
    }

    @SubscribeEvent
    public static void onMenuScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(dev.raftocean.menu.ResearchMenu.TYPE.get(), ResearchScreen::new);
        event.register(dev.raftocean.menu.RadioMenu.TYPE.get(), RadioScreen::new);
        for (var kind : dev.raftocean.entity.RaftPiece.Kind.values()) {
            if (kind.machine()) { event.register(dev.raftocean.menu.SurvivalMenu.type(kind), SurvivalScreen::new); }
            if (kind.navigation()) { event.register(dev.raftocean.menu.NavigationMenu.type(kind), NavigationScreen::new); }
        }
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(dev.raftocean.creature.CreatureContent.SHARK.get(), dev.raftocean.client.render.CreatureRenderer::new);
        event.registerEntityRenderer(dev.raftocean.creature.CreatureContent.BIRD.get(), dev.raftocean.client.render.CreatureRenderer::new);
        event.registerEntityRenderer(dev.raftocean.creature.CreatureContent.GRAZER.get(), dev.raftocean.client.render.CreatureRenderer::new);
        event.registerEntityRenderer(dev.raftocean.creature.CreatureContent.CHUM.get(), dev.raftocean.client.render.CreatureRenderer::new);
        event.registerEntityRenderer(ModContent.RAFT.get(), RaftRenderer::new);
        event.registerEntityRenderer(ModContent.DEBRIS.get(), dev.raftocean.client.render.SalvageRenderer::new);
        event.registerEntityRenderer(ModContent.SALVAGE_HOOK.get(), dev.raftocean.client.render.SalvageRenderer::new);
    }

    @SubscribeEvent
    public static void onGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAboveAll(net.minecraft.resources.Identifier.fromNamespaceAndPath("raft_ocean","interaction_hints"),
                (graphics,delta)->InteractionHints.draw(graphics,Minecraft.getInstance()));
        event.registerAboveAll(net.minecraft.resources.Identifier.fromNamespaceAndPath("raft_ocean","placement"),(graphics,delta)->{
            var game=Minecraft.getInstance();var ghost=dev.raftocean.client.render.RaftPlacementGhost.last;
            if(game.player==null || game.level==null || ghost==null || game.gui.hud.isHidden() || game.gui.screen()!=null
                    || game.level.getGameTime()-dev.raftocean.client.render.RaftPlacementGhost.tick>1
                    || !(game.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit)
                    || !(hit.getEntity() instanceof dev.raftocean.entity.RaftEntity raft)
                    || !dev.raftocean.client.render.RaftPlacementGhost.belongsTo(raft)) {return;}
            var item=game.player.getMainHandItem();var offhand=game.player.getOffhandItem();
            if(!item.is(ModContent.FOUNDATION.get()) && ModContent.structuralKind(item)==null
                    && !offhand.is(ModContent.FOUNDATION.get()) && ModContent.structuralKind(offhand)==null){return;}
            var label=ghost.valid() && ghost.piece()!=null && ghost.piece().kind().rotatable()
                    ?Component.translatable("raft_ocean.ghost.ready.rotate",ghost.piece().rotation()*90)
                    :Component.translatable(ghost.valid()?"raft_ocean.ghost.ready":"raft_ocean.ghost."+ghost.failure());
            int width=game.font.width(label),x=(graphics.guiWidth()-width)/2,y=graphics.guiHeight()/2+22;
            graphics.fill(x-5,y-3,x+width+5,y+12,0xBB102229);
            graphics.text(game.font,label,x,y,ghost.valid()?0xFF75E8BB:0xFFFF8D83);
        });
        event.registerAboveAll(net.minecraft.resources.Identifier.fromNamespaceAndPath("raft_ocean", "navigation"), (graphics, delta) -> {
            Minecraft minecraft = Minecraft.getInstance();
            var player = minecraft.player;
            if (!dev.raftocean.config.RaftConfig.NAV_HUD.get() || player == null || player.isSpectator() || minecraft.gui.hud.isHidden()) { return; }
            var raft = player.level().getEntitiesOfClass(RaftEntity.class, player.getBoundingBox().inflate(1), candidate ->
                            candidate.getCollisionBoxes().stream().anyMatch(box -> dev.raftocean.entity.RaftContact.isSupported(player.getBoundingBox(), box)))
                    .stream().min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(player))).orElse(null);
            if (raft == null) { return; }
            NavigationHud.draw(graphics, minecraft, raft);
        });
        event.registerAboveAll(net.minecraft.resources.Identifier.fromNamespaceAndPath("raft_ocean", "thirst"), (graphics, delta) -> {
            Minecraft minecraft = Minecraft.getInstance();
            var player = minecraft.player;
            if (player == null || !dev.raftocean.config.RaftConfig.THIRST.get() || !dev.raftocean.config.RaftConfig.THIRST_HUD.get() || player.isSpectator() || player.getAbilities().instabuild || minecraft.gui.hud.isHidden()) { return; }
            ThirstHud.draw(graphics,minecraft);
        });
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        net.neoforged.fml.ModList.get().getModContainerById(RaftOcean.MOD_ID).ifPresent(container ->
                container.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                        net.neoforged.neoforge.client.gui.ConfigurationScreen::new));
        RaftOcean.LOGGER.info("Raft Ocean: client setup complete.");
    }

    @SubscribeEvent
    public static void onClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("raftsupport").executes(context -> {
            var player = Minecraft.getInstance().player;
            if (player == null) {
                return 0;
            }
            RaftEntity raft = player.level().getEntitiesOfClass(RaftEntity.class,
                            player.getBoundingBox().inflate(16.0), candidate -> !candidate.isRemoved())
                    .stream().min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(player))).orElse(null);
            String report = raft == null ? "No nearby raft on the client."
                    : RaftPlayerSupport.supportReport(player, raft);
            RaftOcean.LOGGER.info("Client: {}", report);
            player.sendSystemMessage(Component.literal("Client: " + report));
            return raft == null ? 0 : 1;
        }));
    }
}
