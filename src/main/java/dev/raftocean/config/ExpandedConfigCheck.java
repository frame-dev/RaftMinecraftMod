package dev.raftocean.config;

import com.mojang.authlib.GameProfile;
import dev.raftocean.entity.*;
import dev.raftocean.navigation.NavigationCheck;
import dev.raftocean.survival.*;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Exercises configurable transactions and propulsion with real registered items and entities. */
public final class ExpandedConfigCheck {
    private ExpandedConfigCheck() { }
    private static void require(boolean value,String message) {
        if(!value){throw new IllegalStateException(message);}
    }
    public static int run(ServerLevel level) {
        int checks=0;
        var cell=new BlockPos(1,1,0);
        var entities=new ArrayList<RaftEntity>();
        var speeds=List.of(RaftConfig.PROCESSING,RaftConfig.PURIFIER_SPEED,RaftConfig.GRILL_SPEED,
                RaftConfig.SMELTER_SPEED,RaftConfig.POT_SPEED,RaftConfig.CROP_SPEED,RaftConfig.TREE_SPEED,
                RaftConfig.MACHINE_FUEL,RaftConfig.ENGINE_FUEL,RaftConfig.ENGINE_THRUST,
                RaftConfig.SAIL_THRUST,RaftConfig.PADDLE_THRUST,RaftConfig.SPRINT_THIRST);
        var saved=new HashMap<ModConfigSpec.DoubleValue,Double>();speeds.forEach(v->saved.put(v,v.get()));
        int pieces=RaftConfig.PIECE_LIMIT.get(),light=RaftConfig.PLANT_LIGHT.get(),drink=RaftConfig.DRINK_RESTORE.get(),period=RaftConfig.THIRST_PERIOD.get();
        boolean sky=RaftConfig.PLANT_SKY.get(),thirst=RaftConfig.THIRST.get();
        var mode=RaftConfig.MODE.get();boolean peacefulThirst=RaftConfig.PEACEFUL_THIRST.get();
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"ExpandedConfigCheck"));
        try {
            speeds.forEach(v->v.set(1.0));RaftConfig.PROCESSING.set(2.0);
            RaftConfig.PLANT_LIGHT.set(0);RaftConfig.PLANT_SKY.set(false);
            var kinds=List.of(RaftPiece.Kind.PURIFIER,RaftPiece.Kind.GRILL,RaftPiece.Kind.SMELTER,
                    RaftPiece.Kind.COOKING_POT,RaftPiece.Kind.PLANTER,RaftPiece.Kind.TREE_PLANTER);
            var perMachine=List.of(RaftConfig.PURIFIER_SPEED,RaftConfig.GRILL_SPEED,RaftConfig.SMELTER_SPEED,
                    RaftConfig.POT_SPEED,RaftConfig.CROP_SPEED,RaftConfig.TREE_SPEED);
            for(int i=0;i<kinds.size();i++) {
                var kind=kinds.get(i);var raft=SurvivalCheck.create(level,kind);entities.add(raft);var storage=raft.storage(cell);
                storage.rawWater=1;storage.cleanWater=1;
                storage.setItem(1,new ItemStack(Items.OAK_PLANKS,4));
                switch(kind) {
                    case GRILL -> storage.setItem(0,new ItemStack(Items.COD));
                    case SMELTER -> storage.setItem(0,new ItemStack(ModContent.SCRAP.get()));
                    case COOKING_POT -> {
                        storage.setItem(0,new ItemStack(Items.SALMON));storage.setItem(1,new ItemStack(Items.CARROT));
                        storage.setItem(3,new ItemStack(Items.OAK_PLANKS));storage.setItem(6,new ItemStack(Items.BOWL));
                    }
                    case PLANTER, TREE_PLANTER -> {
                        storage.setItem(0,new ItemStack(kind==RaftPiece.Kind.PLANTER?Items.WHEAT_SEEDS:Items.OAK_SAPLING));
                        storage.setItem(1,new ItemStack(SurvivalContent.PLANTING_MIX.get()));
                    }
                    default -> { }
                }
                perMachine.get(i).set(1.5);SurvivalMachines.process(raft,cell,kind,level);
                require(storage.progress==60,"Independent machine speed multiplies global processing speed: "+kind);checks++;
                require(storage.cleanWater==1 && (kind!=RaftPiece.Kind.PURIFIER || storage.rawWater==1),"Speed does not prematurely consume water: "+kind);checks++;
                perMachine.get(i).set(1.0);
            }
            var coveredTree=entities.getLast();var treeStorage=coveredTree.storage(cell);
            var treeSave=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess());
            coveredTree.saveWithoutId(treeSave);treeSave.putString("Structure",treeSave.buildResult().getStringOr("Structure","")+";1,5,0,ROOF,0,0");
            coveredTree.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),treeSave.buildResult()));
            treeStorage=coveredTree.storage(cell);int treeProgress=treeStorage.progress;
            RaftConfig.PLANT_SKY.set(true);SurvivalMachines.process(coveredTree,cell,RaftPiece.Kind.TREE_PLANTER,level);
            require(treeStorage.progress==treeProgress && treeStorage.status.equals("light"),"Enabled sky requirement pauses a roofed planter");checks++;
            RaftConfig.PLANT_SKY.set(false);SurvivalMachines.process(coveredTree,cell,RaftPiece.Kind.TREE_PLANTER,level);
            require(treeStorage.progress>treeProgress,"Disabling sky requirement permits roofed growth with configured light threshold");checks++;
            var purifier=entities.getFirst();var tank=purifier.storage(cell);int capacity=tank.fuelCapacity;
            RaftConfig.MACHINE_FUEL.set(2.0);tank.fuelTicks=0;
            SurvivalMachines.process(purifier,cell,RaftPiece.Kind.PURIFIER,level);
            require(tank.fuelCapacity==capacity*2 && tank.fuelTicks==capacity*2-20,"Machine fuel efficiency extends new fuel and burns real ticks");checks++;
            RaftConfig.MACHINE_FUEL.set(.5);int reserve=tank.fuelTicks;
            SurvivalMachines.process(purifier,cell,RaftPiece.Kind.PURIFIER,level);
            require(tank.fuelCapacity==capacity*2 && tank.fuelTicks==reserve-20,"Changing fuel efficiency preserves already burning reserves");checks++;
            var structure=new RaftStructure();var layout=RaftLayout.starter();
            require(structure.add(cell,new RaftPiece(RaftPiece.Kind.CHEST,0,false),layout),"Limit fixture accepts first chest");checks++;
            require(structure.add(new BlockPos(-1,1,0),new RaftPiece(RaftPiece.Kind.CHEST,0,false),layout),"Limit fixture accepts a second saved chest");checks++;
            RaftConfig.PIECE_LIMIT.set(1);
            require(!structure.canAdd(new BlockPos(0,1,1),new RaftPiece(RaftPiece.Kind.CHEST,0,false),layout),"Configured piece cap rejects new placement preview");checks++;
            require(RaftStructure.decode(structure.encode(),layout).pieces().size()==2,"Lower piece cap keeps saved builds");checks++;
            RaftConfig.PIECE_LIMIT.set(pieces);
            var raft=NavigationCheck.create(level);entities.add(raft);raft.setCurrent(0,0);var engine=raft.storage(NavigationCheck.ENGINE);
            engine.enabled=true;engine.setItem(1,new ItemStack(Items.OAK_PLANKS,4));
            double original=raft.navigation().target(true).length();int engineCapacity=engine.fuelCapacity;
            RaftConfig.ENGINE_THRUST.set(.5);double reduced=raft.navigation().target(true).length();
            require(Math.abs(reduced-original*.5)<1e-9,"Engine strength scales directional thrust");checks++;
            RaftConfig.ENGINE_THRUST.set(0.0);reserve=engine.fuelTicks;
            require(raft.navigation().target(true).lengthSqr()==0 && engine.fuelTicks==reserve,"Zero engine strength pauses thrust and fuel use");checks++;
            RaftConfig.ENGINE_THRUST.set(1.0);RaftConfig.ENGINE_FUEL.set(2.0);engine.fuelTicks=0;raft.navigation().target(true);
            require(engine.fuelCapacity==engineCapacity*2,"Engine efficiency changes new fuel capacity");checks++;
            engine.enabled=false;RaftConfig.PADDLE_THRUST.set(0.0);
            require(!raft.navigation().stroke(new Vec3(1,0,0)),"Zero paddle strength rejects stroke");checks++;
            RaftConfig.PADDLE_THRUST.set(1.0);raft.navigation().stroke(new Vec3(1,0,0));
            require(raft.navigation().target(true).x>0,"Enabled paddle supplies directional impulse");checks++;
            double sail=dev.raftocean.navigation.RaftNavigation.sailDrive(300,300,1);
            RaftConfig.SAIL_THRUST.set(.5);
            require(Math.abs(dev.raftocean.navigation.RaftNavigation.sailDrive(300,300,1)-sail*.5)<1e-9,"Sail multiplier retains wind-dependent drive");checks++;
            RaftConfig.DRINK_RESTORE.set(3);player.setData(SurvivalContent.THIRST,8);Thirst.drink(player);
            require(Thirst.value(player)==11,"Custom cup hydration is applied");checks++;
            RaftConfig.THIRST.set(true);RaftConfig.PEACEFUL_THIRST.set(true);RaftConfig.MODE.set(RaftConfig.Mode.NORMAL);
            RaftConfig.THIRST_PERIOD.set(100);RaftConfig.SPRINT_THIRST.set(3.0);player.setSprinting(true);Thirst.tick(player,100);
            require(Thirst.value(player)==8,"Configured sprint drain removes three points");checks++;
        } finally {
            saved.forEach((value,old)->value.set(old));RaftConfig.PIECE_LIMIT.set(pieces);RaftConfig.PLANT_LIGHT.set(light);
            RaftConfig.PLANT_SKY.set(sky);RaftConfig.DRINK_RESTORE.set(drink);RaftConfig.THIRST_PERIOD.set(period);RaftConfig.THIRST.set(thirst);
            RaftConfig.MODE.set(mode);RaftConfig.PEACEFUL_THIRST.set(peacefulThirst);
            entities.forEach(raft->{raft.pieces().keySet().forEach(pos->{var storage=raft.storage(pos);if(storage!=null){storage.clearContent();}});raft.discard();});player.discard();
        }
        return checks;
    }
}
