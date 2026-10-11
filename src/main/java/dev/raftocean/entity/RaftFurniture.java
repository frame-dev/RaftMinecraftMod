package dev.raftocean.entity;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.world.phys.*;

/** Original data-driven geometry shared by picking, collision and generated models. */
public final class RaftFurniture {
    private static final Map<String,List<AABB>> SHAPES=load();
    private static Map<String,List<AABB>> load() {
        try(var stream=RaftFurniture.class.getResourceAsStream("/data/raft_ocean/furniture.json")) {
            if(stream==null)throw new IllegalStateException("Missing furniture geometry");
            var result=new HashMap<String,List<AABB>>();
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
                result.put(entry.getKey(),List.copyOf(boxes));
            }
            return Map.copyOf(result);
        }catch(java.io.IOException error){throw new IllegalStateException("Cannot load furniture geometry",error);}
    }
    public static List<AABB> geometry(RaftPiece.Kind kind) {
        return Objects.requireNonNull(SHAPES.get(kind.id()),"Missing furniture: "+kind);
    }
    /** Bounded visual light; no temporary world blocks are placed by moving lamps. */
    public static int brightness(Vec3 point,List<Vec3> lights) {
        int value=0;
        for(var source:lights)value=Math.max(value,Math.max(0,14-(int)Math.floor(source.distanceTo(point))));
        return value;
    }
    private RaftFurniture() { }
}
