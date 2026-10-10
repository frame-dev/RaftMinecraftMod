package dev.raftocean.survival;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.menu.SurvivalMenu;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;

/** Native saved items, two-socket charging, closed circuits, pause and conservation regressions. */
public final class BatteryCheck {
    private static final BlockPos CELL=new BlockPos(1,1,0);
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    public static int run(ServerLevel level) {
        int checks=0;var entities=new ArrayList<RaftEntity>();
        int rate=RaftConfig.BATTERY_CHARGE_RATE.get(),yield=RaftConfig.GENERATOR_ENERGY.get(),cost=RaftConfig.PURIFIER_ENERGY.get(),duration=RaftConfig.ELECTRIC_PURIFY_TICKS.get();
        boolean enabled=RaftConfig.AUTOMATION.get();double speed=RaftConfig.PROCESSING.get();
        var ocean=new BlockPos(1,level.getMaxY()-8,0);var previous=level.getBlockState(ocean);
        try {
            RaftConfig.AUTOMATION.set(true);RaftConfig.BATTERY_CHARGE_RATE.set(20);RaftConfig.GENERATOR_ENERGY.set(2000);
            RaftConfig.PURIFIER_ENERGY.set(8);RaftConfig.ELECTRIC_PURIFY_TICKS.set(100);RaftConfig.PROCESSING.set(1D);
            var basic=new ItemStack(Batteries.BASIC.get());var advanced=new ItemStack(Batteries.ADVANCED.get());
            require(Batteries.charge(basic)==0 && Batteries.capacity(basic)==1000 && Batteries.capacity(advanced)==4000 && basic.getMaxStackSize()==1,"Crafted cells start empty, have distinct capacities and cannot stack");checks++;
            CustomData.update(DataComponents.CUSTOM_DATA,basic,tag -> {tag.putInt("RaftBatteryCharge",-10);tag.putString("OtherModData","keep");});
            require(Batteries.charge(basic)==0,"Malformed negative charge cannot generate energy");checks++;
            Batteries.setCharge(basic,100000);require(Batteries.charge(basic)==1000 && basic.get(DataComponents.CUSTOM_DATA).copyTag().getString("OtherModData").orElseThrow().equals("keep"),"Charge clamps to capacity and preserves unrelated item data");checks++;
            Batteries.setCharge(basic,0);
            var charger=SurvivalCheck.create(level,RaftPiece.Kind.BATTERY_CHARGER);entities.add(charger);var storage=charger.storage(CELL);
            storage.setItem(0,new ItemStack(Automation.BIOFUEL.get(),3));Automation.tick(charger,0);
            require(storage.getItem(0).getCount()==3 && storage.fuelTicks==0,"Empty charging station never burns biofuel");checks++;
            storage.setItem(1,basic);storage.setItem(2,advanced);Automation.tick(charger,0);
            require(Batteries.charge(basic)==10 && Batteries.charge(advanced)==10 && storage.getItem(0).getCount()==2 && storage.fuelTicks==1980,"Standalone charger consumes one fuel and splits exactly twenty energy between sockets");checks++;
            storage.paused=true;Automation.tick(charger,1);
            require(Batteries.charge(basic)==10 && storage.fuelTicks==1980 && storage.status.equals("off"),"Pause preserves cell charge and unspent biofuel energy");checks++;
            storage.paused=false;RaftConfig.AUTOMATION.set(false);Automation.tick(charger,2);
            require(Batteries.charge(basic)==10 && storage.fuelTicks==1980,"Global automation switch stops battery charging");checks++;
            RaftConfig.AUTOMATION.set(true);
            for(int i=1;i<250;i++)Automation.tick(charger,i);
            require(Batteries.charge(basic)==1000 && Batteries.charge(advanced)==4000 && storage.getItem(0).isEmpty() && storage.fuelTicks==1000,"Full batteries conserve all five thousand stored energy and remaining one thousand fuel energy");checks++;
            Automation.tick(charger,251);require(storage.fuelTicks==1000 && storage.status.equals("full"),"Full charging station stops and retains remaining fuel");checks++;
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());charger.saveWithoutId(saved);
            var restored=new RaftEntity(ModContent.RAFT.get(),level);entities.add(restored);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            require(Batteries.charge(restored.storage(CELL).getItem(1))==1000 && Batteries.charge(restored.storage(CELL).getItem(2))==4000 && restored.storage(CELL).fuelTicks==1000,"Native raft save/load retains both cell components and charger fuel");checks++;
            var generator=SurvivalCheck.create(level,RaftPiece.Kind.BIOFUEL_GENERATOR);entities.add(generator);var supply=generator.storage(CELL);supply.energy=7;
            var consumer=SurvivalCheck.create(level,RaftPiece.Kind.ELECTRIC_PURIFIER);entities.add(consumer);var receiver=consumer.storage(CELL);
            var cell=new ItemStack(Batteries.BASIC.get());Batteries.setCharge(cell,2);receiver.setItem(1,cell);
            require(!Batteries.spend(List.of(supply),receiver,10) && supply.energy==7 && Batteries.charge(cell)==2,"Underpowered wired-plus-cell step spends nothing");checks++;
            require(Batteries.spend(List.of(supply),receiver,8) && supply.energy==0 && Batteries.charge(cell)==1,"Wired power is preferred and cell supplies only the exact shortfall");checks++;
            receiver.paused=true;require(!Batteries.spend(List.of(supply),receiver,1) && Batteries.charge(cell)==1,"Paused consumer never drains its battery");checks++;
            receiver.paused=false;Batteries.setCharge(cell,40);level.setBlock(ocean,Blocks.WATER.defaultBlockState(),3);
            for(int i=0;i<5;i++)Automation.tick(consumer,i);
            require(receiver.cleanWater==1 && Batteries.charge(cell)==0 && receiver.getItem(1).is(Batteries.BASIC.get()),"A disconnected purifier produces one cup using exactly forty energy without destroying an empty cell");checks++;
            Automation.tick(consumer,6);require(receiver.cleanWater==1 && receiver.status.equals("power"),"Depleted portable power stops production");checks++;
            supply.energy=13;Batteries.setCharge(basic,0);storage.setItem(2,ItemStack.EMPTY);storage.fuelTicks=0;storage.setItem(0,ItemStack.EMPTY);
            Batteries.tickCharger(storage,List.of(supply),0);
            require(Batteries.charge(basic)==13 && supply.energy==0,"Charging accepts scarce wired power without losing the partial amount");checks++;
            RaftConfig.BATTERY_CHARGE_RATE.set(1);Batteries.setCharge(basic,0);Batteries.setCharge(advanced,0);storage.setItem(2,advanced);supply.energy=2;
            Batteries.tickCharger(storage,List.of(supply),0);Batteries.tickCharger(storage,List.of(supply),1);
            require(Batteries.charge(basic)==1 && Batteries.charge(advanced)==1 && supply.energy==0,"One-energy setting alternates sockets fairly");checks++;
            RaftConfig.BATTERY_CHARGE_RATE.set(20);
            var adjacent=new BlockPos(0,1,0);
            var wiredRaft=fixture(level,Map.of(CELL,RaftPiece.Kind.BATTERY_CHARGER,adjacent,RaftPiece.Kind.BIOFUEL_GENERATOR));entities.add(wiredRaft);
            var wiredCharger=wiredRaft.storage(CELL);var wiredGenerator=wiredRaft.storage(adjacent);
            wiredCharger.setItem(0,new ItemStack(Automation.BIOFUEL.get()));wiredGenerator.setItem(0,new ItemStack(Automation.BIOFUEL.get()));
            var wiredBattery=new ItemStack(Batteries.BASIC.get());wiredCharger.setItem(1,wiredBattery);Automation.tick(wiredRaft,0);
            require(Batteries.charge(wiredBattery)==20 && wiredGenerator.fuelTicks==1980 && wiredGenerator.energy==0 && wiredCharger.getItem(0).getCount()==1,
                    "Actual connected generator charges a cell while preserving charger-local biofuel");checks++;
            Batteries.setCharge(wiredBattery,1000);Automation.tick(wiredRaft,1);
            require(wiredGenerator.fuelTicks==1980 && wiredCharger.getItem(0).getCount()==1,"Full batteries generate no circuit demand or fuel burn");checks++;
            var pipeRaft=fixture(level,Map.of(CELL,RaftPiece.Kind.BATTERY_CHARGER,adjacent,RaftPiece.Kind.FUEL_TANK));entities.add(pipeRaft);
            pipeRaft.storage(adjacent).setItem(0,new ItemStack(Automation.BIOFUEL.get()));
            var fueledBattery=new ItemStack(Batteries.BASIC.get());pipeRaft.storage(CELL).setItem(1,fueledBattery);Automation.tick(pipeRaft,0);
            require(pipeRaft.storage(adjacent).getItem(0).isEmpty() && Batteries.charge(fueledBattery)==20 && pipeRaft.storage(CELL).fuelTicks==1980,
                    "Fuel network delivers a canister that the standalone charger converts conservatively");checks++;
            var garden=fixture(level,Map.of(CELL,RaftPiece.Kind.SPRINKLER,adjacent,RaftPiece.Kind.PLANTER));entities.add(garden);
            var gardenCell=new ItemStack(Batteries.BASIC.get());Batteries.setCharge(gardenCell,2000);garden.storage(CELL).setItem(1,gardenCell);garden.storage(CELL).cleanWater=1;
            Automation.tick(garden,0);
            require(garden.storage(adjacent).cleanWater==1 && garden.storage(CELL).cleanWater==0 && Batteries.charge(gardenCell)==1000-RaftConfig.SPRINKLER_ENERGY.get(),
                    "Unwired sprinkler irrigates using only its inserted rechargeable cell");checks++;
            var roles=SurvivalMenu.layout(RaftPiece.Kind.BATTERY_CHARGER);
            require(roles.get(1).label().equals("battery") && roles.get(2).label().equals("battery") && !roles.get(2).output(),"Both charger sockets allow removal at any charge state");checks++;
            List<List<Item>> ingredients=List.of(
                List.of(CraftingExpansion.item("copper_wire"),CraftingExpansion.item("copper_wire"),Items.COPPER_INGOT,CraftingExpansion.item("metal_plate"),ModContent.SCRAP.get(),CraftingExpansion.item("binding_paste")),
                List.of(CraftingExpansion.item("metal_plate"),CraftingExpansion.item("metal_plate"),CraftingExpansion.item("copper_wire"),CraftingExpansion.item("copper_wire"),Items.COPPER_INGOT,Items.COPPER_INGOT,Items.GLASS,CraftingExpansion.item("binding_paste")),
                List.of(CraftingExpansion.item("metal_plate"),CraftingExpansion.item("metal_plate"),CraftingExpansion.item("copper_wire"),CraftingExpansion.item("copper_wire"),ModContent.SCRAP_FASTENERS.get(),Items.GLASS,ModContent.TIMBER_BRACE.get()));
            var outputs=List.of(Batteries.BASIC.get(),Batteries.ADVANCED.get(),ModContent.structuralItem(RaftPiece.Kind.BATTERY_CHARGER));
            for(int i=0;i<3;i++) {
                var grid=new ArrayList<ItemStack>();ingredients.get(i).forEach(item -> grid.add(new ItemStack(item)));while(grid.size()<9)grid.add(ItemStack.EMPTY);
                var input=CraftingInput.of(3,3,grid);var result=level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,level).orElseThrow().value().assemble(input);
                require(result.is(outputs.get(i)) && result.getCount()==1 && Batteries.charge(result)==0,"Loaded battery/charger crafting produces a single new uncharged item");checks++;
            }
            return checks;
        } finally {
            RaftConfig.AUTOMATION.set(enabled);RaftConfig.BATTERY_CHARGE_RATE.set(rate);RaftConfig.GENERATOR_ENERGY.set(yield);
            RaftConfig.PURIFIER_ENERGY.set(cost);RaftConfig.ELECTRIC_PURIFY_TICKS.set(duration);RaftConfig.PROCESSING.set(speed);
            level.setBlock(ocean,previous,3);entities.forEach(RaftEntity::discard);
        }
    }
    private static RaftEntity fixture(ServerLevel level,Map<BlockPos,RaftPiece.Kind> kinds) {
        var structure=new RaftStructure();kinds.forEach((pos,kind) -> require(structure.add(pos,new RaftPiece(kind,0,false),RaftLayout.starter()),"Battery fixture support"));
        var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);seed.putString("Structure",structure.encode());
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(.5,level.getMaxY()-8,.5);return raft;
    }
    private BatteryCheck() { }
}
