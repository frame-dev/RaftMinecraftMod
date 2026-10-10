package dev.raftocean.client.render;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Original textured polygon meshes, reloaded with resource packs and F3+T. */
public final class EntityMeshes extends SimplePreparableReloadListener<Map<Identifier,List<EntityMeshes.Mesh>>> {
    public record Quad(float[][] positions,float[][] uv,float[] normal) {}
    public record Mesh(Identifier texture,List<Quad> quads) {}
    private static volatile Map<Identifier,List<Mesh>> meshes=Map.of();
    private static volatile Map<String,List<Mesh>> namedMeshes=Map.of();
    @Override protected Map<Identifier,List<Mesh>> prepare(ResourceManager manager,ProfilerFiller profiler){
        Map<Identifier,List<Mesh>> result=new HashMap<>();
        for(var entry:manager.listResources("entity_geometry",id->id.getNamespace().equals("raft_ocean") && id.getPath().endsWith(".json")).entrySet()){
            try(var reader=entry.getValue().openAsReader()){
                var object=JsonParser.parseReader(reader).getAsJsonObject();
                List<Mesh> surfaces=new ArrayList<>();
                var groups=new com.google.gson.JsonArray();
                if(object.has("surfaces")){groups=object.getAsJsonArray("surfaces");}else{groups.add(object);}
                for(var group:groups){
                var surface=group.getAsJsonObject();List<Quad> quads=new ArrayList<>();
                for(var value:surface.getAsJsonArray("quads")){
                    var quad=value.getAsJsonObject();float[][] positions=new float[4][3],uv=new float[4][2];
                    for(int vertex=0;vertex<4;vertex++){
                        for(int axis=0;axis<3;axis++){positions[vertex][axis]=quad.getAsJsonArray("positions").get(vertex).getAsJsonArray().get(axis).getAsFloat();}
                        for(int axis=0;axis<2;axis++){uv[vertex][axis]=quad.getAsJsonArray("uv").get(vertex).getAsJsonArray().get(axis).getAsFloat();}
                    }
                    float[] normal=new float[3];for(int axis=0;axis<3;axis++){normal[axis]=quad.getAsJsonArray("normal").get(axis).getAsFloat();}
                    quads.add(new Quad(positions,uv,normal));
                }
                if(quads.isEmpty()){throw new IllegalArgumentException("Empty mesh");}
                surfaces.add(new Mesh(Identifier.parse(surface.get("texture").getAsString()),List.copyOf(quads)));
                }
                result.put(entry.getKey(),List.copyOf(surfaces));
            }catch(Exception error){throw new IllegalStateException("Invalid entity mesh: "+entry.getKey(),error);}
        }
        return Map.copyOf(result);
    }
    @Override protected void apply(Map<Identifier,List<Mesh>> result,ResourceManager manager,ProfilerFiller profiler){
        meshes=result;
        Map<String,List<Mesh>> names=new HashMap<>();
        result.forEach((id,value)->{String path=id.getPath();names.put(path.substring("entity_geometry/".length(),path.length()-5),value);});
        namedMeshes=Map.copyOf(names);
    }
    public static boolean hasMesh(String name){return namedMeshes.containsKey(name);}
    public static void submit(String name,PoseStack pose,SubmitNodeCollector collector,int light,int outline){
        var mesh=namedMeshes.get(name);if(mesh==null){throw new IllegalStateException("Missing custom entity mesh: "+name);}
        for(var surface:mesh){
            submit(surface,pose,collector,light,-1,false);
            if(outline!=0){submit(surface,pose,collector,light,outline,true);}
        }
    }
    private static void submit(Mesh mesh,PoseStack pose,SubmitNodeCollector collector,int light,int color,boolean outline){
        collector.submitCustomGeometry(pose,outline?RenderTypes.outline(mesh.texture()):RenderTypes.entityCutout(mesh.texture()),(frame,buffer)->{
            for(var quad:mesh.quads()){
                for(int vertex=0;vertex<4;vertex++){
                    var p=quad.positions()[vertex];var uv=quad.uv()[vertex];var n=quad.normal();
                    buffer.addVertex(frame,p[0],p[1],p[2]).setColor(color).setUv(uv[0],uv[1])
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(frame,n[0],n[1],n[2]);
                }
            }
        });
    }
}
