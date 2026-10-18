package dev.raftocean.client;

import dev.raftocean.menu.CompactorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original workshop panel with separate feed, portable battery and output roles. */
public final class CompactorScreen extends AbstractContainerScreen<CompactorMenu> {
    private Button pause;
    public CompactorScreen(CompactorMenu menu,Inventory inventory,Component title){super(menu,inventory,title,230,226);}
    @Override protected void init(){
        super.init();pause=addRenderableWidget(Button.builder(Component.empty(),b->{
            if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0);
        }).bounds(leftPos+12,topPos+114,100,18).tooltip(Tooltip.create(Component.translatable("raft_ocean.compactor.pause_help"))).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int x,int y,float partial){
        pause.setMessage(Component.translatable(menu.value(6)==0?"raft_ocean.compactor.pause":"raft_ocean.compactor.resume"));
        super.extractRenderState(graphics,x,y,partial);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){
        super.extractBackground(g,x,y,partial);g.pose().pushMatrix();g.pose().translate(leftPos,topPos);
        g.fill(0,0,230,226,0xFF967751);g.fill(4,4,226,222,0xFF152831);g.fill(9,28,221,30,0xFF70C8D1);
        for(var slot:menu.slots){g.fill(slot.x-1,slot.y-1,slot.x+17,slot.y+17,0xFF58767A);g.fill(slot.x,slot.y,slot.x+16,slot.y+16,0xFF102229);}
        g.fill(12,100,218,107,0xFF304B51);int progress=menu.value(1)<=0?0:Math.clamp(206*menu.value(0)/menu.value(1),0,206);
        g.fill(12,100,12+progress,107,0xFFE7B97C);
        g.fill(164,72,180,76,0xFF304B51);g.fill(164,72,164+(menu.value(3)==0?0:16*menu.value(2)/menu.value(3)),76,0xFF72C99A);
        g.pose().popMatrix();
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){
        text(g,title,12,12,206,0xFFECE9D5);
        text(g,Component.translatable("raft_ocean.compactor.feed",menu.value(5)),12,37,142,0xFFABC4C4);
        text(g,Component.translatable("raft_ocean.compactor.battery"),155,37,42,0xFFABC4C4);
        text(g,Component.translatable("raft_ocean.compactor.bales"),198,37,30,0xFFABC4C4);
        text(g,Component.translatable("raft_ocean.compactor.status."+menu.value(4)),12,85,206,0xFFE7B97C);
        text(g,Component.translatable("raft_ocean.compactor.progress",menu.value(0),menu.value(1)),120,114,100,0xFFABC4C4);
        text(g,Component.translatable("raft_ocean.compactor.charge",menu.value(2)),120,124,100,0xFF72C99A);
    }
    private void text(GuiGraphicsExtractor g,Component text,int x,int y,int width,int color){g.text(font,font.plainSubstrByWidth(text.getString(),width),x,y,color);}
}
