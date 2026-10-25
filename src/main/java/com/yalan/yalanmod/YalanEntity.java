package com.yalan.yalanmod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import com.yalan.yalanmod.network.NetworkHandler;
import com.yalan.yalanmod.network.YalanPacket;

public class YalanEntity extends Monster {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(YalanEntity.class, EntityDataSerializers.INT);

    private static final int MAX_LIFE = 20 * 100;   // desaparece tras 100 s
    private static final String[] PHRASES = {"¡Ayayayai!", "¡GO VALORANT!", "¡SOY MUY PRO!", "¡GO BASKET!"};

    private int life = 0;
    private int talkCooldown = 40;
    private int screamCooldown = 0;
    private boolean musicStarted = false;
    @Nullable private ServerPlayer musicTarget;

    public YalanEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.entityData.set(VARIANT, this.random.nextInt(2));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(VARIANT, 0);
    }

    public int getVariant() { return this.entityData.get(VARIANT); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, null));
    }

    @Override
    public void setTarget(@Nullable LivingEntity t) {
        LivingEntity old = getTarget();
        super.setTarget(t);
        if (!level().isClientSide && t instanceof ServerPlayer sp && old != t) {
            sp.sendSystemMessage(Component.literal("<Yalan> ").withStyle(ChatFormatting.RED)
                    .append(Component.literal("¡TE ENCONTRÉ!").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        life++;
        if (screamCooldown > 0) screamCooldown--;

        if (getTarget() instanceof ServerPlayer sp) {
            // mensaje persistente mientras caza (barra de acción)
            if (life % 20 == 0) {
                sp.displayClientMessage(Component.literal("☠ YALAN TE ESTÁ CAZANDO ☠")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
            }
            if (!musicStarted) {
                musicStarted = true;
                musicTarget = sp;
                sp.connection.send(new ClientboundSoundEntityPacket(
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.DAIGOKU_CHASE.get()),
                        SoundSource.RECORDS, this, 1.5F, 1.0F, this.random.nextLong()));
            }
            if (life % 40 == 0 && distanceToSqr(sp) < 24 * 24) {
                this.playSound(ModSounds.HEARTBEAT.get(), 1.5F, 1.0F);
            }
            if (--talkCooldown <= 0) {
                talkCooldown = 80 + this.random.nextInt(100);
                int i = this.random.nextInt(PHRASES.length);
                sp.sendSystemMessage(Component.literal("<Yalan> ").withStyle(ChatFormatting.RED)
                        .append(Component.literal(PHRASES[i]).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)));
                this.playSound(ModSounds.voice(i), 2.0F, 0.8F + this.random.nextFloat() * 0.3F);
            }
        }
        if (life > MAX_LIFE) vanish();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean ok = super.doHurtTarget(target);
        if (ok && target instanceof ServerPlayer sp && screamCooldown <= 0) {
            screamCooldown = 20 * 15;
            NetworkHandler.sendTo(sp, new YalanPacket(YalanPacket.SCREAM, this.random.nextInt(3), 0, "", ""));
        }
        return ok;
    }

    /** Se va y deja el libro firmado por ASTRO TEEMO UWU */
    private void vanish() {
        this.spawnAtLocation(YalanBook.create());
        this.discard();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource src, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(src, looting, recentlyHit);
        this.spawnAtLocation(YalanBook.create());
        if (this.random.nextFloat() < 0.25F) this.spawnAtLocation(new ItemStack(ModItems.DISC.get()));
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && musicTarget != null) {
            musicTarget.connection.send(new ClientboundStopSoundPacket(ModSounds.DAIGOKU_CHASE.getId(), SoundSource.RECORDS));
            musicTarget = null;
        }
        super.remove(reason);
    }

    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource s) { return ModSounds.VOICE_AYAYAI.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.SCREAM.get(); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putInt("Life", life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.life = tag.getInt("Life");
    }
}
