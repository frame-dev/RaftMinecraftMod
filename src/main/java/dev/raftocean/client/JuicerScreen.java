package dev.raftocean.client;

import dev.raftocean.menu.JuicerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Four food pads, a separate serving vessel and explicit batch controls. */
public final class JuicerScreen extends AbstractContainerScreen<JuicerMenu> {
    private Button blend,pause,serve;
    public JuicerScreen(JuicerMenu menu,Inventory inventory,Component title){super(menu,inventory,title,230,251);}
    private void send(int button){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,button);}
    @Override protected void init(){
        super.init();
        blend=addRenderableWidget(Button.builder(Component.translatable("raft_ocean.juicer.blend"),b->send(0)).bounds(leftPos+12,topPos+121,64,18)
                .tooltip(Tooltip.create(Component.translatable("raft_ocean.juicer.blend_help"))).build());
        pause=addRenderableWidget(Button.builder(Component.empty(),b->send(1)).bounds(leftPos+81,topPos+121,64,18).build());
        serve=addRenderableWidget(Button.builder(Component.translatable("raft_ocean.juicer.serve"),b->send(2)).bounds(leftPos+150,topPos+121,68,18)
                .tooltip(Tooltip.create(Component.translatable("raft_ocean.juicer.serve_help"))).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float partial){
        blend.active=menu.value(4)==7;serve.active=menu.value(7)!=0;
        pause.setMessage(Component.translatable(menu.value(6)==0?"raft_ocean.compactor.pause":"raft_ocean.compactor.resume"));
        super.extractRenderState(g,x,y,partial);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){
        super.extractBackground(g,x,y,partial);g.pose().pushMatrix();g.pose().translate(leftPos,topPos);
        g.fill(0,0,230,251,0xFF967751);g.fill(4,4,226,247,0xFF152831);g.fill(9,28,221,30,0xFF70C8D1);
        for(var slot:menu.slots){g.fill(slot.x-1,slot.y-1,slot.x+17,slot.y+17,0xFF58767A);g.fill(slot.x,slot.y,slot.x+16,slot.y+16,0xFF102229);}
        g.fill(12,75,218,81,0xFF304B51);g.fill(12,75,12+(menu.value(1)<=0?0:Math.clamp(206*menu.value(0)/menu.value(1),0,206)),81,0xFFE7B97C);
        g.fill(41,106,95,113,0xFF304B51);g.fill(41,106,41+(menu.value(3)<=0?0:54*menu.value(2)/menu.value(3)),113,0xFF72C99A);
        g.pose().popMatrix();
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){
        text(g,title,12,12,206,0xFFECE9D5);
        text(g,Component.translatable("raft_ocean.juicer.foods"),12,36,114,0xFFABC4C4);
        text(g,Component.translatable("raft_ocean.juicer.progress",menu.value(0)/20,menu.value(1)/20),120,49,98,0xFFE7B97C);
        text(g,Component.translatable("raft_ocean.compactor.battery"),12,88,84,0xFFABC4C4);
        text(g,Component.translatable("raft_ocean.juicer.glass"),113,88,49,0xFFABC4C4);
        text(g,Component.translatable("raft_ocean.juicer.drink"),180,88,43,0xFFABC4C4);
        text(g,Component.literal(Integer.toString(menu.value(2))),41,95,60,0xFF72C99A);
        text(g,Component.literal("→"),160,104,20,0xFF70C8D1);
        text(g,Component.translatable("raft_ocean.juicer.status."+menu.value(4)),12,145,206,0xFFE7B97C);
    }
    private void text(GuiGraphicsExtractor g,Component text,int x,int y,int width,int color){g.text(font,font.plainSubstrByWidth(text.getString(),width),x,y,color);}
}
