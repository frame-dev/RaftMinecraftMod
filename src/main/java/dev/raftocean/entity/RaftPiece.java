package dev.raftocean.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/** Original geometry shared by picking, rendering, collision, and climbing. */
public record RaftPiece(Kind kind, int rotation, boolean open, String addonId, boolean tableMounted) {
    public enum Kind {
        FLOOR(4), WALL(4), CORNER_WALL(6), CURVED_WALL(6), DOOR(6, 2), STAIRS(6), LADDER(2), ROOF(3),
        PILLAR(6, 3), SHORT_PILLAR(2), RAILING(2), HALF_WALL(2), CRAFTING_TABLE(6), RESEARCH_TABLE(6),
        CHEST(6), BARREL(6), STONECUTTER(8), SMITHING_TABLE(8), GRINDSTONE(6), LOOM(4), CARTOGRAPHY_TABLE(4),
        ANVIL(16), ENCHANTING_TABLE(10), ENDER_CHEST(8), BOOKSHELF(4), COLLECTION_NET(4),
        PURIFIER(4), RAIN_COLLECTOR(4), GRILL(4), COOKING_POT(6), PLANTER(4), TREE_PLANTER(8, 4), SMELTER(8), BED(4),
        SAIL(8, 3), STEERING_WHEEL(4), ENGINE(12), ANCHOR(6), WIND_STREAMER(2, 2), WATER_PIPE(1), FUEL_PIPE(1), POWER_CABLE(1), WATER_TANK(8), FUEL_TANK(8),
        BIOREFINER(10), BIOFUEL_GENERATOR(12), ELECTRIC_PURIFIER(10), SPRINKLER(4), BATTERY_CHARGER(8),
        WINDOW_WALL(4), TRIANGLE_WALL(3), ARCHWAY(6,2), HALF_STAIRS(3), WIDE_STAIRS(10),
        TALL_LADDER(4,2), WIDE_FLOOR(8), WIDE_CRAFTING_TABLE(10), ADDON(1), BASIC_PURIFIER(3), DRYING_RACK(2), DISPOSABLE_ANCHOR(4), BASIC_GRILL(3),
        CABIN_BED(6), DOUBLE_BED(10), FLOOR_LAMP(3,2), TABLE_LAMP(2), WOODEN_CHAIR(3), DINING_TABLE(6), SOFA(6), WOVEN_RUG(1), DECORATIVE_SHELF(5), POTTED_PALM(3,2),
        DECK_BENCH(5), COFFEE_TABLE(4), FLOWER_BOX(2), BOOK_STACK(1), CERAMIC_VASE(1), CHART_STAND(3), NAUTICAL_SCREEN(4,2), CANVAS_CANOPY(8,3), SIDE_TABLE(3), DISPLAY_TABLE(5);
        public final double mass;
        public final int height;
        Kind(double mass) { this(mass, 1); }
        Kind(double mass, int height) { this.mass = mass; this.height = height; }
        public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
        public boolean ladder() { return this==LADDER || this==TALL_LADDER; }
        public boolean floor() { return this==FLOOR || this==WIDE_FLOOR; }
        public boolean grill() { return this == GRILL || this == BASIC_GRILL; }
        public boolean anchor() { return this == ANCHOR || this == DISPOSABLE_ANCHOR; }
        public boolean purifier() { return this == PURIFIER || this == BASIC_PURIFIER; }
        public boolean bed() { return this == BED || this == CABIN_BED || this == DOUBLE_BED; }
        public boolean lamp() { return this == FLOOR_LAMP || this == TABLE_LAMP; }
        public boolean furniture() { return ordinal() >= CABIN_BED.ordinal(); }
        public boolean tableDecoration() { return this==TABLE_LAMP || this==BOOK_STACK || this==CERAMIC_VASE || this==POTTED_PALM || this==CHART_STAND || this==FLOWER_BOX; }
        public double tableSurface() { return switch(this) { case SIDE_TABLE -> .72;case DISPLAY_TABLE -> .78;case DINING_TABLE -> .83;case COFFEE_TABLE -> .485;default -> -1; }; }
        public boolean machine() { return this == BASIC_PURIFIER || this == DRYING_RACK || this == PURIFIER || this == RAIN_COLLECTOR || grill()
                || this == COOKING_POT || this == PLANTER || this == TREE_PLANTER || this == SMELTER || automation(); }
        public boolean conduit() { return this == WATER_PIPE || this == FUEL_PIPE || this == POWER_CABLE; }
        public boolean automation() { return this == WATER_TANK || this == FUEL_TANK || this == BIOREFINER
                || this == BIOFUEL_GENERATOR || this == ELECTRIC_PURIFIER || this == SPRINKLER || this == BATTERY_CHARGER; }
        public boolean navigation() { return this == SAIL || this == STEERING_WHEEL || this == ENGINE || anchor(); }
        public boolean storage() { return this == CHEST || this == BARREL || this == COLLECTION_NET || machine() || navigation(); }
        public boolean station() {
            return storage() || this == WIDE_CRAFTING_TABLE || this == CRAFTING_TABLE || this == RESEARCH_TABLE || this == STONECUTTER || this == SMITHING_TABLE
                    || this == GRINDSTONE || this == LOOM || this == CARTOGRAPHY_TABLE || bed()
                    || this == ANVIL || this == ENCHANTING_TABLE || this == ENDER_CHEST;
        }
    }

    public RaftPiece(Kind kind,int rotation,boolean open) { this(kind,rotation,open,"",false); }
    public RaftPiece(Kind kind,int rotation,boolean open,String addonId) { this(kind,rotation,open,addonId,false); }
    public RaftPiece onTable() { return new RaftPiece(kind,rotation,open,addonId,true); }
    public dev.raftocean.api.RaftBuildable addon() {
        return dev.raftocean.api.RaftBuildables.find(addonId).orElseThrow(
                ()->new IllegalStateException("Required raft addon definition is missing: "+addonId));
    }
    public int width() { return kind==Kind.ADDON && !addonId.isEmpty()?addon().width():kind==Kind.WIDE_FLOOR || kind==Kind.WIDE_STAIRS || kind==Kind.WIDE_CRAFTING_TABLE || kind==Kind.DOUBLE_BED || kind==Kind.DINING_TABLE || kind==Kind.SOFA || kind==Kind.WOVEN_RUG || kind==Kind.DECORATIVE_SHELF || kind==Kind.DECK_BENCH || kind==Kind.COFFEE_TABLE || kind==Kind.FLOWER_BOX || kind==Kind.NAUTICAL_SCREEN || kind==Kind.CANVAS_CANOPY || kind==Kind.DISPLAY_TABLE?2:1; }
    public int depth() { return kind==Kind.ADDON && !addonId.isEmpty()?addon().depth():kind==Kind.CABIN_BED || kind==Kind.DOUBLE_BED || kind==Kind.WOVEN_RUG || kind==Kind.CANVAS_CANOPY?2:1; }
    public int height() { return kind==Kind.ADDON && !addonId.isEmpty()?addon().height():kind.height; }
    public double mass() { return kind==Kind.ADDON && !addonId.isEmpty()?addon().mass():kind.mass; }
    public RaftPiece withRotation(int turn) { return new RaftPiece(kind,turn,open,addonId,tableMounted); }
    public java.util.Set<BlockPos> occupiedCells(BlockPos anchor) {
        var cells=new java.util.LinkedHashSet<BlockPos>();
        for(int x=0;x<width();x++)for(int z=0;z<depth();z++)for(int y=0;y<height();y++) {
            int X=x,Z=z;for(int i=0;i<rotation;i++){int old=X;X=-Z;Z=old;}
            cells.add(anchor.offset(X,y,Z));
        }
        return cells;
    }
    public RaftPiece {
        java.util.Objects.requireNonNull(kind);java.util.Objects.requireNonNull(addonId);
        if(tableMounted && !kind.tableDecoration())throw new IllegalArgumentException("Only tabletop ornaments can be table mounted");
        if(kind!=Kind.ADDON && !addonId.isEmpty() || kind==Kind.ADDON && !addonId.isEmpty()
                && dev.raftocean.api.RaftBuildables.find(addonId).isEmpty()) {
            throw new IllegalArgumentException("Unknown raft addon: "+addonId);
        }
        if (rotation < 0 || rotation > 3 || (open && kind != Kind.DOOR)) {
            throw new IllegalArgumentException("Invalid raft piece state");
        }
    }

    public List<AABB> boxes(BlockPos pos) {
        List<AABB> boxes = new ArrayList<>();
        switch (kind) {
            case CABIN_BED, DOUBLE_BED, FLOOR_LAMP, TABLE_LAMP, WOODEN_CHAIR, DINING_TABLE, SOFA, WOVEN_RUG, DECORATIVE_SHELF, POTTED_PALM,
                    DECK_BENCH, COFFEE_TABLE, FLOWER_BOX, BOOK_STACK, CERAMIC_VASE, CHART_STAND, NAUTICAL_SCREEN, CANVAS_CANOPY, SIDE_TABLE, DISPLAY_TABLE -> boxes.addAll(RaftFurniture.geometry(kind));
            case ADDON -> boxes.addAll(addonId.isEmpty()?List.of(new AABB(-.5,0,-.5,.5,1,.5)):addon().geometry());
            case WINDOW_WALL -> {
                boxes.add(new AABB(-.5,0,-.5,.5,.3,-.375));
                boxes.add(new AABB(-.5,.85,-.5,.5,1,-.375));
                boxes.add(new AABB(-.5,.3,-.5,-.35,.85,-.375));
                boxes.add(new AABB(.35,.3,-.5,.5,.85,-.375));
            }
            case TRIANGLE_WALL -> {
                for(int i=0;i<8;i++)boxes.add(new AABB(-.5+i*.125,0,-.5,-.375+i*.125,(i+1)*.125,-.375));
            }
            case ARCHWAY -> {
                boxes.add(new AABB(-.5,0,-.5,-.375,2,-.375));
                boxes.add(new AABB(.375,0,-.5,.5,2,-.375));
                boxes.add(new AABB(-.375,1.875,-.5,.375,2,-.375));
            }
            case HALF_STAIRS, WIDE_STAIRS -> {
                for(int i=0;i<4;i++)boxes.add(new AABB(-.5,0,-.5+i*.25,
                        kind==Kind.WIDE_STAIRS?1.5:.5,(i+1)*(kind==Kind.HALF_STAIRS?.125:.25),-.25+i*.25));
            }
            case WIDE_FLOOR -> boxes.add(new AABB(-.5,0,-.5,1.5,.125,.5));
            case WIDE_CRAFTING_TABLE -> {
                boxes.add(new AABB(-.5,.75,-.5,1.5,1,.5));
                for(double x:new double[]{-.4375,1.3125})for(double z:new double[]{-.4375,.3125})
                    boxes.add(new AABB(x,0,z,x+.125,.75,z+.125));
            }
            case WATER_PIPE, FUEL_PIPE, POWER_CABLE -> boxes.add(new AABB(-.5, 0, -.5, .5, .125, .5));
            case WATER_TANK, FUEL_TANK, BIOREFINER, BIOFUEL_GENERATOR, ELECTRIC_PURIFIER -> boxes.add(new AABB(-.4375, 0, -.4375, .4375, .9375, .4375));
            case BATTERY_CHARGER -> boxes.add(new AABB(-.4375, 0, -.4375, .4375, .6875, .4375));
            case SPRINKLER -> {
                boxes.add(new AABB(-.3125, 0, -.3125, .3125, .125, .3125));
                boxes.add(new AABB(-.0625, .125, -.0625, .0625, .625, .0625));
                boxes.add(new AABB(-.3125, .5625, -.0625, .3125, .6875, .0625));
            }
            case WIND_STREAMER -> {
                boxes.add(new AABB(-.1875, 0, -.1875, .1875, .125, .1875));
                boxes.add(new AABB(-.03125, .125, -.03125, .03125, 2, .03125));
                for (int part = 0; part < 3; part++) {
                    double z = part * .25;
                    boxes.add(new AABB(-.03125, 1.5 + part * .025, z, .03125, 1.875 - part * .025, z + .25));
                }
            }
            case SAIL -> {
                boxes.add(new AABB(-.1875, 0, -.1875, .1875, .125, .1875));
                boxes.add(new AABB(-.0625, .125, -.0625, .0625, 3, .0625));
                boxes.add(new AABB(-.4375, 1.125, -.03125, .4375, 2.875, .03125));
                boxes.add(new AABB(-.5, 2.8125, -.0625, .5, 2.9375, .0625));
            }
            case STEERING_WHEEL -> {
                boxes.add(new AABB(-.3125, 0, -.3125, .3125, .125, .3125));
                boxes.add(new AABB(-.0625, .125, -.0625, .0625, .625, .0625));
                boxes.add(new AABB(-.3125, .4375, -.0625, .3125, .9375, .0625));
            }
            case ENGINE -> {
                boxes.add(new AABB(-.4375, 0, -.375, .4375, .625, .375));
                boxes.add(new AABB(-.125, .625, -.125, .125, .9375, .125));
                boxes.add(new AABB(-.3125, .125, -.5, .3125, .375, -.375));
            }
            case DISPOSABLE_ANCHOR, ANCHOR -> {
                boxes.add(new AABB(-.375, 0, -.375, .375, .125, .375));
                boxes.add(new AABB(-.25, .125, -.25, .25, .375, .25));
                boxes.add(new AABB(-.0625, .375, -.0625, .0625, .875, .0625));
                boxes.add(new AABB(-.375, .75, -.0625, .375, .875, .0625));
            }
            case BASIC_PURIFIER, PURIFIER, RAIN_COLLECTOR, COOKING_POT -> {
                boxes.add(new AABB(-.375, 0, -.375, .375, .125, .375));
                boxes.add(new AABB(-.375, .125, -.375, -.25, .625, .375));
                boxes.add(new AABB(.25, .125, -.375, .375, .625, .375));
                boxes.add(new AABB(-.25, .125, -.375, .25, .625, -.25));
                boxes.add(new AABB(-.25, .125, .25, .25, .625, .375));
            }
            case PLANTER, TREE_PLANTER -> {
                boxes.add(new AABB(-.5, 0, -.5, .5, .125, .5));
                boxes.add(new AABB(-.5, .125, -.5, -.375, .375, .5));
                boxes.add(new AABB(.375, .125, -.5, .5, .375, .5));
                boxes.add(new AABB(-.375, .125, -.5, .375, .375, -.375));
                boxes.add(new AABB(-.375, .125, .375, .375, .375, .5));
            }
            case DRYING_RACK -> boxes.add(new AABB(-.5,0,-.5,.5,.125,.5));
            case BASIC_GRILL, GRILL -> {
                boxes.add(new AABB(-.4375, .375, -.4375, .4375, .5, .4375));
                for (double x : new double[] {-.375, .25}) {
                    for (double z : new double[] {-.375, .25}) { boxes.add(new AABB(x, 0, z, x + .125, .375, z + .125)); }
                }
            }
            case SMELTER -> {
                boxes.add(new AABB(-.4375, 0, -.4375, .4375, .75, .4375));
                boxes.add(new AABB(-.1875, .75, -.1875, .1875, 1, .1875));
            }
            case BED -> {
                boxes.add(new AABB(-.4375, .125, -.4375, .4375, .375, .4375));
                for (double x : new double[] {-.375, .25}) {
                    for (double z : new double[] {-.375, .25}) { boxes.add(new AABB(x, 0, z, x + .125, .125, z + .125)); }
                }
                boxes.add(new AABB(-.375, .375, -.375, .375, .4375, -.125));
            }
            case COLLECTION_NET -> {
                boxes.add(new AABB(-.5, 0, -.5, .5, .125, -.375));
                boxes.add(new AABB(-.5, 0, .375, .5, .125, .5));
                boxes.add(new AABB(-.5, 0, -.375, -.375, .125, .375));
                boxes.add(new AABB(.375, 0, -.375, .5, .125, .375));
                boxes.add(new AABB(-.375, 0, -.03125, .375, .0625, .03125));
                boxes.add(new AABB(-.03125, 0, -.375, .03125, .0625, .375));
            }
            case FLOOR -> boxes.add(new AABB(-.5, 0, -.5, .5, .125, .5));
            case WALL -> boxes.add(new AABB(-.5, 0, -.5, .5, 1, -.375));
            case CORNER_WALL -> {
                boxes.add(new AABB(-.5, 0, -.5, .5, 1, -.375));
                boxes.add(new AABB(-.5, 0, -.375, -.375, 1, .5));
            }
            case CURVED_WALL -> {
                // Eight continuous steps follow a quarter-circle around the south-east corner.
                for (int segment = 0; segment < 8; segment++) {
                    double a = segment * Math.PI / 16, b = (segment + 1) * Math.PI / 16;
                    boxes.add(new AABB(.5 - Math.cos(a), 0, .5 - Math.sin(b),
                            .5 - .875 * Math.cos(b), 1, .5 - .875 * Math.sin(a)));
                }
            }
            case HALF_WALL -> boxes.add(new AABB(-.5, 0, -.5, .5, .5, -.375));
            case ANVIL -> {
                boxes.add(new AABB(-.375,0,-.4375,.375,.25,.4375));
                boxes.add(new AABB(-.1875,.25,-.25,.1875,.625,.25));
                boxes.add(new AABB(-.5,.625,-.3125,.5,1,.3125));
            }
            case ENCHANTING_TABLE -> boxes.add(new AABB(-.5,0,-.5,.5,.75,.5));
            case BOOKSHELF -> boxes.add(new AABB(-.5,0,-.5,.5,1,.5));
            case CHEST, ENDER_CHEST -> {
                boxes.add(new AABB(-.4375, 0, -.4375, .4375, .625, .4375));
                boxes.add(new AABB(-.4375, .625, -.4375, .4375, .875, .4375));
                boxes.add(new AABB(-.0625, .5, -.5, .0625, .75, -.4375));
            }
            case BARREL -> {
                boxes.add(new AABB(-.375, 0, -.375, .375, .125, .375));
                boxes.add(new AABB(-.4375, .125, -.4375, .4375, .875, .4375));
                boxes.add(new AABB(-.375, .875, -.375, .375, 1, .375));
            }
            case STONECUTTER -> {
                boxes.add(new AABB(-.5, 0, -.5, .5, .5, .5));
                boxes.add(new AABB(-.0625, .5, -.3125, .0625, .875, .3125));
            }
            case SMITHING_TABLE, CARTOGRAPHY_TABLE -> {
                boxes.add(new AABB(-.5, .75, -.5, .5, 1, .5));
                boxes.add(new AABB(-.4375, 0, -.4375, .4375, .75, .4375));
            }
            case GRINDSTONE -> {
                boxes.add(new AABB(-.375, 0, -.25, -.25, .625, .25));
                boxes.add(new AABB(.25, 0, -.25, .375, .625, .25));
                boxes.add(new AABB(-.25, .375, -.1875, .25, .875, .1875));
            }
            case LOOM -> {
                boxes.add(new AABB(-.4375, 0, -.375, -.3125, 1, .375));
                boxes.add(new AABB(.3125, 0, -.375, .4375, 1, .375));
                boxes.add(new AABB(-.4375, .875, -.375, .4375, 1, .375));
                boxes.add(new AABB(-.3125, .25, -.0625, .3125, .875, .0625));
            }
            case CRAFTING_TABLE, RESEARCH_TABLE -> {
                boxes.add(new AABB(-.5, .75, -.5, .5, 1, .5));
                for (double x : new double[] {-.4375, .3125}) {
                    for (double z : new double[] {-.4375, .3125}) {
                        boxes.add(new AABB(x, 0, z, x + .125, .75, z + .125));
                    }
                }
            }
            case PILLAR, SHORT_PILLAR -> {
                boxes.add(new AABB(-.125, 0, -.125, .125, kind.height, .125));
                boxes.add(new AABB(-.1875, 0, -.1875, .1875, .125, .1875));
                boxes.add(new AABB(-.1875, kind.height - .125, -.1875, .1875, kind.height, .1875));
            }
            case RAILING -> {
                boxes.add(new AABB(-.5, 0, -.5, -.375, height(), -.375));
                boxes.add(new AABB(.375, 0, -.5, .5, 1, -.375));
                boxes.add(new AABB(-.5, .875, -.5, .5, 1, -.375));
                boxes.add(new AABB(-.5, .375, -.5, .5, .5, -.375));
            }
            case DOOR -> boxes.add(open
                    ? new AABB(-.5, 0, -.5, -.375, 2, .5)
                    : new AABB(-.5, 0, -.5, .5, 2, -.375));
            case STAIRS -> {
                for (int step = 0; step < 4; step++) {
                    boxes.add(new AABB(-.5, 0, -.5 + step * .25, .5, (step + 1) * .25, -.25 + step * .25));
                }
            }
            case LADDER, TALL_LADDER -> {
                boxes.add(new AABB(-.375, 0, -.5, -.25, height(), -.375));
                boxes.add(new AABB(.25, 0, -.5, .375, height(), -.375));
                for (int rung = 0; rung < height()*4; rung++) {
                    boxes.add(new AABB(-.25, .125 + rung * .25, -.5, .25, .1875 + rung * .25, -.375));
                }
            }
            case ROOF -> {
                for (int strip = 0; strip < 4; strip++) {
                    boxes.add(new AABB(-.5, strip * .25, -.5 + strip * .25,
                            .5, strip * .25 + .125, -.25 + strip * .25));
                }
            }
        }
        double baseY = kind == Kind.COLLECTION_NET && pos.getY() == 0 ? .125 : RaftEntity.DECK_HEIGHT + pos.getY() - 1;
        return boxes.stream().map(this::rotate).map(box -> box.move(pos.getX(), baseY, pos.getZ())).toList();
    }

    public AABB climbBox(BlockPos pos) {
        return rotate(new AABB(-.45, 0, -.65, .45, height(), -.15))
                .move(pos.getX(), RaftEntity.DECK_HEIGHT + pos.getY() - 1, pos.getZ());
    }

    /** Top attachments clear all reserved cells; side attachments use the clicked level. */
    public BlockPos attachment(BlockPos pos, Direction face, boolean forceAbove, double localHitY) {
        if (forceAbove || face == Direction.UP) { return pos.above(height()); }
        if (face == Direction.DOWN) { return pos.below(); }
        double baseY = kind == Kind.COLLECTION_NET && pos.getY() == 0 ? .125 : RaftEntity.DECK_HEIGHT + pos.getY() - 1;
        int level = Math.max(0, Math.min(height() - 1, (int) Math.floor(localHitY - baseY)));
        return pos.above(level).relative(face);
    }

    public List<AABB> collisionBoxes(BlockPos pos) {
        if(kind==Kind.ADDON && !addonId.isEmpty()) {
            double y=RaftEntity.DECK_HEIGHT+pos.getY()-1;
            return addon().collision().stream().map(this::rotate).map(box->box.move(pos.getX(),y,pos.getZ())).toList();
        }
        List<AABB> geometry = boxes(pos);
        if(kind==Kind.WOVEN_RUG) { return List.of(); }
        if(kind==Kind.CANVAS_CANOPY) { return geometry; }
        if(kind.furniture()) {
            // Continuous furniture bodies avoid catching feet between decorative legs.
            AABB collider=geometry.getFirst();for(var box:geometry)collider=collider.minmax(box);
            return List.of(collider);
        }
        if (kind == Kind.COLLECTION_NET) {
            double y = geometry.getFirst().minY;
            return List.of(new AABB(pos.getX()-.5, y, pos.getZ()-.5, pos.getX()+.5, y+.125, pos.getZ()+.5));
        }
        if (kind == Kind.WIND_STREAMER || kind == Kind.SPRINKLER) { return geometry.subList(0, 2); }
        // Keep mast, wheel and chimney space separate from the wider supporting base.
        if(kind==Kind.SAIL || kind==Kind.STEERING_WHEEL || kind==Kind.ENGINE || kind.anchor()
                || kind==Kind.SMELTER || kind==Kind.ANVIL || kind==Kind.SPRINKLER){return geometry;}
        if (!kind.ladder() && kind != Kind.RAILING && !kind.station()) { return geometry; }
        // Vanilla-style continuous thin collider: descending cannot catch on each rung.
        AABB collider = geometry.getFirst();
        for (AABB box : geometry) { collider = collider.minmax(box); }
        return List.of(collider);
    }

    private AABB rotate(AABB box) {
        AABB result = box;
        for (int turn = 0; turn < rotation; turn++) {
            result = new AABB(-result.maxZ, result.minY, result.minX,
                    -result.minZ, result.maxY, result.maxX);
        }
        return result;
    }
}
