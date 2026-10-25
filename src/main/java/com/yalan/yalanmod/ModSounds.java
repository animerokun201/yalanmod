package com.yalan.yalanmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, YalanMod.MODID);

    private static RegistryObject<SoundEvent> reg(String n) {
        return SOUNDS.register(n, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(YalanMod.MODID, n)));
    }

    public static final RegistryObject<SoundEvent> DAIGOKU = reg("virtual_daigoku");
    public static final RegistryObject<SoundEvent> DAIGOKU_CHASE = reg("virtual_daigoku_chase");
    public static final RegistryObject<SoundEvent> SCREAM = reg("scream");
    public static final RegistryObject<SoundEvent> HEARTBEAT = reg("heartbeat");
    public static final RegistryObject<SoundEvent> DRONE = reg("drone");
    public static final RegistryObject<SoundEvent> VOICE_AYAYAI = reg("voice_ayayayai");
    public static final RegistryObject<SoundEvent> VOICE_VALORANT = reg("voice_govalorant");
    public static final RegistryObject<SoundEvent> VOICE_PRO = reg("voice_somuypro");
    public static final RegistryObject<SoundEvent> VOICE_BASKET = reg("voice_gobasket");

    /** 0 = Ayayayai, 1 = GO VALORANT, 2 = SOY MUY PRO, 3 = GO BASKET */
    public static SoundEvent voice(int i) {
        return switch (Math.floorMod(i, 4)) {
            case 0 -> VOICE_AYAYAI.get();
            case 1 -> VOICE_VALORANT.get();
            case 2 -> VOICE_PRO.get();
            default -> VOICE_BASKET.get();
        };
    }
}
