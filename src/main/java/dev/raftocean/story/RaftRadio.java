package dev.raftocean.story;

import dev.raftocean.api.*;
import dev.raftocean.entity.*;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.Batteries;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.*;

/** Native API-backed radio: local antenna geometry, portable charge, bounded explicit scans. */
public final class RaftRadio implements RaftMachineBehavior {
    public static final Identifier ID=Identifier.fromNamespaceAndPath("raft_ocean","survey_receiver");
    public static final Identifier ANTENNA_ID=Identifier.fromNamespaceAndPath("raft_ocean","survey_antenna");
    public static final DeferredBlock<Block> VISUAL=ModContent.BLOCKS.registerSimpleBlock("survey_receiver");
    public static final DeferredBlock<Block> ACTIVE=ModContent.BLOCKS.registerSimpleBlock("survey_receiver_active");
    public static final DeferredBlock<Block> ANTENNA=ModContent.BLOCKS.registerSimpleBlock("survey_antenna");
    public static final DeferredItem<Item> ITEM=ModContent.ITEMS.registerSimpleItem("survey_receiver",p->p.stacksTo(16));
    public static final DeferredItem<Item> ANTENNA_ITEM=ModContent.ITEMS.registerSimpleItem("survey_antenna",p->p.stacksTo(16));
    public static final RaftRadio CONTROLLER=new RaftRadio();
    public static final int OFF=0,HEIGHT=1,NEED_ANTENNAS=2,SPACING=3,BATTERY=4,READY=5,NO_SIGNAL=6,OPEN_CHANNEL=7,OCEAN_ONLY=8;
    public record Aerials(int status,int count) { }
    private record Layout(long revision,Aerials aerials) { }
    private record Scan(long until,BlockPos origin,int channel) { }
    private static final Map<RaftEntity,Map<BlockPos,Layout>> LAYOUTS=new WeakHashMap<>();
    private static final Map<RaftMachineState,Scan> SCANS=new WeakHashMap<>();
    public static void initialize() { }
    public static void register(){
        var top=new AABB(-.4375,.5,-.375,.4375,.9375,.375);
        var desk=List.of(top,new AABB(-.375,0,-.3125,-.25,.5,-.1875),new AABB(.25,0,-.3125,.375,.5,-.1875),
                new AABB(-.375,0,.1875,-.25,.5,.3125),new AABB(.25,0,.1875,.375,.5,.3125));
        RaftBuildables.register(new RaftBuildable(ID,ITEM::get,VISUAL,1,1,1,8,true,desk,desk,(r,a,p,h)->InteractionResult.PASS),CONTROLLER);
        var aerial=List.of(new AABB(-.3125,0,-.3125,.3125,.125,.3125),new AABB(-.03125,.125,-.03125,.03125,2,.03125));
        RaftBuildables.register(new RaftBuildable(ANTENNA_ID,ANTENNA_ITEM::get,ANTENNA,1,1,2,2,true,aerial,aerial,(r,a,p,h)->{
            p.sendOverlayMessage(Component.translatable("raft_ocean.radio.antenna_help"));return InteractionResult.SUCCESS_SERVER;
        }));
    }
    public static Aerials aerials(RaftMachineContext ctx){
        var map=LAYOUTS.computeIfAbsent(ctx.raft(),r->new HashMap<>());var previous=map.get(ctx.anchor());
        if(previous!=null && previous.revision()==ctx.raft().layoutRevision())return previous.aerials();
        var candidates=new ArrayList<BlockPos>();int total=0;
        for(var e:ctx.raft().pieces().entrySet())if(e.getValue().addonId().equals(ANTENNA_ID.toString())){
            total++;var p=e.getKey();double dx=p.getX()-ctx.anchor().getX(),dz=p.getZ()-ctx.anchor().getZ(),distance=dx*dx+dz*dz;
            if(Math.abs(p.getY()-ctx.anchor().getY())<=1 && distance>=9 && distance<=225)candidates.add(p);
        }
        int status=ctx.anchor().getY()<4?HEIGHT:total<3?NEED_ANTENNAS:SPACING;
        if(status==SPACING && candidates.size()>=3){
            // Triangle search with bit intersections avoids cubic scans on large rafts.
            var neighbors=new BitSet[candidates.size()];
            for(int i=0;i<neighbors.length;i++){
                neighbors[i]=new BitSet(neighbors.length);
                for(int j=i+1;j<neighbors.length;j++)if(horizontalDistance(candidates.get(i),candidates.get(j))>=25)neighbors[i].set(j);
            }
            outer:for(int i=0;i<neighbors.length;i++)for(int j=neighbors[i].nextSetBit(i+1);j>=0;j=neighbors[i].nextSetBit(j+1)){
                var common=(BitSet)neighbors[i].clone();common.and(neighbors[j]);
                if(!common.isEmpty()){status=READY;break outer;}
            }
        }
        var result=new Aerials(status,Math.min(3,candidates.size()));map.put(ctx.anchor(),new Layout(ctx.raft().layoutRevision(),result));return result;
    }
    private static double horizontalDistance(BlockPos a,BlockPos b){double x=a.getX()-b.getX(),z=a.getZ()-b.getZ();return x*x+z*z;}
    @Override public int tickInterval(){return 20;}
    @Override public boolean canInsert(RaftMachineContext c,int slot,ItemStack stack){return slot==0 && Batteries.capacity(stack)>0;}
    @Override public void loaded(RaftMachineContext c){c.state().setInt("found",0);c.state().setInt("channel",Math.clamp(c.state().getInt("channel"),0,ReceiverItem.DESTINATIONS.length));SCANS.remove(c.state());}
    @Override public void removed(RaftMachineContext c){SCANS.remove(c.state());var map=LAYOUTS.get(c.raft());if(map!=null)map.remove(c.anchor());}
    public static void refresh(RaftMachineContext ctx){
        if(!ctx.isInstalled())return;
        var s=ctx.state();var aerial=aerials(ctx);s.setInt("antennas",aerial.count());
        int status=s.getInt("enabled")==0?OFF:!(((ServerLevel)ctx.raft().level()).getChunkSource().getGenerator() instanceof dev.raftocean.world.OceanChunkGenerator)?OCEAN_ONLY:
                aerial.status()!=READY?aerial.status():Batteries.charge(s.getItem(0))<2?BATTERY:s.getInt("channel")==ReceiverItem.DESTINATIONS.length?OPEN_CHANNEL:
                s.getInt("found")==0?NO_SIGNAL:READY;
        if(status==READY){var p=ctx.worldPosition();double dx=s.getInt("target_x")-p.x,dz=s.getInt("target_z")-p.z;
            if(dx*dx+dz*dz>8192L*8192)status=NO_SIGNAL;}
        s.setInt("status",status);s.setInt("active",status==READY || status==NO_SIGNAL || status==OPEN_CHANNEL?1:0);
    }
    @Override public void tick(RaftMachineContext ctx){
        if(!ctx.isInstalled())return;refresh(ctx);var s=ctx.state();
        if(s.getInt("active")==1){var battery=s.getItem(0);Batteries.setCharge(battery,Batteries.charge(battery)-2);s.setChanged();}
    }
    public static boolean scan(RaftMachineContext ctx){
        refresh(ctx);var s=ctx.state();if(!ctx.isInstalled() || s.getInt("active")==0 || s.getInt("channel")>=ReceiverItem.DESTINATIONS.length)return false;
        var level=(ServerLevel)ctx.raft().level();long now=level.getGameTime();var pos=BlockPos.containing(ctx.worldPosition());int channel=s.getInt("channel");
        if(channel<0)return false;
        var old=SCANS.get(s);
        if(old!=null && old.channel()==channel && old.until()>now && old.origin().distSqr(pos)<=128L*128)return false;
        // Channel changes are also rate limited; alternate buttons cannot bypass the scan budget.
        if(old!=null && old.until()-1000>now)return false;
        SCANS.put(s,new Scan(now+1200,pos,channel));
        var tag=TagKey.create(Registries.STRUCTURE,Identifier.fromNamespaceAndPath("raft_ocean",ReceiverItem.DESTINATIONS[channel]));
        var target=level.findNearestMapStructure(tag,pos,6,false);if(target!=null)target=ReceiverItem.islandCenter(target);
        s.setInt("found",target==null || target.distSqr(pos)>8192L*8192?0:1);
        if(s.getInt("found")==1){s.setInt("target_x",target.getX());s.setInt("target_z",target.getZ());}
        refresh(ctx);return true;
    }
    public static Vec3 relative(RaftMachineContext ctx){
        if(!ctx.isInstalled())return Vec3.ZERO;
        var s=ctx.state();if(s.getInt("found")==0 || s.getInt("active")==0)return Vec3.ZERO;
        var origin=ctx.worldPosition();double dx=s.getInt("target_x")-origin.x,dz=s.getInt("target_z")-origin.z;
        if(dx*dx+dz*dz>8192L*8192)return Vec3.ZERO;
        var piece=ctx.raft().pieces().get(ctx.anchor());double angle=Math.toRadians(ctx.raft().getYRot()+piece.rotation()*90);
        return new Vec3(dx*Math.cos(angle)+dz*Math.sin(angle),0,-dx*Math.sin(angle)+dz*Math.cos(angle));
    }
    @Override public InteractionResult use(RaftMachineContext ctx,Player player,InteractionHand hand){
        if(player.isSpectator() || !ctx.valid(player))return InteractionResult.FAIL;
        RaftMachineMenus.openCustom(ctx,player,Component.translatable("item.raft_ocean.survey_receiver"),
                (id,inventory,c)->new dev.raftocean.menu.RadioMenu(id,inventory,c));return InteractionResult.SUCCESS_SERVER;
    }
    @Override public net.minecraft.world.level.block.state.BlockState visual(RaftBuildable type,RaftMachineView view){return (view.active()?ACTIVE:VISUAL).get().defaultBlockState();}
    private RaftRadio() { }
}
