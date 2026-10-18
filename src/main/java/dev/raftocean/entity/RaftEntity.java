package dev.raftocean.entity;

import dev.raftocean.registry.ModContent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;

/** A server-controlled platform owning a connected layout of foundation cells. */
public final class RaftEntity extends Entity {
    public static final float DECK_WIDTH = 3.0F;
    public static final float DECK_HEIGHT = 0.25F;
    public static final double MAX_CURRENT = 0.05;
    private static final EntityDataAccessor<String> LAYOUT = SynchedEntityData.defineId(RaftEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> NAVIGATION = SynchedEntityData.defineId(RaftEntity.class, EntityDataSerializers.STRING);
    private final dev.raftocean.navigation.RaftNavigation navigation = new dev.raftocean.navigation.RaftNavigation(this);
    public dev.raftocean.navigation.RaftNavigation navigation() { return navigation; }
    public void publishNavigation(String value) { getEntityData().set(NAVIGATION, value); }
    private static final EntityDataAccessor<String> TREE_GROWTH = SynchedEntityData.defineId(RaftEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> MACHINES = SynchedEntityData.defineId(RaftEntity.class, EntityDataSerializers.STRING);
    private final dev.raftocean.survival.MachineAnimationState machines=new dev.raftocean.survival.MachineAnimationState(this);
    public dev.raftocean.survival.MachineAnimationState machines() {return machines;}
    public void publishMachines(String value) {getEntityData().set(MACHINES,value);}
    private final Map<BlockPos,Integer> treeStages = new LinkedHashMap<>();
    public int treeStage(BlockPos pos) {
        return treeStages.getOrDefault(pos,0);
    }
    public void syncTreeGrowth() {
        if (level().isClientSide()) { return; }
        getEntityData().set(TREE_GROWTH, pieces().entrySet().stream()
                .filter(e->e.getValue().kind()==RaftPiece.Kind.TREE_PLANTER).map(e->{
                    var inv=storage(e.getKey());
                    int stage=!inv.getItem(2).isEmpty()?4:inv.duration>0 && !inv.getItem(0).isEmpty()?1+Math.min(2,inv.progress*3/inv.duration):0;
                    BlockPos p=e.getKey();return p.getX()+","+p.getY()+","+p.getZ()+","+stage;
                }).collect(java.util.stream.Collectors.joining(";")));
    }
    private static final EntityDataAccessor<String> ADDON_MACHINES=SynchedEntityData.defineId(RaftEntity.class,EntityDataSerializers.STRING);
    private final dev.raftocean.api.internal.RaftMachineHost addonMachines=new dev.raftocean.api.internal.RaftMachineHost(this);
    public java.util.Optional<dev.raftocean.api.RaftMachineContext> machineContext(BlockPos pos) { return addonMachines.context(pos); }
    /** Resolve a machine owner from any reserved cell, including a wide piece's second cell. */
    public java.util.Optional<dev.raftocean.api.RaftMachineContext> machineContextAt(BlockPos cell) {
        return pieces().entrySet().stream().filter(entry->entry.getValue().occupiedCells(entry.getKey()).contains(cell))
                .findFirst().flatMap(entry->machineContext(entry.getKey()));
    }
    public dev.raftocean.api.RaftMachineView machineView(BlockPos pos) { return addonMachines.view(pos); }
    public void publishAddonMachines(String value) { getEntityData().set(ADDON_MACHINES,value); }
    private static final EntityDataAccessor<String> HEALTH = SynchedEntityData.defineId(RaftEntity.class, EntityDataSerializers.STRING);
    private final Map<BlockPos, Integer> foundationHealth = new LinkedHashMap<>();
    private final java.util.Set<BlockPos> reinforcedFoundations=new java.util.LinkedHashSet<>();
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private RaftLayout layout = RaftLayout.starter();
    private RaftStructure structure = new RaftStructure();
    private final Map<BlockPos, dev.raftocean.menu.RaftStorage> storage = new LinkedHashMap<>();
    private record StationUse(BlockPos pos,net.minecraft.world.inventory.AbstractContainerMenu menu) { }
    private final Map<java.util.UUID,StationUse> stationUsers=new LinkedHashMap<>();
    public void trackStationUse(Player user,BlockPos pos,net.minecraft.world.inventory.AbstractContainerMenu menu) {
        if(!level().isClientSide() && user.level()==level() && pieces().containsKey(pos)) {
            stationUsers.put(user.getUUID(),new StationUse(pos.immutable(),menu));
        }
    }
    public boolean stationInUse(BlockPos pos) {
        stationUsers.entrySet().removeIf(entry->{
            var user=level().getPlayerByUUID(entry.getKey());
            return user==null || user.containerMenu!=entry.getValue().menu || !entry.getValue().menu.stillValid(user);
        });
        return stationUsers.values().stream().anyMatch(use->use.pos.equals(pos));
    }
    private double currentX = 0.020;
    private double currentZ = 0.008;
    private long waveTicks;
    private Vec3 collisionOrigin;
    private float collisionYaw;
    private List<AABB> worldCollisionCache;
    private final Map<Float, List<AABB>> colliderCache = new LinkedHashMap<>(4, .75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Float, List<AABB>> entry) { return size() > 4; }
    };

    public Vec3 toWorld(Vec3 local) { return RaftTransform.world(local, position(), getYRot()); }
    public Vec3 toLocal(Vec3 world) { return RaftTransform.local(world, position(), getYRot()); }
    public List<AABB> pieceBoxes(BlockPos pos, RaftPiece piece) { return structure.boxes(pos, piece); }
    public List<AABB> pieceCollisionBoxes(BlockPos pos,RaftPiece piece) { return structure.collisionBoxes(pos,piece); }
    public List<AABB> collisionBoxesAt(Vec3 origin, float yaw) {
        List<AABB> rotatedColliders = colliderCache.get(yaw);
        if (rotatedColliders == null) {
            List<AABB> boxes = new ArrayList<>();
            boxes.addAll(layout.rectangles());
            structure.pieces().forEach((pos, piece) -> boxes.addAll(structure.collisionBoxes(pos, piece)));
            rotatedColliders = boxes.stream().flatMap(box -> RaftTransform.colliders(box, yaw).stream()).toList();
            colliderCache.put(yaw, rotatedColliders);
        }
        return rotatedColliders.stream().map(box -> box.move(origin)).toList();
    }
    public void refreshBounds() { setBoundingBox(makeBoundingBox(position())); }
    public boolean rotateTowardHeading(boolean afloat) {
        if (!afloat || navigation.anchored() || pieces().values().stream().noneMatch(p -> p.kind() == RaftPiece.Kind.STEERING_WHEEL)) { return false; }
        float old = getYRot();
        double delta = Math.clamp(RaftTransform.difference(navigation.heading(), old), -dev.raftocean.config.RaftConfig.TURN_SPEED.get(), dev.raftocean.config.RaftConfig.TURN_SPEED.get());
        if (Math.abs(delta) < 1.0E-5) { return false; }
        // Check the midpoint as well as the destination to prevent sweeping through terrain.
        for (double fraction : new double[]{.5, 1}) {
            for (AABB box : collisionBoxesAt(position(), (float)(old + delta * fraction))) {
                if (!hasLoadedCorners(box) || !level().getWorldBorder().isWithinBounds(box)
                        || !level().noCollision(this, box.deflate(1.0E-5))) { return false; }
            }
        }
        setYRot((float)dev.raftocean.navigation.RaftNavigation.normalize(old + delta));
        refreshBounds();
        return true;
    }

    public RaftEntity(EntityType<? extends RaftEntity> type, Level level) {
        super(type, level);
        rebuildLayout();
        if (!level.isClientSide()) {
            getEntityData().set(LAYOUT, encodedLayout());
        }
        blocksBuilding = true;
        setNoGravity(true);
    }

    public Map<BlockPos, BlockState> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }

    public double getDeckY() {
        return getY() + DECK_HEIGHT;
    }

    public int foundationHealth(BlockPos pos) { return foundationHealth.getOrDefault(pos,100); }
    public boolean reinforcedFoundation(BlockPos pos) { return reinforcedFoundations.contains(pos); }
    public boolean attackableFoundation(BlockPos pos) { return edgeFoundation(pos) && !(reinforcedFoundation(pos) && dev.raftocean.config.RaftConfig.REINFORCEMENT.get()); }
    public boolean reinforceFoundation(BlockPos pos,Player player,InteractionHand hand) {
        var held=player.getItemInHand(hand);
        if(isRemoved() || level().isClientSide() || !player.isAlive() || player.level()!=level()
                || player.position().distanceToSqr(toWorld(new Vec3(pos.getX(),DECK_HEIGHT,pos.getZ())))>64
                || !blocks.containsKey(pos) || reinforcedFoundation(pos)
                || !dev.raftocean.config.RaftConfig.REINFORCEMENT.get() || !held.is(dev.raftocean.survival.CraftingExpansion.item("reinforced_brace")))return false;
        reinforcedFoundations.add(pos.immutable());
        if(!player.getAbilities().instabuild){held.shrink(1);player.getInventory().setChanged();}
        publishHealth();return true;
    }
    public boolean edgeFoundation(BlockPos pos) {
        return blocks.containsKey(pos) && java.util.List.of(pos.north(),pos.south(),pos.east(),pos.west()).stream().anyMatch(p->!blocks.containsKey(p));
    }
    private void publishHealth() {
        getEntityData().set(HEALTH,layout.cells().stream().filter(pos->foundationHealth.containsKey(pos) || reinforcedFoundation(pos))
                .map(pos->pos.getX()+","+pos.getZ()+","+foundationHealth(pos)+","+(reinforcedFoundation(pos)?1:0)).collect(java.util.stream.Collectors.joining(";")));
        rebuildLayout();
    }
    public boolean repairFoundation(BlockPos pos, Player player) {
        if(isRemoved() || level().isClientSide() || !blocks.containsKey(pos) || foundationHealth(pos)==100) { return false; }
        if(!player.getAbilities().instabuild) {
            int supply=-1;
            for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
                if(player.getInventory().getItem(slot).is(net.minecraft.tags.ItemTags.PLANKS)){supply=slot;break;}
            }
            if(supply<0){return false;}
            player.getInventory().getItem(supply).shrink(1);player.getInventory().setChanged();
        }
        foundationHealth.put(pos,Math.min(100,foundationHealth(pos)+25));publishHealth();return true;
    }
    public boolean damageFoundation(BlockPos pos, int amount) {
        if(isRemoved() || !(level() instanceof ServerLevel server) || !blocks.containsKey(pos) || amount<=0 || !dev.raftocean.config.RaftConfig.RAFT_DAMAGE.get()
                || reinforcedFoundation(pos) && dev.raftocean.config.RaftConfig.REINFORCEMENT.get()) { return false; }
        int health=foundationHealth(pos)-Math.min(100,amount);
        if(health>0){foundationHealth.put(pos,health);publishHealth();return true;}
        var previous=new java.util.LinkedHashSet<>(layout.cells());
        RaftLayout remainder=layout.afterDamage(pos, structure.platforms());
        if(remainder==null) {
            for(var entry:new LinkedHashMap<>(structure.pieces()).entrySet()){dropDetachedPiece(server,entry.getKey(),entry.getValue());}
            foundationHealth.clear();discard();return true;
        }
        layout=remainder;
        for(BlockPos detached:previous) {
            if(!detached.equals(pos) && !layout.cells().contains(detached)) {
                var item=new net.minecraft.world.entity.item.ItemEntity(server,getX(),getDeckY(),getZ(),new ItemStack(ModContent.FOUNDATION.get()));
                item.setPos(toWorld(new Vec3(detached.getX(),DECK_HEIGHT,detached.getZ())));server.addFreshEntity(item);
            }
        }
        structure.pruneAfterDamage(layout).forEach((cell,piece)->dropDetachedPiece(server,cell,piece));
        foundationHealth.keySet().retainAll(layout.cells());reinforcedFoundations.retainAll(layout.cells());publishLayout();publishHealth();navigation.sync();return true;
    }
    private void dropDetachedPiece(ServerLevel server,BlockPos pos,RaftPiece piece) {
        addonMachines.detach(pos,piece);
        Vec3 world=toWorld(new Vec3(pos.getX(),DECK_HEIGHT+pos.getY()-1,pos.getZ()));
        var inventory=storage.remove(pos);
        boolean spent = piece.kind()==RaftPiece.Kind.DISPOSABLE_ANCHOR && inventory!=null && inventory.enabled;
        if(inventory!=null){net.minecraft.world.Containers.dropContents(server,BlockPos.containing(world),inventory);inventory.clearContent();}
        if(spent)return;
        var item=new net.minecraft.world.entity.item.ItemEntity(server,world.x,world.y,world.z,new ItemStack(ModContent.structuralItem(piece)));
        server.addFreshEntity(item);
    }

    public double getMass() {
        return blocks.size() * 12.0 + structure.mass();
    }

    public double getBuoyancy() {
        return blocks.size() * 40.0;
    }

    public List<AABB> getCollisionBoxes() {
        Vec3 origin=position();float yaw=getYRot();
        if(worldCollisionCache==null || !origin.equals(collisionOrigin) || Float.compare(yaw,collisionYaw)!=0) {
            worldCollisionCache=collisionBoxesAt(origin,yaw);
            collisionOrigin=origin;collisionYaw=yaw;
        }
        return worldCollisionCache;
    }

    public List<AABB> getLocalBoxes() {
        List<AABB> result = new ArrayList<>();
        for (BlockPos pos : blocks.keySet()) {
            result.add(cellBox(pos, Vec3.ZERO));
        }
        structure.pieces().forEach((pos, piece) -> result.addAll(structure.boxes(pos, piece)));
        return result;
    }

    public Map<AABB, BlockState> visualBoxes() {
        Map<AABB, BlockState> result = new LinkedHashMap<>();
        blocks.keySet().forEach(pos -> result.put(cellBox(pos, Vec3.ZERO), ModContent.RAFT_DECK.get().defaultBlockState()));
        structure.pieces().forEach((pos, piece) -> {
            BlockState visual = ModContent.stationVisual(piece.kind()).defaultBlockState();
            var boxes = structure.boxes(pos, piece);
            for (int i = 0; i < boxes.size(); i++) {
                if (piece.kind() != RaftPiece.Kind.SAIL || i != 2 || navigation.enabled(pos)) {
                    result.put(boxes.get(i), piece.kind() == RaftPiece.Kind.SAIL && i != 2 ? ModContent.RAFT_DECK.get().defaultBlockState() : visual);
                }
            }
        });
        return result;
    }

    public int getStructuralCount() { return structure.pieces().size(); }
    public Map<BlockPos, RaftPiece> pieces() { return structure.pieces(); }

    public Vec3 craftingTablePosition(BlockPos pos) {
        var piece=structure.pieces().get(pos);
        return piece!=null && piece.kind()==RaftPiece.Kind.WIDE_CRAFTING_TABLE?stationPosition(pos,piece.kind()):stationPosition(pos, RaftPiece.Kind.CRAFTING_TABLE);
    }

    public Vec3 stationPosition(BlockPos pos, RaftPiece.Kind kind) {
        RaftPiece piece = structure.pieces().get(pos);
        if (piece == null || piece.kind() != kind || !kind.station()) { return null; }
        return toWorld(structure.collisionBoxes(pos, piece).getFirst().getCenter());
    }

    public dev.raftocean.menu.RaftStorage storage(BlockPos pos) {
        RaftPiece piece = structure.pieces().get(pos);
        if (piece == null || !piece.kind().storage()) { return null; }
        return storage.computeIfAbsent(pos.immutable(), key -> new dev.raftocean.menu.RaftStorage(this, key, piece.kind()));
    }

    public boolean removeStructuralPiece(BlockPos pos) {
        Vec3 center = toWorld(new Vec3(pos.getX(), DECK_HEIGHT + pos.getY() - 1, pos.getZ()));
        var removedPiece=structure.pieces().get(pos);
        if (level().isClientSide() || !structure.remove(pos, layout)) { return false; }
        addonMachines.detach(pos,removedPiece);addonMachines.sync();
        var inventory = storage.remove(pos);
        if (inventory != null) {
            net.minecraft.world.Containers.dropContents(level(), BlockPos.containing(center), inventory);
            inventory.clearContent();
        }
        publishLayout();
        navigation.sync();
        return true;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!isRemoved() && !level().isClientSide() && reason.shouldDestroy()) {
            addonMachines.destroyed();
            storage.values().forEach(inventory -> {
                net.minecraft.world.Containers.dropContents(level(), this, inventory);
                inventory.clearContent();
            });
            storage.clear();
        }
        super.remove(reason);
    }

    public boolean isOnLadder(AABB body) {
        return structure.pieces().entrySet().stream().anyMatch(e -> e.getValue().kind().ladder()
                && RaftTransform.bounds(e.getValue().climbBox(e.getKey()), getYRot()).move(position()).intersects(body));
    }

    public Vec3 boardingPosition() {
        BlockPos cell = blocks.keySet().stream().min(java.util.Comparator.comparingInt(p ->
                p.getX() * p.getX() + p.getZ() * p.getZ())).orElse(BlockPos.ZERO);
        return toWorld(new Vec3(cell.getX(), DECK_HEIGHT + 0.01, cell.getZ()));
    }

    public Vec3 boardingPosition(Player player) {
        for (AABB box : getCollisionBoxes().stream().sorted(java.util.Comparator.comparingDouble(cell -> {
            double dx = cell.getCenter().x - getX();
            double dz = cell.getCenter().z - getZ();
            return dx * dx + dz * dz + .001 * (cell.maxY - getY());
        })).toList()) {
            Vec3 target = new Vec3(box.getCenter().x, box.maxY + .01, box.getCenter().z);
            if (level().noCollision(player, player.getBoundingBox().move(target.subtract(player.position())))) {
                return target;
            }
        }
        return null;
    }

    private AABB cellBox(BlockPos cell, Vec3 origin) {
        return new AABB(origin.x + cell.getX() - 0.5, origin.y, origin.z + cell.getZ() - 0.5,
                origin.x + cell.getX() + 0.5, origin.y + DECK_HEIGHT, origin.z + cell.getZ() + 0.5);
    }

    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        if (blocks == null || blocks.isEmpty()) {
            return super.makeBoundingBox(position);
        }
        AABB bounds = null;
        for (AABB local : getLocalBoxes()) {
            AABB box = RaftTransform.bounds(local, getYRot()).move(position);
            bounds = bounds == null ? box : bounds.minmax(box);
        }
        for(var entry:pieces().entrySet()) {
            if(!entry.getValue().kind().station()){continue;}
            BlockPos pos=entry.getKey();double y=pieceBoxes(pos,entry.getValue()).getFirst().minY;
            bounds=bounds.minmax(RaftTransform.bounds(new AABB(pos.getX()-.5,y,pos.getZ()-.5,pos.getX()+.5,y+1.8,pos.getZ()+.5),getYRot()).move(position));
        }
        if (treeStages != null) {
            for(var entry:treeStages.entrySet()) {
                RaftPiece piece=pieces().get(entry.getKey());
                if(entry.getValue()==0 || piece==null || piece.kind()!=RaftPiece.Kind.TREE_PLANTER){continue;}
                BlockPos pos=entry.getKey();double y=pieceBoxes(pos,piece).getFirst().minY+.375;
                AABB crown=new AABB(pos.getX()-.6,y,pos.getZ()-.6,pos.getX()+.6,y+3,pos.getZ()+.6);
                bounds=bounds.minmax(RaftTransform.bounds(crown,getYRot()).move(position));
            }
        }
        return bounds;
    }

    private long layoutRevision;
    public long layoutRevision() { return layoutRevision; }

    private void rebuildLayout() {
        layoutRevision++;
        colliderCache.clear();
        worldCollisionCache=null;
        blocks.clear();
        for (BlockPos pos : layout.cells()) {
            blocks.put(pos, foundationHealth(pos) < 60 ? dev.raftocean.creature.CreatureContent.DAMAGED_DECK.get().defaultBlockState() : ModContent.RAFT_DECK.get().defaultBlockState());
        }
        setBoundingBox(makeBoundingBox(position()));
    }

    private void publishLayout() {
        rebuildLayout();
        getEntityData().set(LAYOUT, encodedLayout());
    }

    private String encodedLayout() { return layout.encode() + "|" + structure.encode(); }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if(ADDON_MACHINES.equals(accessor) && addonMachines!=null)addonMachines.accept(getEntityData().get(ADDON_MACHINES));
        if (MACHINES.equals(accessor) && machines!=null) {machines.accept(getEntityData().get(MACHINES));}
        if (TREE_GROWTH.equals(accessor) && treeStages != null) {
            treeStages.clear();
            String encoded=getEntityData().get(TREE_GROWTH);
            if(encoded.length()<=24000 && !encoded.isEmpty()) {
                for(String entry:encoded.split(";")) {
                    String[] fields=entry.split(",");
                    if(fields.length!=4){continue;}
                    try {
                        treeStages.put(new BlockPos(Integer.parseInt(fields[0]),Integer.parseInt(fields[1]),Integer.parseInt(fields[2])),
                                Math.clamp(Integer.parseInt(fields[3]),0,4));
                    } catch(NumberFormatException ignored) { }
                    if(treeStages.size()>=RaftStructure.MAX_PIECES){break;}
                }
            }
            refreshBounds();
        }
        if (HEALTH.equals(accessor) && foundationHealth != null) {
            foundationHealth.clear();
            reinforcedFoundations.clear();
            String encoded=getEntityData().get(HEALTH);
            if(encoded.length()<=24000 && !encoded.isEmpty()) {
                try { for(String cell:encoded.split(";")) {
                    String[] values=cell.split(",");if(values.length!=3 && values.length!=4){continue;}
                    var pos=new BlockPos(Integer.parseInt(values[0]),0,Integer.parseInt(values[1]));
                    foundationHealth.put(pos,Math.clamp(Integer.parseInt(values[2]),1,100));
                    if(values.length==4 && values[3].equals("1"))reinforcedFoundations.add(pos);
                }} catch(NumberFormatException ignored) { foundationHealth.clear();reinforcedFoundations.clear(); }
            }
            rebuildLayout();
        }
        if (NAVIGATION.equals(accessor) && navigation != null) { navigation.acceptSnapshot(getEntityData().get(NAVIGATION)); }
        if (LAYOUT.equals(accessor) && blocks != null) {
            String[] parts = getEntityData().get(LAYOUT).split("\\|", -1);
            layout = RaftLayout.decode(parts[0], RaftStructure.platforms(parts.length > 1 ? parts[1] : ""));
            structure = RaftStructure.decode(parts.length > 1 ? parts[1] : "", layout);
            rebuildLayout();
        }
    }

    public double getCurrentX() {
        return currentX;
    }

    public double getCurrentZ() {
        return currentZ;
    }

    public void setCurrent(double x, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(z)) {
            return;
        }
        double length = Math.hypot(x, z);
        double scale = length > MAX_CURRENT ? MAX_CURRENT / length : 1.0;
        currentX = x * scale;
        currentZ = z * scale;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ADDON_MACHINES, "");
        builder.define(HEALTH, "");
        builder.define(TREE_GROWTH, "");
        builder.define(MACHINES, "");
        builder.define(LAYOUT, RaftLayout.starter().encode() + "|");
        builder.define(NAVIGATION, "0;300;1;0;0;0;");
    }

    @Override
    protected InterpolationHandler createInterpolationHandler() {
        return LinearInterpolationHandler.create(this, 3);
    }

    @Override
    public boolean canBeCollidedWith(Entity other) {
        return false; // Exact per-cell colliders are provided by RaftCollisionMixin.
    }

    @Override
    public boolean canCollideWith(Entity other) {
        // Players collide with our deck, but must not stop a raft moving beneath them.
        return false;
    }

    public record Pick(BlockPos cell, RaftPiece piece, AABB box, Vec3 hit) { }
    public record Placement(BlockPos cell, RaftPiece piece, List<AABB> boxes, String failure) {
        public boolean valid() { return failure.isEmpty(); }
    }
    public Pick pick(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(5.0));
        var obstruction = level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        if (obstruction.getType() != HitResult.Type.MISS) {
            end = obstruction.getLocation();
        }
        eye = toLocal(eye);
        end = toLocal(end);
        BlockPos selected = null;
        RaftPiece selectedPiece = null;
        AABB selectedBox = null;
        Vec3 hit = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos cell : blocks.keySet()) {
            var clipped = cellBox(cell, Vec3.ZERO).clip(eye, end);
            if (clipped.isPresent() && clipped.get().distanceToSqr(eye) < bestDistance) {
                hit = clipped.get();
                selected = cell;
                selectedBox = cellBox(cell, Vec3.ZERO);
                bestDistance = hit.distanceToSqr(eye);
            }
        }
        for (var entry : structure.pieces().entrySet()) {
            for (AABB localBox : entry.getValue().kind()==RaftPiece.Kind.COLLECTION_NET
                    ? structure.collisionBoxes(entry.getKey(),entry.getValue()) : structure.boxes(entry.getKey(), entry.getValue())) {
                AABB box = localBox;
                var clipped = box.clip(eye, end);
                if (clipped.isPresent() && clipped.get().distanceToSqr(eye) < bestDistance) {
                    hit = clipped.get();
                    selected = entry.getKey();
                    selectedPiece = entry.getValue();
                    selectedBox = box;
                    bestDistance = hit.distanceToSqr(eye);
                }
            }
        }
        return selected==null ? null : new Pick(selected,selectedPiece,selectedBox,hit);
    }
    private static boolean wallLike(RaftPiece.Kind kind) {
        return kind==RaftPiece.Kind.WINDOW_WALL || kind==RaftPiece.Kind.TRIANGLE_WALL || kind==RaftPiece.Kind.ARCHWAY || kind==RaftPiece.Kind.WALL || kind==RaftPiece.Kind.HALF_WALL || kind==RaftPiece.Kind.RAILING
                || kind==RaftPiece.Kind.DOOR || kind==RaftPiece.Kind.CORNER_WALL || kind==RaftPiece.Kind.CURVED_WALL;
    }
    private static int edgeRotation(BlockPos cell,Vec3 hit) {
        double dx=hit.x-cell.getX(),dz=hit.z-cell.getZ();
        return Math.abs(dx)>=Math.abs(dz)?(dx>=0?1:3):(dz>=0?2:0);
    }
    public Placement placement(Player player,ItemStack held,Pick pick) {
        return placement(player,held,pick,0);
    }
    public Placement placement(Player player,ItemStack held,Pick pick,int rotationOffset) {
        if(pick==null){return null;}
        boolean foundation=held.is(ModContent.FOUNDATION.get());
        var kind=ModContent.structuralKind(held);
        if(!foundation && kind==null){return null;}
        BlockPos selected=pick.cell();RaftPiece existing=pick.piece();Vec3 hit=pick.hit();
        var face=hitFace(pick.box(),hit);
        BlockPos snappingCell=selected;
        if(existing!=null && existing.width()*existing.depth()>1) {
            snappingCell=existing.occupiedCells(selected).stream().filter(cell->cell.getY()==selected.getY())
                    .min(java.util.Comparator.comparingDouble(cell->Math.pow(hit.x-cell.getX(),2)+Math.pow(hit.z-cell.getZ(),2))).orElse(selected);
        }
        int edge=edgeRotation(snappingCell,hit);
        int rotation=Math.floorMod(Mth.floor((player.getYRot()-getYRot()+45)/90),4);
        if(kind!=null && kind.rotatable()){rotation=Math.floorMod(rotation+rotationOffset,4);}
        BlockPos target=existing==null?selected.above():structure.attachment(snappingCell,existing,face,player.isShiftKeyDown(),hit.y);
        if(foundation || kind==RaftPiece.Kind.COLLECTION_NET && (existing==null || existing.kind()==RaftPiece.Kind.COLLECTION_NET && selected.getY()==0)) {
            if(existing!=null && !(existing.kind()==RaftPiece.Kind.COLLECTION_NET && selected.getY()==0)){
                return new Placement(selected,null,List.of(cellBox(selected,Vec3.ZERO)),"foundation");
            }
            target=selected.relative(switch(edge){case 0->net.minecraft.core.Direction.NORTH;case 1->net.minecraft.core.Direction.EAST;
                case 2->net.minecraft.core.Direction.SOUTH;default->net.minecraft.core.Direction.WEST;});
        }else if(kind!=null && kind.tableDecoration() && existing!=null && existing.kind().tableSurface()>=0 && face==net.minecraft.core.Direction.UP) {
            target=snappingCell.above(existing.height());
        }else if(wallLike(kind)){
            boolean deck=existing==null || existing.kind().floor() || existing.kind()==RaftPiece.Kind.COLLECTION_NET;
            if(deck){target=snappingCell.above();rotation=player.isShiftKeyDown()?rotation:edge;}
            else if(wallLike(existing.kind()) && !player.isShiftKeyDown()){
                rotation=existing.rotation();
                if(face==net.minecraft.core.Direction.UP){target=selected.above(existing.height());}
                else {
                    // Continue along the wall rather than jumping outward through its thin face.
                    boolean alongX=existing.rotation()%2==0;
                    target=alongX?selected.offset(hit.x>=selected.getX()?1:-1,0,0):selected.offset(0,0,hit.z>=selected.getZ()?1:-1);
                }
            }
        }
        RaftPiece piece=foundation?null:new RaftPiece(kind,rotation,false,kind==RaftPiece.Kind.ADDON?dev.raftocean.api.RaftBuildables.forItem(held).orElseThrow().id().toString():"");
        if(piece!=null && structure.isTableMount(target,piece))piece=piece.onTable();
        var occupied=structure.pieces().get(target);
        boolean cornerJoin=kind==RaftPiece.Kind.WALL && occupied!=null && occupied.kind()==RaftPiece.Kind.WALL
                && Math.floorMod(rotation-occupied.rotation(),2)==1;
        if(cornerJoin){
            int cornerRotation=rotation==Math.floorMod(occupied.rotation()+1,4)?rotation:occupied.rotation();
            piece=new RaftPiece(RaftPiece.Kind.CORNER_WALL,cornerRotation,false);
        }
        List<AABB> boxes=foundation?List.of(cellBox(target,Vec3.ZERO)):structure.boxes(target,piece);
        List<AABB> obstacles=new java.util.ArrayList<>();
        blocks.keySet().forEach(cell->obstacles.add(cellBox(cell,Vec3.ZERO)));
        for(var entry:structure.pieces().entrySet()){
            if(!cornerJoin || !entry.getKey().equals(target)){obstacles.addAll(structure.boxes(entry.getKey(),entry.getValue()));}
        }
        String failure="";
        if(foundation && layout.cells().size()>=dev.raftocean.config.RaftConfig.FOUNDATION_LIMIT.get()){failure="limit";}
        else if(!clearForPiece(boxes) || obstacles.stream().anyMatch(old->boxes.stream().anyMatch(box->old.intersects(box.deflate(1E-5))))){failure="blocked";}
        else if(foundation ? structure.pieces().containsKey(target) || !layout.canAdd(target,structure.platforms())
                : cornerJoin?!structure.canReplaceWallCorner(target,piece,layout):!structure.canAdd(target,piece,layout)){failure="connected";}
        return new Placement(target,piece,boxes,failure);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        ItemStack held = player.getItemInHand(hand);
        if(!dev.raftocean.crew.CrewAccess.allowed(player,this)) {
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.crew.denied"));
            return InteractionResult.FAIL;
        }
        if (held.is(ModContent.PADDLE.get())) { return ModContent.PADDLE.get().use(level(), player, hand); }
        boolean add = held.is(ModContent.FOUNDATION.get());
        RaftPiece.Kind buildKind = ModContent.structuralKind(held);
        boolean remove = held.is(ModContent.BUILDING_HAMMER.get()) && player.isShiftKeyDown();
        Pick pick=pick(player);
        BlockPos selected=pick==null?null:pick.cell();
        RaftPiece selectedPiece=pick==null?null:pick.piece();
        AABB selectedBox=pick==null?null:pick.box();
        Vec3 hit=pick==null?null:pick.hit();
        if (selected == null) {
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.build.aim"));
            return InteractionResult.FAIL;
        }
        if (level().isClientSide()) {
            return add || remove || held.is(ModContent.BUILDING_HAMMER.get()) || held.is(dev.raftocean.survival.CraftingExpansion.item("reinforced_brace")) || buildKind != null || (selectedPiece != null
                    && (selectedPiece.kind() == RaftPiece.Kind.DOOR || selectedPiece.kind()==RaftPiece.Kind.ADDON || selectedPiece.kind().station()))
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (remove) {
            boolean spentAnchor = selectedPiece!=null && selectedPiece.kind()==RaftPiece.Kind.DISPOSABLE_ANCHOR && storage(selected).enabled;
            RaftLayout candidate = RaftLayout.decode(layout.encode(), structure.platforms());
            boolean removed = selectedPiece != null ? removeStructuralPiece(selected)
                    : candidate.remove(selected, structure.platforms()) && structure.supportedBy(candidate) && structure.floatsOn(candidate);
            if (!removed) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.build.connected"));
                return InteractionResult.FAIL;
            }
            boolean armored=selectedPiece==null && reinforcedFoundation(selected);
            if (selectedPiece == null) { layout = candidate; foundationHealth.remove(selected);reinforcedFoundations.remove(selected); publishLayout(); publishHealth(); }
            if (!player.getAbilities().instabuild && !spentAnchor) {
                spawnAtLocation((ServerLevel) level(), new ItemStack(selectedPiece == null
                        ? ModContent.FOUNDATION.get() : ModContent.structuralItem(selectedPiece)));
                if(armored)spawnAtLocation((ServerLevel)level(),new ItemStack(dev.raftocean.survival.CraftingExpansion.item("reinforced_brace")));
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        if(selectedPiece==null && held.is(dev.raftocean.survival.CraftingExpansion.item("reinforced_brace"))) {
            boolean done=reinforceFoundation(selected,player,hand);
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(done?"raft_ocean.reinforce.done":"raft_ocean.reinforce.help"));
            return done?InteractionResult.SUCCESS_SERVER:InteractionResult.FAIL;
        }
        if(held.is(ModContent.BUILDING_HAMMER.get()) && selectedPiece==null) {
            boolean repaired=repairFoundation(selected,player);
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(repaired ? "raft_ocean.repair.done" : "raft_ocean.repair.help",foundationHealth(selected)));
            return repaired ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
        }
        if(held.is(ModContent.BUILDING_HAMMER.get()) && selectedPiece!=null && selectedPiece.kind().rotatable()) {
            int rotation=Math.floorMod(selectedPiece.rotation()+1,4);
            var rotated=selectedPiece.withRotation(rotation);
            var boxes=structure.boxes(selected,rotated);
            List<AABB> obstacles=new java.util.ArrayList<>();
            blocks.keySet().forEach(cell->obstacles.add(cellBox(cell,Vec3.ZERO)));
            structure.pieces().forEach((cell,piece)->{
                if(!cell.equals(selected)){obstacles.addAll(structure.boxes(cell,piece));}
            });
            boolean clear=clearForPiece(boxes)
                    && obstacles.stream().noneMatch(old->boxes.stream().anyMatch(box->old.intersects(box.deflate(1E-5))));
            if(!clear || !structure.rotate(selected,rotation,layout)) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.build.rotate_blocked"));
                return InteractionResult.FAIL;
            }
            publishLayout();
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.build.rotated",rotation*90));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!add && buildKind == null) {
            if(selectedPiece!=null && selectedPiece.kind()==RaftPiece.Kind.ADDON && !selectedPiece.addonId().isEmpty()) {
                var context=machineContext(selected);
                if(context.isPresent()) {
                    var collected=dev.raftocean.api.RaftMachineMenus.quickHarvest(context.get(),player,hand);
                    if(collected!=InteractionResult.PASS)return collected;
                }
                var result=selectedPiece.addon().interaction().use(this,selected,player,hand);
                if(result!=InteractionResult.PASS)return result;
                var machine=dev.raftocean.api.RaftBuildables.machine(selectedPiece.addonId());
                return machine.isPresent()?machine.get().use(machineContext(selected).orElseThrow(),player,hand):InteractionResult.PASS;
            }
            if (selectedPiece != null && selectedPiece.kind().station()) {
                BlockPos table = selected.immutable();
                RaftPiece.Kind kind = selectedPiece.kind();
                if((kind==RaftPiece.Kind.CHEST || kind==RaftPiece.Kind.BARREL) && player.isShiftKeyDown() && held.isEmpty() && dev.raftocean.config.RaftConfig.QUICK_STASH.get()) {
                    int moved=dev.raftocean.survival.QuickStash.store(this,table,player);
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.stash.result",moved));
                    return InteractionResult.SUCCESS_SERVER;
                }
                if(kind==RaftPiece.Kind.COLLECTION_NET && player.isShiftKeyDown() && held.isEmpty()) {
                    int collected=dev.raftocean.salvage.NetHarvest.collect(this,table,player);
                    int remaining=dev.raftocean.salvage.NetHarvest.remaining(this,table);
                    String key=collected>0?(remaining>0?"raft_ocean.net.partial":"raft_ocean.net.collected")
                            :remaining>0?"raft_ocean.net.full":"raft_ocean.net.empty";
                    player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable(key,collected,remaining));
                    return InteractionResult.SUCCESS_SERVER;
                }
                if (kind.bed()) { return dev.raftocean.survival.RaftBeds.use(this, table, player); }
                if (kind.machine()) {
                    var harvestResult = dev.raftocean.survival.MachineHarvest.use(this, table, kind, player, hand);
                    if (harvestResult != InteractionResult.PASS) { return harvestResult; }
                    var cupResult = dev.raftocean.survival.CupTransfers.use(this, table, kind, player, hand);
                    if (cupResult != InteractionResult.PASS) { return cupResult; }
                }
                player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (id, inventory, owner) -> {
                            var menu=dev.raftocean.menu.RaftStationMenus.create(id, inventory, this, table, kind);
                            trackStationUse(owner,table,menu);
                            return menu;
                        },
                        net.minecraft.network.chat.Component.translatable(kind == RaftPiece.Kind.CHEST && dev.raftocean.menu.RaftChests.pairs(this).containsKey(table) ? "raft_ocean.double_chest" : "item.raft_ocean.raft_" + kind.id())));
                return InteractionResult.SUCCESS_SERVER;
            }
            if (selectedPiece == null || selectedPiece.kind() != RaftPiece.Kind.DOOR) { return InteractionResult.PASS; }
            RaftPiece toggled = new RaftPiece(selectedPiece.kind(), selectedPiece.rotation(), !selectedPiece.open());
            if (!clearForPiece(structure.boxes(selected,toggled))) {
                player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.build.blocked"));
                return InteractionResult.FAIL;
            }
            structure.toggleDoor(selected);
            publishLayout();
            return InteractionResult.SUCCESS_SERVER;
        }
        Placement plan=placement(player,held,pick,dev.raftocean.network.RaftBuildRotationSync.rotation((net.minecraft.server.level.ServerPlayer)player));
        if(plan==null || !plan.valid()){
            String reason=plan==null?"aim":plan.failure();
            player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.build."+reason,
                    dev.raftocean.config.RaftConfig.FOUNDATION_LIMIT.get()));
            return InteractionResult.FAIL;
        }
        var occupied=structure.pieces().get(plan.cell());
        boolean placed=plan.piece()==null?layout.add(plan.cell(),structure.platforms())
                : occupied!=null && occupied.kind()==RaftPiece.Kind.WALL && plan.piece().kind()==RaftPiece.Kind.CORNER_WALL
                ? structure.replaceWallCorner(plan.cell(),plan.piece(),layout):structure.add(plan.cell(),plan.piece(),layout);
        if(!placed){return InteractionResult.FAIL;}
        publishLayout();
        if(!player.getAbilities().instabuild){held.shrink(1);}
        if(plan.piece()!=null && plan.piece().kind()==RaftPiece.Kind.ADDON)addonMachines.placed(plan.cell(),player);
        return InteractionResult.SUCCESS_SERVER;
    }

    private boolean clearForPiece(List<AABB> localBoxes) {
        for (AABB local : localBoxes) {
            for(AABB rotated:RaftTransform.colliders(local,getYRot())){
            AABB box = rotated.move(position()).deflate(1.0E-5);
            if (!level().hasChunkAt(BlockPos.containing(box.getCenter()))
                    || !level().getWorldBorder().isWithinBounds(box) || !level().noCollision(this, box)
                    || !level().getEntities(this, box, e -> e instanceof Player && !e.isSpectator()).isEmpty()) {
                return false;
            }
            }
        }
        return true;
    }

    private static net.minecraft.core.Direction hitFace(AABB box, Vec3 hit) {
        double[] gaps = { Math.abs(hit.y - box.maxY), Math.abs(hit.y - box.minY),
                Math.abs(hit.z - box.minZ), Math.abs(hit.z - box.maxZ),
                Math.abs(hit.x - box.minX), Math.abs(hit.x - box.maxX) };
        net.minecraft.core.Direction[] faces = { net.minecraft.core.Direction.UP, net.minecraft.core.Direction.DOWN,
                net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.SOUTH,
                net.minecraft.core.Direction.WEST, net.minecraft.core.Direction.EAST };
        int closest = 0;
        for (int i = 1; i < gaps.length; i++) { if (gaps[i] < gaps[closest]) { closest = i; } }
        return faces[closest];
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        // Structural damage and tools arrive in later milestones. /raft remove cleans up tests.
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            // Client carry is measured around commonTick by EntityTickEvent listeners.
            return;
        }

        waveTicks++;
        addonMachines.tick();
        collectPassingDebris();
        if (tickCount % 20 == 0) { dev.raftocean.survival.Automation.tick(this); dev.raftocean.survival.SurvivalMachines.tick(this); syncTreeGrowth(); machines.sync(); }
        double surface = sampleWaterSurface();
        Vec3 drive = navigation.target(Double.isFinite(surface));
        Vec3 velocity = getDeltaMovement();
        double vx;
        double vy;
        double vz;
        if (Double.isFinite(surface)) {
            double bob = Math.sin((waveTicks % 100000L) * 0.075) * 0.012;
            double targetBottom = surface - DECK_HEIGHT * getMass() / getBuoyancy() + bob;
            vy = Mth.clamp(velocity.y * 0.65 + (targetBottom - getY()) * 0.10, -0.08, 0.08);
            double acceleration = 0.08 * Math.sqrt(108.0 / getMass());
            vx = navigation.anchored() ? 0 : Mth.lerp(acceleration, velocity.x, drive.x);
            vz = navigation.anchored() ? 0 : Mth.lerp(acceleration, velocity.z, drive.z);
        } else {
            vx = velocity.x * 0.80;
            vz = velocity.z * 0.80;
            vy = Math.max(-0.30, velocity.y - 0.04);
        }

        Vec3 movement = new Vec3(vx, vy, vz);
        AABB destination = getBoundingBox().move(movement);
        if (!hasLoadedCorners(destination) || !level().getWorldBorder().isWithinBounds(destination)) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }

        // Clip each cell: an empty hole must not stop the whole raft against terrain.
        for (AABB cell : getCollisionBoxes()) {
            movement = Entity.collideBoundingBox(this, movement, cell, level(),
                    level().getEntityCollisions(this, cell.expandTowards(movement)));
        }
        setDeltaMovement(movement);
        Vec3 previousPosition = position();
        float previousYaw = getYRot();
        setPos(previousPosition.add(movement));
        rotateTowardHeading(Double.isFinite(surface));
        RaftPlayerSupport.onRaftMoved(this, position().subtract(previousPosition), previousYaw);
        resetFallDistance();
    }

    public void collectPassingDebris() {
        if (!(level() instanceof ServerLevel server) || tickCount % 10 != 0) { return; }
        for (var entry : structure.pieces().entrySet()) {
            if (entry.getValue().kind() != RaftPiece.Kind.COLLECTION_NET) { continue; }
            Vec3 center = stationPosition(entry.getKey(), RaftPiece.Kind.COLLECTION_NET);
            AABB area = new AABB(center.x - .9, center.y - .75, center.z - .9,
                    center.x + .9, center.y + .3, center.z + .9);
            for (var debris : server.getEntitiesOfClass(dev.raftocean.salvage.DebrisEntity.class, area,
                    entity -> !entity.isRemoved() && !entity.claimed())) { collectAtNet(entry.getKey(), debris); }
        }
    }

    public boolean collectAtNet(BlockPos pos, dev.raftocean.salvage.DebrisEntity debris) {
        if (level().isClientSide() || debris.level() != level() || debris.isRemoved() || debris.claimed()) { return false; }
        Vec3 center = stationPosition(pos, RaftPiece.Kind.COLLECTION_NET);
        if (center == null) { return false; }
        AABB area = new AABB(center.x - .9, center.y - .75, center.z - .9,
                center.x + .9, center.y + .3, center.z + .9);
        return area.intersects(debris.getBoundingBox()) && debris.collect(storage(pos));
    }

    private boolean hasLoadedCorners(AABB box) {
        for (double x : new double[]{box.minX, box.maxX}) {
            for (double z : new double[]{box.minZ, box.maxZ}) {
                if (!level().hasChunkAt(BlockPos.containing(x, getY(), z))) {
                    return false;
                }
            }
        }
        return true;
    }

    private double sampleWaterSurface() {
        int bottom = Mth.floor(getY()) - 2;
        int top = Mth.floor(getY()) + 2;
        double total = 0.0;
        int count = 0;
        for (BlockPos cell : blocks.keySet()) {
            BlockPos column = BlockPos.containing(toWorld(new Vec3(cell.getX(), 0, cell.getZ())));
            if (!level().hasChunkAt(column)) {
                continue;
            }
            for (int y = top; y >= bottom; y--) {
                BlockPos pos = new BlockPos(column.getX(), y, column.getZ());
                FluidState fluid = level().getFluidState(pos);
                if (fluid.is(FluidTags.WATER)) {
                    total += y + fluid.getHeight(level(), pos);
                    count++;
                    break;
                }
            }
        }
        return count == 0 ? Double.NaN : total / count;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("DeckVersion", 4);
        output.putFloat("HullYaw", getYRot());
        var health=output.childrenList("FoundationHealth");
        layout.cells().stream().filter(pos->foundationHealth.containsKey(pos) || reinforcedFoundation(pos)).forEach(pos->{var entry=health.addChild();entry.putInt("X",pos.getX());entry.putInt("Z",pos.getZ());entry.putInt("HP",foundationHealth(pos));entry.putBoolean("Armored",reinforcedFoundation(pos));});
        output.putString("Layout", layout.encode());
        output.putString("Structure", structure.encode());
        addonMachines.save(output);
        var inventories = output.childrenList("StationStorage");
        storage.forEach((pos, inventory) -> {
            if (structure.pieces().containsKey(pos) && structure.pieces().get(pos).kind().storage()) {
                var entry = inventories.addChild();
                entry.putInt("X", pos.getX()); entry.putInt("Y", pos.getY()); entry.putInt("Z", pos.getZ());
                net.minecraft.world.ContainerHelper.saveAllItems(entry, inventory.getItems());
                entry.putInt("Progress", inventory.progress);
                entry.putInt("Duration", inventory.duration);
                entry.putBoolean("Paused", inventory.paused);
                entry.putBoolean("CookingRequested", inventory.cookingRequested);
                entry.putInt("Fuel", inventory.fuelTicks);
                entry.putInt("FuelCapacity", inventory.fuelCapacity);
                entry.putInt("RawWater", inventory.rawWater);
                entry.putInt("CleanWater", inventory.cleanWater);
                entry.putInt("Energy", inventory.energy);
                entry.putBoolean("NavEnabled", inventory.enabled);
                entry.putInt("NavTrim", inventory.trim);
                entry.putFloat("FuelSpeed", inventory.fuelSpeed);
                entry.putString("Work", inventory.work);
            }
        });
        output.putDouble("CurrentX", currentX);
        output.putDouble("Heading", navigation.heading());
        output.putDouble("CurrentZ", currentZ);
        output.putLong("WaveTicks", waveTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        layout = RaftLayout.decode(input.getStringOr("Layout", RaftLayout.starter().encode()),
                RaftStructure.platforms(input.getStringOr("Structure", "")));
        structure = RaftStructure.decode(input.getStringOr("Structure", ""), layout);
        storage.clear();foundationHealth.clear();reinforcedFoundations.clear();
        for(var entry:input.childrenListOrEmpty("FoundationHealth")) {
            BlockPos pos=new BlockPos(entry.getIntOr("X",0),0,entry.getIntOr("Z",0));
            if(layout.cells().contains(pos)){foundationHealth.put(pos,Math.clamp(entry.getIntOr("HP",100),1,100));if(entry.getBooleanOr("Armored",false))reinforcedFoundations.add(pos);}
        }
        for (var entry : input.childrenListOrEmpty("StationStorage")) {
            BlockPos pos = new BlockPos(entry.getIntOr("X", 0), entry.getIntOr("Y", 0), entry.getIntOr("Z", 0));
            var inventory = storage(pos);
            if (inventory != null) {
                net.minecraft.world.ContainerHelper.loadAllItems(entry, inventory.getItems());
                inventory.progress = Math.clamp(entry.getIntOr("Progress", 0), 0, 200000);
                inventory.duration = Math.clamp(entry.getIntOr("Duration", 0), 0, 200000);
                inventory.paused = entry.getBooleanOr("Paused", false);
                inventory.cookingRequested = inventory.kind()==RaftPiece.Kind.COOKING_POT && entry.getBooleanOr("CookingRequested", inventory.progress>0);
                inventory.fuelTicks = Math.clamp(entry.getIntOr("Fuel", 0), 0, 200000);
                inventory.fuelCapacity = Math.clamp(entry.getIntOr("FuelCapacity", inventory.fuelTicks), 0, 200000);
                inventory.rawWater = Math.clamp(entry.getIntOr("RawWater", 0), 0, 4);
                inventory.cleanWater = Math.clamp(entry.getIntOr("CleanWater", 0), 0, dev.raftocean.survival.Automation.waterCapacity(inventory.kind()));
                inventory.energy = Math.clamp(entry.getIntOr("Energy", 0), 0, dev.raftocean.survival.Automation.ENERGY_CAPACITY);
                inventory.enabled = entry.getBooleanOr("NavEnabled", false);
                inventory.trim = Math.clamp(entry.getIntOr("NavTrim", 0), -180, 180);
                float speed = entry.getFloatOr("FuelSpeed", 1);
                inventory.fuelSpeed = Float.isFinite(speed) ? Math.clamp(speed, .01F, 100F) : 1;
                inventory.work = entry.getStringOr("Work", "");
            }
        }
        float hullYaw = input.getFloatOr("HullYaw", 0);
        setYRot((float)dev.raftocean.navigation.RaftNavigation.normalize(hullYaw));
        yRotO = getYRot();
        publishLayout();
        setCurrent(input.getDoubleOr("CurrentX", 0.020), input.getDoubleOr("CurrentZ", 0.008));
        waveTicks = Math.max(0, input.getLongOr("WaveTicks", 0));
        navigation.setHeading(input.getDoubleOr("Heading", 0));
        navigation.sync();publishHealth();
        addonMachines.load(input);
    }
}
