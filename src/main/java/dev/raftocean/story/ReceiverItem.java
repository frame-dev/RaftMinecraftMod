package dev.raftocean.story;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import java.util.Map;
import java.util.WeakHashMap;

/** Bounded native structure-location search, cached per player to avoid repeated chunk work. */
public final class ReceiverItem extends Item {
    public static final String[] DESTINATIONS={"radio_tower","glasswatch","garden_relay","echo_vault",
            "ember_beacon","tideworks_haven","starfall_archive"};
    private record Signal(ServerLevel level,int stage,BlockPos origin,long until,BlockPos target) { }
    private final Map<Player,Signal> signals=new WeakHashMap<>();
    private final boolean upgraded;
    public ReceiverItem(Properties properties,boolean upgraded) { super(properties);this.upgraded=upgraded; }
    public static BlockPos islandCenter(BlockPos nativeLocate) { return new BlockPos(nativeLocate.getX()+8,65,nativeLocate.getZ()+8); }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(!(level instanceof ServerLevel server)){return InteractionResult.SUCCESS;}
        if(!(server.getChunkSource().getGenerator() instanceof dev.raftocean.world.OceanChunkGenerator)) {
            player.sendOverlayMessage(Component.translatable("raft_ocean.story.ocean_only"));return InteractionResult.FAIL;
        }
        int stage=player.getData(IslandContent.JOURNEY);
        if(player.isShiftKeyDown()) {
            if(!upgraded){player.sendOverlayMessage(Component.translatable("raft_ocean.exploration.upgrade"));return InteractionResult.FAIL;}
            int channel=(player.getData(Exploration.CHANNEL)+2)%(Exploration.ROUTES.length+1)-1;
            player.setData(Exploration.CHANNEL,channel);
            player.sendSystemMessage(Component.translatable("raft_ocean.exploration.channel",
                    channel<0?Component.translatable("raft_ocean.exploration.story"):Component.translatable("raft_ocean.island."+Exploration.ROUTES[channel])));
        }
        int branch=upgraded?player.getData(Exploration.CHANNEL):-1;
        if(branch>=0)stage=DESTINATIONS.length+branch;
        String destination=branch>=0?Exploration.ROUTES[branch]:stage<DESTINATIONS.length?DESTINATIONS[stage]:null;
        if(destination==null){player.sendSystemMessage(Component.translatable("raft_ocean.story.complete"));return InteractionResult.SUCCESS_SERVER;}
        var origin=player.blockPosition();var signal=signals.get(player);
        if(signal==null || signal.level!=server || signal.stage!=stage || signal.until<server.getGameTime()
                || signal.origin.distSqr(origin)>128*128) {
            var tag=TagKey.create(Registries.STRUCTURE,Identifier.fromNamespaceAndPath("raft_ocean",destination));
            BlockPos target=server.findNearestMapStructure(tag,origin,upgraded?6:3,false);
            if(target!=null){target=islandCenter(target);}
            signal=new Signal(server,stage,origin,server.getGameTime()+1200,target);signals.put(player,signal);
        }
        if(signal.target==null || signal.target.distSqr(origin)>(upgraded?8192L*8192:3072L*3072)) {
            player.sendSystemMessage(Component.translatable("raft_ocean.story.no_signal"));return InteractionResult.SUCCESS_SERVER;
        }
        var target=signal.target;int dx=target.getX()-origin.getX(),dz=target.getZ()-origin.getZ();
        int distance=(int)Math.round(Math.hypot(dx,dz));
        double bearing=dev.raftocean.navigation.RaftNavigation.normalize(Math.toDegrees(Math.atan2(-dx,dz)));
        player.sendSystemMessage(Component.translatable("raft_ocean.story.signal",Component.translatable("raft_ocean.island."+destination),
                target.getX(),target.getZ(),distance,(int)Math.round(bearing)));
        return InteractionResult.SUCCESS_SERVER;
    }
}
