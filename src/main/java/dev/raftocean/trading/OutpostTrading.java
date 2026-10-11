package dev.raftocean.trading;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.*;
import dev.raftocean.registry.ModContent;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Native trade codecs and a single active session prevent competing stale outputs. */
public final class OutpostTrading implements Merchant {
    public static final DeferredBlock<TradingPostBlock> POST=ModContent.BLOCKS.registerBlock("trading_post",TradingPostBlock::new,p->p.strength(-1,3600000).noOcclusion());
    private static final Map<ServerLevel,Map<BlockPos,OutpostTrading>> ACTIVE=new WeakHashMap<>();
    private final ServerLevel level;
    private final BlockPos pos;
    private final OutpostData data;
    private final MerchantOffers offers;
    private Player customer;
    public OutpostTrading(ServerLevel level,BlockPos pos,Player player) {
        this.level=level;this.pos=pos.immutable();customer=player;data=OutpostData.get(level);offers=data.stock(level,pos).offers();
    }
    public static void initialize() { }
    public static MerchantOffers catalog(ServerLevel level) {
        var resource=level.getServer().getResourceManager().getResource(Identifier.fromNamespaceAndPath("raft_ocean","trading/outpost.json"));
        try(var reader=resource.orElseThrow(()->new IllegalStateException("Missing outpost trade data")).openAsReader()) {
            var offers=MerchantOffers.CODEC.parse(level.registryAccess().createSerializationContext(JsonOps.INSTANCE),JsonParser.parseReader(reader)).getOrThrow();
            if(offers.isEmpty() || offers.size()>64)throw new IllegalStateException("Outpost requires 1–64 offers");
            for(var offer:offers)if(offer.getCostA().isEmpty() || offer.getResult().isEmpty() || offer.getMaxUses()<1
                    || offer.getCostA().getCount()>offer.getCostA().getMaxStackSize()
                    || offer.getCostB().getCount()>offer.getCostB().getMaxStackSize())throw new IllegalStateException("Invalid outpost trade");
            return offers;
        } catch(java.io.IOException error){throw new IllegalStateException("Cannot read outpost trade data",error);}
    }
    public static boolean open(ServerLevel level,BlockPos pos,Player player) {
        if(!dev.raftocean.config.RaftConfig.OUTPOST_TRADING.get() || dev.raftocean.crew.CrewRescue.downed(player) || player.isSpectator())return false;
        var sessions=ACTIVE.computeIfAbsent(level,l->new HashMap<>());
        sessions.entrySet().removeIf(e->e.getValue().customer==null || !e.getValue().customer.isAlive()
                || !(e.getValue().customer.containerMenu instanceof OutpostMenu menu) || menu.merchant()!=e.getValue());
        var old=sessions.get(pos);
        if(old!=null && old.customer!=player) {
            player.sendOverlayMessage(Component.translatable("raft_ocean.trade.busy"));return false;
        }
        if(old!=null)player.closeContainer();
        var merchant=new OutpostTrading(level,pos,player);sessions.put(pos.immutable(),merchant);
        merchant.openTradingScreen(player,Component.translatable("raft_ocean.trade.title"),1);
        return player.containerMenu instanceof OutpostMenu;
    }
    @Override public void openTradingScreen(Player player,Component title,int tier) {
        var container=player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inventory,p)->new OutpostMenu(id,inventory,this),title));
        if(container.isPresent())player.sendMerchantOffers(container.getAsInt(),offers,1,0,false,true);
    }
    @Override public void setTradingPlayer(Player player){customer=player;}
    @Override public Player getTradingPlayer(){return customer;}
    @Override public MerchantOffers getOffers(){return offers;}
    @Override public void overrideOffers(MerchantOffers offers){throw new UnsupportedOperationException("Server stock is authoritative");}
    @Override public void notifyTrade(MerchantOffer offer){offer.increaseUses();data.setDirty();}
    @Override public void notifyTradeUpdated(ItemStack item){ }
    @Override public int getVillagerXp(){return 0;}
    @Override public void overrideXp(int xp){ }
    @Override public boolean showProgressBar(){return false;}
    @Override public boolean canRestock(){return true;}
    @Override public SoundEvent getNotifyTradeSound(){return SoundEvents.VILLAGER_YES;}
    @Override public boolean isClientSide(){return false;}
    @Override public boolean stillValid(Player player) {
        return customer==player && player.isAlive() && !player.isSpectator() && player.level()==level
                && dev.raftocean.config.RaftConfig.OUTPOST_TRADING.get() && !dev.raftocean.crew.CrewRescue.downed(player)
                && level.getBlockState(pos).is(POST.get()) && player.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos))<=64;
    }
    public static void onStop(net.neoforged.neoforge.event.server.ServerStoppedEvent event){ACTIVE.clear();}
}
