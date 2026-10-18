package dev.raftocean.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.saveddata.*;

/** Stock belongs to a world position, not to a player's temporary menu. */
public final class OutpostData extends SavedData {
    public record Stock(MerchantOffers offers,long restocked,int revision) {
        public static final Codec<Stock> CODEC=RecordCodecBuilder.create(i->i.group(
                MerchantOffers.CODEC.fieldOf("offers").forGetter(Stock::offers),
                Codec.LONG.fieldOf("restocked").forGetter(Stock::restocked),
                Codec.INT.optionalFieldOf("revision",0).forGetter(Stock::revision)).apply(i,Stock::new));
    }
    public static final Codec<OutpostData> CODEC=Codec.unboundedMap(Codec.STRING,Stock.CODEC).xmap(OutpostData::new,d->d.stock);
    public static final SavedDataType<OutpostData> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("raft_ocean","outposts"),OutpostData::new,CODEC);
    private final Map<String,Stock> stock;
    public OutpostData(){this(Map.of());}
    private OutpostData(Map<String,Stock> stock){this.stock=new HashMap<>(stock);}
    public static OutpostData get(ServerLevel level){return level.getDataStorage().computeIfAbsent(TYPE);}
    public Stock stock(ServerLevel level,net.minecraft.core.BlockPos pos) {
        return stock(level,pos,level.getGameTime());
    }
    Stock stock(ServerLevel level,net.minecraft.core.BlockPos pos,long now) {
        String key=Long.toString(pos.asLong());Stock old=stock.get(key);
        if(old==null || now<old.restocked || now-old.restocked>=dev.raftocean.config.RaftConfig.OUTPOST_RESTOCK.get()*20L) {
            old=new Stock(OutpostTrading.catalog(level),now,1);stock.put(key,old);setDirty();
        } else if(old.revision()<1) {
            // Append the new currency offers once; used legacy stock and its restock deadline survive.
            for(var offer:OutpostTrading.catalog(level))if(offer.getCostA().is(dev.raftocean.salvage.SalvageCompactor.BALE.get()))old.offers().add(offer);
            old=new Stock(old.offers(),old.restocked(),1);stock.put(key,old);setDirty();
        }
        return old;
    }
}
