package dev.raftocean.navigation;

import com.mojang.authlib.GameProfile;
import dev.raftocean.entity.*;
import dev.raftocean.menu.NavigationMenu;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Loaded fuel, validated menu buttons, save/load and bounded propulsion regressions. */
public final class NavigationCheck {
    public static final BlockPos ENGINE = new BlockPos(1, 1, 0), SAIL = new BlockPos(-1, 1, 0),
            WHEEL = new BlockPos(0, 1, 1), ANCHOR = new BlockPos(0, 1, -1);
    private NavigationCheck() { }
    public static RaftEntity create(ServerLevel level) {
        var structure = new RaftStructure();
        require(structure.add(ENGINE, new RaftPiece(RaftPiece.Kind.ENGINE, 0, false), RaftLayout.starter()), "Engine support");
        require(structure.add(SAIL, new RaftPiece(RaftPiece.Kind.SAIL, 0, false), RaftLayout.starter()), "Sail support");
        require(structure.add(WHEEL, new RaftPiece(RaftPiece.Kind.STEERING_WHEEL, 0, false), RaftLayout.starter()), "Wheel support");
        require(structure.add(ANCHOR, new RaftPiece(RaftPiece.Kind.ANCHOR, 0, false), RaftLayout.starter()), "Anchor support");
        var seed = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        seed.putString("Structure", structure.encode());
        var raft = new RaftEntity(ModContent.RAFT.get(), level);
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), seed.buildResult()));
        raft.setPos(.5, level.getMaxY()+16, .5);
        return raft;
    }
    public static int run(ServerLevel level) {
        int checks = 0;
        for (String name : List.of("paddle", "raft_sail", "raft_steering_wheel", "raft_engine", "raft_anchor", "raft_wind_streamer")) {
            require(level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,
                    Identifier.fromNamespaceAndPath("raft_ocean", name))).isPresent(), "Loaded navigation recipe " + name); checks++;
        }
        require(RaftNavigation.sailDrive(300, 300, 1) > RaftNavigation.sailDrive(345, 300, 1)
                && RaftNavigation.sailDrive(120, 300, 1) == 0, "Wind alignment and headwind dead zone"); checks++;
        require(RaftNavigation.sailDrive(300, 300, 1.15) > RaftNavigation.sailDrive(300, 300, .75), "Stronger wind gives more thrust"); checks++;
        require(RaftNavigation.sailDrive(30,300,1) > .02 && RaftNavigation.sailDrive(210,300,1) > .02,
                "Sail produces lateral thrust on either broad reach for counter-steering");checks++;
        require(RaftNavigation.normalize(-15) == 345 && RaftNavigation.normalize(375) == 15
                && RaftNavigation.normalize(Double.NaN) == 0, "Finite normalized headings"); checks++;
        require(Math.abs(RaftNavigation.sailDrive(300, 300, 1) * 20 - 1.0) < 1E-9,
                "Aligned sail delivers the requested 1.0 meters per second at unit wind"); checks++;
        var snapshotRaft = new RaftEntity(ModContent.RAFT.get(), level);
        try {
            var snapshot = snapshotRaft.navigation();
            snapshot.acceptSnapshot("45;300;1;150;200;0;1,1,0,180,1,1");
            require(snapshot.engines() == 150 && snapshot.sails() == 200 && snapshot.heading() == 45
                    && snapshot.running(ENGINE), "Large raft navigation counts are not truncated at 128"); checks++;
            for (String invalid : List.of("90;10;1;1;1;0;1,1,0,0,1,1/broken",
                    "NaN;10;1;1;1;0;", "90;10;Infinity;1;1;0;", "90;10;1;513;1;0;",
                    "90;10;1;1;1;0;1,1,0,0,1,1/1,1,0,0,1,0")) {
                snapshot.acceptSnapshot(invalid);
                require(snapshot.heading() == 45 && snapshot.windHeading() == 300 && snapshot.engines() == 150
                        && snapshot.running(ENGINE), "Malformed navigation leaves the last complete frame intact"); checks++;
            }
            snapshot.acceptSnapshot("90;10;1;0;0;0;");
            require(snapshot.heading() == 90 && !snapshot.running(ENGINE), "Complete empty snapshot clears retired engines"); checks++;
        } finally { snapshotRaft.discard(); }
        var paddleRaft = new RaftEntity(ModContent.RAFT.get(), level);
        try {
            paddleRaft.setCurrent(0, 0);
            require(paddleRaft.navigation().stroke(new Vec3(1, 0, 0)), "Starter raft accepts a paddle stroke"); checks++;
            require(Math.abs(paddleRaft.navigation().target(true).length() * 20 - 1.8) < 1E-9,
                    "Paddle delivers requested 1.8 meters per second on starter raft"); checks++;
            for (int i = 0; i < 20; i++) {
                paddleRaft.navigation().stroke(new Vec3(1, 0, 0));
                require(Math.abs(paddleRaft.navigation().target(true).length() * 20 - 1.8) < 1E-9,
                        "Repeated strokes cannot accumulate above paddle speed"); checks++;
            }
            require(paddleRaft.navigation().target(true).length() * 20 < 1.8,
                    "Paddle drive still decays between strokes"); checks++;
        } finally { paddleRaft.discard(); }
        var raft = create(level);
        boolean forced = level.setChunkForced(0,0,true);
        level.getChunkSource().tick(() -> true, false);
        level.waitForEntities(new net.minecraft.world.level.ChunkPos(0,0),0);
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "NavigationCheck"));
        player.setPos(raft.position().add(0, .251, 0));
        var navigation = raft.navigation();
        var engine = raft.storage(ENGINE);
        try {
            var driveMenu = new NavigationMenu(1, player.getInventory(), raft, ENGINE, RaftPiece.Kind.ENGINE);
            var sailMenu = new NavigationMenu(2, player.getInventory(), raft, SAIL, RaftPiece.Kind.SAIL);
            var wheelMenu = new NavigationMenu(3, player.getInventory(), raft, WHEEL, RaftPiece.Kind.STEERING_WHEEL);
            var anchorMenu = new NavigationMenu(4, player.getInventory(), raft, ANCHOR, RaftPiece.Kind.ANCHOR);
            require(driveMenu.stillValid(player) && sailMenu.stillValid(player) && wheelMenu.stillValid(player) && anchorMenu.stillValid(player),
                    "Installed control menus use moving reach"); checks++;
            require(!wheelMenu.clickMenuButton(player, 0) && !anchorMenu.clickMenuButton(player, 1)
                    && !driveMenu.clickMenuButton(player, 999), "Invalid or wrong-kind buttons cannot control raft"); checks++;
            player.setPos(player.position().add(20, 0, 0));
            require(!driveMenu.clickMenuButton(player, 0), "Remote button press rejected"); checks++;
            player.setPos(raft.position().add(0, .251, 0));
            navigation.setHeading(0); wheelMenu.clickMenuButton(player, 1);
            require(navigation.heading() == 345 && wheelMenu.heading() == 345, "Wheel turns shared drive heading left"); checks++;
            wheelMenu.clickMenuButton(player, 2);
            require(navigation.heading() == 0 && !wheelMenu.slots.get(0).isActive(), "Wheel returns heading and hides fuel slot"); checks++;
            sailMenu.clickMenuButton(player, 0); sailMenu.clickMenuButton(player, 2);
            require(raft.storage(SAIL).enabled && raft.storage(SAIL).trim == 15 && sailMenu.trim() == 15, "Sail deploy and trim synchronize"); checks++;
            for (int i=0; i<24; i++) { sailMenu.clickMenuButton(player, 2); }
            require(raft.storage(SAIL).trim == 15, "Sail trim wraps safely through one full rotation"); checks++;
            sailMenu.clickMenuButton(player,3);require(sailMenu.trim()==14,"Sail has one-degree decrement");checks++;
            sailMenu.clickMenuButton(player,4);require(sailMenu.trim()==15,"Sail has one-degree increment");checks++;
            sailMenu.clickMenuButton(player,5);require(sailMenu.trim()==10,"Sail has five-degree decrement");checks++;
            sailMenu.clickMenuButton(player,8);require(sailMenu.trim()==55,"Sail has 45-degree increment");checks++;
            sailMenu.clickMenuButton(player,9);require(sailMenu.trim()==0,"Sail centres independently of hull heading");checks++;
            raft.storage(SAIL).trim=180;sailMenu.clickMenuButton(player,4);require(sailMenu.trim()==-179,"Fine trim wraps precisely at 180 degrees");checks++;
            wheelMenu.clickMenuButton(player,3);require(navigation.heading()==359,"Wheel one-degree step wraps at north/south boundary");checks++;
            wheelMenu.clickMenuButton(player,6);require(navigation.heading()==4,"Wheel five-degree increment");checks++;
            wheelMenu.clickMenuButton(player,7);require(navigation.heading()==319,"Wheel 45-degree decrement");checks++;
            wheelMenu.clickMenuButton(player,9);require(navigation.heading()==raft.getYRot(),"Wheel reset holds present hull heading");checks++;
            require(!anchorMenu.clickMenuButton(player,9) && !driveMenu.clickMenuButton(player,4),"Fine controls cannot steer an anchor or engine menu");checks++;
            raft.storage(SAIL).enabled = false;
            raft.setCurrent(.005,.002);raft.storage(SAIL).enabled=true;raft.storage(SAIL).trim=45;
            Vec3 trimmed=navigation.target(true);
            require(trimmed.x<0,"Trimmed close reach counter-steers default eastward current");checks++;
            raft.storage(SAIL).enabled=false;
            raft.setCurrent(0,0);
            require(navigation.target(true).lengthSqr() == 0 && engine.fuelTicks == 0, "Idle unfueled raft has no drive"); checks++;
            driveMenu.clickMenuButton(player, 0);
            require(navigation.target(true).lengthSqr() == 0 && engine.getItem(1).isEmpty(), "Enabled empty engine cannot create thrust"); checks++;
            driveMenu.setCarried(new ItemStack(Items.STONE, 3)); driveMenu.clicked(0,1,ContainerInput.PICKUP,player);
            require(driveMenu.getCarried().getCount()==3 && engine.getItem(1).isEmpty(), "Engine fuel slot refuses unrelated items without consuming them"); checks++;
            driveMenu.setCarried(new ItemStack(Items.OAK_PLANKS, 8)); driveMenu.clicked(0,1,ContainerInput.PICKUP,player);
            require(driveMenu.getCarried().getCount()==7 && engine.getItem(1).getCount()==1, "Right-click inserts one engine fuel"); checks++;
            driveMenu.setCarried(ItemStack.EMPTY);
            navigation.target(false);
            require(engine.getItem(1).getCount()==1 && engine.fuelTicks==0, "Dry engine does not burn fuel"); checks++;
            Vec3 forward = navigation.target(true);
            require(forward.z > 0 && Math.abs(forward.x)<1E-9 && engine.getItem(1).isEmpty() && engine.fuelTicks>0,
                    "Engine ignites loaded native fuel and drives forward"); checks++;
            int duration = engine.fuelCapacity;
            require(engine.fuelTicks == duration-1 && driveMenu.fuel()>0 && driveMenu.fuel()<=1000, "Exactly one tick burns per active drive tick"); checks++;
            int reserve = engine.fuelTicks;
            require(driveMenu.clickMenuButton(player,10) && driveMenu.trim()==180,"Engine reverse button changes synchronized direction");checks++;
            require(navigation.target(true).z<0,"Reverse engine pushes opposite its mounted forward direction");checks++;
            require(driveMenu.clickMenuButton(player,10) && navigation.target(true).z>0,"Forward button restores mounted thrust");checks++;
            reserve=engine.fuelTicks;
            driveMenu.clickMenuButton(player, 0); navigation.target(true);
            require(engine.fuelTicks==reserve && navigation.engines()==0, "Stopping retains the burn reserve"); checks++;
            driveMenu.clickMenuButton(player, 0); anchorMenu.clickMenuButton(player, 0);
            require(navigation.target(true).lengthSqr()==0 && engine.fuelTicks==reserve && !navigation.stroke(new Vec3(1,0,0)),
                    "Anchor overrides current, drive and paddle without burning fuel"); checks++;
            anchorMenu.clickMenuButton(player, 0); navigation.setHeading(90); raft.setYRot(90); raft.refreshBounds();
            Vec3 left = navigation.target(true);
            require(left.x<0 && Math.abs(left.z)<1E-9 && engine.fuelTicks==reserve-1, "Released anchor resumes fuel and changes propulsion course"); checks++;
            driveMenu.clickMenuButton(player, 0);
            require(navigation.stroke(new Vec3(1,0,0)) && navigation.target(true).x>0, "Paddle creates impulse in looked-at direction"); checks++;
            for (int i=0; i<400; i++) { navigation.target(true); }
            require(navigation.target(true).length()<1E-7, "Paddle impulse decays rather than accelerating forever"); checks++;
            require(!navigation.stroke(new Vec3(0,1,0)), "Looking straight up cannot create an undefined horizontal impulse"); checks++;
            engine.enabled = true; engine.setItem(1,new ItemStack(Items.OAK_PLANKS, 2));
            reserve=engine.fuelTicks;
            for (int i=0; i<reserve; i++) { navigation.target(true); }
            require(engine.fuelTicks==0 && engine.getItem(1).getCount()==2, "Burn reserve lasts its exact remaining ticks"); checks++;
            navigation.target(true);
            require(engine.getItem(1).getCount()==1 && engine.fuelTicks==duration-1, "Next fuel consumed only after reserve is exhausted"); checks++;
            raft.storage(SAIL).enabled = true; navigation.setHeading(225); raft.storage(SAIL).trim=-45; raft.storage(ANCHOR).enabled=true;
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess()); raft.saveWithoutId(output);
            var restored = new RaftEntity(ModContent.RAFT.get(),level);
            restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),output.buildResult()));
            require(restored.getYRot()==90 && restored.navigation().heading()==225 && restored.storage(SAIL).enabled && restored.storage(SAIL).trim==-45
                    && restored.navigation().anchored() && restored.storage(ENGINE).enabled && restored.storage(ENGINE).fuelTicks==engine.fuelTicks
                    && restored.storage(ENGINE).getItem(1).getCount()==1, "Heading, trim, enabled states, fuel and anchors persist"); checks++;
            restored.pieces().keySet().forEach(pos -> restored.storage(pos).clearContent()); restored.discard();
            require(raft.removeStructuralPiece(ANCHOR) && !navigation.anchored(), "Removing the active anchor releases raft"); checks++;
            raft.storage(SAIL).enabled=false; engine.clearContent();
            require(raft.removeStructuralPiece(ENGINE) && !driveMenu.stillValid(player) && navigation.target(true).length()<1E-7,
                    "Removed engine invalidates menu and contributes no thrust"); checks++;
            raft.setYRot(0); raft.refreshBounds();
            Vec3 delta = new Vec3(-.15,.005,.04), relative = player.position().subtract(raft.position());
            raft.setPos(raft.position().add(delta)); RaftPlayerSupport.carryPlayer(raft,player,delta);
            require(player.position().subtract(raft.position()).distanceToSqr(relative)<1E-10, "Player carry preserves coordinates at driven speed"); checks++;
            require(sailMenu.stillValid(player) && wheelMenu.stillValid(player), "Control menus follow movement"); checks++;
            require(level.addFreshEntity(raft), "Paddle test raft registered"); checks++;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.PADDLE.get())); player.setXRot(0); player.setYRot(270);
            var paddleResult = ModContent.PADDLE.get().use(level,player,InteractionHand.MAIN_HAND);
            require(paddleResult.consumesAction() && player.getMainHandItem().getDamageValue()==1,
                    "Native supported paddle stroke: result="+paddleResult+", damage="+player.getMainHandItem().getDamageValue()
                    +", creative="+player.getAbilities().instabuild+", near="+level.getEntitiesOfClass(RaftEntity.class,player.getBoundingBox().inflate(1)).size()
                    +", supported="+raft.getCollisionBoxes().stream().anyMatch(box->RaftContact.isSupported(player.getBoundingBox(),box))); checks++;
            require(ModContent.PADDLE.get().use(level,player,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL,
                    "Paddle cooldown prevents duplicate strokes"); checks++;
            checks += rotationInteractions(level);
            checks += mountedEngines(level);
            player.setPos(player.position().add(20,0,0));
            require(ModContent.PADDLE.get().use(level,player,InteractionHand.MAIN_HAND)==net.minecraft.world.InteractionResult.FAIL,
                    "Paddle cannot steer a distant raft"); checks++;
        } finally {
            raft.pieces().keySet().forEach(pos -> { var storage=raft.storage(pos); if(storage!=null){storage.clearContent();} });
            raft.discard(); player.getInventory().clearContent();
            if(forced){level.setChunkForced(0,0,false);}
        }
        return checks;
    }
    public static int ocean(ServerLevel level) {
        int checks=0;
        var forced = new ArrayList<net.minecraft.world.level.ChunkPos>();
        for(int x=4;x<=6;x++){for(int z=4;z<=6;z++){
            if(level.setChunkForced(x,z,true)){forced.add(new net.minecraft.world.level.ChunkPos(x,z));}
            level.getChunk(x,z);
        }}
        level.getChunkSource().tick(() -> true, false);
        var raft=create(level); raft.setPos(80.5,62.84,80.5); raft.setCurrent(0,0);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"DriveOceanCheck"));
        // The center remains clear of equipment for every hull angle with a world-axis player body.
        player.setPos(raft.position().add(0,.251,0)); level.addNewPlayer(player);
        var engine=raft.storage(ENGINE); engine.enabled=true; engine.setItem(1,new ItemStack(Items.OAK_PLANKS,8));
        try {
            require(level.addFreshEntity(raft),"Ocean drive raft registered");checks++;
            Vec3 initial=raft.position(), relative=player.position().subtract(raft.position());
            for(int tick=0;tick<60;tick++){
                raft.tick();
                require(player.position().subtract(raft.position()).distanceToSqr(relative)<1E-9,"Driven ocean raft carries player at tick "+tick);checks++;
            }
            require(raft.getZ()>initial.z+2 && Math.abs(raft.getX()-initial.x)<1E-8
                    && raft.getY()>62.6 && raft.getY()<63,"Engine propels actual floating ocean raft");checks++;
            raft.storage(ANCHOR).enabled=true; int fuel=engine.fuelTicks;
            Vec3 held=raft.position();
            for(int tick=0;tick<40;tick++){raft.tick();}
            require(Math.abs(raft.getX()-held.x)<1E-8 && Math.abs(raft.getZ()-held.z)<1E-8
                    && engine.fuelTicks==fuel && Math.abs(raft.getY()-held.y)>1E-6,"Anchor holds coordinates but preserves bobbing and fuel");checks++;
            raft.navigation().setHeading(90);
            require(!raft.rotateTowardHeading(true) && raft.getYRot()==0,"Anchor also locks hull rotation");checks++;
            raft.storage(ANCHOR).enabled=false; raft.navigation().setHeading(90);
            Vec3 turn=raft.position();
            for(int tick=0;tick<140;tick++){
                raft.tick();
                Vec3 local=raft.toLocal(player.position());
                require(Math.abs(local.x-relative.x)<1E-8 && Math.abs(local.z-relative.z)<1E-8,
                        "Physical turn preserves local feet at tick "+tick);checks++;
            }
            require(raft.getX()<turn.x-1 && raft.getYRot()==90 && raft.getZ()>turn.z+.5 && engine.fuelTicks<fuel,
                    "Turning under power changes ocean movement direction");checks++;
            require(raft.toLocal(player.position()).distanceToSqr(relative)<1E-9,"Anchor release and turn preserve relative deck coordinates");checks++;
            Vec3 localFeet=raft.toLocal(player.position());
            Vec3 clientOrigin=raft.position().add(-.08,.002,.01);
            float clientYaw=raft.getYRot()-2;
            Vec3 clientFeet=RaftTransform.world(localFeet,clientOrigin,clientYaw);
            var frame=new dev.raftocean.network.RaftMovementFrame(raft.getId(),clientOrigin,clientFeet,clientYaw);
            var packet=new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot(clientFeet,20,0,true,false);
            var rebased=dev.raftocean.network.RaftMovementSync.rebasePacket(player,packet,frame,raft);
            require(rebased!=packet && new Vec3(rebased.getX(0),rebased.getY(0),rebased.getZ(0)).distanceToSqr(player.position())<1E-12
                    && rebased.getYRot(0)==22,"Native position packet rebases both rotating feet and view yaw");checks++;
            var invalidFrame=new dev.raftocean.network.RaftMovementFrame(raft.getId(),clientOrigin,clientFeet,clientYaw-30);
            require(dev.raftocean.network.RaftMovementSync.rebasePacket(player,packet,invalidFrame,raft)==packet,
                    "Native packet rejects excessive angular correction");checks++;
            var wheel = new NavigationMenu(7,player.getInventory(),raft,WHEEL,RaftPiece.Kind.STEERING_WHEEL);
            require(wheel.stillValid(player),"Rotated wheel menu follows world coordinates");checks++;
            raft.navigation().setHeading(0);
            require(raft.rotateTowardHeading(true) && raft.getYRot()==89.25F,"Shortest gradual return from heading target");checks++;
        } finally {
            raft.pieces().keySet().forEach(pos->raft.storage(pos).clearContent()); raft.discard();
            level.removePlayerImmediately(player,Entity.RemovalReason.DISCARDED);
            forced.forEach(chunk -> level.setChunkForced(chunk.x(),chunk.z(),false));
        }
        return checks;
    }
    private static int rotationInteractions(ServerLevel level) {
        int checks=0;
        for(float yaw:new float[]{37,90,179,315}) {
            var raft=new RaftEntity(ModContent.RAFT.get(),level);
            raft.setPos(10.5,level.getMaxY()+16,10.5);raft.setYRot(yaw);raft.refreshBounds();
            var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"RotatedBuildCheck"));
            player.setPos(raft.toWorld(new Vec3(0,.251,0)));
            Vec3 direction=raft.toWorld(new Vec3(1.49,.20,1)).subtract(player.getEyePosition());
            player.setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
            player.setXRot((float)-Math.toDegrees(Math.atan2(direction.y,direction.horizontalDistance())));
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.FOUNDATION.get(),2));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.getBlocks().containsKey(new BlockPos(2,0,1)) && player.getMainHandItem().getCount()==1,
                    "Rotated foundation placement follows local edge at yaw "+yaw);checks++;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.FLOOR),2));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.pieces().containsKey(new BlockPos(1,1,1)) && player.getMainHandItem().getCount()==1,
                    "Rotated building ray attaches to local foundation at yaw "+yaw);checks++;
            player.getInventory().clearContent();raft.discard();
        }
        var raft=create(level);raft.setPos(.5,90.5,.5);raft.setYRot(0);raft.refreshBounds();raft.navigation().setHeading(15);
        BlockPos obstacle=new BlockPos(2,90,-1);
        var old=level.getBlockState(obstacle);
        try {
            require(level.setBlock(obstacle,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3),"Rotation obstacle installed");checks++;
            require(!raft.rotateTowardHeading(true) && raft.getYRot()==0,"Terrain prevents hull rotation before passengers are carried");checks++;
        } finally { level.setBlock(obstacle,old,3);raft.discard(); }
        return checks;
    }
    private static int mountedEngines(ServerLevel level){
        int checks=0;
        for(float yaw:new float[]{0,37,90,315}){for(int rotation=0;rotation<4;rotation++){
            var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            seed.putString("Structure","1,1,0,ENGINE,"+rotation+",0");
            var raft=new RaftEntity(ModContent.RAFT.get(),level);
            try{
                raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
                raft.setYRot(yaw);raft.setCurrent(0,0);raft.setPos(.5,level.getMaxY()+16,.5);
                var engine=raft.storage(ENGINE);engine.enabled=true;engine.fuelTicks=100;engine.fuelCapacity=100;engine.fuelSpeed=1;
                var menu=new NavigationMenu(76,new FakePlayer(level,new GameProfile(UUID.randomUUID(),"EngineBearingCheck")).getInventory(),raft,ENGINE,RaftPiece.Kind.ENGINE);
                for(int reverse=0;reverse<2;reverse++){
                    engine.trim=reverse*180;
                    Vec3 expected=RaftNavigation.direction(yaw+rotation*90+engine.trim).scale(1.5 / 20);
                    require(raft.navigation().target(true).distanceToSqr(expected)<1E-12,"Mounted engine thrust matches physical direction at every tested hull bearing");checks++;
                    require(menu.heading()==(int)RaftNavigation.normalize(yaw+rotation*90+engine.trim),"Engine UI reports actual thrust bearing");checks++;
                }
            }finally{raft.discard();}
        }}
        return checks;
    }
    private static void require(boolean value,String message) { if(!value){throw new IllegalStateException(message);} }
}
