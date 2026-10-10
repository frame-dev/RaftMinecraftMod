package dev.raftocean.client;

import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.NavigationMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class NavigationScreen extends AbstractContainerScreen<NavigationMenu> {
    private Button toggle;
    private Button reverse;
    public NavigationScreen(NavigationMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, 246, 222); }
    @Override protected void init() {
        super.init();
        if(menu.kind()==RaftPiece.Kind.ENGINE){
            reverse=addRenderableWidget(Button.builder(Component.empty(),button->send(10))
                    .bounds(leftPos+140,topPos+105,92,18).build());
        }
        if (menu.kind() != RaftPiece.Kind.STEERING_WHEEL) {
            toggle = addRenderableWidget(Button.builder(Component.empty(), button -> send(0)).bounds(leftPos+140, topPos+(menu.kind()==RaftPiece.Kind.SAIL?105:83), 92, 18).build());
        }
        if (menu.kind() == RaftPiece.Kind.SAIL || menu.kind() == RaftPiece.Kind.STEERING_WHEEL) {
            int[] ids={1,5,3,4,6,2};String[] labels={"-15°","-5°","-1°","+1°","+5°","+15°"};
            for(int i=0;i<ids.length;i++) {
                int id=ids[i];addRenderableWidget(Button.builder(Component.literal(labels[i]),button->send(id))
                        .bounds(leftPos+12+i*37,topPos+83,34,18).build());
            }
            addRenderableWidget(Button.builder(Component.literal("-45°"),button->send(7)).bounds(leftPos+12,topPos+105,37,18).build());
            addRenderableWidget(Button.builder(Component.literal("+45°"),button->send(8)).bounds(leftPos+52,topPos+105,37,18).build());
            addRenderableWidget(Button.builder(Component.translatable("raft_ocean.nav.center"),button->send(9)).bounds(leftPos+92,topPos+105,44,18).build());
        }
    }
    private void send(int button) { if (minecraft.gameMode != null) { minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button); } }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, float partial) {
        if(reverse!=null){reverse.setMessage(Component.translatable(menu.trim()==0?"raft_ocean.nav.forward":"raft_ocean.nav.reverse"));}
        if (toggle != null) {
            String action = switch (menu.kind()) {
                case DISPOSABLE_ANCHOR -> menu.enabled() ? "discard_anchor" : "lower";
                case ANCHOR -> menu.enabled() ? "raise" : "lower";
                case SAIL -> menu.enabled() ? "fold" : "deploy";
                default -> menu.enabled() ? "stop" : "start";
            };
            toggle.setMessage(Component.translatable("raft_ocean.nav." + action));
        }
        super.extractRenderState(graphics, x, y, partial);
    }
    @Override public void extractBackground(GuiGraphicsExtractor graphics, int x, int y, float partial) {
        super.extractBackground(graphics, x, y, partial);
        graphics.pose().pushMatrix(); graphics.pose().translate(leftPos, topPos);
        graphics.fill(3, 4, 249, 226, 0x66000000); graphics.fill(0, 0, 246, 222, 0xFFA48358);
        graphics.fill(4, 4, 242, 218, 0xFF152831); graphics.fill(8, 8, 238, 28, 0xFF213B43);
        graphics.fill(8, 28, 238, 30, 0xFF70C8D1); graphics.fill(10, 35, 236, 119, 0xFF192E36);
        for (var slot : menu.slots) {
            if (slot.isActive()) {
                graphics.fill(slot.x-1, slot.y-1, slot.x+17, slot.y+17, 0xFF58767A);
                graphics.fill(slot.x, slot.y, slot.x+16, slot.y+16, 0xFF102229);
            }
        }
        if (menu.kind() == RaftPiece.Kind.ENGINE) {
            graphics.fill(46, 59, 53, 75, 0xFF3A525B);
            graphics.fill(46, 75 - menu.fuel()*16/1000, 53, 75, 0xFFE7A455);
        }
        graphics.pose().popMatrix();
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.text(font, title, 13, 14, 0xFFECE9D5);
        graphics.text(font, Component.translatable(menu.kind()==RaftPiece.Kind.ENGINE?"raft_ocean.nav.thrust":"raft_ocean.nav.heading", menu.heading()), 75, 40, 0xFFECE9D5);
        graphics.text(font, Component.translatable("raft_ocean.nav.wind", menu.wind(), menu.windPower()), 75, 53, 0xFF70C8D1);
        if (menu.kind() == RaftPiece.Kind.SAIL) {
            graphics.text(font, Component.translatable("raft_ocean.nav.trim", menu.trim()), 75, 67, 0xFFABC4C4);
        } else if (menu.kind() == RaftPiece.Kind.ENGINE) {
            graphics.text(font, Component.translatable("raft_ocean.ui.slot.fuel"), 21, 81, 0xFFE7A455);
            graphics.text(font, Component.translatable("raft_ocean.nav.fuel", menu.fuel()/10), 75, 67, 0xFFE7A455);
        }
        String state = menu.anchored() ? "anchored" : menu.kind() == RaftPiece.Kind.STEERING_WHEEL ? "steer_hint"
                : menu.kind() == RaftPiece.Kind.ENGINE && menu.enabled() && menu.fuel() == 0 ? "needs_fuel"
                : menu.enabled() ? "enabled" : "disabled";
        if(menu.kind()!=RaftPiece.Kind.SAIL && menu.kind()!=RaftPiece.Kind.STEERING_WHEEL) {
            int lineY=104;
            for(var line:font.split(Component.translatable("raft_ocean.nav."+state),120)){
                graphics.text(font,line,14,lineY,0xFFABC4C4);lineY+=9;
            }
        } else if(menu.anchored()) {
            graphics.text(font,Component.translatable("raft_ocean.nav.anchored"),12,67,0xFFE7A455);
        }
        graphics.text(font, playerInventoryTitle, 43, 125, 0xFFABC4C4);
    }
}
