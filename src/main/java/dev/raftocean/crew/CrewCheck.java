package dev.raftocean.crew;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.progression.Research;
import dev.raftocean.registry.ModContent;
import dev.raftocean.world.OceanRaftData;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Real attachment sharing, role revocation, death interception and rescue transactions. */
public final class CrewCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    public static int run(ServerLevel level) {
        checks=0;
        var a=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CrewCaptain"));
        var b=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CrewMember"));
        var stranger=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CrewStranger"));
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.setPos(8,100,8);
        var data=OceanRaftData.get(level);var crews=CrewData.get(level);
        boolean shared=RaftConfig.SHARED_RESEARCH.get(),permissions=RaftConfig.CREW_PERMISSIONS.get(),rescue=RaftConfig.CREW_RESCUE.get();
        try {
            RaftConfig.SHARED_RESEARCH.set(true);RaftConfig.CREW_PERMISSIONS.set(true);RaftConfig.CREW_RESCUE.set(true);
            data.remember(a.getUUID(),raft.getUUID(),raft.position());data.remember(b.getUUID(),raft.getUUID(),raft.position());
            require(crews.role(level,raft.getUUID(),a.getUUID()).equals("captain"),"First saved owner becomes captain");
            require(CrewAccess.allowed(a,raft) && CrewAccess.allowed(b,raft),"Captain and established crew may operate raft");
            require(!CrewAccess.allowed(stranger,raft),"Visitors cannot edit an owned raft");
            require(!crews.role(level,raft.getUUID(),b.getUUID(),a.getUUID(),"guest"),"Crew cannot demote captain");
            crews.guest(level,raft.getUUID(),b.getUUID());require(!CrewAccess.allowed(b,raft),"Guest restriction applies immediately");
            RaftConfig.CREW_PERMISSIONS.set(false);require(CrewAccess.allowed(b,raft),"Open permissions configuration works");RaftConfig.CREW_PERMISSIONS.set(true);
            a.setData(Research.SCHEMA,1);b.setData(Research.SCHEMA,1);
            a.setData(Research.KNOWLEDGE,1);a.setData(Research.MATERIALS,3);CrewAccess.sync(a);CrewAccess.sync(b);
            require(b.getData(Research.KNOWLEDGE)==0,"Guest receives no shared research");
            require(crews.role(level,raft.getUUID(),a.getUUID(),b.getUUID(),"crew"),"Captain may promote guest");
            CrewAccess.sync(b);require(b.getData(Research.KNOWLEDGE)==1 && b.getData(Research.MATERIALS)==3,"Promoted crew receives saved knowledge and samples");
            b.setData(Research.KNOWLEDGE,3);b.setData(Research.MATERIALS,7);CrewAccess.sync(b);CrewAccess.sync(a);
            require(a.getData(Research.KNOWLEDGE)==3 && a.getData(Research.MATERIALS)==7,"Crew research unions without losing earlier samples");
            var save=CrewData.CODEC.encodeStart(NbtOps.INSTANCE,crews).getOrThrow();
            var restored=CrewData.CODEC.parse(NbtOps.INSTANCE,save).getOrThrow();
            require(restored.crew(level,raft.getUUID()).knowledge()==3 && restored.role(level,raft.getUUID(),a.getUUID()).equals("captain"),"Native crew codec preserves ownership and shared research");
            RaftConfig.SHARED_RESEARCH.set(false);a.setData(Research.KNOWLEDGE,7);CrewAccess.sync(a);CrewAccess.sync(b);
            require(b.getData(Research.KNOWLEDGE)==3,"Disabled sharing leaves personal research untouched");RaftConfig.SHARED_RESEARCH.set(true);
            var replacement=UUID.randomUUID();crews.replace(raft.getUUID(),replacement);data.replaceRaft(raft.getUUID(),replacement,Vec3.ZERO);
            require(crews.crew(level,replacement).knowledge()==3 && crews.role(level,replacement,a.getUUID()).equals("captain"),"Replacement raft retains crew permissions and research");
            data.replaceRaft(replacement,raft.getUUID(),raft.position());crews.replace(replacement,raft.getUUID());
            a.setPos(8,100,8);b.setPos(9,100,8);a.setShiftKeyDown(true);
            var online=level.getServer().getPlayerList().getPlayersByUUID();online.put(a.getUUID(),a);online.put(b.getUUID(),b);
            b.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND,3));
            var death=new LivingDeathEvent(b,b.damageSources().generic());CrewRescue.onDeath(death);
            require(death.isCanceled() && CrewRescue.downed(b) && b.getHealth()==1,"Lethal crew damage opens bounded rescue window");
            require(!CrewAccess.allowed(b,raft),"Incapacitated crew cannot operate machinery");
            require(b.getMainHandItem().getCount()==3,"Incapacitation preserves inventory exactly");
            var damage=new LivingIncomingDamageEvent(b,new net.neoforged.neoforge.common.damagesource.DamageContainer(b.damageSources().generic(),2));
            CrewRescue.onDamage(damage);require(damage.isCanceled(),"Ordinary damage does not bypass rescue window");
            require(CrewRescue.begin(a,b),"Nearby empty-hand sneak interaction starts rescue");
            for(int tick=0;tick<59;tick++) {
                if(tick%4==0)require(CrewRescue.begin(a,b),"Holding right-click does not reset rescue progress");
                CrewRescue.onTick(new PlayerTickEvent.Post(a));
            }
            require(CrewRescue.downed(b),"Rescue requires full three-second channel");
            CrewRescue.onTick(new PlayerTickEvent.Post(a));
            require(!CrewRescue.downed(b) && b.getHealth()==RaftConfig.RESCUE_HEALTH.get().floatValue(),"Completed channel restores configured health");
            require(b.getMainHandItem().getCount()==3,"Rescue neither duplicates nor consumes inventory");
            b.setData(CrewRescue.DOWNED,100);require(CrewRescue.begin(a,b),"Second rescue may start");
            a.setPos(20,100,8);CrewRescue.onTick(new PlayerTickEvent.Post(a));a.setPos(8,100,8);
            for(int tick=0;tick<65;tick++)CrewRescue.onTick(new PlayerTickEvent.Post(a));
            require(CrewRescue.downed(b),"Walking away cancels channel instead of remote revive");
            a.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
            require(!CrewRescue.begin(a,b),"Rescue requires empty main hand");a.getInventory().clearContent();
            b.setData(CrewRescue.DOWNED,0);RaftConfig.CREW_RESCUE.set(false);
            death=new LivingDeathEvent(b,b.damageSources().generic());CrewRescue.onDeath(death);
            require(!death.isCanceled(),"Rescue configuration can be disabled");RaftConfig.CREW_RESCUE.set(true);
            death=new LivingDeathEvent(b,b.damageSources().fellOutOfWorld());CrewRescue.onDeath(death);
            require(!death.isCanceled(),"Void damage remains lethal");
            death=new LivingDeathEvent(b,b.damageSources().genericKill());CrewRescue.onDeath(death);
            require(!death.isCanceled(),"Kill commands remain lethal");
            online.remove(a.getUUID());death=new LivingDeathEvent(b,b.damageSources().generic());CrewRescue.onDeath(death);
            require(!death.isCanceled(),"No online teammate means ordinary death");online.put(a.getUUID(),a);
            b.setData(CrewRescue.DOWNED,1);b.setInvulnerableTime(0);b.setPermanentlyInvulnerable(false);
            b.connection.markClientLoaded();CrewRescue.onTick(new PlayerTickEvent.Post(b));
            require(!CrewRescue.downed(b) && !b.isAlive(),"Expired rescue window results in actual native death");
        } finally {
            level.getServer().getPlayerList().getPlayersByUUID().remove(a.getUUID());level.getServer().getPlayerList().getPlayersByUUID().remove(b.getUUID());
            CrewRescue.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(a));
            RaftConfig.SHARED_RESEARCH.set(shared);RaftConfig.CREW_PERMISSIONS.set(permissions);RaftConfig.CREW_RESCUE.set(rescue);
            a.discard();b.discard();stranger.discard();raft.discard();
        }
        return checks;
    }
    private CrewCheck() { }
}
