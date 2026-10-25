package com.yalan.yalanmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.yalan.yalanmod.YalanEntity;
import com.yalan.yalanmod.YalanMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Random;

/** Dibuja la foto recortada como un "cartón" que siempre te mira, con animación de balanceo y glitch. */
public class YalanRenderer extends EntityRenderer<YalanEntity> {
    private static final ResourceLocation[] TEX = {
            new ResourceLocation(YalanMod.MODID, "textures/entity/yalan_kid.png"),
            new ResourceLocation(YalanMod.MODID, "textures/entity/yalan_teen.png")};
    private static final float[] HEIGHT = {1.6F, 1.95F};
    private static final float[] ASPECT = {265F / 512F, 168F / 512F};   // ancho/alto de cada imagen
    private static final Random RNG = new Random();

    public YalanRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.4F;
    }

    @Override
    public ResourceLocation getTextureLocation(YalanEntity e) {
        return TEX[Math.floorMod(e.getVariant(), TEX.length)];
    }

    @Override
    public void render(YalanEntity e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        int v = Math.floorMod(e.getVariant(), TEX.length);
        float h = HEIGHT[v], w = h * ASPECT[v];
        float age = e.tickCount + pt;
        boolean moving = e.getDeltaMovement().horizontalDistanceSqr() > 0.0006;

        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(180F - this.entityRenderDispatcher.camera.getYRot()));
        // balanceo (como bailando) y saltitos al correr
        float wob = (float) Math.sin(age * (moving ? 0.9 : 0.15)) * (moving ? 10F : 2.5F);
        ps.mulPose(Axis.ZP.rotationDegrees(wob));
        if (moving) ps.translate(0, Math.abs(Math.sin(age * 0.9)) * 0.14, 0);
        // glitch: salta de lado unos ticks
        if (e.tickCount % 50 < 3) ps.translate((RNG.nextFloat() - 0.5F) * 0.25F, 0, 0);
        ps.scale(1F, 1F + 0.05F * (float) Math.sin(age * 0.35), 1F);

        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(e)));
        PoseStack.Pose pose = ps.last();
        Matrix4f m = pose.pose();
        Matrix3f n = pose.normal();
        int full = 0xF000F0;   // siempre brillante: se ve aunque esté oscuro
        vert(vc, m, n, -w / 2, 0, 0, 1, full);
        vert(vc, m, n, w / 2, 0, 1, 1, full);
        vert(vc, m, n, w / 2, h, 1, 0, full);
        vert(vc, m, n, -w / 2, h, 0, 0, full);
        ps.popPose();
        super.render(e, yaw, pt, ps, buf, light);
    }

    private static void vert(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float u, float v, int light) {
        vc.vertex(m, x, y, 0).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(n, 0, 0, 1).endVertex();
    }
}
