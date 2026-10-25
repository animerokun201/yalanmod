package com.yalan.yalanmod.network;

import com.yalan.yalanmod.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class YalanPacket {
    public static final int STEP = 0, END = 1, SCREAM = 2;

    public final int type, a, b;
    public final String center, bottom;

    public YalanPacket(int type, int a, int b, String center, String bottom) {
        this.type = type; this.a = a; this.b = b; this.center = center; this.bottom = bottom;
    }

    public static void encode(YalanPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.type); buf.writeVarInt(p.a); buf.writeVarInt(p.b);
        buf.writeUtf(p.center); buf.writeUtf(p.bottom);
    }

    public static YalanPacket decode(FriendlyByteBuf buf) {
        return new YalanPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readUtf(), buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.handle(this)));
        ctx.get().setPacketHandled(true);
    }
}
