package dev.raftocean.menu;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.block.EnchantingTableBlock;

/** Native enchantments, seed, lapis and XP; bookshelf power follows local raft coordinates. */
public final class RaftEnchantingMenu extends EnchantmentMenu {
    private final RaftEntity raft;
    private final BlockPos pos;
    private int lastPower=-1;
    public RaftEnchantingMenu(int id,Inventory inventory,RaftEntity raft,BlockPos pos){
        super(id,inventory,RaftStationMenus.access(raft,pos,RaftPiece.Kind.ENCHANTING_TABLE));
        this.raft=raft;this.pos=pos.immutable();
    }
    @Override public boolean stillValid(Player player){return RaftStationMenus.valid(raft,pos,RaftPiece.Kind.ENCHANTING_TABLE,player);}
    public int bookshelfPower(){
        if(raft==null){return 0;}
        int power=0;
        for(var offset:EnchantingTableBlock.BOOKSHELF_OFFSETS){
            var shelf=raft.pieces().get(pos.offset(offset));
            if(shelf==null || shelf.kind()!=RaftPiece.Kind.BOOKSHELF){continue;}
            BlockPos gap=pos.offset(offset.getX()/2,offset.getY(),offset.getZ()/2);
            boolean blocked=raft.pieces().entrySet().stream().anyMatch(entry->entry.getKey().getX()==gap.getX()
                    && entry.getKey().getZ()==gap.getZ() && gap.getY()>=entry.getKey().getY()
                    && gap.getY()<entry.getKey().getY()+entry.getValue().kind().height);
            if(!blocked){power++;}
        }
        return Math.min(15,power);
    }
    @Override public void broadcastChanges(){
        int power=bookshelfPower();
        if(power!=lastPower){lastPower=power;super.slotsChanged(getSlot(0).container);}
        super.broadcastChanges();
    }
}
