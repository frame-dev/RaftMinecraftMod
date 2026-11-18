package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.api.RaftPlatforms;
import dev.raftocean.building.BuildingHammer;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.progression.Research;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.CraftingExpansion;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Saved net armor, real hammer transactions, graph damage and addon snapshots. */
public final class RaftNetArmorCheck {
    private static int checks;
    private RaftNetArmorCheck() { }

    private static void require(boolean condition,String message) {
        if(!condition)throw new IllegalStateException(message);
        checks++;
    }

    private static RaftEntity raft(ServerLevel level,String layout,String structure) {
        var value=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        value.putString("Layout",layout);value.putString("Structure",structure);
        var raft=new RaftEntity(ModContent.RAFT.get(),level);
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),value.buildResult()));
        raft.setPos(8,level.getMaxY()-12,8);
        level.getChunkAt(BlockPos.containing(raft.position()));
        return raft;
    }

    public static int run(ServerLevel level) {
        checks=0;
        boolean armor=RaftConfig.REINFORCEMENT.get(),damage=RaftConfig.RAFT_DAMAGE.get(),
                palette=RaftConfig.HAMMER_PALETTE.get(),permissions=RaftConfig.CREW_PERMISSIONS.get();
        var raft=raft(level,"0,0;1,0","2,0,0,COLLECTION_NET,0,0");
        var bridge=raft(level,"0,0;2,0","1,0,0,COLLECTION_NET,0,0;2,1,0,CHEST,0,0");
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"NetArmorCheck"));
        var net=new BlockPos(2,0,0);
        var brace=CraftingExpansion.item("reinforced_brace");
        try {
            RaftConfig.REINFORCEMENT.set(true);RaftConfig.RAFT_DAMAGE.set(true);
            RaftConfig.HAMMER_PALETTE.set(true);RaftConfig.CREW_PERMISSIONS.set(false);
            player.setData(Research.SCHEMA,1);player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);
            require(raft.attackableFoundation(net) && !raft.edgeFoundation(new BlockPos(2,1,0)),
                    "Sea-level nets are valid exposed targets; upper cells are excluded");
            var snapshot=RaftPlatforms.snapshot(raft);
            var status=RaftPlatforms.inspect(raft,net).orElseThrow();
            require(snapshot.size()==3 && status.collectionNet() && status.exposed() && status.health()==100
                    && !status.armored(),"Addon snapshot identifies unarmored sea-level nets");
            var movingCell=new BlockPos.MutableBlockPos(2,0,0);
            var immutable=RaftPlatforms.inspect(raft,movingCell).orElseThrow();
            movingCell.set(3,0,0);
            require(immutable.cell().equals(net),"Addon snapshots copy mutable position inputs");
            boolean readOnly=false;
            try{snapshot.clear();}catch(UnsupportedOperationException expected){readOnly=true;}
            require(readOnly,"Addon snapshot lists cannot mutate live raft state");
            require(raft.damageFoundation(net,25) && raft.foundationHealth(net)==75,"An unarmored net takes normal bite damage");
            player.getInventory().setItem(9,new ItemStack(Items.OAK_PLANKS,2));
            require(raft.repairFoundation(net,player) && raft.foundationHealth(net)==100
                    && player.getInventory().getItem(9).getCount()==1 && !raft.repairFoundation(net,player),
                    "Net repairs spend one plank and refuse a full-health duplicate");
            require(raft.damageFoundation(net,25) && raft.foundationHealth(net)==75 && status.health()==100,
                    "Damage updates live net health while old addon snapshots stay detached");
            player.setPos(raft.toWorld(new Vec3(0,.626,0)));
            player.getInventory().setItem(9,new ItemStack(Items.OAK_PLANKS,2));
            require(raft.repairFoundation(net,player) && raft.foundationHealth(net)==100
                    && player.getInventory().getItem(9).getCount()==1,"Net repair uses one plank for twenty-five health");
            var hammer=new ItemStack(ModContent.BUILDING_HAMMER.get());
            BuildingHammer.choose(hammer,new ItemStack(brace));
            player.setItemInHand(InteractionHand.MAIN_HAND,hammer);
            player.getInventory().setItem(10,new ItemStack(brace,2));
            Vec3 look=raft.toWorld(new Vec3(2,.2,0)).subtract(player.getEyePosition());
            player.setYRot((float)Math.toDegrees(Math.atan2(-look.x,look.z)));
            player.setXRot((float)-Math.toDegrees(Math.atan2(look.y,look.horizontalDistance())));
            var pick=raft.pick(player);
            require(pick!=null && pick.cell().equals(net) && raft.placement(player,hammer,pick).valid()
                    && raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && player.getInventory().getItem(10).getCount()==1 && player.getMainHandItem()==hammer,
                    "Real net hit and hammer preview apply one backpack brace");
            require(!raft.attackableFoundation(net) && !raft.damageFoundation(net,25)
                    && !raft.placeWithHammer(player,InteractionHand.MAIN_HAND).consumesAction()
                    && player.getInventory().getItem(10).getCount()==1,"Armored nets reject shark damage and duplicate reinforcement");
            raft.storage(net).setItem(0,new ItemStack(Items.OAK_LOG,5));
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
            raft.saveWithoutId(saved);
            var copy=raft(level,"0,0","");
            try {
                copy.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
                require(copy.reinforcedFoundation(net) && copy.storage(net).getItem(0).getCount()==5,
                        "Net armor and stored salvage survive native save/load");
            } finally {copy.storage(net).clearContent();copy.discard();}
            RaftConfig.REINFORCEMENT.set(false);
            require(!RaftPlatforms.inspect(raft,net).orElseThrow().protectionActive()
                    && RaftPlatforms.inspect(raft,net).orElseThrow().armored()
                    && raft.damageFoundation(net,10),"Config disables protection while retaining saved armor");
            RaftConfig.REINFORCEMENT.set(true);
            int[] refund={0},cargo={0};
            java.util.function.Consumer<EntityJoinLevelEvent> drops=event->{
                if(event.getLevel()==level && event.getEntity() instanceof ItemEntity item){
                    if(item.getItem().is(brace))refund[0]+=item.getItem().getCount();
                    if(item.getItem().is(Items.COD))cargo[0]+=item.getItem().getCount();
                    item.discard();
                }
            };
            NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class,drops);
            try {
                player.setShiftKeyDown(true);
                require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                        && !raft.seaLevelPlatform(net) && !raft.reinforcedFoundation(net) && refund[0]==1,
                        "Dismantling an armored net refunds one brace and clears the armor");
                require(RaftPlatforms.inspect(raft,net).isEmpty() && snapshot.size()==3,
                        "Addon snapshots exclude removed cells without mutating past snapshots");
                bridge.storage(new BlockPos(2,1,0)).setItem(0,new ItemStack(Items.COD,3));
                var broken=new BlockPos(1,0,0);
                for(int bite=0;bite<9;bite++)require(bridge.damageFoundation(broken,10)
                        && bridge.seaLevelPlatform(broken),"A net survives each of the first nine normal bites");
                require(bridge.damageFoundation(broken,10) && !bridge.seaLevelPlatform(broken)
                        && bridge.getBlocks().size()==1 && bridge.getStructuralCount()==0 && cargo[0]==3,
                        "The tenth bite breaks a bridge net and prunes disconnected raft pieces");
                var brokenSave=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
                bridge.saveWithoutId(brokenSave);
                var remnant=raft(level,"0,0","");
                try {
                    remnant.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),brokenSave.buildResult()));
                    require(remnant.seaLevelCells().size()==1 && RaftPlatforms.inspect(remnant,broken).isEmpty(),
                            "Destroyed net bridge saves as a valid connected remnant");
                } finally {remnant.discard();}
            } finally {NeoForge.EVENT_BUS.unregister(drops);}
            return checks;
        } finally {
            raft.discard();bridge.discard();player.discard();
            RaftConfig.REINFORCEMENT.set(armor);RaftConfig.RAFT_DAMAGE.set(damage);
            RaftConfig.HAMMER_PALETTE.set(palette);RaftConfig.CREW_PERMISSIONS.set(permissions);
        }
    }
}
