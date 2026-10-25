package com.yalan.yalanmod.client;

import com.yalan.yalanmod.ModSounds;
import com.yalan.yalanmod.network.YalanPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

/** Estado del lado cliente: apagón, frases y screamers. */
public class ClientHooks {
    public static boolean blackout = false;
    public static int blackAge = 0, stepAge = 0, stage = 0, fadeOut = -1;
    public static String center = "", bottom = "";
    public static int screamTicks = 0, screamIndex = 0;
    public static final int SCREAM_LEN = 45;

    public static void handle(YalanPacket p) {
        Minecraft mc = Minecraft.getInstance();
        switch (p.type) {
            case YalanPacket.STEP -> {
                if (!blackout) blackAge = 0;
                blackout = true;
                fadeOut = -1;
                center = p.center; bottom = p.bottom; stage = p.a; stepAge = 0;
                float vol = 0.35F + 0.25F * p.a;
                mc.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.voice(p.b), 0.85F, vol));
                mc.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.HEARTBEAT.get(), 1.0F, 0.6F + 0.2F * p.a));
                if (p.a == 0) mc.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.DRONE.get(), 1.0F, 0.8F));
            }
            case YalanPacket.END -> { if (blackout) fadeOut = 40; }
            case YalanPacket.SCREAM -> {
                screamIndex = p.a;
                screamTicks = SCREAM_LEN;
                mc.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.SCREAM.get(), 1.0F, 1.5F));
            }
        }
    }

    public static void tick() {
        if (blackout) {
            blackAge++; stepAge++;
            if (fadeOut > 0) { fadeOut--; if (fadeOut == 0) { blackout = false; fadeOut = -1; } }
        }
        if (screamTicks > 0) screamTicks--;
    }
}
