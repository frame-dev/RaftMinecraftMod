package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.api.*;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.Automation;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Installed addon fixtures exercise real utility graphs, batch commits and native menus. */
public final class RaftUtilityApiCheck {
    private static final BlockPos MACHINE=new BlockPos(0,1,0),GEN=new BlockPos(-1,1,0),WATER=new BlockPos(0,1,-1),FUEL=new BlockPos(1,1,0);
    private static final Identifier ID=Identifier.fromNamespaceAndPath("raft_test","utility_processor");
    private static int checks;
    private RaftUtilityApiCheck() { }
    public static int run(ServerLevel level) {
        checks=0;boolean automation=RaftConfig.AUTOMATION.get();int yield=RaftConfig.GENERATOR_ENERGY.get();
        var entities=new ArrayList<RaftEntity>();var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"UtilityApiCheck"));
        var box=new AABB(-.5,0,-.5,.5,.75,.5);
        RaftBuildables.register(new RaftBuildable(ID,()->Items.REDSTONE,()->Blocks.BRICKS,1,1,1,8,true,List.of(box),List.of(box),
                (raft,pos,user,hand)->net.minecraft.world.InteractionResult.PASS),new RaftMachineBehavior() {
            public Set<RaftUtility> utilityInputs() { return Set.of(RaftUtility.values()); }
            public Set<Integer> quickHarvestSlots() { return Set.of(1); }
            public int powerDemand(RaftMachineContext ctx) { return ctx.state().getInt("demand"); }
            public boolean canInsert(RaftMachineContext ctx,int slot,ItemStack stack) { return slot==0 && stack.is(Items.KELP); }
            public boolean canExtract(RaftMachineContext ctx,int slot,net.minecraft.world.entity.player.Player user) { return ctx.state().getInt("locked")==0; }
            public void tick(RaftMachineContext ctx) { }
        });
        String sources="-1,1,0,BIOFUEL_GENERATOR,0,0;0,1,-1,WATER_TANK,0,0;1,1,0,FUEL_TANK,0,0";
        try {
            RaftConfig.AUTOMATION.set(true);RaftConfig.GENERATOR_ENERGY.set(2000);
            var raft=fixture(level,sources,entities);var ctx=raft.machineContext(MACHINE).orElseThrow();var state=ctx.state();
            var gen=raft.storage(GEN);var water=raft.storage(WATER);var fuel=raft.storage(FUEL);
            gen.energy=5;water.cleanWater=3;fuel.setItem(0,new ItemStack(Automation.BIOFUEL.get(),2));
            var cost=new RaftUtilityCost(6,1,1);
            require(!RaftMachineUtilities.consume(ctx,cost) && gen.energy==5 && water.cleanWater==3 && fuel.getItem(0).getCount()==2,"Insufficient power cannot partially spend water/fuel");
            gen.energy=7;
            require(RaftMachineUtilities.canConsume(ctx,cost) && gen.energy==7 && water.cleanWater==3,"Resource preflight is read-only");
            require(RaftMachineUtilities.consume(ctx,cost) && gen.energy==1 && water.cleanWater==2 && fuel.getItem(0).getCount()==1,"Three-resource commit spends exact units");
            gen.energy=100;water.cleanWater=0;
            require(!RaftMachineUtilities.consume(ctx,new RaftUtilityCost(10,1,0)) && gen.energy==100,"Missing water preserves otherwise available energy");
            water.cleanWater=3;water.paused=true;
            require(!RaftMachineUtilities.consume(ctx,new RaftUtilityCost(0,1,0)) && water.cleanWater==3,"Paused source cannot be drained");water.paused=false;
            RaftConfig.AUTOMATION.set(false);
            require(!RaftMachineUtilities.consume(ctx,cost) && gen.energy==100,"Global automation disablement closes addon utility inputs");RaftConfig.AUTOMATION.set(true);
            var inputs=Map.of(0,new ItemStack(Items.KELP,2));var outputs=Map.of(1,new ItemStack(Items.DRIED_KELP,2));
            state.setItem(0,new ItemStack(Items.KELP,3));state.setItem(1,new ItemStack(Items.DRIED_KELP,63));
            require(!RaftMachineTransactions.tryProcess(ctx,inputs,outputs,cost) && state.getItem(0).getCount()==3 && gen.energy==100,"Output capacity preflight preserves every input and utility");
            state.setItem(1,new ItemStack(Items.DIAMOND));
            require(!RaftMachineTransactions.tryProcess(ctx,inputs,outputs,cost) && fuel.getItem(0).getCount()==1,"Incompatible output preserves biofuel");
            state.setItem(1,ItemStack.EMPTY);
            var named=new ItemStack(Items.KELP,3);named.set(DataComponents.CUSTOM_NAME,Component.literal("Private sample"));state.setItem(0,named);
            require(!RaftMachineTransactions.tryProcess(ctx,inputs,outputs,cost) && gen.energy==100,"Input matching preserves distinct item components");
            state.setItem(0,new ItemStack(Items.KELP,3));
            require(RaftMachineTransactions.tryProcess(ctx,inputs,outputs,cost) && state.getItem(0).getCount()==1 && state.getItem(1).getCount()==2
                    && gen.energy==94 && water.cleanWater==2 && fuel.getItem(0).isEmpty(),"Batch commits items and three utility types once");
            require(!RaftMachineTransactions.tryProcess(ctx,inputs,outputs,cost) && gen.energy==94 && state.getItem(1).getCount()==2,"Repeated insufficient batch cannot duplicate output");
            state.clearContent();state.setItem(0,new ItemStack(Items.KELP));
            require(RaftMachineTransactions.tryProcess(ctx,Map.of(0,new ItemStack(Items.KELP)),Map.of(0,new ItemStack(Items.DRIED_KELP)),RaftUtilityCost.NONE)
                    && state.getItem(0).is(Items.DRIED_KELP),"Same-slot processing uses staged input consumption");
            state.clearContent();player.getInventory().clearContent();player.setPos(ctx.worldPosition());
            var menu=RaftMachineMenus.inventory(80,player.getInventory(),ctx);
            state.setItem(1,new ItemStack(Items.DRIED_KELP,2));player.getInventory().setItem(9,new ItemStack(Items.DRIED_KELP,3));
            menu.quickMoveStack(player,9);
            require(state.getItem(1).getCount()==2 && player.getInventory().countItem(Items.DRIED_KELP)==3,"Native shift-click cannot merge inventory items into a read-only output");
            state.setItem(0,new ItemStack(Items.KELP,2));player.getInventory().clearContent();player.getInventory().setItem(9,new ItemStack(Items.KELP,3));
            menu.quickMoveStack(player,9);
            require(state.getItem(0).getCount()==5 && player.getInventory().countItem(Items.KELP)==0,"Native shift-click still merges permitted inputs");
            player.setShiftKeyDown(true);player.getInventory().clearContent();state.setInt("locked",1);
            RaftMachineMenus.quickHarvest(ctx,player,InteractionHand.MAIN_HAND);
            require(state.getItem(1).getCount()==2,"Addon extraction lock also protects quick harvest");state.setInt("locked",0);
            RaftMachineMenus.quickHarvest(ctx,player,InteractionHand.MAIN_HAND);
            require(state.getItem(1).isEmpty() && state.getItem(0).getCount()==5 && player.getInventory().countItem(Items.DRIED_KELP)==2,"Opt-in addon harvest collects only declared unlocked outputs");
            gen.energy=0;gen.fuelTicks=0;gen.setItem(0,new ItemStack(Automation.BIOFUEL.get(),2));state.setInt("demand",0);
            Automation.tick(raft);
            require(gen.getItem(0).getCount()==2 && gen.fuelTicks==0,"Idle addon never ignites a connected generator");
            state.setInt("demand",10);Automation.tick(raft);
            require(gen.energy==20 && gen.fuelTicks==1980 && gen.getItem(0).getCount()==1,"Advertised addon demand wakes normal twenty-energy generator production");
            require(RaftMachineUtilities.consume(ctx,new RaftUtilityCost(10,0,0)) && gen.energy==10,"Addon draws the generated energy");
            state.setInt("demand",0);Automation.tick(raft);require(gen.fuelTicks==1980,"Clearing demand preserves generator burn reserve");
            var isolated=fixture(level,"",entities);var isolatedCtx=isolated.machineContext(MACHINE).orElseThrow();
            require(!RaftMachineUtilities.consume(isolatedCtx,new RaftUtilityCost(1,0,0)) && gen.energy==10,"Different rafts cannot share utility resources");
            var wired=fixture(level,"-1,1,-1,BIOFUEL_GENERATOR,0,0;0,1,-1,POWER_CABLE,0,0",entities);
            var wiredCtx=wired.machineContext(MACHINE).orElseThrow();wired.storage(new BlockPos(-1,1,-1)).energy=5;
            require(RaftMachineUtilities.canConsume(wiredCtx,new RaftUtilityCost(5,0,0)),"Cable topology reaches distant producer");
            require(wired.removeStructuralPiece(new BlockPos(0,1,-1)) && !RaftMachineUtilities.canConsume(wiredCtx,new RaftUtilityCost(1,0,0)),"Removing cable invalidates cached utility topology");
            var loop=fixture(level,"-1,1,-1,BIOFUEL_GENERATOR,0,0;0,1,-1,POWER_CABLE,0,0;-1,1,0,POWER_CABLE,0,0",entities);
            var loopCtx=loop.machineContext(MACHINE).orElseThrow();loop.storage(new BlockPos(-1,1,-1)).energy=5;
            require(!RaftMachineUtilities.canConsume(loopCtx,new RaftUtilityCost(6,0,0)),"Touching one network on two faces never counts its energy twice");
            require(RaftMachineUtilities.acceptsAt(raft,MACHINE,RaftUtility.POWER)
                    && !RaftMachineUtilities.acceptsAt(raft,MACHINE.above(),RaftUtility.POWER),"Declaration lookup matches occupied machine cells only");
            var wideId=Identifier.fromNamespaceAndPath("raft_test","wide_utility_processor");
            var wideBox=new AABB(-.5,0,-.5,1.5,.75,.5);
            RaftBuildables.register(new RaftBuildable(wideId,()->Items.EMERALD,()->Blocks.BRICKS,2,1,1,8,true,List.of(wideBox),List.of(wideBox),
                    (owner,pos,user,hand)->net.minecraft.world.InteractionResult.PASS),new RaftMachineBehavior() {
                public Set<RaftUtility> utilityInputs() { return Set.of(RaftUtility.POWER); }
                public void tick(RaftMachineContext context) { }
            });
            for(int rotation=0;rotation<4;rotation++) {
                var piece=new RaftPiece(RaftPiece.Kind.ADDON,rotation,false,wideId.toString());
                var second=piece.occupiedCells(MACHINE).stream().filter(cell->!cell.equals(MACHINE)).findFirst().orElseThrow();
                var source=second.offset(second.getX(),0,second.getZ());
                var layout=RaftLayout.starter();require(layout.add(new BlockPos(source.getX(),0,source.getZ())),"Wide utility source foundation added");
                var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                seed.putString("Layout",layout.encode());seed.putString("Structure","0,1,0,ADDON,"+rotation+",0,"+wideId+";"+source.getX()+",1,"+source.getZ()+",BIOFUEL_GENERATOR,0,0");
                var wide=new RaftEntity(ModContent.RAFT.get(),level);wide.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));entities.add(wide);
                wide.storage(source).energy=3;var wideCtx=wide.machineContext(MACHINE).orElseThrow();
                require(RaftMachineUtilities.acceptsAt(wide,second,RaftUtility.POWER)
                        && RaftMachineUtilities.consume(wideCtx,new RaftUtilityCost(1,0,0)) && wide.storage(source).energy==2,
                        "Rotated wide footprint connects at its non-anchor cell");
                require(!RaftMachineUtilities.canConsume(wideCtx,new RaftUtilityCost(0,1,0)),"Undeclared resource input cannot draw a network");
            }
            state.clearContent();require(raft.removeStructuralPiece(MACHINE) && !ctx.isInstalled(),"Removing addon invalidates retained server handles");
            require(!RaftMachineUtilities.consume(ctx,new RaftUtilityCost(1,0,0)) && gen.energy==10,"Stale handle cannot spend surviving network energy");
            boolean rejected=false;try { new RaftUtilityCost(-1,0,0); } catch(IllegalArgumentException expected) { rejected=true; }
            require(rejected,"Negative costs cannot generate free resources");
        } finally {
            for(var raft:entities) {
                for(var pos:raft.pieces().keySet()) {
                    var storage=raft.storage(pos);if(storage!=null)storage.clearContent();
                    raft.machineContext(pos).ifPresent(ctx->ctx.state().clearContent());
                }
                raft.discard();
            }
            player.getInventory().clearContent();player.discard();RaftConfig.AUTOMATION.set(automation);RaftConfig.GENERATOR_ENERGY.set(yield);
        }
        return checks;
    }
    private static RaftEntity fixture(ServerLevel level,String pieces,List<RaftEntity> entities) {
        var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        seed.putString("Structure","0,1,0,ADDON,0,0,"+ID+(pieces.isEmpty()?"":";"+pieces));
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(8,level.getMaxY()+24,8);entities.add(raft);return raft;
    }
    private static void require(boolean ok,String message) { if(!ok)throw new IllegalStateException(message);checks++; }
}
