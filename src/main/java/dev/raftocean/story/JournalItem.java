package dev.raftocean.story;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class JournalItem extends Item {
    private final int stage;
    public JournalItem(Properties properties,int stage) { super(properties);this.stage=stage; }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(!level.isClientSide()) {
            if(player.isSpectator())return InteractionResult.FAIL;
            if(player.isShiftKeyDown()){ExpeditionNotebook.open(player);return InteractionResult.SUCCESS_SERVER;}
            player.sendSystemMessage(Component.translatable("raft_ocean.story.log."+stage));
            for(int i=0;i<dev.raftocean.progression.StorySchematics.PLANS.size();i++){
                if(dev.raftocean.progression.StorySchematics.PLANS.get(i).island().equals(ReceiverItem.DESTINATIONS[stage]))
                    dev.raftocean.progression.StorySchematics.discover(player,i);
            }
            player.setData(IslandContent.JOURNEY,Math.max(player.getData(IslandContent.JOURNEY),stage+1));
        }
        return InteractionResult.SUCCESS;
    }
}
