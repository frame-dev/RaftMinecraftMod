package dev.raftocean.progression;

import com.mojang.serialization.Codec;
import dev.raftocean.survival.SurvivalContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

/** One-time material study followed by free, explicitly learned recipe projects. */
public final class Research {
    public static final ResearchCatalog.Project[] TOPICS=ResearchCatalog.PROJECTS;
    public static final int ALL_PROJECTS=(1<<TOPICS.length)-1;
    public static final int ALL_MATERIALS=(1<<ResearchCatalog.MATERIALS.length)-1;
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> KNOWLEDGE=
            SurvivalContent.ATTACHMENTS.register("research",()->AttachmentType.builder(()->0)
                    .serialize(Codec.intRange(0,ALL_PROJECTS).fieldOf("knowledge")).copyOnDeath()
                    .sync((holder,to)->holder==to,net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> MATERIALS=
            SurvivalContent.ATTACHMENTS.register("research_materials",()->AttachmentType.builder(()->0)
                    .serialize(Codec.intRange(0,ALL_MATERIALS).fieldOf("materials")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> SCHEMA=
            SurvivalContent.ATTACHMENTS.register("research_schema",()->AttachmentType.builder(()->0)
                    .serialize(Codec.intRange(0,1).fieldOf("schema")).copyOnDeath().build());
    private Research() {}
    public static void initialize() {}
    public static TagKey<Item> tag(String path){return TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("raft_ocean",path));}
    private static void migrate(Player player){
        if(player.level().isClientSide() || player.getData(SCHEMA)==1){return;}
        int old=player.getData(KNOWLEDGE),knowledge=0,samples=player.getData(MATERIALS);
        for(int i=0;i<TOPICS.length;i++){
            if(TOPICS[i].legacy()>=0 && (old&1<<TOPICS[i].legacy())!=0){knowledge|=1<<i;samples|=TOPICS[i].materials();}
        }
        player.setData(KNOWLEDGE,knowledge);player.setData(MATERIALS,samples);player.setData(SCHEMA,1);
    }
    public static boolean materialKnown(Player player,int material){
        migrate(player);return material>=0 && material<ResearchCatalog.MATERIALS.length && (player.getData(MATERIALS)&1<<material)!=0;
    }
    public static int materialIndex(ItemStack sample){
        if(sample.isEmpty()){return -1;}
        for(int i=0;i<ResearchCatalog.MATERIALS.length;i++){
            if(sample.is(tag("research/materials/"+ResearchCatalog.MATERIALS[i].id()))){return i;}
        }
        return -1;
    }
    public static boolean research(Player player,ItemStack sample){
        int material=materialIndex(sample);
        if(player.level().isClientSide() || material<0 || materialKnown(player,material)){return false;}
        sample.shrink(1);player.setData(MATERIALS,player.getData(MATERIALS)|1<<material);return true;
    }
    public static int state(Player player,int topic){
        if(topic<0 || topic>=TOPICS.length){return -1;}migrate(player);
        if((player.getData(KNOWLEDGE)&1<<topic)!=0){return 2;}
        return (player.getData(MATERIALS)&TOPICS[topic].materials())==TOPICS[topic].materials()?1:0;
    }
    public static boolean study(Player player,int topic){
        if(player.level().isClientSide() || state(player,topic)!=1){return false;}
        player.setData(KNOWLEDGE,player.getData(KNOWLEDGE)|1<<topic);
        if(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer){
            ResearchRecipes.reconcile(serverPlayer,true);
            var advancement=serverPlayer.level().getServer().getAdvancements().get(
                    Identifier.fromNamespaceAndPath("raft_ocean","research/"+TOPICS[topic].id()));
            if(advancement!=null){serverPlayer.getAdvancements().award(advancement,"learned");}
        }
        return true;
    }
    public static boolean allowed(Player player,ItemStack output){return blockedProject(player,output)<0;}
    /** Administrative unlock is permanent, consumes no samples, and keeps the player's gamemode. */
    public static void unlockAll(net.minecraft.server.level.ServerPlayer player){
        player.setData(SCHEMA,1);
        player.setData(MATERIALS,ALL_MATERIALS);
        player.setData(KNOWLEDGE,ALL_PROJECTS);
        ResearchRecipes.reconcile(player,true);
        for(var topic:TOPICS){
            var advancement=player.level().getServer().getAdvancements().get(
                    Identifier.fromNamespaceAndPath("raft_ocean","research/"+topic.id()));
            if(advancement!=null){player.getAdvancements().award(advancement,"learned");}
        }
    }
    /** Crafting and conversion share the same output research requirement. */
    public static int blockedCraftingProject(Player player,ItemStack output,net.minecraft.world.item.crafting.CraftingInput input){
        return blockedProject(player,output);
    }
    public static int blockedProject(Player player,ItemStack output){
        if(!dev.raftocean.config.RaftConfig.RESEARCH.get() || player.getAbilities().instabuild){return -1;}
        migrate(player);int mask=player.getData(KNOWLEDGE);
        for(int topic=0;topic<TOPICS.length;topic++){
            if(output.is(tag("research/unlocks/"+TOPICS[topic].id())) && (mask&1<<topic)==0){return topic;}
        }
        return -1;
    }
}
