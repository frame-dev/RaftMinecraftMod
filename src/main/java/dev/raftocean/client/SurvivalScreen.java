package dev.raftocean.client;

import dev.raftocean.entity.RaftPiece;
import dev.raftocean.menu.SurvivalMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original compact nautical UI; all item movement remains in the native container screen. */
public final class SurvivalScreen extends AbstractContainerScreen<SurvivalMenu> {
    private static final int INK = 0xFF152831, PANEL = 0xFF213B43, LIGHT = 0xFFECE9D5, MUTED = 0xFFABC4C4;
    private Button tab;
    private Button pause;
    public SurvivalScreen(SurvivalMenu menu, Inventory inventory, Component title) { super(menu, inventory, title, 246, 222); }

    @Override protected void init() {
        super.init();
        tab = addRenderableWidget(Button.builder(Component.translatable("raft_ocean.ui.supplies"), button -> {
            if (minecraft.gameMode != null && menu.getCarried().isEmpty()) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
        }).bounds(leftPos + 172, topPos + 7, 64, 18)
                .tooltip(Tooltip.create(Component.translatable("raft_ocean.ui.tab_tip"))).build());
        pause = addRenderableWidget(Button.builder(Component.translatable("raft_ocean.ui.pause"), button -> {
            if (minecraft.gameMode != null && menu.getCarried().isEmpty()) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
            }
        }).bounds(leftPos + 12, topPos + 108, 46, 13)
                .tooltip(Tooltip.create(Component.translatable("raft_ocean.ui.pause_tip"))).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        tab.active = menu.getCarried().isEmpty();
        pause.visible = !menu.suppliesPage();
        pause.active = menu.getCarried().isEmpty();
        pause.setMessage(Component.translatable("raft_ocean.ui." + (menu.paused() ? "resume" : "pause")));
        tab.setMessage(Component.translatable("raft_ocean.ui." + (menu.suppliesPage() ? "process" : "supplies")));
        super.extractRenderState(graphics, mouseX, mouseY, partial);
        if (!menu.suppliesPage() && hoveredSlot != null && hoveredSlot.getItem().isEmpty()) {
            var role = menu.layout().get(hoveredSlot.index);
            if (role != null) {
                graphics.setComponentTooltipForNextFrame(font, List.of(
                        Component.translatable("raft_ocean.ui.slot." + role.label()),
                        Component.translatable("raft_ocean.ui." + (role.output() ? "take_only" : "insert_tip"))), mouseX, mouseY);
            }
        }
        if (!menu.suppliesPage() && menu.waterMachine() && mouseX >= leftPos + 145 && mouseX <= leftPos + 184
                && mouseY >= topPos + 74 && mouseY <= topPos + 109) {
            graphics.setComponentTooltipForNextFrame(font, List.of(
                    Component.translatable("raft_ocean.ui.reservoir", menu.cleanWater(), menu.waterCapacity()),
                    Component.translatable("raft_ocean.ui.cup_tip")), mouseX, mouseY);
        }
    }

    @Override public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        super.extractBackground(graphics, mouseX, mouseY, partial);
        graphics.pose().pushMatrix();
        graphics.pose().translate(leftPos, topPos);
        graphics.fill(3, 4, 250, 226, 0x66000000);
        graphics.fill(0, 0, 246, 222, 0xFF6B523A);
        graphics.fill(1, 1, 245, 221, 0xFFA48358);
        graphics.fill(4, 4, 242, 218, INK);
        for (int y = 6; y < 218; y += 12) {
            graphics.fill(1, y, 3, y + 4, 0xFFBC9D6B);
            graphics.fill(243, y, 245, y + 4, 0xFF563C28);
        }
        graphics.fill(7, 6, 239, 27, PANEL);
        graphics.fill(7, 27, 239, 29, accent());
        graphics.fill(10, 34, 237, 121, 0xFF192E36);
        graphics.fill(38, 135, 207, 193, PANEL);
        graphics.fill(38, 195, 207, 215, PANEL);
        for (var slot : menu.slots) {
            if (!slot.isActive()) { continue; }
            boolean output = slot.index < 27 && menu.layout().containsKey(slot.index) && menu.layout().get(slot.index).output();
            int color = output ? accent() : 0xFF58767A;
            graphics.fill(slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, color);
            graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0xFF102229);
            graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 1, 0xFF09191F);
        }
        if (!menu.suppliesPage()) {
            // A segmented progress meter and distinct orange fuel meter never cover item slots.
            meter(graphics, 65, 92, 72, 6, menu.progress(), accent());
            for (int x = 83; x < 137; x += 18) { graphics.fill(x, 92, x + 1, 98, INK); }
            if (menu.kind() == RaftPiece.Kind.BIOFUEL_GENERATOR) { meter(graphics,65,103,72,4,menu.energy()*1000/dev.raftocean.survival.Automation.ENERGY_CAPACITY,0xFFDFBC66); }
            if (menu.heated()) { meter(graphics, 43, 80, 6, 16, menu.fuel(), 0xFFE7A455); }
            if (menu.waterMachine()) {
                if (menu.kind().purifier()) { reservoir(graphics, 150, menu.rawWater(), 0xFF678EAC); }
                reservoir(graphics, 171, menu.cleanWater(), 0xFF70D3DD);
            }
        }
        graphics.pose().popMatrix();
    }

    private int accent() {
        return switch (menu.kind()) {
            case BASIC_GRILL, GRILL, SMELTER -> 0xFFE3A064;
            case PLANTER, TREE_PLANTER -> 0xFF9ABA76;
            case COOKING_POT -> 0xFFD6BE79;
            default -> 0xFF70C8D1;
        };
    }
    private void meter(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int value, int color) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF486067);
        graphics.fill(x, y, x + width, y + height, 0xFF0B1C23);
        if (height > width) { graphics.fill(x, y + height - height * value / 1000, x + width, y + height, color); }
        else { graphics.fill(x, y, x + width * value / 1000, y + height, color); }
    }
    private void reservoir(GuiGraphicsExtractor graphics, int x, int units, int color) {
        for (int i = 0; i < 4; i++) {
            graphics.fill(x, 94 - i * 5, x + 10, 98 - i * 5, i * menu.waterCapacity() < units * 4 ? color : 0xFF3A525B);
        }
    }
    @Override protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, font.plainSubstrByWidth(title.getString(),154), 12, 12, LIGHT);
        graphics.text(font, playerInventoryTitle, 43, 125, MUTED);
        if (menu.suppliesPage()) {
            int y=96;
            for(var line:font.split(Component.translatable("raft_ocean.ui.supply_hint"),194)){
                graphics.text(font,line,25,y,MUTED);y+=9;
            }
            return;
        }
        for (var role : menu.layout().values()) {
            Component label = Component.translatable("raft_ocean.ui.slot." + role.label());
            String text=label.getString();
            int width=role.output()?46:34;
            if(font.width(text)>width){text=font.plainSubstrByWidth(text,width-6)+"…";}
            int x = Math.clamp(role.x() + 8 - font.width(text) / 2,12,239-font.width(text));
            int y=role.y()+20+(role.output() && role.x()==218 && role.y()==44?10:0);
            graphics.text(font, text, x, y, role.output() ? accent() : MUTED);
        }
        if (menu.kind() == RaftPiece.Kind.BIOFUEL_GENERATOR) { graphics.text(font,Component.translatable("raft_ocean.ui.energy",menu.energy()),65,69,LIGHT); }
        if(menu.kind()==RaftPiece.Kind.BATTERY_CHARGER){
            graphics.text(font,Component.translatable("raft_ocean.battery.percent",menu.batteryPercent(1)),171,81,accent());
            graphics.text(font,Component.translatable("raft_ocean.battery.percent",menu.batteryPercent(2)),207,81,accent());
        }
        graphics.text(font, Component.translatable("raft_ocean.ui.inputs"), 22, 33, MUTED);
        graphics.text(font, font.plainSubstrByWidth(Component.translatable(menu.kind()==RaftPiece.Kind.BATTERY_CHARGER?"raft_ocean.battery.sockets":"raft_ocean.ui.outputs").getString(),43), 194, 33, accent());
        graphics.text(font, Component.translatable("raft_ocean.ui.progress", menu.progress() / 10), 65, 79, LIGHT);
        if (menu.waterMachine()) {
            if (menu.kind().purifier()) {
                graphics.text(font, Component.translatable("raft_ocean.ui.sea"), 146, 69, 0xFF94B5CD);
                graphics.text(font, Integer.toString(menu.rawWater()), 152, 102, 0xFF94B5CD);
            }
            graphics.text(font, Component.translatable("raft_ocean.ui.clean"), 167, 69, 0xFF70D3DD);
            graphics.text(font, Integer.toString(menu.cleanWater()), 173, 102, 0xFF70D3DD);
        }
        graphics.text(font, font.plainSubstrByWidth(Component.translatable("raft_ocean.ui.status." + menu.status()).getString(),170), 65, 113,
                menu.status().equals("working") ? accent() : MUTED);
    }
}
