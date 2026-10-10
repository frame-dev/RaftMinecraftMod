package dev.raftocean.menu;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Native XP, repair, naming and hooks; the raft anvil never damages a terrain block beneath it. */
public final class RaftAnvilMenu extends AnvilMenu {
    private final RaftEntity raft;
    private final BlockPos pos;
    private final Access movingAccess;
    public RaftAnvilMenu(int id,Inventory inventory,RaftEntity raft,BlockPos pos){
        this(id,inventory,raft,pos,new Access(RaftStationMenus.access(raft,pos,RaftPiece.Kind.ANVIL)));
    }
    private RaftAnvilMenu(int id,Inventory inventory,RaftEntity raft,BlockPos pos,Access access){
        super(id,inventory,access);this.raft=raft;this.pos=pos.immutable();movingAccess=access;
    }
    @Override public boolean stillValid(Player player){return RaftStationMenus.valid(raft,pos,RaftPiece.Kind.ANVIL,player);}
    @Override protected void onTake(Player player,ItemStack item){
        movingAccess.suppressEffect=true;
        try{super.onTake(player,item);}finally{movingAccess.suppressEffect=false;}
        if(getCost()==0){
            var center=raft.stationPosition(pos,RaftPiece.Kind.ANVIL);
            if(center!=null){raft.level().playSound(null,BlockPos.containing(center),SoundEvents.ANVIL_USE,SoundSource.BLOCKS,1,1);}
        }
    }
    private static final class Access implements ContainerLevelAccess {
        private final ContainerLevelAccess delegate;
        private boolean suppressEffect;
        private Access(ContainerLevelAccess delegate){this.delegate=delegate;}
        @Override public <T> Optional<T> evaluate(BiFunction<Level,BlockPos,T> action){return delegate.evaluate(action);}
        @Override public void execute(BiConsumer<Level,BlockPos> action){if(!suppressEffect){delegate.execute(action);}}
    }
}
