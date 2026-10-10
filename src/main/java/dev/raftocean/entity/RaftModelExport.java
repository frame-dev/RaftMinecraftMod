package dev.raftocean.entity;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;

/** Exports canonical visual geometry for reproducible data-driven custom model assets. */
public final class RaftModelExport {
    private RaftModelExport() { }
    public static void write() {
        Map<String,List<double[]>> shapes=new LinkedHashMap<>();
        for(var kind:RaftPiece.Kind.values()) {
            shapes.put(kind.id(),new RaftPiece(kind,0,false).boxes(new BlockPos(0,1,0)).stream()
                    .map(box->new double[]{box.minX,box.minY-.25,box.minZ,box.maxX,box.maxY-.25,box.maxZ}).toList());
        }
        try {
            Path path=Path.of("..","model-layouts.json");
            Files.writeString(path,new GsonBuilder().setPrettyPrinting().create().toJson(shapes));
            Map<String,List<String>> catalog=new LinkedHashMap<>();
            catalog.put("items",net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().stream().filter(id->id.getNamespace().equals("raft_ocean")).map(id->id.getPath()).sorted().toList());
            catalog.put("blocks",net.minecraft.core.registries.BuiltInRegistries.BLOCK.keySet().stream().filter(id->id.getNamespace().equals("raft_ocean")).map(id->id.getPath()).sorted().toList());
            catalog.put("entities",net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(id->id.getNamespace().equals("raft_ocean")).map(id->id.getPath()).sorted().toList());
            Files.writeString(Path.of("..","model-registry.json"),new GsonBuilder().setPrettyPrinting().create().toJson(catalog));
        } catch(java.io.IOException error) { throw new IllegalStateException("Cannot export raft model geometry",error); }
    }
}
