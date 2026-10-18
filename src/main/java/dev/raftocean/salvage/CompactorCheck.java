package dev.raftocean.salvage;

import com.mojang.authlib.GameProfile;
import dev.raftocean.api.*;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.Batteries;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native mixed-batch conservation, battery pauses, menu filtering and persisted ownership. */
public final class CompactorCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    public static int run(ServerLevel level){
        checks=0;boolean enabled=RaftConfig.RECYCLING.get();int seconds=RaftConfig.COMPACTOR_SECONDS.get();
        var raft=new RaftEntity(ModContent.RAFT.get(),level);RaftEntity restored=null;
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CompactorCheck"));
        try {
            RaftConfig.RECYCLING.set(true);RaftConfig.COMPACTOR_SECONDS.set(30);
            var pos=new BlockPos(0,1,0);var structure=new RaftStructure();
            require(structure.add(pos,new RaftPiece(RaftPiece.Kind.ADDON,0,false,SalvageCompactor.ID.toString()),RaftLayout.starter()),"Compactor fits supported foundation");
            var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);seed.putString("Structure",structure.encode());
            raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));raft.setPos(8,level.getMaxY()-12,8);
            var ctx=raft.machineContext(pos).orElseThrow();var state=ctx.state();var behavior=SalvageCompactor.CONTROLLER;
            player.setPos(ctx.worldPosition());var menu=new dev.raftocean.menu.CompactorMenu(3,player.getInventory(),ctx);
            require(menu.stillValid(player),"Installed moving-machine menu valid");
            require(SalvageCompactor.value(new ItemStack(Items.KELP))==1 && SalvageCompactor.value(new ItemStack(Items.OAK_PLANKS))==2
                    && SalvageCompactor.value(new ItemStack(Items.IRON_INGOT))==4,"Data-driven material tiers load");
            var named=new ItemStack(Items.IRON_INGOT);named.set(DataComponents.CUSTOM_NAME,Component.literal("Keep me"));
            require(SalvageCompactor.value(named)==0 && SalvageCompactor.value(new ItemStack(SalvageCompactor.BALE.get()))==0,"Valuables and currency cannot be recycled");
            require(!menu.getSlot(8).mayPlace(new ItemStack(SalvageCompactor.BALE.get())) && !menu.getSlot(7).mayPlace(new ItemStack(Items.COAL))
                    && menu.getSlot(7).mayPlace(new ItemStack(Batteries.BASIC.get())) && !menu.getSlot(0).mayPlace(named),"Native inventory rules protect input, battery and output");
            state.setItem(0,new ItemStack(Items.KELP,7));state.setItem(1,new ItemStack(Items.OAK_PLANKS,7));state.setItem(2,new ItemStack(Items.IRON_INGOT,5));
            var plan=SalvageCompactor.batch(state);require(!plan.isEmpty() && plan.values().stream().mapToInt(s->s.getCount()*SalvageCompactor.value(s)).sum()==40,"Mixed input plans exact points without overfill loss");
            var battery=new ItemStack(Batteries.BASIC.get());Batteries.setCharge(battery,100);state.setItem(7,battery);
            for(int i=0;i<10;i++)behavior.tick(ctx);
            require(state.getInt("progress")==10 && state.getItem(0).getCount()==7 && state.getItem(1).getCount()==7 && state.getItem(2).getCount()==5
                    && Batteries.charge(battery)==90 && state.getItem(8).isEmpty(),"Processing retains inputs and spends one charge per second");
            Batteries.setCharge(battery,0);behavior.tick(ctx);require(state.getInt("progress")==10 && state.getInt("active")==0,"Empty battery pauses without losing progress");
            Batteries.setCharge(battery,90);state.setItem(8,new ItemStack(SalvageCompactor.BALE.get(),4));behavior.tick(ctx);
            require(state.getInt("progress")==10 && Batteries.charge(battery)==90,"Four-bale buffer pauses without charge loss");
            state.setItem(8,new ItemStack(Items.STONE));behavior.tick(ctx);require(state.getInt("progress")==10 && Batteries.charge(battery)==90,"Foreign blocked output is never overwritten");
            state.setItem(8,ItemStack.EMPTY);RaftConfig.RECYCLING.set(false);behavior.tick(ctx);
            require(state.getInt("progress")==10 && Batteries.charge(battery)==90,"Disabled recycling preserves inputs, power and progress");RaftConfig.RECYCLING.set(true);
            require(menu.value(0)==10 && menu.value(1)==30 && menu.value(2)==90 && menu.value(5)==40,"Native menu publishes bounded progress, power and feed points");
            require(menu.clickMenuButton(player,0) && state.getInt("paused")==1,"Owner can pause the compactor");behavior.tick(ctx);
            require(state.getInt("progress")==10 && Batteries.charge(battery)==90 && menu.value(4)==5,"Manual pause preserves inventory, energy and status");
            require(!menu.clickMenuButton(player,99),"Unknown controls refused");
            menu.setCarried(new ItemStack(Items.STONE));require(!menu.clickMenuButton(player,0),"Carried-item control request refused");menu.setCarried(ItemStack.EMPTY);
            var other=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CompactorOther"));other.setPos(player.position());
            require(!menu.clickMenuButton(other,0) && menu.quickMoveStack(other,0).isEmpty(),"Another player cannot control or take from this session");other.discard();
            require(menu.clickMenuButton(player,0) && state.getInt("paused")==0,"Owner resumes saved work");
            player.getInventory().setItem(9,new ItemStack(Items.KELP,2));menu.quickMoveStack(player,9);
            require(state.getItem(0).getCount()==9 && player.getInventory().getItem(9).isEmpty(),"Shift-input reaches feed rather than battery or output");
            state.getItem(0).shrink(2);player.getInventory().setItem(9,new ItemStack(Batteries.BASIC.get()));
            menu.quickMoveStack(player,9);require(state.getItem(0).getCount()==7 && state.getItem(7)==battery,"Occupied battery socket cannot merge into materials");player.getInventory().clearContent();
            require(menu.clickMenuButton(player,0),"Pause before a real save");
            var reporter=new ProblemReporter.Collector();var saved=TagValueOutput.createWithContext(reporter,level.registryAccess());raft.saveWithoutId(saved);
            restored=new RaftEntity(ModContent.RAFT.get(),level);restored.load(TagValueInput.create(reporter,level.registryAccess(),saved.buildResult()));
            var copy=restored.machineContext(pos).orElseThrow();require(reporter.isEmpty() && copy.state().getInt("progress")==10 && Batteries.charge(copy.state().getItem(7))==90,"Native raft persistence retains batch and portable charge");
            behavior.tick(copy);require(copy.state().getInt("paused")==1 && copy.state().getInt("progress")==10 && Batteries.charge(copy.state().getItem(7))==90,"Saved manual pause survives reload without a processing step");
            copy.state().setInt("paused",0);
            for(int i=0;i<20;i++)behavior.tick(copy);
            require(copy.state().getItem(8).is(SalvageCompactor.BALE.get()) && copy.state().getItem(8).getCount()==1 && copy.state().getInt("progress")==0
                    && Batteries.charge(copy.state().getItem(7))==70,"Restored batch completes exactly once");
            int remaining=0;for(int i=0;i<7;i++)remaining+=SalvageCompactor.value(copy.state().getItem(i))*copy.state().getItem(i).getCount();
            require(remaining==1,"Completion consumes exactly forty of forty-one material points");
            behavior.tick(copy);require(copy.state().getItem(8).getCount()==1 && Batteries.charge(copy.state().getItem(7))==70,"Insufficient materials cannot duplicate a bale or burn power");
            require(state.getItem(8).isEmpty() && state.getItem(0).getCount()==7,"Independent restored machine cannot mutate source raft");
            raft.setPos(raft.position().add(4,0,4));player.setPos(ctx.worldPosition());require(menu.stillValid(player),"Inventory follows raft movement");
            player.setPos(player.position().add(12,0,0));require(!menu.stillValid(player),"Out-of-reach inventory closes");
            raft.discard();behavior.tick(ctx);require(state.getInt("progress")==10,"Detached contexts cannot process");
        } finally {
            raft.discard();if(restored!=null)restored.discard();player.discard();RaftConfig.RECYCLING.set(enabled);RaftConfig.COMPACTOR_SECONDS.set(seconds);
        }
        return checks;
    }
    private CompactorCheck() { }
}
