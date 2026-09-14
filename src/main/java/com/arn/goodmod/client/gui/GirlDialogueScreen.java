package com.arn.goodmod.client.gui;

import com.arn.goodmod.client.skin.CustomSkinManager;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.network.ModPackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class GirlDialogueScreen extends Screen {
    private final BaseGirlEntity girl;
    private int dialogueTopY = 0;

    public GirlDialogueScreen(BaseGirlEntity girl) {
        super(Component.translatable("entity.goodmod." + girl.getGirlName().toLowerCase()));
        this.girl = girl;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int btnWidth = 120;
        int btnHeight = 20;
        int rowHeight = 24;

        // 1. Gather dialogue options: custom skin scenes or fallback to girl's default options
        List<BaseGirlEntity.DialogueOption> options = new ArrayList<>();
        List<CustomSkinManager.CustomScene> activeCustomScenes = new ArrayList<>();

        String customSkin = girl.getCustomSkin();
        if (customSkin != null && !customSkin.isEmpty() && !customSkin.equalsIgnoreCase(CustomSkinManager.DEFAULT_SKIN)) {
            CustomSkinManager.CustomSkin skinData = CustomSkinManager.getSkinData(girl.getGirlName(), customSkin);
            if (skinData != null && skinData.hasCustomScenes()) {
                activeCustomScenes.addAll(skinData.customScenes);
                for (CustomSkinManager.CustomScene scene : skinData.customScenes) {
                    options.add(new BaseGirlEntity.DialogueOption(Component.literal(scene.name), scene.actionState));
                }
            }
        }
        if (options.isEmpty()) {
            options = girl.getDialogueOptions();
        }

        int count = options.size();
        boolean hasStrip = girl.hasOutfitToggle();
        int actionRows = (count <= 1) ? 1 : ((count + 1) / 2);
        int totalHeight = (hasStrip ? rowHeight : 0) + (actionRows * rowHeight) + 6 + btnHeight + 6 + btnHeight;
        int currentY = centerY - (totalHeight / 2);
        this.dialogueTopY = currentY;

        // 2. Strip / Dress up button (if girl supports clothing toggle)
        if (hasStrip) {
            boolean isNude = girl.getClothState() == 1;
            Component outfitText = Component.translatable(isNude ? "action.names.dressup" : "action.names.strip");
            this.addRenderableWidget(Button.builder(outfitText, btn -> {
                int newState = isNude ? 0 : 1;
                girl.setClothState(newState);
                PacketDistributor.sendToServer(new ModPackets.OutfitChangePayload(girl.getId(), newState));
                this.onClose();
            }).bounds(centerX - btnWidth / 2, currentY, btnWidth, btnHeight).build());
            currentY += rowHeight;
        }

        // 3. Dynamic action buttons (showing custom scene names if custom skin is equipped)
        for (int i = 0; i < count; i++) {
            BaseGirlEntity.DialogueOption opt = options.get(i);
            int bx;
            int by;

            if (count == 1) {
                bx = centerX - btnWidth / 2;
                by = currentY;
            } else if (i == count - 1 && (count % 2 != 0)) {
                // Center odd last button
                int row = i / 2;
                bx = centerX - btnWidth / 2;
                by = currentY + (row * rowHeight);
            } else {
                int row = i / 2;
                int col = i % 2;
                bx = (col == 0) ? (centerX - btnWidth - 4) : (centerX + 4);
                by = currentY + (row * rowHeight);
            }

            final int sceneIndex = i;
            this.addRenderableWidget(Button.builder(opt.label(), btn -> {
                if (sceneIndex < activeCustomScenes.size()) {
                    CustomSkinManager.CustomScene cs = activeCustomScenes.get(sceneIndex);
                    if (cs.needsToStrip && girl.hasOutfitToggle() && girl.getClothState() == 0) {
                        girl.setClothState(1);
                        PacketDistributor.sendToServer(new ModPackets.OutfitChangePayload(girl.getId(), 1));
                    }
                }
                com.arn.goodmod.client.ClientGameEvents.startLocalScene(girl, opt.actionState());
                PacketDistributor.sendToServer(new ModPackets.StartScenePayload(girl.getId(), opt.actionState().name()));
                this.onClose();
            }).bounds(bx, by, btnWidth, btnHeight).build());
        }

        int utilityY = currentY + (actionRows * rowHeight) + 6;
        int uBtnW = 76;
        int uSpacing = 4;
        int totalUW = (uBtnW * 3) + (uSpacing * 2);
        int uStartX = centerX - (totalUW / 2);

        // 4. Companion Control Buttons: Follow, Set Home, Go Home
        Component followText = Component.translatable(girl.isFollowing() ? "action.names.stopfollowme" : "action.names.followme");
        this.addRenderableWidget(Button.builder(followText, btn -> {
            PacketDistributor.sendToServer(new ModPackets.ToggleFollowPayload(girl.getId()));
            this.onClose();
        }).bounds(uStartX, utilityY, uBtnW, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.translatable("action.names.setnewhome"), btn -> {
            PacketDistributor.sendToServer(new ModPackets.SetHomePayload(girl.getId()));
            this.onClose();
        }).bounds(uStartX + uBtnW + uSpacing, utilityY, uBtnW, btnHeight).build());

        this.addRenderableWidget(Button.builder(Component.translatable("action.names.gohome"), btn -> {
            PacketDistributor.sendToServer(new ModPackets.GoHomePayload(girl.getId()));
            this.onClose();
        }).bounds(uStartX + (uBtnW + uSpacing) * 2, utilityY, uBtnW, btnHeight).build());

        int cancelY = utilityY + btnHeight + 6;

        // 5. Cancel / Close button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), btn -> this.onClose())
                .bounds(centerX - btnWidth / 2, cancelY, btnWidth, btnHeight).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int titleY = Math.max(10, dialogueTopY - 18);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFF69B4);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
