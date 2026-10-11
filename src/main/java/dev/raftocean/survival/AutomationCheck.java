package dev.raftocean.survival;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;

/** Native recipes, disconnected circuits, resource conservation, irrigation and save/load regression checks. */
public final class AutomationCheck {
    private AutomationCheck() { }
    private static void require(boolean value,String message) { if(!value){throw new IllegalStateException(message);} }
    private static RaftEntity fixture(ServerLevel level,Map<BlockPos,RaftPiece.Kind> kinds) {
        var structure=new RaftStructure();
        kinds.forEach((p,k)->require(structure.add(p,new RaftPiece(k,0,false),RaftLayout.starter()),"Utility support "+k));
        var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);seed.putString("Structure",structure.encode());
        var raft=new RaftEntity(ModContent.RAFT.get(),level);
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));raft.setPos(.5,100,.5);return raft;
    }
    public static int run(ServerLevel level) {
        int checks=0;var entities=new ArrayList<RaftEntity>();
        var a=new BlockPos(-1,1,-1);var b=new BlockPos(0,1,-1);var c=new BlockPos(1,1,-1);
        var tank=new BlockPos(-1,1,0);var pipe=new BlockPos(0,1,0);var engine=new BlockPos(1,1,0);
        var waterTank=new BlockPos(-1,1,1);var waterPipe=new BlockPos(0,1,1);var planter=new BlockPos(1,1,1);
        var oceanCell=new BlockPos(1,100,-1);var previous=level.getBlockState(oceanCell);
        boolean enabled=RaftConfig.AUTOMATION.get();
        try {
            RaftConfig.AUTOMATION.set(true);
            var refine=SurvivalCheck.create(level,RaftPiece.Kind.BIOREFINER);entities.add(refine);
            var cell=new BlockPos(1,1,0);var inventory=refine.storage(cell);
            inventory.setItem(0,new ItemStack(Items.POTATO,2));inventory.setItem(1,new ItemStack(Items.HONEYCOMB,2));
            for(int i=0;i<29;i++){SurvivalMachines.tick(refine);}
            require(inventory.getItem(0).getCount()==2 && inventory.getItem(2).isEmpty(),"Biorefiner inputs stay until commit");checks++;
            SurvivalMachines.tick(refine);
            require(inventory.getItem(0).getCount()==1 && inventory.getItem(1).getCount()==1
                    && inventory.getItem(2).is(Automation.BIOFUEL.get()) && inventory.getItem(2).getCount()==2,"Honeycomb recipe commits two biofuel");checks++;
            inventory.setItem(2,new ItemStack(Automation.BIOFUEL.get(),64));int progress=inventory.progress;
            SurvivalMachines.tick(refine);
            require(inventory.getItem(0).getCount()==1 && inventory.progress==progress,"Full refiner output preserves ingredients");checks++;
            inventory.setItem(2,ItemStack.EMPTY);inventory.setItem(1,new ItemStack(Items.SUGAR));
            for(int i=0;i<45;i++){SurvivalMachines.tick(refine);}
            require(inventory.getItem(2).getCount()==1 && inventory.getItem(0).isEmpty(),"Renewable sugar recipe produces one fuel");checks++;
            var kinds=new HashMap<BlockPos,RaftPiece.Kind>();
            kinds.put(a,RaftPiece.Kind.BIOFUEL_GENERATOR);kinds.put(b,RaftPiece.Kind.POWER_CABLE);kinds.put(c,RaftPiece.Kind.ELECTRIC_PURIFIER);
            kinds.put(tank,RaftPiece.Kind.FUEL_TANK);kinds.put(pipe,RaftPiece.Kind.FUEL_PIPE);kinds.put(engine,RaftPiece.Kind.ENGINE);
            kinds.put(waterTank,RaftPiece.Kind.WATER_TANK);kinds.put(waterPipe,RaftPiece.Kind.WATER_PIPE);kinds.put(planter,RaftPiece.Kind.PLANTER);
            var raft=fixture(level,kinds);entities.add(raft);
            var source=raft.storage(tank);var motor=raft.storage(engine);source.setItem(0,new ItemStack(Automation.BIOFUEL.get(),5));
            motor.setItem(1,new ItemStack(Items.OAK_PLANKS));
            require(!Automation.transferFuel(source,motor,1) && source.getItem(0).getCount()==5,"Pipes cannot replace incompatible engine fuel");checks++;
            motor.setItem(1,ItemStack.EMPTY);require(Automation.transferFuel(source,motor,1) && source.getItem(0).getCount()==4
                    && motor.getItem(1).getCount()==1,"Fuel transfer conserves exactly one canister");checks++;
            motor.setItem(1,new ItemStack(Automation.BIOFUEL.get(),64));
            require(!Automation.transferFuel(source,motor,1) && source.getItem(0).getCount()==4,"Full engine preserves tank fuel");checks++;
            motor.setItem(1,ItemStack.EMPTY);
            var reservoir=raft.storage(waterTank);var soil=raft.storage(planter);reservoir.cleanWater=64;soil.cleanWater=4;
            require(Automation.transferWater(reservoir,soil,4)==0 && reservoir.cleanWater==64,"Full consumer preserves water");checks++;
            soil.cleanWater=0;require(Automation.transferWater(reservoir,soil,4)==1 && reservoir.cleanWater==63 && soil.cleanWater==1,"Water transfer conserves one cup");checks++;
            var generator=raft.storage(a);generator.energy=3;var second=raft.storage(tank);second.energy=2;
            require(!Automation.spend(List.of(generator,second),6) && generator.energy==3 && second.energy==2,"Insufficient aggregate power never partially spends");checks++;
            require(Automation.spend(List.of(generator,second),4) && generator.energy==0 && second.energy==1,"Distributed power spends exact total");checks++;
            second.energy=0;
            require(Automation.components(raft.pieces(),Automation.Network.POWER).size()==1,"Cable joins generator and purifier");checks++;
            var disconnected=new HashMap<>(raft.pieces());disconnected.remove(b);
            require(Automation.components(disconnected,Automation.Network.POWER).size()==2,"Removing cable separates power components");checks++;
            disconnected.put(b,new RaftPiece(RaftPiece.Kind.FUEL_PIPE,0,false));
            require(Automation.components(disconnected,Automation.Network.POWER).size()==2,"Fuel pipes cannot bridge power");checks++;
            level.setBlock(oceanCell,Blocks.WATER.defaultBlockState(),3);
            for(int i=0;i<5;i++){Automation.tick(raft);}
            require(raft.storage(c).cleanWater==1 && generator.energy>=0 && generator.fuelTicks>0,"Connected generator powers real water purification");checks++;
            require(motor.getItem(1).is(Automation.BIOFUEL.get()),"Fuel network supplies engine");checks++;
            motor.enabled=true;raft.navigation().target(true);
            require(motor.fuelCapacity==Math.clamp((int)(3200*RaftConfig.ENGINE_FUEL.get()),1,200000) && motor.fuelTicks==motor.fuelCapacity-1,"Engine burns biofuel like coal");checks++;
            reservoir.cleanWater=64;generator.energy=1700;
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());raft.saveWithoutId(saved);
            var restored=new RaftEntity(ModContent.RAFT.get(),level);entities.add(restored);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            require(restored.storage(waterTank).cleanWater==64 && restored.storage(a).energy==1700
                    && restored.storage(a).fuelTicks==generator.fuelTicks,"Tank capacity, energy and remaining fuel persist");checks++;
            var sprinkler=fixture(level,Map.of(b,RaftPiece.Kind.BIOFUEL_GENERATOR,pipe,RaftPiece.Kind.SPRINKLER,
                    engine,RaftPiece.Kind.PLANTER,tank,RaftPiece.Kind.WATER_TANK));entities.add(sprinkler);
            sprinkler.storage(b).energy=10;sprinkler.storage(pipe).cleanWater=2;
            Automation.tick(sprinkler);
            require(sprinkler.storage(engine).cleanWater==1 && sprinkler.storage(pipe).cleanWater==1
                    && sprinkler.storage(b).energy==10-RaftConfig.SPRINKLER_ENERGY.get(),"Sprinkler conserves one cup and exact energy");checks++;
            sprinkler.storage(b).energy=0;int remaining=sprinkler.storage(pipe).cleanWater;
            Automation.tick(sprinkler);
            require(sprinkler.storage(engine).cleanWater==1 && sprinkler.storage(pipe).cleanWater==remaining,"Unpowered sprinkler preserves water");checks++;
            sprinkler.storage(b).setItem(0,new ItemStack(Automation.BIOFUEL.get(),2));sprinkler.storage(engine).cleanWater=4;
            Automation.tick(sprinkler);
            require(sprinkler.storage(b).getItem(0).getCount()==2,"Generator does not burn fuel without irrigation demand");checks++;
            RaftConfig.AUTOMATION.set(false);int fuelCount=source.getItem(0).getCount();
            Automation.tick(raft);require(source.getItem(0).getCount()==fuelCount && generator.energy==1700,"Disabled networks preserve resources");checks++;
            RaftConfig.AUTOMATION.set(true);
            level.setBlock(oceanCell,Blocks.AIR.defaultBlockState(),3);
            generator.fuelTicks=generator.fuelCapacity=0;generator.energy=0;
            generator.setItem(0,new ItemStack(Automation.BIOFUEL.get(),2));source.clearContent();raft.storage(c).cleanWater=0;
            Automation.tick(raft,0);
            require(generator.getItem(0).getCount()==2 && generator.energy==0 && raft.storage(c).status.equals("water"),
                    "Dry purifier never creates fuel demand");checks++;
            sprinkler.storage(engine).cleanWater=0;sprinkler.storage(pipe).cleanWater=0;sprinkler.storage(b).energy=0;
            Automation.tick(sprinkler,0);
            require(sprinkler.storage(b).getItem(0).getCount()==2 && sprinkler.storage(b).fuelTicks==0,
                    "Dry sprinkler never ignites a biofuel canister");checks++;
            inventory.setItem(0,new ItemStack(Items.POTATO));inventory.setItem(1,new ItemStack(Items.SUGAR));
            inventory.setItem(2,ItemStack.EMPTY);SurvivalMachines.tick(refine);int savedProgress=inventory.progress;
            inventory.paused=true;SurvivalMachines.tick(refine);
            require(inventory.progress==savedProgress && inventory.getItem(0).getCount()==1 && inventory.status.equals("off"),
                    "Paused refinery preserves real recipe ingredients and progress");checks++;
            var pauseSave=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());refine.saveWithoutId(pauseSave);
            var pausedCopy=new RaftEntity(ModContent.RAFT.get(),level);entities.add(pausedCopy);
            pausedCopy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),pauseSave.buildResult()));
            require(pausedCopy.storage(cell).paused && pausedCopy.storage(cell).progress==savedProgress
                    && pausedCopy.storage(cell).duration==inventory.duration,"Pause and duration survive entity save/load");checks++;
            inventory.paused=false;SurvivalMachines.tick(refine);
            require(inventory.progress>savedProgress,"Resume continues existing refinery batch");checks++;
            reservoir.paused=true;reservoir.cleanWater=5;soil.cleanWater=0;
            require(Automation.transferWater(reservoir,soil,4)==0 && reservoir.cleanWater==5,"Paused water tank preserves reserves");checks++;
            reservoir.paused=false;soil.paused=true;
            require(Automation.transferWater(reservoir,soil,4)==0 && soil.cleanWater==0,"Paused planter rejects automatic refill");checks++;
            source.paused=true;source.setItem(0,new ItemStack(Automation.BIOFUEL.get(),3));motor.setItem(1,ItemStack.EMPTY);
            require(!Automation.transferFuel(source,motor,1) && source.getItem(0).getCount()==3,"Paused fuel tank stops distribution");checks++;
            generator.energy=100;generator.paused=true;
            require(!Automation.spend(List.of(generator),4) && generator.energy==100,"Paused generator cannot power consumers");checks++;
            var fair=fixture(level,Map.of(pipe,RaftPiece.Kind.WATER_TANK,tank,RaftPiece.Kind.PLANTER,engine,RaftPiece.Kind.PLANTER));entities.add(fair);
            fair.storage(pipe).cleanWater=1;Automation.tick(fair,0);
            fair.storage(pipe).cleanWater=1;Automation.tick(fair,1);
            require(fair.storage(tank).cleanWater==1 && fair.storage(engine).cleanWater==1,
                    "Scarce water reaches both consumers over consecutive seconds");checks++;
            var fairPower=fixture(level,Map.of(pipe,RaftPiece.Kind.BIOFUEL_GENERATOR,tank,RaftPiece.Kind.SPRINKLER,
                    engine,RaftPiece.Kind.SPRINKLER,b,RaftPiece.Kind.PLANTER));entities.add(fairPower);
            fairPower.storage(tank).cleanWater=2;fairPower.storage(engine).cleanWater=2;
            fairPower.storage(pipe).energy=RaftConfig.SPRINKLER_ENERGY.get();Automation.tick(fairPower,0);
            fairPower.storage(pipe).energy=RaftConfig.SPRINKLER_ENERGY.get();Automation.tick(fairPower,1);
            require(fairPower.storage(tank).cleanWater==1 && fairPower.storage(engine).cleanWater==1,
                    "Scarce power rotates between sprinkler consumers");checks++;
            var fairFuel=fixture(level,Map.of(pipe,RaftPiece.Kind.FUEL_TANK,tank,RaftPiece.Kind.ENGINE,engine,RaftPiece.Kind.ENGINE));entities.add(fairFuel);
            fairFuel.storage(pipe).setItem(0,new ItemStack(Automation.BIOFUEL.get()));Automation.tick(fairFuel,0);
            fairFuel.storage(pipe).setItem(0,new ItemStack(Automation.BIOFUEL.get()));Automation.tick(fairFuel,1);
            require(fairFuel.storage(tank).getItem(1).getCount()==1 && fairFuel.storage(engine).getItem(1).getCount()==1,
                    "Scarce fuel reaches both engines over consecutive seconds");checks++;
            var limited=fixture(level,Map.of(pipe,RaftPiece.Kind.PLANTER,tank,RaftPiece.Kind.WATER_TANK,
                    engine,RaftPiece.Kind.WATER_TANK));entities.add(limited);
            limited.storage(tank).cleanWater=2;limited.storage(engine).cleanWater=2;Automation.tick(limited,0);
            require(limited.storage(pipe).cleanWater==1 && limited.storage(tank).cleanWater+limited.storage(engine).cleanWater==3,
                    "Multiple water sources respect the one-cup recipient transfer rate");checks++;
            var irrigation=fixture(level,Map.of(pipe,RaftPiece.Kind.SPRINKLER,tank,RaftPiece.Kind.PLANTER,
                    engine,RaftPiece.Kind.PLANTER,b,RaftPiece.Kind.BIOFUEL_GENERATOR));entities.add(irrigation);
            irrigation.storage(pipe).cleanWater=4;irrigation.storage(b).energy=100;
            Automation.tick(irrigation,0);Automation.tick(irrigation,1);
            require(irrigation.storage(tank).cleanWater==1 && irrigation.storage(engine).cleanWater==1,
                    "Sprinkler rotates irrigation between thirsty planters");checks++;
            return checks;
        } finally {RaftConfig.AUTOMATION.set(enabled);level.setBlock(oceanCell,previous,3);entities.forEach(RaftEntity::discard);}
    }
}
