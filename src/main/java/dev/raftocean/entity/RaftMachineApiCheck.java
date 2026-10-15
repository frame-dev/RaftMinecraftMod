package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.api.*;
import dev.raftocean.registry.ModContent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native machine ownership, persistence, transactions and moving-menu regressions. */
public final class RaftMachineApiCheck {
    private static int checks;
    private RaftMachineApiCheck() { }
    public static int run(ServerLevel level) {
        checks=0;var loaded=new AtomicInteger();var removed=new AtomicInteger();
        var id=Identifier.fromNamespaceAndPath("raft_test","processor");var pos=new BlockPos(0,1,0);
        var box=new AABB(-.5,0,-.5,.5,.75,.5);
        var type=new RaftBuildable(id,()->Items.COPPER_INGOT,()->Blocks.BRICKS,1,1,1,8,true,
                List.of(box),List.of(box),(raft,anchor,player,hand)->InteractionResult.PASS);
        RaftBuildables.register(type,new RaftMachineBehavior() {
            public int tickInterval() { return 2; }
            public void loaded(RaftMachineContext ctx) { loaded.incrementAndGet();ctx.state().setInt("duration",3);ctx.state().setInt("loaded_yaw",Math.round(ctx.raft().getYRot())); }
            public void removed(RaftMachineContext ctx) {
                removed.incrementAndGet();
                require(ctx.raft().machineContext(ctx.anchor()).isEmpty(),"Removal callbacks cannot recreate machine ownership");
            }
            public boolean canInsert(RaftMachineContext ctx,int slot,ItemStack stack) { return slot!=1; }
            public void tick(RaftMachineContext ctx) {
                var state=ctx.state();state.setInt("active",0);
                if(state.getInt("paused")!=0 || !state.getItem(0).is(Items.KELP) || !state.getItem(1).isEmpty())return;
                state.setInt("active",1);int progress=state.getInt("progress")+1;
                if(progress==3) { state.getItem(0).shrink(1);state.setItem(1,new ItemStack(Items.DRIED_KELP));progress=0; }
                state.setInt("progress",progress);
            }
        });
        var layout=RaftLayout.starter();var structure=new RaftStructure();
        require(structure.add(pos,new RaftPiece(RaftPiece.Kind.ADDON,0,false,id.toString()),layout),"Machine footprint");
        var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        seed.putString("Layout",layout.encode());seed.putString("Structure",structure.encode());
        seed.putFloat("HullYaw",37);
        var raft=new RaftEntity(ModContent.RAFT.get(),level);
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(8,level.getMaxY()+24,8);level.addFreshEntity(raft);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"MachineApiCheck"));
        RaftEntity restored=null;
        try {
            require(loaded.get()==1,"Legacy addon layout creates state and calls loaded");
            var ctx=raft.machineContext(pos).orElseThrow();var state=ctx.state();
            require(state.getInt("loaded_yaw")==37,"Load hook sees restored hull coordinates");
            state.setItem(0,new ItemStack(Items.KELP,4));state.setString("owner","native-test");state.setInt("large",100000);
            player.setPos(ctx.worldPosition().add(0,0,-2));
            var menu=RaftMachineMenus.inventory(8,player.getInventory(),ctx);
            require(menu.stillValid(player),"Machine menu near raft");
            require(menu.getSlot(0).mayPlace(new ItemStack(Items.KELP)) && !menu.getSlot(1).mayPlace(new ItemStack(Items.KELP)),"Server slot rules");
            var delta=new Vec3(24,0,24);raft.setPos(raft.position().add(delta));player.setPos(player.position().add(delta));
            require(menu.stillValid(player),"Menu follows translated raft");
            player.setPos(player.position().add(12,0,0));require(!menu.stillValid(player),"Menu closes out of range");
            raft.setPos(8,level.getMaxY()+24,8);player.setPos(ctx.worldPosition().add(0,0,-2));
            require(raft.machineContextAt(pos).orElseThrow().state()==state,"Occupied-cell lookup resolves owner");
            state.setInt("old",7);state.removeInt("old");state.setString("old","legacy");state.removeString("old");
            require(!state.hasInt("old") && !state.hasString("old") && state.integerProperties().get("large")==100000
                    && state.stringProperties().get("owner").equals("native-test"),"Bounded property migration and snapshots");
            for(int tick=1;tick<=4;tick++) { raft.tickCount=tick;raft.tick(); }
            require(state.getInt("progress")==2 && state.getItem(0).getCount()==4,"Actual raft tick interval retains input until complete");
            var reporter=new ProblemReporter.Collector();var saved=TagValueOutput.createWithContext(reporter,level.registryAccess());
            raft.saveWithoutId(saved);restored=new RaftEntity(ModContent.RAFT.get(),level);
            restored.load(TagValueInput.create(reporter,level.registryAccess(),saved.buildResult()));
            var copy=restored.machineContext(pos).orElseThrow().state();
            require(reporter.isEmpty() && copy.getInt("progress")==2 && copy.getInt("large")==100000
                    && copy.getString("owner").equals("native-test") && copy.getItem(0).getCount()==4,"Native exact inventory and property persistence");
            require(loaded.get()==2 && copy!=state,"Each raft owns distinct state and receives load hook");
            raft.tickCount=6;raft.tick();require(state.getItem(1).is(Items.DRIED_KELP) && state.getItem(0).getCount()==3 && copy.getItem(0).getCount()==4,"Atomic output and independent owners");
            raft.tickCount=8;raft.tick();require(state.getInt("progress")==0 && state.getItem(0).getCount()==3,"Full output stops processing");
            state.setItem(1,ItemStack.EMPTY);state.setInt("paused",1);raft.tickCount=10;raft.tick();
            require(state.getInt("progress")==0 && !raft.machineView(pos).active(),"Pause and synchronized inactive presentation");
            state.setInt("paused",0);raft.tickCount=20;raft.tick();
            require(raft.machineView(pos).active() && raft.machineView(pos).progress()==1,"Actual raft publishes bounded progress");
            require(new RaftMachineView(true,Integer.MAX_VALUE,-1,Integer.MAX_VALUE).equals(new RaftMachineView(true,1000000,0,4095)),"Presentation clamping");
            var area=raft.getBoundingBox().inflate(8);int before=items(level,area);
            require(raft.removeStructuralPiece(pos),"Machine removal");
            require(items(level,area)-before==3 && state.isEmpty() && removed.get()==1,"Inventory drops once and removal hook runs once: drops="+(items(level,area)-before)+", empty="+state.isEmpty()+", hooks="+removed.get()+", area="+area+", center="+ctx.worldPosition());
            require(!ctx.valid(player) && !menu.stillValid(player) && raft.machineContext(pos).isEmpty(),"Detached handle and menu invalidate");
            require(!raft.removeStructuralPiece(pos) && removed.get()==1 && items(level,area)-before==3,"Repeated removal cannot duplicate inventory");
            boolean rejected=false;try { new RaftMachineState(3); } catch(IllegalArgumentException expected) { rejected=true; }
            require(rejected,"Invalid inventory size rejected");
            rejected=false;try { copy.setString("bad","x".repeat(257)); } catch(IllegalArgumentException expected) { rejected=true; }
            require(rejected,"Unbounded saved strings rejected");
            var data=copy.data("progress","large");data.set(0,1);require(data.getCount()==2 && copy.getInt("progress")==1,"Custom-menu data binding");
            copy.clearContent();restored.discard();
            require(restored.machineContext(pos).isEmpty(),"Destroying an intact machine closes state lookup permanently");
            var last=new RaftEntity(ModContent.RAFT.get(),level);seed.putString("Layout","0,0");
            last.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));last.setPos(8,level.getMaxY()+24,8);level.addFreshEntity(last);
            try {
                last.machineContext(pos).orElseThrow().state().setItem(0,new ItemStack(Items.KELP,2));
                require(last.damageFoundation(BlockPos.ZERO,100) && last.isRemoved() && last.machineContext(pos).isEmpty(),"Last foundation damage cannot reopen a detached machine in its callback");
            } finally {last.discard();}
        } finally {
            raft.discard();player.discard();
            if(restored!=null) { restored.machineContext(pos).ifPresent(ctx->ctx.state().clearContent());restored.discard(); }
            level.getEntitiesOfClass(ItemEntity.class,new AABB(0,level.getMaxY(),0,20,level.getMaxY()+60,20)).forEach(ItemEntity::discard);
        }
        checks += RaftUtilityApiCheck.run(level);
        return checks;
    }
    private static int items(ServerLevel level,AABB area) { return level.getEntitiesOfClass(ItemEntity.class,area).stream().filter(e->e.getItem().is(Items.KELP)).mapToInt(e->e.getItem().getCount()).sum(); }
    private static void require(boolean condition,String message) { if(!condition)throw new IllegalStateException(message);checks++; }
}
