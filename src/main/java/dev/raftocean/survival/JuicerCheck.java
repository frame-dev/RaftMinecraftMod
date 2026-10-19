package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.api.*;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.menu.JuicerMenu;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Loaded four-food recipes, physical serving, private liquid and actual saved raft state. */
public final class JuicerCheck {
    private static int checks;
    private static final BlockPos CELL=new BlockPos(0,1,0);
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    private static RaftEntity fixture(ServerLevel level){
        var structure=new RaftStructure();require(structure.add(CELL,new RaftPiece(RaftPiece.Kind.ADDON,0,false,RaftJuicer.ID.toString()),RaftLayout.starter()),"Juicer supported footprint");
        var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);seed.putString("Structure",structure.encode());
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(8,level.getMaxY()-12,8);return raft;
    }
    private static RaftEntity restore(ServerLevel level,RaftEntity raft){
        var reporter=new ProblemReporter.Collector();var out=TagValueOutput.createWithContext(reporter,level.registryAccess());raft.saveWithoutId(out);
        var copy=new RaftEntity(ModContent.RAFT.get(),level);copy.load(TagValueInput.create(reporter,level.registryAccess(),out.buildResult()));
        require(reporter.isEmpty(),"Native juicer save has no codec errors");return copy;
    }
    private static void step(RaftMachineContext ctx,int seconds){for(int i=0;i<seconds;i++)RaftJuicer.CONTROLLER.tick(ctx);}
    public static int run(ServerLevel level){
        checks=0;boolean enabled=RaftConfig.JUICING.get(),bonus=RaftConfig.MEAL_BONUSES.get();double speed=RaftConfig.PROCESSING.get(),juicerSpeed=RaftConfig.JUICER_SPEED.get();
        var rafts=new ArrayList<RaftEntity>();var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"JuicerCheck"));
        boolean forced=level.setChunkForced(0,0,true);level.getChunkSource().tick(()->true,false);level.waitForEntities(new net.minecraft.world.level.ChunkPos(0,0),0);
        var area=new net.minecraft.world.phys.AABB(0,level.getMaxY()-16,0,16,level.getMaxY(),16);
        var priorDrops=new HashSet<UUID>();
        try{
            long deadline=System.nanoTime()+30_000_000_000L;
            level.getServer().managedBlock(()->{
                level.getChunkSource().tick(()->true,false);
                if(System.nanoTime()>deadline)throw new IllegalStateException("Juicer fixture chunk never became entity-ticking");
                return level.areEntitiesActuallyLoadedAndTicking(new net.minecraft.world.level.ChunkPos(0,0));
            });
            level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area).forEach(e->priorDrops.add(e.getUUID()));
            RaftConfig.JUICING.set(true);RaftConfig.PROCESSING.set(1.0);RaftConfig.JUICER_SPEED.set(1.0);RaftConfig.MEAL_BONUSES.set(true);
            var recipes=level.recipeAccess().getRecipes().stream().filter(h->h.value() instanceof JuicingRecipe).toList();
            require(recipes.size()==3,"All three native data-pack juices load");
            for(var holder:recipes){
                var recipe=(JuicingRecipe)holder.value();var raft=fixture(level);rafts.add(raft);var ctx=raft.machineContext(CELL).orElseThrow();var s=ctx.state();
                var ingredients=recipe.placementInfo().ingredients();require(ingredients.size()==4,"Juice recipe has four portions");
                for(int i=0;i<4;i++)s.setItem(i,ingredients.get(i).items().map(h->new ItemStack(h.value(),2)).findFirst().orElseThrow());
                var result=recipe.assemble(RaftJuicer.input(s));require(result.getCount()==1 && result.getItem() instanceof JuiceDrinkItem,"Recipe produces a single prepared drink");
                var battery=new ItemStack(Batteries.BASIC.get());Batteries.setCharge(battery,200);s.setItem(4,battery);
                player.setPos(ctx.worldPosition());var menu=new JuicerMenu(4,player.getInventory(),ctx);
                require(menu.stillValid(player) && menu.slots.size()==43,"Moving native menu exposes seven role slots plus player inventory");
                require(!menu.getSlot(6).mayPlace(result) && menu.getSlot(5).mayPlace(new ItemStack(RaftJuicer.GLASS.get()))
                        && !menu.getSlot(4).mayPlace(new ItemStack(Items.COAL)) && !RaftJuicer.CONTROLLER.canExtract(ctx,7,player)
                        && !RaftJuicer.CONTROLLER.canExtract(ctx,8,player),"Output, battery and private liquid are protected");
                step(ctx,10);require(s.getInt("progress")==0 && Batteries.charge(battery)==200 && s.getItem(0).getCount()==2,"Supplied juicer never starts or drains itself");
                var stranger=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"JuicerOther"));stranger.setPos(player.position());
                require(!menu.clickMenuButton(stranger,0) && menu.quickMoveStack(stranger,0).isEmpty(),"Foreign session cannot blend or take supplies");stranger.discard();
                menu.setCarried(new ItemStack(Items.STONE));require(!menu.clickMenuButton(player,0),"Carried-item blend refused");menu.setCarried(ItemStack.EMPTY);
                require(menu.clickMenuButton(player,0) && !menu.clickMenuButton(player,0) && !menu.clickMenuButton(player,88),"One explicit batch queues; duplicate and unknown controls refused");
                require(s.getItem(0).getCount()==2 && Batteries.charge(battery)==200,"Starting a batch consumes no materials or power");
                step(ctx,1);require(s.getInt("progress")==20 && Batteries.charge(battery)==199 && s.getItem(0).getCount()==2,"Powered step retains inputs until completion");
                require(menu.clickMenuButton(player,1),"Owner pauses active blending");step(ctx,3);
                require(s.getInt("progress")==20 && Batteries.charge(battery)==199,"Pause conserves power and work");
                var copy=restore(level,raft);rafts.add(copy);var restored=copy.machineContext(CELL).orElseThrow();var saved=restored.state();step(restored,1);
                require(saved.getInt("paused")==1 && saved.getInt("running")==1 && saved.getInt("progress")==20 && Batteries.charge(saved.getItem(4))==199,"Paused queued blend survives actual raft save/load");
                saved.setInt("paused",0);Batteries.setCharge(saved.getItem(4),0);step(restored,1);
                require(saved.getInt("progress")==20 && saved.getItem(0).getCount()==2,"Empty battery safely waits");Batteries.setCharge(saved.getItem(4),199);
                RaftConfig.JUICING.set(false);step(restored,1);require(saved.getInt("progress")==20 && Batteries.charge(saved.getItem(4))==199,"Disabled juicing retains charge and work");RaftConfig.JUICING.set(true);
                step(restored,(recipe.duration()+19)/20-1);
                require(ItemStack.matches(saved.getItem(8),result) && saved.getItem(6).isEmpty() && saved.getInt("running")==0 && saved.getItem(7).isEmpty(),"Completed liquid waits for separate serving without a glass");
                for(int i=0;i<4;i++)require(saved.getItem(i).getCount()==1,"One finished batch consumes exactly one portion per food pad");
                int charge=Batteries.charge(saved.getItem(4));require(charge==200-(recipe.duration()+19)/20,"Batch spends one charge per actual running second");step(restored,10);
                require(ItemStack.matches(saved.getItem(8),result) && Batteries.charge(saved.getItem(4))==charge && !RaftJuicer.start(restored),"Ready liquid blocks a second batch and idle drain");
                var readyRaft=restore(level,copy);rafts.add(readyRaft);var ready=readyRaft.machineContext(CELL).orElseThrow();
                require(ItemStack.matches(ready.state().getItem(8),result) && !RaftJuicer.serve(ready),"Unbottled drink survives reload and cannot be served without a glass");
                ready.state().setItem(5,new ItemStack(RaftJuicer.GLASS.get(),2));ready.state().setItem(6,new ItemStack(Items.STONE,64));
                require(!RaftJuicer.serve(ready) && ready.state().getItem(5).getCount()==2 && ItemStack.matches(ready.state().getItem(8),result),"Blocked output consumes neither glass nor liquid");
                ready.state().setItem(6,result.copyWithCount(result.getMaxStackSize()));
                require(!RaftJuicer.serve(ready) && ready.state().getItem(5).getCount()==2 && !ready.state().getItem(8).isEmpty(),"Full matching drink stack also preserves glass and liquid");
                ready.state().setItem(6,ItemStack.EMPTY);require(RaftJuicer.serve(ready) && ready.state().getItem(5).getCount()==1
                        && ready.state().getItem(8).isEmpty() && ItemStack.matches(ready.state().getItem(6),result),"One physical glass and one prepared portion make exactly one filled drink");
                require(!RaftJuicer.serve(ready) && ready.state().getItem(5).getCount()==1,"Repeated serving cannot duplicate drinks");
                RaftConfig.JUICER_SPEED.set(2.0);int beforeFast=Batteries.charge(ready.state().getItem(4));
                require(RaftJuicer.start(ready),"A second batch still requires an explicit Blend request");step(ready,(recipe.duration()+39)/40);
                require(!ready.state().getItem(8).isEmpty() && Batteries.charge(ready.state().getItem(4))==beforeFast-(recipe.duration()+39)/40,"Configured faster blending spends charge only for actual powered steps");
                require(RaftJuicer.serve(ready) && ready.state().getItem(6).getCount()==2 && ready.state().getItem(5).isEmpty(),"Second serving merges output and spends the second real glass");RaftConfig.JUICER_SPEED.set(1.0);
                player.setData(SurvivalContent.THIRST,0);player.removeAllEffects();
                var empty=result.copy().finishUsingItem(level,player);
                require(empty.is(RaftJuicer.GLASS.get()) && empty.getCount()==1 && Thirst.value(player)==RaftConfig.JUICE_HYDRATION.get(),"Drinking restores configured hydration and returns one reusable glass");
                require(!player.getActiveEffects().isEmpty(),"Prepared juice applies its original temporary bonus");
                player.setData(SurvivalContent.THIRST,19);RaftConfig.MEAL_BONUSES.set(false);player.removeAllEffects();result.copy().finishUsingItem(level,player);
                require(Thirst.value(player)==20 && player.getActiveEffects().isEmpty(),"Hydration is capped and disabled drink bonuses stay disabled");RaftConfig.MEAL_BONUSES.set(true);
                s.setInt("paused",0);s.setItem(0,new ItemStack(Items.STONE));step(ctx,1);
                require(s.getInt("running")==0 && s.getInt("progress")==0 && s.getItem(7).isEmpty() && Batteries.charge(battery)==199,"Changing a queued recipe cancels prior work without another charge");
                player.setPos(ctx.worldPosition());raft.setPos(raft.position().add(3,0,3));player.setPos(ctx.worldPosition());require(menu.stillValid(player),"Juicer screen follows translated raft");
                player.setPos(player.position().add(12,0,0));require(!menu.stillValid(player),"Walking away invalidates controls");
                int foodBefore=foodDrops(level,area),juiceBefore=juiceDrops(level,area);
                require(copy.removeStructuralPiece(CELL) && saved.getItem(7).isEmpty() && saved.getItem(8).isEmpty(),"Dismantling clears private liquid state");
                require(foodDrops(level,area)==foodBefore+4 && juiceDrops(level,area)==juiceBefore,"Actively tracked dismantling drops every remaining food but no free filled glass");
                require(!RaftJuicer.start(restored) && !RaftJuicer.serve(restored),"Detached juicer handles cannot restart or serve stale liquid");
            }
            return checks;
        }finally{
            rafts.forEach(RaftEntity::discard);player.discard();
            level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area).stream().filter(e->!priorDrops.contains(e.getUUID())).forEach(net.minecraft.world.entity.item.ItemEntity::discard);
            if(forced)level.setChunkForced(0,0,false);RaftConfig.JUICING.set(enabled);RaftConfig.PROCESSING.set(speed);RaftConfig.JUICER_SPEED.set(juicerSpeed);RaftConfig.MEAL_BONUSES.set(bonus);
        }
    }
    private static int foodDrops(ServerLevel level,net.minecraft.world.phys.AABB area){return level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area).stream().filter(e->RaftJuicer.isIngredient(e.getItem())).mapToInt(e->e.getItem().getCount()).sum();}
    private static int juiceDrops(ServerLevel level,net.minecraft.world.phys.AABB area){return level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area).stream().filter(e->e.getItem().getItem() instanceof JuiceDrinkItem).mapToInt(e->e.getItem().getCount()).sum();}
    private JuicerCheck() { }
}
