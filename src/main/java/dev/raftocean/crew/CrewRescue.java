package dev.raftocean.crew;

import com.mojang.serialization.Codec;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.survival.SurvivalContent;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** A bounded rescue window; no inventory is copied or dropped until actual death. */
public final class CrewRescue {
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> DOWNED=SurvivalContent.ATTACHMENTS.register(
            "downed",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,12000).fieldOf("ticks")).build());
    private record Channel(UUID target,int ticks) { }
    private static final Map<UUID,Channel> CHANNELS=new HashMap<>();
    public static void initialize() { }
    public static boolean downed(Player player) { return player.getData(DOWNED)>0; }
    public static boolean canRescue(ServerPlayer rescuer,ServerPlayer target) {
        if(!RaftConfig.CREW_RESCUE.get() || !rescuer.isAlive() || rescuer.isSpectator() || downed(rescuer)
                || !target.isAlive() || !downed(target) || !rescuer.isShiftKeyDown()
                || !rescuer.getMainHandItem().isEmpty() || rescuer.distanceToSqr(target)>9
                || !CrewAccess.sameCrew(rescuer,target))return false;
        var home=dev.raftocean.world.OceanRaftData.get(rescuer.level()).home(rescuer.getUUID());
        return !CrewData.get(rescuer.level()).role(rescuer.level(),home.raft(),rescuer.getUUID()).equals("guest");
    }
    public static boolean begin(ServerPlayer rescuer,ServerPlayer target) {
        if(!canRescue(rescuer,target))return false;
        Channel current=CHANNELS.get(rescuer.getUUID());
        if(current!=null && current.target.equals(target.getUUID()))return true;
        CHANNELS.put(rescuer.getUUID(),new Channel(target.getUUID(),0));return true;
    }
    public static void onDeath(LivingDeathEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !RaftConfig.CREW_RESCUE.get()
                || player.isSpectator() || player.getAbilities().instabuild || downed(player)
                || event.getSource().is(DamageTypes.GENERIC_KILL) || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD))return;
        boolean partner=player.level().getServer().getPlayerList().getPlayersByUUID().values().stream().anyMatch(other->other!=player
                && other.isAlive() && !other.isSpectator() && !downed(other) && CrewAccess.sameCrew(player,other)
                && !CrewData.get(other.level()).role(other.level(),dev.raftocean.world.OceanRaftData.get(other.level())
                        .home(other.getUUID()).raft(),other.getUUID()).equals("guest"));
        if(!partner)return;
        event.setCanceled(true);player.setHealth(1);player.closeContainer();
        player.setData(DOWNED,RaftConfig.RESCUE_SECONDS.get()*20);
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,4,false,false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,4,false,false));
        player.sendSystemMessage(Component.translatable("raft_ocean.crew.downed_help"));
        for(var other:player.level().getServer().getPlayerList().getPlayersByUUID().values())if(other!=player && CrewAccess.sameCrew(player,other))
            other.sendSystemMessage(Component.translatable("raft_ocean.crew.rescue_alert",player.getDisplayName()));
    }
    public static void onDamage(LivingIncomingDamageEvent event) {
        if(event.getSource().getEntity() instanceof Player attacker && downed(attacker))event.setCanceled(true);
        if(event.getEntity() instanceof Player player && downed(player)
                && !event.getSource().is(DamageTypes.GENERIC_KILL) && !event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD))event.setCanceled(true);
    }
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if(event.getEntity() instanceof ServerPlayer rescuer && event.getTarget() instanceof ServerPlayer target && downed(target)) {
            event.setCanceled(true);event.setCancellationResult(begin(rescuer,target)?InteractionResult.SUCCESS_SERVER:InteractionResult.FAIL);
        }
    }
    public static void onUse(PlayerInteractEvent.RightClickItem event) {
        if(downed(event.getEntity())){event.setCanceled(true);event.setCancellationResult(InteractionResult.FAIL);}
    }
    public static void onBlock(PlayerInteractEvent.RightClickBlock event) {
        if(downed(event.getEntity())){event.setCanceled(true);event.setCancellationResult(InteractionResult.FAIL);}
    }
    public static void onTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        int left=player.getData(DOWNED);
        if(left>0) {
            if(!RaftConfig.CREW_RESCUE.get() || !player.isAlive())left=1;
            player.setData(DOWNED,left-1);
            if(left==1){
                player.kill(player.level());
                // Native loading protection can reject damage during a reconnect/dimension load.
                if(player.isAlive())player.setData(DOWNED,1);
                return;
            }
            player.setAirSupply(300);
            if(player.tickCount%20==0) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,40,4,false,false));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,4,false,false));
                player.sendOverlayMessage(Component.translatable("raft_ocean.crew.downed",(left+19)/20));
            }
        }
        Channel channel=CHANNELS.get(player.getUUID());if(channel==null)return;
        var target=player.level().getServer().getPlayerList().getPlayer(channel.target);
        if(target==null || !canRescue(player,target)) {
            CHANNELS.remove(player.getUUID());player.sendOverlayMessage(Component.translatable("raft_ocean.crew.rescue_interrupted"));return;
        }
        int ticks=channel.ticks+1;
        if(ticks>=60) {
            CHANNELS.remove(player.getUUID());target.setData(DOWNED,0);
            target.setHealth(Math.min(target.getMaxHealth(),RaftConfig.RESCUE_HEALTH.get().floatValue()));
            target.removeEffect(MobEffects.SLOWNESS);target.removeEffect(MobEffects.WEAKNESS);
            target.setInvulnerableTime(40);
            target.sendSystemMessage(Component.translatable("raft_ocean.crew.revived",player.getDisplayName()));
            player.sendOverlayMessage(Component.translatable("raft_ocean.crew.rescue_done"));
        } else {
            CHANNELS.put(player.getUUID(),new Channel(channel.target,ticks));
            if(ticks%10==0)player.sendOverlayMessage(Component.translatable("raft_ocean.crew.rescuing",(ticks*100)/60));
        }
    }
    public static void onLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id=event.getEntity().getUUID();CHANNELS.remove(id);CHANNELS.values().removeIf(c->c.target.equals(id));
    }
    public static void onAttack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        if(downed(event.getEntity()))event.setCanceled(true);
    }
    public static void onBreak(PlayerInteractEvent.LeftClickBlock event) {
        if(downed(event.getEntity()))event.setCanceled(true);
    }
    public static void onStop(net.neoforged.neoforge.event.server.ServerStoppedEvent event) { CHANNELS.clear(); }
    private CrewRescue() { }
}
