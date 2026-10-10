package dev.raftocean.world;

import dev.raftocean.RaftOcean;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.registry.ModContent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** First entry and default death respawn only; normal reconnects retain saved positions. */
public final class OceanSpawn {
    private static final Map<UUID, Request> PENDING = new HashMap<>();
    private OceanSpawn() { }

    public static boolean isOcean(ServerLevel level) {
        return level.dimension().equals(Level.OVERWORLD)
                && level.getChunkSource().getGenerator() instanceof OceanChunkGenerator;
    }

    public static void onServerStarting(ServerStartingEvent event) {
        PENDING.clear();
        ServerLevel level = event.getServer().overworld();
        if (isOcean(level)) {
            level.setRespawnData(LevelData.RespawnData.of(Level.OVERWORLD, new BlockPos(0, 63, 0), 0, 0));
        }
    }

    public static void onStopped(ServerStoppedEvent event) { PENDING.clear(); }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && isOcean(player.level())
                && !player.isSpectator() && OceanRaftData.get(player.level()).home(player.getUUID()) == null) {
            PENDING.put(player.getUUID(), new Request());
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { PENDING.remove(event.getEntity().getUUID()); }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isEndConquered()
                || player.isSpectator() || !isOcean(player.level())) { return; }
        var config = player.getRespawnConfig();
        if (config != null && config.respawnData().dimension().equals(player.level().dimension())
                && player.position().distanceToSqr(Vec3.atCenterOf(config.respawnData().pos())) < 64) {
            return; // A working bed or forced respawn has already placed the player.
        }
        PENDING.put(player.getUUID(), new Request());
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) { return; }
        Request request = PENDING.get(player.getUUID());
        if (request == null) { return; }
        if (!player.isAlive() || !isOcean(player.level()) || player.isSpectator()) {
            PENDING.remove(player.getUUID());
            return;
        }
        if (++request.ticks < 2) { return; }
        ServerLevel level = player.level();
        OceanRaftData data = OceanRaftData.get(level);
        OceanRaftData.Home home = data.home(player.getUUID());
        boolean first = home == null;
        if(home==null && dev.raftocean.config.RaftConfig.SHARED_STARTER.get()){home=data.sharedHome();}
        RaftEntity raft = home != null && level.getEntity(home.raft()) instanceof RaftEntity found && !found.isRemoved()
                ? found : null;
        if (home != null && raft == null && request.ticks < 62) {
            if (request.ticks == 2) {
                // Bring the player near the saved raft so normal player chunk tickets load its entity.
                Vec3 pos = home.position();
                player.teleportTo(pos.x, OceanChunkGenerator.SEA_LEVEL + 2, pos.z);
                loadArea(level, pos);
            }
            return;
        }
        if (raft == null) {
            Vec3 location = home == null ? starterLocation(data.size()) : home.position();
            raft = createRaft(level, location);
            if (raft == null) {
                PENDING.remove(player.getUUID());
                player.sendSystemMessage(Component.translatable("raft_ocean.ocean.spawn_failed"));
                RaftOcean.LOGGER.error("Could not create a safe starter raft for {}", player.getPlainTextName());
                return;
            }
            if(home!=null && (dev.raftocean.config.RaftConfig.SHARED_STARTER.get() || data.crewSize(home.raft())>1)){data.replaceRaft(home.raft(),raft.getUUID(),raft.position());}
            data.remember(player.getUUID(), raft.getUUID(), raft.position());
        }
        Vec3 target = dev.raftocean.survival.RaftBeds.spawnPosition(player, raft);
        if (target == null) { target = raft.boardingPosition(player); }
        if (target == null && !(dev.raftocean.config.RaftConfig.SHARED_STARTER.get()
                && data.sharedHome()!=null && data.sharedHome().raft().equals(raft.getUUID()))) {
            // Preserve the built raft; use a separate empty recovery raft if no surface has headroom.
            raft = createRaft(level, raft.position().add(8, 0, 0));
            if (raft != null) {
                data.remember(player.getUUID(), raft.getUUID(), raft.position());
                target = raft.boardingPosition(player);
            }
        }
        PENDING.remove(player.getUUID());
        if (target == null) {
            player.sendSystemMessage(Component.translatable("raft_ocean.ocean.spawn_failed"));
            return;
        }
        if(first){data.remember(player.getUUID(),raft.getUUID(),raft.position());}
        player.teleportTo(target.x, target.y, target.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        if (first && dev.raftocean.config.RaftConfig.STARTER_SUPPLIES.get()) {
            if(dev.raftocean.config.RaftConfig.STARTER_HAMMER.get()){player.addItem(new ItemStack(ModContent.BUILDING_HAMMER.get()));}
            player.addItem(new ItemStack(ModContent.FOUNDATION.get(), dev.raftocean.config.RaftConfig.STARTER_FOUNDATIONS.get()));
            player.addItem(new ItemStack(Items.OAK_PLANKS, dev.raftocean.config.RaftConfig.STARTER_PLANKS.get()));
            player.addItem(new ItemStack(Items.BREAD, dev.raftocean.config.RaftConfig.STARTER_FOOD.get()));
        }
        player.sendSystemMessage(Component.translatable(first ? "raft_ocean.ocean.welcome" : "raft_ocean.ocean.respawned"));
    }

    public static Vec3 starterLocation(int index) {
        return new Vec3((index % 16) * 16 + .5, OceanChunkGenerator.SEA_LEVEL, (index / 16) * 16 + .5);
    }

    public static RaftEntity createRaft(ServerLevel level, Vec3 near) {
        // Search a bounded area for clear water; never overwrite terrain or player structures.
        for (int attempt = 0; attempt < 16; attempt++) {
            Vec3 pos = near.add((attempt % 4) * 8, 0, (attempt / 4) * 8);
            loadArea(level, pos);
            BlockPos water = BlockPos.containing(pos.x, OceanChunkGenerator.SEA_LEVEL - 1, pos.z);
            if (!level.getFluidState(water).is(FluidTags.WATER)
                    || level.getFluidState(water.above()).is(FluidTags.WATER)) { continue; }
            RaftEntity raft = new RaftEntity(ModContent.RAFT.get(), level);
            raft.setPos(pos.x, OceanChunkGenerator.SEA_LEVEL - .15, pos.z);
            if (!level.getWorldBorder().isWithinBounds(raft.getBoundingBox()) || !level.noCollision(raft, raft.getBoundingBox())
                    || !level.getEntitiesOfClass(RaftEntity.class, raft.getBoundingBox().inflate(1)).isEmpty()) { continue; }
            if (level.addFreshEntity(raft)) { return raft; }
        }
        return null;
    }

    private static void loadArea(ServerLevel level, Vec3 pos) {
        int x = BlockPos.containing(pos).getX() >> 4;
        int z = BlockPos.containing(pos).getZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) { level.getChunk(x + dx, z + dz); }
        }
    }

    public static void onRaftTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof RaftEntity raft && raft.level() instanceof ServerLevel level
                && isOcean(level) && level.getGameTime() % 20 == 0) {
            OceanRaftData.get(level).update(raft.getUUID(), raft.position());
        }
    }

    private static final class Request { int ticks; }
}
