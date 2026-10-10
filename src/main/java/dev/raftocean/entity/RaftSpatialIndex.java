package dev.raftocean.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Supplements the origin-section index for hulls extending into other sections. */
public final class RaftSpatialIndex {
    private static final Map<Level, Map<RaftEntity, Boolean>> RAFTS = new WeakHashMap<>();

    private RaftSpatialIndex() { }

    public static synchronized void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof RaftEntity raft) {
            RAFTS.computeIfAbsent(event.getLevel(), key -> new WeakHashMap<>()).put(raft, Boolean.TRUE);
        }
    }

    public static synchronized void onLeave(EntityLeaveLevelEvent event) {
        // Tracking can stop and resume without another join event. Keep weak entries
        // for temporarily inaccessible entities; intersecting() checks live visibility.
        if (event.getEntity() instanceof RaftEntity raft && raft.isRemoved()) {
            var rafts = RAFTS.get(event.getLevel());
            if (rafts != null) { rafts.remove(raft); }
        }
    }

    public static synchronized void onUnload(LevelEvent.Unload event) {
        RAFTS.remove(event.getLevel());
    }

    public static synchronized List<RaftEntity> intersecting(Level level, AABB area) {
        var rafts = RAFTS.get(level);
        if (rafts == null || rafts.isEmpty()) { return List.of(); }
        List<RaftEntity> result = new ArrayList<>();
        for (RaftEntity raft : rafts.keySet()) {
            // A canceled spawn, unloaded section or removed entity must never become
            // an invisible platform. Entity lookup by ID uses the live tracking index.
            if (!raft.isRemoved() && level.getEntity(raft.getId()) == raft
                    && raft.getBoundingBox().intersects(area)) {
                result.add(raft);
            }
        }
        return result;
    }
}
