package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.api.*;
import dev.raftocean.registry.ModContent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native extension, footprint, save/load, ghost and menu regressions. */
public final class RaftExpansionCheck {
    private RaftExpansionCheck() { }
    public static int run(ServerLevel level) {
        int checks=0;var anchor=new BlockPos(0,1,0);
        for(var kind:List.of(RaftPiece.Kind.WIDE_FLOOR,RaftPiece.Kind.WIDE_STAIRS,RaftPiece.Kind.WIDE_CRAFTING_TABLE)) {
            for(int rotation=0;rotation<4;rotation++) {
                var layout=RaftLayout.starter();var structure=new RaftStructure();var piece=new RaftPiece(kind,rotation,false);
                require(structure.add(anchor,piece,layout),"Wide piece placed: "+kind);checks++;
                var second=piece.occupiedCells(anchor).stream().filter(p->!p.equals(anchor)).findFirst().orElseThrow();
                require(!structure.add(second,new RaftPiece(RaftPiece.Kind.WALL,0,false),layout),"Second tile reserved");checks++;
                require(structure.pieces().size()==1 && structure.mass()==piece.mass(),"Wide piece is one owner/mass");checks++;
                require(RaftStructure.decode(structure.encode(),layout).pieces().equals(structure.pieces()),"Rotated footprint persists");checks++;
                require(structure.remove(anchor,layout) && structure.add(second,new RaftPiece(RaftPiece.Kind.WALL,0,false),layout),"Remove frees second tile");checks++;
            }
        }
        var base=RaftLayout.starter();var wide=new RaftPiece(RaftPiece.Kind.WIDE_CRAFTING_TABLE,0,false);
        var edge=new BlockPos(1,1,0);require(!new RaftStructure().add(edge,wide,base),"Bench needs both support cells");checks++;
        var structure=new RaftStructure();require(structure.add(anchor,wide,base),"Wide menu bench");checks++;
        var weakened=RaftLayout.decode(base.encode());require(weakened.remove(new BlockPos(1,0,0)),"Remove second foundation in damage fixture");
        require(!structure.supportedBy(weakened),"Lost second support detected");checks++;
        require(structure.pruneAfterDamage(weakened).containsKey(anchor) && structure.supportedBy(weakened),"Damage prunes unsupported wide piece");checks++;
        structure=new RaftStructure();structure.add(anchor,wide,base);
        var raft=raft(level,structure,base);var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"WideAddonCheck"));
        try {
            player.setPos(raft.toWorld(new Vec3(0,.251,-2)));
            var menu=dev.raftocean.menu.RaftStationMenus.create(8,player.getInventory(),raft,anchor,wide.kind());
            require(menu instanceof dev.raftocean.menu.RaftCraftingMenu && menu.stillValid(player),"Wide bench opens actual native crafting menu");checks++;
            require(raft.craftingTablePosition(anchor)!=null,"Wide crafting reach finds bench");checks++;
        } finally {raft.discard();}
        var used=new AtomicInteger();var id=Identifier.fromNamespaceAndPath("raft_test","wide_shelf");
        var shape=new AABB(-.5,0,-.5,1.5,.75,.5);
        var definition=new RaftBuildable(id,()->Items.BRICK,()->Blocks.BRICKS,2,1,1,8,true,List.of(shape),List.of(shape),
                (owner,pos,user,hand)->{used.incrementAndGet();return InteractionResult.SUCCESS_SERVER;});
        RaftBuildables.register(definition);
        require(RaftBuildables.forItem(new ItemStack(Items.BRICK)).orElseThrow()==definition,"Addon item mapping");checks++;
        boolean duplicate=false;try{RaftBuildables.register(definition);}catch(IllegalArgumentException expected){duplicate=true;}
        require(duplicate,"Duplicate IDs rejected");checks++;
        for(int rotation=0;rotation<4;rotation++) {
            var piece=new RaftPiece(RaftPiece.Kind.ADDON,rotation,false,id.toString());structure=new RaftStructure();
            require(structure.add(anchor,piece,base),"Addon footprint supported");checks++;
            var restored=RaftStructure.decode(structure.encode(),base);
            require(restored.pieces().get(anchor).addonId().equals(id.toString()) && restored.mass()==8
                    && restored.collisionBoxes(anchor,piece).equals(structure.collisionBoxes(anchor,piece)),"Addon identity, mass and geometry persist");checks++;
        }
        structure=new RaftStructure();var addon=new RaftPiece(RaftPiece.Kind.ADDON,0,false,id.toString());structure.add(anchor,addon,base);
        raft=raft(level,structure,base);
        try {
            player.setPos(raft.toWorld(new Vec3(.5,.251,-2)));player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            aim(player,raft,new Vec3(.5,.5,-.5));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && used.get()==1,"Native addon right-click callback");checks++;
            require(ModContent.structuralItem(addon)==Items.BRICK,"Addon removal refunds its own item");checks++;
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BRICK,2));
            var fixture=new RaftEntity.Pick(new BlockPos(-1,0,0),null,new AABB(-1.5,0,-.5,-.5,.25,.5),new Vec3(-1,.24,0));
            var plan=raft.placement(player,player.getMainHandItem(),fixture);
            require(plan!=null && plan.piece().addonId().equals(id.toString()) && plan.boxes().size()==1
                    && player.getMainHandItem().getCount()==2,"Addon ghost uses custom shape without consuming items");checks++;
        } finally {raft.discard();player.discard();}
        raft=raft(level,new RaftStructure(),base);
        player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"AddonPlaceCheck"));
        try {
            player.setPos(raft.toWorld(new Vec3(0,.251,-2)));
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.BRICK,2));
            aim(player,raft,new Vec3(0,.24,0));
            var plan=raft.placement(player,player.getMainHandItem(),raft.pick(player));
            require(plan!=null && plan.valid() && plan.piece().addonId().equals(id.toString()),"Addon item produces valid native placement");checks++;
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction()
                    && raft.pieces().get(plan.cell()).equals(plan.piece()) && player.getMainHandItem().getCount()==1,
                    "Native addon placement consumes exactly one custom item");checks++;
            var area=raft.getBoundingBox().inflate(3);
            int before=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area).stream()
                    .filter(item->item.getItem().is(Items.BRICK)).mapToInt(item->item.getItem().getCount()).sum();
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.BUILDING_HAMMER.get()));
            player.setShiftKeyDown(true);aim(player,raft,new Vec3(.5,.5,-.5));
            require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && raft.pieces().isEmpty(),"Native addon hammer removes whole footprint");checks++;
            int after=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,area).stream()
                    .filter(item->item.getItem().is(Items.BRICK)).mapToInt(item->item.getItem().getCount()).sum();
            require(after-before==1,"Native addon removal refunds exactly one original item");checks++;
        } finally {raft.discard();player.discard();}
        return checks;
    }
    private static RaftEntity raft(ServerLevel level,RaftStructure structure,RaftLayout layout) {
        var output=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);output.putString("Layout",layout.encode());output.putString("Structure",structure.encode());
        var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),output.buildResult()));
        raft.setPos(8,level.getMaxY()+24,8);require(level.addFreshEntity(raft),"Register expansion fixture");return raft;
    }
    private static void aim(FakePlayer player,RaftEntity raft,Vec3 point) {
        var delta=raft.toWorld(point).subtract(player.getEyePosition());
        player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
        player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
}
