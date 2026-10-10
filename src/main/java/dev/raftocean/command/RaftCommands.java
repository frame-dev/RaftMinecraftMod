package dev.raftocean.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPlayerSupport;
import dev.raftocean.registry.ModContent;
import java.util.Comparator;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class RaftCommands {
    private RaftCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("raft")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("spawn").executes(context -> spawn(context.getSource())))
                .then(Commands.literal("board").executes(context -> board(context.getSource())))
                .then(Commands.literal("info").executes(context -> info(context.getSource())))
                .then(Commands.literal("support").executes(context -> support(context.getSource())))
                .then(Commands.literal("carrycheck").executes(context -> carryCheck(context.getSource())))
                .then(Commands.literal("remove").executes(context -> remove(context.getSource())))
                .then(Commands.literal("research")
                        .then(Commands.literal("unlock")
                                .executes(context -> unlockResearch(context.getSource(),java.util.List.of(context.getSource().getPlayerOrException())))
                                .then(Commands.argument("targets",net.minecraft.commands.arguments.EntityArgument.players())
                                        .executes(context -> unlockResearch(context.getSource(),
                                                net.minecraft.commands.arguments.EntityArgument.getPlayers(context,"targets"))))))
                .then(Commands.literal("current")
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg(-RaftEntity.MAX_CURRENT, RaftEntity.MAX_CURRENT))
                                .then(Commands.argument("z", DoubleArgumentType.doubleArg(-RaftEntity.MAX_CURRENT, RaftEntity.MAX_CURRENT))
                                        .executes(context -> current(context.getSource(),
                                                DoubleArgumentType.getDouble(context, "x"),
                                                DoubleArgumentType.getDouble(context, "z")))))));
    }

    private static int unlockResearch(CommandSourceStack source,java.util.Collection<ServerPlayer> targets) {
        for(var player:targets){dev.raftocean.progression.Research.unlockAll(player);}
        source.sendSuccess(()->Component.translatable("commands.raft_ocean.research.unlocked",targets.size()),true);
        return targets.size();
    }

    private static int spawn(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        int x = Mth.floor(pos.x);
        int z = Mth.floor(pos.z);
        int top = Math.min(level.getMaxY() - 2, Mth.floor(pos.y) + 8);
        int bottom = Math.max(level.getMinY(), Mth.floor(pos.y) - 16);
        for (int y = top; y >= bottom; y--) {
            BlockPos waterPos = new BlockPos(x, y, z);
            if (!level.hasChunkAt(waterPos)) {
                continue;
            }
            FluidState water = level.getFluidState(waterPos);
            if (!water.is(FluidTags.WATER) || level.getFluidState(waterPos.above()).is(FluidTags.WATER)) {
                continue;
            }
            RaftEntity raft = new RaftEntity(ModContent.RAFT.get(), level);
            raft.setPos(x + 0.5, y + water.getHeight(level, waterPos) - 0.15, z + 0.5);
            if (!level.noCollision(raft, raft.getBoundingBox())
                    || !level.getWorldBorder().isWithinBounds(raft.getBoundingBox())) {
                source.sendFailure(Component.translatable("commands.raft_ocean.raft.blocked"));
                return 0;
            }
            if (!level.addFreshEntity(raft)) {
                source.sendFailure(Component.translatable("commands.raft_ocean.raft.spawn_failed"));
                return 0;
            }
            source.sendSuccess(() -> Component.translatable("commands.raft_ocean.raft.spawned"), false);
            return 1;
        }
        source.sendFailure(Component.translatable("commands.raft_ocean.raft.no_water"));
        return 0;
    }

    private static RaftEntity nearest(CommandSourceStack source) {
        Vec3 pos = source.getPosition();
        return source.getLevel().getEntitiesOfClass(RaftEntity.class,
                        new net.minecraft.world.phys.AABB(pos, pos).inflate(16.0),
                        raft -> !raft.isRemoved() && raft.distanceToSqr(pos) <= 256.0)
                .stream().min(Comparator.comparingDouble(raft -> raft.distanceToSqr(pos))).orElse(null);
    }

    private static RaftEntity findOrReport(CommandSourceStack source) {
        RaftEntity raft = nearest(source);
        if (raft == null) {
            source.sendFailure(Component.translatable("commands.raft_ocean.raft.not_found"));
        }
        return raft;
    }

    private static int board(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        RaftEntity raft = findOrReport(source);
        if (raft == null) {
            return 0;
        }
        Vec3 target = raft.boardingPosition(player);
        if (target == null) {
            source.sendFailure(Component.translatable("commands.raft_ocean.raft.board_blocked"));
            return 0;
        }
        player.teleportTo(target.x, target.y, target.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        source.sendSuccess(() -> Component.translatable("commands.raft_ocean.raft.boarded"), false);
        return 1;
    }

    private static int current(CommandSourceStack source, double x, double z) {
        RaftEntity raft = findOrReport(source);
        if (raft == null) {
            return 0;
        }
        raft.setCurrent(x, z);
        source.sendSuccess(() -> Component.translatable("commands.raft_ocean.raft.current",
                format(raft.getCurrentX()), format(raft.getCurrentZ())), false);
        return 1;
    }

    private static int info(CommandSourceStack source) {
        RaftEntity raft = findOrReport(source);
        if (raft == null) {
            return 0;
        }
        source.sendSuccess(() -> Component.translatable("commands.raft_ocean.raft.info",
                raft.getBlocks().size(), format(raft.getX()), format(raft.getDeckY()), format(raft.getZ()),
                format(raft.getDeltaMovement().horizontalDistance() * 20.0),
                format(raft.getMass()), format(raft.getBuoyancy()), raft.getStructuralCount()), false);
        return 1;
    }

    private static int support(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        RaftEntity raft = findOrReport(source);
        if (raft == null) {
            return 0;
        }
        String report = RaftPlayerSupport.supportReport(player, raft);
        dev.raftocean.RaftOcean.LOGGER.info("{}: {}", player.getPlainTextName(), report);
        source.sendSuccess(() -> Component.literal(report), false);
        return 1;
    }

    private static int carryCheck(CommandSourceStack source) {
        try {
            int assertions = RaftCarryCheck.run(source.getLevel(), source.getPosition());
            source.sendSuccess(() -> Component.literal(assertions
                    + " server carry assertions passed (six directions, bobbing, jump detach)."), false);
            return 1;
        } catch (RuntimeException failure) {
            dev.raftocean.RaftOcean.LOGGER.error("Raft runtime carry check failed", failure);
            source.sendFailure(Component.literal("Carry check failed: " + failure.getMessage()));
            return 0;
        }
    }

    private static int remove(CommandSourceStack source) {
        RaftEntity raft = findOrReport(source);
        if (raft == null) {
            return 0;
        }
        raft.discard();
        source.sendSuccess(() -> Component.translatable("commands.raft_ocean.raft.removed"), false);
        return 1;
    }

    private static String format(double number) {
        return String.format(Locale.ROOT, "%.3f", number);
    }
}
