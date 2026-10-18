package dev.raftocean.client;

import dev.raftocean.menu.NotebookMenu;
import dev.raftocean.story.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

/** Paged native text keeps original lore readable in both supported languages. */
public final class NotebookScreen extends AbstractContainerScreen<NotebookMenu> {
    private int category,entry,page;
    private Button previous,next;
    public NotebookScreen(NotebookMenu menu,Inventory inventory,Component title){super(menu,inventory,title,320,236);}
    @Override protected void init(){
        super.init();
        for(int i=0;i<3;i++){
            final int tab=i;
            addRenderableWidget(Button.builder(Component.translatable("raft_ocean.notebook.tab."+i),b->{category=tab;entry=page=0;})
                    .bounds(leftPos+10,topPos+43+i*25,91,20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("<"),b->changeEntry(-1)).bounds(leftPos+111,topPos+201,24,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->changeEntry(1)).bounds(leftPos+157,topPos+201,24,20).build());
        previous=addRenderableWidget(Button.builder(Component.literal("<"),b->page--).bounds(leftPos+208,topPos+201,24,20).build());
        next=addRenderableWidget(Button.builder(Component.literal(">"),b->page++).bounds(leftPos+282,topPos+201,24,20).build());
    }
    private List<Integer> entries(){
        List<Integer> result=new ArrayList<>();
        if(category==0){result.add(-1);for(int i=0;i<menu.journey();i++)result.add(i);}
        else if(category==1){for(int i=0;i<ExpeditionNotebook.NOTE_COUNT;i++)if((menu.notes()&1<<i)!=0)result.add(i);}
        else for(int i=0;i<3;i++)if((menu.branches()&1<<i)!=0)result.add(i);
        return result;
    }
    private void changeEntry(int delta){var list=entries();entry=list.isEmpty()?0:Math.floorMod(entry+delta,list.size());page=0;}
    private Component heading(int index){
        return category==0?Component.translatable(index<0?"raft_ocean.notebook.route":"raft_ocean.island."+ReceiverItem.DESTINATIONS[index]):
                category==1?Component.translatable(ExpeditionNotebook.noteKey(index)+".title"):
                Component.translatable("raft_ocean.island."+Exploration.ROUTES[index]);
    }
    private Component body(int index){
        if(category==0 && index<0){
            Component signal=menu.journey()<ReceiverItem.DESTINATIONS.length?
                    Component.translatable("raft_ocean.island."+ReceiverItem.DESTINATIONS[menu.journey()]):Component.translatable("raft_ocean.radio.open_channel");
            return Component.translatable("raft_ocean.notebook.overview",signal,menu.journey(),ReceiverItem.DESTINATIONS.length,Integer.bitCount(menu.notes()),ExpeditionNotebook.NOTE_COUNT);
        }
        return category==0?Component.translatable("raft_ocean.story.log."+index):category==2?
                Component.translatable("raft_ocean.exploration.log."+index):
                Component.translatable(ExpeditionNotebook.noteKey(index)+".page").append("\n\n")
                        .append(Component.translatable(ExpeditionNotebook.noteKey(index)+".context"));
    }
    private List<FormattedCharSequence> lines(){var list=entries();if(list.isEmpty())return font.split(Component.translatable("raft_ocean.notebook.empty"),184);
        entry=Math.clamp(entry,0,list.size()-1);return font.split(body(list.get(entry)),184);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float partial){
        int pages=Math.max(1,(lines().size()+12)/13);page=Math.clamp(page,0,pages-1);
        previous.active=page>0;next.active=page+1<pages;super.extractRenderState(g,x,y,partial);
    }
    @Override public void extractBackground(GuiGraphicsExtractor g,int x,int y,float partial){
        super.extractBackground(g,x,y,partial);g.pose().pushMatrix();g.pose().translate(leftPos,topPos);
        g.fill(0,0,320,236,0xFF694A32);g.fill(4,4,316,232,0xFF182D35);
        g.fill(106,36,310,225,0xFFE9DAB3);g.fill(109,39,111,204,0xFFB69866);
        g.fill(10,30,310,32,0xFF78C9CC);g.pose().popMatrix();
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int x,int y){
        g.text(font,title,12,13,0xFFE9DAB3);var list=entries();
        Component heading=list.isEmpty()?Component.translatable("raft_ocean.notebook.tab."+category):heading(list.get(Math.clamp(entry,0,list.size()-1)));
        int row=0;for(var line:font.split(heading,184)){if(row>=2)break;g.text(font,line,118,43+row*10,0xFF384F54);row++;}
        var text=lines();for(int i=0;i<13 && page*13+i<text.size();i++)g.text(font,text.get(page*13+i),118,70+i*10,0xFF493B2B);
        g.text(font,Component.literal((list.isEmpty()?0:entry+1)+"/"+list.size()),138,207,0xFF493B2B);
        g.text(font,Component.literal((page+1)+"/"+Math.max(1,(text.size()+12)/13)),240,207,0xFF493B2B);
    }
}
