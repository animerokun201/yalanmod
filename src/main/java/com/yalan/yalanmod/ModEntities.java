package com.yalan.yalanmod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, YalanMod.MODID);

    public static final RegistryObject<EntityType<YalanEntity>> YALAN = ENTITIES.register("yalan",
            () -> EntityType.Builder.of(YalanEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.9F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(YalanMod.MODID, "yalan").toString()));

    public static void onAttributes(EntityAttributeCreationEvent e) {
        e.put(YALAN.get(), YalanEntity.createAttributes().build());
    }
}
