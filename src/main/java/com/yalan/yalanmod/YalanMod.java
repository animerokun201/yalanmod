package com.yalan.yalanmod;

import com.yalan.yalanmod.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(YalanMod.MODID)
public class YalanMod {
    public static final String MODID = "yalanmod";

    public YalanMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSounds.SOUNDS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModItems.ITEMS.register(bus);
        bus.addListener(ModEntities::onAttributes);
        bus.addListener(ModItems::onCreativeTab);
        bus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(NetworkHandler::register));
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }
}
