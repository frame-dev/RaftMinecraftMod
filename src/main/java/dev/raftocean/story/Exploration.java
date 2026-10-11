package dev.raftocean.story;

import com.mojang.serialization.Codec;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.SurvivalContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/** Optional branches deliberately never complete or advance the main journey. */
public final class Exploration {
    public static final String[] ROUTES={"kelp_observatory","basalt_refuge","orchard_cove",
            "broken_keel","coral_grotto","drowned_court","chart_vault","merchant_harbor","lantern_market"};
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> CHANNEL=SurvivalContent.ATTACHMENTS.register(
            "exploration_channel",()->AttachmentType.builder(()->-1).serialize(Codec.intRange(-1,8).fieldOf("channel")).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Integer>> DISCOVERIES=SurvivalContent.ATTACHMENTS.register(
            "exploration_logs",()->AttachmentType.builder(()->0).serialize(Codec.intRange(0,7).fieldOf("logs")).copyOnDeath().build());
    public static final DeferredItem<Item> SURVEY=log("survey_log",0);
    public static final DeferredItem<Item> REFUGE=log("refuge_log",1);
    public static final DeferredItem<Item> ORCHARD=log("orchard_log",2);
    private static DeferredItem<Item> log(String id,int bit) {
        return ModContent.ITEMS.registerItem(id,p->new Item(p.stacksTo(1)) {
            @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
                if(!level.isClientSide()) {
                    player.setData(DISCOVERIES,player.getData(DISCOVERIES)|1<<bit);
                    player.sendSystemMessage(Component.translatable("raft_ocean.exploration.log."+bit));
                }
                return InteractionResult.SUCCESS;
            }
        });
    }
    public static void initialize() { }
    private Exploration() { }
}
