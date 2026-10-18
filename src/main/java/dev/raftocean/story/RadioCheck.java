package dev.raftocean.story;

import com.mojang.authlib.GameProfile;
import dev.raftocean.api.RaftMachineContext;
import dev.raftocean.entity.*;
import dev.raftocean.menu.RadioMenu;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.Batteries;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Actual supported upper decks, server buttons, portable-charge conservation and hull-relative radar. */
public final class RadioCheck {
    private static int checks;
    private static final BlockPos RECEIVER=new BlockPos(0,5,0);
    private static final List<BlockPos> GOOD=List.of(new BlockPos(-4,5,0),new BlockPos(4,5,0),new BlockPos(0,5,4));
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    private static RaftEntity fixture(ServerLevel level,int receiverY,int rotation,List<BlockPos> antennas){
        var layout=RaftLayout.starter();var columns=new ArrayList<BlockPos>();
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)columns.add(new BlockPos(x,0,z));
        columns.sort(Comparator.comparingInt(p->Math.abs(p.getX())+Math.abs(p.getZ())));
        for(var p:columns)if(!layout.cells().contains(p))require(layout.add(p),"Radio fixture connected foundations");
        var structure=new RaftStructure();require(structure.add(new BlockPos(-2,1,-2),new RaftPiece(RaftPiece.Kind.PILLAR,0,false),layout),"Radio fixture pillar");
        columns.sort(Comparator.comparingInt(p->Math.abs(p.getX()+2)+Math.abs(p.getZ()+2)));
        for(var p:columns)require(structure.add(new BlockPos(p.getX(),4,p.getZ()),new RaftPiece(RaftPiece.Kind.FLOOR,0,false),layout),"Radio fixture supported upper floor");
        require(structure.add(new BlockPos(0,receiverY,0),new RaftPiece(RaftPiece.Kind.ADDON,rotation,false,RaftRadio.ID.toString()),layout),"Receiver mounts with shared collision/support");
        for(var p:antennas)require(structure.add(p,new RaftPiece(RaftPiece.Kind.ADDON,0,false,RaftRadio.ANTENNA_ID.toString()),layout),"Antenna mounts without intersecting support");
        var seed=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());seed.putString("Layout",layout.encode());seed.putString("Structure",structure.encode());
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(8,level.getMaxY()+24,8);return raft;
    }
    public static int run(ServerLevel level){
        checks=0;var rafts=new ArrayList<RaftEntity>();int limit=dev.raftocean.config.RaftConfig.PIECE_LIMIT.get();
        try{
            dev.raftocean.config.RaftConfig.PIECE_LIMIT.set(Math.max(limit,400));
            var low=fixture(level,1,0,GOOD);rafts.add(low);
            require(RaftRadio.aerials(low.machineContext(new BlockPos(0,1,0)).orElseThrow()).status()==RaftRadio.HEIGHT,"Receiver at foundation level requests upper floor");
            var missing=fixture(level,5,0,GOOD.subList(0,2));rafts.add(missing);
            require(RaftRadio.aerials(missing.machineContext(RECEIVER).orElseThrow()).status()==RaftRadio.NEED_ANTENNAS,"Two antennas cannot receive");
            var close=fixture(level,5,0,List.of(new BlockPos(4,5,0),new BlockPos(4,5,1),new BlockPos(4,5,2)));rafts.add(close);
            require(RaftRadio.aerials(close.machineContext(RECEIVER).orElseThrow()).status()==RaftRadio.SPACING,"Three close antennas fail pair separation");
            var extra=fixture(level,5,0,List.of(new BlockPos(4,5,0),new BlockPos(4,5,1),new BlockPos(4,5,2),new BlockPos(-4,5,0),new BlockPos(0,5,4)));rafts.add(extra);
            require(RaftRadio.aerials(extra.machineContext(RECEIVER).orElseThrow()).status()==RaftRadio.READY,"Extra poor antennas do not hide a valid triangle");
            for(int rotation=0;rotation<4;rotation++){
                var raft=fixture(level,5,rotation,GOOD);rafts.add(raft);var ctx=raft.machineContext(RECEIVER).orElseThrow();var state=ctx.state();
                require(RaftRadio.aerials(ctx).status()==RaftRadio.READY,"Three upper-floor spaced antennas work at every mounting rotation");
                var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"RadioOwner"));player.setPos(ctx.worldPosition());
                var other=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"RadioOther"));other.setPos(player.position());
                var menu=new RadioMenu(4,player.getInventory(),ctx);
                require(!menu.getSlot(0).mayPlace(new ItemStack(Items.COAL)) && menu.getSlot(0).mayPlace(new ItemStack(Batteries.BASIC.get())),"Radio socket only accepts portable batteries");
                var battery=new ItemStack(Batteries.BASIC.get());Batteries.setCharge(battery,100);state.setItem(0,battery);
                require(!menu.clickMenuButton(other,0) && !menu.clickMenuButton(player,99),"Foreign owners and invalid button IDs are refused");
                require(menu.clickMenuButton(player,0) && state.getInt("enabled")==1,"Valid owner switches on");
                for(int i=0;i<40;i++)menu.broadcastChanges();
                require(Batteries.charge(battery)==100,"Opening and refreshing UI does not multiply energy drain");
                require(!menu.clickMenuButton(player,3) && state.getInt("channel")==0,"Future story channels cannot be selected before discovery");
                if(level.getChunkSource().getGenerator() instanceof dev.raftocean.world.OceanChunkGenerator){
                    RaftRadio.CONTROLLER.tick(ctx);require(Batteries.charge(battery)==98,"Enabled valid Ocean receiver spends exactly two charge per processing second");
                    menu.clickMenuButton(player,0);RaftRadio.CONTROLLER.tick(ctx);require(Batteries.charge(battery)==98,"Off receiver retains charge");menu.clickMenuButton(player,0);
                    state.setInt("found",1);var origin=ctx.worldPosition();state.setInt("target_x",(int)Math.round(origin.x+256));state.setInt("target_z",(int)Math.round(origin.z+128));
                    for(float yaw:new float[]{0,30,90,177,270}){
                        raft.setYRot(yaw);RaftRadio.refresh(ctx);var relative=RaftRadio.relative(ctx);double angle=Math.toRadians(yaw+rotation*90);
                        double dx=state.getInt("target_x")-ctx.worldPosition().x,dz=state.getInt("target_z")-ctx.worldPosition().z;
                        require(Math.abs(relative.x-(dx*Math.cos(angle)+dz*Math.sin(angle)))<1E-8
                                && Math.abs(relative.z-(-dx*Math.sin(angle)+dz*Math.cos(angle)))<1E-8,"Radar follows hull and installed console orientation");
                        require(Math.abs(menu.value(6))<=8192 && Math.abs(menu.value(7))<=8192,"Relative data fits signed native short slots");
                    }
                    Batteries.setCharge(battery,1);RaftRadio.CONTROLLER.tick(ctx);require(Batteries.charge(battery)==1 && state.getInt("status")==RaftRadio.BATTERY,"Undercharged cell spends nothing");
                    Batteries.setCharge(battery,100);raft.setYRot(0);
                    if(rotation==0){
                        require(RaftRadio.scan(ctx),"Explicit native structure scan runs on valid powered receiver");
                        require(!RaftRadio.scan(ctx),"Repeated scans reuse the shared receiver cooldown");
                        player.setData(IslandContent.JOURNEY,2);menu.clickMenuButton(player,3);
                        require(!RaftRadio.scan(ctx),"Switching channels cannot bypass the ten-second scan budget");
                    }
                }else{RaftRadio.CONTROLLER.tick(ctx);require(Batteries.charge(battery)==100 && state.getInt("status")==RaftRadio.OCEAN_ONLY,"Non-ocean worlds do not spend portable power");}
                var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());raft.saveWithoutId(saved);
                var restored=new RaftEntity(ModContent.RAFT.get(),level);rafts.add(restored);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
                require(Batteries.charge(restored.machineContext(RECEIVER).orElseThrow().state().getItem(0))==Batteries.charge(battery),"Native raft save retains installed battery components");
                player.setPos(ctx.worldPosition().add(20,0,0));require(!menu.stillValid(player) && !menu.clickMenuButton(player,0),"Moving out of reach closes controls");
                player.setPos(ctx.worldPosition());int charge=Batteries.charge(battery);
                require(raft.removeStructuralPiece(GOOD.getFirst()),"An antenna can be removed independently");
                RaftRadio.CONTROLLER.tick(ctx);
                require(RaftRadio.aerials(ctx).status()==RaftRadio.NEED_ANTENNAS && Batteries.charge(battery)==charge,
                        "Layout revision invalidates antenna cache and stops power use");
                var dropArea=new net.minecraft.world.phys.AABB(ctx.worldPosition(),ctx.worldPosition()).inflate(4);
                int droppedBefore=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,dropArea).stream().mapToInt(e->Batteries.charge(e.getItem())).sum();
                require(raft.removeStructuralPiece(RECEIVER),"Receiver can be removed through existing raft lifecycle");
                RaftRadio.CONTROLLER.tick(ctx);
                int droppedAfter=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,dropArea).stream().mapToInt(e->Batteries.charge(e.getItem())).sum();
                require(!menu.stillValid(player) && state.getItem(0).isEmpty() && menu.value(10)==0,"Removed receiver invalidates its menu and empties installed inventory");
                require(droppedAfter-droppedBefore==charge,"Removal drops the battery with its exact remaining charge and stale ticks spend nothing");
            }
            return checks;
        }finally{rafts.forEach(RaftEntity::discard);dev.raftocean.config.RaftConfig.PIECE_LIMIT.set(limit);}
    }
    private RadioCheck() { }
}
