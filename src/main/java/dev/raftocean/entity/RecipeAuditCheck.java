package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.config.RaftConfig;
import dev.raftocean.menu.RaftCraftingMenu;
import dev.raftocean.progression.Research;
import dev.raftocean.registry.ModContent;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Loaded recipes, placement, tag alternatives, native take/remainders and research locks. */
public final class RecipeAuditCheck {
    private record Definition(Identifier id,CraftingRecipe recipe,int width,int height,List<Optional<Ingredient>> cells,List<ItemStack> fixed) {
        boolean shaped(){return width>0;}
        boolean fits(int side){return shaped()?width<=side && height<=side:cells.size()<=side*side;}
    }
    private static int checks;
    public static int run(ServerLevel level) {
        checks=0;
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"RecipeAudit"));
        player.setData(Research.SCHEMA,1);player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);
        boolean research=RaftConfig.RESEARCH.get();RaftConfig.RESEARCH.set(true);
        var table=new BlockPos(1,1,0);var wide=new BlockPos(-1,1,0);
        var structure=new RaftStructure();var layout=RaftLayout.starter();
        require(structure.add(table,new RaftPiece(RaftPiece.Kind.CRAFTING_TABLE,0,false),layout),"Audit table support");
        require(structure.add(wide,new RaftPiece(RaftPiece.Kind.WIDE_CRAFTING_TABLE,0,false),layout),"Audit wide-table support");
        var raft=new RaftEntity(ModContent.RAFT.get(),level);
        var seed=TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);seed.putString("Structure",structure.encode());
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),seed.buildResult()));
        raft.setPos(8.5,level.getMaxY()-8,8.5);player.setPos(raft.position());
        var tablePos=new BlockPos(12,level.getMaxY()-8,8);
        level.getChunkAt(tablePos);var oldBlock=level.getBlockState(tablePos);
        var menus=new LinkedHashMap<String,AbstractCraftingMenu>();
        menus.put("inventory_2x2",player.inventoryMenu);
        menus.put("raft_3x3",new RaftCraftingMenu(9,player.getInventory(),raft,table));
        menus.put("wide_raft_3x3",new RaftCraftingMenu(10,player.getInventory(),raft,wide));
        menus.put("vanilla_3x3",new CraftingMenu(11,player.getInventory(),ContainerLevelAccess.create(level,tablePos)));
        var report=new ArrayList<String>();report.add("recipe,type,grid_2x2,tag_alternative_cases,placement_cases,assertions,research_locked");
        int mod=0,vanilla=0,small=0,special=0,variants=0,placements=0;
        try {
            level.setBlock(tablePos,Blocks.CRAFTING_TABLE.defaultBlockState(),3);
            for(var entry:menus.entrySet())require(entry.getValue().stillValid(player),"Audit menu reachable: "+entry.getKey());
            var loaded=level.recipeAccess().getRecipes().stream().filter(h->h.value() instanceof CraftingRecipe)
                    .sorted(Comparator.comparing(h->h.id().identifier().toString())).toList();
            var loadedIds=loaded.stream().map(h->h.id().identifier()).collect(java.util.stream.Collectors.toSet());
            for(var resource:level.getServer().getResourceManager().listResources("recipe",id->id.getNamespace().equals("raft_ocean") && id.getPath().endsWith(".json")).entrySet()) {
                try(var reader=resource.getValue().openAsReader()) {
                    var json=com.google.gson.JsonParser.parseReader(reader);
                    var decoded=Recipe.DIRECT_CODEC.parse(level.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE),json).getOrThrow();
                    if(decoded instanceof CraftingRecipe) {
                        String path=resource.getKey().getPath();
                        var id=Identifier.fromNamespaceAndPath("raft_ocean",path.substring("recipe/".length(),path.length()-".json".length()));
                        require(loadedIds.contains(id),"Declared crafting recipe not loaded: "+id);
                    }
                }
            }
            for(var holder:loaded) {
                var recipe=(CraftingRecipe)holder.value();var id=holder.id().identifier();
                Definition def;
                if(recipe instanceof ShapedRecipe shaped)def=new Definition(id,recipe,shaped.getWidth(),shaped.getHeight(),shaped.getIngredients(),List.of());
                else if(recipe instanceof ShapelessRecipe)def=new Definition(id,recipe,0,0,recipe.placementInfo().ingredients().stream().map(Optional::of).toList(),List.of());
                else {def=dynamic(id,recipe,level);special++;}
                var samples=new ArrayList<ItemStack>();
                if(!def.fixed().isEmpty())samples.addAll(def.fixed());
                else for(var cell:def.cells())samples.add(cell.map(i->alternatives(i,level,id).getFirst()).orElse(ItemStack.EMPTY));
                int variantCount=0,placementCount=0,startChecks=checks;
                require(!recipe.assemble(CraftingInput.of(3,3,grid(def,samples,3,0,0,false,0))).isEmpty(),"Empty crafting output: "+id);
                if(!def.fits(2))require(!recipe.matches(CraftingInput.of(2,2,grid(def,samples,3,0,0,false,0).subList(0,4)),level),"3x3 recipe incorrectly fits 2x2: "+id);
                for(int side:new int[]{2,3}) {
                    if(!def.fits(side))continue;
                    int rows=def.shaped()?side-def.height()+1:1,columns=def.shaped()?side-def.width()+1:1;
                    for(int y=0;y<rows;y++)for(int x=0;x<columns;x++)for(boolean mirror:new boolean[]{false,true}) {
                        validate(level,def,grid(def,samples,side,x,y,mirror,0),side);placementCount++;
                    }
                    if(!def.shaped())for(int shift=1;shift<side*side;shift++) {
                        validate(level,def,grid(def,samples,side,0,0,false,shift),side);placementCount++;
                    }
                }
                for(int i=0;i<def.cells().size();i++)if(def.cells().get(i).isPresent()) {
                    for(var choice:alternatives(def.cells().get(i).get(),level,id)) {
                        var changed=new ArrayList<>(samples);changed.set(i,choice);
                        if(!def.fixed().isEmpty() && !def.recipe().matches(CraftingInput.of(3,3,grid(def,changed,3,0,0,false,0)),level))continue;
                        validate(level,def,grid(def,changed,3,0,0,false,0),3);variantCount++;
                        if(def.fits(2)){validate(level,def,grid(def,changed,2,0,0,false,0),2);variantCount++;}
                    }
                }
                for(var entry:menus.entrySet()) {
                    int side=entry.getKey().equals("inventory_2x2")?2:3;
                    if(!def.fits(side))continue;
                    var grid=grid(def,samples,side,0,0,false,0);
                    for(int action=0;action<3;action++)transaction(player,entry.getValue(),def,grid,side,action,entry.getKey());
                }
                var expected=recipe.assemble(CraftingInput.of(3,3,grid(def,samples,3,0,0,false,0)));
                player.setData(Research.KNOWLEDGE,0);boolean locked=Research.blockedCraftingProject(player,expected,
                        CraftingInput.of(3,3,grid(def,samples,3,0,0,false,0)))>=0;
                for(var entry:menus.entrySet()) {
                    int side=entry.getKey().equals("inventory_2x2")?2:3;
                    if(!def.fits(side))continue;
                    var grid=grid(def,samples,side,0,0,false,0);fill(player,entry.getValue(),grid,side);
                    if(locked) {
                        require(entry.getValue().getSlot(0).getItem().isEmpty(),"Research-locked recipe exposed: "+id+" at "+entry.getKey());
                        entry.getValue().quickMoveStack(player,0);
                        for(int i=0;i<grid.size();i++)require(ItemStack.matches(grid.get(i),entry.getValue().getSlot(i+1).getItem()),"Locked recipe consumed ingredients: "+id);
                    }else require(ItemStack.matches(expected,entry.getValue().getSlot(0).getItem()),"Starter recipe incorrectly locked: "+id+" at "+entry.getKey());
                }
                player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);
                if(id.getNamespace().equals("raft_ocean"))mod++;else vanilla++;
                if(def.fits(2))small++;
                variants+=variantCount;placements+=placementCount;
                report.add(id+","+(def.shaped()?"shaped":"shapeless")+","+def.fits(2)+","+variantCount+","+placementCount+","+(checks-startChecks)+","+locked);
            }
            require(mod>100,"Loaded mod crafting coverage unexpectedly incomplete: "+mod);
            Files.write(Path.of("crafting-audit.csv"),report);
            Files.writeString(Path.of("crafting-audit-summary.txt"),"Crafting recipes: "+(mod+vanilla)+"\nMod: "+mod+"\nVanilla/other: "+vanilla+"\nFits 2x2: "+small+"\nPlacement cases: "+placements+"\nIngredient alternative cases: "+variants+"\nDynamic/special recipe fixtures: "+special+"\nAssertions: "+checks+"\n");
            System.out.println("Raft crafting audit: "+mod+" mod and "+vanilla+" vanilla/other recipes; "+small+" fit 2x2; "+placements+" placements, "+variants+" ingredient alternatives; "+special+" dynamic fixtures.");
        } catch(java.io.IOException error){throw new IllegalStateException("Could not write crafting audit report",error);}
        finally {
            player.setData(Research.KNOWLEDGE,Research.ALL_PROJECTS);
            for(var menu:menus.values())clear(player,menu,menu==player.inventoryMenu?2:3);
            level.setBlock(tablePos,oldBlock,3);RaftConfig.RESEARCH.set(research);raft.discard();player.discard();
        }
        return checks;
    }
    private static List<ItemStack> alternatives(Ingredient ingredient,ServerLevel level,Identifier id) {
        var items=ingredient.items().map(h->new ItemStack(h.value())).filter(s->s.isItemEnabled(level.enabledFeatures()) && ingredient.test(s))
                .sorted(Comparator.comparing(s->BuiltInRegistries.ITEM.getKey(s.getItem()).toString())).toList();
        require(!items.isEmpty(),"Empty or unsupported crafting ingredient: "+id);return items;
    }
    private static List<ItemStack> grid(Definition def,List<ItemStack> samples,int side,int x,int y,boolean mirror,int shift) {
        var grid=new ArrayList<ItemStack>(Collections.nCopies(side*side,ItemStack.EMPTY));
        for(int i=0;i<samples.size();i++) {
            int slot=def.shaped()?(y+i/def.width())*side+x+(mirror?def.width()-1-i%def.width():i%def.width())
                    :Math.floorMod((mirror?samples.size()-1-i:i)+shift,side*side);
            grid.set(slot,samples.get(i).copy());
        }
        return grid;
    }
    private static void validate(ServerLevel level,Definition def,List<ItemStack> grid,int side) {
        var input=CraftingInput.of(side,side,grid);
        require(def.recipe().matches(input,level),"Recipe cannot match its own "+side+"x"+side+" fixture: "+def.id());
        var chosen=level.recipeAccess().getRecipeFor(RecipeType.CRAFTING,input,level);
        require(chosen.isPresent(),"No native recipe selected: "+def.id());
        require(ItemStack.matches(def.recipe().assemble(input),chosen.get().value().assemble(input)),
                "Recipe shadowed: "+def.id()+" by "+chosen.get().id().identifier()+" in "+side+"x"+side+" grid");
    }
    private static void transaction(FakePlayer player,AbstractCraftingMenu menu,Definition def,List<ItemStack> grid,int side,int action,String station) {
        fill(player,menu,grid,side);var positioned=CraftingInput.ofPositioned(side,side,grid);var input=positioned.input();
        var expected=def.recipe().assemble(input);var remainders=def.recipe().getRemainingItems(input);
        String context=def.id()+" at "+station+" action "+action;
        require(ItemStack.matches(expected,menu.getSlot(0).getItem()),"Menu output mismatch: "+context);
        if(action==2) {
            menu.quickMoveStack(player,0);
            require(player.getInventory().countItem(expected.getItem())==expected.getCount(),"Shift result quantity mismatch: "+context);
            for(int slot=0;slot<player.getInventory().getContainerSize();slot++) {
                var received=player.getInventory().getItem(slot);
                if(received.is(expected.getItem()))verifyTaken(def,expected.copyWithCount(received.getCount()),received,player,context);
            }
        } else {
            menu.clicked(0,action,ContainerInput.PICKUP,player);
            if(action==1 && expected.getItem() instanceof BundleItem) {
                require(menu.getCarried().isEmpty() && ItemStack.matches(expected,menu.getSlot(0).getItem()),"Native bundle secondary-click changed output: "+context);
                for(int i=0;i<grid.size();i++)require(ItemStack.matches(grid.get(i),menu.getSlot(i+1).getItem()),"Bundle secondary-click consumed ingredients: "+context);
                menu.clicked(0,0,ContainerInput.PICKUP,player);
            }
            verifyTaken(def,expected,menu.getCarried(),player,context);
        }
        var remaining=new ArrayList<ItemStack>(Collections.nCopies(side*side,ItemStack.EMPTY));
        for(int y=0;y<input.height();y++)for(int x=0;x<input.width();x++)remaining.set((y+positioned.top())*side+x+positioned.left(),remainders.get(y*input.width()+x));
        for(int i=0;i<grid.size();i++)require(ItemStack.matches(remaining.get(i),menu.getSlot(i+1).getItem()),"Ingredient consumption/remainder mismatch: "+context+" slot "+i);
    }
    private static void verifyTaken(Definition def,ItemStack expected,ItemStack received,FakePlayer player,String context) {
        if(def.recipe() instanceof MapExtendingRecipe) {
            var source=MapItem.getSavedData(expected,player.level());
            var expanded=MapItem.getSavedData(received,player.level());
            require(source!=null && expanded!=null && expanded.scale==source.scale+1,"Expanded map scale mismatch: "+context);
            expected=expected.copy();
            expected.remove(net.minecraft.core.component.DataComponents.MAP_POST_PROCESSING);
            expected.set(net.minecraft.core.component.DataComponents.MAP_ID,received.get(net.minecraft.core.component.DataComponents.MAP_ID));
        }
        require(ItemStack.matches(expected,received),"Taken result quantity/components mismatch: "+context);
    }
    private static void fill(FakePlayer player,AbstractCraftingMenu menu,List<ItemStack> grid,int side) {
        clear(player,menu,side);for(int i=0;i<grid.size();i++)menu.getSlot(i+1).set(grid.get(i).copy());
    }
    private static Definition dynamic(Identifier id,CraftingRecipe recipe,ServerLevel level) {
        if(recipe instanceof TransmuteRecipe || recipe instanceof DyeRecipe || recipe instanceof ImbueRecipe) {
            var ingredients=recipe.placementInfo().ingredients();
            for(int length=recipe instanceof ImbueRecipe?9:2;length<=ingredients.size();length++) {
                var cells=ingredients.subList(0,length).stream().map(Optional::of).toList();
                var stacks=new ArrayList<ItemStack>();
                for(var ingredient:ingredients.subList(0,length))stacks.add(alternatives(ingredient,level,id).getFirst());
                int width=recipe instanceof ImbueRecipe?3:0;
                var candidate=new Definition(id,recipe,width,width,cells,stacks);
                if(recipe.matches(CraftingInput.of(3,3,grid(candidate,stacks,3,0,0,false,0)),level))return candidate;
                // A recoloring recipe can reject a no-op color. Try each source/material pair.
                for(var source:alternatives(ingredients.getFirst(),level,id))for(var material:alternatives(ingredients.get(1),level,id)) {
                    stacks=new ArrayList<>();stacks.add(source);for(int i=1;i<length;i++)stacks.add(material.copy());
                    candidate=new Definition(id,recipe,width,width,cells,stacks);
                    if(recipe.matches(CraftingInput.of(3,3,grid(candidate,stacks,3,0,0,false,0)),level))return candidate;
                }
            }
            throw new IllegalStateException("No valid data-driven custom fixture: "+id);
        }
        List<ItemStack> stacks;int width=0,height=0;
        if(recipe instanceof RepairItemRecipe) {
            var a=new ItemStack(Items.WOODEN_SWORD);var b=a.copy();a.setDamageValue(20);b.setDamageValue(30);stacks=List.of(a,b);
        }else if(recipe instanceof FireworkRocketRecipe)stacks=List.of(new ItemStack(Items.PAPER),new ItemStack(Items.GUNPOWDER));
        else if(recipe instanceof FireworkStarRecipe)stacks=List.of(new ItemStack(Items.GUNPOWDER),new ItemStack(Items.DYE.red()));
        else if(recipe instanceof FireworkStarFadeRecipe) {
            var star=new ItemStack(Items.FIREWORK_STAR);star.set(net.minecraft.core.component.DataComponents.FIREWORK_EXPLOSION,net.minecraft.world.item.component.FireworkExplosion.DEFAULT);
            stacks=List.of(star,new ItemStack(Items.DYE.blue()));
        }else if(recipe instanceof BookCloningRecipe) {
            var book=new ItemStack(Items.WRITTEN_BOOK);book.set(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT,net.minecraft.world.item.component.WrittenBookContent.EMPTY);
            stacks=List.of(book,new ItemStack(Items.WRITABLE_BOOK));
        }else if(recipe instanceof BannerDuplicateRecipe) {
            var blank=alternatives(serializedIngredient(recipe,"banner",level),level,id).getFirst();
            var banner=blank.copy();
            banner.set(net.minecraft.core.component.DataComponents.BANNER_PATTERNS,new net.minecraft.world.level.block.entity.BannerPatternLayers.Builder()
                    .add(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BANNER_PATTERN).getOrThrow(net.minecraft.world.level.block.entity.BannerPatterns.CROSS),DyeColor.RED).build());
            stacks=List.of(banner,blank);
        }else if(recipe instanceof ShieldDecorationRecipe)stacks=List.of(alternatives(serializedIngredient(recipe,"target",level),level,id).getFirst(),alternatives(serializedIngredient(recipe,"banner",level),level,id).getFirst());
        else if(recipe instanceof DecoratedPotRecipe) {
            width=height=3;stacks=List.of(ItemStack.EMPTY,new ItemStack(Items.BRICK),ItemStack.EMPTY,new ItemStack(Items.BRICK),ItemStack.EMPTY,new ItemStack(Items.BRICK),ItemStack.EMPTY,new ItemStack(Items.BRICK),ItemStack.EMPTY);
        }else if(recipe instanceof MapExtendingRecipe) {
            width=height=3;var map=MapItem.create(level,0,0,(byte)0,false,false);var paper=new ItemStack(Items.PAPER);
            stacks=List.of(paper,paper,paper,paper,map,paper,paper,paper,paper);
        }else throw new IllegalStateException("Uncovered crafting serializer: "+id+" "+recipe.getClass().getName());
        var cells=stacks.stream().map(s->s.isEmpty()?Optional.<Ingredient>empty():Optional.of(Ingredient.of(s.getItem()))).toList();
        return new Definition(id,recipe,width,height,cells,stacks);
    }
    private static void clear(FakePlayer player,AbstractCraftingMenu menu,int side) {
        menu.setCarried(ItemStack.EMPTY);for(int i=1;i<=side*side;i++)menu.getSlot(i).set(ItemStack.EMPTY);
        player.getInventory().clearContent();
    }
    private static Ingredient serializedIngredient(CraftingRecipe recipe,String field,ServerLevel level) {
        var ops=level.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var data=Recipe.DIRECT_CODEC.encodeStart(ops,recipe).getOrThrow().getAsJsonObject();
        return Ingredient.CODEC.parse(ops,data.get(field)).getOrThrow();
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);checks++;}
    private RecipeAuditCheck() { }
}
