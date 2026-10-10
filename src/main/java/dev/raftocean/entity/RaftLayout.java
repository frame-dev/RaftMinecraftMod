package dev.raftocean.entity;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;

/** Bounded, connected flat foundation layout. Coordinates are relative to the raft origin. */
public final class RaftLayout {
    public static final int MAX_CELLS = 1024;
    public static final int MAX_RADIUS = 32;
    private final Set<BlockPos> cells;

    private RaftLayout(Set<BlockPos> cells) {
        this.cells = new LinkedHashSet<>(cells);
    }

    public static RaftLayout starter() {
        Set<BlockPos> cells = new LinkedHashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                cells.add(new BlockPos(x, 0, z));
            }
        }
        return new RaftLayout(cells);
    }

    public Set<BlockPos> cells() {
        return Collections.unmodifiableSet(cells);
    }

    /** Merge filled foundation runs before rotation; holes and deck edges remain exact. */
    public java.util.List<net.minecraft.world.phys.AABB> rectangles() {
        java.util.Map<String, net.minecraft.world.phys.AABB> active = new java.util.HashMap<>();
        java.util.List<net.minecraft.world.phys.AABB> result = new java.util.ArrayList<>();
        var rows = new java.util.TreeMap<Integer, java.util.TreeSet<Integer>>();
        for (var cell : cells) { rows.computeIfAbsent(cell.getZ(), key -> new java.util.TreeSet<>()).add(cell.getX()); }
        for (var row : rows.entrySet()) {
            var xs = new java.util.ArrayList<>(row.getValue());
            for (int i=0; i<xs.size(); i++) {
                int start=xs.get(i), end=start;
                while (i+1<xs.size() && xs.get(i+1)==end+1) { end=xs.get(++i); }
                String key=start+","+end;
                var previous=active.get(key);
                double minZ=row.getKey()-.5, maxZ=row.getKey()+.5;
                if (previous!=null && previous.maxZ==minZ) {
                    active.put(key,new net.minecraft.world.phys.AABB(previous.minX,0,previous.minZ,previous.maxX,.25,maxZ));
                } else {
                    if (previous!=null) { result.add(previous); }
                    active.put(key,new net.minecraft.world.phys.AABB(start-.5,0,minZ,end+.5,.25,maxZ));
                }
            }
        }
        result.addAll(active.values());
        return result;
    }

    public boolean add(BlockPos pos) {
        return add(pos, Set.of());
    }

    public boolean add(BlockPos pos, Set<BlockPos> nets) {
        if (cells.size() >= MAX_CELLS || !inBounds(pos) || cells.contains(pos)
                || nets.contains(pos) || neighbors(pos).stream().noneMatch(p -> cells.contains(p) || nets.contains(p))) {
            return false;
        }
        return cells.add(pos.immutable());
    }
    public boolean canAdd(BlockPos pos, Set<BlockPos> nets) {
        return new RaftLayout(cells).add(pos,nets);
    }

    public boolean remove(BlockPos pos) {
        return remove(pos, Set.of());
    }

    public boolean remove(BlockPos pos, Set<BlockPos> nets) {
        if (!cells.contains(pos) || cells.size() <= 1) {
            return false;
        }
        Set<BlockPos> remaining = new LinkedHashSet<>(cells);
        remaining.remove(pos);
        remaining.addAll(nets);
        if (!connected(remaining)) {
            return false;
        }
        return cells.remove(pos);
    }

    /** Keep the largest connected remnant when a bite breaks a bridge; detached floats become drops. */
    public RaftLayout afterDamage(BlockPos broken) {
        return afterDamage(broken, Set.of());
    }

    public RaftLayout afterDamage(BlockPos broken, Set<BlockPos> nets) {
        Set<BlockPos> remaining = new LinkedHashSet<>(cells);remaining.addAll(nets);remaining.remove(broken);
        if (remaining.isEmpty()) { return null; }
        Set<BlockPos> largest = Set.of();
        while (!remaining.isEmpty()) {
            Set<BlockPos> component = new LinkedHashSet<>();
            java.util.ArrayDeque<BlockPos> queue = new java.util.ArrayDeque<>();queue.add(remaining.iterator().next());
            while (!queue.isEmpty()) {
                BlockPos pos=queue.removeFirst();
                if (!remaining.remove(pos)) { continue; }
                component.add(pos);neighbors(pos).stream().filter(remaining::contains).forEach(queue::add);
            }
            component.retainAll(cells);
            if (component.size()>largest.size()) { largest=component; }
        }
        return largest.isEmpty() ? null : new RaftLayout(largest);
    }

    public String encode() {
        return cells.stream().sorted((a, b) -> a.getX() == b.getX()
                        ? Integer.compare(a.getZ(), b.getZ()) : Integer.compare(a.getX(), b.getX()))
                .map(pos -> pos.getX() + "," + pos.getZ()).collect(Collectors.joining(";"));
    }

    public static RaftLayout decode(String encoded) {
        return decode(encoded, Set.of());
    }

    public static RaftLayout decode(String encoded, Set<BlockPos> nets) {
        if (encoded.length() > 10240 || encoded.isBlank()) {
            throw new IllegalArgumentException("Invalid raft layout length");
        }
        Set<BlockPos> cells = new LinkedHashSet<>();
        for (String cell : encoded.split(";", -1)) {
            String[] coordinates = cell.split(",", -1);
            if (coordinates.length != 2) {
                throw new IllegalArgumentException("Invalid raft coordinate");
            }
            BlockPos pos = new BlockPos(Integer.parseInt(coordinates[0]), 0, Integer.parseInt(coordinates[1]));
            if (!inBounds(pos) || !cells.add(pos) || cells.size() > MAX_CELLS) {
                throw new IllegalArgumentException("Invalid raft cell");
            }
        }
        Set<BlockPos> platforms = new LinkedHashSet<>(cells);platforms.addAll(nets);
        if (!connected(platforms)) {
            throw new IllegalArgumentException("Disconnected raft layout");
        }
        return new RaftLayout(cells);
    }

    public boolean connectedThrough(Set<BlockPos> nets) {
        Set<BlockPos> platforms = new LinkedHashSet<>(cells); platforms.addAll(nets);
        return connected(platforms);
    }

    private static boolean inBounds(BlockPos pos) {
        return pos.getY() == 0 && Math.abs((long) pos.getX()) <= MAX_RADIUS
                && Math.abs((long) pos.getZ()) <= MAX_RADIUS;
    }

    private static Set<BlockPos> neighbors(BlockPos pos) {
        return Set.of(pos.north(), pos.south(), pos.east(), pos.west());
    }

    private static boolean connected(Set<BlockPos> cells) {
        if (cells.isEmpty()) {
            return false;
        }
        Set<BlockPos> visited = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(cells.iterator().next());
        while (!queue.isEmpty()) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }
            neighbors(pos).stream().filter(cells::contains).filter(p -> !visited.contains(p)).forEach(queue::addLast);
        }
        return visited.size() == cells.size();
    }
}
