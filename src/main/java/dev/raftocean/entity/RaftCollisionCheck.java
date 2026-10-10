package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.registry.ModContent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native collision regressions at the distant ends of a loaded, rotating raft. */
public final class RaftCollisionCheck {
    private RaftCollisionCheck() { }

    public static int run(ServerLevel level) {
        boolean forced = level.setChunkForced(0, 0, true);
        level.getChunkSource().tick(() -> true, false);
        level.waitForEntities(new net.minecraft.world.level.ChunkPos(0, 0), 0);
        RaftLayout layout = RaftLayout.starter();
        for (int x = 2; x <= 30; x++) {
            for (int z = -1; z <= 1; z++) {
                require(layout.add(new BlockPos(x, 0, z)), "Extend distant deck");
            }
        }
        require(layout.remove(new BlockPos(24, 0, 0)), "Create a real interior deck hole");
        RaftStructure structure = new RaftStructure();
        require(structure.add(new BlockPos(31, 0, 0), new RaftPiece(RaftPiece.Kind.COLLECTION_NET, 0, false), layout),
                "Attach distant collection net");
        for (int y = 1; y <= 13; y += 3) {
            require(structure.add(new BlockPos(30, y, 1), new RaftPiece(RaftPiece.Kind.PILLAR, 0, false), layout),
                    "Support upper deck");
        }
        require(structure.add(new BlockPos(30, 16, 1), new RaftPiece(RaftPiece.Kind.FLOOR, 0, false), layout),
                "Build upper floor");
        require(structure.add(new BlockPos(29, 16, 1), new RaftPiece(RaftPiece.Kind.FLOOR, 0, false), layout),
                "Extend upper floor");
        RaftEntity raft = new RaftEntity(ModContent.RAFT.get(), level);
        var reporter = new ProblemReporter.Collector();
        var seed = TagValueOutput.createWithContext(reporter, level.registryAccess());
        seed.putString("Layout", layout.encode());
        seed.putString("Structure", structure.encode());
        raft.readAdditionalSaveData(TagValueInput.create(reporter, level.registryAccess(), seed.buildResult()));
        Vec3 origin = new Vec3(8, level.getMaxY() + 28, 8);
        raft.setPos(origin);
        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "RaftCollisionCheck"));
        int checks = 0;
        try {
            require(level.addFreshEntity(raft), "Register distant-deck fixture");
            for (float yaw : new float[] {0, 37, 90, 143, 180, 217, 270, 323}) {
                raft.setYRot(yaw);
                raft.refreshBounds();
                for (double x : new double[] {0, 8, 16, 25, 29, 31}) {
                    player.setPos(raft.toWorld(new Vec3(x, RaftEntity.DECK_HEIGHT + RaftContact.CLEARANCE, 0)));
                    player.setDeltaMovement(Vec3.ZERO);
                    player.setOnGround(false);
                    double feet = player.getY();
                    player.move(MoverType.SELF, new Vec3(0, -.08, 0));
                    require(Math.abs(player.getY() - (feet - RaftContact.CLEARANCE)) < 1.0E-6,
                            "Deck collision missing at local X=" + x + ", yaw=" + yaw + "; player fell " + (feet - player.getY()));
                    checks++;
                    var area = player.getBoundingBox().inflate(.2);
                    require(level.getEntitiesOfClass(RaftEntity.class, area).stream().filter(e -> e == raft).count() == 1,
                            "Typed distant-deck search must find exactly one raft"); checks++;
                    require(level.getEntities((Entity) null, area, e -> e == raft).equals(List.of(raft)),
                            "Untyped picking search must find the distant raft"); checks++;
                    require(level.getEntities(raft, area, e -> true).stream().noneMatch(e -> e == raft),
                            "Excluded entity must stay excluded"); checks++;
                    require(level.getEntitiesOfClass(RaftEntity.class, area, e -> false).isEmpty(),
                            "Search predicate must stay authoritative"); checks++;
                    require(level.getEntitiesOfClass(FakePlayer.class, area).isEmpty(),
                            "Raft must not match another entity type"); checks++;
                    List<RaftEntity> limited = new ArrayList<>();
                    level.getEntities(EntityTypeTest.forClass(RaftEntity.class), area, e -> true, limited, 1);
                    require(limited.equals(List.of(raft)), "Limited search must find the distant raft once"); checks++;
                    require(level.hasEntities(EntityTypeTest.forClass(RaftEntity.class), area, e -> true),
                            "Existence search must see the distant raft"); checks++;
                }
                player.setPos(raft.toWorld(new Vec3(25, RaftEntity.DECK_HEIGHT + RaftContact.CLEARANCE, 0)));
                Vec3 before = player.position();
                Vec3 shift = new Vec3(.03, .005, -.01);
                raft.setPos(raft.position().add(shift));
                RaftPlayerSupport.carryPlayer(raft, player, shift, yaw);
                require(player.position().distanceToSqr(before.add(shift)) < 1.0E-10,
                        "Distant supported player must follow the raft"); checks++;
                player.setPos(raft.toWorld(new Vec3(24, RaftEntity.DECK_HEIGHT + RaftContact.CLEARANCE, 3)));
                double feet = player.getY();
                player.move(MoverType.SELF, new Vec3(0, -.08, 0));
                require(player.getY() < feet - .079, "Water beyond the deck must remain unsupported"); checks++;
                player.setPos(raft.toWorld(new Vec3(24, RaftEntity.DECK_HEIGHT + RaftContact.CLEARANCE, 0)));
                feet = player.getY();
                player.move(MoverType.SELF, new Vec3(0, -.08, 0));
                require(player.getY() < feet - .079, "A real interior hole must remain unsupported"); checks++;
                player.setPos(raft.toWorld(new Vec3(29, 15.375 + RaftContact.CLEARANCE, 1)));
                feet = player.getY();
                player.move(MoverType.SELF, new Vec3(0, -.08, 0));
                require(Math.abs(player.getY() - (feet - RaftContact.CLEARANCE)) < 1.0E-6,
                        "Upper-floor collision must work across horizontal and vertical sections"); checks++;
                raft.setPos(origin);
            }
            raft.discard();
            var area = player.getBoundingBox().inflate(5);
            require(level.getEntitiesOfClass(RaftEntity.class, area).isEmpty(), "Removed raft must not leave ghost support"); checks++;
            var sprinkler=dev.raftocean.survival.SurvivalCheck.create(level,RaftPiece.Kind.SPRINKLER);
            sprinkler.setPos(origin);
            try {
                require(level.addFreshEntity(sprinkler),"Register sprinkler collision fixture");
                for(float yaw:new float[]{0,37,90,143,180,217,270,323}) {
                    sprinkler.setYRot(yaw);sprinkler.refreshBounds();
                    Vec3 start=sprinkler.toWorld(new Vec3(.2,RaftEntity.DECK_HEIGHT+.125+RaftContact.CLEARANCE,.6));
                    Vec3 target=sprinkler.toWorld(new Vec3(1.8,RaftEntity.DECK_HEIGHT+.125+RaftContact.CLEARANCE,.6));
                    player.setPos(start);player.setOnGround(false);player.setDeltaMovement(Vec3.ZERO);
                    for(int step=0;step<16;step++){player.move(MoverType.SELF,target.subtract(start).scale(1.0/16));}
                    require(player.position().distanceToSqr(target)<1.0E-8,
                            "Sprinkler's empty shoulder space must not block walking at yaw "+yaw+"; expected "+target+", got "+player.position());checks++;
                }
            } finally {sprinkler.discard();}
            return checks;
        } finally {
            raft.discard();
            if (forced) { level.setChunkForced(0, 0, false); }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new IllegalStateException(message); }
    }
}
