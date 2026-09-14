package com.arn.goodmod.client.gui;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.client.ClientGameEvents;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HornyMeterOverlay {
    private static final ResourceLocation HORN_TEX = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/gui/hornymeter.png");

    public static final int FRAME_W = 146;
    public static final int FRAME_H = 175;

    private static float visualHorny = 0.0F;
    private static float thrustPulse = 0.0F;

    public static void onThrust() {
        thrustPulse = 0.22F;
    }

    public static void render(GuiGraphics graphics) {
        BaseGirlEntity girl = ClientGameEvents.getActiveSceneGirl();
        if (girl == null || !girl.isInScene()) {
            visualHorny = 0.0F;
            thrustPulse = 0.0F;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }

        float targetHorny = girl.getHorny();
        visualHorny = Mth.lerp(0.18F, visualHorny, targetHorny);
        if (Math.abs(visualHorny - targetHorny) < 0.05F) {
            visualHorny = targetHorny;
        }

        thrustPulse *= 0.82F;
        if (thrustPulse < 0.005F) {
            thrustPulse = 0.0F;
        }

        float pct = Mth.clamp(visualHorny / 100.0F, 0.0F, 1.0F);

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        float baseScale = 0.58F;
        float dynamicScale = baseScale * (1.0F + thrustPulse);

        float scaledW = FRAME_W * dynamicScale;
        float scaledH = FRAME_H * dynamicScale;
        float posX = screenWidth - scaledW - 16.0F;
        float posY = screenHeight - scaledH - 32.0F;

        PoseStack pose = graphics.pose();
        pose.pushPose();

        float centerX = posX + (scaledW / 2.0F);
        float centerY = posY + (scaledH / 2.0F);
        pose.translate(centerX, centerY, 0.0F);
        pose.scale(dynamicScale / baseScale, dynamicScale / baseScale, 1.0F);
        pose.translate(-centerX, -centerY, 0.0F);

        pose.translate(posX, posY, 0.0F);
        pose.scale(baseScale, baseScale, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. Base frame (empty dark reservoir with borders)
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(HORN_TEX, 0, 0, 0.0F, 0.0F, FRAME_W, FRAME_H, 256, 256);

        // 2. Liquid tint color
        float r = 1.0F;
        float g;
        float b;
        if (pct >= 0.85F) {
            float glow = (float) (Math.sin(System.currentTimeMillis() / 85.0) * 0.15);
            g = Mth.clamp(0.12F + glow, 0.0F, 0.4F);
            b = Mth.clamp(0.65F + glow, 0.4F, 0.9F);
        } else if (pct >= 0.5F) {
            g = 0.10F;
            b = 0.55F;
        } else {
            g = 0.28F;
            b = 0.65F;
        }

        graphics.setColor(r, g, b, 0.92F);

        // 3. Left & right ball fills
        float ballPct = Math.min(1.0F, visualHorny / 25.0F);
        int ballH = Math.round(31.0F * ballPct);
        if (ballH > 0) {
            int ballTopY = 138 + (31 - ballH);
            float ballTexV = 138.0F + (31 - ballH);
            graphics.blit(HORN_TEX, 8, ballTopY, 212.0F, ballTexV, 28, ballH, 256, 256);
            graphics.blit(HORN_TEX, 110, ballTopY, 212.0F, ballTexV, 28, ballH, 256, 256);
        }

        // 4. Center shaft fill
        int shaftH = Math.round(159.0F * pct);
        if (shaftH > 0) {
            int shaftTopY = 8 + (159 - shaftH);
            float shaftTexV = 8.0F + (159 - shaftH);
            graphics.blit(HORN_TEX, 57, shaftTopY, 159.0F, shaftTexV, 32, shaftH, 256, 256);
        }

        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 5. Text labels
        Font font = mc.font;
        boolean isClimax = visualHorny >= 99.0F || girl.getAction().name().contains("CUM");

        if (isClimax) {
            boolean flash = (System.currentTimeMillis() / 200) % 2 == 0;
            int climaxColor = flash ? 0xFFFFD700 : 0xFFFF1493;
            graphics.drawCenteredString(font, Component.literal("★ CLIMAX ★"), 73, 180, climaxColor);
        } else {
            int displayPct = Math.min(100, Math.round(visualHorny));
            graphics.drawCenteredString(font, Component.literal(displayPct + "%"), 73, 180, 0xFFFF69B4);
            graphics.drawCenteredString(font, Component.literal("[SPACE] Thrust"), 73, 192, 0xFFD0D0D0);
        }

        RenderSystem.disableBlend();
        pose.popPose();
    }
}
