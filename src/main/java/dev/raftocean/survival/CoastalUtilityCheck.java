package dev.raftocean.survival;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.registry.ModContent;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native armor/save/refund and component-safe single/double storage transactions. */
public final class CoastalUtilityCheck {
    private static int count;
    private CoastalUtilityCheck() { }
    public static int run(ServerLevel level) {
        count=0;boolean armor=RaftConfig.REINFORCEMENT.get(),stash=RaftConfig.QUICK_STASH.get(),damage=RaftConfig.RAFT_DAMAGE.get();
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"CoastalUtilityCheck"));
        var raft=create(level,"");RaftEntity copy=null;
        try {
            RaftConfig.REINFORCEMENT.set(true);RaftConfig.QUICK_STASH.set(true);RaftConfig.RAFT_DAMAGE.set(true);
            var edge=new BlockPos(1,0,1);player.setPos(raft.toWorld(new Vec3(1,.251,-1)));
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(CraftingExpansion.item("reinforced_brace"),3));
            require(raft.damageFoundation(edge,25) && raft.foundationHealth(edge)==75,"Armor fixture starts damaged");
            require(raft.reinforceFoundation(edge,player,InteractionHand.MAIN_HAND) && player.getMainHandItem().getCount()==2,"Armor consumes exactly one brace");
            require(raft.edgeFoundation(edge) && !raft.attackableFoundation(edge) && !raft.damageFoundation(edge,100) && raft.foundationHealth(edge)==75,"Armor protects without repairing and sharks skip its edge");
            require(!raft.reinforceFoundation(edge,player,InteractionHand.MAIN_HAND) && player.getMainHandItem().getCount()==2,"Repeated armor does not consume items");
            var reporter=new ProblemReporter.Collector();var saved=TagValueOutput.createWithContext(reporter,level.registryAccess());raft.saveWithoutId(saved);
            copy=new RaftEntity(ModContent.RAFT.get(),level);copy.load(TagValueInput.create(reporter,level.registryAccess(),saved.buildResult()));
            require(reporter.isEmpty() && copy.reinforcedFoundation(edge) && copy.foundationHealth(edge)==75 && !copy.damageFoundation(edge,100),"Armor and old damage survive native saves");
            RaftConfig.REINFORCEMENT.set(false);require(raft.reinforcedFoundation(edge) && raft.attackableFoundation(edge) && raft.damageFoundation(edge,1),"Disabling armor preserves records and restores ordinary damage");
            RaftConfig.REINFORCEMENT.set(true);player.setPos(raft.position().add(100,0,0));
            require(!raft.reinforceFoundation(new BlockPos(-1,0,-1),player,InteractionHand.MAIN_HAND) && player.getMainHandItem().getCount()==2,"Distant reinforcement rejected");
            player.setPos(raft.toWorld(new Vec3(1,.251,-1)));player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.BUILDING_HAMMER.get()));
            player.setShiftKeyDown(true);aim(player,raft,new Vec3(1,.12,1));var area=raft.getBoundingBox().inflate(4);
            int before=braceDrops(level,area);
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && !raft.getBlocks().containsKey(edge) && !raft.reinforcedFoundation(edge),"Native hammer removes armored foundation");
            require(braceDrops(level,area)-before==1,"Hammer refunds one armor brace");player.setShiftKeyDown(false);
            raft.discard();raft=create(level,"0,1,0,CHEST,0,0;1,1,0,CHEST,0,0");var pos=new BlockPos(0,1,0);var partner=pos.east();
            player.getInventory().clearContent();player.setPos(raft.stationPosition(pos,dev.raftocean.entity.RaftPiece.Kind.CHEST));
            for(int slot=0;slot<27;slot++)raft.storage(pos).setItem(slot,new ItemStack(Items.STONE,64));
            raft.storage(partner).setItem(0,new ItemStack(Items.KELP,63));
            player.getInventory().setItem(0,new ItemStack(Items.KELP,7));player.getInventory().setItem(9,new ItemStack(Items.KELP,10));
            player.getInventory().setItem(10,new ItemStack(Items.DIAMOND,4));
            require(QuickStash.store(raft,pos,player)==10 && player.getInventory().getItem(9).isEmpty()
                    && raft.storage(partner).getItem(0).getCount()==64 && raft.storage(partner).getItem(1).getCount()==9,"Double chest merges then fills other half");
            require(player.getInventory().getItem(0).getCount()==7 && player.getInventory().getItem(10).getCount()==4,"Hotbar and unmatched supplies preserved");
            require(QuickStash.store(raft,pos,player)==0,"Repeated stash conserves counts");
            var named=new ItemStack(Items.KELP,5);named.set(DataComponents.CUSTOM_NAME,Component.literal("Keep me"));player.getInventory().setItem(9,named);
            require(QuickStash.store(raft,pos,player)==0 && player.getInventory().getItem(9).getCount()==5,"Different item components are not merged");
            for(int slot=0;slot<27;slot++)raft.storage(partner).setItem(slot,new ItemStack(Items.STONE,64));
            raft.storage(partner).setItem(0,new ItemStack(Items.KELP,63));player.getInventory().setItem(9,new ItemStack(Items.KELP,10));
            require(QuickStash.store(raft,pos,player)==1 && player.getInventory().getItem(9).getCount()==9,"Partial fit leaves all excess in backpack");
            RaftConfig.QUICK_STASH.set(false);raft.storage(partner).setItem(0,new ItemStack(Items.KELP,1));
            require(QuickStash.store(raft,pos,player)==0 && player.getInventory().getItem(9).getCount()==9,"Stash switch preserves inventory");RaftConfig.QUICK_STASH.set(true);
            player.setPos(player.position().add(100,0,0));require(QuickStash.store(raft,pos,player)==0,"Distant stash rejected");
            raft.discard();raft=create(level,"0,1,0,BARREL,0,0");player.setPos(raft.stationPosition(pos,dev.raftocean.entity.RaftPiece.Kind.BARREL));
            raft.storage(pos).setItem(0,new ItemStack(Items.KELP,1));
            require(QuickStash.store(raft,pos,player)==9 && raft.storage(pos).getItem(0).getCount()==10,"Barrel stash shares exact transaction rules");
        } finally {
            for(var pos:raft.pieces().keySet()){var storage=raft.storage(pos);if(storage!=null)storage.clearContent();}raft.discard();
            if(copy!=null)copy.discard();player.getInventory().clearContent();player.discard();
            RaftConfig.REINFORCEMENT.set(armor);RaftConfig.QUICK_STASH.set(stash);RaftConfig.RAFT_DAMAGE.set(damage);
            level.getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(0,level.getMaxY(),0,20,level.getMaxY()+60,20)).forEach(ItemEntity::discard);
        }
        return count;
    }
    private static RaftEntity create(ServerLevel level,String pieces) {
        var tag=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);tag.putString("Structure",pieces);
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),tag.buildResult()));
        raft.setPos(8,level.getMaxY()+24,8);level.addFreshEntity(raft);return raft;
    }
    private static int braceDrops(ServerLevel level,net.minecraft.world.phys.AABB area) { return level.getEntitiesOfClass(ItemEntity.class,area).stream().filter(e->e.getItem().is(CraftingExpansion.item("reinforced_brace"))).mapToInt(e->e.getItem().getCount()).sum(); }
    private static void aim(FakePlayer player,RaftEntity raft,Vec3 target) {
        var delta=raft.toWorld(target).subtract(player.getEyePosition());player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
        player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));
    }
    private static void require(boolean value,String message) { if(!value)throw new IllegalStateException(message);count++; }
}
