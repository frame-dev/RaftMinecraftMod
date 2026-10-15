package dev.raftocean.api;

import java.util.*;
import net.minecraft.world.item.ItemStack;

/** Small server-thread batch processing with exact item-component and utility preflight. */
public final class RaftMachineTransactions {
    private RaftMachineTransactions() { }
    public static boolean tryProcess(RaftMachineContext ctx,Map<Integer,ItemStack> inputs,
            Map<Integer,ItemStack> outputs,RaftUtilityCost utilities) {
        var staged=stage(ctx,inputs,outputs);
        if(staged==null || !RaftMachineUtilities.consume(ctx,utilities))return false;
        staged.forEach(ctx.state()::setItem);ctx.state().setChanged();return true;
    }
    public static boolean canProcess(RaftMachineContext ctx,Map<Integer,ItemStack> inputs,
            Map<Integer,ItemStack> outputs,RaftUtilityCost utilities) {
        return stage(ctx,inputs,outputs)!=null && RaftMachineUtilities.canConsume(ctx,utilities);
    }
    private static Map<Integer,ItemStack> stage(RaftMachineContext ctx,Map<Integer,ItemStack> inputs,Map<Integer,ItemStack> outputs) {
        Objects.requireNonNull(inputs);Objects.requireNonNull(outputs);
        if(!ctx.isInstalled())return null;
        var state=ctx.state();Map<Integer,ItemStack> staged=new TreeMap<>();
        for(var entry:inputs.entrySet()) {
            validate(entry.getKey(),entry.getValue(),state.getContainerSize());
            var existing=state.getItem(entry.getKey()).copy();var required=entry.getValue();
            if(!ItemStack.isSameItemSameComponents(existing,required) || existing.getCount()<required.getCount())return null;
            existing.shrink(required.getCount());staged.put(entry.getKey(),existing);
        }
        for(var entry:outputs.entrySet()) {
            validate(entry.getKey(),entry.getValue(),state.getContainerSize());
            var existing=staged.getOrDefault(entry.getKey(),state.getItem(entry.getKey())).copy();var product=entry.getValue();
            if(!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing,product))return null;
            if((long)existing.getCount()+product.getCount()>Math.min(product.getMaxStackSize(),state.getMaxStackSize()))return null;
            if(existing.isEmpty())existing=product.copy();else existing.grow(product.getCount());
            staged.put(entry.getKey(),existing);
        }
        return staged;
    }
    private static void validate(Integer slot,ItemStack stack,int size) {
        if(slot==null || slot<0 || slot>=size || stack==null || stack.isEmpty()
                || stack.getCount()>stack.getMaxStackSize())throw new IllegalArgumentException("Invalid machine batch slot/stack");
    }
}
