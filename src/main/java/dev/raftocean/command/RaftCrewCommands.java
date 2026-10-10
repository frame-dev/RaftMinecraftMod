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
                .then(Commands.literal("info").executes(context->info(context.getSource())))
                .then(Commands.literal("role").then(Commands.argument("player",net.minecraft.commands.arguments.EntityArgument.player())
                    .then(Commands.literal("crew").executes(c->role(c.getSource(),net.minecraft.commands.arguments.EntityArgument.getPlayer(c,"player"),"crew")))
                    .then(Commands.literal("guest").executes(c->role(c.getSource(),net.minecraft.commands.arguments.EntityArgument.getPlayer(c,"player"),"guest")))))
                .then(Commands.literal("rescue").then(Commands.argument("player",net.minecraft.commands.arguments.EntityArgument.player())
                    .executes(c->rescue(c.getSource(),net.minecraft.commands.arguments.EntityArgument.getPlayer(c,"player"))))));
    }
    public static boolean joinRaft(ServerPlayer player,RaftEntity raft) {
        if(!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping() || !OceanSpawn.isOcean(player.level())
                || raft.isRemoved() || raft.level()!=player.level()
){return false;}
        if(raft.getCollisionBoxes().stream().noneMatch(box->box.inflate(8).contains(player.position()))){return false;}
        Vec3 target=raft.boardingPosition(player);if(target==null){return false;}
        player.teleportTo(target.x,target.y,target.z);player.setDeltaMovement(Vec3.ZERO);player.resetFallDistance();
        var data=OceanRaftData.get(player.level());var old=data.home(player.getUUID());
        var crew=dev.raftocean.crew.CrewData.get(player.level());
        crew.crew(player.level(),raft.getUUID());
        data.remember(player.getUUID(),raft.getUUID(),raft.position());
        if(old==null || !old.raft().equals(raft.getUUID()))crew.guest(player.level(),raft.getUUID(),player.getUUID());
        dev.raftocean.crew.CrewAccess.sync(player);
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
        source.sendSuccess(()->Component.translatable("raft_ocean.crew.info",data.crewSize(home.raft())),false);
        source.sendSuccess(()->Component.translatable("raft_ocean.crew.your_role",Component.translatable("raft_ocean.crew.role."+
                dev.raftocean.crew.CrewData.get(player.level()).role(player.level(),home.raft(),player.getUUID()))),false);return 1;
    }
    private static int role(CommandSourceStack source,ServerPlayer target,String role) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer captain=source.getPlayerOrException();var home=OceanRaftData.get(captain.level()).home(captain.getUUID());
        if(home==null || target.level()!=captain.level() || !dev.raftocean.crew.CrewData.get(captain.level())
                .role(captain.level(),home.raft(),captain.getUUID(),target.getUUID(),role)) {
            source.sendFailure(Component.translatable("raft_ocean.crew.role_failed"));return 0;
        }
        target.closeContainer();dev.raftocean.crew.CrewAccess.publish(target);
        source.sendSuccess(()->Component.translatable("raft_ocean.crew.role_set",target.getDisplayName(),Component.translatable("raft_ocean.crew.role."+role)),false);return 1;
    }
    private static int rescue(CommandSourceStack source,ServerPlayer target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if(!dev.raftocean.crew.CrewRescue.begin(source.getPlayerOrException(),target)) {
            source.sendFailure(Component.translatable("raft_ocean.crew.rescue_failed"));return 0;
        }
        return 1;
    }
    private RaftCrewCommands() { }
}
