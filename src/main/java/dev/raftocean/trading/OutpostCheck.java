package dev.raftocean.trading;

import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native result pickup, shift-click, persistence and session/reach safeguards. */
public final class OutpostCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    public static int run(ServerLevel level) {
        checks=0;BlockPos pos=new BlockPos(24,level.getMaxY()-12,24);
        var before=level.getBlockState(pos);level.setBlock(pos,OutpostTrading.POST.get().defaultBlockState(),3);
        var player=new TradePlayer(level);var other=new TradePlayer(level);
        player.setPos(24.5,pos.getY()+1,24.5);other.setPos(player.position());
        boolean enabled=dev.raftocean.config.RaftConfig.OUTPOST_TRADING.get();
        try {
            dev.raftocean.config.RaftConfig.OUTPOST_TRADING.set(true);
            OutpostData.get(level).stock(level,pos,level.getGameTime()+dev.raftocean.config.RaftConfig.OUTPOST_RESTOCK.get()*20L);
            var catalog=OutpostTrading.catalog(level);require(catalog.size()==25,"Twenty-five native data-driven barter offers load");
            for(var offer:catalog)require(offer.getResult().getCount()<=offer.getResult().getMaxStackSize(),"Trade output fits a native stack");
            require(OutpostTrading.open(level,pos,player),"Counter opens native merchant screen without merchant entity");
            require(!OutpostTrading.open(level,pos,other),"One counter serializes competing player sessions");
            var menu=(OutpostMenu)player.containerMenu;
            var first=menu.getOffers().getFirst();int uses=first.getUses();
            menu.setSelectionHint(0);menu.getSlot(0).set(new ItemStack(Items.STONE,4));
            require(menu.getSlot(2).getItem().isEmpty(),"Wrong fish payment gives no result");
            menu.getSlot(0).set(new ItemStack(Items.COD,3));require(menu.getSlot(2).getItem().isEmpty(),"Insufficient fish gives no result");
            menu.getSlot(0).set(new ItemStack(Items.COD,4));require(menu.getSlot(2).getItem().is(Items.WHEAT_SEEDS),"Exact fish barter shows seeds");
            menu.clicked(2,0,ContainerInput.PICKUP,player);
            require(menu.getCarried().is(Items.WHEAT_SEEDS) && menu.getCarried().getCount()==6 && menu.getSlot(0).getItem().isEmpty(),"Native result pickup consumes exactly four cod");
            require(first.getUses()==uses+1,"Successful trade spends shared stock once");menu.setCarried(ItemStack.EMPTY);
            menu.getSlot(0).set(new ItemStack(Items.COD,4));menu.quickMoveStack(player,2);
            require(player.getInventory().countItem(Items.WHEAT_SEEDS)==6 && menu.getSlot(0).getItem().isEmpty(),"Shift-trade works without vanilla Entity cast");
            require(first.getUses()==uses+2,"Shift-trade also spends stock once");
            var data=OutpostData.get(level);var ops=level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            var restored=OutpostData.CODEC.parse(ops,OutpostData.CODEC.encodeStart(ops,data).getOrThrow()).getOrThrow();
            require(restored.stock(level,pos).offers().getFirst().getUses()==first.getUses(),"Native saved stock retains used trades across reload");
            var jsonOps=level.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
            var legacy=OutpostData.CODEC.encodeStart(jsonOps,data).getOrThrow().getAsJsonObject();
            for(var entry:legacy.entrySet()){
                var stock=entry.getValue().getAsJsonObject();stock.remove("revision");
                var recipes=stock.getAsJsonObject("offers").getAsJsonArray("Recipes");while(recipes.size()>20)recipes.remove(recipes.size()-1);
            }
            var migrated=OutpostData.CODEC.parse(jsonOps,legacy).getOrThrow();var migratedStock=migrated.stock(level,pos);
            require(migratedStock.offers().size()==25 && migratedStock.offers().getFirst().getUses()==first.getUses()
                    && migratedStock.restocked()==data.stock(level,pos).restocked(),"Legacy stock gains bale offers while retaining used stock and restock deadline");
            require(migrated.stock(level,pos).offers().size()==25,"Repeated opening cannot duplicate migrated offers");
            menu.setSelectionHint(22);menu.getSlot(0).set(new ItemStack(dev.raftocean.salvage.SalvageCompactor.BALE.get(),1));
            require(menu.getSlot(2).getItem().isEmpty(),"Bale decoration requires full price");
            menu.getSlot(0).set(new ItemStack(dev.raftocean.salvage.SalvageCompactor.BALE.get(),2));menu.quickMoveStack(player,2);
            require(menu.getOffers().get(22).getUses()==1 && menu.getSlot(0).getItem().isEmpty()
                    && player.getInventory().countItem(dev.raftocean.registry.ModContent.structuralItem(dev.raftocean.entity.RaftPiece.Kind.TABLE_LAMP))==1,"Native bale trade buys a usable lamp and consumes exact currency");
            menu.setSelectionHint(10);var decor=menu.getOffers().get(10);
            menu.getSlot(0).set(new ItemStack(dev.raftocean.registry.ModContent.SCRAP.get(),6));
            require(menu.getSlot(2).getItem().isEmpty(),"Valuable decoration requires both payment ingredients");
            menu.getSlot(1).set(new ItemStack(Items.GOLD_NUGGET,3));
            require(!menu.getSlot(2).getItem().isEmpty(),"Scrap and recovered gold buy original raft decoration");
            menu.quickMoveStack(player,2);require(decor.getUses()==1 && menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty(),"Two-input trade consumes exact scrap and gold");
            menu.setSelectionHint(0);
            while(first.getUses()<first.getMaxUses())first.increaseUses();
            menu.getSlot(0).set(new ItemStack(Items.COD,4));require(menu.getSlot(2).getItem().isEmpty(),"Exhausted stock cannot be purchased");
            player.closeContainer();require(OutpostTrading.open(level,pos,other),"Closing releases counter for another player");other.closeContainer();
            var old=data.stock(level,pos);var fresh=data.stock(level,pos,old.restocked()+dev.raftocean.config.RaftConfig.OUTPOST_RESTOCK.get()*20L);
            require(fresh.offers().getFirst().getUses()==0,"Configured elapsed time restocks exhausted offers");
            require(OutpostTrading.open(level,pos,player),"Restocked counter reopens");menu=(OutpostMenu)player.containerMenu;
            menu.getSlot(0).set(new ItemStack(Items.COD,4));player.setPos(200,pos.getY(),200);
            require(!menu.stillValid(player) && menu.quickMoveStack(player,2).isEmpty(),"Walking away cannot take a stale output");player.closeContainer();
            player.setPos(24.5,pos.getY()+1,24.5);require(OutpostTrading.open(level,pos,player),"Nearby visitor can reopen");
            dev.raftocean.config.RaftConfig.OUTPOST_TRADING.set(false);
            require(!player.containerMenu.stillValid(player) && !OutpostTrading.open(level,pos,other),"Trading disable affects open and new screens");player.closeContainer();
        } finally {
            player.closeContainer();other.closeContainer();player.discard();other.discard();
            level.setBlock(pos,before,3);dev.raftocean.config.RaftConfig.OUTPOST_TRADING.set(enabled);
        }
        return checks;
    }
    private static final class TradePlayer extends FakePlayer {
        TradePlayer(ServerLevel level){super(level,new GameProfile(UUID.randomUUID(),"OutpostCheck"));}
        @Override public java.util.OptionalInt openMenu(MenuProvider provider){
            closeContainer();containerMenu=provider.createMenu(7,getInventory(),this);return java.util.OptionalInt.of(7);
        }
    }
    private OutpostCheck() { }
}
