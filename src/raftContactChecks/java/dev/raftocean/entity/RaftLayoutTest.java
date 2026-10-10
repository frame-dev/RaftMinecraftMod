package dev.raftocean.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RaftLayoutTest {
    public static void run() {
        RaftLayout layout = RaftLayout.starter();
        require(layout.cells().size() == 9, "Starter deck has nine cells");
        require(layout.add(new BlockPos(2, 0, 0)), "Edge extension succeeds");
        require(!layout.add(new BlockPos(2, 0, 0)), "Duplicate is rejected");
        require(!layout.add(new BlockPos(5, 0, 5)), "Floating piece is rejected");
        require(layout.remove(BlockPos.ZERO), "Center hole is allowed");
        require(!layout.cells().contains(BlockPos.ZERO), "Removed cell has no foundation");
        require(RaftLayout.decode(layout.encode()).cells().equals(layout.cells()), "Saved layout round-trips including holes");
        RaftLayout bridge = RaftLayout.decode("0,0;1,0;2,0");
        require(!bridge.remove(new BlockPos(1, 0, 0)), "Removing a bridge cannot split the raft");
        require(bridge.remove(new BlockPos(2, 0, 0)), "Leaf removal is allowed");
        require(bridge.remove(new BlockPos(1, 0, 0)), "A second leaf may be removed");
        require(!bridge.remove(BlockPos.ZERO), "Last cell cannot be removed");
        boolean rejected = false;
        try { RaftLayout.decode("0,0;8,8"); } catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "Disconnected saved layout is rejected");
        for (Vec3 delta : new Vec3[]{new Vec3(0.02,0,0),new Vec3(-0.02,0,0),
                new Vec3(0,0,0.02),new Vec3(0,0,-0.02),new Vec3(-0.02,0,-0.008)}) {
            AABB deck = new AABB(-0.5,0,-0.5,0.5,0.25,0.5);
            AABB player = new AABB(-0.3,0.251,-0.3,0.3,2.051,0.3);
            Vec3 carry = RaftContact.carryDisplacement(player, deck.move(delta), delta, 0);
            require(carry.distanceToSqr(delta) < 1.0E-12, "Carry follows each signed X/Z movement vector");
        }
        require(RaftLayout.starter().rectangles().size()==1,"Filled starter merges into one rectangle");
        var holes=RaftLayout.decode("0,0;1,0;2,0;0,1;2,1;0,2;1,2;2,2");
        double area=holes.rectangles().stream().mapToDouble(box -> box.getXsize()*box.getZsize()).sum();
        require(area==8 && holes.rectangles().stream().noneMatch(box -> box.contains(new Vec3(1,.1,1))),"Merged deck preserves exact area and central hole");
        System.out.println("19 foundation/directional-carry regression checks passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
