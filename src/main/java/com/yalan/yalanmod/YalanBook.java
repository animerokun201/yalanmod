package com.yalan.yalanmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class YalanBook {
    private static final String[] BODY = {
            "GO VALORANT!\n\nGO VALORANT!\n\nGO VALORANT!",
            "GO VALORANT!!!\n\nNo mires atrás.\n\nGO VALORANT!",
            "SOY MUY PRO\n\nGO VALORANT!\n\nSOY MUY PRO",
            "GO BASKET\n\nGO VALORANT!\n\nGO BASKET",
            "Ayayayai!\n\nGO VALORANT!\n\nAyayayai!",
            "GO VALORANT!\nGO VALORANT!\nGO VALORANT!\nGO VALORANT!\nGO VALORANT!"
    };

    public static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "Manual de Yalan");
        tag.putString("author", "ASTRO TEEMO UWU");
        tag.putInt("generation", 0);
        ListTag pages = new ListTag();
        pages.add(page("MANUAL DE INSTRUCCIONES\n\n1. Si Yalan aparece, corre.\n2. Si todo se pone negro, no te muevas... o corre.\n3. GO VALORANT!\n\n- ASTRO TEEMO UWU"));
        for (int i = 1; i < 49; i++) pages.add(page(BODY[i % BODY.length] + "\n\n(pág. " + (i + 1) + ")"));
        pages.add(page("Firmado con cariño:\n\nASTRO TEEMO UWU\n\nGO VALORANT!"));
        tag.put("pages", pages);
        return book;
    }

    private static StringTag page(String text) {
        return StringTag.valueOf(Component.Serializer.toJson(Component.literal(text)));
    }
}
