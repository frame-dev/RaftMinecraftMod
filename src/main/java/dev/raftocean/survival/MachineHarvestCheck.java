package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.SurvivalMenu;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Real inventory transfers verify completed outputs, permissions and partial fit. */
public final class MachineHarvestCheck {
    private static final BlockPos CELL=new BlockPos(1,1,0);
    private MachineHarvestCheck() { }
    public static int run(ServerLevel level) {
        int checks=0;boolean enabled=RaftConfig.QUICK_HARVEST.get(),permissions=RaftConfig.CREW_PERMISSIONS.get();
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"MachineHarvestCheck"));
        player.setShiftKeyDown(true);player.getAbilities().instabuild=false;
        try {
            RaftConfig.QUICK_HARVEST.set(true);
            for(var kind:RaftPiece.Kind.values()) {
                if(!kind.machine()) { continue; }
                var raft=SurvivalCheck.create(level,kind);var storage=raft.storage(CELL);
                try {
                    player.getInventory().clearContent();player.setPos(raft.stationPosition(CELL,kind));
                    var outputs=MachineHarvest.outputSlots(kind);
                    SurvivalMenu.layout(kind).forEach((slot,role)->storage.setItem(slot,new ItemStack(role.output()?Items.COOKED_COD:Items.STICK,3)));
                    var result=MachineHarvest.use(raft,CELL,kind,player,InteractionHand.MAIN_HAND);
                    require(outputs.isEmpty()?result==InteractionResult.PASS:result.consumesAction(),"Only machines with completed-output roles use the shortcut: "+kind); checks++;
                    require(player.getInventory().countItem(Items.COOKED_COD)==outputs.size()*3,"Exact completed output count collected: "+kind); checks++;
                    require(SurvivalMenu.layout(kind).entrySet().stream().filter(e->!e.getValue().output())
                            .allMatch(e->storage.getItem(e.getKey()).is(Items.STICK) && storage.getItem(e.getKey()).getCount()==3),
                            "Ingredients, fuel and battery sockets remain untouched: "+kind); checks++;
                    require(player.getMainHandItem().isEmpty(),"Backpack-first collection preserves empty selected hand: "+kind); checks++;
                    MachineHarvest.use(raft,CELL,kind,player,InteractionHand.MAIN_HAND);
                    require(player.getInventory().countItem(Items.COOKED_COD)==outputs.size()*3,"Repeated collection cannot duplicate goods: "+kind); checks++;
                } finally { storage.clearContent();raft.discard(); }
            }
            var raft=SurvivalCheck.create(level,RaftPiece.Kind.GRILL);var storage=raft.storage(CELL);
            try {
                player.setPos(raft.stationPosition(CELL,RaftPiece.Kind.GRILL));
                for(boolean creative:List.of(false,true)) {
                    player.getAbilities().instabuild=creative;player.getInventory().clearContent();
                    for(int slot=0;slot<36;slot++) { player.getInventory().setItem(slot,new ItemStack(Items.STONE,64)); }
                    player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                    player.getInventory().setItem(9,new ItemStack(Items.COOKED_COD,63));storage.setItem(2,new ItemStack(Items.COOKED_COD,8));
                    MachineHarvest.use(raft,CELL,RaftPiece.Kind.GRILL,player,InteractionHand.OFF_HAND);
                    require(storage.getItem(2).getCount()==7 && player.getInventory().getItem(9).getCount()==64,
                            "Partial fit preserves excess in survival and creative"); checks++;
                    MachineHarvest.use(raft,CELL,RaftPiece.Kind.GRILL,player,InteractionHand.OFF_HAND);
                    require(storage.getItem(2).getCount()==7,"Full inventory never deletes finished goods"); checks++;
                }
                player.getAbilities().instabuild=false;player.getInventory().clearContent();
                player.setShiftKeyDown(false);
                require(MachineHarvest.use(raft,CELL,RaftPiece.Kind.GRILL,player,InteractionHand.MAIN_HAND)==InteractionResult.PASS,
                        "Ordinary right-click still opens the machine"); checks++;
                player.setShiftKeyDown(true);RaftConfig.QUICK_HARVEST.set(false);
                require(MachineHarvest.use(raft,CELL,RaftPiece.Kind.GRILL,player,InteractionHand.MAIN_HAND)==InteractionResult.PASS && storage.getItem(2).getCount()==7,
                        "Configuration disables shortcut without losing outputs"); checks++;
                RaftConfig.QUICK_HARVEST.set(true);player.setPos(player.position().add(100,0,0));
                require(MachineHarvest.use(raft,CELL,RaftPiece.Kind.GRILL,player,InteractionHand.MAIN_HAND)==InteractionResult.FAIL && storage.getItem(2).getCount()==7,
                        "Remote collection is rejected"); checks++;
                player.setPos(raft.stationPosition(CELL,RaftPiece.Kind.GRILL));
                var spectator=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"SpectatorHarvestCheck")) {
                    @Override public boolean isSpectator() { return true; }
                };
                spectator.setShiftKeyDown(true);spectator.setPos(player.position());
                require(MachineHarvest.use(raft,CELL,RaftPiece.Kind.GRILL,spectator,InteractionHand.MAIN_HAND)==InteractionResult.FAIL && storage.getItem(2).getCount()==7,
                        "Spectator shortcut cannot mutate a machine"); checks++;
                RaftConfig.CREW_PERMISSIONS.set(true);
                dev.raftocean.world.OceanRaftData.get(level).remember(UUID.randomUUID(),raft.getUUID(),raft.position());
                require(MachineHarvest.use(raft,CELL,RaftPiece.Kind.GRILL,player,InteractionHand.MAIN_HAND)==InteractionResult.FAIL && storage.getItem(2).getCount()==7,
                        "Guest shortcut cannot bypass crew ownership"); checks++;
                spectator.discard();
            } finally { storage.clearContent();raft.discard(); }
            var chest=SurvivalCheck.create(level,RaftPiece.Kind.CHEST);
            try {
                var spectator=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"SpectatorStashCheck")) {
                    @Override public boolean isSpectator() { return true; }
                };
                spectator.setPos(chest.stationPosition(CELL,RaftPiece.Kind.CHEST));
                chest.storage(CELL).setItem(0,new ItemStack(Items.KELP));spectator.getInventory().setItem(9,new ItemStack(Items.KELP,5));
                require(QuickStash.store(chest,CELL,spectator)==0 && spectator.getInventory().getItem(9).getCount()==5,
                        "Spectator quick stash also preserves inventory"); checks++;
                spectator.getInventory().clearContent();spectator.discard();
            } finally { chest.storage(CELL).clearContent();chest.discard(); }
        } finally {
            RaftConfig.QUICK_HARVEST.set(enabled);RaftConfig.CREW_PERMISSIONS.set(permissions);
            player.getInventory().clearContent();player.discard();
        }
        return checks;
    }
    private static void require(boolean ok,String message) { if(!ok) { throw new IllegalStateException(message); } }
}
