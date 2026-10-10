package dev.raftocean.client;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.navigation.RaftNavigation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.util.Locale;

/** North-up compass: amber is hull heading, cyan is wind blowing toward the arrow tip. */
public final class NavigationHud {
    private NavigationHud() { }
    public static void draw(GuiGraphicsExtractor graphics, Minecraft minecraft, RaftEntity raft) {
        var nav = raft.navigation();
        graphics.fill(6, 6, 218, 94, 0xD9182B35);
        graphics.fill(6, 6, 218, 8, 0xFF67C8D5);
        graphics.fill(6, 92, 218, 94, 0xFF304853);
        graphics.fill(79, 16, 80, 86, 0xFF36525D);
        int cx = 42, cy = 45;
        for (int degrees = 0; degrees < 360; degrees += 15) {
            double a = Math.toRadians(degrees);
            int x = cx + (int)Math.round(Math.sin(a)*23), y = cy + (int)Math.round(Math.cos(a)*23);
            graphics.fill(x, y, x+2, y+2, degrees%90==0 ? 0xFFB9CFCC : 0xFF3C616B);
        }
        graphics.text(minecraft.font, Component.literal("N"), cx-3, cy-33, 0xFFECE9D5);
        graphics.text(minecraft.font, Component.literal("S"), cx-3, cy+27, 0xFFABC4C4);
        graphics.text(minecraft.font, Component.literal("W"), cx-33, cy-3, 0xFFABC4C4);
        graphics.text(minecraft.font, Component.literal("E"), cx+29, cy-3, 0xFFABC4C4);
        arrow(graphics, cx, cy, nav.windHeading(), 19, 0xFF69D1DF);
        arrow(graphics, cx, cy, raft.getYRot(), 12, 0xFFF2BD6C);
        graphics.fill(cx-2,cy-2,cx+2,cy+2,0xFFF3E6C7);
        graphics.text(minecraft.font, Component.translatable("raft_ocean.nav.speed"), 90, 15, 0xFF9CBAC1);
        double speed = raft.getDeltaMovement().horizontalDistance() * 20;
        graphics.text(minecraft.font, Component.literal(String.format(Locale.ROOT, "%.1f m/s", speed)), 90, 28, 0xFFECE9D5);
        graphics.fill(90, 41, 207, 45, 0xFF304853);
        graphics.fill(90, 41, 90 + (int)Math.clamp(speed/(dev.raftocean.config.RaftConfig.MAX_SPEED.get()*20)*117, 0, 117), 45, 0xFFF2BD6C);
        graphics.text(minecraft.font, Component.translatable("raft_ocean.nav.wind_label"), 90, 51, 0xFF9CBAC1);
        graphics.text(minecraft.font, Component.literal(cardinal(nav.windHeading()) + " " + (int)RaftNavigation.normalize(nav.windHeading()) + " deg"), 90, 64, 0xFF69D1DF);
        graphics.text(minecraft.font, nav.anchored() ? Component.translatable("raft_ocean.nav.anchor_short")
                : Component.translatable("raft_ocean.nav.drives",nav.sails(),nav.engines()), 90, 79, 0xFFABC4C4);
        if (nav.anchored()) { graphics.text(minecraft.font, Component.literal("A"), 198, 15, 0xFFF2BD6C); }
    }
    private static String cardinal(double yaw) {
        String[] points = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return points[(int)Math.floor((RaftNavigation.normalize(yaw)+22.5)/45)%8];
    }
    private static void arrow(GuiGraphicsExtractor graphics, int cx, int cy, double yaw, int length, int color) {
        var d = RaftNavigation.direction(yaw);
        int x = cx+(int)Math.round(d.x*length), y = cy+(int)Math.round(d.z*length);
        line(graphics,cx,cy,x,y,color);
        int bx = cx+(int)Math.round(d.x*(length-5)), by = cy+(int)Math.round(d.z*(length-5));
        line(graphics,x,y,bx+(int)Math.round(d.z*3),by-(int)Math.round(d.x*3),color);
        line(graphics,x,y,bx-(int)Math.round(d.z*3),by+(int)Math.round(d.x*3),color);
    }
    private static void line(GuiGraphicsExtractor graphics,int x0,int y0,int x1,int y1,int color) {
        int steps=Math.max(Math.abs(x1-x0),Math.abs(y1-y0));
        for(int i=0;i<=steps;i++) {
            double t=steps==0?0:(double)i/steps;
            int x=(int)Math.round(x0+(x1-x0)*t),y=(int)Math.round(y0+(y1-y0)*t);
            graphics.fill(x,y,x+2,y+2,color);
        }
    }
}
