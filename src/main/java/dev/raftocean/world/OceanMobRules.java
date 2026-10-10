package dev.raftocean.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Ocean survival uses its own underwater predator rather than drowned zombies. */
public final class OceanMobRules {
    private OceanMobRules() { }
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity().getType()==EntityTypes.DROWNED
                && event.getLevel() instanceof ServerLevel level && OceanSpawn.isOcean(level)) {
            event.setCanceled(true);
        }
    }
    public static void onTick(EntityTickEvent.Pre event) {
        var entity=event.getEntity();
        if(entity.getType()==EntityTypes.DROWNED && entity.level() instanceof ServerLevel level && OceanSpawn.isOcean(level)) {
            entity.discard();
            event.setCanceled(true);
        }
    }
}
