package dev.raftocean.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** API v3: register on both logical sides during common setup, using enqueueWork. */
public final class RaftBuildables {
    public static final int API_VERSION=3;
    private static final Map<String,RaftMachineBehavior> MACHINES=new LinkedHashMap<>();
    private static final Map<String,RaftBuildable> TYPES=new LinkedHashMap<>();
    private RaftBuildables() { }
    public static synchronized void register(RaftBuildable type) {
        if(TYPES.putIfAbsent(type.id().toString(),type)!=null) {
            throw new IllegalArgumentException("Duplicate raft buildable: "+type.id());
        }
    }
    public static synchronized void register(RaftBuildable type,RaftMachineBehavior behavior) {
        java.util.Objects.requireNonNull(behavior);
        if(behavior.slots()<9 || behavior.slots()>54 || behavior.slots()%9!=0 || behavior.tickInterval()<1 || behavior.tickInterval()>1200)
            throw new IllegalArgumentException("Invalid machine inventory/tick interval: "+type.id());
        java.util.Objects.requireNonNull(behavior.utilityInputs());
        if(behavior.utilityInputs().stream().anyMatch(java.util.Objects::isNull)
                || behavior.quickHarvestSlots().stream().anyMatch(slot->slot==null || slot<0 || slot>=behavior.slots()))
            throw new IllegalArgumentException("Invalid machine utility/output declarations: "+type.id());
        register(type);MACHINES.put(type.id().toString(),behavior);
    }
    /** Optional adapters must defer resolving foreign classes and registries until their mod is loaded. */
    public static boolean registerOptional(String modId,java.util.function.Supplier<RaftBuildable> definition,
            java.util.function.Supplier<RaftMachineBehavior> behavior) {
        java.util.Objects.requireNonNull(definition);java.util.Objects.requireNonNull(behavior);
        if(modId==null || !modId.matches("[a-z0-9_]+"))throw new IllegalArgumentException("Invalid mod ID");
        if(!net.neoforged.fml.ModList.get().isLoaded(modId))return false;
        register(definition.get(),behavior.get());return true;
    }
    public static synchronized Optional<RaftMachineBehavior> machine(String id) { return Optional.ofNullable(MACHINES.get(id)); }
    public static synchronized Optional<RaftBuildable> find(String id) { return Optional.ofNullable(TYPES.get(id)); }
    public static synchronized Optional<RaftBuildable> forItem(ItemStack stack) {
        return TYPES.values().stream().filter(type->stack.is(type.item().get())).findFirst();
    }
    public static synchronized Map<Identifier,RaftBuildable> definitions() {
        var copy=new LinkedHashMap<Identifier,RaftBuildable>();TYPES.values().forEach(type->copy.put(type.id(),type));
        return Map.copyOf(copy);
    }
}
