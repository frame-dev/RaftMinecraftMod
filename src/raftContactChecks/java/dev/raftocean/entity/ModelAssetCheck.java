package dev.raftocean.entity;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import dev.raftocean.client.render.MachineVisualLayout;

/** Uses the pinned Minecraft model parser without starting the user's client. */
public final class ModelAssetCheck {
    public static void main(String[] args) throws Exception {
        var gson=new GsonBuilder()
                .registerTypeAdapter(CuboidFace.class,new CuboidFace.Deserializer())
                .registerTypeAdapter(CuboidModelElement.class,new CuboidModelElement.Deserializer())
                .registerTypeAdapter(ItemTransform.class,new ItemTransform.Deserializer()).create();
        int models=0,elements=0,inventoryModels=0;
        try(var paths=Files.walk(Path.of("src/main/resources/assets/raft_ocean/models"))){
            for(var path:paths.toList()){
                if(!Files.isRegularFile(path) || !path.toString().endsWith(".json")){continue;}
                var object=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if(!object.has("elements")){continue;}
                for(var element:object.getAsJsonArray("elements")){
                    for(var face:element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
                        var uv=face.getValue().getAsJsonObject().getAsJsonArray("uv");
                        if(uv!=null)for(var value:uv) {
                            double coordinate=value.getAsDouble();
                            if(!Double.isFinite(coordinate) || coordinate<0 || coordinate>16)
                                throw new IllegalStateException("Texture UV leaves its atlas sprite: "+path+"/"+face.getKey());
                        }
                    }
                    gson.fromJson(element,CuboidModelElement.class);elements++;
                }
                if(object.has("display") && object.getAsJsonObject("display").has("gui")){
                    var transform=gson.fromJson(object.getAsJsonObject("display").get("gui"),ItemTransform.class);
                    var pose=new PoseStack();transform.apply(false,pose.last());
                    float minX=Float.POSITIVE_INFINITY,minY=minX,maxX=Float.NEGATIVE_INFINITY,maxY=maxX;
                    for(var value:object.getAsJsonArray("elements")){
                        var e=gson.fromJson(value,CuboidModelElement.class);
                        for(int corner=0;corner<8;corner++){
                            var p=new Vector3f((corner&1)==0?e.from().x():e.to().x(),
                                    (corner&2)==0?e.from().y():e.to().y(),(corner&4)==0?e.from().z():e.to().z()).mul(1F/16);
                            if(e.rotation()!=null){
                                p.sub(e.rotation().origin());e.rotation().transform().transformPosition(p);p.add(e.rotation().origin());
                            }
                            pose.last().pose().transformPosition(p);
                            minX=Math.min(minX,p.x);maxX=Math.max(maxX,p.x);minY=Math.min(minY,p.y);maxY=Math.max(maxY,p.y);
                        }
                    }
                    float span=Math.max(maxX-minX,maxY-minY);
                    if(span<.65F || span>.945F || Math.abs(minX+maxX)>.002F || Math.abs(minY+maxY)>.002F){
                        throw new IllegalStateException("Inventory silhouette is small, clipped or off-center: "+path+" span="+span);
                    }
                    inventoryModels++;
                }
                models++;
            }
        }
        if(models<216 || elements<1000){throw new IllegalStateException("Fine model assets are missing");}
        int expectedItems=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/raft_ocean/model_coverage.json"))).getAsJsonObject().getAsJsonArray("items").size();
        expectedItems += JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/raft_ocean/model_coverage.json"))).getAsJsonObject().getAsJsonArray("item_variants").size();
        if(inventoryModels!=expectedItems){throw new IllegalStateException("Missing inventory fit checks: "+inventoryModels);}
        System.out.println(models+" models and "+elements+" elements accepted by the native Minecraft model parser.");
        System.out.println(inventoryModels+" GUI silhouettes fitted and centered using native ItemTransform matrices.");
        verifyMachineMotion();
        verifyMachineAlignment();
        verifyPolygonMeshes();
    }

    private static void verifyPolygonMeshes() throws Exception {
        int meshes=0,quads=0;
        try(var paths=Files.walk(Path.of("src/main/resources/assets/raft_ocean/entity_geometry"))) {
            for(var path:paths.filter(p->p.toString().endsWith(".json")).toList()) {
                var object=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                var surfaces=new com.google.gson.JsonArray();
                if(object.has("surfaces"))surfaces=object.getAsJsonArray("surfaces");else surfaces.add(object);
                if(surfaces.isEmpty())throw new IllegalStateException("Empty polygon model: "+path);
                for(var surface:surfaces) {
                    var faces=surface.getAsJsonObject().getAsJsonArray("quads");
                    if(faces==null || faces.isEmpty())throw new IllegalStateException("Empty material group: "+path);
                    for(var face:faces) {
                        var quad=face.getAsJsonObject();
                        for(String name:java.util.List.of("positions","uv")) {
                            var values=quad.getAsJsonArray(name);
                            if(values.size()!=4)throw new IllegalStateException("Invalid polygon vertices: "+path);
                            for(var vertex:values) {
                                if(vertex.getAsJsonArray().size()!=(name.equals("positions")?3:2))throw new IllegalStateException("Invalid polygon attributes: "+path);
                                for(var value:vertex.getAsJsonArray())if(!Float.isFinite(value.getAsFloat()))throw new IllegalStateException("Nonfinite polygon vertex: "+path);
                            }
                        }
                        var normal=quad.getAsJsonArray("normal");
                        if(normal.size()!=3)throw new IllegalStateException("Invalid polygon normal: "+path);
                        double length=0;for(var value:normal){double n=value.getAsDouble();length+=n*n;}
                        if(!Double.isFinite(length) || Math.abs(length-1)>.0001)throw new IllegalStateException("Unnormalized polygon normal: "+path);
                        quads++;
                    }
                }
                meshes++;
            }
        }
        System.out.println(meshes+" polygon meshes and "+quads+" quads passed material/vertex/normal validation.");
    }

    private static void verifyMachineMotion() {
        var rotor = new dev.raftocean.client.render.MachineRotor();
        float before = rotor.update(1000, 0, 3);
        for (int frame=1;frame<=40;frame++) {
            float angle = rotor.update(1000+frame*.25, 1, 3);
            if (angle < before || angle-before > .76F) { throw new IllegalStateException("Machine rotor jumps during start"); }
            before = angle;
        }
        for (int frame=1;frame<=200;frame++) {
            float angle = rotor.update(1010+frame*.25, 0, 3);
            if (angle < before || angle-before > .76F) { throw new IllegalStateException("Machine rotor reverses during stop"); }
            before = angle;
        }
        if (Math.abs(rotor.update(2000,0,3)-before)>.0001F
                || Math.abs(rotor.update(Double.NaN,1,3)-before)>.0001F) {
            throw new IllegalStateException("Resuming an unseen machine changes its rotor phase");
        }
        var bounds = new AABB(-.4,0,-.4,.4,.2,.4);
        var bricks = MachineVisualLayout.detail(bounds,.14,.735,.27,.44,1,.73);
        if(bricks.minY<bounds.minY || bricks.maxY>bounds.maxY || bricks.minX<bounds.minX || bricks.maxZ>bounds.maxZ) {
            throw new IllegalStateException("Drying bricks leave the authored tray envelope");
        }
        System.out.println("243 machine rotor continuity and normalized-detail checks passed.");
    }

    private static void verifyMachineAlignment() throws Exception {
        var layouts=JsonParser.parseString(Files.readString(Path.of("build/model-layouts.json"))).getAsJsonObject();
        int checks=0;
        for(var entry:layouts.entrySet()){
            var boxes=new java.util.ArrayList<AABB>();
            for(var value:entry.getValue().getAsJsonArray()){
                var b=value.getAsJsonArray();boxes.add(new AABB(b.get(0).getAsDouble(),b.get(1).getAsDouble(),b.get(2).getAsDouble(),
                        b.get(3).getAsDouble(),b.get(4).getAsDouble(),b.get(5).getAsDouble()));
            }
            var envelope=MachineVisualLayout.envelope(boxes);
            if(entry.getKey().equals("grill") && Math.abs(envelope.minY)>.00001){throw new IllegalStateException("Grill base must include its legs");}
            for(var box:boxes) for(int turn=0;turn<4;turn++){
                float yaw=turn*90;
                var pivot=new Vec3(.13,2.25,-.27);
                var mounted=RaftTransform.bounds(box,yaw).move(3,2.25,7);
                var restored=MachineVisualLayout.canonicalBox(mounted,pivot,yaw);
                var pose=new PoseStack();pose.translate(pivot.x,pivot.y,pivot.z);
                pose.rotate(com.mojang.math.Axis.YP.rotationDegrees(-yaw));pose.translate(-pivot.x,-pivot.y,-pivot.z);
                for(int corner=0;corner<8;corner++){
                    var p=new Vector3f((float)((corner&1)==0?restored.minX:restored.maxX),
                            (float)((corner&2)==0?restored.minY:restored.maxY),(float)((corner&4)==0?restored.minZ:restored.maxZ));
                    pose.last().pose().transformPosition(p);
                    if(p.x<mounted.minX-.0001 || p.x>mounted.maxX+.0001 || p.y<mounted.minY-.0001 || p.y>mounted.maxY+.0001
                            || p.z<mounted.minZ-.0001 || p.z>mounted.maxZ+.0001){throw new IllegalStateException("Mounted geometry left its bounds: "+entry.getKey());}
                    checks++;
                }
            }
            if(java.util.Set.of("purifier","rain_collector","cooking_pot").contains(entry.getKey())){
                for(int level=0;level<=4;level++)for(double wave:new double[]{-.1,0,.1}){
                    var water=MachineVisualLayout.water(envelope,level/4F,wave);
                    double radius=MachineVisualLayout.VESSEL_INNER_RADIUS*envelope.getXsize();
                    if(water.minY<=envelope.minY+MachineVisualLayout.VESSEL_FLOOR*envelope.getYsize()
                            || water.maxY>=envelope.minY+.94*envelope.getYsize()
                            || water.getXsize()/2>=radius || Math.abs(water.getCenter().x-envelope.getCenter().x)>.00001
                            || Math.abs(water.getCenter().z-envelope.getCenter().z)>.00001){throw new IllegalStateException("Vessel fill intersects its model: "+entry.getKey());}
                    checks++;
                }
            }
        }
        System.out.println(checks+" machine fill and mounted-geometry alignment checks passed.");
    }
}
