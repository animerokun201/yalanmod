package com.yalan.yalanmod;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, YalanMod.MODID);

    /** Virtual_Daigoku.mp3 dura 208.4 s = 4168 ticks */
    public static final RegistryObject<Item> DISC = ITEMS.register("virtual_daigoku_disc",
            () -> new RecordItem(14, ModSounds.DAIGOKU, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 4168));

    public static final RegistryObject<Item> EGG = ITEMS.register("yalan_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.YALAN, 0x120000, 0xC40000, new Item.Properties()));

    public static void onCreativeTab(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(DISC);
        if (e.getTabKey() == CreativeModeTabs.SPAWN_EGGS) e.accept(EGG);
    }
}
