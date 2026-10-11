package dev.raftocean.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.*;

/** Rotated ownership, collision, support loss, bed homes and visual light regressions. */
public final class RaftFurnitureCheck {
    private static int checks;
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);checks++;}
    public static int run(ServerLevel level) {
        checks=0;var anchor=new BlockPos(0,1,0);
        for(var kind:RaftPiece.Kind.values())if(kind.furniture())for(int rotation=0;rotation<4;rotation++) {
            var layout=RaftLayout.starter();var structure=new RaftStructure();var piece=new RaftPiece(kind,rotation,false);
            require(structure.add(anchor,piece,layout),"Furniture has complete supports: "+kind);
            require(piece.occupiedCells(anchor).size()==piece.width()*piece.depth()*piece.height(),"Furniture footprint rotates without duplicates");
            require(ModContent.structuralKind(new ItemStack(ModContent.structuralItem(kind)))==kind,"Furniture item resolves placement");
            for(var cell:piece.occupiedCells(anchor)) {
                require(structure.pieceAt(cell)==piece,"Every occupied tile resolves one owner");
                require(!structure.add(cell,new RaftPiece(RaftPiece.Kind.SHORT_PILLAR,0,false),layout),"Overlap refused throughout footprint");
            }
            require(RaftStructure.decode(structure.encode(),layout).pieces().equals(structure.pieces()),"Furniture rotation saves and loads");
            require(structure.collisionBoxes(anchor,piece).size()==(kind==RaftPiece.Kind.WOVEN_RUG?0:kind==RaftPiece.Kind.CANVAS_CANOPY?piece.boxes(anchor).size():1),"Furniture preserves safe collision and open canopy interior");
            if(kind==RaftPiece.Kind.CANVAS_CANOPY) {
                var bottom=piece.occupiedCells(anchor).stream().filter(p->p.getY()==anchor.getY()).toList();
                double x=bottom.stream().mapToInt(BlockPos::getX).average().orElseThrow(),z=bottom.stream().mapToInt(BlockPos::getZ).average().orElseThrow();
                var standing=new net.minecraft.world.phys.AABB(x-.3,RaftEntity.DECK_HEIGHT,z-.3,x+.3,RaftEntity.DECK_HEIGHT+1.8,z+.3);
                require(structure.collisionBoxes(anchor,piece).stream().noneMatch(standing::intersects),"Canopy has walkable headroom");
            }
            if(piece.width()*piece.depth()>1) {
                var support=piece.occupiedCells(anchor).stream().filter(p->p.getY()==anchor.getY() && !p.equals(anchor)).findFirst().orElseThrow().below();
                var damaged=RaftLayout.decode(layout.encode());require(damaged.remove(support),"Remove distant furniture support");
                require(!structure.supportedBy(damaged),"Wide furniture detects distant support loss");
                require(structure.pruneAfterDamage(damaged).containsKey(anchor),"Unsupported furniture removed as one piece");
                var incomplete=new RaftStructure();require(!incomplete.add(anchor,piece,damaged),"Cannot place furniture across absent support");
            }
            var intact=new RaftStructure();require(intact.add(anchor,piece,layout) && intact.remove(anchor,layout) && intact.pieces().isEmpty(),"One removal frees complete footprint");
        }
        for(var kind:List.of(RaftPiece.Kind.CABIN_BED,RaftPiece.Kind.DOUBLE_BED)) {
            var structure=new RaftStructure();require(structure.add(anchor,new RaftPiece(kind,0,false),RaftLayout.starter()),"Bed fixture supported");
            var seed=net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess());
            seed.putString("Layout",RaftLayout.starter().encode());seed.putString("Structure",structure.encode());
            var raft=new RaftEntity(ModContent.RAFT.get(),level);raft.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));raft.setPos(0,level.getMaxY()-8,0);
            var player=new net.neoforged.neoforge.common.util.FakePlayer(level,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"FurnitureSleeper"));
            try {
                player.setPos(raft.stationPosition(anchor,kind));
                require(RaftBeds.use(raft,anchor,player).consumesAction(),"Larger bed supports native raft rest");
                player.setData(SurvivalContent.BED_HOME,new RaftBeds.Home(raft.getUUID(),anchor));
                require(RaftBeds.spawnPosition(player,raft)!=null,"Larger bed has a safe respawn position");
                require(raft.removeStructuralPiece(anchor),"Larger bed can be reclaimed");
                require(RaftBeds.spawnPosition(player,raft)==null,"Removed bed invalidates moving home");
            }finally{raft.discard();player.discard();}
        }
        require(RaftFurniture.brightness(Vec3.ZERO,List.of())==0,"No lamps adds no light");
        require(RaftFurniture.brightness(Vec3.ZERO,List.of(Vec3.ZERO))==14,"Lamp maximum light");
        require(RaftFurniture.brightness(new Vec3(5,0,0),List.of(Vec3.ZERO))==9,"Lamp light attenuates");
        require(RaftFurniture.brightness(new Vec3(15,0,0),List.of(Vec3.ZERO))==0,"Lamp light range bounded");
        require(RaftFurniture.brightness(new Vec3(20,0,0),List.of(new Vec3(20,0,0)))==14,"Moving lamp retains local light");
        return checks;
    }
    private RaftFurnitureCheck() { }
}
