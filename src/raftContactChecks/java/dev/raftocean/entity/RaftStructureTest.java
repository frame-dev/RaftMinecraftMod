package dev.raftocean.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Support graph, save format, door reservations, and upper-surface regressions. */
public final class RaftStructureTest {
    private static int checks;
    public static void run() {
        checks = 0;
        RaftLayout base = RaftLayout.starter();
        for(var kind:new RaftPiece.Kind[]{RaftPiece.Kind.SAIL,RaftPiece.Kind.STEERING_WHEEL}){
            for(int rotation=0;rotation<4;rotation++){
                var piece=new RaftPiece(kind,rotation,false);
                var body=new AABB(.08,RaftEntity.DECK_HEIGHT+.126,.08,.68,RaftEntity.DECK_HEIGHT+1.926,.68);
                for(int i=0;i<rotation;i++){body=new AABB(-body.maxZ,body.minY,body.minX,-body.minZ,body.maxY,body.maxX);}
                final var passage=body;
                require(piece.collisionBoxes(new BlockPos(0,1,0)).stream().noneMatch(box->box.intersects(passage)),
                        "Visible clearance around mast/wheel remains open: "+kind+" "+rotation);
                require(piece.collisionBoxes(new BlockPos(0,1,0)).stream().anyMatch(box->box.contains(new Vec3(0,RaftEntity.DECK_HEIGHT+.2,0))),
                        "Actual mast/post remains solid: "+kind+" "+rotation);
            }
        }
        for(var kind:new RaftPiece.Kind[]{RaftPiece.Kind.ENGINE,RaftPiece.Kind.SMELTER}){
            double height=kind==RaftPiece.Kind.ENGINE?.626:.751;
            var passage=new AABB(.2,RaftEntity.DECK_HEIGHT+height,.2,.8,RaftEntity.DECK_HEIGHT+height+1.8,.8);
            require(new RaftPiece(kind,0,false).collisionBoxes(new BlockPos(0,1,0)).stream().noneMatch(box->box.intersects(passage)),
                    "Exhaust/chimney does not fill upper corner air: "+kind);
        }
        RaftStructure edge=new RaftStructure();
        RaftPiece net=new RaftPiece(RaftPiece.Kind.COLLECTION_NET,0,false);
        require(edge.add(new BlockPos(2,0,0),net,base),"Net attaches at foundation edge");
        require(edge.collisionBoxes(new BlockPos(2,0,0),net).getFirst().maxY==RaftEntity.DECK_HEIGHT,"Net walking surface is flush with foundations");
        require(!edge.add(BlockPos.ZERO,net,base),"Net cannot overlap foundation");
        require(RaftStructure.decode(edge.encode(),base).encode().equals(edge.encode()),"Foundation-level net survives structural save");
        require(!edge.add(new BlockPos(5,0,0),net,base),"Disconnected net rejected");
        RaftStructure mounted=new RaftStructure();
        BlockPos floorCell=new BlockPos(0,1,0),doorCell=floorCell.above();
        RaftPiece mountedFloor=new RaftPiece(RaftPiece.Kind.FLOOR,0,false);
        RaftPiece doorPiece=new RaftPiece(RaftPiece.Kind.DOOR,0,false);
        require(mounted.add(floorCell,mountedFloor,base) && mounted.add(doorCell,doorPiece,base),"Upper-floor doorway has support");
        require(mounted.boxes(doorCell,doorPiece).getFirst().minY==mounted.boxes(floorCell,mountedFloor).getFirst().maxY,"Door bottom mounts flush to thin floor");
        require(mounted.attachment(doorCell,doorPiece,Direction.NORTH,false,1.75).equals(doorCell.above().north()),"Mounted doorway side attachments use actual clicked level");
        for(int rotation=0;rotation<4;rotation++) {
            var corner=new RaftPiece(RaftPiece.Kind.CORNER_WALL,rotation,false);
            var curved=new RaftPiece(RaftPiece.Kind.CURVED_WALL,rotation,false);
            require(corner.boxes(new BlockPos(0,1,0)).size()==2,"Corner has two independently colliding edges");
            require(curved.boxes(new BlockPos(0,1,0)).size()==8,"Curve retains thin segmented collider");
            require(curved.collisionBoxes(new BlockPos(0,1,0)).stream().noneMatch(box->box.contains(new Vec3(0,.75,0))),"Curved wall keeps cell centre walkable");
        }
        RaftStructure structure = new RaftStructure();
        BlockPos lower = new BlockPos(0, 1, 0);
        BlockPos upper = lower.above();
        RaftPiece wall = new RaftPiece(RaftPiece.Kind.WALL, 0, false);
        require(!structure.add(new BlockPos(0, 5, 0), wall, base), "Floating wall rejected");
        require(structure.add(lower, wall, base), "Wall attaches to foundation");
        require(structure.add(upper, new RaftPiece(RaftPiece.Kind.FLOOR, 0, false), base), "Upper floor attaches to wall");
        require(!structure.remove(lower, base), "Cannot remove support beneath an upper floor");
        require(structure.add(new BlockPos(1, 2, 0), new RaftPiece(RaftPiece.Kind.ROOF, 1, false), base), "Roof connects to upper floor");
        require(structure.mass() == 11, "Structural mass sums distinct pieces");
        require(RaftStructure.decode(structure.encode(), base).encode().equals(structure.encode()), "Save round trip");
        RaftLayout hole = RaftLayout.decode(base.encode());
        require(hole.remove(BlockPos.ZERO) && !structure.supportedBy(hole), "Foundation under tower cannot disappear");
        RaftStructure door = new RaftStructure();
        require(door.add(lower, new RaftPiece(RaftPiece.Kind.DOOR, 3, false), base), "Two-cell door supported");
        require(!door.add(upper, wall, base), "Door reserves upper cell");
        door.toggleDoor(lower);
        require(RaftStructure.decode(door.encode(), base).pieces().get(lower).open(), "Open door persists");
        require(!door.add(new BlockPos(0, RaftStructure.MAX_HEIGHT, 0), new RaftPiece(RaftPiece.Kind.DOOR, 0, false), base), "Door height bounded");
        RaftLayout single = RaftLayout.decode("0,0");
        RaftStructure loaded = new RaftStructure();
        require(loaded.add(lower, new RaftPiece(RaftPiece.Kind.STAIRS, 0, false), single), "One foundation supports one stair");
        require(loaded.add(upper, wall, single), "One foundation now carries stairs and a wall");
        require(loaded.add(upper.above(),new RaftPiece(RaftPiece.Kind.ENGINE,0,false),single), "Higher capacity accepts a heavy engine");
        require(!loaded.add(upper.above(2),wall,single), "Weight above the increased capacity still refuses placement");
        require(loaded.pieces().size() == 3, "Failed overload leaves supported equipment intact");
        RaftLayout bridged=RaftLayout.starter();
        require(bridged.add(new BlockPos(3,0,0),edge.platforms()),"Foundation extends through edge net");
        require(edge.supportedBy(bridged),"Net connects separated wood foundations");
        require(!edge.remove(new BlockPos(2,0,0),bridged),"Required net bridge cannot be removed");
        require(RaftLayout.decode(bridged.encode(),RaftStructure.platforms(edge.encode())).encode().equals(bridged.encode()),"Net-connected layout reloads");
        boolean rejected = false;
        try { RaftStructure.decode("0,4,0,WALL,0,0", base); } catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "Unsupported saved graph rejected");
        AABB floor = new RaftPiece(RaftPiece.Kind.FLOOR, 0, false).boxes(upper).getFirst();
        AABB body = new AABB(-.3, floor.maxY + .001, -.3, .3, floor.maxY + 1.801, .3);
        Vec3 delta = new Vec3(.02, -.005, -.01);
        require(RaftContact.carryDisplacement(body, floor.move(delta), delta, 0).distanceToSqr(delta) < 1.0E-12,
                "Carry recognizes elevated floor height");
        RaftPiece ladder = new RaftPiece(RaftPiece.Kind.LADDER, 1, false);
        require(ladder.boxes(lower).size() == 6 && ladder.collisionBoxes(lower).size() == 1,
                "Ladder rungs use a continuous collider for descending");
        require(ladder.climbBox(lower).getXsize() < ladder.climbBox(lower).getZsize(), "Ladder climb zone rotates");
        require(new RaftPiece(RaftPiece.Kind.DOOR, 0, false).boxes(lower).getFirst().getZsize()
                < new RaftPiece(RaftPiece.Kind.DOOR, 0, true).boxes(lower).getFirst().getZsize(), "Door opens sideways");
        RaftPiece pillar = new RaftPiece(RaftPiece.Kind.PILLAR, 0, false);
        RaftPiece floorPiece = new RaftPiece(RaftPiece.Kind.FLOOR, 0, false);
        RaftStructure tower = new RaftStructure();
        BlockPos secondFloor = lower.above(3);
        require(tower.add(lower, pillar, base), "Tall pillar attaches to foundation");
        require(!tower.add(lower.above(), wall, base), "Pillar reserves middle cell");
        require(!tower.add(lower.above(2), wall, base), "Pillar reserves upper cell");
        require(tower.add(secondFloor, floorPiece, base), "Second floor attaches above tall pillar");
        require(floorPiece.boxes(secondFloor).getFirst().minY - RaftEntity.DECK_HEIGHT == 3,
                "Second floor gives three blocks of clear height");
        require(tower.add(secondFloor.east(), floorPiece, base), "Upper floor extends horizontally");
        require(!tower.remove(lower, base), "Cannot remove last pillar supporting upper deck");
        require(tower.add(lower.east(), pillar, base) && tower.remove(lower, base),
                "Alternate pillar supports deck after first pillar removal");
        RaftPiece railing = new RaftPiece(RaftPiece.Kind.RAILING, 2, false);
        require(tower.add(secondFloor.east().above(), railing, base), "Railing mounts above upper floor");
        require(tower.collisionBoxes(secondFloor.east().above(), railing).getFirst().minY
                == floorPiece.boxes(secondFloor.east()).getFirst().maxY,
                "Upper railing sits directly on thin floor without a gap");
        require(!tower.remove(secondFloor.east(), base), "Remove mounted railing before its floor");
        RaftPiece halfWall = new RaftPiece(RaftPiece.Kind.HALF_WALL, 0, false);
        require(tower.boxes(secondFloor.above(), halfWall).getFirst().minY
                == floorPiece.boxes(secondFloor).getFirst().maxY, "Upper half wall sits on thin floor");
        require(RaftStructure.decode(tower.encode(), base).encode().equals(tower.encode()),
                "Pillar deck and railing save round trip");
        require(!tower.add(new BlockPos(-1, 5, 0), pillar, base), "Tall pillar bounded by top reserved cell");
        boolean overlapRejected = false;
        try { RaftStructure.decode("0,1,0,PILLAR,0,0;0,2,0,WALL,0,0", base); }
        catch (IllegalArgumentException expected) { overlapRejected = true; }
        require(overlapRejected, "Saved pillar overlap rejected");
        require(pillar.attachment(lower, Direction.UP, false, 3.25).equals(secondFloor),
                "Pillar top placement skips all reserved cells");
        require(pillar.attachment(lower, Direction.NORTH, true, .5).equals(secondFloor),
                "Crouch placement snaps above tall pillar");
        require(pillar.attachment(lower, Direction.EAST, false, 2.5).equals(lower.above(2).east()),
                "Pillar side placement uses clicked level");
        require(new RaftPiece(RaftPiece.Kind.DOOR, 0, false)
                .attachment(lower, Direction.UP, false, 2.25).equals(lower.above(2)), "Door top snap preserved");
        require(railing.boxes(lower).size() == 4 && railing.collisionBoxes(lower).size() == 1,
                "Railing has visible rails and continuous safety collision");
        RaftStructure shortTower = new RaftStructure();
        require(shortTower.add(lower, new RaftPiece(RaftPiece.Kind.SHORT_PILLAR, 0, false), base)
                && shortTower.add(upper, floorPiece, base), "Short pillar supports a low platform");
        require(new RaftPiece(RaftPiece.Kind.HALF_WALL, 0, false).boxes(lower).getFirst().getYsize() == .5,
                "Half wall has half-block collision height");
        AABB raised = floorPiece.boxes(secondFloor).getFirst();
        AABB raisedBody = new AABB(-.3, raised.maxY + .001, -.3, .3, raised.maxY + 1.801, .3);
        require(RaftContact.carryDisplacement(raisedBody, raised.move(delta), delta, 0).distanceToSqr(delta) < 1.0E-12,
                "Pillar-height second floor retains movement carry");
        RaftPiece craftingTable = new RaftPiece(RaftPiece.Kind.CRAFTING_TABLE, 0, false);
        require(craftingTable.boxes(lower).size() == 5 && craftingTable.collisionBoxes(lower).size() == 1,
                "Crafting table has original legs and a continuous collider");
        require(tower.add(secondFloor.above(), craftingTable, base), "Table mounts on upper floor");
        require(tower.boxes(secondFloor.above(), craftingTable).stream().mapToDouble(box -> box.minY).min().orElseThrow()
                == floorPiece.boxes(secondFloor).getFirst().maxY, "Crafting table sits flush on upper floor");
        require(!tower.remove(secondFloor, base), "Remove mounted table before removing its floor");
        require(RaftStructure.decode(tower.encode(), base).encode().equals(tower.encode()), "Mounted crafting table persists");
        System.out.println(checks + " structural support/geometry regression checks passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
        checks++;
    }
}
