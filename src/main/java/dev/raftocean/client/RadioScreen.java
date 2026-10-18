package dev.raftocean.client;

import dev.raftocean.menu.RadioMenu;
import dev.raftocean.story.RaftRadio;
import dev.raftocean.story.ReceiverItem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original receiver-relative radar; all signals and setup statuses come from the server. */
public final class RadioScreen extends AbstractContainerScreen<RadioMenu> {
    private Button power,scan;
    public RadioScreen(RadioMenu menu,Inventory inventory,Component title){super(menu,inventory,title,280,263);}
    private void send(int button){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,button);}
    @Override protected void init(){
        super.init();
        power=addRenderableWidget(Button.builder(Component.empty(),b->send(0)).bounds(leftPos+145,topPos+112,120,18).build());
        scan=addRenderableWidget(Button.builder(Component.translatable("raft_ocean.radio.scan"),b->send(1)).bounds(leftPos+145,topPos+133,120,18)
                .tooltip(Tooltip.create(Component.translatable("raft_ocean.radio.scan_help"))).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->send(2)).bounds(leftPos+14,topPos+155,25,18).build());
        addRenderableWidget(Button.builder(Component.translatable("raft_ocean.radio.current"),b->send(4)).bounds(leftPos+43,topPos+155,65,18).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->send(3)).bounds(leftPos+112,topPos+155,25,18).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int x,int y,float partial){
        power.setMessage(Component.translatable(menu.value(0)==0?"raft_ocean.radio.on":"raft_ocean.radio.off"));
        scan.active=menu.value(1)==RaftRadio.READY || menu.value(1)==RaftRadio.NO_SIGNAL;
        super.extractRenderState(graphics,x,y,partial);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){
        super.extractBackground(g,x,y,partial);g.pose().pushMatrix();g.pose().translate(leftPos,topPos);
        g.fill(0,0,280,263,0xFF967751);g.fill(4,4,276,259,0xFF152831);g.fill(9,29,271,31,0xFF70C8D1);
        g.fill(14,35,137,151,0xFF0A1C24);g.fill(75,38,76,148,0xFF294C50);g.fill(18,92,134,93,0xFF294C50);
        for(int ring:new int[]{25,50})for(int angle=0;angle<360;angle+=6){
            int px=75+(int)Math.round(Math.sin(Math.toRadians(angle))*ring),py=93+(int)Math.round(Math.cos(Math.toRadians(angle))*ring);
            g.fill(px,py,px+1,py+1,0xFF315B5A);
        }
        if(menu.value(1)==RaftRadio.READY || menu.value(1)==RaftRadio.NO_SIGNAL){
            double time=minecraft.level==null?0:(minecraft.level.getGameTime()+partial)*.04;
            for(int radius=2;radius<51;radius+=2){int px=75+(int)(Math.sin(time)*radius),py=93+(int)(Math.cos(time)*radius);g.fill(px,py,px+1,py+1,0xFF428A77);}
        }
        g.fill(73,91,78,96,0xFFF0CF7A);
        if(menu.value(1)==RaftRadio.READY){
            double scale=Math.max(512,Math.ceil(menu.value(8)/512.0)*512);
            int px=75+(int)Math.round(menu.value(6)*49/scale),py=93-(int)Math.round(menu.value(7)*49/scale);
            g.fill(px-2,py-2,px+3,py+3,0xFF70D4ED);
        }
        for(var slot:menu.slots){g.fill(slot.x-1,slot.y-1,slot.x+17,slot.y+17,0xFF58767A);g.fill(slot.x,slot.y,slot.x+16,slot.y+16,0xFF102229);}
        g.fill(176,60,265,68,0xFF304B51);g.fill(176,60,176+(menu.value(4)==0?0:89*menu.value(3)/menu.value(4)),68,0xFF72C99A);
        g.pose().popMatrix();
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){
        g.text(font,font.plainSubstrByWidth(title.getString(),250),12,13,0xFFECE9D5);
        int channel=Math.clamp(menu.value(5),0,ReceiverItem.DESTINATIONS.length);
        Component name=Component.translatable(channel==ReceiverItem.DESTINATIONS.length?"raft_ocean.radio.open_channel":"raft_ocean.island."+ReceiverItem.DESTINATIONS[channel]);
        g.text(font,font.plainSubstrByWidth(name.getString(),120),145,38,0xFF70C8D1);
        g.text(font,Component.translatable("raft_ocean.radio.charge",menu.value(3),menu.value(4)),176,76,0xFFABC4C4);
        g.text(font,Component.translatable("raft_ocean.radio.aerials",menu.value(2)),145,91,0xFFABC4C4);
        int line=0;
        for(var text:font.split(Component.translatable("raft_ocean.radio.status."+menu.value(1)),122)){
            if(line>=2)break;g.text(font,text,145,154+line*10,0xFFE7B97C);line++;
        }
        g.text(font,Component.translatable("raft_ocean.radio.distance",menu.value(8)),19,39,0xFFABC4C4);
        g.text(font,Component.translatable("raft_ocean.radio.facing",menu.value(10)),19,139,0xFFABC4C4);
    }
}
