package dev.raftocean.progression;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native inventory acquisition, server research gates and saved discovery. */
public final class StorySchematicCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    private static FakePlayer player(ServerLevel level){
        var p=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"SchematicCheck"));
        p.setData(Research.SCHEMA,1);return p;
    }
    public static int topic(String id){
        for(int i=0;i<Research.TOPICS.length;i++)if(Research.TOPICS[i].id().equals(id))return i;
        throw new IllegalStateException("Missing test project: "+id);
    }
    public static int run(ServerLevel level){
        checks=0;boolean enabled=dev.raftocean.config.RaftConfig.RESEARCH.get();
        try{
            dev.raftocean.config.RaftConfig.RESEARCH.set(true);
            require(StorySchematics.PLANS.stream().map(StorySchematics.Plan::id).toList().equals(
                    List.of("signal_array","tide_drive","bioenergy","waterworks","power_storage","deepwater","reef_armaments")),
                    "Discovery bits remain in their stable saved order");
            var starter=player(level);starter.setData(Research.MATERIALS,Research.ALL_MATERIALS);
            for(String id:List.of("electrical","navigation","smelter","grill","purifier","fasteners"))
                require(Research.state(starter,topic(id))==1,"Pre-story travel and survival have no schematic cycle: "+id);
            for(int index=0;index<StorySchematics.PLANS.size();index++){
                var p=player(level);var plan=StorySchematics.PLANS.get(index);
                p.setData(Research.MATERIALS,Research.ALL_MATERIALS);
                for(String id:plan.projects()){
                    int topic=topic(id);
                    require(Research.state(p,topic)==3 && !Research.study(p,topic),"All materials cannot bypass island schematic: "+id);
                }
                p.setData(Research.MATERIALS,0);
                p.getInventory().setItem(16,new ItemStack(StorySchematics.item(index)));
                p.getInventory().tick();
                require(StorySchematics.known(p,index) && p.getInventory().getItem(16).getCount()==1,
                        "Non-hotbar native inventory tick records discovery without consuming paper: "+plan.id());
                require(p.getData(Research.KNOWLEDGE)==0 && p.getData(Research.MATERIALS)==0,"Discovery grants neither materials nor learned recipes");
                for(String id:plan.projects())require(Research.state(p,topic(id))==0 && !Research.study(p,topic(id)),"Discovered schematic still requires material research");
                p.getInventory().clearContent();
                require(StorySchematics.known(p,index) && !StorySchematics.discover(p,index),"Paper loss and duplicate discovery cannot undo progress");
                p.setData(Research.MATERIALS,Research.ALL_MATERIALS);
                for(String id:plan.projects())require(Research.state(p,topic(id))==1 && Research.study(p,topic(id)) && Research.state(p,topic(id))==2,
                        "Learning is explicit after schematic and material requirements: "+id);
                var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());p.saveWithoutId(saved);
                var restored=player(level);restored.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
                require(StorySchematics.known(restored,index),"Discovery survives player serialization after losing paper");
                for(String id:plan.projects())require(Research.state(restored,topic(id))==2,"Learned schematic project survives reload");
                var respawn=player(level);respawn.copyAttachmentsFrom(p,true);
                require(StorySchematics.known(respawn,index),"Actual death attachment copy retains schematic discovery");
            }
            var old=player(level);old.setData(Research.KNOWLEDGE,1<<topic("engine"));
            require(Research.state(old,topic("engine"))==2 && !StorySchematics.known(old,1),"Existing researched engines stay learned without new paper");
            old.setData(Research.KNOWLEDGE,1<<topic("power"));
            require(Research.allowed(old,new ItemStack(dev.raftocean.survival.Batteries.BASIC.get())),"Old power project retains basic battery crafting after early electronics migration");
            old.setData(Research.KNOWLEDGE,0);old.setData(Research.MATERIALS,Research.ALL_MATERIALS);old.getAbilities().instabuild=true;
            require(Research.state(old,topic("engine"))==1,"Creative may learn without exploration gates");
            old.getAbilities().instabuild=false;dev.raftocean.config.RaftConfig.RESEARCH.set(false);
            require(Research.state(old,topic("engine"))==1,"Research-disabled configuration bypasses schematic gate");
            dev.raftocean.config.RaftConfig.RESEARCH.set(true);
            require(!StorySchematics.discover(old,-1) && !StorySchematics.discover(old,StorySchematics.PLANS.size()),"Invalid discovery indices are rejected");
            Research.unlockAll(old);
            require(old.getData(StorySchematics.DISCOVERED)==StorySchematics.ALL,"Administrative unlock also discovers every schematic");
            var recovering=player(level);
            recovering.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new ItemStack(dev.raftocean.story.IslandContent.WATCH_LOG.get()));
            dev.raftocean.story.IslandContent.WATCH_LOG.get().use(level,recovering,net.minecraft.world.InteractionHand.MAIN_HAND);
            require(StorySchematics.known(recovering,1) && recovering.getMainHandItem().getCount()==1,"Existing journal recovers its island schematic without consuming the journal");
            return checks;
        }finally{dev.raftocean.config.RaftConfig.RESEARCH.set(enabled);}
    }
    private StorySchematicCheck() { }
}
