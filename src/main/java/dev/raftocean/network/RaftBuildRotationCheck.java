package dev.raftocean.network;

import com.mojang.authlib.GameProfile;
import dev.raftocean.building.BuildingHammer;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.registry.ModContent;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** A replacement server player must place the same rotation the client is still previewing. */
public final class RaftBuildRotationCheck {
    private RaftBuildRotationCheck() { }

    public static int run(ServerLevel level) {
        var profile = new GameProfile(UUID.randomUUID(), "RotationCheck");
        var original = new FakePlayer(level, profile);
        var respawned = new FakePlayer(level, profile);
        var endReturn = new FakePlayer(level, profile);
        var reconnected = new FakePlayer(level, profile);
        var raft = new RaftEntity(ModContent.RAFT.get(), level);
        raft.setPos(8, level.getMaxY() - 12, 8);
        raft.setYRot(37);
        level.getChunkAt(BlockPos.containing(raft.position()));
        try {
            require(RaftBuildRotationSync.rotation(original) == 0, "New sessions start at the default angle");
            RaftBuildRotationSync.acceptRotation(original, 2);
            RaftBuildRotationSync.acceptRotation(original, 4);
            RaftBuildRotationSync.acceptRotation(original, -1);
            require(RaftBuildRotationSync.rotation(original) == 2, "Invalid rotation packets leave the current angle alone");

            // PlayerList.respawn reuses the play connection before it restores the old entity ID.
            respawned.connection = original.connection;
            require(RaftBuildRotationSync.rotation(respawned) == 2, "Death respawn keeps the client preview offset");
            respawned.setId(original.getId());
            endReturn.connection = respawned.connection;
            endReturn.setId(respawned.getId());
            require(RaftBuildRotationSync.rotation(endReturn) == 2, "Returning from the End keeps the client preview offset");
            require(RaftBuildRotationSync.rotation(reconnected) == 0, "A reconnect with the same UUID starts at zero");

            var item = ModContent.structuralItem(RaftPiece.Kind.BASIC_PURIFIER);
            var hammer = new ItemStack(ModContent.BUILDING_HAMMER.get());
            BuildingHammer.choose(hammer, new ItemStack(item));
            respawned.getInventory().setItem(9, new ItemStack(item, 2));
            respawned.setItemInHand(InteractionHand.MAIN_HAND, hammer);
            respawned.setPos(raft.toWorld(new Vec3(0, .626, 0)));
            Vec3 look = raft.toWorld(new Vec3(1, .24, 0)).subtract(respawned.getEyePosition());
            respawned.setYRot((float) Math.toDegrees(Math.atan2(-look.x, look.z)));
            respawned.setXRot((float) -Math.toDegrees(Math.atan2(look.y, look.horizontalDistance())));
            var pick = raft.pick(respawned);
            var ghost = raft.placement(respawned, hammer, pick, 2);
            var defaultPlan = raft.placement(respawned, hammer, pick, 0);
            require(ghost != null && ghost.valid() && defaultPlan != null
                    && ghost.piece().rotation() != defaultPlan.piece().rotation(), "Fixture distinguishes the retained angle from zero");
            require(raft.interact(respawned, InteractionHand.MAIN_HAND, Vec3.ZERO).consumesAction()
                    && ghost.piece().equals(raft.pieces().get(ghost.cell()))
                    && respawned.getInventory().getItem(9).getCount() == 1,
                    "First hammer placement after respawn matches the ghost and consumes one piece");

            RaftBuildRotationSync.acceptRotation(respawned, 3);
            require(RaftBuildRotationSync.rotation(endReturn) == 3 && RaftBuildRotationSync.rotation(reconnected) == 0,
                    "Subsequent rotation packets update only the addressed session");
            return 8;
        } finally {
            raft.discard();
            original.discard();
            respawned.discard();
            endReturn.discard();
            reconnected.discard();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
