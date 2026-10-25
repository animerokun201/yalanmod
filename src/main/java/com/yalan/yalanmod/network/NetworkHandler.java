package com.yalan.yalanmod.network;

import com.yalan.yalanmod.YalanMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(YalanMod.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        CHANNEL.registerMessage(0, YalanPacket.class, YalanPacket::encode, YalanPacket::decode, YalanPacket::handle);
    }

    public static void sendTo(ServerPlayer player, YalanPacket pkt) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), pkt);
    }
}
