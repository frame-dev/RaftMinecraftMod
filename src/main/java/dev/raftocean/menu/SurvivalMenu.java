package dev.raftocean.menu;

import dev.raftocean.entity.RaftPiece;
import dev.raftocean.survival.SurvivalContent;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.*;

/** Role-filtered slots retain all old inventory indices and use native click transactions. */
public final class SurvivalMenu extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, "raft_ocean");
    private static final Map<RaftPiece.Kind, DeferredHolder<MenuType<?>, MenuType<SurvivalMenu>>> REGISTERED = new EnumMap<>(RaftPiece.Kind.class);
    public static final List<String> STATUSES = List.of("supplies", "working", "fuel", "output", "rain", "light", "water", "full", "cup", "power", "network", "off");
    static {
        for (var kind : RaftPiece.Kind.values()) {
            if (kind.machine()) {
                REGISTERED.put(kind, TYPES.register(kind.id(), () -> new MenuType<>(
                        (id, inventory) -> new SurvivalMenu(id, inventory, kind, new SimpleContainer(27), new SimpleContainerData(8)),
                        FeatureFlags.VANILLA_SET)));
            }
        }
    }
    public static MenuType<SurvivalMenu> type(RaftPiece.Kind kind) { return REGISTERED.get(kind).get(); }
    public record Layout(int x, int y, String label, boolean output) { }
    private final Container storage;
    private final ContainerData data;
    private final Player owner;
    private final RaftPiece.Kind kind;
    private final Map<Integer, Layout> layout;

    public SurvivalMenu(int id, Inventory inventory, RaftPiece.Kind kind, RaftStorage storage) {
        this(id, inventory, kind, storage, new ContainerData() {
            private int page;
            public int get(int index) {
                return switch (index) {
                    case 0 -> storage.duration <= 0 ? 0 : Math.clamp((int)((long)storage.progress * 1000 / storage.duration), 0, 1000);
                    case 1 -> storage.fuelCapacity <= 0 ? 0 : Math.clamp((int)((long)storage.fuelTicks * 1000 / storage.fuelCapacity), 0, 1000);
                    case 2 -> Math.max(0, STATUSES.indexOf(storage.status));
                    case 3 -> storage.rawWater;
                    case 4 -> storage.cleanWater;
                    case 5 -> page;
                    case 6 -> storage.energy;
                    case 7 -> storage.paused ? 1 : 0;
                    default -> 0;
                };
            }
            public void set(int index, int value) { if (index == 5) { page = Math.clamp(value, 0, 1); } }
            public int getCount() { return 8; }
        });
    }

    private SurvivalMenu(int id, Inventory inventory, RaftPiece.Kind kind, Container storage, ContainerData data) {
        super(type(kind), id);
        this.storage = storage; this.data = data; this.kind = kind; this.owner = inventory.player;
        layout = layout(kind);
        int extra = 0;
        for (int index = 0; index < 27; index++) {
            int cell = index;
            Layout role = layout.get(index);
            int x = role == null ? 43 + extra % 9 * 18 : role.x();
            int y = role == null ? 42 + extra / 9 * 18 : role.y();
            if (role == null) { extra++; }
            addSlot(new Slot(storage, index, x, y) {
                @Override public boolean isActive() { return suppliesPage() == (role == null); }
                @Override public boolean mayPickup(Player player) { return isActive(); }
                @Override public boolean mayPlace(ItemStack stack) { return isActive() && accepts(cell, stack); }
            });
        }
        addStandardInventorySlots(inventory, 43, 138);
        addDataSlots(data);
    }

    public RaftPiece.Kind kind() { return kind; }
    public Map<Integer, Layout> layout() { return layout; }
    public boolean paused() { return data.get(7) != 0; }
    public boolean suppliesPage() { return data.get(5) == 1; }
    public int progress() { return data.get(0); }
    public int fuel() { return data.get(1); }
    public int energy() { return data.get(6); }
    public int batteryPercent(int slot) {
        var stack=storage.getItem(slot);int capacity=dev.raftocean.survival.Batteries.capacity(stack);
        return capacity==0?0:dev.raftocean.survival.Batteries.charge(stack)*100/capacity;
    }
    public int waterCapacity() { return dev.raftocean.survival.Automation.waterCapacity(kind); }
    public int rawWater() { return data.get(3); }
    public int cleanWater() { return data.get(4); }
    public String status() { return STATUSES.get(Math.clamp(data.get(2), 0, STATUSES.size() - 1)); }
    public boolean heated() { return kind.purifier() || kind.grill()
            || kind == RaftPiece.Kind.SMELTER || kind == RaftPiece.Kind.COOKING_POT || kind == RaftPiece.Kind.BIOFUEL_GENERATOR || kind == RaftPiece.Kind.BATTERY_CHARGER; }
    public boolean waterMachine() { return kind != RaftPiece.Kind.DRYING_RACK && !kind.grill() && kind != RaftPiece.Kind.SMELTER
            && kind != RaftPiece.Kind.BIOREFINER && kind != RaftPiece.Kind.BIOFUEL_GENERATOR && kind != RaftPiece.Kind.FUEL_TANK && kind != RaftPiece.Kind.BATTERY_CHARGER; }

    public boolean accepts(int slot, ItemStack stack) {
        Layout role = layout.get(slot);
        if (role == null) { return true; }
        if (role.output()) { return false; }
        return switch (role.label()) {
            case "damp_brick" -> stack.is(dev.raftocean.survival.CraftingExpansion.item("damp_brick"));
            case "organic" -> inTag(stack,"biofuel_organics");
            case "catalyst" -> stack.is(Items.HONEYCOMB) || stack.is(Items.SUGAR);
            case "battery" -> dev.raftocean.survival.Batteries.capacity(stack)>0;
            case "biofuel" -> stack.is(dev.raftocean.survival.Automation.BIOFUEL.get());
            case "fluid_cup" -> stack.is(SurvivalContent.EMPTY_CUP.get()) || stack.is(SurvivalContent.FRESH_WATER.get());
            case "fuel" -> stack.has(DataComponents.COOKING_FUEL);
            case "cup" -> stack.is(SurvivalContent.EMPTY_CUP.get()) || kind.purifier() && stack.is(SurvivalContent.SALT_WATER.get());
            case "water" -> stack.is(SurvivalContent.FRESH_WATER.get());
            case "mix" -> stack.is(SurvivalContent.PLANTING_MIX.get());
            case "sapling" -> stack.is(net.minecraft.tags.ItemTags.SAPLINGS);
            case "seed" -> stack.is(Items.WHEAT_SEEDS) || stack.is(Items.BEETROOT_SEEDS) || stack.is(Items.CARROT) || stack.is(Items.POTATO)
                    || stack.is(Items.SUGAR_CANE) || stack.is(Items.KELP);
            case "fish" -> inTag(stack, "stew_fish");
            case "base" -> inTag(stack,"pot_bases");
            case "veg" -> inTag(stack, "stew_vegetables");
            case "bowl" -> stack.is(Items.BOWL);
            case "food", "ore" -> owner.level().recipeAccess().propertySet(kind.grill()
                    ? RecipePropertySet.CAMPFIRE_INPUT : RecipePropertySet.FURNACE_INPUT).test(stack);
            default -> false;
        };
    }
    private static boolean inTag(ItemStack stack, String path) {
        return stack.is(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("raft_ocean", path)));
    }
    public static Map<Integer, Layout> layout(RaftPiece.Kind kind) {
        Map<Integer, Layout> roles = new LinkedHashMap<>();
        switch (kind) {
            case WATER_TANK, ELECTRIC_PURIFIER, SPRINKLER -> {
                roles.put(0,new Layout(22,44,"fluid_cup",false));
                if(kind!=RaftPiece.Kind.WATER_TANK){roles.put(1,new Layout(58,44,"battery",false));}
                roles.put(2,new Layout(194,44,"filled",true));
                roles.put(3,new Layout(218,44,"empty",true));
            }
            case BATTERY_CHARGER -> {
                roles.put(0,new Layout(22,44,"biofuel",false));
                roles.put(1,new Layout(174,44,"battery",false));
                roles.put(2,new Layout(210,44,"battery",false));
            }
            case FUEL_TANK, BIOFUEL_GENERATOR -> roles.put(0,new Layout(22,44,"biofuel",false));
            case BIOREFINER -> {
                roles.put(0,new Layout(22,44,"organic",false));
                roles.put(1,new Layout(58,44,"catalyst",false));
                roles.put(2,new Layout(194,44,"result",true));
            }
            case BASIC_PURIFIER, PURIFIER, RAIN_COLLECTOR -> {
                roles.put(0, new Layout(22, 44, "cup", false));
                roles.put(2, new Layout(194, 44, "filled", true));
                roles.put(3, new Layout(218, 44, "empty", true));
            }
            case DRYING_RACK -> {
                roles.put(0,new Layout(22,44,"damp_brick",false));
                roles.put(2,new Layout(194,44,"result",true));
            }
            case BASIC_GRILL, GRILL, SMELTER -> {
                roles.put(0, new Layout(22, 44, kind.grill() ? "food" : "ore", false));
                roles.put(2, new Layout(194, 44, "result", true));
            }
            case PLANTER, TREE_PLANTER -> {
                roles.put(0, new Layout(22, 44, kind == RaftPiece.Kind.TREE_PLANTER ? "sapling" : "seed", false));
                roles.put(1, new Layout(58, 44, "mix", false));
                roles.put(4, new Layout(94, 44, "water", false));
                roles.put(2, new Layout(194, 44, kind == RaftPiece.Kind.TREE_PLANTER ? "timber" : "crop", true));
                roles.put(3, new Layout(218, 44, "seed", true));
                roles.put(5, new Layout(218, 78, "empty", true));
            }
            case COOKING_POT -> {
                roles.put(0, new Layout(22, 44, "base", false));
                roles.put(1, new Layout(58, 44, "veg", false));
                roles.put(2, new Layout(94, 44, "water", false));
                roles.put(6, new Layout(130, 44, "bowl", false));
                roles.put(4, new Layout(194, 44, "stew", true));
                roles.put(5, new Layout(218, 44, "empty", true));
            }
            default -> throw new IllegalArgumentException("Not a survival machine: " + kind);
        }
        if (!kind.automation() && kind != RaftPiece.Kind.DRYING_RACK && kind != RaftPiece.Kind.RAIN_COLLECTOR && kind != RaftPiece.Kind.PLANTER && kind != RaftPiece.Kind.TREE_PLANTER) {
            roles.put(kind == RaftPiece.Kind.COOKING_POT ? 3 : 1, new Layout(22, 78, "fuel", false));
        }
        return Collections.unmodifiableMap(roles);
    }

    @Override public boolean stillValid(Player player) { return storage.stillValid(player); }
    @Override public boolean clickMenuButton(Player player, int button) {
        if ((button != 0 && button != 1) || player.isSpectator() || !stillValid(player) || !getCarried().isEmpty()) { return false; }
        if (button == 0) { data.set(5, suppliesPage() ? 0 : 1); }
        else {
            if (!(storage instanceof RaftStorage machine)) { return false; }
            machine.paused = !machine.paused;
            machine.status = machine.paused ? "off" : "supplies";
            machine.setChanged();
        }
        broadcastChanges();
        return true;
    }
    @Override public void clicked(int index, int button, ContainerInput input, Player player) {
        if (!stillValid(player) || index >= 0 && (index >= slots.size() || !slots.get(index).isActive())) { return; }
        super.clicked(index, button, input, player);
    }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) { return slot.isActive(); }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !stillValid(player)) { return ItemStack.EMPTY; }
        Slot slot = slots.get(index);
        if (!slot.isActive() || !slot.hasItem()) { return ItemStack.EMPTY; }
        ItemStack stack = slot.getItem(), before = stack.copy();
        boolean moved = false;
        if (index < 27) { moved = moveItemStackTo(stack, 27, slots.size(), true); }
        else {
            // Vanilla's merge pass does not call mayPlace: filter before each individual destination.
            for (int i = 0; i < 27 && !stack.isEmpty(); i++) {
                if (slots.get(i).mayPlace(stack)) { moved |= moveItemStackTo(stack, i, i + 1, false); }
            }
            if (!moved) { moved = index < 54 ? moveItemStackTo(stack, 54, 63, false) : moveItemStackTo(stack, 27, 54, false); }
        }
        if (!moved) { return ItemStack.EMPTY; }
        if (stack.isEmpty()) { slot.setByPlayer(ItemStack.EMPTY); } else { slot.setChanged(); }
        slot.onTake(player, stack);
        return before;
    }
}
