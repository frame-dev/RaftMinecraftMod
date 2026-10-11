package dev.raftocean.entity;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.world.phys.*;

/** Original data-driven geometry shared by picking, collision and generated models. */
public final class RaftFurniture {
    private record Definition(List<AABB> visual,List<AABB> collision) { }
    private static final Map<String,Definition> SHAPES=load();
    private static Map<String,Definition> load() {
        try(var stream=RaftFurniture.class.getResourceAsStream("/data/raft_ocean/furniture.json")) {
            if(stream==null)throw new IllegalStateException("Missing furniture geometry");
            var result=new HashMap<String,Definition>();
            var root=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
            for(var entry:root.entrySet()) {
                var definition=entry.getValue().getAsJsonObject();int width=definition.get("width").getAsInt(),depth=definition.get("depth").getAsInt(),height=definition.get("height").getAsInt();
                var boxes=new ArrayList<AABB>();
                for(var part:definition.getAsJsonArray("parts")) {
                    var array=part.getAsJsonObject().getAsJsonArray("box");
                    double[] v=new double[6];for(int i=0;i<6;i++){v[i]=array.get(i).getAsDouble();if(!Double.isFinite(v[i]))throw new IllegalArgumentException("Nonfinite furniture geometry");}
                    var box=new AABB(v[0],v[1],v[2],v[3],v[4],v[5]);
                    if(v[0]>=v[3] || v[1]>=v[4] || v[2]>=v[5] || box.minX<-.5 || box.maxX>width-.5 || box.minZ<-.5 || box.maxZ>depth-.5 || box.minY<0 || box.maxY>height)throw new IllegalArgumentException("Furniture outside footprint: "+entry.getKey());
                    boxes.add(box);
                }
                if(boxes.isEmpty() || boxes.size()>128)throw new IllegalArgumentException("Invalid furniture part count");
                var collision=new ArrayList<AABB>();
                if(definition.has("collision")) {
                    for(var part:definition.getAsJsonArray("collision")) {
                        var values=part.getAsJsonArray();
                        if(values.size()!=6)throw new IllegalArgumentException("Invalid furniture collision coordinates: "+entry.getKey());
                        double[] v=new double[6];
                        for(int i=0;i<6;i++){v[i]=values.get(i).getAsDouble();if(!Double.isFinite(v[i]))throw new IllegalArgumentException("Nonfinite furniture collision");}
                        if(v[0]>=v[3] || v[1]>=v[4] || v[2]>=v[5] || v[0]<-.5 || v[3]>width-.5
                                || v[2]<-.5 || v[5]>depth-.5 || v[1]<0 || v[4]>height)throw new IllegalArgumentException("Furniture collision outside footprint: "+entry.getKey());
                        collision.add(new AABB(v[0],v[1],v[2],v[3],v[4],v[5]));
                    }
                    if(collision.isEmpty() || collision.size()>128)throw new IllegalArgumentException("Invalid furniture collision count");
                } else {
                    AABB body=boxes.getFirst();for(var box:boxes)body=body.minmax(box);collision.add(body);
                }
                result.put(entry.getKey(),new Definition(List.copyOf(boxes),List.copyOf(collision)));
            }
            return Map.copyOf(result);
        }catch(java.io.IOException error){throw new IllegalStateException("Cannot load furniture geometry",error);}
    }
    public static List<AABB> geometry(RaftPiece.Kind kind) {
        return Objects.requireNonNull(SHAPES.get(kind.id()),"Missing furniture: "+kind).visual();
    }
    public static List<AABB> collision(RaftPiece.Kind kind) {
        return Objects.requireNonNull(SHAPES.get(kind.id()),"Missing furniture: "+kind).collision();
    }
    /** Bounded visual light; no temporary world blocks are placed by moving lamps. */
    public static int brightness(Vec3 point,List<Vec3> lights) {
        int value=0;
        for(var source:lights)value=Math.max(value,Math.max(0,14-(int)Math.floor(source.distanceTo(point))));
        return value;
    }
    private RaftFurniture() { }
}
