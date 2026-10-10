package dev.raftocean.survival;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.menu.RaftStorage;
import dev.raftocean.registry.ModContent;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredItem;

/** Portable charge is stored on the native item stack, never in a separate inventory counter. */
public final class Batteries {
    public static final DeferredItem<BatteryItem> BASIC = ModContent.ITEMS.registerItem("tidal_battery", p -> new BatteryItem(p.stacksTo(1),1000));
    public static final DeferredItem<BatteryItem> ADVANCED = ModContent.ITEMS.registerItem("deepcycle_battery", p -> new BatteryItem(p.stacksTo(1),4000));
    public static void initialize() { }
    public static int capacity(ItemStack stack) { return stack.getItem() instanceof BatteryItem battery ? battery.capacity : 0; }
    public static int charge(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag()
                .getInt("RaftBatteryCharge").orElse(0),0,capacity(stack));
    }
    public static void setCharge(ItemStack stack,int amount) {
        if(capacity(stack)==0)return;
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag -> tag.putInt("RaftBatteryCharge",Math.clamp(amount,0,capacity(stack))));
    }
    public static int missing(RaftStorage charger) {
        int total=0;for(int slot=1;slot<=2;slot++)total+=capacity(charger.getItem(slot))-charge(charger.getItem(slot));return total;
    }
    /** Preflight all sources so an underpowered processing step spends nothing. */
    public static boolean spend(List<RaftStorage> generators,RaftStorage consumer,int amount) {
        if(consumer.paused || amount<=0)return false;
        var battery=consumer.getItem(1);
        int bank=(int)generators.stream().filter(g -> !g.paused).mapToLong(g -> g.energy).sum();
        if((long)bank+charge(battery)<amount)return false;
        int wired=Math.min(bank,amount);
        if(wired>0)Automation.spend(generators,wired);
        if(wired<amount){setCharge(battery,charge(battery)-(amount-wired));consumer.setChanged();}
        return true;
    }
    public static void tickCharger(RaftStorage charger,List<RaftStorage> generators,long second) {
        if(charger.paused)return;
        int missing=missing(charger);
        if(missing==0){charger.status=capacity(charger.getItem(1))+capacity(charger.getItem(2))==0?"supplies":"full";updateProgress(charger);return;}
        int budget=Math.min(missing,RaftConfig.BATTERY_CHARGE_RATE.get());
        int bank=(int)generators.stream().filter(g -> !g.paused).mapToLong(g -> g.energy).sum();
        int wired=Math.min(budget,bank);
        if(wired>0)Automation.spend(generators,wired);
        int deficit=budget-wired;
        if(deficit>0 && charger.fuelTicks==0 && charger.getItem(0).is(Automation.BIOFUEL.get())) {
            charger.removeItem(0,1);charger.fuelTicks=charger.fuelCapacity=RaftConfig.GENERATOR_ENERGY.get();
        }
        int fueled=Math.min(deficit,charger.fuelTicks);charger.fuelTicks-=fueled;
        int received=wired+fueled,remaining=received;
        // Alternate the first socket, so both batteries charge even with one unit per update.
        int first=1+(int)Math.floorMod(second,2L);
        for(int i=0;i<2;i++) {
            int slot=i==0?first:3-first;var battery=charger.getItem(slot);
            int take=Math.min(remaining,Math.min((received+1)/2,capacity(battery)-charge(battery)));
            setCharge(battery,charge(battery)+take);remaining-=take;
        }
        // Unused half of the budget goes to the other socket, rather than vanishing.
        for(int slot=1;slot<=2 && remaining>0;slot++) {
            var battery=charger.getItem(slot);int take=Math.min(remaining,capacity(battery)-charge(battery));
            setCharge(battery,charge(battery)+take);remaining-=take;
        }
        if(remaining!=0)throw new IllegalStateException("Battery charge conservation failed");
        charger.status=received>0?"working":bank==0 && charger.fuelTicks==0?"fuel":"power";
        updateProgress(charger);charger.setChanged();
    }
    private static void updateProgress(RaftStorage charger) {
        charger.duration=capacity(charger.getItem(1))+capacity(charger.getItem(2));
        charger.progress=charge(charger.getItem(1))+charge(charger.getItem(2));
    }
    public static final class BatteryItem extends Item {
        private final int capacity;
        public BatteryItem(Properties properties,int capacity) { super(properties);this.capacity=capacity; }
        @Override public boolean isBarVisible(ItemStack stack) { return true; }
        @Override public int getBarWidth(ItemStack stack) { return Math.round(13F*charge(stack)/capacity); }
        @Override public int getBarColor(ItemStack stack) { return charge(stack)==0?0xC85C4F:0x62D2AE; }
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,
                net.minecraft.world.item.component.TooltipDisplay display,
                java.util.function.Consumer<net.minecraft.network.chat.Component> output,TooltipFlag flag) {
            output.accept(net.minecraft.network.chat.Component.translatable("raft_ocean.battery.charge",charge(stack),capacity)
                    .withStyle(net.minecraft.ChatFormatting.AQUA));
            output.accept(net.minecraft.network.chat.Component.translatable("raft_ocean.battery.recharge")
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
    private Batteries() { }
}
