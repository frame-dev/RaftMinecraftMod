package dev.raftocean.salvage;

import dev.raftocean.api.*;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.registry.ModContent;
import dev.raftocean.survival.Batteries;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.*;

/** Mixed-material batches retain all inputs until an exact, atomic completion. */
public final class SalvageCompactor implements RaftMachineBehavior {
    public static final Identifier ID=Identifier.fromNamespaceAndPath("raft_ocean","salvage_compactor");
    public static final DeferredBlock<Block> VISUAL=ModContent.BLOCKS.registerSimpleBlock("salvage_compactor");
    public static final DeferredBlock<Block> ACTIVE=ModContent.BLOCKS.registerSimpleBlock("salvage_compactor_active");
    public static final DeferredItem<Item> ITEM=ModContent.ITEMS.registerSimpleItem("salvage_compactor",p->p.stacksTo(16));
    public static final DeferredItem<Item> BALE=ModContent.ITEMS.registerSimpleItem("salvage_bale");
    public static final SalvageCompactor CONTROLLER=new SalvageCompactor();
    public static final int POINTS=40, OUTPUT=8, BATTERY=7;
    private static final TagKey<Item> LOW=tag("light"),MEDIUM=tag("standard"),HIGH=tag("dense");
    private static TagKey<Item> tag(String name){return TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("raft_ocean","recycling/"+name));}
    public static void initialize() { }
    public static void register(){
        var boxes=List.of(new AABB(-.4375,0,-.4375,.4375,.875,.4375));
        RaftBuildables.register(new RaftBuildable(ID,ITEM::get,VISUAL,1,1,1,12,true,boxes,boxes,(r,a,p,h)->InteractionResult.PASS),CONTROLLER);
    }
    public static int value(ItemStack stack){
        // Named, damaged, enchanted or component-bearing valuables must never be recycled silently.
        if(stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack,new ItemStack(stack.getItem())))return 0;
        return stack.is(HIGH)?4:stack.is(MEDIUM)?2:stack.is(LOW)?1:0;
    }
    /** Bounded subset sum finds exactly forty points, including mixed stacks, without wasting overfill. */
    public static Map<Integer,ItemStack> batch(RaftMachineState state){
        int[][] plans=new int[POINTS+1][];plans[0]=new int[7];
        for(int slot=0;slot<7;slot++){
            var stack=state.getItem(slot);int value=value(stack);if(value==0)continue;
            for(int unit=0;unit<Math.min(stack.getCount(),POINTS/value);unit++)
                for(int total=POINTS;total>=value;total--)if(plans[total]==null && plans[total-value]!=null){
                    plans[total]=plans[total-value].clone();plans[total][slot]++;
                }
        }
        if(plans[POINTS]==null)return Map.of();
        Map<Integer,ItemStack> result=new LinkedHashMap<>();
        for(int slot=0;slot<7;slot++)if(plans[POINTS][slot]>0)result.put(slot,state.getItem(slot).copyWithCount(plans[POINTS][slot]));
        return result;
    }
    @Override public int tickInterval(){return 20;}
    @Override public boolean canInsert(RaftMachineContext c,int slot,ItemStack stack){return slot>=0 && slot<7?value(stack)>0:slot==BATTERY && Batteries.capacity(stack)>0;}
    @Override public Set<Integer> quickHarvestSlots(){return Set.of(OUTPUT);}
    @Override public void loaded(RaftMachineContext c){c.state().setInt("active",0);c.state().setInt("progress",Math.clamp(c.state().getInt("progress"),0,1800));}
    @Override public void tick(RaftMachineContext ctx){
        if(!ctx.isInstalled())return;
        var s=ctx.state();s.setInt("active",0);int duration=RaftConfig.COMPACTOR_SECONDS.get();s.setInt("duration",duration);
        if(!RaftConfig.RECYCLING.get() || s.getInt("paused")!=0)return;
        var inputs=batch(s);if(inputs.isEmpty())return;
        var out=s.getItem(OUTPUT);if(!out.isEmpty() && (!ItemStack.isSameItemSameComponents(out,new ItemStack(BALE.get())) || out.getCount()>=4))return;
        var battery=s.getItem(BATTERY);if(Batteries.charge(battery)<1)return;
        int progress=Math.min(s.getInt("progress"),duration-1)+1;
        if(progress>=duration && !RaftMachineTransactions.tryProcess(ctx,inputs,Map.of(OUTPUT,new ItemStack(BALE.get())),RaftUtilityCost.NONE))return;
        Batteries.setCharge(battery,Batteries.charge(battery)-1);s.setInt("progress",progress>=duration?0:progress);s.setInt("active",1);s.setChanged();
    }
    @Override public InteractionResult use(RaftMachineContext ctx,Player player,InteractionHand hand){
        if(player.isSpectator() || !ctx.valid(player))return InteractionResult.FAIL;
        player.sendOverlayMessage(Component.translatable("raft_ocean.compactor.help",ctx.state().getInt("progress"),RaftConfig.COMPACTOR_SECONDS.get()));
        RaftMachineMenus.openCustom(ctx,player,Component.translatable("item.raft_ocean.salvage_compactor"),
                (id,inventory,c)->new dev.raftocean.menu.CompactorMenu(id,inventory,c));return InteractionResult.SUCCESS_SERVER;
    }
    @Override public net.minecraft.world.level.block.state.BlockState visual(RaftBuildable type,RaftMachineView view){return (view.active()?ACTIVE:VISUAL).get().defaultBlockState();}
    private SalvageCompactor() { }
}
