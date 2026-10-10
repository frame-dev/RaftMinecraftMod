package dev.raftocean.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.entity.RaftTransform;
import dev.raftocean.registry.ModContent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Cosmetic moving parts share the authoritative hull frame; colliders stay stable. */
public final class RaftRenderer extends EntityRenderer<RaftEntity, RaftRenderState> {
    private record Channel(BlockPos pos, String name) { }
    private final java.util.Map<RaftEntity, java.util.Map<Channel, Transition>> transitions = new java.util.WeakHashMap<>();
    private static final class Flywheel {
        double time, angle;
        float speed;
        boolean initialized;
        float update(double now, boolean running) {
            return update(now,running,1);
        }
        float update(double now, boolean running, int direction) {
            if (!initialized || now < time || now-time > 20) { time=now; initialized=true; }
            double dt=Math.clamp(now-time,0,2); time=now;
            float oldSpeed=speed;
            speed+=( (running?24*direction:0)-speed)*(float)(1-Math.exp(-dt/5));
            angle=(angle+(oldSpeed+speed)*.5*dt)%360;
            return (float)angle;
        }
    }
    private final java.util.Map<RaftEntity, java.util.Map<BlockPos, Flywheel>> engines = new java.util.WeakHashMap<>();
    private record Transition(float start, float target, double since) {
        float value(double time) {
            double t = Math.clamp((time-since)/12,0,1);
            return (float)(start+(target-start)*t*t*(3-2*t));
        }
    }
    private float transition(RaftEntity entity, BlockPos pos, boolean enabled, double time) {
        return transition(entity,pos,"deployment",enabled?1:0,time,false);
    }
    private float transition(RaftEntity entity, BlockPos pos, String name, float target, double time, boolean circular) {
        var parts = transitions.computeIfAbsent(entity, key -> new java.util.HashMap<>());
        var channel=new Channel(pos.immutable(),name);
        var state = parts.get(channel);
        if(state==null || time<state.since){state=new Transition(target,target,time);parts.put(channel,state);}
        if(circular){target=state.target+(float)RaftTransform.difference(target,state.target);}
        if (Math.abs(state.target-target)>.001F) { state = new Transition(state.value(time),target,time); parts.put(channel,state); }
        return state.value(time);
    }
    public RaftRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius = 0; }
    @Override public RaftRenderState createRenderState() { return new RaftRenderState(); }

    @Override public void extractRenderState(RaftEntity entity, RaftRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.cells.clear();
        state.ghost=RaftPlacementGhost.extract(entity);
        state.hullYaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        double time = entity.tickCount + partialTick;
        var previous = transitions.get(entity);
        if (previous != null) { previous.keySet().removeIf(channel -> !entity.pieces().containsKey(channel.pos)); }
        var oldEngines=engines.get(entity);
        if(oldEngines!=null){oldEngines.keySet().removeIf(pos->!entity.pieces().containsKey(pos)
                || entity.pieces().get(pos).kind()!=RaftPiece.Kind.ENGINE && entity.pieces().get(pos).kind()!=RaftPiece.Kind.GRINDSTONE
                && entity.pieces().get(pos).kind()!=RaftPiece.Kind.STONECUTTER);}
        var wood = ModContent.RAFT_DECK.get().defaultBlockState();
        entity.getBlocks().forEach((pos, visual) -> add(entity, state,
                new AABB(pos.getX()-.5, 0, pos.getZ()-.5, pos.getX()+.5, .25, pos.getZ()+.5),
                visual, Vec3.ZERO, 0, 0, 0, 0));
        entity.getBlocks().keySet().stream().filter(entity::reinforcedFoundation).forEach(pos->{
            var metal=ModContent.stationVisual(RaftPiece.Kind.ANVIL).defaultBlockState();
            for(int side:new int[]{-1,1}) {
                add(entity,state,new AABB(pos.getX()-.44,.02,pos.getZ()+side*.48-.025,pos.getX()+.44,.245,pos.getZ()+side*.48+.025),metal,Vec3.ZERO,0,0,0,0);
                add(entity,state,new AABB(pos.getX()+side*.48-.025,.02,pos.getZ()-.44,pos.getX()+side*.48+.025,.245,pos.getZ()+.44),metal,Vec3.ZERO,0,0,0,0);
            }
        });
        var chestPairs = dev.raftocean.menu.RaftChests.pairs(entity);
        entity.pieces().forEach((pos, piece) -> {
            var kind = piece.kind();
            var boxes = entity.pieceBoxes(pos, piece);
            boolean special = kind.machine() || kind.navigation() || kind == RaftPiece.Kind.BED || kind == RaftPiece.Kind.WIND_STREAMER;
            BlockState visual = ModContent.stationVisual(kind).defaultBlockState();
            double baseY = MachineVisualLayout.envelope(boxes).minY;
            if(kind.machine()){animateMachine(entity,state,pos,kind,baseY,time);}
            if(kind==RaftPiece.Kind.CRAFTING_TABLE || kind==RaftPiece.Kind.SMITHING_TABLE || kind==RaftPiece.Kind.CARTOGRAPHY_TABLE) {
                float active=transition(entity,pos,"bench_use",entity.machines().at(pos).working()?1:0,time,false);
                if(active>.01) {
                    double top=boxes.stream().mapToDouble(box->box.maxY).max().orElse(baseY+1);
                    Vec3 hinge=new Vec3(pos.getX(),top+.025,pos.getZ());
                    float tilt=(float)(Math.sin(time*.25)*30*active);
                    add(entity,state,new AABB(-.025,0,-.025,.025,.28,.025).move(hinge),wood,hinge,0,0,0,tilt);
                    add(entity,state,new AABB(-.12,.23,-.04,.12,.31,.04).move(hinge),ModContent.stationVisual(RaftPiece.Kind.ENGINE).defaultBlockState(),hinge,0,0,0,tilt);
                }
            }
            if (kind == RaftPiece.Kind.CHEST && chestPairs.containsKey(pos)) {
                BlockPos other = chestPairs.get(pos);
                if (pos.getX() < other.getX() || pos.getX() == other.getX() && pos.getZ() < other.getZ()) {
                    double cx = (pos.getX()+other.getX())*.5, cz = (pos.getZ()+other.getZ())*.5;
                    AABB join = pos.getX()!=other.getX()
                            ? new AABB(cx-.07,baseY,cz-.4375,cx+.07,baseY+.875,cz+.4375)
                            : new AABB(cx-.4375,baseY,cz-.07,cx+.4375,baseY+.875,cz+.07);
                    add(entity,state,join,wood,Vec3.ZERO,0,0,0,0);
                }
            }
            if (kind == RaftPiece.Kind.TREE_PLANTER && entity.treeStage(pos)>0) {
                var machine=entity.machines().at(pos);
                float target=machine.ready()?1:Math.max(.1F,machine.progress()/1000F);
                double growth=transition(entity,pos,"tree_growth",target,time,false), trunk= .4 + growth*2.1;
                double x=pos.getX(), z=pos.getZ(), y=baseY+.15;
                double crown=.2+growth*.42;
                Vec3 root=new Vec3(x,y,z);
                add(entity,state,new AABB(x-crown,y,z-crown,x+crown,y+trunk+.35,z+crown),
                        ModContent.TREE_BARK.get().defaultBlockState(),root,0,0,0,
                        (float)(Math.sin(time*.08+pos.getX())*.8*entity.navigation().windStrength()));
            }
            Vec3 pivot = new Vec3(pos.getX(), baseY, pos.getZ());
            float mount = piece.rotation() * 90;
            if(kind==RaftPiece.Kind.ENGINE){
                float bearing=mount+entity.navigation().trim(pos);
                var arrow=ModContent.MACHINE_WATER.get().defaultBlockState();
                double top=boxes.stream().mapToDouble(box->box.maxY).max().orElse(baseY+1)+.015;
                for(AABB part:java.util.List.of(new AABB(-.025,top,-.22,.025,top+.025,.19),
                        new AABB(-.12,top,.10,.12,top+.025,.18),new AABB(-.075,top,.18,.075,top+.025,.25),
                        new AABB(-.03,top,.25,.03,top+.025,.31))){
                    add(entity,state,RaftTransform.bounds(part,bearing).move(pos.getX(),0,pos.getZ()),arrow,
                            Vec3.ZERO,0,0,0,0);
                }
            }
            if(kind==RaftPiece.Kind.ADDON && !piece.addonId().isEmpty()) {
                var canonical=entity.pieceBoxes(pos,piece.withRotation(0));
                AABB envelope=canonical.getFirst();for(var box:canonical)envelope=envelope.minmax(box);
                var controller=dev.raftocean.api.RaftBuildables.machine(piece.addonId());
                var material=controller.isPresent()?controller.get().visual(piece.addon(),entity.machineView(pos)):piece.addon().visual().get().defaultBlockState();
                add(entity,state,envelope,material,pivot,0,mount,0,0);
                return;
            }
            if (kind.machine() || !kind.navigation() && kind != RaftPiece.Kind.CHEST
                    && kind != RaftPiece.Kind.ENDER_CHEST && kind != RaftPiece.Kind.BARREL
                    && kind != RaftPiece.Kind.LOOM && kind != RaftPiece.Kind.GRINDSTONE
                    && kind != RaftPiece.Kind.STONECUTTER && kind != RaftPiece.Kind.DOOR
                    && kind != RaftPiece.Kind.BED && kind != RaftPiece.Kind.WIND_STREAMER) {
                var canonical=entity.pieceBoxes(pos,piece.withRotation(0));
                AABB envelope=canonical.getFirst();
                for(var part:canonical){envelope=envelope.minmax(part);}
                add(entity,state,envelope,ModContent.displayModel(kind).defaultBlockState(),
                        new Vec3(pos.getX(),baseY,pos.getZ()),0,piece.rotation()*90,0,0);
                return;
            }
            for (int i = 0; i < boxes.size(); i++) {
                AABB box = boxes.get(i);
                float turn = 0, spin = 0, flutter = 0;
                Vec3 animationPivot = pivot;
                BlockState material = ModContent.modelPart(kind,i).defaultBlockState();
                if((kind==RaftPiece.Kind.CHEST || kind==RaftPiece.Kind.ENDER_CHEST) && (i==1 || i==2)) {
                    boolean opened=entity.machines().at(pos).working() || chestPairs.containsKey(pos) && entity.machines().at(chestPairs.get(pos)).working();
                    flutter=-65*transition(entity,pos,"chest_open",opened?1:0,time,false);
                    animationPivot=RaftTransform.world(new Vec3(0,.625,-.4375),pivot,mount);
                } else if(kind==RaftPiece.Kind.BARREL && i==2) {
                    float opening=transition(entity,pos,"barrel_open",entity.machines().at(pos).working()?1:0,time,false);
                    box=box.move(0,opening*.08,0);flutter=-18*opening;animationPivot=box.getCenter();
                } else if((kind==RaftPiece.Kind.GRINDSTONE && i==2) || (kind==RaftPiece.Kind.STONECUTTER && i==1)) {
                    animationPivot=box.getCenter();spin=engines.computeIfAbsent(entity,key->new java.util.HashMap<>())
                            .computeIfAbsent(pos.immutable(),key->new Flywheel()).update(time,entity.machines().at(pos).working());
                } else if(kind==RaftPiece.Kind.LOOM && i==3) {
                    float active=transition(entity,pos,"loom",entity.machines().at(pos).working()?1:0,time,false);
                    box=box.move(0,Math.sin(time*.3)*.025*active,0);
                } else if (kind == RaftPiece.Kind.WIND_STREAMER) {
                    if (i < 2) { material = ModContent.modelPart(kind,i).defaultBlockState(); }
                    else {
                        turn = transition(entity,pos,"wind",(float)RaftTransform.difference(entity.navigation().windHeading(), state.hullYaw)-mount,time,true);
                        flutter = (float)(Math.sin(time*.24 + i*1.1 + pos.getX()) * (3 + i) * entity.navigation().windStrength());
                        animationPivot = pivot.add(0, 1.6875, 0);
                    }
                } else if (kind == RaftPiece.Kind.SAIL) {
                    if (i != 2) { material = ModContent.modelPart(kind,i).defaultBlockState(); }
                    if (i == 2) {
                        float deployed = transition(entity,pos,entity.navigation().enabled(pos),time);
                        if (deployed < .001) { continue; }
                        box = new AABB(box.minX, box.maxY-box.getYsize()*deployed, box.minZ, box.maxX, box.maxY, box.maxZ);
                    }
                    if (i >= 2) {
                        turn = transition(entity,pos,"trim",entity.navigation().trim(pos)-mount,time,true);
                        animationPivot = pivot.add(0, 2, 0);
                        if (i == 2) {
                            // Keep the authored cloth UVs and borders continuous. Rescaling
                            // the complete model into four strips repeats both texture and trim.
                            flutter=(float)(Math.sin(time*.22+pos.getZ())*2.5*entity.navigation().windStrength());
                        }
                    }
                } else if (kind == RaftPiece.Kind.STEERING_WHEEL && i == 2) {
                    // Open rim with four spokes instead of a solid panel.
                    float angle = transition(entity,pos,"wheel",(float)Math.clamp(RaftTransform.difference(entity.navigation().heading(), state.hullYaw) * 3, -135, 135),time,false);
                    Vec3 hub = pivot.add(0, .6875, 0);
                    for (int side = 0; side < 24; side++) {
                        AABB rim = new AABB(-.04, -.27, -.0625, .04, -.23, .0625);
                        AABB spoke = new AABB(-.03125, -.25, -.046875, .03125, .25, .046875);
                        addWheelPart(entity, state, rim, ModContent.modelPart(kind,0).defaultBlockState(), hub, mount, angle + side*15);
                        if (side < 2) { addWheelPart(entity, state, spoke, ModContent.modelPart(kind,1).defaultBlockState(), hub, mount, angle + side*90); }
                    }
                    continue;
                } else if (kind == RaftPiece.Kind.ENGINE && i == 2) {
                    float angle=engines.computeIfAbsent(entity,key->new java.util.HashMap<>()).computeIfAbsent(pos.immutable(),key->new Flywheel())
                            .update(time,entity.navigation().running(pos),entity.navigation().trim(pos)==0?1:-1);
                    Vec3 hub=RaftTransform.world(new Vec3(0,.28,-.57),pivot,mount);
                    for(int spoke=0;spoke<8;spoke++){
                        for(double side:new double[]{-.28,.28}){
                            AABB arm=new AABB(side-.025,0,-.025,side+.025,.44,.025);
                            AABB mounted=RaftTransform.bounds(arm,mount).move(hub);
                            add(entity,state,mounted,ModContent.modelPart(kind,1).defaultBlockState(),hub,mount,0,0,angle+spoke*45);
                        }
                        AABB blade=RaftTransform.bounds(new AABB(-.34,.39,-.08,.34,.47,.08),mount).move(hub);
                        add(entity,state,blade,ModContent.modelPart(kind,0).defaultBlockState(),hub,mount,0,0,angle+spoke*45);
                    }
                    continue;
                } else if (kind.anchor() && i == 3) {
                    animationPivot = box.getCenter();
                    spin = 90 * transition(entity,pos,entity.navigation().enabled(pos),time);
                }
                add(entity, state, box, material, animationPivot, mount, turn, spin, flutter);
            }
        });
    }

    private void animateMachine(RaftEntity entity,RaftRenderState state,BlockPos pos,RaftPiece.Kind kind,double base,double time) {
        var machine=entity.machines().at(pos);
        float active=transition(entity,pos,"machine_activity",machine.working()?1:0,time,false);
        float growth=transition(entity,pos,"crop_growth",machine.ready()?1:machine.progress()/1000F,time,false);
        Vec3 pivot=new Vec3(pos.getX(),base,pos.getZ());
        var piece=entity.pieces().get(pos);
        var viewer=net.minecraft.client.Minecraft.getInstance().player;
        double effectRange=dev.raftocean.config.RaftConfig.EFFECT_DISTANCE.get();
        Vec3 machineWorld=RaftTransform.world(pivot,new Vec3(state.x,state.y,state.z),state.hullYaw);
        boolean effects=effectRange>0 && viewer!=null && viewer.position().distanceToSqr(machineWorld)<=effectRange*effectRange;
        var modelBounds=MachineVisualLayout.envelope(entity.pieceBoxes(pos,piece.withRotation(0))).move(-pivot.x,-pivot.y,-pivot.z);
        float mount=piece.rotation()*90;
        var water=ModContent.MACHINE_WATER.get().defaultBlockState();
        var steam=ModContent.MACHINE_STEAM.get().defaultBlockState();
        var food=ModContent.MACHINE_FOOD.get().defaultBlockState();
        var stem=ModContent.CROP_STEM.get().defaultBlockState();
        var grain=ModContent.CROP_GRAIN.get().defaultBlockState();
        if(kind==RaftPiece.Kind.WATER_TANK || kind==RaftPiece.Kind.FUEL_TANK) {
            float fill=transition(entity,pos,"tank_gauge",kind==RaftPiece.Kind.WATER_TANK?machine.cleanWater()/64F:machine.progress()/1000F,time,false);
            if(fill>.001) {machinePart(entity,state,new AABB(.175,.252,-.374,.208,.252+.375*fill,-.358),water,pivot,mount,0);}
        }
        if(effects && kind==RaftPiece.Kind.SPRINKLER && active>.01) {
            for(int drop=0;drop<6;drop++) {
                double angle=time*.06+drop*Math.PI/3,phase=(time*.045+drop*.16)%1;
                double radius=.18+.5*phase,x=Math.sin(angle)*radius,z=Math.cos(angle)*radius;
                double y=.62+.2*Math.sin(phase*Math.PI)-.4*phase,size=.012*active;
                machinePart(entity,state,new AABB(x-size,y,z-size,x+size,y+.025,z+size),water,pivot,mount,0);
            }
        }
        if(kind==RaftPiece.Kind.BATTERY_CHARGER && active>.01) {
            double pulse=.015+.007*Math.sin(time*.2);
            machinePart(entity,state,new AABB(-.22,.54,-.35,-.22+pulse,.57,-.33),grain,pivot,mount,0);
        }
        if(kind==RaftPiece.Kind.BIOFUEL_GENERATOR && active>.01) {
            double glow=.025+.012*Math.sin(time*.25);
            machinePart(entity,state,new AABB(-.16,.76,-.17,-.16+glow,.79,-.14),grain,pivot,mount,0);
        }
        if(kind==RaftPiece.Kind.PLANTER && machine.crop()!=0) {
            boolean aquatic=machine.crop()==6;
            if(aquatic){add(entity,state,new AABB(-.28,.15,-.28,.28,.17,.28).move(pivot),water,Vec3.ZERO,0,0,0,0);}
            double height=.12+growth*(machine.crop()>=5?1.22:machine.crop()==1?.72:.38);
            for(int plant=0;plant<4;plant++) {
                double x=plant%2*.32-.16,z=plant/2*.32-.16;
                Vec3 root=pivot.add(x,.15,z);
                float sway=(float)(Math.sin(time*.11+plant)*2*entity.navigation().windStrength());
                add(entity,state,new AABB(-.018,0,-.018,.018,height,.018).move(root),stem,root,0,0,0,sway);
                if(machine.crop()>=5){
                    for(int joint=1;joint<=3;joint++){
                        double y=height*joint/4;
                        add(entity,state,new AABB(-.035,y,-.035,.035,y+.035,.035).move(root),grain,root,0,0,0,sway);
                        if(aquatic){add(entity,state,new AABB(-.10,y,-.03,.10,y+.05,.03).move(root),stem,root,0,0,0,sway*2);}
                    }
                }
                for(int leaf=0;leaf<2;leaf++) {
                    double y=height*(.35+leaf*.3),side=leaf==0?-1:1;
                    AABB blade=new AABB(Math.min(0,side*.11),y,-.035,Math.max(.018,side*.11),y+.025,.035).move(root);
                    add(entity,state,blade,stem,root,0,0,0,sway);
                }
                if(machine.crop()==1 && growth>.6){add(entity,state,new AABB(-.035,height-.1,-.035,.035,height+.05,.035).move(root),grain,root,0,0,0,sway);}
            }
        }
        if(kind.purifier() || kind==RaftPiece.Kind.RAIN_COLLECTOR || kind==RaftPiece.Kind.COOKING_POT) {
            int units=kind.purifier()?Math.max(machine.rawWater(),machine.cleanWater()):machine.cleanWater();
            float fill=transition(entity,pos,"water_level",Math.clamp(units/4F,0,1),time,false);
            if(fill>.01 || active>.01) {
                double wave=Math.sin(time*.13)*.005*active;
                AABB waterBox=MachineVisualLayout.water(modelBounds,fill,wave);
                double surface=waterBox.minY;
                machinePart(entity,state,waterBox,water,pivot,mount,0);
                if(effects && active>.01 && kind!=RaftPiece.Kind.RAIN_COLLECTOR) {
                    for(int bubble=0;bubble<3;bubble++) {
                        double x=Math.sin(time*.09+bubble*2)*.15,z=Math.cos(time*.07+bubble*2)*.15;
                        double size=(.015+.012*Math.sin(time*.23+bubble))*active;
                        machinePart(entity,state,new AABB(x-size,surface+.012,z-size,x+size,surface+.025,z+size),steam,pivot,mount,0);
                    }
                }
            }
            if(kind==RaftPiece.Kind.COOKING_POT && active>.01) {
                double surface=MachineVisualLayout.waterHeight(modelBounds,fill,0),top=modelBounds.maxY+.12;
                var stirrer=ModContent.RAFT_DECK.get().defaultBlockState();
                machinePart(entity,state,new AABB(-.016,surface+.015,-.016,.016,top,.016),stirrer,pivot,mount,0);
                machinePart(entity,state,new AABB(-.19,surface+.015,-.025,.19,surface+.045,.025),stirrer,pivot,mount,(float)(time*3*active));
            }
            if(effects && kind==RaftPiece.Kind.RAIN_COLLECTOR && active>.01) {
                for(int drop=0;drop<3;drop++) {
                    double phase=(time*.055+drop*.3)%1;
                    double bottom=MachineVisualLayout.waterHeight(modelBounds,fill,0)+.025;
                    double y=bottom+(1-phase)*(modelBounds.maxY+.55-bottom);
                    double x=(drop-1)*.14;
                    machinePart(entity,state,new AABB(x-.012,y,-.012,x+.012,y+.065,.012),water,pivot,mount,0);
                }
            }
        }
        if(kind.grill() && (active>.01 || machine.ready())) {
            for(int portion=0;portion<2;portion++) {
                double x=portion==0?-.15:.15;
                machinePart(entity,state,new AABB(x-.08,modelBounds.maxY+.008,-.18,x+.08,modelBounds.maxY+.068,.18),food,pivot,mount,0);
            }
        }
        boolean heated=kind.grill() || kind==RaftPiece.Kind.SMELTER || kind.purifier() || kind==RaftPiece.Kind.COOKING_POT;
        if(effects && heated && active>.01) {
            if(kind.grill() || kind==RaftPiece.Kind.SMELTER) for(int spark=0;spark<3;spark++) {
                double size=(.025+.015*Math.sin(time*.27+spark*2))*active;
                double x=(spark-1)*.1,y=kind.grill()?modelBounds.maxY-.12:modelBounds.minY+.28*modelBounds.getYsize();
                double z=kind==RaftPiece.Kind.SMELTER?modelBounds.minZ+.03*modelBounds.getZsize():0;
                AABB ember=new AABB(x-size,y,z-size,x+size,y+size*2,z+size);
                machinePart(entity,state,ember,ModContent.MACHINE_EMBER.get().defaultBlockState(),pivot,mount,0);
            }
            for(int puff=0;puff<3;puff++) {
                double phase=(time*.025+puff*.32)%1,size=(.018+.04*phase)*(1-phase)*active;
                double y=modelBounds.maxY+.025+phase*.6,x=Math.sin(time*.03+puff)*.13;
                machinePart(entity,state,new AABB(x-size,y,-size,x+size,y+size*2,size),steam,pivot,mount,0);
            }
        }
    }

    private void machinePart(RaftEntity entity,RaftRenderState state,AABB local,BlockState visual,Vec3 pivot,float mount,float turn){
        add(entity,state,RaftTransform.bounds(local,mount).move(pivot),visual,pivot,mount,turn,0,0);
    }

    private void addWheelPart(RaftEntity entity, RaftRenderState state, AABB relative, BlockState visual, Vec3 pivot, float mount, float spin) {
        AABB local = RaftTransform.bounds(relative, mount).move(pivot);
        add(entity, state, local, visual, pivot, mount, 0, spin, 0);
    }

    private void add(RaftEntity entity, RaftRenderState state, AABB box, BlockState visual,
            Vec3 pivot, float mount, float turn, float spin, float flutter) {
        MovingBlockRenderState block = new MovingBlockRenderState();
        Vec3 world = RaftTransform.world(box.getCenter(), new Vec3(state.x, state.y, state.z), state.hullYaw);
        BlockPos worldPos = BlockPos.containing(world);
        block.blockState = visual; block.blockPos = worldPos; block.randomSeedPos = BlockPos.containing(box.getCenter());
        if (entity.level() instanceof ClientLevel level) {
            block.biome = level.getBiome(worldPos); block.cardinalLighting = level.cardinalLighting(); block.lightEngine = level.getLightEngine();
        }
        state.cells.add(new RaftRenderState.DeckCell(box, block, pivot, mount, turn, spin, flutter));
    }

    @Override public void submit(RaftRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.rotate(Axis.YP.rotationDegrees(-state.hullYaw));
        for (var cell : state.cells) {
            pose.pushPose();
            Vec3 pivot = cell.pivot();
            pose.translate(pivot.x, pivot.y, pivot.z);
            pose.rotate(Axis.YP.rotationDegrees(-cell.turn()));
            pose.rotate(Axis.YP.rotationDegrees(-cell.mountYaw()));
            pose.rotate(Axis.ZP.rotationDegrees(cell.spin()));
            pose.rotate(Axis.XP.rotationDegrees(cell.flutter()));
            pose.rotate(Axis.YP.rotationDegrees(cell.mountYaw()));
            pose.translate(-pivot.x, -pivot.y, -pivot.z);
            AABB box = cell.localBox();
            // Boxes arrive in the mounted raft frame. Restore their canonical
            // axes before scaling a model, then rotate the geometry as a whole.
            // Swapping only AABB dimensions leaves lids, shafts and textures
            // facing the wrong way on quarter-turned installations.
            box=MachineVisualLayout.canonicalBox(box,pivot,cell.mountYaw());
            pose.translate(pivot.x,pivot.y,pivot.z);
            pose.rotate(Axis.YP.rotationDegrees(-cell.mountYaw()));
            pose.translate(-pivot.x,-pivot.y,-pivot.z);
            pose.translate(box.minX, box.minY, box.minZ);
            pose.scale((float)box.getXsize(), (float)box.getYsize(), (float)box.getZsize());
            var visualId=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(cell.block().blockState.getBlock());
            String meshName="station_"+visualId.getPath();
            if(visualId.getNamespace().equals("raft_ocean") && EntityMeshes.hasMesh(meshName)){
                EntityMeshes.submit(meshName,pose,collector,state.lightCoords,state.outlineColor);
            }else{
                collector.submitMovingBlock(pose, cell.block(), state.outlineColor);
            }
            pose.popPose();
        }
        RaftPlacementGhost.submit(state.ghost,pose,collector);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }
}
