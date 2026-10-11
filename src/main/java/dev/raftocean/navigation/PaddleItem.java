package dev.raftocean.navigation;

import dev.raftocean.entity.RaftContact;
import dev.raftocean.entity.RaftEntity;
import java.util.Comparator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public final class PaddleItem extends Item {
    public PaddleItem(Properties properties) { super(properties); }
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var raft = level.getEntitiesOfClass(RaftEntity.class, player.getBoundingBox().inflate(1), candidate ->
                        !candidate.isRemoved() && candidate.getCollisionBoxes().stream().anyMatch(box -> RaftContact.isSupported(player.getBoundingBox(), box)))
                .stream().min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(player))).orElse(null);
        if (raft == null || !dev.raftocean.crew.CrewAccess.allowed(player,raft) || raft.navigation().anchored() || player.getCooldowns().isOnCooldown(player.getItemInHand(hand))) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) { return InteractionResult.SUCCESS; }
        if (!raft.navigation().stroke(player.getLookAngle())) { return InteractionResult.FAIL; }
        player.getItemInHand(hand).hurtAndBreak(1, player, hand);
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 8);
        return InteractionResult.SUCCESS_SERVER;
    }
}
