package dev.raftocean.story;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;

/** Verifies actual generated side-site loot and clear exploration trails. */
public final class StoryExplorationCheck {
    private StoryExplorationCheck() { }

    public static int run(ServerLevel level,String name,BlockPos center,int size) {
        JsonObject catalog;
        try(var stream=StoryExplorationCheck.class.getResourceAsStream("/data/raft_ocean/story_island_exploration.json")) {
            if(stream==null)throw new IllegalStateException("Missing story exploration catalog");
            catalog=new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),JsonObject.class);
        } catch(java.io.IOException error) { throw new IllegalStateException("Cannot read story exploration catalog",error); }
        var island=catalog.getAsJsonObject(name);
        var origin=new BlockPos(center.getX()-size/2,49,center.getZ()-size/2);
        int checks=0;
        if(island.getAsJsonArray("sites").size()!=4)throw new IllegalStateException("Missing side sites: "+name);
        for(var value:island.getAsJsonArray("sites")) {
            var site=value.getAsJsonObject();
            var entry=site.getAsJsonArray("entry");
            var entrance=origin.offset(entry.get(0).getAsInt(),entry.get(1).getAsInt(),entry.get(2).getAsInt());
            if(!level.getBlockState(entrance.above()).isAir() || !level.getBlockState(entrance.above(2)).isAir())
                throw new IllegalStateException("Side-site entrance blocked: "+name+" "+site.get("kind"));
            checks++;
            var pos=site.getAsJsonArray("pos");
            var anchor=origin.offset(pos.get(0).getAsInt(),pos.get(1).getAsInt(),pos.get(2).getAsInt());
            boolean supplies=false;
            for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++) {
                if(level.getBlockEntity(anchor.offset(dx,0,dz)) instanceof Container container) {
                    for(int slot=0;slot<container.getContainerSize();slot++)if(!container.getItem(slot).isEmpty())supplies=true;
                }
            }
            if(!supplies)throw new IllegalStateException("Side-site loot missing: "+name+" "+site.get("kind"));
            checks++;
        }
        for(var value:island.getAsJsonArray("trails")) {
            var cell=value.getAsJsonArray();
            var floor=origin.offset(cell.get(0).getAsInt(),cell.get(1).getAsInt(),cell.get(2).getAsInt());
            if(!level.getBlockState(floor.above()).isAir() || !level.getBlockState(floor.above(2)).isAir())
                throw new IllegalStateException("Generated exploration trail obstructed: "+name+" "+floor);
            if(level.getBlockState(floor).isAir() || !level.getFluidState(floor).isEmpty())
                throw new IllegalStateException("Generated exploration trail lacks dry support: "+name+" "+floor);
            checks++;
        }
        return checks;
    }
}
