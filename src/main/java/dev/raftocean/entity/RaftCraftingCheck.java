package dev.raftocean.entity;

import com.mojang.authlib.GameProfile;
import dev.raftocean.menu.RaftCraftingMenu;
import dev.raftocean.registry.ModContent;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Production recipe/menu checks on an unregistered raft in the isolated server. */
public final class RaftCraftingCheck {
    private RaftCraftingCheck() { }

    public static int run(ServerLevel level) {
        int checks = 0;
        var tableItem = ModContent.structuralItem(RaftPiece.Kind.CRAFTING_TABLE);
        CraftingInput bootstrap = CraftingInput.of(2, 2, List.of(new ItemStack(Items.OAK_PLANKS),
                new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.STICK)));
        var recipe = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, bootstrap, level);
        require(recipe.isPresent() && recipe.get().value().assemble(bootstrap).is(tableItem),
                "Raft table must craft directly in the 2x2 player grid without a conflicting vanilla recipe");
        checks++;
        CraftingInput conversion = CraftingInput.of(1, 1, List.of(new ItemStack(Items.CRAFTING_TABLE)));
        var converted = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, conversion, level);
        require(converted.isPresent() && converted.get().value().assemble(conversion).is(tableItem), "Vanilla table conversion");
        checks++;

        RaftStructure structure = new RaftStructure();
        BlockPos table = new BlockPos(1, 1, 0);
        require(structure.add(table, new RaftPiece(RaftPiece.Kind.CRAFTING_TABLE, 0, false), RaftLayout.starter()),
                "Table attaches to a foundation");
        checks++;
        var seed = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        seed.putString("Structure", structure.encode());
        RaftEntity raft = new RaftEntity(ModContent.RAFT.get(), level);
        raft.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), seed.buildResult()));
        raft.setPos(0, level.getMaxY() + 16, 0);
        FakePlayer player = new FakePlayer(level,
                new GameProfile(UUID.fromString("49695961-e5a2-471e-9e99-39aac1bb73d4"), "RaftCraftCheck"));
        player.setPos(raft.position().add(0, RaftEntity.DECK_HEIGHT + .001, 0));
        player.setData(dev.raftocean.progression.Research.SCHEMA, 1);
        player.setData(dev.raftocean.progression.Research.KNOWLEDGE, dev.raftocean.progression.Research.ALL_PROJECTS);
        RaftCraftingMenu menu = new RaftCraftingMenu(1, player.getInventory(), raft, table);
        require(menu.slots.size() == 46 && menu.stillValid(player), "Vanilla 3x3 crafting menu opens near raft table");
        checks++;
        Vec3 drift = new Vec3(20, .012, -20);
        raft.setPos(raft.position().add(drift));
        player.setPos(player.position().add(drift));
        require(menu.stillValid(player), "Crafting menu follows raft beyond its original world coordinates");
        checks++;
        menu.getSlot(1).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(2).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(3).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(5).set(new ItemStack(Items.STICK));
        require(menu.getResultSlot().getItem().is(ModContent.FOUNDATION.get())
                && menu.getResultSlot().getItem().getCount() == 2, "3x3 foundation recipe produces correct result while drifting");
        checks++;
        menu.quickMoveStack(player, 0);
        require(player.getInventory().countItem(ModContent.FOUNDATION.get()) == 2
                && menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().isEmpty()),
                "Shift crafting consumes ingredients once and transfers result");
        checks++;
        menu.getSlot(1).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(4).set(new ItemStack(Items.OAK_PLANKS));
        require(menu.getResultSlot().getItem().is(Items.STICK) && menu.getResultSlot().getItem().getCount() == 4,
                "Vanilla crafting recipes remain available");
        checks++;
        menu.quickMoveStack(player, 0);
        require(player.getInventory().countItem(Items.STICK) == 4, "Vanilla output transfer");
        checks++;
        checks += materialChecks(level, menu, player);
        player.setPos(player.position().add(12, 0, 0));
        require(!menu.stillValid(player), "Leaving moving table range invalidates menu");
        checks++;
        player.setPos(raft.position().add(0, RaftEntity.DECK_HEIGHT + .001, 0));
        menu.getSlot(1).set(new ItemStack(Items.OAK_PLANKS, 5));
        int before = player.getInventory().countItem(Items.OAK_PLANKS);
        var empty = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        empty.putString("Structure", "");
        raft.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), empty.buildResult()));
        require(!menu.stillValid(player), "Removing table invalidates its menu");
        checks++;
        menu.removed(player);
        require(player.getInventory().countItem(Items.OAK_PLANKS) == before + 5
                && menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().isEmpty()),
                "Closing after removal returns ingredients without loss");
        checks++;
        menu.removed(player);
        require(player.getInventory().countItem(Items.OAK_PLANKS) == before + 5, "Repeated close cannot duplicate ingredients");
        return checks + 1;
    }

    private static int materialChecks(ServerLevel level, RaftCraftingMenu menu, FakePlayer player) {
        Item leaf = ModContent.DRIFT_LEAF.get(), fiber = ModContent.PLANT_FIBER.get();
        Item rope = ModContent.CORDAGE.get(), mesh = ModContent.WOVEN_MESH.get();
        Item brace = ModContent.TIMBER_BRACE.get(), fastener = ModContent.SCRAP_FASTENERS.get();
        Item head = ModContent.METAL_HOOK_HEAD.get();
        int checks = 0;
        for (String name : List.of("plant_fiber", "cordage", "woven_mesh", "timber_brace", "scrap_fasteners",
                "metal_hook_head", "raft_chest_from_materials", "raft_barrel_from_materials")) {
            var key = ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath("raft_ocean", name));
            var advancement = level.getServer().getAdvancements().get(
                    Identifier.fromNamespaceAndPath("raft_ocean", "recipes/materials/" + name));
            require(advancement != null && advancement.value().rewards().recipes().contains(key), "Material recipe unlock " + name);
            checks++;
        }
        checks += craft(menu, player, fiber, 2, leaf);
        checks += craft(menu, player, rope, 1, fiber, fiber);
        checks += craft(menu, player, brace, 2, Items.SPRUCE_PLANKS, Items.STICK);
        checks += craft(menu, player, fastener, 4, ModContent.SCRAP.get());
        checks += craft(menu, player, head, 1, fastener, fastener, null, Items.IRON_NUGGET, Items.IRON_NUGGET);
        checks += craft(menu, player, mesh, 1, rope, rope, rope, rope, rope, rope, rope, rope, rope);
        checks += craft(menu, player, mesh, 1, Items.STRING, rope, Items.STRING, rope, rope, rope, Items.STRING, rope, Items.STRING);
        checks += craft(menu, player, ModContent.structuralItem(RaftPiece.Kind.COLLECTION_NET), 1,
                brace, rope, brace, rope, mesh, rope, brace, rope, brace);
        checks += craft(menu, player, ModContent.REINFORCED_HOOK.get(), 1, ModContent.GRAPPLING_HOOK.get(), head, rope);
        checks += craft(menu, player, ModContent.structuralItem(RaftPiece.Kind.CHEST), 1,
                Items.SPRUCE_PLANKS, Items.SPRUCE_PLANKS, Items.SPRUCE_PLANKS, brace, fastener, brace,
                Items.SPRUCE_PLANKS, Items.SPRUCE_PLANKS, Items.SPRUCE_PLANKS);
        checks += craft(menu, player, ModContent.structuralItem(RaftPiece.Kind.BARREL), 1,
                brace, fastener, brace, Items.OAK_PLANKS, mesh, Items.OAK_PLANKS, brace, rope, brace);
        checks += craft(menu, player, ModContent.structuralItem(RaftPiece.Kind.ROOF), 2,
                Items.OAK_PLANKS, Items.OAK_PLANKS, Items.OAK_PLANKS, rope, mesh, Items.STRING);
        checks += craft(menu, player, ModContent.structuralItem(RaftPiece.Kind.PILLAR), 1,
                brace, null, null, brace, null, null, rope);
        var incomplete = CraftingInput.of(2, 2, List.of(new ItemStack(fiber), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
        require(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, incomplete, level).isEmpty(),
                "Cordage requires two occupied fiber slots, not one oversized stack");
        return checks + 1;
    }

    private static int craft(RaftCraftingMenu menu, FakePlayer player, Item result, int count, Item... ingredients) {
        require(menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().isEmpty()), "Previous craft consumed its grid");
        for (int i = 0; i < ingredients.length; i++) {
            if (ingredients[i] != null) { menu.getSlot(i + 1).set(new ItemStack(ingredients[i])); }
        }
        require(menu.getResultSlot().getItem().is(result) && menu.getResultSlot().getItem().getCount() == count,
                "Loaded material-chain recipe produces " + result);
        int before = player.getInventory().countItem(result);
        menu.quickMoveStack(player, 0);
        require(player.getInventory().countItem(result) == before + count
                && menu.getInputGridSlots().stream().allMatch(slot -> slot.getItem().isEmpty()),
                "Material-chain craft consumes once and shift-transfers " + result);
        return 2;
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new IllegalStateException(message); }
    }
}
