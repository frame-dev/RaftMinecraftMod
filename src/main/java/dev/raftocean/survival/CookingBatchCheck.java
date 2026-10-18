package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.menu.SurvivalMenu;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Real loaded recipes, deliberate batches, co-op controls and native save migration. */
public final class CookingBatchCheck {
    private static final BlockPos CELL=new BlockPos(1,1,0);
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    private static void step(RaftEntity raft,int seconds){for(int i=0;i<seconds;i++)SurvivalMachines.process(raft,CELL,RaftPiece.Kind.COOKING_POT,(ServerLevel)raft.level());}
    private static void supplies(RaftEntity raft){
        var s=raft.storage(CELL);s.setItem(0,new ItemStack(Items.SALMON,3));s.setItem(1,new ItemStack(Items.CARROT,3));
        s.setItem(6,new ItemStack(Items.BOWL,3));s.setItem(3,new ItemStack(Items.OAK_PLANKS,8));s.cleanWater=3;
    }
    private static RaftEntity restore(ServerLevel level,net.minecraft.nbt.CompoundTag tag){
        var raft=new RaftEntity(dev.raftocean.registry.ModContent.RAFT.get(),level);
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag));return raft;
    }
    public static int run(ServerLevel level){
        checks=0;boolean manual=RaftConfig.MANUAL_COOKING.get();double speed=RaftConfig.PROCESSING.get(),potSpeed=RaftConfig.POT_SPEED.get();
        var rafts=new ArrayList<RaftEntity>();
        try{
            RaftConfig.MANUAL_COOKING.set(true);RaftConfig.PROCESSING.set(1.0);RaftConfig.POT_SPEED.set(1.0);
            int recipes=0;
            for(var holder:level.recipeAccess().getRecipes())if(holder.value() instanceof ProcessingRecipe recipe && recipe.kind()==RaftPiece.Kind.COOKING_POT){
                recipes++;var raft=SurvivalCheck.create(level,RaftPiece.Kind.COOKING_POT);rafts.add(raft);var s=raft.storage(CELL);
                var foods=recipe.placementInfo().ingredients().stream().map(i->i.items().map(h->new ItemStack(h.value())).findFirst().orElseThrow())
                        .filter(i->!i.is(SurvivalContent.FRESH_WATER.get()) && !i.is(Items.BOWL)).toList();
                require(foods.size()==2,"Existing recipe keeps its two food inputs: "+holder.id());
                s.setItem(0,foods.get(0).copyWithCount(2));s.setItem(1,foods.get(1).copyWithCount(2));
                s.setItem(6,new ItemStack(Items.BOWL,2));s.setItem(3,new ItemStack(Items.OAK_PLANKS,8));s.cleanWater=2;
                step(raft,40);require(s.progress==0 && s.fuelTicks==0 && s.getItem(3).getCount()==8 && s.getItem(4).isEmpty(),"Supplied manual pot never starts itself");
                require(SurvivalMachines.startCooking(s,level) && !SurvivalMachines.startCooking(s,level),"Exactly one recipe queues; duplicate start is refused");
                require(s.getItem(0).getCount()==2 && s.getItem(6).getCount()==2 && s.cleanWater==2 && s.fuelTicks==0,"Cook click consumes no items, water or fuel");
                step(raft,(recipe.duration()+19)/20);
                var input=net.minecraft.world.item.crafting.CraftingInput.of(4,1,List.of(foods.get(0),foods.get(1),new ItemStack(SurvivalContent.FRESH_WATER.get()),new ItemStack(Items.BOWL)));
                require(ItemStack.matches(s.getItem(4),recipe.assemble(input)) && !s.cookingRequested && s.status.equals("cook_done"),"Loaded recipe produces one correct completed meal");
                require(s.getItem(0).getCount()==1 && s.getItem(1).getCount()==1 && s.getItem(6).getCount()==1 && s.cleanWater==1,"One batch consumes one set of ingredients and bowl");
                int fuel=s.fuelTicks;step(raft,40);
                require(s.getItem(4).getCount()==1 && s.fuelTicks==fuel && s.cleanWater==1,"Spare supplies never trigger a second batch or burn reserve");
                require(SurvivalMachines.startCooking(s,level),"Explicit second Cook queues the next batch");step(raft,40);
                require(s.getItem(4).getCount()==2 && s.getItem(6).isEmpty(),"Second batch merges safely into the output");
            }
            require(recipes>=5,"All current loaded cooking-pot recipes covered");
            var raft=SurvivalCheck.create(level,RaftPiece.Kind.COOKING_POT);rafts.add(raft);supplies(raft);var s=raft.storage(CELL);
            var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CookingControlCheck"));player.setPos(raft.stationPosition(CELL,RaftPiece.Kind.COOKING_POT));
            var menu=new SurvivalMenu(1,player.getInventory(),RaftPiece.Kind.COOKING_POT,s);
            var stranger=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CookingStranger"));stranger.setPos(player.position());
            require(!menu.clickMenuButton(stranger,2) && !s.cookingRequested,"Foreign menu owner cannot start a batch");
            menu.setCarried(new ItemStack(Items.STONE));require(!menu.clickMenuButton(player,2),"Cook rejects clicks while carrying an item");menu.setCarried(ItemStack.EMPTY);
            require(menu.clickMenuButton(player,2) && menu.cookingRequested(),"Authorized native Cook button synchronizes queued state");step(raft,3);
            require(s.progress>0 && s.getItem(0).getCount()==3,"Active batch progresses without consuming foods early");
            require(menu.clickMenuButton(player,1),"Pause button works during a batch");int progress=s.progress,burn=s.fuelTicks;step(raft,10);
            require(s.progress==progress && s.fuelTicks==burn && s.cookingRequested,"Paused batch preserves progress, intent and fuel");
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());raft.saveWithoutId(saved);
            var copy=restore(level,saved.buildResult());rafts.add(copy);var loaded=copy.storage(CELL);
            require(loaded.cookingRequested && loaded.paused && loaded.progress==progress && loaded.fuelTicks==burn,"Paused queued batch survives actual raft save/load");
            loaded.paused=false;step(copy,40);require(!loaded.cookingRequested && loaded.getItem(4).getCount()==1 && loaded.getItem(0).getCount()==2,"Reloaded batch finishes exactly once");
            var old=saved.buildResult().copy();old.getListOrEmpty("StationStorage").getCompoundOrEmpty(0).remove("CookingRequested");
            var migrated=restore(level,old);rafts.add(migrated);require(migrated.storage(CELL).cookingRequested,"Older in-progress saves migrate as one queued batch");
            var idleTag=old.copy();idleTag.getListOrEmpty("StationStorage").getCompoundOrEmpty(0).putInt("Progress",0);
            var idle=restore(level,idleTag);rafts.add(idle);require(!idle.storage(CELL).cookingRequested,"Older idle saves do not start a new batch");
            s.paused=false;s.setItem(0,new ItemStack(Items.COOKED_COD,3));s.setItem(1,new ItemStack(Items.POTATO,3));step(raft,1);
            require(!s.cookingRequested && s.progress==0 && s.status.equals("cook_changed"),"Switching recipes cancels intent instead of reusing paid cooking time");
            int remaining=s.fuelTicks;step(raft,3);require(s.fuelTicks==remaining && s.status.equals("cook_changed"),"Recipe-change feedback persists without hidden fuel burn");
            s.setItem(6,ItemStack.EMPTY);require(!SurvivalMachines.startCooking(s,level) && !s.cookingRequested,"Missing bowl cannot queue food");s.setItem(6,new ItemStack(Items.BOWL,3));
            s.setItem(4,new ItemStack(Items.STONE,64));require(!SurvivalMachines.startCooking(s,level) && s.status.equals("output"),"Blocked output cannot queue a batch");s.setItem(4,ItemStack.EMPTY);
            s.cleanWater=0;s.setItem(2,new ItemStack(SurvivalContent.FRESH_WATER.get()));s.setItem(5,new ItemStack(SurvivalContent.EMPTY_CUP.get(),16));
            require(!SurvivalMachines.startCooking(s,level) && s.status.equals("cup") && s.getItem(2).getCount()==1,"Blocked cup return cannot consume a physical cup on Cook");
            s.setItem(5,ItemStack.EMPTY);require(SurvivalMachines.startCooking(s,level),"Unblocking cup return permits a new batch");step(raft,40);
            require(s.getItem(2).isEmpty() && s.getItem(5).is(SurvivalContent.EMPTY_CUP.get()) && s.getItem(4).getCount()==1,"Manual batch preserves the reusable cup exchange");
            s.getItem(4).setCount(0);s.cleanWater=2;s.cookingRequested=false;RaftConfig.MANUAL_COOKING.set(false);step(raft,80);
            require(s.getItem(4).getCount()==2 && s.getItem(6).isEmpty(),"Automatic compatibility mode still processes consecutive batches");
            return checks;
        }finally{
            for(var raft:rafts){raft.pieces().keySet().forEach(p->raft.storage(p).clearContent());raft.discard();}
            RaftConfig.MANUAL_COOKING.set(manual);RaftConfig.PROCESSING.set(speed);RaftConfig.POT_SPEED.set(potSpeed);
        }
    }
    private CookingBatchCheck() { }
}
