package dev.raftocean.survival;

import dev.raftocean.api.*;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.registry.ModContent;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.registries.*;

/** One deliberate four-food batch, then a separate physical-glass serving transaction. */
public final class RaftJuicer implements RaftMachineBehavior {
    public static final Identifier ID=Identifier.fromNamespaceAndPath("raft_ocean","raft_juicer");
    public static final DeferredBlock<Block> VISUAL=ModContent.BLOCKS.registerSimpleBlock("raft_juicer");
    public static final DeferredBlock<Block> ACTIVE=ModContent.BLOCKS.registerSimpleBlock("raft_juicer_active");
    public static final DeferredBlock<Block> READY=ModContent.BLOCKS.registerSimpleBlock("raft_juicer_ready");
    public static final DeferredItem<Item> ITEM=ModContent.ITEMS.registerSimpleItem("raft_juicer",p->p.stacksTo(16));
    public static final DeferredItem<Item> GLASS=ModContent.ITEMS.registerSimpleItem("drinking_glass",p->p.stacksTo(16));
    public static final DeferredItem<JuiceDrinkItem> MELON=drink("melon_breeze",net.minecraft.world.effect.MobEffects.SPEED);
    public static final DeferredItem<JuiceDrinkItem> ORCHARD=drink("orchard_cooler",net.minecraft.world.effect.MobEffects.NIGHT_VISION);
    public static final DeferredItem<JuiceDrinkItem> BERRY=drink("shoreberry_tonic",net.minecraft.world.effect.MobEffects.RESISTANCE);
    public static final RaftJuicer CONTROLLER=new RaftJuicer();
    private static DeferredItem<JuiceDrinkItem> drink(String name,net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect){
        return ModContent.ITEMS.registerItem(name,p->new JuiceDrinkItem(p.stacksTo(16).component(DataComponents.CONSUMABLE,Consumables.DEFAULT_DRINK).usingConvertsTo(GLASS.get()),effect));
    }
    public static void initialize(){JuicingRecipe.initialize();}
    public static void register(){
        var boxes=List.of(new AABB(-.4375,0,-.4375,.4375,.5,.4375),new AABB(.0625,.5,-.25,.4375,1.5,.375));
        RaftBuildables.register(new RaftBuildable(ID,ITEM::get,VISUAL,1,1,2,8,true,boxes,boxes,(r,a,p,h)->InteractionResult.PASS),CONTROLLER);
    }
    public static CraftingInput input(RaftMachineState s){return CraftingInput.of(2,2,List.of(s.getItem(0),s.getItem(1),s.getItem(2),s.getItem(3)));}
    public static Optional<RecipeHolder<JuicingRecipe>> recipe(RaftMachineContext ctx){return ((ServerLevel)ctx.raft().level()).recipeAccess().getRecipeFor(JuicingRecipe.TYPE.get(),input(ctx.state()),ctx.raft().level());}
    @Override public int tickInterval(){return 20;}
    @Override public boolean canInsert(RaftMachineContext c,int slot,ItemStack stack){return slot>=0 && slot<4?isIngredient(stack):slot==4?Batteries.capacity(stack)>0:slot==5 && stack.is(GLASS.get());}
    public static boolean isIngredient(ItemStack stack){return !stack.isEmpty() && stack.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,Identifier.fromNamespaceAndPath("raft_ocean","juicer_ingredients")));}
    @Override public boolean canExtract(RaftMachineContext c,int slot,Player player){return slot>=0 && slot<=6;}
    @Override public Set<Integer> quickHarvestSlots(){return Set.of(6);}
    @Override public void loaded(RaftMachineContext ctx){ctx.state().setInt("active",0);ctx.state().setInt("progress",Math.clamp(ctx.state().getInt("progress"),0,24000));}
    @Override public void removed(RaftMachineContext ctx){
        // Private slots represent the queued recipe and unbottled liquid, never free physical glasses.
        ctx.state().setItem(7,ItemStack.EMPTY);ctx.state().setItem(8,ItemStack.EMPTY);
    }
    public static boolean start(RaftMachineContext ctx){
        if(!ctx.isInstalled() || !RaftConfig.JUICING.get())return false;var s=ctx.state();
        if(s.getInt("running")!=0 || s.getInt("paused")!=0 || !s.getItem(8).isEmpty() || Batteries.charge(s.getItem(4))<1)return false;
        var holder=recipe(ctx).orElse(null);if(holder==null)return false;var result=holder.value().assemble(input(s));
        if(!(result.getItem() instanceof JuiceDrinkItem) || result.getCount()!=1)return false;
        s.setString("work",holder.id().identifier().toString());s.setItem(7,result.copy());s.setInt("duration",holder.value().duration());s.setInt("progress",0);s.setInt("running",1);s.setInt("changed",0);return true;
    }
    public static boolean serve(RaftMachineContext ctx){
        if(!ctx.isInstalled())return false;var s=ctx.state();var ready=s.getItem(8);
        if(!(ready.getItem() instanceof JuiceDrinkItem) || ready.getCount()!=1 || !s.getItem(5).is(GLASS.get()))return false;
        return RaftMachineTransactions.tryProcess(ctx,Map.of(5,s.getItem(5).copyWithCount(1),8,ready.copy()),Map.of(6,ready.copy()),RaftUtilityCost.NONE);
    }
    @Override public void tick(RaftMachineContext ctx){
        if(!ctx.isInstalled())return;var s=ctx.state();s.setInt("active",0);
        if(!RaftConfig.JUICING.get() || s.getInt("paused")!=0 || s.getInt("running")==0)return;
        var holder=recipe(ctx).orElse(null);
        if(holder==null || !holder.id().identifier().toString().equals(s.getString("work")) || holder.value().duration()!=s.getInt("duration")
                || !ItemStack.matches(holder.value().assemble(input(s)),s.getItem(7))){
            s.setInt("running",0);s.setInt("progress",0);s.setInt("changed",1);s.setItem(7,ItemStack.EMPTY);return;
        }
        var battery=s.getItem(4);if(Batteries.charge(battery)<1 || !s.getItem(8).isEmpty())return;
        int progress=s.getInt("progress")+Math.max(1,(int)Math.round(20*RaftConfig.PROCESSING.get()*RaftConfig.JUICER_SPEED.get()));
        if(progress>=s.getInt("duration")){
            var inputs=new LinkedHashMap<Integer,ItemStack>();for(int i=0;i<4;i++)inputs.put(i,s.getItem(i).copyWithCount(1));
            if(!RaftMachineTransactions.tryProcess(ctx,inputs,Map.of(8,s.getItem(7).copy()),RaftUtilityCost.NONE))return;
            s.setItem(7,ItemStack.EMPTY);s.setInt("running",0);s.setInt("progress",0);
        }else s.setInt("progress",progress);
        Batteries.setCharge(battery,Batteries.charge(battery)-1);s.setInt("active",1);s.setChanged();
    }
    @Override public InteractionResult use(RaftMachineContext ctx,Player player,InteractionHand hand){
        if(player.isSpectator() || !ctx.valid(player))return InteractionResult.FAIL;
        RaftMachineMenus.openCustom(ctx,player,Component.translatable("item.raft_ocean.raft_juicer"),(id,inventory,c)->new dev.raftocean.menu.JuicerMenu(id,inventory,c));return InteractionResult.SUCCESS_SERVER;
    }
    @Override public RaftMachineView view(RaftMachineContext c){var s=c.state();return new RaftMachineView(s.getInt("active")!=0,s.getInt("progress"),s.getInt("duration"),s.getItem(8).isEmpty()?0:1);}
    @Override public net.minecraft.world.level.block.state.BlockState visual(RaftBuildable type,RaftMachineView view){return (view.variant()==1?READY:view.active()?ACTIVE:VISUAL).get().defaultBlockState();}
    private RaftJuicer() { }
}
