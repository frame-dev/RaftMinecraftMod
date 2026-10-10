package dev.raftocean.command;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.world.OceanRaftData;
import dev.raftocean.world.OceanSpawn;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Public cooperative home selection; only boards nearby, loaded, safe rafts. */
public final class RaftCrewCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("raftcrew")
                .then(Commands.literal("join").executes(context->join(context.getSource())))
                .then(Commands.literal("info").executes(context->info(context.getSource()))));
    }
    public static boolean joinRaft(ServerPlayer player,RaftEntity raft) {
        if(!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping() || !OceanSpawn.isOcean(player.level())
                || raft.isRemoved() || raft.level()!=player.level()
){return false;}
        if(raft.getCollisionBoxes().stream().noneMatch(box->box.inflate(8).contains(player.position()))){return false;}
        Vec3 target=raft.boardingPosition(player);if(target==null){return false;}
        player.teleportTo(target.x,target.y,target.z);player.setDeltaMovement(Vec3.ZERO);player.resetFallDistance();
        OceanRaftData.get(player.level()).remember(player.getUUID(),raft.getUUID(),raft.position());
        player.setData(dev.raftocean.survival.SurvivalContent.BED_HOME,dev.raftocean.survival.RaftBeds.Home.EMPTY);
        player.setRespawnPosition(null,false);
        return true;
    }
    private static int join(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player=source.getPlayerOrException();
        var raft=player.level().getEntitiesOfClass(RaftEntity.class,player.getBoundingBox().inflate(8),r->!r.isRemoved())
                .stream().min(java.util.Comparator.comparingDouble(r->r.getBoundingBox().getCenter().distanceToSqr(player.position()))).orElse(null);
        if(raft==null || !joinRaft(player,raft)){source.sendFailure(Component.translatable("raft_ocean.crew.join_failed"));return 0;}
        source.sendSuccess(()->Component.translatable("raft_ocean.crew.joined"),false);return 1;
    }
    private static int info(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player=source.getPlayerOrException();
        var data=OceanRaftData.get(player.level());var home=data.home(player.getUUID());
        if(home==null){source.sendFailure(Component.translatable("raft_ocean.crew.no_home"));return 0;}
        source.sendSuccess(()->Component.translatable("raft_ocean.crew.info",data.crewSize(home.raft())),false);return 1;
    }
    private RaftCrewCommands() { }
}
