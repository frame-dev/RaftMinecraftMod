package dev.raftocean.entity;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.*;
import dev.raftocean.registry.ModContent;
import net.neoforged.neoforge.common.util.FakePlayer;
import com.mojang.authlib.GameProfile;

/** Actual table placement, support dependency and synchronized geometry regressions. */
public final class RaftTabletopCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    public static int run(ServerLevel level) {
        checks=0;var layout=RaftLayout.starter();var anchor=new BlockPos(0,1,0);
        for(var kind:List.of(RaftPiece.Kind.SIDE_TABLE,RaftPiece.Kind.DISPLAY_TABLE,RaftPiece.Kind.DINING_TABLE,RaftPiece.Kind.COFFEE_TABLE))for(int rotation=0;rotation<4;rotation++) {
            var table=new RaftPiece(kind,rotation,false);
            for(var decoration:List.of(RaftPiece.Kind.TABLE_LAMP,RaftPiece.Kind.BOOK_STACK,RaftPiece.Kind.CERAMIC_VASE,RaftPiece.Kind.POTTED_PALM,RaftPiece.Kind.CHART_STAND)) {
                var structure=new RaftStructure();require(structure.add(anchor,table,layout),"Table fixture supported");
                for(var cell:table.occupiedCells(anchor)) {
                    var pos=cell.above();var ornament=new RaftPiece(decoration,rotation,false);
                    require(structure.add(pos,ornament,layout),"Decoration mounted on either rotated table tile");
                    double bottom=structure.boxes(pos,ornament).stream().mapToDouble(box->box.minY).min().orElseThrow();
                    require(Math.abs(bottom-RaftEntity.DECK_HEIGHT-kind.tableSurface())<1e-9,"Decoration bottom touches actual table surface");
                    require(structure.boxes(anchor,table).stream().noneMatch(old->structure.boxes(pos,ornament).stream().anyMatch(box->old.intersects(box.deflate(1e-5)))),"Tabletop geometry clears cloth and table body");
                    require(!structure.add(pos,new RaftPiece(RaftPiece.Kind.BOOK_STACK,0,false),layout),"Occupied display tile rejects another ornament");
                }
                var saved=RaftStructure.decode(structure.encode(),layout);
                for(var entry:structure.pieces().entrySet())require(saved.boxes(entry.getKey(),entry.getValue()).equals(structure.boxes(entry.getKey(),entry.getValue())),"Mounted height persists exactly");
                require(!structure.remove(anchor,layout),"Cannot reclaim a table supporting ornaments");
                for(var cell:table.occupiedCells(anchor))require(structure.remove(cell.above(),layout),"Remove ornament independently");
                require(structure.remove(anchor,layout),"Empty table can be reclaimed");
            }
            var structure=new RaftStructure();structure.add(anchor,table,layout);
            var seed=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());seed.putString("Layout",layout.encode());seed.putString("Structure",structure.encode());
            var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));raft.setPos(8.5,level.getMaxY()+16,8.5);
            var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"TabletopPlacement"));
            try {
                require(level.addFreshEntity(raft),"Tabletop placement fixture registered");
                player.setPos(raft.toWorld(new Vec3(0,.25,-2.5)));
                player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.CERAMIC_VASE),2));
                var target=new Vec3(0,RaftEntity.DECK_HEIGHT+kind.tableSurface(),0);
                var delta=raft.toWorld(target).subtract(player.getEyePosition());
                player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));
                var plan=raft.placement(player,player.getMainHandItem(),raft.pick(player));
                require(plan!=null && plan.valid() && plan.cell().equals(anchor.above()),"Ray-traced ghost snaps onto table instead of floating");
                require(raft.interact(player,InteractionHand.MAIN_HAND,Vec3.ZERO).consumesAction() && player.getMainHandItem().getCount()==1,"Native tabletop placement consumes one item");
                require(raft.pieceBoxes(plan.cell(),plan.piece()).equals(plan.boxes()),"Placed tabletop geometry equals ghost");
                float yaw=raft.getYRot();raft.setYRot(73);raft.setPos(raft.position().add(3,.1,-2));
                require(raft.pieceBoxes(plan.cell(),plan.piece()).equals(plan.boxes()),"Moving and rotating raft preserves local tabletop mount");raft.setYRot(yaw);
            }finally{raft.discard();player.discard();}
        }
        var table=new RaftPiece(RaftPiece.Kind.DISPLAY_TABLE,0,false);var flower=new RaftPiece(RaftPiece.Kind.FLOWER_BOX,0,false);
        var structure=new RaftStructure();require(structure.add(anchor,table,layout) && structure.add(anchor.above(),flower,layout),"Wide flower box fits wide display table");
        var damaged=RaftLayout.decode(layout.encode());require(damaged.remove(new BlockPos(1,0,0)),"Damage distant display-table support");
        var removed=structure.pruneAfterDamage(damaged);require(removed.containsKey(anchor) && removed.containsKey(anchor.above()),"Support damage removes table and ornament without floating leftovers");
        structure=new RaftStructure();structure.add(anchor,new RaftPiece(RaftPiece.Kind.SIDE_TABLE,0,false),layout);
        require(!structure.add(anchor.above(),flower,layout),"Wide decoration cannot overhang small table");
        structure=new RaftStructure();structure.add(anchor,new RaftPiece(RaftPiece.Kind.WIDE_FLOOR,0,false),layout);structure.add(anchor.above(),table,layout);
        var pos=anchor.above(2);var vase=new RaftPiece(RaftPiece.Kind.CERAMIC_VASE,0,false);require(structure.add(pos,vase,layout),"Ornament supported on upper-floor table");
        require(Math.abs(structure.boxes(pos,vase).getFirst().minY-(RaftEntity.DECK_HEIGHT+.125+table.kind().tableSurface()))<1e-9,"Tabletop incorporates upper-floor mounting offset");
        var legacy=RaftStructure.decode("1,1,0,WALL,0,0;2,1,0,BOOK_STACK,0,0",layout);
        require(!legacy.pieces().get(new BlockPos(2,1,0)).tableMounted(),"Old side-attached decoration saves retain legacy support behavior");
        require(structure.pieces().get(pos).tableMounted() && structure.encode().contains(",tabletop"),"New table dependency explicitly saved");
        boolean orphanRejected=false;try{RaftStructure.decode("0,2,0,CERAMIC_VASE,0,0,tabletop",layout);}catch(IllegalArgumentException expected){orphanRejected=true;}
        require(orphanRejected,"Missing parent table cannot load as an orphaned ornament");
        return checks;
    }
    private RaftTabletopCheck() { }
}
