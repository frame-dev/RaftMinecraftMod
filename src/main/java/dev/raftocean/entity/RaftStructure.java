package dev.raftocean.entity;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/** Bounded structural graph rooted in foundations; tall pieces reserve every occupied level. */
public final class RaftStructure {
    public static final int MAX_PIECES = 512;
    public static final int MAX_HEIGHT = 16;
    private final Map<BlockPos, RaftPiece> pieces = new LinkedHashMap<>();

    public Map<BlockPos, RaftPiece> pieces() { return Collections.unmodifiableMap(pieces); }
    public double mass() { return pieces.values().stream().mapToDouble(p -> p.mass()).sum(); }
    public Set<BlockPos> platforms() {
        return pieces.entrySet().stream().filter(e -> e.getKey().getY()==0 && e.getValue().kind()==RaftPiece.Kind.COLLECTION_NET)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    public static Set<BlockPos> platforms(String encoded) {
        if(encoded.length()>24000){throw new IllegalArgumentException("Structure too large");}
        Set<BlockPos> result=new LinkedHashSet<>();
        if(!encoded.isEmpty()){for(String entry:encoded.split(";",-1)){
            String[] fields=entry.split(",",-1);
            if(fields.length!=6 && fields.length!=7){throw new IllegalArgumentException("Invalid structural record");}
            if(fields[1].equals("0") && fields[3].equals("COLLECTION_NET")){
                result.add(new BlockPos(Integer.parseInt(fields[0]),0,Integer.parseInt(fields[2])));
            }
        }}
        return result;
    }

    public List<AABB> boxes(BlockPos pos, RaftPiece piece) {
        double offset = mountingOffset(pos, piece);
        return piece.boxes(pos).stream().map(box -> box.move(0, offset, 0)).toList();
    }

    public List<AABB> collisionBoxes(BlockPos pos, RaftPiece piece) {
        double offset = mountingOffset(pos, piece);
        return piece.collisionBoxes(pos).stream().map(box -> box.move(0, offset, 0)).toList();
    }

    private double mountingOffset(BlockPos pos, RaftPiece piece) {
        RaftPiece below = pieceAt(pos.below());
        return floorMounted(piece) && below != null && below.kind().floor() ? -.875 : 0;
    }

    public BlockPos attachment(BlockPos pos, RaftPiece piece, net.minecraft.core.Direction face,
            boolean forceAbove, double localHitY) {
        return piece.attachment(pos,face,forceAbove,localHitY-mountingOffset(pos,piece));
    }

    public RaftPiece pieceAt(BlockPos pos) { return pieces.entrySet().stream().filter(e->e.getValue().occupiedCells(e.getKey()).contains(pos)).map(Map.Entry::getValue).findFirst().orElse(null); }

    private static boolean floorMounted(RaftPiece piece) {
        if(piece.kind()==RaftPiece.Kind.ADDON && !piece.addonId().isEmpty())return piece.addon().floorMounted();
        return piece.kind() == RaftPiece.Kind.WINDOW_WALL || piece.kind()==RaftPiece.Kind.TRIANGLE_WALL
                || piece.kind()==RaftPiece.Kind.ARCHWAY || piece.kind() == RaftPiece.Kind.WALL || piece.kind() == RaftPiece.Kind.CORNER_WALL
                || piece.kind() == RaftPiece.Kind.CURVED_WALL || piece.kind() == RaftPiece.Kind.DOOR
                || piece.kind() == RaftPiece.Kind.RAILING || piece.kind() == RaftPiece.Kind.HALF_WALL
                || piece.kind().conduit() || piece.kind().station() || piece.kind() == RaftPiece.Kind.WIND_STREAMER || piece.kind()==RaftPiece.Kind.BOOKSHELF;
    }

    public boolean add(BlockPos pos, RaftPiece piece, RaftLayout layout) {
        int limit=dev.raftocean.config.RaftConfig.SPEC.isLoaded()?dev.raftocean.config.RaftConfig.PIECE_LIMIT.get():MAX_PIECES;
        if (pieces.size() >= limit || !inBounds(pos, piece) || layout.cells().contains(pos)) { return false; }
        Set<BlockPos> occupied = occupied();
        for(var cell:piece.occupiedCells(pos))if(occupied.contains(cell) || layout.cells().contains(cell))return false;
        if(piece.width()*piece.depth()>1 && floorMounted(piece)) {
            for(var cell:piece.occupiedCells(pos))if(cell.getY()==pos.getY()
                    && !occupied.contains(cell.below()) && !layout.cells().contains(cell.below()))return false;
        }
        pieces.put(pos.immutable(), piece);
        if (!supportedBy(layout) || !floatsOn(layout)) {
            pieces.remove(pos);
            return false;
        }
        return true;
    }
    public boolean canAdd(BlockPos pos, RaftPiece piece, RaftLayout layout) {
        RaftStructure candidate=new RaftStructure();candidate.pieces.putAll(pieces);
        return candidate.add(pos,piece,layout);
    }

    /** Two perpendicular full wall faces share one saved corner piece and one support cell. */
    public boolean replaceWallCorner(BlockPos pos,RaftPiece corner,RaftLayout layout){
        var old=pieces.get(pos);
        if(old==null || old.kind()!=RaftPiece.Kind.WALL || corner.kind()!=RaftPiece.Kind.CORNER_WALL
                || old.rotation()!=corner.rotation() && old.rotation()!=Math.floorMod(corner.rotation()+3,4)){return false;}
        pieces.remove(pos);
        if(add(pos,corner,layout)){return true;}
        pieces.put(pos,old);return false;
    }
    public boolean canReplaceWallCorner(BlockPos pos,RaftPiece corner,RaftLayout layout){
        var candidate=new RaftStructure();candidate.pieces.putAll(pieces);
        return candidate.replaceWallCorner(pos,corner,layout);
    }

    public boolean remove(BlockPos pos, RaftLayout layout) {
        RaftPiece support=pieces.get(pos);
        if(support!=null && support.kind().floor()) {
            for(var cell:support.occupiedCells(pos)) {
                RaftPiece mounted=pieceAt(cell.above());
                if(mounted!=null && floorMounted(mounted))return false;
            }
        }
        RaftPiece old = pieces.remove(pos);
        if (old == null) { return false; }
        if (!supportedBy(layout)) {
            pieces.put(pos, old);
            return false;
        }
        return true;
    }

    /** Damage prunes disconnected structures and excess weight so every resulting save remains valid. */
    public Map<BlockPos, RaftPiece> pruneAfterDamage(RaftLayout layout) {
        Map<BlockPos, RaftPiece> removed = new LinkedHashMap<>();
        boolean changed;
        do {
            changed=false;
            Set<BlockPos> all=occupied();all.addAll(layout.cells());
            Set<BlockPos> visited=new LinkedHashSet<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>(layout.cells());
            while(!queue.isEmpty()) {
                BlockPos pos=queue.removeFirst();if(!visited.add(pos)){continue;}
                neighbors(pos).stream().filter(all::contains).filter(p->!visited.contains(p)).forEach(queue::addLast);
            }
            for(var entry:new LinkedHashMap<>(pieces).entrySet()) {
                if(!visited.contains(entry.getKey()) || entry.getValue().width()*entry.getValue().depth()>1 && floorMounted(entry.getValue())
                        && entry.getValue().occupiedCells(entry.getKey()).stream().filter(cell->cell.getY()==entry.getKey().getY()).anyMatch(cell->!all.contains(cell.below()))) { removed.put(entry.getKey(),pieces.remove(entry.getKey()));changed=true; }
            }
            if(!floatsOn(layout) && !pieces.isEmpty()) {
                BlockPos heavy=pieces.entrySet().stream().max(java.util.Comparator.comparingDouble(e->e.getValue().mass())).orElseThrow().getKey();
                removed.put(heavy,pieces.remove(heavy));changed=true;
            }
        } while(changed);
        return removed;
    }

    public void toggleDoor(BlockPos pos) {
        RaftPiece old = pieces.get(pos);
        if (old != null && old.kind() == RaftPiece.Kind.DOOR) {
            pieces.put(pos, new RaftPiece(old.kind(), old.rotation(), !old.open()));
        }
    }

    public boolean floatsOn(RaftLayout layout) {
        return layout.cells().size() * 12 + mass() <= layout.cells().size() * 40 * .9;
    }

    public boolean supportedBy(RaftLayout layout) {
        if (!layout.connectedThrough(platforms())) { return false; }
        Set<BlockPos> occupied = occupied();
        for(var entry:pieces.entrySet()) {
            var piece=entry.getValue();
            if(piece.width()*piece.depth()>1 && floorMounted(piece)) {
                for(var cell:piece.occupiedCells(entry.getKey()))if(cell.getY()==entry.getKey().getY()
                        && !occupied.contains(cell.below()) && !layout.cells().contains(cell.below()))return false;
            }
        }
        occupied.addAll(layout.cells());
        Set<BlockPos> visited = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(layout.cells());
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) { continue; }
            for (BlockPos neighbor : neighbors(pos)) {
                if (occupied.contains(neighbor) && !visited.contains(neighbor)) { queue.addLast(neighbor); }
            }
        }
        return visited.size() == occupied.size();
    }

    private Set<BlockPos> occupied() {
        Set<BlockPos> cells = new LinkedHashSet<>();
        pieces.forEach((pos, piece) -> {
            cells.addAll(piece.occupiedCells(pos));
        });
        return cells;
    }

    public String encode() {
        return pieces.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey().getX() + "," + e.getKey().getY() + "," + e.getKey().getZ()
                        + "," + e.getValue().kind().name() + "," + e.getValue().rotation()
                        + "," + (e.getValue().open() ? 1 : 0) + (e.getValue().addonId().isEmpty()?"":","+e.getValue().addonId())).collect(Collectors.joining(";"));
    }

    public static RaftStructure decode(String encoded, RaftLayout layout) {
        RaftStructure result = new RaftStructure();
        if (encoded.isEmpty()) { return result; }
        if (encoded.length() > 24000) { throw new IllegalArgumentException("Structure too large"); }
        for (String entry : encoded.split(";", -1)) {
            String[] fields = entry.split(",", -1);
            if ((fields.length != 6 && fields.length!=7) || !(fields[5].equals("0") || fields[5].equals("1"))) {
                throw new IllegalArgumentException("Invalid structural record");
            }
            BlockPos pos = new BlockPos(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
            RaftPiece piece = new RaftPiece(RaftPiece.Kind.valueOf(fields[3]), Integer.parseInt(fields[4]), fields[5].equals("1"),fields.length==7?fields[6]:"");
            if (!inBounds(pos, piece) || result.pieces.size() >= MAX_PIECES || result.pieces.putIfAbsent(pos, piece) != null) {
                throw new IllegalArgumentException("Invalid structural cell");
            }
        }
        int reserved = result.pieces.values().stream().mapToInt(p -> p.width()*p.depth()*p.height()).sum();
        if (result.occupied().stream().anyMatch(layout.cells()::contains) || result.occupied().size() != reserved || !result.supportedBy(layout) || !result.floatsOn(layout)) {
            throw new IllegalArgumentException("Unsupported, overlapping, or overloaded structure");
        }
        return result;
    }

    private static boolean inBounds(BlockPos pos, RaftPiece piece) {
        return (pos.getY()>=1 || pos.getY()==0 && piece.kind()==RaftPiece.Kind.COLLECTION_NET)
                && piece.occupiedCells(pos).stream().allMatch(cell->Math.abs((long)cell.getX())<=RaftLayout.MAX_RADIUS
                        && Math.abs((long)cell.getZ())<=RaftLayout.MAX_RADIUS && cell.getY()<=MAX_HEIGHT);
    }

    private static Set<BlockPos> neighbors(BlockPos pos) {
        return Set.of(pos.north(), pos.south(), pos.east(), pos.west(), pos.above(), pos.below());
    }
}
