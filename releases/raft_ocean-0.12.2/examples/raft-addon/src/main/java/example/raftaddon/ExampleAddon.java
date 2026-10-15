package example.raftaddon;

import dev.raftocean.api.RaftBuildable;
import dev.raftocean.api.RaftBuildables;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExampleAddon.ID)
public final class ExampleAddon {
    public static final String ID="raft_example";
    private static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(ID);
    private static final java.util.function.Supplier<Block> CRATE=BLOCKS.registerSimpleBlock("work_crate");
    private static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.BlockItem> CRATE_ITEM=ITEMS.registerSimpleBlockItem("work_crate",CRATE);
    private static final java.util.function.Supplier<Block> DRYER=BLOCKS.registerSimpleBlock("kelp_dryer");
    private static final java.util.function.Supplier<Block> DRYER_ACTIVE=BLOCKS.registerSimpleBlock("kelp_dryer_active");
    private static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.BlockItem> DRYER_ITEM=ITEMS.registerSimpleBlockItem("kelp_dryer",DRYER);
    public ExampleAddon(IEventBus bus) {
        BLOCKS.register(bus);ITEMS.register(bus);bus.addListener(this::setup);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::checkExample);
    }
    private void checkExample(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        if(Boolean.getBoolean("raftOcean.carrySmoke"))
            com.mojang.logging.LogUtils.getLogger().info("Raft example: {} native dryer assertions passed.",ExampleMachineCheck.run(event.getServer().overworld()));
    }
    private void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            var machineBox=new AABB(-.5,0,-.5,.5,.875,.5);
            RaftBuildables.register(new RaftBuildable(Identifier.fromNamespaceAndPath(ID,"kelp_dryer"),
                    ()->DRYER_ITEM.get(),DRYER,1,1,1,10,true,List.of(machineBox),List.of(machineBox),
                    (raft,anchor,player,hand)->InteractionResult.PASS),new KelpDryer(DRYER,DRYER_ACTIVE));
            var box=new AABB(-.5,0,-.5,1.5,.75,.5);
            RaftBuildables.register(new RaftBuildable(Identifier.fromNamespaceAndPath(ID,"work_crate"),
                    ()->CRATE_ITEM.get(),CRATE,2,1,1,8,true,List.of(box),List.of(box),
                    (raft,anchor,player,hand)->{
                        player.sendOverlayMessage(net.minecraft.network.chat.Component.translatable("raft_example.crate.message"));
                        return InteractionResult.SUCCESS_SERVER;
                    }));
        });
    }
}
