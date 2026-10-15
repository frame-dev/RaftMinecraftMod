package dev.raftocean.client;

import dev.raftocean.config.RaftConfig;
import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import dev.raftocean.survival.MachineHarvest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;

/** Short contextual prompts use public piece types, never private container contents. */
public final class InteractionHints {
    private InteractionHints() { }
    public static void draw(GuiGraphicsExtractor graphics,Minecraft game) {
        var player=game.player;
        if(!RaftConfig.INTERACTION_HINTS.get() || player==null || player.isSpectator() || game.gui.hud.isHidden() || game.gui.screen()!=null
                || !player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()
                || !(game.hitResult instanceof EntityHitResult hit) || !(hit.getEntity() instanceof RaftEntity raft)) { return; }
        var pick=raft.pick(player);if(pick==null || pick.piece()==null) { return; }
        var kind=pick.piece().kind();
        boolean addonOutputs=kind==RaftPiece.Kind.ADDON && dev.raftocean.api.RaftBuildables.machine(pick.piece().addonId())
                .map(behavior->!behavior.quickHarvestSlots().isEmpty()).orElse(false);
        String action=kind==RaftPiece.Kind.COLLECTION_NET?"net"
                :(kind==RaftPiece.Kind.CHEST || kind==RaftPiece.Kind.BARREL) && RaftConfig.QUICK_STASH.get()?"stash"
                :RaftConfig.QUICK_HARVEST.get() && (addonOutputs || !MachineHarvest.outputSlots(kind).isEmpty())?"machine":null;
        if(action==null) { return; }
        var lines=game.font.split(Component.translatable("raft_ocean.hint."+action),Math.max(60,Math.min(240,graphics.guiWidth()-20)));
        int y=graphics.guiHeight()/2+22;
        for(var line:lines) {
            int width=game.font.width(line),x=(graphics.guiWidth()-width)/2;
            graphics.fill(x-5,y-2,x+width+5,y+10,0xBB102229);
            graphics.text(game.font,line,x,y,0xFFABCFC5);y+=11;
        }
    }
}
