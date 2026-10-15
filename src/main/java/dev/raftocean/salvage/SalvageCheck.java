package dev.raftocean.salvage;

import com.mojang.authlib.GameProfile;
import dev.raftocean.entity.*;
import dev.raftocean.registry.ModContent;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Actual loot, recipes, moving net deposits, save/load, and server-side cast/reel regressions. */
public final class SalvageCheck {
    private SalvageCheck() { }
    public static int run(ServerLevel level) {
        int checks = 0;
        Vec3 origin = new Vec3(0, level.getMaxY() + 24, 0);
        checks += streamChecks(level, origin);
        checks += anchorChecks(level);
        for (var kind : DebrisEntity.Kind.values()) {
            var debris = DebrisEntity.create(level, kind, origin);
            List<ItemStack> first = debris.loot();
            require(!first.isEmpty(), "Loaded salvage loot table " + kind); checks++;
            require(same(first, debris.loot()), "Loot rolls once " + kind); checks++;
            var reporter = new ProblemReporter.Collector();
            var output = TagValueOutput.createWithContext(reporter, level.registryAccess());
            debris.saveWithoutId(output);
            var restored = DebrisEntity.create(level, DebrisEntity.Kind.PLANK, origin);
            restored.load(TagValueInput.create(reporter, level.registryAccess(), output.buildResult()));
            require(reporter.isEmpty() && restored.kind() == kind && same(first, restored.loot()), "Exact salvage save/load " + kind); checks++;
            if (kind == DebrisEntity.Kind.BOTTLE) {
                require(first.getFirst().is(ModContent.SEALED_NOTE.get()) && first.getFirst().has(DataComponents.CUSTOM_NAME)
                        && first.getFirst().get(DataComponents.LORE).lines().size() == 2, "Original bottle note text"); checks++;
            }
            SimpleContainer inventory = new SimpleContainer(27);
            require(restored.collect(inventory) && restored.isRemoved(), "Salvage collected " + kind); checks++;
            require(!restored.collect(inventory), "Removed salvage cannot collect twice " + kind); checks++;
            debris.discard();
        }
        require(DebrisSpawning.canSpawn(127, 23) && !DebrisSpawning.canSpawn(128, 23)
                && !DebrisSpawning.canSpawn(127, 24), "Both spawn caps enforced"); checks++;
        var old = fixed(level, origin, List.of(new ItemStack(Items.OAK_PLANKS)));
        var expired = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        old.saveWithoutId(expired); expired.putLong("Born", level.getGameTime() - DebrisSpawning.LIFETIME);
        old.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), expired.buildResult()));
        old.tick(); require(old.isRemoved(), "Unloaded elapsed time expires debris"); checks++;

        RaftStructure structure = new RaftStructure();
        BlockPos net = new BlockPos(1, 1, 0);
        require(structure.add(net, new RaftPiece(RaftPiece.Kind.COLLECTION_NET, 0, false), RaftLayout.starter()), "Net support"); checks++;
        var seed = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        seed.putString("Structure", structure.encode());
        var raft = new RaftEntity(ModContent.RAFT.get(), level);
        raft.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), seed.buildResult()));
        raft.setPos(origin);
        var container = raft.storage(net);
        for (int i = 0; i < container.getContainerSize(); i++) { container.setItem(i, new ItemStack(Items.STONE, 64)); }
        container.setItem(0, new ItemStack(Items.OAK_PLANKS, 63));
        var mixed = fixed(level, raft.stationPosition(net, RaftPiece.Kind.COLLECTION_NET),
                List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(ModContent.SCRAP.get())));
        require(!raft.collectAtNet(net, mixed) && !mixed.isRemoved() && container.getItem(0).getCount() == 63,
                "Full net rolls back partial deposits"); checks++;
        require(mixed.loot().size() == 2, "Full net preserves complete loot"); checks++;
        container.setItem(1, ItemStack.EMPTY);
        Vec3 delta = new Vec3(20, .012, -20);
        raft.setPos(raft.position().add(delta)); mixed.setPos(mixed.position().add(delta));
        require(raft.collectAtNet(net, mixed) && container.getItem(0).getCount() == 64
                && container.getItem(1).is(ModContent.SCRAP.get()), "Net follows raft and deposits exact loot"); checks++;
        require(!raft.collectAtNet(net, mixed), "Net cannot duplicate collected loot"); checks++;
        var distant = fixed(level, origin.add(100, 0, 0), List.of(new ItemStack(Items.OAK_LOG)));
        require(!raft.collectAtNet(net, distant), "Net proximity checked"); checks++;
        distant.discard();
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        raft.saveWithoutId(saved);
        var restoredRaft = new RaftEntity(ModContent.RAFT.get(), level);
        restoredRaft.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
        require(restoredRaft.storage(net).getItem(0).getCount() == 64 && restoredRaft.storage(net).getItem(1).is(ModContent.SCRAP.get()),
                "Net cargo persists"); checks++;
        restoredRaft.storage(net).clearContent(); restoredRaft.discard(); container.clearContent(); raft.discard();

        var craft = CraftingInput.of(2, 2, List.of(new ItemStack(Items.OAK_PLANKS), new ItemStack(Items.OAK_PLANKS),
                ItemStack.EMPTY, new ItemStack(Items.STICK)));
        require(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, craft, level).orElseThrow().value().assemble(craft).is(ModContent.GRAPPLING_HOOK.get()),
                "Hook bootstrap inventory recipe"); checks++;
        var upgrade = CraftingInput.of(2, 2, List.of(new ItemStack(ModContent.GRAPPLING_HOOK.get()), new ItemStack(ModContent.METAL_HOOK_HEAD.get()),
                new ItemStack(ModContent.CORDAGE.get()), ItemStack.EMPTY));
        require(level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, upgrade, level).orElseThrow().value().assemble(upgrade).is(ModContent.REINFORCED_HOOK.get()),
                "Hook upgrade recipe"); checks++;

        FakePlayer player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SalvageCheck"));
        player.setPos(origin); level.addNewPlayer(player);
        try {
            require(level.getPlayerByUUID(player.getUUID()) == player, "Registered fake player for server hook ticks"); checks++;
            for (var item : List.of(ModContent.GRAPPLING_HOOK.get(), ModContent.REINFORCED_HOOK.get())) {
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
                item.use(level, player, InteractionHand.MAIN_HAND);
                SalvageHookEntity hook = GrapplingHookItem.active(player);
                require(hook != null && player.getMainHandItem().getDamageValue() == 1, "Hook cast and durability"); checks++;
                require(hook.range() == (item == ModContent.GRAPPLING_HOOK.get() ? 24 : 40), "Hook tier range"); checks++;
                item.use(level, player, InteractionHand.MAIN_HAND);
                require(GrapplingHookItem.active(player) == hook && player.getMainHandItem().getDamageValue() == 1,
                        "Second use reels the same cast"); checks++;
                DebrisEntity target = fixed(level, player.position().add(14, 1, 0), List.of(new ItemStack(Items.OAK_LOG, 2)));
                require(hook.attach(target) && target.claimed(), "Exclusive hook attachment"); checks++;
                var other = new SalvageHookEntity(ModContent.SALVAGE_HOOK.get(), level);
                require(!other.attach(target) && !target.collect(new SimpleContainer(27)), "Other collectors cannot steal claimed debris"); checks++;
                int before = player.getInventory().countItem(Items.OAK_LOG);
                hook.reel();
                for (int tick = 0; tick < 100 && !hook.isRemoved(); tick++) {
                    player.setPos(player.position().add(.02, 0, .008)); hook.tick();
                }
                require(hook.isRemoved() && target.isRemoved() && player.getInventory().countItem(Items.OAK_LOG) == before + 2,
                        "Reel retrieves once while owner moves"); checks++;
                require(GrapplingHookItem.active(player) == null, "Finished cast clears owner cache"); checks++;
                var cancellation = new SalvageHookEntity(ModContent.SALVAGE_HOOK.get(), level);
                cancellation.cast(player, InteractionHand.MAIN_HAND, false);
                var remaining = fixed(level, player.position(), List.of(new ItemStack(ModContent.SCRAP.get())));
                require(cancellation.attach(remaining), "Cancellation fixture attachment"); checks++;
                cancellation.discard();
                require(!remaining.claimed() && !remaining.isRemoved() && remaining.collect(player, null), "Cancelled hook releases intact loot"); checks++;
                other.discard();
            }
        } finally {
            var hook = GrapplingHookItem.active(player); if (hook != null) { hook.discard(); }
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
        return checks;
    }

    private static boolean settle(DebrisStream state, Vec3 origin, Vec3 course, long readyAt) {
        for (long tick = readyAt - 180; tick < readyAt; tick += 20) {
            if (state.update(origin, course, tick)) { return false; }
        }
        return state.update(origin, course, readyAt);
    }

    private static int streamChecks(ServerLevel level, Vec3 origin) {
        int checks = 0;
        for (Vec3 direction : List.of(new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(0, 0, 1),
                new Vec3(0, 0, -1), new Vec3(1, 0, 1).normalize())) {
            var state = new DebrisStream();
            require(!state.update(origin, direction, 0) && settle(state, origin, direction, 200), "New corridor settles for ten seconds"); checks++;
            Vec3 debris = DebrisStream.spawnPosition(origin, direction, 20, 3);
            Vec3 relative = debris.subtract(origin);
            require(Math.abs(relative.dot(direction) - 20) < 1.0E-10 && DebrisStream.incoming(relative, direction, 8),
                    "Spawn is ahead in travel direction"); checks++;
            Vec3 raft = origin;
            Vec3 drift = DebrisStream.drift(direction);
            double sideBefore = relative.dot(new Vec3(-direction.z, 0, direction.x));
            for (int tick = 0; tick < 800; tick++) { raft = raft.add(direction.scale(.02)); debris = debris.add(drift); }
            relative = debris.subtract(raft);
            require(relative.dot(direction) < -10 && Math.abs(relative.dot(new Vec3(-direction.z, 0, direction.x)) - sideBefore) < 1.0E-8,
                    "Uncollected debris passes behind without centering or turning back"); checks++;
            require(!DebrisStream.incoming(relative, direction, 8), "Passed debris frees incoming corridor capacity"); checks++;
            require(state.update(origin.add(direction.scale(1)), direction, 220), "Normal travel does not restart spawning delay"); checks++;
            require(!state.update(origin.add(direction.scale(1)), direction.scale(-1), 240)
                    && settle(state, origin.add(direction.scale(2)), direction.scale(-1), 440), "Reversed course waits before new corridor"); checks++;
            require(!state.update(origin.add(100, 0, 100), direction.scale(-1), 460), "Large displacement pauses new debris"); checks++;
        }
        require(DebrisStream.matchesDrift(new Vec3(-.018, 0, 0), new Vec3(1, 0, 0))
                && !DebrisStream.matchesDrift(new Vec3(.018, 0, 0), new Vec3(1, 0, 0)),
                "Old opposite-flow debris does not block new corridor spawning"); checks++;
        var resumed = new DebrisStream();
        resumed.update(origin, new Vec3(1, 0, 0), 0);
        require(settle(resumed, origin, new Vec3(1, 0, 0), 200), "Continuous occupied travel settles normally"); checks++;
        resumed.pause(220);
        require(!resumed.update(origin, new Vec3(1, 0, 0), 240)
                && settle(resumed, origin, new Vec3(1, 0, 0), 440), "Anchor release waits for a fresh debris corridor"); checks++;
        require(!resumed.update(origin, new Vec3(1, 0, 0), 800)
                && settle(resumed, origin, new Vec3(1, 0, 0), 1000), "Long unoccupied gap restarts settling instead of spawning immediately"); checks++;
        var gradual = new DebrisStream();
        gradual.update(origin, new Vec3(1, 0, 0), 0);
        settle(gradual, origin, new Vec3(1, 0, 0), 200);
        for (int degrees = 5; degrees <= 30; degrees += 5) {
            double radians = Math.toRadians(degrees);
            gradual.update(origin, new Vec3(Math.cos(radians), 0, Math.sin(radians)), 200 + degrees);
        }
        require(!gradual.update(origin, new Vec3(Math.cos(Math.PI / 6), 0, Math.sin(Math.PI / 6)), 270),
                "Gradual cumulative turn resets settling delay"); checks++;
        var raft = new RaftEntity(ModContent.RAFT.get(), level); raft.setPos(origin);
        var debris = DebrisEntity.create(level, DebrisEntity.Kind.PLANK, origin.add(20, 0, 0));
        debris.assignStream(raft, new Vec3(1, 0, 0));
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        debris.saveWithoutId(saved);
        var restored = DebrisEntity.create(level, DebrisEntity.Kind.PLANK, origin);
        restored.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
        require(restored.drift().distanceToSqr(new Vec3(-DebrisStream.DRIFT_SPEED, 0, 0)) < 1.0E-20,
                "Fixed stream drift survives save/reload"); checks++;
        raft.setCurrent(0, -.05);
        require(restored.drift().distanceToSqr(debris.drift()) < 1.0E-20, "Existing debris does not turn when raft course changes"); checks++;
        debris.discard(); restored.discard(); raft.discard();
        return checks;
    }

    private static int anchorChecks(ServerLevel level) {
        BlockPos water=new BlockPos(8,level.getMaxY()-4,8);
        var before=level.getBlockState(water);
        var raft=dev.raftocean.navigation.NavigationCheck.create(level);
        raft.setPos(8.5,water.getY()+.7,8.5);
        var debris=DebrisEntity.create(level,DebrisEntity.Kind.PLANK,new Vec3(8.5,water.getY()+.9,8.5));
        try {
            level.setBlock(water,net.minecraft.world.level.block.Blocks.WATER.defaultBlockState(),3);
            require(level.addFreshEntity(raft),"Anchor source registered");
            debris.assignStream(raft,new Vec3(1,0,0));
            raft.storage(dev.raftocean.navigation.NavigationCheck.ANCHOR).enabled=true;
            Vec3 start=debris.position();
            for(int tick=0;tick<20;tick++){debris.tick();}
            require(debris.getX()==start.x && debris.getZ()==start.z && debris.getDeltaMovement().horizontalDistanceSqr()==0,
                    "Lowered source anchor freezes debris horizontal drift without stopping water flotation");
            raft.storage(dev.raftocean.navigation.NavigationCheck.ANCHOR).enabled=false;
            debris.tick();
            require(Math.abs(debris.getX()-(start.x-DebrisStream.DRIFT_SPEED))<1E-10,
                    "Raising anchor resumes original course without teleporting debris");
            return 3;
        } finally {debris.discard();raft.discard();level.setBlock(water,before,3);}
    }

    private static DebrisEntity fixed(ServerLevel level, Vec3 position, List<ItemStack> stacks) {
        var seed = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        seed.putString("Kind", "CRATE"); seed.putBoolean("Rolled", true); seed.putLong("Born", level.getGameTime());
        var items = NonNullList.withSize(16, ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) { items.set(i, stacks.get(i).copy()); }
        ContainerHelper.saveAllItems(seed, items);
        var debris = DebrisEntity.create(level, DebrisEntity.Kind.CRATE, position);
        debris.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), seed.buildResult()));
        return debris;
    }
    private static boolean same(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) { return false; }
        for (int i = 0; i < a.size(); i++) { if (!ItemStack.matches(a.get(i), b.get(i))) { return false; } }
        return true;
    }
    private static void require(boolean condition, String message) { if (!condition) { throw new IllegalStateException(message); } }
}
