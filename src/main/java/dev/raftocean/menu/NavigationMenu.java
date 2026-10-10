package dev.raftocean.menu;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.*;

/** Native item transactions and validated server buttons for each installed control. */
public final class NavigationMenu extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, "raft_ocean");
    private static final Map<RaftPiece.Kind, DeferredHolder<MenuType<?>, MenuType<NavigationMenu>>> REGISTERED = new EnumMap<>(RaftPiece.Kind.class);
    static {
        for (var kind : RaftPiece.Kind.values()) {
            if (kind.navigation()) { REGISTERED.put(kind, TYPES.register(kind.id(), () -> new MenuType<>(
                    (id, inventory) -> new NavigationMenu(id, inventory, kind, new SimpleContainer(27), new SimpleContainerData(7), null, null), FeatureFlags.VANILLA_SET))); }
        }
    }
    public static MenuType<NavigationMenu> type(RaftPiece.Kind kind) { return REGISTERED.get(kind).get(); }
    private final RaftPiece.Kind kind;
    private final Container storage;
    private final ContainerData data;
    private final RaftEntity raft;
    private final BlockPos pos;
    public NavigationMenu(int id, Inventory inventory, RaftEntity raft, BlockPos pos, RaftPiece.Kind kind) {
        this(id, inventory, kind, raft.storage(pos), new ContainerData() {
            public int get(int index) {
                var state = raft.storage(pos);
                if (state == null) { return 0; }
                return switch (index) {
                    case 0 -> state.enabled ? 1 : 0;
                    case 1 -> state.fuelCapacity <= 0 ? 0 : (int)((long)state.fuelTicks * 1000 / state.fuelCapacity);
                    case 2 -> kind==RaftPiece.Kind.ENGINE ? (int)dev.raftocean.navigation.RaftNavigation.normalize(
                            raft.getYRot()+raft.pieces().get(pos).rotation()*90+state.trim) : (int)raft.navigation().heading();
                    case 3 -> state.trim;
                    case 4 -> (int)raft.navigation().windHeading();
                    case 5 -> raft.navigation().anchored() ? 1 : 0;
                    case 6 -> (int)(raft.navigation().windStrength() * 100);
                    default -> 0;
                };
            }
            public void set(int index, int value) { }
            public int getCount() { return 7; }
        }, raft, pos.immutable());
    }
    private NavigationMenu(int id, Inventory inventory, RaftPiece.Kind kind, Container storage, ContainerData data, RaftEntity raft, BlockPos pos) {
        super(type(kind), id);
        this.kind = kind; this.storage = storage; this.data = data; this.raft = raft; this.pos = pos;
        addSlot(new Slot(storage, 1, 23, 59) {
            @Override public boolean isActive() { return kind == RaftPiece.Kind.ENGINE; }
            @Override public boolean mayPickup(Player player) { return isActive(); }
            @Override public boolean mayPlace(ItemStack stack) { return isActive() && (stack.has(DataComponents.COOKING_FUEL) || stack.is(dev.raftocean.survival.Automation.BIOFUEL.get())); }
        });
        addStandardInventorySlots(inventory, 43, 138);
        addDataSlots(data);
    }
    public RaftPiece.Kind kind() { return kind; }
    public boolean enabled() { return data.get(0) != 0; }
    public int fuel() { return data.get(1); }
    public int heading() { return data.get(2); }
    public int trim() { return data.get(3); }
    public int wind() { return data.get(4); }
    public boolean anchored() { return data.get(5) != 0; }
    public int windPower() { return data.get(6); }
    @Override public boolean stillValid(Player player) { return storage.stillValid(player); }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (raft == null || raft.level().isClientSide() || !stillValid(player) || button < 0 || button > 10) { return false; }
        var state = raft.storage(pos);
        if (button == 0 && kind == RaftPiece.Kind.DISPOSABLE_ANCHOR && state.enabled) {
            if (!raft.removeStructuralPiece(pos)) { return false; }
            player.closeContainer();
            return true;
        }
        if (button == 0 && kind != RaftPiece.Kind.STEERING_WHEEL) { state.enabled = !state.enabled; }
        else if (button == 10 && kind == RaftPiece.Kind.ENGINE) { state.trim = state.trim == 0 ? 180 : 0; }
        else if (button > 0 && button <= 9 && (kind == RaftPiece.Kind.STEERING_WHEEL || kind == RaftPiece.Kind.SAIL)) {
            int step = switch(button) {case 1->-15;case 2->15;case 3->-1;case 4->1;
                case 5->-5;case 6->5;case 7->-45;case 8->45;default->0;};
            if(kind==RaftPiece.Kind.STEERING_WHEEL) {
                if(button==9){raft.navigation().setHeading(raft.getYRot());}
                else {raft.navigation().turn(step);}
            } else {state.trim=button==9?0:Math.floorMod(state.trim+step+180,360)-180;}
        } else { return false; }
        state.setChanged(); raft.navigation().sync(); broadcastChanges(); return true;
    }
    @Override public void clicked(int index, int button, ContainerInput input, Player player) {
        if (!stillValid(player) || index >= 0 && (index >= slots.size() || !slots.get(index).isActive())) { return; }
        super.clicked(index, button, input, player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !stillValid(player)) { return ItemStack.EMPTY; }
        var slot = slots.get(index);
        if (!slot.isActive() || !slot.hasItem()) { return ItemStack.EMPTY; }
        var stack = slot.getItem(); var before = stack.copy();
        boolean moved = index == 0 ? moveItemStackTo(stack, 1, 37, true)
                : slots.get(0).mayPlace(stack) && moveItemStackTo(stack, 0, 1, false);
        if (!moved && index != 0) { moved = index < 28 ? moveItemStackTo(stack, 28, 37, false) : moveItemStackTo(stack, 1, 28, false); }
        if (!moved) { return ItemStack.EMPTY; }
        if (stack.isEmpty()) { slot.setByPlayer(ItemStack.EMPTY); } else { slot.setChanged(); }
        slot.onTake(player, stack); return before;
    }
}
