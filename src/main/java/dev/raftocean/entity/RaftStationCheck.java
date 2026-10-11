package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.menu.RaftStationMenus;
import dev.raftocean.registry.ModContent;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Exercises actual native station processing, shared storage, and entity inventory persistence. */
public final class RaftStationCheck {
    private RaftStationCheck() { }

    public static int run(ServerLevel level) {
        int checks = 0;
        BlockPos pos = new BlockPos(1, 1, 0);
        for (RaftPiece.Kind kind : RaftPiece.Kind.values()) {
            if (!kind.station() || kind == RaftPiece.Kind.RESEARCH_TABLE || kind == RaftPiece.Kind.CRAFTING_TABLE || kind==RaftPiece.Kind.WIDE_CRAFTING_TABLE || kind == RaftPiece.Kind.COLLECTION_NET || kind.machine() || kind.navigation() || kind.bed()) { continue; }
            ItemStack vanilla = new ItemStack(ModContent.vanillaStation(kind));
            require(ModContent.structuralKind(vanilla) == kind, "Direct vanilla placement " + kind);
            checks++;
            var input = CraftingInput.of(1, 1, List.of(vanilla));
            var recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
            require(recipe.isPresent() && recipe.get().value().assemble(input).is(ModContent.structuralItem(kind)),
                    "Optional station conversion " + kind);
            checks++;
            RaftEntity raft = create(level, pos, kind);
            FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "RaftStationCheck"));
            player.setPos(raft.position().add(0, RaftEntity.DECK_HEIGHT + .001, 0));
            AbstractContainerMenu menu = RaftStationMenus.create(1, player.getInventory(), raft, pos, kind);
            require(menu.stillValid(player), "Native menu near station " + kind);
            checks++;
            Vec3 delta = new Vec3(20, .012, -20);
            raft.setPos(raft.position().add(delta));
            player.setPos(player.position().add(delta));
            require(menu.stillValid(player), "Menu follows movement " + kind);
            checks++;
            player.setPos(player.position().add(12, 0, 0));
            require(!menu.stillValid(player), "Leaving range invalidates " + kind);
            checks++;
            player.setPos(raft.position().add(0, RaftEntity.DECK_HEIGHT + .001, 0));

            if(kind==RaftPiece.Kind.ENDER_CHEST){
                menu.getSlot(0).set(new ItemStack(Items.DIAMOND,7));
                var other=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"OtherEnderUser"));
                other.setPos(player.position());
                var otherMenu=RaftStationMenus.create(2,other.getInventory(),raft,pos,kind);
                require(otherMenu.getSlot(0).getItem().isEmpty() && player.getEnderChestInventory().getItem(0).getCount()==7,
                        "Ender storage is private per player");checks++;
                require(removeAndCountDrops(level,raft,pos)==0 && player.getEnderChestInventory().getItem(0).getCount()==7
                        && !menu.stillValid(player),"Removing Ender Chest never drops private contents");checks++;
                otherMenu.removed(other);menu.removed(player);player.getEnderChestInventory().clearContent();
                raft.discard();other.discard();player.getInventory().clearContent();continue;
            }else if (kind.storage()) {
                var inventory = raft.storage(pos);
                var second = RaftStationMenus.create(2, player.getInventory(), raft, pos, kind);
                menu.getSlot(0).set(new ItemStack(Items.OAK_PLANKS, 10));
                require(second.getSlot(0).getItem().getCount() == 10, "Storage menus share one inventory");
                checks++;
                menu.quickMoveStack(player, 0);
                require(inventory.getItem(0).isEmpty() && second.getSlot(0).getItem().isEmpty()
                        && player.getInventory().countItem(Items.OAK_PLANKS) == 10, "Shared shift transfer exactly once");
                checks++;
                inventory.setItem(0, new ItemStack(Items.OAK_PLANKS, 5));
                ItemStack namedTool = new ItemStack(Items.IRON_SWORD);
                namedTool.setDamageValue(37);
                namedTool.set(DataComponents.CUSTOM_NAME, Component.literal("Station Check Tool"));
                inventory.setItem(26, namedTool);
                var reporter = new ProblemReporter.Collector();
                var saved = TagValueOutput.createWithContext(reporter, level.registryAccess());
                raft.saveWithoutId(saved);
                RaftEntity restored = new RaftEntity(ModContent.RAFT.get(), level);
                restored.load(TagValueInput.create(reporter, level.registryAccess(), saved.buildResult()));
                require(reporter.isEmpty() && restored.storage(pos).getItem(0).getCount() == 5
                        && ItemStack.isSameItemSameComponents(restored.storage(pos).getItem(26), namedTool),
                        "Storage saves exact counts, slots, names, and damage");
                checks++;
                int dropped = removeAndCountDrops(level, raft, pos);
                require(dropped >= 0, "Storage piece can be removed");
                checks++;
                require(!menu.stillValid(player) && !second.stillValid(player), "Both storage screens invalidate on removal");
                checks++;
                require(dropped == 6 && inventory.isEmpty(), "Stored contents drop once on removal");
                checks++;
                require(removeAndCountDrops(level, raft, pos) == -1, "Repeated removal cannot duplicate drops");
                checks++;
                restored.storage(pos).clearContent();
                restored.discard();
                second.removed(player);
                RaftEntity unloading = create(level, pos, kind);
                unloading.storage(pos).setItem(0, new ItemStack(Items.STONE, 7));
                require(countDrops(level, () -> unloading.remove(net.minecraft.world.entity.Entity.RemovalReason.UNLOADED_TO_CHUNK)) == 0
                        && unloading.storage(pos).getItem(0).getCount() == 7, "Chunk unload preserves cargo");
                checks++;
                RaftEntity destroying = create(level, pos, kind);
                var cargo = destroying.storage(pos);
                cargo.setItem(0, new ItemStack(Items.STONE, 7));
                require(countDrops(level, destroying::discard) == 7 && cargo.isEmpty(), "Destroyed raft drops cargo");
                checks++;
                require(countDrops(level, destroying::discard) == 0, "Repeated destruction cannot duplicate cargo");
                checks++;
            } else {
                int output;
                ItemStack leftover;
                switch (kind) {
                    case ANVIL -> {
                        player.experienceLevel=50;
                        var sword=new ItemStack(Items.IRON_SWORD);sword.setDamageValue(100);
                        menu.getSlot(0).set(sword);menu.getSlot(1).set(new ItemStack(Items.IRON_INGOT,2));
                        output=2;leftover=new ItemStack(Items.IRON_SWORD);
                        require(menu.getSlot(output).getItem().getDamageValue()<100
                                && ((dev.raftocean.menu.RaftAnvilMenu)menu).getCost()>0,"Native anvil repairs tools with an XP cost");checks++;
                    }
                    case ENCHANTING_TABLE -> {
                        player.experienceLevel=50;
                        menu.getSlot(0).set(new ItemStack(Items.IRON_SWORD));
                        menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI,8));
                        require(menu.clickMenuButton(player,0) && menu.getSlot(0).getItem().isEnchanted()
                                && menu.getSlot(1).getItem().getCount()==7 && player.experienceLevel==49,
                                "Native enchanting consumes one level and lapis and enchants the item");checks++;
                        output=0;leftover=new ItemStack(Items.IRON_SWORD);
                    }
                    case STONECUTTER -> {
                        menu.getSlot(0).set(new ItemStack(Items.STONE));
                        require(menu.clickMenuButton(player, 0), "Stonecutter recipe selection");
                        checks++;
                        output = 1; leftover = new ItemStack(Items.STONE, 3);
                    }
                    case SMITHING_TABLE -> {
                        menu.getSlot(0).set(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
                        menu.getSlot(1).set(new ItemStack(Items.DIAMOND_SWORD));
                        menu.getSlot(2).set(new ItemStack(Items.NETHERITE_INGOT));
                        output = 3; leftover = new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
                        require(menu.getSlot(output).getItem().is(Items.NETHERITE_SWORD), "Smithing upgrade recipe");
                        checks++;
                    }
                    case GRINDSTONE -> {
                        ItemStack first = new ItemStack(Items.IRON_SWORD); first.setDamageValue(150);
                        ItemStack other = new ItemStack(Items.IRON_SWORD); other.setDamageValue(150);
                        menu.getSlot(0).set(first); menu.getSlot(1).set(other);
                        output = 2; leftover = new ItemStack(Items.IRON_SWORD);
                        require(menu.getSlot(output).getItem().is(Items.IRON_SWORD)
                                && menu.getSlot(output).getItem().getDamageValue() < 150, "Grindstone tool repair");
                        checks++;
                    }
                    case LOOM -> {
                        menu.getSlot(0).set(new ItemStack(Items.BANNER.pick(net.minecraft.world.item.DyeColor.WHITE)));
                        menu.getSlot(1).set(new ItemStack(Items.DYE.pick(net.minecraft.world.item.DyeColor.RED)));
                        require(menu.clickMenuButton(player, 0), "Loom pattern selection");
                        checks++;
                        output = 3; leftover = new ItemStack(Items.BANNER.pick(net.minecraft.world.item.DyeColor.WHITE));
                    }
                    case CARTOGRAPHY_TABLE -> {
                        menu.getSlot(0).set(MapItem.create(level, 0, 0, (byte) 0, true, false));
                        menu.getSlot(1).set(new ItemStack(Items.MAP));
                        output = 2; leftover = MapItem.create(level, 0, 0, (byte) 0, true, false);
                        require(menu.getSlot(output).getItem().is(Items.FILLED_MAP)
                                && menu.getSlot(output).getItem().getCount() == 2, "Cartography map cloning");
                        checks++;
                    }
                    default -> throw new IllegalStateException("Unexpected station " + kind);
                }
                ItemStack result = menu.getSlot(output).getItem().copy();
                require(!result.isEmpty(), "Station produces output " + kind);
                checks++;
                int before = player.getInventory().countItem(result.getItem());
                menu.quickMoveStack(player, output);
                if(kind==RaftPiece.Kind.ANVIL){require(player.experienceLevel<50,"Anvil output consumes native XP");checks++;}
                require(player.getInventory().countItem(result.getItem()) == before + result.getCount(), "Station output transfer " + kind);
                checks++;
                menu.getSlot(0).set(leftover.copy());
                int initial = player.getInventory().countItem(leftover.getItem());
                require(raft.removeStructuralPiece(pos) && !menu.stillValid(player), "Station removal invalidates menu " + kind);
                checks++;
                menu.removed(player);
                require(player.getInventory().countItem(leftover.getItem()) == initial + leftover.getCount(), "Return station inputs on removal " + kind);
                checks++;
                menu.removed(player);
                require(player.getInventory().countItem(leftover.getItem()) == initial + leftover.getCount(), "Repeated close does not duplicate " + kind);
                checks++;
            }
            menu.removed(player);
            raft.discard();
        }
        return checks+enchantmentShelves(level);
    }

    private static int enchantmentShelves(ServerLevel level){
        int checks=0;
        var layout=new java.util.ArrayList<String>();
        for(int x=-2;x<=2;x++){for(int z=-2;z<=2;z++){layout.add(x+","+z);}}
        var pieces=new java.util.ArrayList<String>();pieces.add("0,1,0,ENCHANTING_TABLE,0,0");
        int count=0;
        for(var offset:net.minecraft.world.level.block.EnchantingTableBlock.BOOKSHELF_OFFSETS){
            if(offset.getY()==0 && count++<15){pieces.add(offset.getX()+",1,"+offset.getZ()+",BOOKSHELF,0,0");}
        }
        var raft=new RaftEntity(ModContent.RAFT.get(),level);
        var data=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,level.registryAccess());
        data.putString("Layout",String.join(";",layout));data.putString("Structure",String.join(";",pieces));
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),data.buildResult()));
        raft.setPos(8.5,level.getMaxY()+16,8.5);
        var player=new FakePlayer(level,new GameProfile(UUID.randomUUID(),"RaftEnchantCheck"));
        player.setPos(raft.position().add(0,.251,0));
        var menu=new dev.raftocean.menu.RaftEnchantingMenu(1,player.getInventory(),raft,new BlockPos(0,1,0));
        try{
            require(menu.bookshelfPower()==15,"Raft shelves supply native maximum enchanting power");checks++;
            menu.getSlot(0).set(new ItemStack(Items.IRON_SWORD));
            require(menu.costs[2]==30,"Native third offer reaches level 30 from raft shelves");checks++;
            raft.setYRot(83);Vec3 delta=new Vec3(20,.01,5);raft.setPos(raft.position().add(delta));player.setPos(player.position().add(delta));
            menu.broadcastChanges();
            require(menu.bookshelfPower()==15 && menu.costs[2]==30 && menu.stillValid(player),"Enchanting power and menu follow raft translation and rotation");checks++;
            pieces.add("1,1,0,WALL,0,0");data.putString("Structure",String.join(";",pieces));
            raft.load(TagValueInput.create(ProblemReporter.DISCARDING,level.registryAccess(),data.buildResult()));menu.broadcastChanges();
            require(menu.bookshelfPower()<15 && menu.costs[2]<30,"An occupied gap blocks shelves and refreshes native offers");checks++;
        }finally{menu.removed(player);player.getInventory().clearContent();raft.discard();}
        return checks;
    }

    private static RaftEntity create(ServerLevel level, BlockPos pos, RaftPiece.Kind kind) {
        RaftStructure structure = new RaftStructure();
        require(structure.add(pos, new RaftPiece(kind, 0, false), RaftLayout.starter()), "Place station " + kind);
        var seed = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        seed.putString("Structure", structure.encode());
        RaftEntity raft = new RaftEntity(ModContent.RAFT.get(), level);
        raft.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), seed.buildResult()));
        raft.setPos(0, level.getMaxY() + 16, 0);
        return raft;
    }

    private static int removeAndCountDrops(ServerLevel level, RaftEntity raft, BlockPos pos) {
        boolean[] removed = {false};
        int count = countDrops(level, () -> removed[0] = raft.removeStructuralPiece(pos));
        require(removed[0] || count == 0, "Failed removal cannot spawn contents");
        return removed[0] ? count : -1;
    }

    private static int countDrops(ServerLevel level, Runnable action) {
        // Above-terrain fixtures can occupy hidden entity sections; observe additions instead of spatial queries.
        var count = new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof ItemEntity item) {
                count.addAndGet(item.getItem().getCount());
            }
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.event.entity.EntityJoinLevelEvent.class, listener);
        try {
            action.run();
            return count.get();
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new IllegalStateException(message); }
    }
}
