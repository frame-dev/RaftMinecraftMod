package dev.raftocean.building;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.*;
import dev.raftocean.menu.HammerMenu;
import dev.raftocean.progression.Research;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native palette controls and actual shared placement commits, never hand-swapping proxies. */
public final class HammerCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    private static int button(HammerMenu menu,Item item){for(int i=0;i<menu.entries().size();i++)if(menu.entries().get(i).item()==item)return i+1;throw new IllegalStateException("Missing palette item");}
    private static void aim(FakePlayer player,RaftEntity raft,Vec3 point){
        var delta=raft.toWorld(point).subtract(player.getEyePosition());player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
        player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));
    }
    public static int run(ServerLevel level){
        checks=0;boolean enabled=RaftConfig.HAMMER_PALETTE.get(),research=RaftConfig.RESEARCH.get(),permissions=RaftConfig.CREW_PERMISSIONS.get();
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.setPos(8,level.getMaxY()-12,8);level.getChunkAt(BlockPos.containing(raft.position()));level.addFreshEntity(raft);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"HammerCheck"));var stranger=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"HammerOther"));
        try{
            RaftConfig.HAMMER_PALETTE.set(true);RaftConfig.RESEARCH.set(true);RaftConfig.CREW_PERMISSIONS.set(true);
            player.setData(Research.SCHEMA,1);player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);
            player.setPos(raft.toWorld(new Vec3(0,.626,0)));var hammer=new ItemStack(ModContent.BUILDING_HAMMER.get());player.setItemInHand(InteractionHand.MAIN_HAND,hammer);
            var menu=new HammerMenu(4,player.getInventory(),InteractionHand.MAIN_HAND);
            require(menu.stillValid(player) && menu.slots.isEmpty() && menu.selected()==-1,"New and legacy hammers start in safe repair mode with no lootable preview slots");
            require(menu.entries().stream().map(e->e.item()).distinct().count()==menu.entries().size(),"Native and addon palette has no duplicate items");
            require(menu.entries().stream().anyMatch(e->e.item()==dev.raftocean.survival.RaftJuicer.ITEM.get() && e.category()==3),"API-backed juicer automatically appears in addon palette");
            int foundation=button(menu,ModContent.FOUNDATION.get());
            require(!menu.clickMenuButton(stranger,foundation) && !menu.clickMenuButton(player,-1) && !menu.clickMenuButton(player,9999),"Foreign and out-of-bounds palette requests refused");
            menu.setCarried(new ItemStack(Items.STONE));require(!menu.clickMenuButton(player,foundation),"Selection refuses carried-item requests");menu.setCarried(ItemStack.EMPTY);
            player.getInventory().setItem(9,new ItemStack(ModContent.FOUNDATION.get(),2));
            require(menu.count(foundation-1)==2 && menu.clickMenuButton(player,foundation) && menu.selected()==foundation-1,"Native menu synchronizes backpack count and selection");
            require(player.getItemInHand(InteractionHand.MAIN_HAND)==hammer && player.getInventory().getItem(9).getCount()==2,"Selecting a piece neither swaps the hammer nor spends resources");
            var ops=level.registryAccess().createSerializationContext(NbtOps.INSTANCE);var restored=ItemStack.CODEC.parse(ops,ItemStack.CODEC.encodeStart(ops,hammer).getOrThrow()).getOrThrow();
            require(BuildingHammer.selection(restored).is(ModContent.FOUNDATION.get()),"Hammer choice survives native item serialization");
            aim(player,raft,new Vec3(1.48,.24,0));var preview=raft.placement(player,hammer,raft.pick(player));
            require(preview!=null && preview.valid() && preview.cell().equals(new BlockPos(2,0,0)),"Selected hammer produces the normal foundation ghost");
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && raft.getBlocks().containsKey(preview.cell())
                    && player.getInventory().getItem(9).getCount()==1 && player.getItemInHand(InteractionHand.MAIN_HAND)==hammer,"Actual hammer placement consumes one backpack foundation and keeps hammer equipped");
            require(!raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && player.getInventory().getItem(9).getCount()==1,"Blocked repeated placement spends no second foundation");
            player.getInventory().setItem(9,ItemStack.EMPTY);aim(player,raft,new Vec3(1.48,.24,1));preview=raft.placement(player,hammer,raft.pick(player));
            require(preview!=null && preview.failure().equals("materials") && !raft.placeWithHammer(player,InteractionHand.MAIN_HAND).consumesAction(),"Missing supplies give a red ghost and no free placement");
            var special=new ItemStack(ModContent.FOUNDATION.get());special.set(DataComponents.CUSTOM_NAME,Component.literal("Keep me"));player.getInventory().setItem(9,special);
            require(BuildingHammer.count(player,BuildingHammer.selection(hammer))==0 && !raft.placeWithHammer(player,InteractionHand.MAIN_HAND).consumesAction() && special.getCount()==1,"Component-bearing pieces are preserved instead of silently stripping their data");
            player.getInventory().setItem(9,new ItemStack(ModContent.FOUNDATION.get(),2));
            dev.raftocean.world.OceanRaftData.get(level).remember(stranger.getUUID(),raft.getUUID(),raft.position());
            dev.raftocean.crew.CrewData.get(level).guest(level,raft.getUUID(),player.getUUID());
            require(dev.raftocean.crew.CrewData.get(level).role(level,raft.getUUID(),player.getUUID()).equals("guest"),"Permission fixture uses an owned raft and an actual guest");
            require(!raft.placeWithHammer(player,InteractionHand.MAIN_HAND).consumesAction() && player.getInventory().getItem(9).getCount()==2,"Crew guest cannot bypass placement permissions through hammer");
            RaftConfig.CREW_PERMISSIONS.set(false);
            int netButton=button(menu,ModContent.structuralItem(RaftPiece.Kind.COLLECTION_NET));player.setData(Research.KNOWLEDGE,0);
            require(!menu.allowed(netButton-1) && !menu.clickMenuButton(player,netButton),"Unknown research stays hidden and server rejects its selection");
            BuildingHammer.choose(hammer,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.COLLECTION_NET)));player.getInventory().setItem(10,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.COLLECTION_NET),2));
            preview=raft.placement(player,hammer,raft.pick(player));
            require(preview!=null && preview.failure().equals("research") && !raft.placeWithHammer(player,InteractionHand.MAIN_HAND).consumesAction() && player.getInventory().getItem(10).getCount()==2,"Forged or stale saved research choice cannot bypass build gate");
            player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);require(menu.clickMenuButton(player,netButton),"Newly learned piece becomes selectable without reopening");
            require(raft.placeWithHammer(player,InteractionHand.MAIN_HAND).consumesAction() && raft.pieces().containsKey(new BlockPos(2,0,1)) && player.getInventory().getItem(10).getCount()==1,"Hammer nets retain sea-level edge placement and exact item cost");
            var wall=ModContent.structuralItem(RaftPiece.Kind.WALL);player.getInventory().setItem(11,new ItemStack(wall,2));require(menu.clickMenuButton(player,button(menu,wall)),"Wall selected from shared catalog");
            aim(player,raft,new Vec3(-1,.24,-1.48));require(raft.placeWithHammer(player,InteractionHand.MAIN_HAND).consumesAction()
                    && raft.pieces().get(new BlockPos(-1,1,-1)).kind()==RaftPiece.Kind.WALL && player.getInventory().getItem(11).getCount()==1,"Hammer walls reuse normal edge snapping");
            for(int i=0;i<menu.entries().size();i++){
                var candidate=menu.entries().get(i).stack();int before=BuildingHammer.count(player,candidate);
                require(menu.clickMenuButton(player,i+1) && BuildingHammer.selection(hammer).is(candidate.getItem()),"Every native/addon catalog entry selects its exact registered item");
                var pick=new RaftEntity.Pick(BlockPos.ZERO,null,new net.minecraft.world.phys.AABB(-.5,0,-.5,.5,.25,.5),new Vec3(.48,.24,0));
                for(int rotation=0;rotation<4;rotation++){
                    var selectedPlan=raft.placement(player,hammer,pick,rotation);var directPlan=raft.placement(player,candidate,pick,rotation);
                    require(selectedPlan!=null && directPlan!=null && selectedPlan.cell().equals(directPlan.cell()) && Objects.equals(selectedPlan.piece(),directPlan.piece())
                            && selectedPlan.boxes().equals(directPlan.boxes()),"Selected and direct build ghosts share coordinates, rotated shape and footprint");
                }
                require(BuildingHammer.count(player,candidate)==before && player.getMainHandItem()==hammer,"Browsing every blueprint never grants or spends pieces");
            }
            var forged=hammer.copy();net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,forged,tag->tag.putString(BuildingHammer.CHOICE,"minecraft:diamond"));
            require(BuildingHammer.selection(forged).isEmpty(),"Forged non-buildable item selection is ignored safely");
            require(menu.clickMenuButton(player,0) && BuildingHammer.selection(hammer).isEmpty(),"Repair control clears build mode");
            var repairCell=new BlockPos(1,0,-1);raft.damageFoundation(repairCell,25);player.getInventory().setItem(12,new ItemStack(Items.OAK_PLANKS,2));aim(player,raft,new Vec3(1,.24,-1));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && raft.foundationHealth(repairCell)==100 && player.getInventory().getItem(12).getCount()==1,"Legacy hammer repair still spends one plank and repairs twenty-five health");
            require(menu.clickMenuButton(player,foundation),"Return to build mode");RaftConfig.HAMMER_PALETTE.set(false);
            require(!menu.stillValid(player) && !menu.clickMenuButton(player,0) && BuildingHammer.selection(hammer).isEmpty(),"Config disable closes palette and returns legacy hammer behavior");
            RaftConfig.HAMMER_PALETTE.set(true);require(BuildingHammer.selection(hammer).is(ModContent.FOUNDATION.get()),"Re-enabling preserves saved selection");
            player.setItemInHand(InteractionHand.MAIN_HAND,hammer.copy());require(!menu.stillValid(player) && !menu.clickMenuButton(player,0),"Replacing held hammer invalidates old session");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,hammer);var off=new HammerMenu(5,player.getInventory(),InteractionHand.OFF_HAND);
            require(off.stillValid(player) && off.clickMenuButton(player,foundation),"Offhand palette tracks its actual hammer");
            player.getAbilities().instabuild=true;player.getInventory().setItem(9,ItemStack.EMPTY);aim(player,raft,new Vec3(1.48,.24,-1));
            require(raft.placeWithHammer(player,InteractionHand.OFF_HAND).consumesAction() && raft.getBlocks().containsKey(new BlockPos(2,0,-1)) && player.getItemInHand(InteractionHand.OFF_HAND)==hammer,"Creative placement needs no prefab and preserves the offhand hammer");player.getAbilities().instabuild=false;
            raft.setYRot(73);raft.setPos(raft.position().add(16,0,16));level.getChunkAt(BlockPos.containing(raft.position()));player.setPos(raft.toWorld(new Vec3(0,.626,0)));
            player.getInventory().setItem(9,new ItemStack(ModContent.FOUNDATION.get(),2));aim(player,raft,new Vec3(-1.48,.24,0));var turned=raft.placement(player,hammer,raft.pick(player));
            require(turned!=null && turned.valid() && raft.placeWithHammer(player,InteractionHand.OFF_HAND).consumesAction() && raft.getBlocks().containsKey(turned.cell())
                    && player.getInventory().getItem(9).getCount()==1,"Translated and turned raft retains hull-relative hammer placement and one-item cost");
            player.setShiftKeyDown(true);require(BuildingHammer.preview(player,hammer).isEmpty(),"Sneaking suppresses build ghost for deliberate legacy dismantling");player.setShiftKeyDown(false);
            player.setPos(player.position().add(30,0,0));require(!raft.placeWithHammer(player,InteractionHand.OFF_HAND).consumesAction(),"Out-of-reach aim cannot build");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);require(!BuildingHammer.open(player,InteractionHand.OFF_HAND),"Network menu request without a held hammer is refused");
            var buffer=new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());try{
                var payload=new dev.raftocean.network.HammerMenuPayload(true);dev.raftocean.network.HammerMenuPayload.STREAM_CODEC.encode(buffer,payload);
                require(dev.raftocean.network.HammerMenuPayload.STREAM_CODEC.decode(buffer).offhand() && !buffer.isReadable(),"Bounded hand-only palette request codec round-trips");
            }finally{buffer.release();}
            return checks;
        }finally{
            raft.discard();player.discard();stranger.discard();RaftConfig.HAMMER_PALETTE.set(enabled);RaftConfig.RESEARCH.set(research);RaftConfig.CREW_PERMISSIONS.set(permissions);
        }
    }
    private HammerCheck() { }
}
