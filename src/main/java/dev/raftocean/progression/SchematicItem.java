package dev.raftocean.progression;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** All player inventory slots tick on the server, including chest transfers. */
public final class SchematicItem extends Item {
    private final int schematic;
    public SchematicItem(Properties properties,int schematic){super(properties);this.schematic=schematic;}
    @Override public void inventoryTick(ItemStack stack,ServerLevel level,Entity owner,@org.jspecify.annotations.Nullable EquipmentSlot slot){
        if(owner instanceof Player player)StorySchematics.discover(player,schematic);
    }
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(!level.isClientSide()){
            StorySchematics.discover(player,schematic);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("raft_ocean.schematic.help"));
        }
        return InteractionResult.SUCCESS;
    }
}
