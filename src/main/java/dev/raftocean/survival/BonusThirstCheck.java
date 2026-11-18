package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native drink consumption, reserve timing, save/load and configuration boundaries. */
public final class BonusThirstCheck {
    private static int checks;
    private BonusThirstCheck() { }
    private static void require(boolean value,String message) {
        if(!value)throw new IllegalStateException(message);
        checks++;
    }
    private static void reset(FakePlayer player,int reserve) {
        player.setData(SurvivalContent.THIRST,20);
        player.setData(SurvivalContent.BONUS_THIRST,reserve);
        player.getPersistentData().putInt("RaftThirstTicks",0);
        player.getPersistentData().putInt("RaftBonusThirstTicks",0);
        player.setSprinting(false);
    }
    public static int run(ServerLevel level) {
        checks=0;
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"BonusThirst"));
        var copy=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"SavedBonusThirst"));
        int grant=RaftConfig.JUICE_BONUS_HYDRATION.get(),multiplier=RaftConfig.BONUS_THIRST_MULTIPLIER.get(),
                period=RaftConfig.THIRST_PERIOD.get(),hydration=RaftConfig.JUICE_HYDRATION.get();
        boolean enabled=RaftConfig.THIRST.get(),effects=RaftConfig.MEAL_BONUSES.get(),peaceful=RaftConfig.PEACEFUL_THIRST.get();
        double sprint=RaftConfig.SPRINT_THIRST.get();
        var mode=RaftConfig.MODE.get();
        try {
            RaftConfig.THIRST.set(true);RaftConfig.MODE.set(RaftConfig.Mode.NORMAL);
            RaftConfig.JUICE_BONUS_HYDRATION.set(6);RaftConfig.BONUS_THIRST_MULTIPLIER.set(2);
            RaftConfig.JUICE_HYDRATION.set(12);RaftConfig.THIRST_PERIOD.set(100);
            RaftConfig.SPRINT_THIRST.set(2D);RaftConfig.MEAL_BONUSES.set(false);
            require(Thirst.reserve(player)==0,"New and legacy players start without bonus thirst");
            player.setData(SurvivalContent.THIRST,2);
            new ItemStack(SurvivalContent.FRESH_WATER.get()).finishUsingItem(level,player);
            require(Thirst.reserve(player)==0,"Ordinary fresh water never creates a juice reserve");
            player.setData(SurvivalContent.THIRST,10);
            var glass=new ItemStack(RaftJuicer.MELON.get()).finishUsingItem(level,player);
            require(glass.is(RaftJuicer.GLASS.get()) && Thirst.value(player)==20 && Thirst.reserve(player)==6,
                    "A real prepared drink restores hydration, adds reserve and returns its glass");
            require(player.getActiveEffects().isEmpty(),"Reserve works with temporary potion bonuses disabled");
            for(int drink=0;drink<4;drink++)new ItemStack(RaftJuicer.ORCHARD.get()).finishUsingItem(level,player);
            require(Thirst.reserve(player)==20 && Thirst.value(player)==20,"Repeated drinks cap each hydration meter at twenty");
            reset(player,0);RaftConfig.JUICE_BONUS_HYDRATION.set(0);
            new ItemStack(RaftJuicer.BERRY.get()).finishUsingItem(level,player);
            require(Thirst.reserve(player)==0,"Zero configured grant disables new juice reserves");
            RaftConfig.JUICE_BONUS_HYDRATION.set(6);

            reset(player,2);Thirst.tick(player,199);
            require(Thirst.reserve(player)==2 && Thirst.value(player)==20,"Bonus points last the entire configured longer period");
            Thirst.tick(player,1);
            require(Thirst.reserve(player)==1 && Thirst.value(player)==20,"Bonus thirst drains before ordinary hydration");
            Thirst.tick(player,200);
            require(Thirst.reserve(player)==0 && Thirst.value(player)==20,"An exact reserve boundary spends no extra ordinary hydration");
            Thirst.tick(player,100);
            require(Thirst.value(player)==19,"Ordinary depletion resumes when the reserve is empty");

            reset(player,1);reset(copy,1);
            Thirst.tick(player,350);
            for(int tick=0;tick<17;tick++)Thirst.tick(copy,20);
            Thirst.tick(copy,10);
            require(Thirst.value(player)==19 && Thirst.reserve(player)==0
                    && Thirst.value(player)==Thirst.value(copy) && Thirst.reserve(player)==Thirst.reserve(copy)
                    && player.getPersistentData().getIntOr("RaftThirstTicks",0)==copy.getPersistentData().getIntOr("RaftThirstTicks",0),
                    "Batched and ordinary tick depletion keep identical remainder timing");
            reset(player,1);player.getPersistentData().putInt("RaftThirstTicks",77);
            Thirst.tick(player,252);
            require(Thirst.value(player)==19 && player.getPersistentData().getIntOr("RaftThirstTicks",0)==29,
                    "Reserve expiration preserves earlier ordinary partial depletion");
            reset(player,2);player.setSprinting(true);Thirst.tick(player,100);
            require(Thirst.reserve(player)==1 && Thirst.value(player)==20,"Sprinting speeds reserve depletion by the configured multiplier");
            reset(player,2);Thirst.tick(player,113);
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            player.saveWithoutId(saved);
            copy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            require(Thirst.reserve(copy)==2 && copy.getPersistentData().getIntOr("RaftBonusThirstTicks",0)==113,
                    "Reserve points and partial progress survive native player saving");
            Thirst.tick(copy,87);
            require(Thirst.reserve(copy)==1 && Thirst.value(copy)==20,"A restored reserve continues from its saved boundary");
            var clone=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"BonusDeath"));
            try {
                clone.copyAttachmentsFrom(player,true);
                require(Thirst.reserve(clone)==0,"Death clears the bonus reserve");
                clone.copyAttachmentsFrom(player,false);
                require(Thirst.reserve(clone)==Thirst.reserve(player),"Non-death player replacement retains the reserve");
            }finally{clone.discard();}
            RaftConfig.THIRST.set(false);Thirst.tick(player,20);
            require(Thirst.reserve(player)==0 && Thirst.value(player)==20
                    && player.getPersistentData().getIntOr("RaftBonusThirstTicks",0)==0,"Disabling thirst clears both depletion timers and the reserve");
            RaftConfig.THIRST.set(true);reset(player,3);player.getAbilities().instabuild=true;Thirst.tick(player,20);
            require(Thirst.reserve(player)==0 && Thirst.value(player)==20,"Creative remains exempt from thirst");player.getAbilities().instabuild=false;
            RaftConfig.MODE.set(RaftConfig.Mode.PEACEFUL);RaftConfig.PEACEFUL_THIRST.set(false);reset(player,3);Thirst.tick(player,20);
            require(Thirst.reserve(player)==0 && Thirst.value(player)==20,"Peaceful thirst exemption clears unused reserve");
            RaftConfig.PEACEFUL_THIRST.set(true);reset(player,2);Thirst.tick(player,200);
            require(Thirst.reserve(player)==1,"Enabled Peaceful thirst still uses normal reserve timing");
            return checks;
        } finally {
            player.discard();copy.discard();
            RaftConfig.JUICE_BONUS_HYDRATION.set(grant);RaftConfig.BONUS_THIRST_MULTIPLIER.set(multiplier);
            RaftConfig.THIRST_PERIOD.set(period);RaftConfig.JUICE_HYDRATION.set(hydration);
            RaftConfig.THIRST.set(enabled);RaftConfig.MEAL_BONUSES.set(effects);RaftConfig.SPRINT_THIRST.set(sprint);
            RaftConfig.PEACEFUL_THIRST.set(peaceful);RaftConfig.MODE.set(mode);
        }
    }
}
