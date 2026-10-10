package dev.raftocean.client;

import dev.raftocean.progression.Research;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Optional recipe viewers refresh when the owner's synchronized research changes. */
@EventBusSubscriber(modid="raft_ocean",value=Dist.CLIENT)
public final class ResearchRecipeVisibility {
    public static Runnable changed=()->{};
    private static Object previousPlayer;
    private static int previousMask=-1;
    private static boolean previousEnabled,previousCreative;
    @SubscribeEvent public static void onTick(ClientTickEvent.Post event) {
        var player=Minecraft.getInstance().player;
        int mask=player==null?0:player.getData(Research.KNOWLEDGE);
        boolean enabled=player!=null && dev.raftocean.config.RaftConfig.RESEARCH.get();
        boolean creative=player!=null && player.getAbilities().instabuild;
        if(player!=previousPlayer || mask!=previousMask || enabled!=previousEnabled || creative!=previousCreative){
            previousPlayer=player;previousMask=mask;previousEnabled=enabled;previousCreative=creative;changed.run();
        }
    }
    private ResearchRecipeVisibility() { }
}
