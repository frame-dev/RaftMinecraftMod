package dev.raftocean.crew;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Separate save preserves existing raft homes and stable research bit assignments. */
public final class CrewData extends SavedData {
    public record Crew(UUID captain,Map<UUID,String> roles,int knowledge,int materials) {
        public static final Codec<Crew> CODEC=RecordCodecBuilder.create(i->i.group(
                UUIDUtil.CODEC.fieldOf("captain").forGetter(Crew::captain),
                Codec.unboundedMap(UUIDUtil.STRING_CODEC,Codec.STRING).fieldOf("roles").forGetter(Crew::roles),
                Codec.INT.fieldOf("knowledge").forGetter(Crew::knowledge),
                Codec.INT.fieldOf("materials").forGetter(Crew::materials)).apply(i,Crew::new));
    }
    public static final Codec<CrewData> CODEC=Codec.unboundedMap(UUIDUtil.STRING_CODEC,Crew.CODEC)
            .xmap(CrewData::new,d->d.crews);
    public static final SavedDataType<CrewData> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("raft_ocean","crews"),CrewData::new,CODEC);
    private final Map<UUID,Crew> crews;
    public CrewData() { this(Map.of()); }
    private CrewData(Map<UUID,Crew> map) { crews=new LinkedHashMap<>(map); }
    public static CrewData get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }
    public Crew crew(ServerLevel level,UUID raft) {
        UUID captain=dev.raftocean.world.OceanRaftData.get(level).captain(raft);
        if(captain==null)return null;
        if(!crews.containsKey(raft)) { crews.put(raft,new Crew(captain,Map.of(),0,0));setDirty(); }
        return crews.get(raft);
    }
    public String role(ServerLevel level,UUID raft,UUID player) {
        Crew crew=crew(level,raft);
        if(crew==null)return "unclaimed";
        if(crew.captain.equals(player))return "captain";
        var home=dev.raftocean.world.OceanRaftData.get(level).home(player);
        if(home==null || !home.raft().equals(raft))return "guest";
        return crew.roles.getOrDefault(player,"crew");
    }
    public boolean role(ServerLevel level,UUID raft,UUID captain,UUID player,String role) {
        Crew crew=crew(level,raft);
        var home=dev.raftocean.world.OceanRaftData.get(level).home(player);
        if(crew==null || !crew.captain.equals(captain) || crew.captain.equals(player)
                || home==null || !home.raft().equals(raft) || !Set.of("crew","guest").contains(role))return false;
        var roles=new HashMap<>(crew.roles);roles.put(player,role);
        crews.put(raft,new Crew(crew.captain,Map.copyOf(roles),crew.knowledge,crew.materials));setDirty();return true;
    }
    public void guest(ServerLevel level,UUID raft,UUID player) {
        Crew crew=crew(level,raft);
        if(crew==null || crew.captain.equals(player))return;
        var roles=new HashMap<>(crew.roles);roles.put(player,"guest");
        crews.put(raft,new Crew(crew.captain,Map.copyOf(roles),crew.knowledge,crew.materials));setDirty();
    }
    public void share(ServerLevel level,UUID raft,int knowledge,int materials) {
        Crew c=crew(level,raft);if(c==null)return;
        int k=(c.knowledge|knowledge)&dev.raftocean.progression.Research.ALL_PROJECTS;
        int m=(c.materials|materials)&dev.raftocean.progression.Research.ALL_MATERIALS;
        if(k!=c.knowledge || m!=c.materials) {crews.put(raft,new Crew(c.captain,c.roles,k,m));setDirty();}
    }
    public void replace(UUID old,UUID replacement) {
        Crew crew=crews.remove(old);if(crew!=null){crews.put(replacement,crew);setDirty();}
    }
}
