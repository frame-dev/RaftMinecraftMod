package dev.raftocean.survival;

import java.util.ArrayList;
import java.util.List;
import dev.raftocean.entity.RaftPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Loaded recipes and animation snapshots exercise real server processing transactions. */
public final class WorkshopCheck {
    private static final BlockPos CELL=new BlockPos(1,1,0);
    private WorkshopCheck() { }
    public static int run(ServerLevel level,FakePlayer player) {
        int checks=0;
        String[] names={"damp_brick","fired_brick","metal_plate","copper_wire","binding_paste","canvas_roll","reinforced_brace",
                "root_skewer","fish_skewer","roasted_roots","charred_fish_skewer","garden_broth","mushroom_chowder","sea_grain_bowl","hearty_fish_pot",
                "foundation_from_braces","sail_from_canvas","engine_from_plates","anchor_from_plates","wheel_from_braces","purifier_from_bricks",
                "smelter_from_bricks","grill_from_plates","pot_from_plates","tree_planter_from_braces","bed_from_canvas","net_from_braces","chest_from_braces","roof_from_canvas"};
        for(String name:names) {
            require(level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("raft_ocean",name))).isPresent(),"Workshop recipe loaded: "+name);checks++;
        }
        checks+=craft(level,"damp_brick",2,Items.CLAY_BALL,Items.CLAY_BALL,Items.SAND);
        checks+=craft(level,"metal_plate",2,Items.IRON_INGOT,Items.IRON_INGOT,dev.raftocean.registry.ModContent.SCRAP_FASTENERS.get());
        checks+=craft(level,"copper_wire",4,Items.COPPER_INGOT,dev.raftocean.registry.ModContent.PLANT_FIBER.get());
        checks+=craft(level,"binding_paste",2,Items.WHEAT,Items.WHEAT,dev.raftocean.registry.ModContent.DRIFT_LEAF.get());
        checks+=craft(level,"root_skewer",1,Items.CARROT,Items.POTATO,Items.STICK);
        checks+=craft(level,"fish_skewer",1,Items.SALMON,Items.STICK);
        String[] meals={"garden_broth","mushroom_chowder","sea_grain_bowl","hearty_fish_pot"};
        Item[] bases={Items.WHEAT,Items.BROWN_MUSHROOM,Items.DRIED_KELP,Items.COOKED_COD};
        Item[] vegetables={Items.CARROT,Items.POTATO,Items.BEETROOT,Items.POTATO};
        for(int i=0;i<meals.length;i++) {
            var raft=SurvivalCheck.create(level,RaftPiece.Kind.COOKING_POT);var inventory=raft.storage(CELL);
            try {
                inventory.setItem(0,new ItemStack(bases[i]));inventory.setItem(1,new ItemStack(vegetables[i]));
                inventory.setItem(2,new ItemStack(SurvivalContent.FRESH_WATER.get()));inventory.setItem(6,new ItemStack(Items.BOWL));
                inventory.setItem(3,new ItemStack(Items.OAK_PLANKS,4));
                var colliders=raft.getCollisionBoxes();
                SurvivalMachines.process(raft,CELL,RaftPiece.Kind.COOKING_POT,level);raft.machines().sync();
                require(raft.machines().at(CELL).working() && raft.machines().at(CELL).progress()>0 && !raft.machines().at(CELL).ready(),"Cooking drives shared working animation");checks++;
                for(int step=0;step<40;step++){SurvivalMachines.process(raft,CELL,RaftPiece.Kind.COOKING_POT,level);}
                raft.machines().sync();
                require(inventory.getItem(4).is(CraftingExpansion.item(meals[i])) && inventory.getItem(5).is(SurvivalContent.EMPTY_CUP.get())
                        && inventory.getItem(0).isEmpty() && inventory.getItem(1).isEmpty() && inventory.getItem(6).isEmpty(),"Pot cooks original meal and returns physical cup: "+meals[i]);checks++;
                require(!raft.machines().at(CELL).working() && raft.machines().at(CELL).ready()
                        && colliders.equals(raft.getCollisionBoxes()),"Ready/idle snapshots do not change raft collision");checks++;
                var food=inventory.getItem(4).copy();
                require(food.finishUsingItem(level,player).is(Items.BOWL),"Eating a bowl meal returns its bowl");checks++;
            } finally {inventory.clearContent();raft.discard();}
        }
        for(String[] pair:new String[][]{{"damp_brick","fired_brick"},{"root_skewer","roasted_roots"},{"fish_skewer","charred_fish_skewer"}}) {
            var kind=pair[0].equals("damp_brick")?RaftPiece.Kind.SMELTER:RaftPiece.Kind.GRILL;
            var raft=SurvivalCheck.create(level,kind);var inventory=raft.storage(CELL);
            try {
                inventory.setItem(0,new ItemStack(CraftingExpansion.item(pair[0])));inventory.setItem(1,new ItemStack(Items.OAK_PLANKS,3));
                for(int tick=0;tick<30;tick++){SurvivalMachines.process(raft,CELL,kind,level);}
                require(inventory.getItem(0).isEmpty() && inventory.getItem(2).is(CraftingExpansion.item(pair[1])),"Native station finishes workshop processing: "+pair[1]);checks++;
            } finally {inventory.clearContent();raft.discard();}
        }
        var planter=SurvivalCheck.create(level,RaftPiece.Kind.PLANTER);
        try {
            var soil=planter.storage(CELL);soil.setItem(0,new ItemStack(Items.WHEAT_SEEDS));soil.setItem(1,new ItemStack(SurvivalContent.PLANTING_MIX.get()));soil.cleanWater=1;
            for(int tick=0;tick<60;tick++){SurvivalMachines.process(planter,CELL,RaftPiece.Kind.PLANTER,level);}
            planter.machines().sync();
            require(planter.machines().at(CELL).crop()==1 && planter.machines().at(CELL).progress()==500 && planter.machines().at(CELL).working(),"Wheat growth phase is synchronized without exposing inventory");checks++;
            var parser=new MachineAnimationState(planter);parser.accept("1,1,0,99999,1,8,-3,1,99");
            require(parser.at(CELL).progress()==1000 && parser.at(CELL).rawWater()==4 && parser.at(CELL).cleanWater()==0 && parser.at(CELL).crop()==6,"Animation snapshot numbers are bounded");checks++;
            parser.accept("bad,data;1,1,0,100,0,0,1,0,2");
            require(parser.at(CELL).crop()==2 && !parser.at(CELL).working(),"Malformed snapshot entries do not corrupt valid cells");checks++;
        } finally {planter.storage(CELL).clearContent();planter.discard();}
        var sync=new net.neoforged.neoforge.event.OnDatapackSyncEvent(level.getServer().getPlayerList(),null);
        ProcessingRecipe.onSync(sync);
        require(sync.getRecipeTypesToSend().size()==8 && sync.getRecipeTypesToSend().contains(ProcessingRecipe.type(RaftPiece.Kind.BIOREFINER))
                && sync.getRecipeTypesToSend().contains(ProcessingRecipe.type(RaftPiece.Kind.BASIC_PURIFIER))
                && sync.getRecipeTypesToSend().contains(ProcessingRecipe.type(RaftPiece.Kind.DRYING_RACK)),"All custom machine recipe types synchronize without JEI on the server");checks++;
        for(var crop:List.of(Items.SUGAR_CANE,Items.KELP)){
            var raft=SurvivalCheck.create(level,RaftPiece.Kind.PLANTER);var soil=raft.storage(CELL);
            try{
                player.setPos(raft.position().add(0,.26,0));
                var menu=new dev.raftocean.menu.SurvivalMenu(72,player.getInventory(),RaftPiece.Kind.PLANTER,soil);
                require(menu.slots.get(0).mayPlace(new ItemStack(crop)),"Planter accepts renewable cane/kelp input");checks++;
                soil.setItem(0,new ItemStack(crop));soil.setItem(1,new ItemStack(SurvivalContent.PLANTING_MIX.get()));
                soil.setItem(4,new ItemStack(SurvivalContent.FRESH_WATER.get()));
                SurvivalMachines.process(raft,CELL,RaftPiece.Kind.PLANTER,level);raft.machines().sync();
                require(raft.machines().at(CELL).working() && raft.machines().at(CELL).crop()==(crop==Items.KELP?6:5),"New plant starts synchronized growth");checks++;
                for(int tick=0;tick<120;tick++){SurvivalMachines.process(raft,CELL,RaftPiece.Kind.PLANTER,level);}
                raft.machines().sync();
                require(soil.getItem(2).is(crop) && soil.getItem(2).getCount()==2 && soil.getItem(3).is(crop)
                        && soil.getItem(3).getCount()==1 && soil.getItem(5).is(SurvivalContent.EMPTY_CUP.get())
                        && soil.getItem(0).isEmpty() && soil.cleanWater==0,"Growth returns harvest, replanting cutting and cup exactly once");checks++;
                require(raft.machines().at(CELL).ready(),"New crop ready state survives processing stop");checks++;
                soil.removeItem(2,2);soil.removeItem(3,1);raft.machines().sync();
                require(raft.machines().at(CELL).crop()==0,"Harvesting clears the crop model");checks++;
            }finally{soil.clearContent();raft.discard();}
        }
        var oldMenu=player.containerMenu;
        for(var kind:List.of(RaftPiece.Kind.CHEST,RaftPiece.Kind.BARREL,RaftPiece.Kind.CRAFTING_TABLE,
                RaftPiece.Kind.STONECUTTER,RaftPiece.Kind.GRINDSTONE,RaftPiece.Kind.LOOM,RaftPiece.Kind.SMITHING_TABLE,RaftPiece.Kind.CARTOGRAPHY_TABLE)) {
            var raft=SurvivalCheck.create(level,kind);
            try {
                player.setPos(raft.position().add(0,.26,0));
                var menu=dev.raftocean.menu.RaftStationMenus.create(71,player.getInventory(),raft,CELL,kind);
                player.containerMenu=menu;raft.trackStationUse(player,CELL,menu);raft.machines().sync();
                require(raft.machines().at(CELL).working(),"Open station drives use/open animation: "+kind);checks++;
                player.containerMenu=oldMenu;raft.machines().sync();
                require(!raft.machines().at(CELL).working(),"Closing station stops use/open animation: "+kind);checks++;
            } finally {player.containerMenu=oldMenu;raft.discard();}
        }
        return checks;
    }
    private static int craft(ServerLevel level,String result,int count,Item... ingredients) {
        List<ItemStack> input=new ArrayList<>();for(Item item:ingredients){input.add(new ItemStack(item));}
        while(input.size()<9){input.add(ItemStack.EMPTY);}
        var recipe=level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,CraftingInput.of(3,3,input),level).orElseThrow();
        var stack=recipe.value().assemble(CraftingInput.of(3,3,input));
        require(stack.is(CraftingExpansion.item(result)) && stack.getCount()==count,"Native crafting produces workshop material: "+result);return 1;
    }
    private static void require(boolean condition,String message) {if(!condition){throw new IllegalStateException(message);}}
}
