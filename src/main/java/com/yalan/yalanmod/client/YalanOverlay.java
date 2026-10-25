package com.yalan.yalanmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yalan.yalanmod.YalanMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Random;

public class YalanOverlay {
    private static final Random RNG = new Random();
    private static final ResourceLocation[] SCREAMS = {
            new ResourceLocation(YalanMod.MODID, "textures/gui/scream_0.png"),
            new ResourceLocation(YalanMod.MODID, "textures/gui/scream_1.png"),
            new ResourceLocation(YalanMod.MODID, "textures/gui/scream_2.png")};
    private static final int[] SCREAM_W = {535, 331, 210};
    private static final int SCREAM_H = 640;

    public static void render(GuiGraphics g, float pt, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (ClientHooks.screamTicks > 0) { drawScream(g, pt, w, h); return; }
        if (!ClientHooks.blackout) return;

        Font font = mc.font;
        float alpha = Math.min(1f, (ClientHooks.blackAge + pt) / 15f);
        if (ClientHooks.fadeOut >= 0) alpha = Math.min(alpha, (ClientHooks.fadeOut - pt) / 40f);
        alpha = Mth.clamp(alpha, 0f, 1f);
        g.fill(0, 0, w, h, ((int) (alpha * 255) << 24));

        int st = ClientHooks.stage;
        float ta = Math.min(1f, (ClientHooks.stepAge + pt) / 10f) * alpha;
        int a = Math.max(8, (int) (ta * 255));
        float sc = 1.6f + 0.7f * st;
        float jit = 0.4f * st;

        var ps = g.pose();
        ps.pushPose();
        ps.translate(w / 2f + (RNG.nextFloat() - 0.5f) * jit, h / 2f + (RNG.nextFloat() - 0.5f) * jit, 0);
        ps.scale(sc, sc, 1f);
        g.drawString(font, ClientHooks.center, -font.width(ClientHooks.center) / 2, -4, (a << 24) | 0xFFFFFF, false);
        ps.popPose();

        float pulse = 0.75f + 0.25f * Mth.sin((ClientHooks.stepAge + pt) * 0.3f);
        int ba = Math.max(8, (int) (ta * pulse * 255));
        ps.pushPose();
        ps.translate(w / 2f, h - 60, 0);
        ps.scale(1.3f, 1.3f, 1f);
        g.drawString(font, ClientHooks.bottom, -font.width(ClientHooks.bottom) / 2, 0, (ba << 24) | 0xAA0000, false);
        ps.popPose();
    }

    private static void drawScream(GuiGraphics g, float pt, int w, int h) {
        int idx = Math.floorMod(ClientHooks.screamIndex, 3);
        float prog = 1f - (ClientHooks.screamTicks - pt) / ClientHooks.SCREAM_LEN;
        g.fill(0, 0, w, h, 0xFF000000);
        float sh = h * (1.0f + 0.5f * prog);
        float sw = sh * SCREAM_W[idx] / SCREAM_H;
        float shake = 6 + 20 * prog;
        int x = (int) ((w - sw) / 2 + (RNG.nextFloat() - 0.5f) * shake);
        int y = (int) ((h - sh) / 2 + (RNG.nextFloat() - 0.5f) * shake);
        RenderSystem.enableBlend();
        g.blit(SCREAMS[idx], x, y, (int) sw, (int) sh, 0, 0, SCREAM_W[idx], SCREAM_H, SCREAM_W[idx], SCREAM_H);
        if ((ClientHooks.screamTicks / 2) % 2 == 0) g.fill(0, 0, w, h, 0x66FF0000);
    }
}
