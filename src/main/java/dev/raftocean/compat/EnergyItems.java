package dev.raftocean.compat;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.menu.RaftStorage;
import dev.raftocean.survival.Automation;
import dev.raftocean.survival.Batteries;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Standard NeoForge energy compatibility; one raft charge unit equals one FE. */
public final class EnergyItems {
    private EnergyItems() { }
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Energy.ITEM,(stack,access)->
                !RaftConfig.ENERGY_ITEMS.get() || stack.getCount()!=1 ? null :
                new ItemAccessEnergyHandler(access,DataComponents.DAMAGE,Batteries.capacity(stack)) {
                    @Override protected int getAmountFrom(ItemResource resource) {
                        return resource.is(validItem)?Batteries.charge(resource.toStack()):0;
                    }
                    @Override protected ItemResource update(ItemResource resource,int amount) {
                        var value=resource.toStack();Batteries.setCharge(value,amount);
                        return resource.with(DataComponents.CUSTOM_DATA,value.get(DataComponents.CUSTOM_DATA));
                    }
                },Batteries.BASIC.get(),Batteries.ADVANCED.get());
    }
    public static EnergyHandler handler(ItemStack stack) {
        if(!RaftConfig.ENERGY_ITEMS.get() || stack.isEmpty() || stack.getCount()!=1)return null;
        return ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
    }
    public static boolean foreign(ItemStack stack) { return Batteries.capacity(stack)==0 && handler(stack)!=null; }
    public static boolean compatible(ItemStack stack) { return Batteries.capacity(stack)>0 || handler(stack)!=null; }
    public static long missing(ItemStack stack) {
        var handler=handler(stack);return handler==null?0:Math.max(0,handler.getCapacityAsLong()-handler.getAmountAsLong());
    }
    public static int percent(ItemStack stack) {
        if(Batteries.capacity(stack)>0)return (int)((long)Batteries.charge(stack)*100/Batteries.capacity(stack));
        var handler=handler(stack);if(handler==null || handler.getCapacityAsLong()<=0)return 0;
        return (int)Math.clamp(100D*handler.getAmountAsLong()/handler.getCapacityAsLong(),0D,100D);
    }
    public static void tick(RaftStorage charger,List<RaftStorage> generators,long second) {
        chargePorts(charger,generators,second,handler(charger.getItem(1)),handler(charger.getItem(2)));
    }
    /** Charges only accepted energy; rejecting or rate-limited ports cannot destroy fuel. */
    public static void chargePorts(RaftStorage charger,List<RaftStorage> generators,long second,EnergyHandler left,EnergyHandler right) {
        if(charger.paused)return;
        var ports=new EnergyHandler[]{left,right};
        long missing=0;for(var port:ports)if(port!=null)missing+=Math.min(Integer.MAX_VALUE,Math.max(0,port.getCapacityAsLong()-port.getAmountAsLong()));
        int bank=(int)Math.min(Integer.MAX_VALUE,generators.stream().filter(g->!g.paused).mapToLong(g->Math.max(0,g.energy)).sum());
        boolean freshFuel=charger.fuelTicks==0 && charger.getItem(0).is(Automation.BIOFUEL.get());
        long fuel=charger.fuelTicks+(freshFuel?RaftConfig.GENERATOR_ENERGY.get():0L);
        int budget=(int)Math.min(Math.min(missing,RaftConfig.BATTERY_CHARGE_RATE.get()),bank+fuel);
        int received=0;
        if(budget>0)try(var transaction=Transaction.openRoot()) {
            int first=(int)Math.floorMod(second,2L);
            for(int i=0;i<2;i++) {
                var port=ports[(first+i)%2];if(port!=null)received+=port.insert(Math.min(budget-received,(budget+1)/2),transaction);
            }
            for(int i=0;i<2 && received<budget;i++) {
                var port=ports[(first+i)%2];if(port!=null)received+=port.insert(budget-received,transaction);
            }
            int wired=Math.min(bank,received),needed=received-wired;
            transaction.commit();
            if(wired>0)Automation.spend(generators,wired);
            if(needed>0 && freshFuel) {
                charger.removeItem(0,1);charger.fuelTicks=charger.fuelCapacity=RaftConfig.GENERATOR_ENERGY.get();
            }
            charger.fuelTicks-=needed;
        }
        long capacity=0,amount=0;
        for(var port:ports)if(port!=null){capacity+=Math.min(Integer.MAX_VALUE,port.getCapacityAsLong());amount+=Math.min(Integer.MAX_VALUE,port.getAmountAsLong());}
        charger.duration=1000;charger.progress=capacity<=0?0:(int)Math.clamp(1000D*amount/capacity,0D,1000D);
        charger.status=missing==0?capacity==0?"supplies":"full":received>0?"working":budget==0?"fuel":"power";
        charger.setChanged();
    }
}
