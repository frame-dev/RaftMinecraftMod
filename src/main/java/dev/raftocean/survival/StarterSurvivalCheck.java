package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.SurvivalMenu;
import dev.raftocean.progression.Research;
import dev.raftocean.registry.ModContent;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Proves the starter water and smelter paths need no prior metalwork or furnace. */
public final class StarterSurvivalCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    public static int run(ServerLevel level){
        checks=0;var cell=new BlockPos(1,1,0);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"StarterSurvival"));
        player.setData(Research.SCHEMA,1);
        var purifier=SurvivalCheck.create(level,RaftPiece.Kind.BASIC_PURIFIER);
        var rack=SurvivalCheck.create(level,RaftPiece.Kind.DRYING_RACK);
        var clock=level.dimensionType().defaultClock().orElseThrow();
        long time=level.clockManager().getInstance(clock).totalTicks();
        float rain=level.getRainLevel(1F),thunder=level.getThunderLevel(1F);
        double speed=dev.raftocean.config.RaftConfig.PROCESSING.get(),waterSpeed=dev.raftocean.config.RaftConfig.PURIFIER_SPEED.get();
        var roof=BlockPos.containing(rack.stationPosition(cell,RaftPiece.Kind.DRYING_RACK)).above(2);
        level.getChunkAt(roof);var oldRoof=level.getBlockState(roof);
        try{
            dev.raftocean.config.RaftConfig.PROCESSING.set(1D);dev.raftocean.config.RaftConfig.PURIFIER_SPEED.set(1D);
            require(Research.allowed(player,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.BASIC_PURIFIER)))
                    && Research.allowed(player,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.DRYING_RACK))),"Starter purifier and drying rack need no research");
            var water=purifier.storage(cell);player.setPos(purifier.stationPosition(cell,RaftPiece.Kind.BASIC_PURIFIER));
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SurvivalContent.SALT_WATER.get()));
            require(CupTransfers.use(purifier,cell,RaftPiece.Kind.BASIC_PURIFIER,player,InteractionHand.MAIN_HAND).consumesAction()
                    && water.rawWater==1 && player.getMainHandItem().is(SurvivalContent.EMPTY_CUP.get()),"Basic purifier accepts seawater and returns its cup");
            water.setItem(1,new ItemStack(Items.OAK_PLANKS,8));
            for(int i=0;i<19;i++)SurvivalMachines.process(purifier,cell,RaftPiece.Kind.BASIC_PURIFIER,level);
            require(water.cleanWater==0 && water.rawWater==1,"Basic purifier does not finish before twenty seconds");
            SurvivalMachines.process(purifier,cell,RaftPiece.Kind.BASIC_PURIFIER,level);
            require(water.cleanWater==1 && water.rawWater==0,"Basic purifier makes fresh water using starter plank fuel");
            require(CupTransfers.use(purifier,cell,RaftPiece.Kind.BASIC_PURIFIER,player,InteractionHand.MAIN_HAND).consumesAction()
                    && player.getMainHandItem().is(SurvivalContent.FRESH_WATER.get()) && water.cleanWater==0,"Basic purifier delivers one filled cup without loss");
            var inventory=rack.storage(cell);inventory.setItem(0,new ItemStack(CraftingExpansion.item("damp_brick"),2));
            var menu=new SurvivalMenu(19,player.getInventory(),RaftPiece.Kind.DRYING_RACK,inventory);
            require(menu.accepts(0,new ItemStack(CraftingExpansion.item("damp_brick"))) && !menu.accepts(0,new ItemStack(Items.IRON_INGOT))
                    && menu.layout().values().stream().noneMatch(role->role.label().equals("fuel")),"Drying UI filters wet bricks and needs no fuel slot");
            level.clockManager().setTotalTicks(clock,6000);level.updateSkyBrightness();level.setRainLevel(0);level.setThunderLevel(0);
            for(int i=0;i<149;i++)SurvivalMachines.process(rack,cell,RaftPiece.Kind.DRYING_RACK,level);
            require(inventory.progress==2980 && inventory.getItem(2).isEmpty() && inventory.getItem(0).getCount()==2,"Drying takes 150 sunny seconds and preserves inputs until completion");
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());rack.saveWithoutId(saved);
            var copy=SurvivalCheck.create(level,RaftPiece.Kind.DRYING_RACK);
            copy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            require(copy.storage(cell).progress==2980 && copy.storage(cell).getItem(0).getCount()==2,"Pending brick drying survives native raft saving");copy.discard();
            level.setRainLevel(1);SurvivalMachines.process(rack,cell,RaftPiece.Kind.DRYING_RACK,level);
            require(inventory.progress==2980 && inventory.getItem(0).getCount()==2,"Rain pauses drying without consuming the brick");
            level.setRainLevel(0);level.setThunderLevel(0);level.clockManager().setTotalTicks(clock,13000);level.updateSkyBrightness();
            SurvivalMachines.process(rack,cell,RaftPiece.Kind.DRYING_RACK,level);require(inventory.progress==2980,"Night pauses drying without resetting its progress");
            level.clockManager().setTotalTicks(clock,6000);level.updateSkyBrightness();level.setBlock(roof,Blocks.STONE.defaultBlockState(),3);
            SurvivalMachines.process(rack,cell,RaftPiece.Kind.DRYING_RACK,level);require(inventory.progress==2980,"Solid overhead cover pauses drying");
            level.setBlock(roof,oldRoof,3);inventory.setItem(2,new ItemStack(CraftingExpansion.item("dried_brick"),64));
            SurvivalMachines.process(rack,cell,RaftPiece.Kind.DRYING_RACK,level);require(inventory.progress==2980 && inventory.getItem(0).getCount()==2,"A full output cannot consume a drying brick");
            inventory.setItem(2,ItemStack.EMPTY);SurvivalMachines.process(rack,cell,RaftPiece.Kind.DRYING_RACK,level);
            require(inventory.getItem(2).is(CraftingExpansion.item("dried_brick")) && inventory.getItem(2).getCount()==1
                    && inventory.getItem(0).getCount()==1 && inventory.cleanWater==0 && inventory.fuelTicks==0,"Sun drying yields one dry brick without fuel or water");
            var sample=new ItemStack(CraftingExpansion.item("dried_brick"),2);
            require(Research.research(player,sample) && sample.getCount()==1,"A sun-dried brick satisfies the brick research material");
            require(Research.research(player,new ItemStack(Items.CLAY_BALL)),"Clay is the remaining smelter research sample");
            int project=java.util.stream.IntStream.range(0,Research.TOPICS.length).filter(i->Research.TOPICS[i].id().equals("smelter")).findFirst().orElseThrow();
            require(Research.study(player,project) && Research.allowed(player,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.SMELTER))),"Smelter research unlocks without firing bricks or researching metal");
            var d=new ItemStack(CraftingExpansion.item("dried_brick"));var p=new ItemStack(Items.OAK_PLANKS);
            var input=net.minecraft.world.item.crafting.CraftingInput.of(3,3,java.util.List.of(d,d,d,d,ItemStack.EMPTY,d,p,d,p));
            require(level.recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,input,level).orElseThrow().value().assemble(input)
                    .is(ModContent.structuralItem(RaftPiece.Kind.SMELTER)),"Six dry bricks and two planks craft the first smelter without metal fittings");
            anchorAndGrill(level);
            return checks;
        }finally{
            dev.raftocean.config.RaftConfig.PROCESSING.set(speed);dev.raftocean.config.RaftConfig.PURIFIER_SPEED.set(waterSpeed);
            level.clockManager().setTotalTicks(clock,time);level.updateSkyBrightness();level.setRainLevel(rain);level.setThunderLevel(thunder);level.setBlock(roof,oldRoof,3);
            player.discard();purifier.discard();rack.discard();
        }
    }
    private static void anchorAndGrill(ServerLevel level) {
        var cell=new BlockPos(1,1,0);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"StarterAnchor"));
        var anchor=SurvivalCheck.create(level,RaftPiece.Kind.DISPOSABLE_ANCHOR);
        var grill=SurvivalCheck.create(level,RaftPiece.Kind.BASIC_GRILL);
        try {
            require(Research.allowed(player,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.DISPOSABLE_ANCHOR)))
                    && Research.allowed(player,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.BASIC_GRILL))),"Starter anchor and grill need no research");
            player.setPos(anchor.stationPosition(cell,RaftPiece.Kind.DISPOSABLE_ANCHOR));
            var menu=new dev.raftocean.menu.NavigationMenu(20,player.getInventory(),anchor,cell,RaftPiece.Kind.DISPOSABLE_ANCHOR);
            require(!anchor.navigation().anchored() && !menu.clickMenuButton(player,1),"Unused stone anchor permits drift and refuses steering controls");
            require(menu.clickMenuButton(player,0) && anchor.navigation().anchored(),"Lowering the stone anchor stops the raft");
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());anchor.saveWithoutId(saved);
            var restored=SurvivalCheck.create(level,RaftPiece.Kind.DISPOSABLE_ANCHOR);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            require(restored.navigation().anchored() && restored.storage(cell).enabled,"A deployed disposable anchor persists across saving");restored.discard();
            require(menu.clickMenuButton(player,0) && !anchor.navigation().anchored() && !anchor.pieces().containsKey(cell),"Releasing consumes the disposable anchor and resumes drift");
            require(!menu.clickMenuButton(player,0),"Stale anchor menu cannot reuse the consumed anchor");
            var inv=grill.storage(cell);var foodMenu=new SurvivalMenu(21,player.getInventory(),RaftPiece.Kind.BASIC_GRILL,inv);
            require(foodMenu.heated() && !foodMenu.waterMachine() && foodMenu.accepts(0,new ItemStack(Items.COD))
                    && !foodMenu.accepts(0,new ItemStack(Items.RAW_IRON)),"Basic grill uses food inputs, plank fuel and no water or ore smelting");
            inv.setItem(0,new ItemStack(Items.COD));SurvivalMachines.process(grill,cell,RaftPiece.Kind.BASIC_GRILL,level);
            require(inv.getItem(0).getCount()==1 && inv.getItem(2).isEmpty() && inv.progress==0,"Unfueled grill preserves food");
            inv.setItem(1,new ItemStack(Items.OAK_PLANKS,16));
            for(int i=0;i<40;i++)SurvivalMachines.process(grill,cell,RaftPiece.Kind.BASIC_GRILL,level);
            require(inv.getItem(0).isEmpty() && inv.getItem(2).is(Items.COOKED_COD) && inv.getItem(2).getCount()==1,"Basic grill cooks exactly one fish using wood fuel");
            for(var kind:java.util.List.of(dev.raftocean.salvage.DebrisEntity.Kind.CRATE,dev.raftocean.salvage.DebrisEntity.Kind.BARREL)) {
                boolean found=false;
                for(int i=0;i<128;i++) {
                    var debris=dev.raftocean.salvage.DebrisEntity.create(level,kind,net.minecraft.world.phys.Vec3.ZERO);
                    for(var stack:debris.loot())if(stack.is(Items.COBBLESTONE)) {
                        require(stack.getCount()>=2 && stack.getCount()<=16,"Ocean stone loot preserves two-to-four item rolls when repeated entries merge");found=true;
                    }
                    debris.discard();
                }
                require(found,"Floating crates and barrels supply starter-anchor cobblestone: "+kind);
            }
        } finally {anchor.discard();grill.discard();}
    }
    private StarterSurvivalCheck(){}
}
