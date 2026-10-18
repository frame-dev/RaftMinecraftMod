package dev.raftocean.navigation;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.menu.NavigationMenu;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native multi-drive layouts, fuel conservation and exact capacity boundaries. */
public final class NavigationLoadCheck {
    private static int checks;
    private static final BlockPos FIRST=new BlockPos(-1,1,-1),SECOND=new BlockPos(0,1,-1),
            SAIL_A=new BlockPos(-1,1,1),SAIL_B=new BlockPos(1,1,1),ANCHOR=new BlockPos(0,1,1);
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    private static RaftEntity fixture(ServerLevel level,int foundations,int engines,int sails){
        var layout=RaftLayout.starter();var candidates=new ArrayList<BlockPos>();
        for(int x=-15;x<=16;x++)for(int z=-15;z<=16;z++)candidates.add(new BlockPos(x,0,z));
        candidates.sort(Comparator.comparingInt(p->Math.abs(p.getX())+Math.abs(p.getZ())));
        for(var p:candidates)if(layout.cells().size()<foundations && !layout.cells().contains(p))
            require(layout.add(p),"Connected foundation load fixture");
        var structure=new RaftStructure();
        for(int i=0;i<engines;i++)require(structure.add(new BlockPos(i%3-1,1,i/3-1),
                new RaftPiece(RaftPiece.Kind.ENGINE,0,false),layout),"Engine fixture support");
        if(sails>0)require(structure.add(SAIL_A,new RaftPiece(RaftPiece.Kind.SAIL,0,false),layout),"First sail support");
        if(sails>1)require(structure.add(SAIL_B,new RaftPiece(RaftPiece.Kind.SAIL,0,false),layout),"Second sail support");
        require(structure.add(ANCHOR,new RaftPiece(RaftPiece.Kind.ANCHOR,0,false),layout),"Anchor fixture support");
        var edge=layout.cells().stream().max(Comparator.comparingInt(BlockPos::getX)).orElseThrow().east();
        require(structure.add(edge,new RaftPiece(RaftPiece.Kind.COLLECTION_NET,0,false),layout),"Edge collection net fixture");
        var seed=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        seed.putString("Layout",layout.encode());seed.putString("Structure",structure.encode());
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(0,level.getMaxY()+24,0);raft.setCurrent(0,0);return raft;
    }
    private static void fuel(RaftEntity raft,BlockPos pos){var s=raft.storage(pos);s.enabled=true;s.setItem(1,new ItemStack(Items.OAK_PLANKS,3));}
    public static int run(ServerLevel level){
        checks=0;var rafts=new ArrayList<RaftEntity>();
        boolean limits=RaftConfig.ENGINE_LOAD_LIMITS.get();int capacity=RaftConfig.ENGINE_FOUNDATIONS.get();
        double engine=RaftConfig.ENGINE_THRUST.get(),sail=RaftConfig.SAIL_THRUST.get(),wind=RaftConfig.WIND.get(),max=RaftConfig.MAX_SPEED.get();
        try{
            RaftConfig.ENGINE_LOAD_LIMITS.set(true);RaftConfig.ENGINE_FOUNDATIONS.set(100);
            RaftConfig.ENGINE_THRUST.set(1.0);RaftConfig.SAIL_THRUST.set(1.0);RaftConfig.WIND.set(1.0);RaftConfig.MAX_SPEED.set(.16);
            for(int n=1;n<=5;n++)for(int c:new int[]{1,9,100,1024}){
                require(RaftNavigation.engineLoadScale(n*c,n,c)==1,"Capacity boundary is full drive");
                require(RaftNavigation.engineLoadScale(n*c*11/10,n,c)==(n*c*11/10>n*c?.5:1),"Inclusive 110 percent boundary");
                require(RaftNavigation.engineLoadScale(n*c*11/10+1,n,c)==0,"First overburdened foundation stalls");
            }
            require(RaftNavigation.engineLoadScale(1024,6,1)==1 && RaftNavigation.engineLoadScale(9,0,100)==0,"Six powered engines bypass load; zero powered engines never do");
            for(double bearing:new double[]{91,120,180,240,269})require(RaftNavigation.sailDrive(bearing,0,1)==0,"Upwind sail cannot power the raft");
            require(RaftNavigation.sailDrive(90,0,1)>0 && RaftNavigation.sailDrive(270,0,1)>0,"Both crosswind directions remain steerable");
            for(int size:new int[]{100,101,110,111}){
                var raft=fixture(level,size,2,0);rafts.add(raft);fuel(raft,FIRST);var nav=raft.navigation();
                var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"EngineLoadCheck"));
                var menu=new NavigationMenu(1,player.getInventory(),raft,FIRST,RaftPiece.Kind.ENGINE);
                var motion=nav.target(true);double expected=size<=100?.075:size<=110?.0375:0;
                require(Math.abs(motion.z-expected)<1E-10 && Math.abs(motion.x)<1E-10,"Actual one-engine speed follows load band: "+size);
                require(menu.foundations()==size && menu.capacity()==100 && menu.engineStatus()==(size<=100?2:size<=110?3:4),"Server menu exposes exact load band");
                require(raft.storage(FIRST).getItem(1).getCount()==(size>110?3:2),"Stalled ignition preserves fuel stack");
                if(size==111){
                    raft.storage(FIRST).fuelTicks=30;raft.storage(FIRST).fuelCapacity=100;
                    require(nav.target(true).lengthSqr()==0 && raft.storage(FIRST).fuelTicks==30,"Overloaded engine preserves an existing burn reserve");
                    fuel(raft,SECOND);require(nav.engineLoad().powered()==2 && nav.target(true).z==.075,"Second fueled engine restores full requested base speed");
                    require(raft.storage(FIRST).fuelTicks==29 && raft.storage(SECOND).fuelTicks>0,"Both participating engines burn once per tick");
                    raft.storage(SECOND).trim=180;require(nav.target(true).length()<1E-10,"Opposed engines cancel instead of adding speed");
                    raft.storage(SECOND).enabled=false;require(nav.target(true).lengthSqr()==0,"Turning off needed engine restores stall");
                    raft.setCurrent(.003,.004);require(nav.target(true).length()>.0049,"Stalled engines still permit natural drift");raft.setCurrent(0,0);
                    RaftConfig.ENGINE_LOAD_LIMITS.set(false);
                    require(nav.target(true).z>0 && menu.engineStatus()==5,"Legacy config restores gradual engine scaling");
                    RaftConfig.ENGINE_LOAD_LIMITS.set(true);RaftConfig.ENGINE_FOUNDATIONS.set(200);
                    require(nav.target(true).z==.075 && menu.capacity()==200,"Custom foundation capacity takes effect without rebuilding");RaftConfig.ENGINE_FOUNDATIONS.set(100);
                }
                raft.storage(SECOND).trim=0;fuel(raft,SECOND);
                require(Math.abs(nav.target(true).z-.075)<1E-10,"Two aligned engines share one base-speed budget");
                int reserve=raft.storage(FIRST).fuelTicks;raft.storage(ANCHOR).enabled=true;
                require(nav.target(true).lengthSqr()==0 && raft.storage(FIRST).fuelTicks==reserve,"Anchor overrides loaded multi-engine drive without fuel use");
            }
            var large=fixture(level,1024,6,0);rafts.add(large);
            for(int i=0;i<6;i++)fuel(large,new BlockPos(i%3-1,1,i/3-1));
            require(large.navigation().engineLoad().capacity()==-1 && Math.abs(large.navigation().target(true).z-.075)<1E-10,"Six real fueled engines move a maximum-size raft at base speed");
            large.storage(FIRST).enabled=false;
            require(large.navigation().target(true).lengthSqr()==0,"Five fueled engines cannot bypass maximum foundation load");
            var sailing=fixture(level,100,0,2);rafts.add(sailing);var nav=sailing.navigation();nav.target(true);sailing.setYRot((float)nav.windHeading());
            sailing.storage(SAIL_A).enabled=true;var single=nav.target(true);sailing.storage(SAIL_B).enabled=true;
            require(single.distanceToSqr(nav.target(true))<1E-12,"Deploying a duplicate sail cannot stack speed on the same loaded hull");
            sailing.storage(SAIL_A).trim=90;sailing.storage(SAIL_B).trim=-90;
            require(nav.target(true).length()<1E-8,"Opposed crosswind sails counteract each other");
            sailing.storage(SAIL_A).trim=180;sailing.storage(SAIL_B).trim=180;
            require(nav.target(true).lengthSqr()==0,"Multiple upwind sails cannot overpower wind");
            var reader=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"SailFeedbackCheck"));
            var menu=new NavigationMenu(3,reader.getInventory(),sailing,SAIL_A,RaftPiece.Kind.SAIL);
            require(menu.sailStatus()==1,"Sail menu identifies headwind");
            sailing.storage(SAIL_A).trim=0;require(menu.sailStatus()==2,"Sail menu identifies useful wind");
            RaftConfig.SAIL_THRUST.set(0.0);require(menu.sailStatus()==4,"Configured-off sail drive is not misreported as headwind");RaftConfig.SAIL_THRUST.set(1.0);
            RaftConfig.WIND.set(0.0);require(nav.target(true).lengthSqr()==0 && menu.sailStatus()==3,"Zero wind produces no sail drive and correct menu status");
            sailing.storage(SAIL_A).enabled=false;require(menu.sailStatus()==0,"Folded sails show folded status");
            return checks;
        }finally{
            for(var raft:rafts){raft.pieces().keySet().forEach(p->raft.storage(p).clearContent());raft.discard();}
            RaftConfig.ENGINE_LOAD_LIMITS.set(limits);RaftConfig.ENGINE_FOUNDATIONS.set(capacity);RaftConfig.ENGINE_THRUST.set(engine);
            RaftConfig.SAIL_THRUST.set(sail);RaftConfig.WIND.set(wind);RaftConfig.MAX_SPEED.set(max);
        }
    }
    private NavigationLoadCheck() { }
}
