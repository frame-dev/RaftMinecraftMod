package dev.raftocean.survival;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** All transfers exchange one physical cup; old saved stacks retain every remaining cup. */
public final class CupTransfers {
    public static final int CAPACITY = 4;
    private CupTransfers() { }

    public static ItemStack replaceOne(Player player, InteractionHand hand, ItemStack replacement) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getCount() == 1) {
            player.setItemInHand(hand, replacement);
            return replacement;
        }
        held.shrink(1);
        if (!player.addItem(replacement)) { player.drop(replacement, false, Prediction.SERVER_ONLY); }
        return held;
    }

    public static InteractionResult use(RaftEntity raft, BlockPos pos, RaftPiece.Kind kind, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || !kind.machine()) { return InteractionResult.PASS; }
        boolean empty = held.is(SurvivalContent.EMPTY_CUP.get());
        boolean salt = held.is(SurvivalContent.SALT_WATER.get());
        boolean fresh = held.is(SurvivalContent.FRESH_WATER.get());
        boolean draws = empty && (kind.purifier() || kind == RaftPiece.Kind.RAIN_COLLECTOR || kind == RaftPiece.Kind.WATER_TANK || kind == RaftPiece.Kind.ELECTRIC_PURIFIER || kind == RaftPiece.Kind.SPRINKLER);
        boolean pours = salt && kind.purifier()
                || fresh && ((kind == RaftPiece.Kind.PLANTER || kind == RaftPiece.Kind.TREE_PLANTER) || kind == RaftPiece.Kind.COOKING_POT || kind == RaftPiece.Kind.WATER_TANK || kind == RaftPiece.Kind.ELECTRIC_PURIFIER || kind == RaftPiece.Kind.SPRINKLER);
        if (!draws && !pours) { return InteractionResult.PASS; }
        if (player.isSpectator() || !dev.raftocean.menu.RaftStationMenus.valid(raft,pos,kind,player)
                || raft.storage(pos)==null) { return InteractionResult.FAIL; }
        if (raft.level().isClientSide()) { return InteractionResult.SUCCESS; }
        var storage = raft.storage(pos);
        String blocked = draws && storage.cleanWater == 0 ? "empty"
                : pours && (salt ? storage.rawWater : storage.cleanWater) >= Automation.waterCapacity(kind) ? "full" : null;
        if (blocked != null) {
            player.sendOverlayMessage(Component.translatable("raft_ocean.cup." + blocked));
            return InteractionResult.FAIL;
        }
        if (draws) { storage.cleanWater--; }
        else if (salt) { storage.rawWater++; }
        else { storage.cleanWater++; }
        replaceOne(player, hand, new ItemStack(draws ? SurvivalContent.FRESH_WATER.get() : SurvivalContent.EMPTY_CUP.get()));
        storage.setChanged();
        player.inventoryMenu.sendAllDataToRemote();
        player.sendOverlayMessage(Component.translatable("raft_ocean.cup." + (draws ? "filled" : "poured")));
        return InteractionResult.SUCCESS_SERVER;
    }
}
