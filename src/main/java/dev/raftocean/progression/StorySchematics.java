package dev.raftocean.progression;

import com.mojang.serialization.Codec;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.SurvivalContent;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/** Append-only discovery bits; possession records discovery, not recipe learning. */
public final class StorySchematics {
    public record Plan(String id,String island,String loot,List<String> projects) { }
    public static final List<Plan> PLANS=load();
    public static final int ALL=(1<<PLANS.size())-1;
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> DISCOVERED=
            SurvivalContent.ATTACHMENTS.register("story_schematics",()->AttachmentType.builder(()->0)
                    .serialize(Codec.intRange(0,ALL).fieldOf("schematics")).copyOnDeath()
                    .sync((holder,to)->holder==to,net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    private static final List<DeferredItem<Item>> ITEMS=new ArrayList<>();
    static {
        for(int i=0;i<PLANS.size();i++){
            final int index=i;
            ITEMS.add(ModContent.ITEMS.registerItem("schematic_"+PLANS.get(i).id(),p->new SchematicItem(p.stacksTo(1),index)));
        }
    }
    private static List<Plan> load(){
        try(var stream=StorySchematics.class.getResourceAsStream("/data/raft_ocean/story_schematics.json")){
            if(stream==null)throw new IllegalStateException("Missing story schematics");
            var array=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonArray();
            List<Plan> plans=new ArrayList<>();Set<String> ids=new HashSet<>(),projects=new HashSet<>();
            for(var value:array){
                var plan=value.getAsJsonObject();String id=plan.get("id").getAsString();
                if(!id.matches("[a-z_]+") || !ids.add(id))throw new IllegalStateException("Invalid schematic id: "+id);
                List<String> topics=new ArrayList<>();
                for(var topic:plan.getAsJsonArray("projects")){
                    String name=topic.getAsString();
                    if(Arrays.stream(ResearchCatalog.PROJECTS).noneMatch(p->p.id().equals(name)) || !projects.add(name))
                        throw new IllegalStateException("Unknown or repeated schematic project: "+name);
                    topics.add(name);
                }
                if(topics.isEmpty())throw new IllegalStateException("Empty schematic: "+id);
                plans.add(new Plan(id,plan.get("island").getAsString(),plan.get("loot").getAsString(),List.copyOf(topics)));
            }
            if(plans.isEmpty() || plans.size()>30)throw new IllegalStateException("Invalid schematic count");
            return List.copyOf(plans);
        } catch(java.io.IOException error){throw new IllegalStateException("Cannot read schematics",error);}
    }
    public static void initialize() { }
    public static Item item(int index){return ITEMS.get(index).get();}
    public static void display(java.util.function.Consumer<Item> output){ITEMS.forEach(i->output.accept(i.get()));}
    public static int required(int topic){
        if(topic<0 || topic>=Research.TOPICS.length)return -1;
        String id=Research.TOPICS[topic].id();
        for(int i=0;i<PLANS.size();i++)if(PLANS.get(i).projects().contains(id))return i;
        return -1;
    }
    public static boolean known(Player player,int index){return index>=0 && index<PLANS.size() && (player.getData(DISCOVERED)&1<<index)!=0;}
    public static boolean discover(Player player,int index){
        if(player.level().isClientSide() || player.isSpectator() || index<0 || index>=PLANS.size() || known(player,index))return false;
        player.setData(DISCOVERED,player.getData(DISCOVERED)|1<<index);
        player.sendSystemMessage(Component.translatable("raft_ocean.schematic.discovered",
                Component.translatable("item.raft_ocean.schematic_"+PLANS.get(index).id())));
        dev.raftocean.crew.CrewAccess.publish(player);
        if(player.containerMenu instanceof dev.raftocean.menu.ResearchMenu)player.containerMenu.broadcastChanges();
        return true;
    }
    private StorySchematics() { }
}
