package dev.raftocean.salvage;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Simulate the complete deposit before committing; full nets never partially consume debris. */
public final class SalvageTransfer {
    private SalvageTransfer() { }

    public static boolean deposit(Container destination, List<ItemStack> loot) {
        List<ItemStack> slots = new ArrayList<>();
        for (int i = 0; i < destination.getContainerSize(); i++) { slots.add(destination.getItem(i).copy()); }
        for (ItemStack original : loot) {
            ItemStack remainder = original.copy();
            for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++) {
                for (int i = 0; i < slots.size() && !remainder.isEmpty(); i++) {
                    ItemStack slot = slots.get(i);
                    if (!destination.canPlaceItem(i, remainder) || (pass == 0 && slot.isEmpty())
                            || (pass == 1 && !slot.isEmpty())
                            || (!slot.isEmpty() && !ItemStack.isSameItemSameComponents(slot, remainder))) { continue; }
                    int max = Math.min(destination.getMaxStackSize(remainder), remainder.getMaxStackSize());
                    int count = Math.min(remainder.getCount(), Math.max(0, max - slot.getCount()));
                    if (count == 0) { continue; }
                    if (slot.isEmpty()) { slots.set(i, remainder.copyWithCount(count)); }
                    else { slot.grow(count); }
                    remainder.shrink(count);
                }
            }
            if (!remainder.isEmpty()) { return false; }
        }
        for (int i = 0; i < slots.size(); i++) { destination.setItem(i, slots.get(i)); }
        destination.setChanged();
        return true;
    }
}
