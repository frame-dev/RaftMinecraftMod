package dev.raftocean.survival;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.RaftStorage;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;

/** One-second loaded-raft processing; inputs and outputs commit as a single transaction. */
public final class SurvivalMachines {
    private SurvivalMachines() { }
    public static void tick(RaftEntity raft) {
        if (!(raft.level() instanceof ServerLevel level)) { return; }
        for (var entry : raft.pieces().entrySet()) {
            var kind = entry.getValue().kind();
            if (kind.machine() && (!kind.automation() || kind == RaftPiece.Kind.BIOREFINER)) {
                process(raft, entry.getKey(), kind, level);
            }
        }
    }
    public static void process(RaftEntity raft, BlockPos pos, RaftPiece.Kind kind, ServerLevel level) {
        RaftStorage inventory = raft.storage(pos);
        if (inventory == null) { return; }
        if (inventory.paused) { inventory.status="off"; return; }
        if(kind==RaftPiece.Kind.COOKING_POT && dev.raftocean.config.RaftConfig.MANUAL_COOKING.get() && !inventory.cookingRequested){
            if(!Set.of("recipe","cup","water","output","cook_changed").contains(inventory.status))
                inventory.status=inventory.getItem(4).isEmpty()?"cook_ready":"cook_done";return;
        }
        inventory.status = "supplies";
        acceptCup(inventory, kind);
        if (kind.purifier() || kind == RaftPiece.Kind.RAIN_COLLECTOR) {
            processWater(raft, pos, kind, level, inventory);
            return;
        }
        int[] inputs = switch (kind) {
            case BIOREFINER -> new int[] {0, 1};
            case COOKING_POT -> new int[] {0, 1, 2, 6};
            case PLANTER, TREE_PLANTER -> new int[] {0, 1, 4};
            default -> new int[] {0};
        };
        int[] outputs = switch (kind) {
            case COOKING_POT -> new int[] {4};
            case PLANTER, TREE_PLANTER -> new int[] {2, 3};
            default -> new int[] {2};
        };
        List<ItemStack> results = new ArrayList<>();
        String key;
        int duration;
        if (kind.grill() || kind == RaftPiece.Kind.SMELTER) {
            var input = new SingleRecipeInput(inventory.getItem(0));
            RecipeHolder<? extends AbstractCookingRecipe> holder = kind.grill()
                    ? level.recipeAccess().getRecipeFor(RecipeType.CAMPFIRE_COOKING, input, level).orElse(null)
                    : level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level).orElse(null);
            if (holder == null) { rejectRecipe(inventory, !inventory.getItem(0).isEmpty()); return; }
            results.add(holder.value().assemble(input));
            duration = holder.value().cookingTime(); key = holder.id().identifier().toString();
        } else {
            int waterSlot = kind == RaftPiece.Kind.COOKING_POT ? 2 : 4;
            if (kind != RaftPiece.Kind.BIOREFINER && kind != RaftPiece.Kind.DRYING_RACK && inventory.cleanWater == 0) { if (!inventory.status.equals("cup")) { inventory.status = "water"; } return; }
            var input = CraftingInput.of(inputs.length, 1, Arrays.stream(inputs).mapToObj(slot -> slot == waterSlot
                    ? new ItemStack(SurvivalContent.FRESH_WATER.get()) : inventory.getItem(slot)).toList());
            var holder = level.recipeAccess().getRecipeFor(ProcessingRecipe.type(kind), input, level).orElse(null);
            if (holder == null) {
                rejectRecipe(inventory, Arrays.stream(inputs).allMatch(slot -> slot == waterSlot || !inventory.getItem(slot).isEmpty()));
                return;
            }
            results.add(holder.value().assemble(input));
            holder.value().byproducts().forEach(stack -> {
                ItemStack product = stack.create();
                if (!product.is(SurvivalContent.EMPTY_CUP.get())) { results.add(product); }
            });
            if (results.size() > outputs.length) { rejectRecipe(inventory, true); return; }
            duration = holder.value().duration(); key = holder.id().identifier().toString();
        }
        if(kind==RaftPiece.Kind.COOKING_POT && dev.raftocean.config.RaftConfig.MANUAL_COOKING.get()
                && !inventory.work.isEmpty() && !key.equals(inventory.work)){
            inventory.cookingRequested=false;reset(inventory);inventory.status="cook_changed";inventory.setChanged();return;
        }
        inventory.duration = duration;
        if (!key.equals(inventory.work)) { inventory.work = key; inventory.progress = 0; }
        for (int i = 0; i < results.size(); i++) {
            if (!accepts(inventory.getItem(outputs[i]), results.get(i))) { inventory.status = "output"; return; }
        }
        if (kind == RaftPiece.Kind.RAIN_COLLECTOR && !rainReaches(raft, pos, level)) { inventory.status = "rain"; return; }
        if ((kind == RaftPiece.Kind.PLANTER || kind == RaftPiece.Kind.TREE_PLANTER) && !lightReaches(raft, pos, kind, level)) { inventory.status = "light"; return; }
        if(kind == RaftPiece.Kind.DRYING_RACK && !sunReaches(raft,pos,kind,level)){inventory.status="light";return;}
        boolean heated = kind.grill()
                || kind == RaftPiece.Kind.SMELTER || kind == RaftPiece.Kind.COOKING_POT;
        int advance = 20;
        if (heated) {
            advance = burnFuel(raft, pos, kind, level, inventory);
            if (advance == 0) { return; }
        }
        inventory.status = "working";
        inventory.progress += Math.max(1,(int)(advance * speed(kind)));
        if (inventory.progress >= duration) {
            for (int slot : inputs) {
                if (slot == (kind == RaftPiece.Kind.COOKING_POT ? 2 : (kind == RaftPiece.Kind.PLANTER || kind == RaftPiece.Kind.TREE_PLANTER) ? 4 : -1)) {
                    inventory.cleanWater--;
                } else { inventory.removeItem(slot, 1); }
            }
            for (int i = 0; i < results.size(); i++) {
                int slot = outputs[i];
                if (inventory.getItem(slot).isEmpty()) { inventory.setItem(slot, results.get(i).copy()); }
                else { inventory.getItem(slot).grow(results.get(i).getCount()); }
            }
            inventory.progress = 0;
            if(kind==RaftPiece.Kind.COOKING_POT){inventory.cookingRequested=false;
                if(dev.raftocean.config.RaftConfig.MANUAL_COOKING.get())inventory.status="cook_done";}
        }
        inventory.setChanged();
    }
    /** Queue exactly one validated recipe; no items, water or fuel commit on a button click. */
    public static boolean startCooking(RaftStorage inventory,ServerLevel level){
        if(inventory.kind()!=RaftPiece.Kind.COOKING_POT || inventory.paused || inventory.cookingRequested
                || !dev.raftocean.config.RaftConfig.MANUAL_COOKING.get())return false;
        boolean cup=inventory.getItem(2).is(SurvivalContent.FRESH_WATER.get());
        if(inventory.cleanWater<=0 && !cup){inventory.status="water";return false;}
        if(cup && inventory.cleanWater<CupTransfers.CAPACITY && !accepts(inventory.getItem(5),new ItemStack(SurvivalContent.EMPTY_CUP.get()))){inventory.status="cup";return false;}
        var input=CraftingInput.of(4,1,List.of(inventory.getItem(0),inventory.getItem(1),new ItemStack(SurvivalContent.FRESH_WATER.get()),inventory.getItem(6)));
        var holder=level.recipeAccess().getRecipeFor(ProcessingRecipe.type(RaftPiece.Kind.COOKING_POT),input,level).orElse(null);
        if(holder==null){inventory.status="recipe";return false;}
        var result=holder.value().assemble(input);
        if(holder.value().byproducts().stream().anyMatch(s->!s.create().is(SurvivalContent.EMPTY_CUP.get()))
                || !accepts(inventory.getItem(4),result)){inventory.status="output";return false;}
        String key=holder.id().identifier().toString();
        if(!key.equals(inventory.work))inventory.progress=0;
        inventory.work=key;inventory.duration=holder.value().duration();inventory.cookingRequested=true;
        inventory.status="cook_queued";inventory.setChanged();return true;
    }
    private static double speed(RaftPiece.Kind kind) {
        var cfg = dev.raftocean.config.RaftConfig.PROCESSING;
        double machine = switch(kind) {
            case BASIC_PURIFIER, PURIFIER -> dev.raftocean.config.RaftConfig.PURIFIER_SPEED.get();
            case RAIN_COLLECTOR -> dev.raftocean.config.RaftConfig.RAIN_SPEED.get();
            case BASIC_GRILL, GRILL -> dev.raftocean.config.RaftConfig.GRILL_SPEED.get();
            case SMELTER -> dev.raftocean.config.RaftConfig.SMELTER_SPEED.get();
            case COOKING_POT -> dev.raftocean.config.RaftConfig.POT_SPEED.get();
            case PLANTER -> dev.raftocean.config.RaftConfig.CROP_SPEED.get();
            case TREE_PLANTER -> dev.raftocean.config.RaftConfig.TREE_SPEED.get();
            default -> 1;
        };
        return cfg.get() * machine;
    }
    private static boolean accepts(ItemStack existing, ItemStack result) {
        return !result.isEmpty() && result.getCount() <= result.getMaxStackSize()
                && (existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, result)
                && existing.getCount() + result.getCount() <= existing.getMaxStackSize());
    }
    private static void rejectRecipe(RaftStorage inventory, boolean supplied) {
        reset(inventory);
        if(inventory.kind()==RaftPiece.Kind.COOKING_POT && dev.raftocean.config.RaftConfig.MANUAL_COOKING.get())inventory.cookingRequested=false;
        inventory.status = supplied ? "recipe" : "supplies";
    }
    private static void reset(RaftStorage inventory) { inventory.progress = 0; inventory.duration = 0; inventory.work = ""; }

    /** Legacy inserted cups and the optional cup slot exchange contents without deleting containers. */
    private static void acceptCup(RaftStorage storage, RaftPiece.Kind kind) {
        int input = kind == RaftPiece.Kind.COOKING_POT ? 2 : (kind == RaftPiece.Kind.PLANTER || kind == RaftPiece.Kind.TREE_PLANTER) ? 4 : 0;
        ItemStack cup = storage.getItem(input);
        boolean purifier = kind.purifier();
        boolean collector = kind == RaftPiece.Kind.RAIN_COLLECTOR;
        boolean fresh = kind == RaftPiece.Kind.COOKING_POT || (kind == RaftPiece.Kind.PLANTER || kind == RaftPiece.Kind.TREE_PLANTER);
        boolean pour = purifier && cup.is(SurvivalContent.SALT_WATER.get()) && storage.rawWater < CupTransfers.CAPACITY
                || fresh && cup.is(SurvivalContent.FRESH_WATER.get()) && storage.cleanWater < CupTransfers.CAPACITY;
        boolean draw = (purifier || collector) && cup.is(SurvivalContent.EMPTY_CUP.get()) && storage.cleanWater > 0;
        if (!pour && !draw) { return; }
        int output = draw ? 2 : purifier ? 3 : 5;
        ItemStack result = new ItemStack(draw ? SurvivalContent.FRESH_WATER.get() : SurvivalContent.EMPTY_CUP.get());
        if (!accepts(storage.getItem(output), result)) { storage.status = "cup"; return; }
        storage.removeItem(input, 1);
        if (draw) { storage.cleanWater--; }
        else if (purifier) { storage.rawWater++; }
        else { storage.cleanWater++; }
        if (storage.getItem(output).isEmpty()) { storage.setItem(output, result); }
        else { storage.getItem(output).grow(1); }
        storage.setChanged();
    }

    private static void processWater(RaftEntity raft, BlockPos pos, RaftPiece.Kind kind, ServerLevel level, RaftStorage storage) {
        var input = CraftingInput.of(1, 1, List.of(new ItemStack(kind.purifier()
                ? SurvivalContent.SALT_WATER.get() : SurvivalContent.EMPTY_CUP.get())));
        var holder = level.recipeAccess().getRecipeFor(ProcessingRecipe.type(kind), input, level).orElse(null);
        if (holder == null || !holder.value().assemble(input).is(SurvivalContent.FRESH_WATER.get())
                || !holder.value().byproducts().isEmpty()) { reset(storage); return; }
        storage.duration = holder.value().duration();
        String key = holder.id().identifier().toString();
        if (!key.equals(storage.work)) { storage.work = key; storage.progress = 0; }
        if (storage.cleanWater >= CupTransfers.CAPACITY) { storage.status = "full"; return; }
        int advance = 20;
        if (kind.purifier()) {
            if (storage.rawWater == 0) { return; }
            advance = burnFuel(raft, pos, kind, level, storage);
            if (advance == 0) { return; }
        } else if (!rainReaches(raft, pos, level)) { storage.status = "rain"; return; }
        storage.status = "working";
        storage.progress += Math.max(1,(int)(advance * speed(kind)));
        if (storage.progress >= storage.duration) {
            if (kind.purifier()) { storage.rawWater--; }
            storage.cleanWater++;
            storage.progress = 0;
        }
        storage.setChanged();
    }

    private static int burnFuel(RaftEntity raft, BlockPos pos, RaftPiece.Kind kind, ServerLevel level, RaftStorage inventory) {
        int fuelSlot = kind == RaftPiece.Kind.COOKING_POT ? 3 : 1;
        if (inventory.fuelTicks == 0) {
            ItemStack fuel = inventory.getItem(fuelSlot);
            // Biofuel burns exactly like coal.
            ItemStack rated = fuel.is(dev.raftocean.survival.Automation.BIOFUEL.get()) ? new ItemStack(net.minecraft.world.item.Items.COAL) : fuel;
            var reader = new FuelReader(BlockPos.containing(raft.stationPosition(pos, kind)));
            reader.setLevel(level); reader.setItem(1, rated.copy());
            int burn = reader.duration(level, rated) * (rated == fuel ? 1 : dev.raftocean.survival.Automation.BIOFUEL_BURN_MULTIPLIER);
            var remainder = fuel.getCraftingRemainder();
            float speed = reader.speed(level, rated);
            if (burn <= 0 || remainder != null && fuel.getCount() != 1 || !Float.isFinite(speed) || speed <= 0) {
                inventory.status = "fuel"; return 0;
            }
            inventory.fuelSpeed = Math.clamp(speed, .01F, 100F);
            inventory.fuelTicks = inventory.fuelCapacity = Math.clamp((int)(burn * dev.raftocean.config.RaftConfig.MACHINE_FUEL.get()),1,200000);
            fuel.shrink(1);
            if (fuel.isEmpty() && remainder != null) { inventory.setItem(fuelSlot, remainder.create()); }
        }
        int burned = Math.min(20, inventory.fuelTicks);
        inventory.fuelTicks -= burned;
        return Math.max(1, (int)(burned * inventory.fuelSpeed));
    }
    public static boolean sunReaches(RaftEntity raft,BlockPos pos,RaftPiece.Kind kind,ServerLevel level) {
        Vec3 center=raft.stationPosition(pos,kind);
        if(center==null || !level.dimensionType().hasSkyLight() || level.isDarkOutside() || level.isRaining())return false;
        var sky=BlockPos.containing(center).above();
        return level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,sky.getX(),sky.getZ())<=sky.getY()
                && level.canSeeSky(sky) && !covered(raft,center);
    }
    public static boolean rainReaches(RaftEntity raft, BlockPos pos, ServerLevel level) {
        Vec3 center = raft.stationPosition(pos, RaftPiece.Kind.RAIN_COLLECTOR);
        return center != null && level.isRainingAt(BlockPos.containing(center).above()) && !covered(raft, center);
    }
    private static boolean lightReaches(RaftEntity raft, BlockPos pos, RaftPiece.Kind kind, ServerLevel level) {
        Vec3 center = raft.stationPosition(pos, kind);
        return center != null && (level.getMaxLocalRawBrightness(BlockPos.containing(center).above()) >= dev.raftocean.config.RaftConfig.PLANT_LIGHT.get())
                && (!dev.raftocean.config.RaftConfig.PLANT_SKY.get() || !covered(raft, center));
    }
    public static boolean covered(RaftEntity raft, Vec3 center) {
        return raft.getCollisionBoxes().stream().anyMatch(box -> box.minY > center.y + .5
                && center.x >= box.minX && center.x <= box.maxX && center.z >= box.minZ && center.z <= box.maxZ);
    }
    private static final class FuelReader extends FurnaceBlockEntity {
        FuelReader(BlockPos pos) { super(pos, Blocks.FURNACE.defaultBlockState()); }
        int duration(ServerLevel level, ItemStack stack) { return getBurnDuration(level, stack); }
        float speed(ServerLevel level, ItemStack stack) { return getSpeedMultiplier(level, stack); }
    }
}
