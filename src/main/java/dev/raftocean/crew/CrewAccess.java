package dev.raftocean.crew;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.progression.Research;
import dev.raftocean.world.OceanRaftData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class CrewAccess {
    public static boolean allowed(Player player,RaftEntity raft) {
        if(player.level().isClientSide())return true;
        if(CrewRescue.downed(player))return false;
        if(!RaftConfig.CREW_PERMISSIONS.get() || player.getAbilities().instabuild)return true;
        return !CrewData.get((net.minecraft.server.level.ServerLevel)raft.level())
                .role((net.minecraft.server.level.ServerLevel)raft.level(),raft.getUUID(),player.getUUID()).equals("guest");
    }
    public static boolean sameCrew(ServerPlayer a,ServerPlayer b) {
        if(a.level()!=b.level())return false;
        var data=OceanRaftData.get(a.level());var home=data.home(a.getUUID());var other=data.home(b.getUUID());
        return home!=null && other!=null && home.raft().equals(other.raft());
    }
    public static void sync(ServerPlayer player) {
        if(!RaftConfig.SHARED_RESEARCH.get())return;
        var home=OceanRaftData.get(player.level()).home(player.getUUID());if(home==null)return;
        var data=CrewData.get(player.level());
        if(data.role(player.level(),home.raft(),player.getUUID()).equals("guest"))return;
        // Migrate legacy research before publishing its bits into a crew's current schema.
        Research.state(player,0);
        data.share(player.level(),home.raft(),player.getData(Research.KNOWLEDGE),player.getData(Research.MATERIALS),player.getData(dev.raftocean.progression.StorySchematics.DISCOVERED));
        var crew=data.crew(player.level(),home.raft());
        int k=player.getData(Research.KNOWLEDGE)|crew.knowledge();
        int m=player.getData(Research.MATERIALS)|crew.materials();
        int s=player.getData(dev.raftocean.progression.StorySchematics.DISCOVERED)|crew.schematics();
        if(k!=player.getData(Research.KNOWLEDGE) || m!=player.getData(Research.MATERIALS)
                || s!=player.getData(dev.raftocean.progression.StorySchematics.DISCOVERED)) {
            player.setData(Research.KNOWLEDGE,k);player.setData(Research.MATERIALS,m);
            player.setData(dev.raftocean.progression.StorySchematics.DISCOVERED,s);
            dev.raftocean.progression.ResearchRecipes.reconcile(player,true);
        }
    }
    public static void publish(Player player) {
        if(!(player instanceof ServerPlayer server))return;
        sync(server);
        for(var member:server.level().getServer().getPlayerList().getPlayersByUUID().values())if(member!=server && sameCrew(server,member))sync(member);
    }
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player)sync(player);
    }
    public static void onTick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player && player.tickCount%20==0)sync(player);
    }
    private CrewAccess() { }
}
