package dev.raftocean.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.raftocean.RaftOcean;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

/** Per-world starter ownership; player death does not erase the raft reference. */
public final class OceanRaftData extends SavedData {
    public record Home(UUID raft, Vec3 position) {
        public static final Codec<Home> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("raft").forGetter(Home::raft),
                Vec3.CODEC.fieldOf("position").forGetter(Home::position)).apply(instance, Home::new));
    }

    public static final Codec<OceanRaftData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Home.CODEC).fieldOf("homes").forGetter((OceanRaftData data) -> data.homes),
            Home.CODEC.optionalFieldOf("shared").forGetter((OceanRaftData data) -> java.util.Optional.ofNullable(data.shared))
    ).apply(instance, (homes, shared) -> new OceanRaftData(homes, shared.orElse(null))));
    public static final SavedDataType<OceanRaftData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(RaftOcean.MOD_ID, "ocean_rafts"), OceanRaftData::new, CODEC);
    private final Map<UUID, Home> homes;
    private Home shared;

    public OceanRaftData() { this(Map.of()); }
    private OceanRaftData(Map<UUID, Home> homes) { this(homes,null); }
    private OceanRaftData(Map<UUID, Home> homes,Home shared) { this.homes = new LinkedHashMap<>(homes);this.shared=shared; }
    public static OceanRaftData get(ServerLevel level) { return level.getDataStorage().computeIfAbsent(TYPE); }
    public Home home(UUID player) { return homes.get(player); }
    public int size() { return homes.size(); }
    public Home sharedHome() {
        if(shared==null && !homes.isEmpty()){shared=homes.values().iterator().next();setDirty();}
        return shared;
    }
    public long crewSize(UUID raft) { return homes.values().stream().filter(home->home.raft().equals(raft)).count(); }
    public void replaceRaft(UUID old,UUID raft,Vec3 position) {
        homes.replaceAll((player,home)->home.raft().equals(old)?new Home(raft,position):home);
        if(shared!=null && shared.raft().equals(old)){shared=new Home(raft,position);}
        setDirty();
    }

    public void remember(UUID player, UUID raft, Vec3 position) {
        Home home = new Home(raft, position);
        if (!home.equals(homes.put(player, home))) { setDirty(); }
        if(shared==null && dev.raftocean.config.RaftConfig.SHARED_STARTER.get()){shared=home;setDirty();}
    }

    public void update(UUID raft, Vec3 position) {
        if(shared!=null && shared.raft().equals(raft) && !shared.position().equals(position)){shared=new Home(raft,position);setDirty();}
        homes.replaceAll((player, old) -> {
            if (!old.raft().equals(raft) || old.position().equals(position)) { return old; }
            setDirty();
            return new Home(raft, position);
        });
    }
}
