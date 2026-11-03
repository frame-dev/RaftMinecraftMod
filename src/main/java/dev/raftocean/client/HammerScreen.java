package dev.raftocean.client;

import dev.raftocean.menu.HammerMenu;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Category pages keep large rafts and addon catalogs usable without filling the hotbar. */
public final class HammerScreen extends AbstractContainerScreen<HammerMenu> {
    private int category,page;
    private final List<Button> choices=new ArrayList<>();
    private Button previous,next;
    public HammerScreen(HammerMenu menu,Inventory inventory,Component title){super(menu,inventory,title,340,258);}
    private void send(int button){if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,button);}
    private List<Integer> visible(){
        var result=new ArrayList<Integer>();for(int i=0;i<menu.entries().size();i++)if(menu.entries().get(i).category()==category && menu.allowed(i))result.add(i);return result;
    }
    @Override protected void init(){
        super.init();choices.clear();
        for(int i=0;i<4;i++){
            final int tab=i;addRenderableWidget(Button.builder(Component.translatable("raft_ocean.hammer.category."+i),b->{category=tab;page=0;})
                    .bounds(leftPos+10+i*81,topPos+35,78,20).build());
        }
        for(int i=0;i<12;i++){
            final int cell=i;choices.add(addRenderableWidget(Button.builder(Component.empty(),b->{
                var list=visible();int index=page*12+cell;if(index<list.size())send(list.get(index)+1);
            }).bounds(leftPos+10+i%2*162,topPos+61+i/2*23,158,21).build()));
        }
        addRenderableWidget(Button.builder(Component.translatable("raft_ocean.hammer.repair"),b->send(0)).bounds(leftPos+10,topPos+228,74,20).build());
        previous=addRenderableWidget(Button.builder(Component.literal("<"),b->page--).bounds(leftPos+90,topPos+228,24,20).build());
        next=addRenderableWidget(Button.builder(Component.literal(">"),b->page++).bounds(leftPos+118,topPos+228,24,20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float partial){
        var list=visible();int pages=Math.max(1,(list.size()+11)/12);page=Math.clamp(page,0,pages-1);
        previous.active=page>0;next.active=page+1<pages;
        for(int i=0;i<choices.size();i++){
            var button=choices.get(i);int cell=page*12+i;button.visible=cell<list.size();if(!button.visible)continue;
            int index=list.get(cell);String name=font.plainSubstrByWidth(menu.entries().get(index).stack().getHoverName().getString(),111);
            button.setMessage(Component.literal((menu.selected()==index?"▶ ":"")+name+" ×"+menu.count(index)));
        }
        super.extractRenderState(g,x,y,partial);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){
        super.extractBackground(g,x,y,partial);g.pose().pushMatrix();g.pose().translate(leftPos,topPos);
        g.fill(0,0,340,258,0xFF967751);g.fill(4,4,336,254,0xFF152831);g.fill(10,28,330,30,0xFF70C8D1);g.fill(10,201,330,223,0xFF213B43);g.pose().popMatrix();
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){
        g.text(font,title,12,13,0xFFECE9D5);
        g.text(font,Component.translatable("raft_ocean.hammer.prefab"),168,13,0xFFABC4C4);
        int selected=menu.selected();ItemStack stack=selected>=0 && selected<menu.entries().size()?menu.entries().get(selected).stack():new ItemStack(dev.raftocean.registry.ModContent.BUILDING_HAMMER.get());
        g.item(stack,13,204);var label=selected<0?Component.translatable("raft_ocean.hammer.repair_mode"):stack.getHoverName();
        g.text(font,font.plainSubstrByWidth(label.getString(),286),36,209,0xFFE7B97C);
        g.text(font,Component.translatable("raft_ocean.hammer.controls",RaftOceanClient.hammerKey(),RaftOceanClient.rotateKey()),151,234,0xFFABC4C4);
        if(visible().isEmpty())g.text(font,Component.translatable("raft_ocean.hammer.empty"),15,86,0xFFABC4C4);
    }
}
