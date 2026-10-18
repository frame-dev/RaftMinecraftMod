package dev.raftocean.progression;

import com.mojang.authlib.GameProfile;
import dev.raftocean.entity.*;
import dev.raftocean.menu.*;
import dev.raftocean.registry.ModContent;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Native transactions, real crafting result gates, persistence and moving-station reach. */
public final class ResearchCheck {
    private static int checks;
    private ResearchCheck() { }
    private static void require(boolean condition, String message) {
        if (!condition) { throw new IllegalStateException(message); } checks++;
    }
    public static int run(ServerLevel level) {
        checks=StorySchematicCheck.run(level);
        var player=new NativeResearchPlayer(level);
        var other=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"ResearchOther"));
        var raft=new RaftEntity(ModContent.RAFT.get(),level);
        var pos=new BlockPos(-1,1,0);
        var structure=new RaftStructure();
        require(structure.add(pos,new RaftPiece(RaftPiece.Kind.RESEARCH_TABLE,0,false),RaftLayout.starter()),"Research bench support");
        require(structure.add(new BlockPos(1,1,0),new RaftPiece(RaftPiece.Kind.CRAFTING_TABLE,0,false),RaftLayout.starter()),"Crafting bench support");
        var seed=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());seed.putString("Structure",structure.encode());
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(8,level.getMaxY()+20,8);player.setPos(raft.position());
        var menu=new ResearchMenu(1,player.getInventory(),raft,pos);
        boolean enabled=dev.raftocean.config.RaftConfig.RESEARCH.get();
        try {
            dev.raftocean.config.RaftConfig.RESEARCH.set(true);
            var starterGrid=player.inventoryMenu;
            starterGrid.getSlot(1).set(new ItemStack(Items.BREAD,2));
            require(starterGrid.getSlot(0).getItem().is(dev.raftocean.survival.SurvivalContent.BAIT.get()) && starterGrid.getSlot(0).getItem().getCount()==8,
                    "Bread crafts eight Crumb Bait in native inventory before any research");
            starterGrid.clicked(0,1,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
            require(starterGrid.getCarried().is(dev.raftocean.survival.SurvivalContent.BAIT.get()) && starterGrid.getCarried().getCount()==8 && starterGrid.getSlot(1).getItem().getCount()==1,
                    "Native right-click crafts one bait batch using exactly one bread");
            starterGrid.setCarried(ItemStack.EMPTY);starterGrid.getSlot(1).set(ItemStack.EMPTY);
            var starterTable=new RaftCraftingMenu(10,player.getInventory(),raft,new BlockPos(1,1,0));
            starterTable.getSlot(9).set(new ItemStack(Items.BREAD));
            require(starterTable.getSlot(0).getItem().is(dev.raftocean.survival.SurvivalContent.BAIT.get()) && starterTable.getSlot(0).getItem().getCount()==8,
                    "Bread crafts Crumb Bait anywhere in the mounted 3x3 grid before research");
            starterTable.getSlot(9).set(ItemStack.EMPTY);
            mountingChecks(player,starterGrid,starterTable);
            discoveryChecks(player);
            require(Research.blockedProject(player,new ItemStack(dev.raftocean.survival.SurvivalContent.FISHING_ROD.get()))>=0,
                    "Advanced baited rod retains its Bait preparation research requirement");
            require(menu.stillValid(player) && menu.slots.size()==37,"Mounted research menu has a protected temporary sample slot");
            require(!menu.clickMenuButton(player,-1) && !menu.clickMenuButton(player,Research.TOPICS.length),"Invalid learn buttons are refused");
            require(!menu.clickMenuButton(other,0),"Another player cannot research through this owner's menu");
            player.getInventory().setItem(0,new ItemStack(Items.IRON_INGOT,2));
            require(!menu.clickMenuButton(player,1) && player.getInventory().getItem(0).getCount()==2,"Missing prerequisites cannot consume samples");
            Item wire=dev.raftocean.survival.CraftingExpansion.item("copper_wire");
            var inventoryMenu=player.inventoryMenu;
            inventoryMenu.getSlot(1).set(new ItemStack(Items.COPPER_INGOT));
            inventoryMenu.getSlot(2).set(new ItemStack(ModContent.PLANT_FIBER.get()));
            require(inventoryMenu.getSlot(0).getItem().isEmpty(),"Native 2x2 crafting hides an unresearched output");
            require(inventoryMenu.quickMoveStack(player,0).isEmpty() && inventoryMenu.getSlot(1).getItem().getCount()==1,
                    "Shift crafting a locked recipe cannot consume ingredients");
            require(!menu.clickMenuButton(player,ResearchMenu.STUDY_SAMPLE),"Empty sample cannot be studied");
            menu.getSlot(0).set(new ItemStack(Items.DIAMOND,2));
            require(!menu.clickMenuButton(player,ResearchMenu.STUDY_SAMPLE) && menu.getSlot(0).getItem().getCount()==2,"Unsupported material is not consumed");
            for(int material=0;material<ResearchCatalog.MATERIALS.length;material++){
                String id=ResearchCatalog.MATERIALS[material].item();
                if(id.equals("#minecraft:planks")){id="minecraft:oak_planks";}
                if(id.equals("#minecraft:coals")){id="minecraft:coal";}
                Item sample=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
                require(sample!=null && sample!=Items.AIR,"Research material exists: "+id);
                int cost=Research.sampleCost(material);
                menu.getSlot(0).set(new ItemStack(sample,cost-1));
                require(menu.inputState()==3 && !menu.clickMenuButton(player,ResearchMenu.STUDY_SAMPLE)
                        && menu.getSlot(0).getItem().getCount()==cost-1 && !Research.materialKnown(player,material),
                        "Incomplete research batch spends nothing and stays unknown: "+id);
                menu.getSlot(0).set(new ItemStack(sample,cost+1));
                require(menu.clickMenuButton(player,ResearchMenu.STUDY_SAMPLE) && menu.getSlot(0).getItem().getCount()==1,"Material research consumes exactly its declared batch");
                require(!menu.clickMenuButton(player,ResearchMenu.STUDY_SAMPLE) && menu.getSlot(0).getItem().getCount()==1,"Previously studied material is never consumed again");
                require(Research.materialKnown(player,material) && menu.materialKnown(material),"Material checklist is synchronized");
            }
            int engineTopic=StorySchematicCheck.topic("engine");
            require(menu.state(engineTopic)==3 && !menu.clickMenuButton(player,engineTopic)
                    && menu.getSlot(0).getItem().getCount()==1,"Real research menu refuses forged learn clicks without a schematic");
            player.getInventory().setItem(16,new ItemStack(StorySchematics.item(1)));player.getInventory().tick();
            require(menu.state(engineTopic)==1 && (player.getData(Research.KNOWLEDGE)&1<<engineTopic)==0,
                    "Automatic discovery updates the live menu without learning its recipe");
            player.getInventory().setItem(16,ItemStack.EMPTY);
            require(Research.state(player,1)==1 && inventoryMenu.getSlot(0).getItem().isEmpty(),"Researching materials alone does not learn recipes");
            require(menu.clickMenuButton(player,0) && !menu.clickMenuButton(player,0),"Learning a recipe is explicit and cannot repeat");
            require(menu.clickMenuButton(player,1) && player.getInventory().getItem(0).getCount()==2,"Learning costs no additional ingredients");
            inventoryMenu.getSlot(2).set(inventoryMenu.getSlot(2).getItem().copy());
            require(inventoryMenu.getSlot(0).getItem().is(wire) && inventoryMenu.getSlot(0).getItem().getCount()==4,"Native output becomes available after research");
            inventoryMenu.quickMoveStack(player,0);
            require(player.getInventory().countItem(wire)==4 && inventoryMenu.getSlot(1).getItem().isEmpty()
                    && inventoryMenu.getSlot(2).getItem().isEmpty(),"Unlocked native crafting consumes once and returns all output");
            require(other.getData(Research.KNOWLEDGE)==0 && !Research.allowed(other,new ItemStack(wire)),"Research remains private to each multiplayer user");
            var crafting=new RaftCraftingMenu(2,other.getInventory(),raft,new BlockPos(1,1,0));other.setPos(raft.position());
            crafting.getSlot(1).set(new ItemStack(Items.COPPER_INGOT));crafting.getSlot(2).set(new ItemStack(ModContent.PLANT_FIBER.get()));
            require(crafting.getSlot(0).getItem().isEmpty(),"Mounted 3x3 crafting uses the same research gate");
            other.getAbilities().instabuild=true;crafting.getSlot(2).set(crafting.getSlot(2).getItem().copy());
            require(crafting.getSlot(0).getItem().is(wire),"Creative crafting bypasses research");other.getAbilities().instabuild=false;
            dev.raftocean.config.RaftConfig.RESEARCH.set(false);crafting.getSlot(2).set(crafting.getSlot(2).getItem().copy());
            require(crafting.getSlot(0).getItem().is(wire),"Synced config disables crafting restrictions");dev.raftocean.config.RaftConfig.RESEARCH.set(true);
            player.setData(StorySchematics.DISCOVERED,StorySchematics.ALL);
            for(int topic=2;topic<Research.TOPICS.length;topic++){
                require(Research.state(player,topic)==0 || Research.state(player,topic)==1,"Prerequisite graph can be traversed");
                require(menu.clickMenuButton(player,topic) && menu.getSlot(0).getItem().getCount()==1,"Projects reuse known materials without consuming them");
                var advancement=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("raft_ocean","research/"+Research.TOPICS[topic].id()));
                require(advancement!=null && player.getAdvancements().getOrStartProgress(advancement).isDone(),"Research awards the native advancement");
            }
            require(player.getData(Research.KNOWLEDGE)==Research.ALL_PROJECTS,"All recipe projects unlock");
            var saved=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());player.saveWithoutId(saved);
            other.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),saved.buildResult()));
            require(other.getData(Research.KNOWLEDGE)==Research.ALL_PROJECTS && other.getData(Research.MATERIALS)==Research.ALL_MATERIALS,"Projects and materials survive native player serialization");
            var legacy=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"LegacyResearch"));legacy.setData(Research.KNOWLEDGE,63);
            for(int topic=0;topic<Research.TOPICS.length;topic++){
                if(Research.TOPICS[topic].legacy()>=0){require(Research.state(legacy,topic)==2,"Old six-topic knowledge is preserved in the new project list");}
            }
            require(legacy.getData(Research.SCHEMA)==1,"Legacy migration runs once");
            var previous=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"PreviousResearch"));
            previous.setData(Research.SCHEMA,1);
            previous.setData(Research.MATERIALS,Research.ALL_MATERIALS);
            previous.setData(Research.KNOWLEDGE,(1<<24)-1);
            for(int topic=0;topic<Research.TOPICS.length;topic++){
                require(Research.state(previous,topic)==(topic<24?2:StorySchematics.required(topic)>=0?3:1),"Saved 24-project progress preserves old unlocks and leaves appended projects ready to learn");
            }
            require(previous.getData(Research.KNOWLEDGE)==(1<<24)-1,"Opening expanded research does not auto-learn new projects");

            var delta=new Vec3(30,.01,-25);raft.setPos(raft.position().add(delta));player.setPos(player.position().add(delta));
            require(menu.stillValid(player),"Research menu follows its moving hull");
            player.setPos(player.position().add(12,0,0));
            require(!menu.stillValid(player) && !menu.clickMenuButton(player,0),"Leaving table range invalidates study");
            player.setPos(raft.position());
            seed=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());seed.putString("Structure","");
            raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
            require(!menu.stillValid(player),"Removed research table closes its menu");
            int remaining=player.getInventory().countItem(dev.raftocean.survival.CraftingExpansion.item("binding_paste"));
            menu.removed(player);
            require(menu.getSlot(0).getItem().isEmpty() && player.getInventory().countItem(dev.raftocean.survival.CraftingExpansion.item("binding_paste"))==remaining+1,"Closing a removed table returns the unconsumed sample");
            require(player.getData(Research.KNOWLEDGE)==Research.ALL_PROJECTS,"Removing the table cannot delete knowledge");
            commandChecks(level);
            staleResultChecks(level);
            return checks;
        } finally {raft.discard();dev.raftocean.config.RaftConfig.RESEARCH.set(enabled);player.getInventory().clearContent();other.getInventory().clearContent();}
    }
    private static void staleResultChecks(ServerLevel level){
        var player=new NativeResearchPlayer(level);
        player.setData(Research.SCHEMA,1);
        var cell=new BlockPos(1,1,0);var wide=new BlockPos(-1,1,0);
        var raft=new RaftEntity(ModContent.RAFT.get(),level);
        var structure=new RaftStructure();
        require(structure.add(cell,new RaftPiece(RaftPiece.Kind.CRAFTING_TABLE,0,false),RaftLayout.starter()),"Refresh table support");
        require(structure.add(wide,new RaftPiece(RaftPiece.Kind.WIDE_CRAFTING_TABLE,0,false),RaftLayout.starter()),"Refresh wide table support");
        var seed=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());seed.putString("Structure",structure.encode());
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(8.5,level.getMaxY()-8,8.5);player.setPos(raft.position());
        var tablePos=new BlockPos(12,level.getMaxY()-8,8);level.getChunkAt(tablePos);
        var oldBlock=level.getBlockState(tablePos);
        var menus=java.util.List.of(player.inventoryMenu,new RaftCraftingMenu(21,player.getInventory(),raft,cell),
                new RaftCraftingMenu(22,player.getInventory(),raft,wide),
                new net.minecraft.world.inventory.CraftingMenu(23,player.getInventory(),net.minecraft.world.inventory.ContainerLevelAccess.create(level,tablePos)));
        boolean enabled=dev.raftocean.config.RaftConfig.RESEARCH.get();
        try{
            level.setBlock(tablePos,net.minecraft.world.level.block.Blocks.CRAFTING_TABLE.defaultBlockState(),3);
            dev.raftocean.config.RaftConfig.RESEARCH.set(true);
            for(var menu:menus){
                player.containerMenu=menu;
                for(boolean shift:new boolean[]{false,true}){
                    player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);
                    menu.getSlot(1).set(new ItemStack(Items.CHEST,2));
                    require(!menu.getSlot(0).getItem().isEmpty(),"Stale-result fixture begins unlocked");
                    player.setData(Research.KNOWLEDGE,0);
                    if(shift){require(menu.quickMoveStack(player,0).isEmpty(),"Shift craft refuses a result whose research was revoked without an input edit");}
                    else{menu.clicked(0,0,net.minecraft.world.inventory.ContainerInput.PICKUP,player);}
                    require(menu.getSlot(0).getItem().isEmpty() && menu.getCarried().isEmpty()
                            && menu.getSlot(1).getItem().getCount()==2 && player.getInventory().isEmpty(),
                            "Stale research result cannot consume ingredients or yield items in any crafting menu");
                }
                ResearchRecipes.reconcile(player,true);
                player.setData(Research.MATERIALS,Research.ALL_MATERIALS);
                int storage=java.util.stream.IntStream.range(0,Research.TOPICS.length).filter(i->Research.TOPICS[i].id().equals("storage")).findFirst().orElseThrow();
                require(Research.study(player,storage) && !menu.getSlot(0).getItem().isEmpty(),"Learning refreshes an existing result immediately without touching its inputs");
                player.setData(Research.KNOWLEDGE,0);ResearchRecipes.reconcile(player,true);
                require(menu.getSlot(0).getItem().isEmpty(),"Reconciliation clears open and inventory results after a research reset");
                player.getAbilities().instabuild=true;ResearchRecipes.reconcile(player,true);
                require(!menu.getSlot(0).getItem().isEmpty(),"Creative result appears without an input edit");
                player.getAbilities().instabuild=false;
                menu.clicked(0,1,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
                require(menu.getCarried().isEmpty() && menu.getSlot(1).getItem().getCount()==2,"Leaving Creative cannot take its stale research result");
                dev.raftocean.config.RaftConfig.RESEARCH.set(false);ResearchRecipes.reconcile(player,true);
                require(!menu.getSlot(0).getItem().isEmpty(),"Disabling research immediately refreshes an existing grid");
                dev.raftocean.config.RaftConfig.RESEARCH.set(true);
                require(menu.quickMoveStack(player,0).isEmpty() && menu.getSlot(1).getItem().getCount()==2,"Reenabling research blocks a stale result before the next reconciliation tick");
                menu.getSlot(1).set(ItemStack.EMPTY);
            }
        }finally{
            player.containerMenu=player.inventoryMenu;player.getAbilities().instabuild=false;
            dev.raftocean.config.RaftConfig.RESEARCH.set(enabled);
            for(var menu:menus)for(int slot=1;slot<=menu.getGridWidth()*menu.getGridHeight();slot++)menu.getSlot(slot).set(ItemStack.EMPTY);
            player.getInventory().clearContent();player.discard();raft.discard();level.setBlock(tablePos,oldBlock,3);
        }
    }
    private static void commandChecks(ServerLevel level){
        var player=new NativeResearchPlayer(level);
        player.setData(Research.SCHEMA,1);
        player.getInventory().setItem(0,new ItemStack(Items.IRON_INGOT,2));
        player.inventoryMenu.getSlot(4).set(new ItemStack(Items.CHEST,2));
        require(player.inventoryMenu.getSlot(0).getItem().isEmpty(),"Command fixture begins with a locked chest conversion");
        var source=level.getServer().createCommandSourceStack().withEntity(player);
        var dispatcher=level.getServer().getCommands().getDispatcher();
        try {
            require(dispatcher.execute("raft research unlock",source)==1,"Research unlock command targets its executing player");
            require(player.getData(Research.KNOWLEDGE)==Research.ALL_PROJECTS && player.getData(Research.MATERIALS)==Research.ALL_MATERIALS,
                    "Command learns all projects and materials");
            require(player.inventoryMenu.getSlot(0).getItem().is(ModContent.structuralItem(RaftPiece.Kind.CHEST))
                    && player.inventoryMenu.getSlot(4).getItem().getCount()==2,
                    "Command refreshes an existing grid immediately without consuming inputs");
            require(!player.getAbilities().instabuild && player.getInventory().getItem(0).getCount()==2,
                    "Command keeps Survival mode and consumes no samples");
            for(var holder:level.recipeAccess().getRecipes())if(ResearchRecipes.gated(holder))
                require(player.getRecipeBook().contains(holder.id()),"Command discovers research recipe: "+holder.id());
            for(var topic:Research.TOPICS){
                var advancement=level.getServer().getAdvancements().get(Identifier.fromNamespaceAndPath("raft_ocean","research/"+topic.id()));
                require(advancement!=null && player.getAdvancements().getOrStartProgress(advancement).isDone(),"Command awards research advancement: "+topic.id());
            }
            require(dispatcher.execute("raft research unlock",source)==1 && player.getInventory().getItem(0).getCount()==2,
                    "Repeated unlock is safe and consumes nothing");
            var parse=dispatcher.parse("raft research unlock @a",level.getServer().createCommandSourceStack());
            require(parse.getExceptions().isEmpty() && !parse.getReader().canRead(),"Console accepts online-player selectors");
        }catch(com.mojang.brigadier.exceptions.CommandSyntaxException error){throw new IllegalStateException("Research command failed",error);}
        finally {player.inventoryMenu.getSlot(4).set(ItemStack.EMPTY);player.getInventory().clearContent();player.discard();}
    }
    private static void mountingChecks(FakePlayer player,net.minecraft.world.inventory.AbstractCraftingMenu inventory,
            net.minecraft.world.inventory.AbstractCraftingMenu table) {
        var conversions=java.util.Map.ofEntries(
                java.util.Map.entry(Items.CHEST,RaftPiece.Kind.CHEST),
                java.util.Map.entry(Items.BARREL,RaftPiece.Kind.BARREL),
                java.util.Map.entry(Items.CRAFTING_TABLE,RaftPiece.Kind.CRAFTING_TABLE),
                java.util.Map.entry(Items.STONECUTTER,RaftPiece.Kind.STONECUTTER),
                java.util.Map.entry(Items.SMITHING_TABLE,RaftPiece.Kind.SMITHING_TABLE),
                java.util.Map.entry(Items.GRINDSTONE,RaftPiece.Kind.GRINDSTONE),
                java.util.Map.entry(Items.LOOM,RaftPiece.Kind.LOOM),
                java.util.Map.entry(Items.CARTOGRAPHY_TABLE,RaftPiece.Kind.CARTOGRAPHY_TABLE),
                java.util.Map.entry(Items.ANVIL,RaftPiece.Kind.ANVIL),
                java.util.Map.entry(Items.ENCHANTING_TABLE,RaftPiece.Kind.ENCHANTING_TABLE),
                java.util.Map.entry(Items.ENDER_CHEST,RaftPiece.Kind.ENDER_CHEST),
                java.util.Map.entry(Items.BOOKSHELF,RaftPiece.Kind.BOOKSHELF));
        require(player.getData(Research.KNOWLEDGE)==0,"Vanilla mounts tested before any research");
        for(var conversion:conversions.entrySet())for(var menu:java.util.List.of(inventory,table)) {
            int slot=menu==inventory?4:9;var output=ModContent.structuralItem(conversion.getValue());
            menu.getSlot(slot).set(new ItemStack(conversion.getKey(),2));
            if(!Research.allowed(player,new ItemStack(output))){
                require(menu.getSlot(0).getItem().isEmpty(),"Unresearched conversion gives no result: "+conversion.getValue());
                menu.clicked(0,1,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
                menu.quickMoveStack(player,0);
                require(menu.getCarried().isEmpty() && menu.getSlot(slot).getItem().getCount()==2,
                        "Hidden conversion cannot consume inputs: "+conversion.getValue());
            }
            player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);
            menu.getSlot(slot).set(menu.getSlot(slot).getItem().copy());
            require(menu.getSlot(0).getItem().is(output) && menu.getSlot(0).getItem().getCount()==1,
                    "Vanilla block converts after research: "+conversion.getValue());
            menu.clicked(0,1,net.minecraft.world.inventory.ContainerInput.PICKUP,player);
            require(menu.getCarried().is(output) && menu.getCarried().getCount()==1 && menu.getSlot(slot).getItem().getCount()==1,
                    "Right-click mount consumes one vanilla block: "+conversion.getValue());
            menu.setCarried(ItemStack.EMPTY);player.getInventory().clearContent();
            menu.quickMoveStack(player,0);
            require(menu.getSlot(slot).getItem().isEmpty() && player.getInventory().countItem(output)==1,
                    "Shift-click mount returns exactly one raft item: "+conversion.getValue());
            player.getInventory().clearContent();
            player.setData(Research.KNOWLEDGE,0);
        }
        var brace=new ItemStack(ModContent.TIMBER_BRACE.get());
        var fastener=new ItemStack(ModContent.SCRAP_FASTENERS.get());
        var plank=new ItemStack(Items.OAK_PLANKS);
        var materialGrid=net.minecraft.world.item.crafting.CraftingInput.of(3,3,
                java.util.List.of(plank,plank,plank,brace,fastener,brace,plank,plank,plank));
        require(Research.blockedCraftingProject(player,new ItemStack(ModContent.structuralItem(RaftPiece.Kind.CHEST)),materialGrid)>=0,
                "Crafting storage from materials still needs research");
        for(int i=0;i<materialGrid.size();i++)table.getSlot(i+1).set(materialGrid.getItem(i).copy());
        require(table.getSlot(0).getItem().isEmpty(),"Native materials-based chest recipe remains locked");
        for(int slot=1;slot<=9;slot++)table.getSlot(slot).set(ItemStack.EMPTY);
    }
    private static void discoveryChecks(NativeResearchPlayer player){
        ResearchRecipes.reconcile(player,true);
        var recipes=player.level().recipeAccess().getRecipes();
        var gated=recipes.stream().filter(ResearchRecipes::gated).toList();
        require(gated.size()>30,"Research recipe discovery covers the gated catalog");
        player.getRecipeBook().addRecipes(recipes,player);
        for(var holder:gated)require(!player.getRecipeBook().contains(holder.id()),"Ingredient discovery cannot reveal locked recipe: "+holder.id());
        var chest=gated.stream().filter(h->h.id().identifier().toString().equals("raft_ocean:raft_chest")).findFirst().orElseThrow();
        player.getRecipeBook().add(chest.id());
        player.getRecipeBook().sendInitialRecipeBook(player);
        require(!player.getRecipeBook().contains(chest.id()),"Login removes a stale unresearched recipe from saved book");
        player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);
        ResearchRecipes.reconcile(player,true);
        for(var holder:gated)require(player.getRecipeBook().contains(holder.id()),"Learned research discovers recipe: "+holder.id());
        player.setData(Research.KNOWLEDGE,0);
        ResearchRecipes.reconcile(player,true);
        require(!player.getRecipeBook().contains(chest.id()),"Reset research hides previously discovered recipes");
        dev.raftocean.config.RaftConfig.RESEARCH.set(false);ResearchRecipes.reconcile(player,true);
        require(player.getRecipeBook().contains(chest.id()),"Disabled research reveals recipes");
        dev.raftocean.config.RaftConfig.RESEARCH.set(true);ResearchRecipes.reconcile(player,true);
        require(!player.getRecipeBook().contains(chest.id()),"Reenabled research reapplies discovery gate");
        player.getAbilities().instabuild=true;ResearchRecipes.reconcile(player,true);
        require(player.getRecipeBook().contains(chest.id()),"Creative reveals research recipes");
        player.getAbilities().instabuild=false;ResearchRecipes.reconcile(player,true);
        require(!player.getRecipeBook().contains(chest.id()),"Returning to Survival hides unfinished research");
    }
    /** FakePlayer normally disables advancement awards; use the real native tracker here. */
    private static final class NativeResearchPlayer extends FakePlayer {
        private net.minecraft.server.PlayerAdvancements nativeAdvancements;
        private NativeResearchPlayer(ServerLevel level) {
            super(level,new GameProfile(UUID.randomUUID(),"ResearchCheck"));
            var server=level.getServer();
            nativeAdvancements=new net.minecraft.server.PlayerAdvancements(server.getFixerUpper(),server.getPlayerList(),
                    server.getAdvancements(),java.nio.file.Path.of("build","research-check-"+getUUID()+".json"),this);
        }
        @Override public net.minecraft.server.PlayerAdvancements getAdvancements() {
            return nativeAdvancements==null?super.getAdvancements():nativeAdvancements;
        }
    }
}
