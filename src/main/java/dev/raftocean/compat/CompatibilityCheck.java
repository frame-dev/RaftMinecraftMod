package dev.raftocean.compat;

import dev.raftocean.api.RaftBuildables;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.survival.*;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Native capability transactions and optional integration regressions without third-party jars. */
public final class CompatibilityCheck {
    private static int checks;
    private static void require(boolean value,String message) {if(!value)throw new IllegalStateException(message);checks++;}
    public static int run(ServerLevel level) {
        checks=0;boolean enabled=RaftConfig.ENERGY_ITEMS.get();int rate=RaftConfig.BATTERY_CHARGE_RATE.get();
        var raft=SurvivalCheck.create(level,RaftPiece.Kind.BATTERY_CHARGER);
        var generator=SurvivalCheck.create(level,RaftPiece.Kind.BIOFUEL_GENERATOR);
        var cell=new BlockPos(1,1,0);var charger=raft.storage(cell);var bank=generator.storage(cell);
        try {
            RaftConfig.ENERGY_ITEMS.set(true);RaftConfig.BATTERY_CHARGE_RATE.set(50);
            var battery=new ItemStack(Batteries.BASIC.get());var nativeHandler=EnergyItems.handler(battery);
            require(nativeHandler!=null && nativeHandler.getCapacityAsLong()==1000,"Raft batteries expose NeoForge energy capability");
            try(var tx=Transaction.openRoot()){require(nativeHandler.insert(70,tx)==70,"Standard energy insertion accepts requested amount");}
            require(Batteries.charge(battery)==0,"Aborted standard charging rolls back native item data");
            try(var tx=Transaction.openRoot()){nativeHandler.insert(70,tx);tx.commit();}
            require(Batteries.charge(battery)==70 && nativeHandler.getAmountAsLong()==70,"Committed energy uses saved raft battery charge");
            try(var tx=Transaction.openRoot()){require(nativeHandler.extract(30,tx)==30,"External devices can draw raft battery energy");tx.commit();}
            require(Batteries.charge(battery)==40 && battery.copy().getComponents().equals(battery.getComponents()),"External extraction preserves saved item components");
            var copy=battery.copy();require(EnergyItems.handler(copy).getAmountAsLong()==40,"Copied or saved battery components retain external charge");
            var left=new SimpleEnergyHandler(1000);var right=new SimpleEnergyHandler(1000);
            bank.energy=100;EnergyItems.chargePorts(charger,List.of(bank),0,left,right);
            require(left.getAmountAsLong()==25 && right.getAmountAsLong()==25 && bank.energy==50,"Two foreign ports share exactly fifty wired FE");
            charger.paused=true;EnergyItems.chargePorts(charger,List.of(bank),1,left,right);
            require(bank.energy==50 && left.getAmountAsLong()==25,"Paused charger cannot consume energy");charger.paused=false;
            var rejecting=new SimpleEnergyHandler(1000,0,0);charger.setItem(0,new ItemStack(Automation.BIOFUEL.get(),2));bank.energy=0;
            EnergyItems.chargePorts(charger,List.of(bank),2,rejecting,rejecting);
            require(charger.getItem(0).getCount()==2 && charger.fuelTicks==0,"Rejecting foreign ports cannot consume biofuel");
            var limited=new SimpleEnergyHandler(1000,3,0);
            EnergyItems.chargePorts(charger,List.of(bank),3,limited,null);
            require(limited.getAmountAsLong()==6 && charger.getItem(0).getCount()==1
                    && charger.fuelTicks==RaftConfig.GENERATOR_ENERGY.get()-6,"Rate-limited foreign charging spends only accepted energy");
            int fuel=charger.fuelTicks;var full=new SimpleEnergyHandler(10,10,10,10);
            EnergyItems.chargePorts(charger,List.of(bank),4,full,null);
            require(charger.fuelTicks==fuel && charger.status.equals("full"),"Full foreign item preserves remaining fuel");
            require(!RaftBuildables.registerOptional("absent_raft_compat_test",()->{throw new AssertionError("Resolved absent mod");},()->{throw new AssertionError("Resolved absent behavior");}),"Missing mods never resolve their adapter classes");
            RaftConfig.ENERGY_ITEMS.set(false);
            require(EnergyItems.handler(battery)==null && !EnergyItems.compatible(new ItemStack(Items.STICK)),"Compatibility can be disabled and ordinary items are rejected");
            require(EnergyItems.compatible(battery),"Native batteries remain usable with external compatibility disabled");
            return checks;
        } finally {RaftConfig.ENERGY_ITEMS.set(enabled);RaftConfig.BATTERY_CHARGE_RATE.set(rate);raft.discard();generator.discard();}
    }
    private CompatibilityCheck() { }
}
