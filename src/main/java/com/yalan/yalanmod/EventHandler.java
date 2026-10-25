package com.yalan.yalanmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.yalan.yalanmod.network.NetworkHandler;
import com.yalan.yalanmod.network.YalanPacket;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventHandler {
    /** Probabilidad: 1 entre CHANCE cada segundo (900 = aprox. cada 15 min). Baja el número para que salga más seguido. */
    private static final int CHANCE = 900;
    private static final long COOLDOWN = 20L * 60 * 4;   // mínimo 4 min entre apariciones
    private static final int STEP = 40;                   // ticks entre frases en la oscuridad
    private static final int STEPS = 5;

    private static final String[] CENTER = {"Ayayayai...", "GO VALORANT...", "SOY MUY PRO...", "GO BASKET...", "¡AYAYAYAI!"};
    private static final String[] BOTTOM = {
            "Valorant está cerca....",
            "Algo se acerca en la oscuridad....",
            "Yalan está cada vez más cerca....",
            "Escuchas pasos detrás de ti....",
            "CORRE."};
    private static final int[] VOICE = {0, 1, 2, 3, 0};

    private static final Map<UUID, Integer> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> LAST = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide || !(e.player instanceof ServerPlayer sp)) return;
        UUID id = sp.getUUID();
        Integer t = ACTIVE.get(id);
        if (t == null) {
            if (sp.tickCount % 20 != 0 || sp.isSpectator()) return;
            if (sp.level().dimension() != Level.OVERWORLD) return;
            long now = sp.level().getGameTime();
            if (now - LAST.getOrDefault(id, -999999L) < COOLDOWN) return;
            if (sp.getRandom().nextInt(CHANCE) != 0) return;
            if (yalanNear(sp)) return;
            start(sp);
            return;
        }
        if (sp.level().dimension() != Level.OVERWORLD) { finish(sp, false); return; }
        if (t % STEP == 0 && t / STEP < STEPS) {
            int i = t / STEP;
            NetworkHandler.sendTo(sp, new YalanPacket(YalanPacket.STEP, i, VOICE[i], CENTER[i], BOTTOM[i]));
        }
        if (t >= STEP * STEPS + 20) { finish(sp, true); return; }
        ACTIVE.put(id, t + 1);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        ACTIVE.remove(e.getEntity().getUUID());
    }

    private static boolean yalanNear(ServerPlayer sp) {
        return !sp.serverLevel().getEntitiesOfClass(YalanEntity.class, sp.getBoundingBox().inflate(120)).isEmpty();
    }

    public static void start(ServerPlayer sp) {
        ACTIVE.put(sp.getUUID(), 0);
    }

    private static void finish(ServerPlayer sp, boolean spawn) {
        ACTIVE.remove(sp.getUUID());
        LAST.put(sp.getUUID(), sp.level().getGameTime());
        NetworkHandler.sendTo(sp, new YalanPacket(YalanPacket.END, 0, 0, "", ""));
        if (spawn) spawnYalan(sp);
    }

    /** Aparece a 22-32 bloques del jugador: se ve de lejos y da tiempo de reaccionar. */
    public static boolean spawnYalan(ServerPlayer sp) {
        ServerLevel level = sp.serverLevel();
        RandomSource r = sp.getRandom();
        for (int i = 0; i < 40; i++) {
            double ang = r.nextDouble() * Math.PI * 2;
            double dist = 22 + r.nextDouble() * 10;
            int x = Mth.floor(sp.getX() + Math.cos(ang) * dist);
            int z = Mth.floor(sp.getZ() + Math.sin(ang) * dist);
            for (int y = sp.getBlockY() + 6; y >= sp.getBlockY() - 8; y--) {
                BlockPos p = new BlockPos(x, y, z);
                if (!level.hasChunkAt(p)) break;
                if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()
                        && level.getBlockState(p.below()).blocksMotion()) {
                    YalanEntity yalan = ModEntities.YALAN.get().create(level);
                    if (yalan == null) return false;
                    yalan.moveTo(x + 0.5, y, z + 0.5, r.nextFloat() * 360F, 0F);
                    yalan.setTarget(sp);
                    level.addFreshEntity(yalan);
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("yalan").requires(s -> s.hasPermission(2))
                .then(Commands.literal("evento").executes(c -> {
                    start(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("aparecer").executes(c -> {
                    spawnYalan(c.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("screamer").executes(c -> {
                    NetworkHandler.sendTo(c.getSource().getPlayerOrException(),
                            new YalanPacket(YalanPacket.SCREAM, c.getSource().getLevel().random.nextInt(3), 0, "", ""));
                    return 1;
                }))
                .then(Commands.literal("libro").executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    p.getInventory().add(YalanBook.create());
                    return 1;
                })));
    }
}
