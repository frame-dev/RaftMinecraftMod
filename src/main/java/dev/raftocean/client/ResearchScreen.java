package dev.raftocean.client;

import dev.raftocean.menu.ResearchMenu;
import dev.raftocean.progression.Research;
import dev.raftocean.progression.ResearchCatalog;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/** Paged material checklist and recipe projects share synchronized server statuses. */
public final class ResearchScreen extends AbstractContainerScreen<ResearchMenu> {
    private final Button[] rows=new Button[6];
    private Button study;
    private int page;
    private boolean materials;
    public ResearchScreen(ResearchMenu menu,Inventory inventory,Component title){super(menu,inventory,title,360,303);}
    private void send(int id){if(minecraft.gameMode!=null){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}}
    @Override protected void init(){
        super.init();
        study=addRenderableWidget(Button.builder(Component.translatable("raft_ocean.research.study"),b->send(ResearchMenu.STUDY_SAMPLE)).bounds(leftPos+10,topPos+89,96,20).build());
        addRenderableWidget(Button.builder(Component.translatable("raft_ocean.research.materials"),b->{materials=true;page=0;}).bounds(leftPos+124,topPos+31,108,20).build());
        addRenderableWidget(Button.builder(Component.translatable("raft_ocean.research.recipes"),b->{materials=false;page=0;}).bounds(leftPos+236,topPos+31,108,20).build());
        addRenderableWidget(Button.builder(Component.literal("<"),b->page=Math.max(0,page-1)).bounds(leftPos+124,topPos+177,25,20).build());
        addRenderableWidget(Button.builder(Component.literal(">"),b->page=Math.min((count()-1)/6,page+1)).bounds(leftPos+319,topPos+177,25,20).build());
        for(int row=0;row<6;row++){
            int offset=row;rows[row]=addRenderableWidget(Button.builder(Component.translatable("raft_ocean.research.learn"),b->send(page*6+offset))
                    .bounds(leftPos+282,topPos+55+row*20,62,18).build());
        }
    }
    private int count(){return materials?ResearchCatalog.MATERIALS.length:Research.TOPICS.length;}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int x,int y,float partial){
        study.active=menu.inputState()==1;
        study.setMessage(Component.translatable(menu.inputState()==2?"raft_ocean.research.known":"raft_ocean.research.study"));
        int material=Research.materialIndex(menu.getSlot(0).getItem());
        study.setTooltip(material<0?null:Tooltip.create(Component.translatable("raft_ocean.research.batch",Research.sampleCost(material))));
        for(int row=0;row<6;row++){
            int index=page*6+row;rows[row].visible=!materials && index<count();
            if(rows[row].visible){int state=menu.state(index);rows[row].active=state==1;
                String key=state==3?"raft_ocean.schematic.locked":state==2?"raft_ocean.research.known":(Research.requiredMaterialCount(index)==0?"raft_ocean.research.free":"raft_ocean.research.learn");
                rows[row].setMessage(Component.translatable(key));
                String tooltip=Research.requiredMaterialCount(index)==0?Component.translatable("raft_ocean.research.description."+Research.TOPICS[index].id()).getString()+"\n"+Component.translatable("raft_ocean.research.free.help").getString():Component.translatable("raft_ocean.research.description."+Research.TOPICS[index].id()).getString();
                int schematic=dev.raftocean.progression.StorySchematics.required(index);
                if(schematic>=0){
                    var plan=dev.raftocean.progression.StorySchematics.PLANS.get(schematic);
                    tooltip+="\n"+Component.translatable("raft_ocean.schematic.requirement",
                            Component.translatable("item.raft_ocean.schematic_"+plan.id()),
                            Component.translatable("raft_ocean.island."+plan.island())).getString();
                }
                rows[row].setTooltip(Tooltip.create(Component.literal(tooltip)));
            }
        }
        super.extractRenderState(graphics,x,y,partial);
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics,int x,int y,float partial){
        super.extractBackground(graphics,x,y,partial);
        graphics.pose().pushMatrix();graphics.pose().translate(leftPos,topPos);
        graphics.fill(0,0,360,303,0xFF967751);graphics.fill(4,4,356,299,0xFF152831);
        graphics.fill(9,28,351,30,0xFF70C8D1);graphics.fill(9,32,110,174,0xFF213B43);
        for(var slot:menu.slots){graphics.fill(slot.x-1,slot.y-1,slot.x+17,slot.y+17,0xFF58767A);graphics.fill(slot.x,slot.y,slot.x+16,slot.y+16,0xFF102229);}
        for(int row=0;row<6;row++){graphics.fill(124,55+row*20,279,73+row*20,0xFF213B43);}
        graphics.pose().popMatrix();
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics,int x,int y){
        graphics.text(font,title,12,13,0xFFECE9D5);
        graphics.text(font,Component.translatable("raft_ocean.research.input"),12,45,0xFFECE9D5);
        for(int row=0;row<6;row++){
            int index=page*6+row;if(index>=count()){break;}
            int color=materials?(menu.materialKnown(index)?0xFF70DDA2:0xFFABC4C4):(menu.state(index)==1 || menu.state(index)==2?0xFF70DDA2:0xFFABC4C4);
            Component label=Component.translatable("raft_ocean.research."+(materials?"material.":"topic.")+
                    (materials?ResearchCatalog.MATERIALS[index].id():Research.TOPICS[index].id()));
            if(materials){
                String item=ResearchCatalog.MATERIALS[index].item();
                if(item.equals("#minecraft:planks")){item="minecraft:oak_planks";}
                if(item.equals("#minecraft:coals")){item="minecraft:coal";}
                graphics.item(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(item)),Research.sampleCost(index)),126,56+row*20);
            }
            if(materials){graphics.text(font,Component.literal("x"+Research.sampleCost(index)),300,60+row*20,color);}
            graphics.text(font,font.plainSubstrByWidth(label.getString(),materials?145:150),materials?147:128,60+row*20,color);
            if(materials && menu.materialKnown(index)){graphics.text(font,Component.literal("+"),330,60+row*20,color);}
        }
        graphics.text(font,Component.literal((page+1)+" / "+((count()+5)/6)),215,183,0xFFECE9D5);
        graphics.text(font,Component.translatable("raft_ocean.research.help"),12,200,0xFF70C8D1);
    }
}
