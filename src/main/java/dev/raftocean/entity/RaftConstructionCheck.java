package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.RaftOcean;
import dev.raftocean.registry.ModContent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Isolated bootstrap checks for loaded resources, entity persistence, and upper-deck carry. */
public final class RaftConstructionCheck {
    private RaftConstructionCheck() { }

    public static int run(ServerLevel level) {
        int checks = 0;
        List<String> names = new ArrayList<>(List.of("foundation", "building_hammer"));
        for (RaftPiece.Kind kind : RaftPiece.Kind.values()) { if(kind!=RaftPiece.Kind.ADDON)names.add("raft_" + kind.id()); }
        for (String name : names) {
            Identifier id = Identifier.fromNamespaceAndPath(RaftOcean.MOD_ID, name);
            var key = ResourceKey.create(Registries.RECIPE, id);
            require(level.getServer().getRecipeManager().byKey(key).isPresent(), "Missing recipe " + id);
            checks++;
            var advancement = level.getServer().getAdvancements().get(
                    Identifier.fromNamespaceAndPath(RaftOcean.MOD_ID, "recipes/building/" + name));
            require(advancement != null && advancement.value().rewards().recipes().contains(key),
                    "Missing recipe-book unlock " + id);
            checks++;
        }
        CreativeModeTab tab = ModContent.CONSTRUCTION_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), true, level.registryAccess()));
        require(tab.getDisplayItems().size() == names.size(), "Construction tab must contain every building item");
        checks++;
        for (RaftPiece.Kind kind : RaftPiece.Kind.values()) {
            if(kind==RaftPiece.Kind.ADDON)continue;
            require(tab.getSearchTabDisplayItems().stream().anyMatch(stack -> stack.is(ModContent.structuralItem(kind))),
                    "Missing searchable construction item " + kind);
            checks++;
        }

        RaftLayout layout = RaftLayout.starter();
        for (int x : new int[] {-2, 2}) {
            for (int z = -1; z <= 1; z++) {
                require(layout.add(new BlockPos(x, 0, z)), "Additional station foundation");
                checks++;
            }
        }
        for (int z = -1; z <= 1; z++) {
            require(layout.add(new BlockPos(-3, 0, z)), "Survival station foundation"); checks++;
            require(layout.add(new BlockPos(3, 0, z)), "Survival station foundation"); checks++;
            require(layout.add(new BlockPos(-4, 0, z)), "Navigation station foundation"); checks++;
            require(layout.add(new BlockPos(4, 0, z)), "Navigation station foundation"); checks++;
        }
        RaftStructure structure = new RaftStructure();
        for (int x : new int[] {-5,5}) {
            for(int z=-1;z<=1;z++) { require(layout.add(new BlockPos(x,0,z)),"Capacity for expanded construction fixture");checks++; }
        }
        add(structure, layout, new BlockPos(-4, 1, -1), RaftPiece.Kind.CORNER_WALL, 0, false);
        add(structure, layout, new BlockPos(4, 1, 1), RaftPiece.Kind.CURVED_WALL, 2, false);
        add(structure, layout, new BlockPos(4, 1, 0), RaftPiece.Kind.TREE_PLANTER, 0, false);
        add(structure, layout, new BlockPos(0, 1, 0), RaftPiece.Kind.PILLAR, 0, false);
        add(structure, layout, new BlockPos(0, 4, 0), RaftPiece.Kind.FLOOR, 0, false);
        add(structure, layout, new BlockPos(0, 5, 0), RaftPiece.Kind.RAILING, 1, false);
        add(structure, layout, new BlockPos(-1, 1, -1), RaftPiece.Kind.DOOR, 3, true);
        add(structure, layout, new BlockPos(1, 1, -1), RaftPiece.Kind.WALL, 2, false);
        add(structure, layout, new BlockPos(1, 2, -1), RaftPiece.Kind.ROOF, 2, false);
        add(structure, layout, new BlockPos(1, 1, 0), RaftPiece.Kind.STAIRS, 1, false);
        add(structure, layout, new BlockPos(-1, 1, 0), RaftPiece.Kind.LADDER, 0, false);
        add(structure, layout, new BlockPos(0, 1, 1), RaftPiece.Kind.SHORT_PILLAR, 0, false);
        add(structure, layout, new BlockPos(-1, 1, 1), RaftPiece.Kind.HALF_WALL, 3, false);
        add(structure, layout, new BlockPos(0, 2, 1), RaftPiece.Kind.CRAFTING_TABLE, 0, false);
        add(structure, layout, new BlockPos(-2, 1, -1), RaftPiece.Kind.CHEST, 0, false);
        add(structure, layout, new BlockPos(-2, 1, 0), RaftPiece.Kind.BARREL, 1, false);
        add(structure, layout, new BlockPos(-2, 1, 1), RaftPiece.Kind.STONECUTTER, 2, false);
        add(structure, layout, new BlockPos(2, 1, -1), RaftPiece.Kind.SMITHING_TABLE, 3, false);
        add(structure, layout, new BlockPos(2, 1, 0), RaftPiece.Kind.GRINDSTONE, 0, false);
        add(structure, layout, new BlockPos(2, 1, 1), RaftPiece.Kind.LOOM, 1, false);
        add(structure, layout, new BlockPos(1, 1, 1), RaftPiece.Kind.CARTOGRAPHY_TABLE, 2, false);
        add(structure, layout, new BlockPos(-2, 2, 1), RaftPiece.Kind.COLLECTION_NET, 0, false);
        add(structure, layout, new BlockPos(-3, 1, -1), RaftPiece.Kind.PURIFIER, 0, false);
        add(structure, layout, new BlockPos(-3, 1, 0), RaftPiece.Kind.RAIN_COLLECTOR, 1, false);
        add(structure, layout, new BlockPos(-3, 1, 1), RaftPiece.Kind.GRILL, 2, false);
        add(structure, layout, new BlockPos(3, 1, -1), RaftPiece.Kind.COOKING_POT, 3, false);
        add(structure, layout, new BlockPos(3, 1, 0), RaftPiece.Kind.PLANTER, 0, false);
        add(structure, layout, new BlockPos(3, 1, 1), RaftPiece.Kind.SMELTER, 1, false);
        add(structure, layout, new BlockPos(-3, 2, -1), RaftPiece.Kind.BED, 2, false);
        add(structure, layout, new BlockPos(-2, 2, -1), RaftPiece.Kind.SAIL, 0, false);
        add(structure, layout, new BlockPos(-2, 2, 0), RaftPiece.Kind.STEERING_WHEEL, 1, false);
        add(structure, layout, new BlockPos(2, 2, -1), RaftPiece.Kind.ENGINE, 2, false);
        add(structure, layout, new BlockPos(2, 2, 1), RaftPiece.Kind.ANCHOR, 3, false);
        add(structure, layout, new BlockPos(3, 2, 0), RaftPiece.Kind.WIND_STREAMER, 0, false);
        checks += RaftPiece.Kind.values().length;
        add(structure,layout,new BlockPos(-5,1,-1),RaftPiece.Kind.ANVIL,0,false);
        add(structure,layout,new BlockPos(-5,1,0),RaftPiece.Kind.ENCHANTING_TABLE,0,false);
        add(structure,layout,new BlockPos(-5,1,1),RaftPiece.Kind.ENDER_CHEST,0,false);
        add(structure,layout,new BlockPos(5,1,0),RaftPiece.Kind.BOOKSHELF,0,false);
        add(structure,layout,new BlockPos(5,1,1),RaftPiece.Kind.RESEARCH_TABLE,0,false);

        int automationCell = 0;
        for(var kind:RaftPiece.Kind.values()) {
            if(!kind.automation() && !kind.conduit()){continue;}
            int x = 6 + automationCell / 3, z = automationCell % 3 - 1;
            require(layout.add(new BlockPos(x,0,z)),"Utility fixture foundation");
            add(structure,layout,new BlockPos(x,1,z),kind,automationCell%4,false);
            automationCell++;checks++;
        }

        if(!layout.cells().contains(new BlockPos(9,0,0)))require(layout.add(new BlockPos(9,0,0)),"Connect expansion fixture");
        require(layout.add(new BlockPos(-6,0,-1)) && layout.add(new BlockPos(-6,0,0)),"Starter-machine fixture foundations");
        add(structure,layout,new BlockPos(-6,1,-1),RaftPiece.Kind.BASIC_PURIFIER,0,false);
        add(structure,layout,new BlockPos(-6,1,0),RaftPiece.Kind.DRYING_RACK,0,false);
        require(layout.add(new BlockPos(-6,0,1)) && layout.add(new BlockPos(-7,0,0)),"Additional starter foundations");
        add(structure,layout,new BlockPos(-6,1,1),RaftPiece.Kind.BASIC_GRILL,0,false);
        add(structure,layout,new BlockPos(-7,1,0),RaftPiece.Kind.DISPOSABLE_ANCHOR,0,false);
        int expansionCell=10;
        for(var kind:RaftPiece.Kind.values())if(kind.ordinal()>RaftPiece.Kind.BATTERY_CHARGER.ordinal() && kind!=RaftPiece.Kind.ADDON && !kind.machine() && kind!=RaftPiece.Kind.DISPOSABLE_ANCHOR) {
            for(int x=expansionCell;x<Math.min(expansionCell+3,33);x++)require(layout.add(new BlockPos(x,0,0)),"Expansion fixture foundation");
            add(structure,layout,new BlockPos(expansionCell,1,0),kind,0,false);expansionCell+=3;
        }
        var reporter = new ProblemReporter.Collector();
        var seed = TagValueOutput.createWithContext(reporter, level.registryAccess());
        seed.putString("Layout", layout.encode());
        seed.putString("Structure", structure.encode());
        seed.putDouble("CurrentX", -.02);
        seed.putDouble("CurrentZ", .01);
        RaftEntity raft = new RaftEntity(ModContent.RAFT.get(), level);
        raft.readAdditionalSaveData(TagValueInput.create(reporter, level.registryAccess(), seed.buildResult()));
        raft.setPos(0, level.getMaxY() + 16, 0);
        var saved = TagValueOutput.createWithContext(reporter, level.registryAccess());
        raft.saveWithoutId(saved);
        RaftEntity restored = new RaftEntity(ModContent.RAFT.get(), level);
        restored.load(TagValueInput.create(reporter, level.registryAccess(), saved.buildResult()));
        require(reporter.isEmpty(), "Entity serialization reported a problem");
        checks++;
        require(restored.getStructuralCount() == RaftPiece.Kind.values().length-1 && restored.getMass() == raft.getMass(),
                "Every piece and its mass must survive entity save/load");
        checks++;
        require(restored.getLocalBoxes().equals(raft.getLocalBoxes())
                && restored.getCollisionBoxes().equals(raft.getCollisionBoxes()),
                "Open door, rotations, and mounted geometry must survive entity save/load");
        checks++;
        var resaved = TagValueOutput.createWithContext(reporter, level.registryAccess());
        restored.saveWithoutId(resaved);
        require(saved.buildResult().getStringOr("Structure", "").equals(resaved.buildResult().getStringOr("Structure", "")),
                "Save structure encoding must stay stable");
        checks++;
        require(resaved.buildResult().getDoubleOr("CurrentX", 0) == -.02
                && resaved.buildResult().getDoubleOr("CurrentZ", 0) == .01, "Current must survive save/load");
        checks++;

        FakePlayer player = new FakePlayer(level,
                new GameProfile(UUID.fromString("8a7081ad-d617-4e7e-a629-2ec54a646e08"), "RaftBuildCheck"));
        // Keep this exact-carry fixture clear of the railing; overlap recovery has separate tests.
        Vec3 relative = new Vec3(0, RaftEntity.DECK_HEIGHT + 3 + .125 + RaftContact.CLEARANCE, 0);
        player.setPos(restored.position().add(relative));
        player.setDeltaMovement(Vec3.ZERO);
        double previousWave = 0;
        for (int tick = 0; tick < 120; tick++) {
            double wave = Math.sin(tick * .075) * .012;
            double direction = tick < 60 ? 1 : -1;
            Vec3 delta = new Vec3(.02 * direction, wave - previousWave, .008 * direction);
            restored.setPos(restored.position().add(delta));
            RaftPlayerSupport.carryPlayer(restored, player, delta);
            require(player.position().subtract(restored.position()).distanceToSqr(relative) < 1.0E-12,
                    "Second-floor carry changed relative position at tick " + tick);
            checks++;
            previousWave = wave;
        }
        return checks+RaftClearanceCheck.run(level)+RaftBuildingUpgradeCheck.run(level);
    }

    private static void add(RaftStructure structure, RaftLayout layout, BlockPos pos,
            RaftPiece.Kind kind, int rotation, boolean open) {
        require(structure.add(pos, new RaftPiece(kind, rotation, open), layout), "Could not build " + kind);
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new IllegalStateException(message); }
    }
}
