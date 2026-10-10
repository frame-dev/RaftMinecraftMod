package dev.raftocean.client;

import dev.raftocean.survival.Thirst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Ten original pixel water drops, half-drop precision and a readable low-water warning. */
public final class ThirstHud {
    private ThirstHud() { }
    public static void draw(GuiGraphicsExtractor graphics, Minecraft minecraft) {
        int amount = Math.clamp(Thirst.value(minecraft.player), 0, 20);
        int x = graphics.guiWidth()/2+8, y = graphics.guiHeight()-70;
        boolean low = amount <= 4;
        int fill = low ? 0xFFFFA65F : 0xFF59D3EB;
        graphics.fill(x-3,y-3,x+96,y+23,0xD9182B35);
        graphics.fill(x-3,y-3,x+96,y-2,low ? 0xFFE89856 : 0xFF409FB8);
        graphics.text(minecraft.font,Component.translatable("raft_ocean.hud.hydration",amount*5),x,y,0xFFF0F6F8);
        for (int drop=0;drop<10;drop++) {
            int dx=x+drop*9, dy=y+11;
            graphics.fill(dx+3,dy,dx+4,dy+2,0xFF426575);
            graphics.fill(dx+2,dy+2,dx+5,dy+4,0xFF426575);
            graphics.fill(dx+1,dy+4,dx+6,dy+8,0xFF426575);
            graphics.fill(dx+2,dy+8,dx+5,dy+9,0xFF426575);
            int points=Math.clamp(amount-drop*2,0,2);
            if(points>0) {
                graphics.fill(dx+2,dy+5,dx+5,dy+8,fill);
                if(points==2) {
                    graphics.fill(dx+3,dy+1,dx+4,dy+4,fill);
                    graphics.fill(dx+2,dy+4,dx+5,dy+5,fill);
                    graphics.fill(dx+2,dy+5,dx+3,dy+6,0xFFCDF8FF);
                }
            }
        }
        if(low) {
            int color=(minecraft.player.tickCount/10)%2==0 ? 0xFFFFCE91 : 0xFFFFA65F;
            graphics.text(minecraft.font,Component.translatable("raft_ocean.hud.drink"),x,y-14,color);
        }
    }
}
