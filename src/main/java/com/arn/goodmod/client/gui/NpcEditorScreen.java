package com.arn.goodmod.client.gui;

import com.arn.goodmod.client.skin.CustomSkinManager;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.network.ModPackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class NpcEditorScreen extends Screen {
    private final BaseGirlEntity girl;
    private List<String> availableSkins;
    private int skinIndex = 0;
    private String selectedSkin;
    private int selectedClothState;

    private Button prevSkinBtn;
    private Button nextSkinBtn;
    private Button clothToggleBtn;

    public NpcEditorScreen(BaseGirlEntity girl) {
        super(Component.translatable("gui.goodmod.npc_editor.title", girl.getName().getString()));
        this.girl = girl;
        this.selectedSkin = girl.getCustomSkin();
        this.selectedClothState = girl.getClothState();
        refreshSkinsList();
    }

    private void refreshSkinsList() {
        this.availableSkins = CustomSkinManager.getAvailableSkins(girl.getGirlName());
        if (availableSkins.isEmpty()) {
            availableSkins.add(CustomSkinManager.DEFAULT_SKIN);
        }
        if (!CustomSkinManager.isValidSkinForGirl(girl.getGirlName(), selectedSkin)) {
            selectedSkin = CustomSkinManager.DEFAULT_SKIN;
            girl.setCustomSkin(selectedSkin);
        }
        this.skinIndex = 0;
        for (int i = 0; i < availableSkins.size(); i++) {
            if (availableSkins.get(i).equalsIgnoreCase(selectedSkin)) {
                this.skinIndex = i;
                break;
            }
        }
        this.selectedSkin = availableSkins.get(skinIndex);
    }

    @Override
    protected void init() {
        int panelWidth = 320;
        int panelHeight = 200;
        int panelLeft = (this.width - panelWidth) / 2;
        int panelTop = (this.height - panelHeight) / 2;

        int rightX = panelLeft + 130;
        int rightY = panelTop + 45;
        int rightWidth = 175;

        // Prev / Next Skin buttons
        int arrowWidth = 24;
        prevSkinBtn = Button.builder(Component.literal("<"), btn -> {
            if (!availableSkins.isEmpty()) {
                skinIndex = (skinIndex - 1 + availableSkins.size()) % availableSkins.size();
                selectedSkin = availableSkins.get(skinIndex);
                girl.setCustomSkin(selectedSkin);
            }
        }).bounds(rightX, rightY, arrowWidth, 20).build();
        this.addRenderableWidget(prevSkinBtn);

        nextSkinBtn = Button.builder(Component.literal(">"), btn -> {
            if (!availableSkins.isEmpty()) {
                skinIndex = (skinIndex + 1) % availableSkins.size();
                selectedSkin = availableSkins.get(skinIndex);
                girl.setCustomSkin(selectedSkin);
            }
        }).bounds(rightX + rightWidth - arrowWidth, rightY, arrowWidth, 20).build();
        this.addRenderableWidget(nextSkinBtn);

        // Clothing Toggle Button
        clothToggleBtn = Button.builder(getClothButtonText(), btn -> {
            selectedClothState = (selectedClothState == 0) ? 1 : 0;
            girl.setClothState(selectedClothState);
            btn.setMessage(getClothButtonText());
        }).bounds(rightX, rightY + 30, rightWidth, 20).build();
        this.addRenderableWidget(clothToggleBtn);

        // Open Skins Folder Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.goodmod.npc_editor.open_folder"), btn -> {
            CustomSkinManager.openSkinsFolder(girl.getGirlName());
        }).bounds(rightX, rightY + 60, rightWidth, 20).build());

        // Reload Skins Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.goodmod.npc_editor.reload_skins"), btn -> {
            CustomSkinManager.reload();
            refreshSkinsList();
            girl.setCustomSkin(selectedSkin);
        }).bounds(rightX, rightY + 85, rightWidth, 20).build());

        // Apply / Done Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
            applyAndClose();
        }).bounds(rightX, rightY + 115, rightWidth, 20).build());
    }

    private Component getClothButtonText() {
        String stateStr = (selectedClothState == 1) ? "Nude" : "Dressed";
        return Component.literal("Clothing: " + stateStr);
    }

    private void applyAndClose() {
        PacketDistributor.sendToServer(new ModPackets.SetGirlCustomizationPayload(girl.getId(), selectedSkin, selectedClothState));
        this.onClose();
    }

    @Override
    public void onClose() {
        // Ensure server syncs even if closed with ESC
        PacketDistributor.sendToServer(new ModPackets.SetGirlCustomizationPayload(girl.getId(), selectedSkin, selectedClothState));
        super.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. Render vanilla background blur FIRST so it never blurs our custom UI elements!
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // Dark background overlay behind panel
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xD0101014, 0xD0101014);

        int panelWidth = 320;
        int panelHeight = 200;
        int panelLeft = (this.width - panelWidth) / 2;
        int panelTop = (this.height - panelHeight) / 2;

        // Main window panel
        guiGraphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, 0xF0181822);
        guiGraphics.renderOutline(panelLeft, panelTop, panelWidth, panelHeight, 0xFF606080);

        // 3D Preview Frame on left
        int previewX = panelLeft + 15;
        int previewY = panelTop + 30;
        int previewW = 100;
        int previewH = 155;
        guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0x900C0C12);
        guiGraphics.renderOutline(previewX, previewY, previewW, previewH, 0xFF404055);

        // Advance animation tick count for preview entity so it animates in real-time in GUI
        girl.tickCount++;

        // Render Girl Entity in 3D Preview
        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, previewX, previewY, previewX + previewW, previewY + previewH, 50, 0.0625F, mouseX, mouseY, girl);
        } catch (Exception ignored) {}

        // 2. Render buttons (renderable widgets)
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // 3. Render Header Title & labels ON TOP of panel and widgets (razor sharp text)
        Component titleText = Component.literal(girl.getName().getString().toUpperCase() + " CUSTOMIZATION");
        guiGraphics.drawCenteredString(this.font, titleText, panelLeft + panelWidth / 2, panelTop + 10, 0xFFFFFF);

        int rightX = panelLeft + 130;
        int rightY = panelTop + 45;
        int rightWidth = 175;
        int arrowWidth = 24;

        // Label above selector
        guiGraphics.drawString(this.font, Component.literal("Model / Skin Pack:"), rightX, rightY - 12, 0xCCCCCC);

        // Dedicated sleek dark background box for skin name between '<' and '>' buttons
        int boxX = rightX + arrowWidth + 3;
        int boxW = rightWidth - (arrowWidth * 2) - 6;
        guiGraphics.fill(boxX, rightY, boxX + boxW, rightY + 20, 0xFF0D0D14);
        guiGraphics.renderOutline(boxX, rightY, boxW, 20, 0xFF555577);

        // Display skin name centered inside the box
        String fullDisplayName = CustomSkinManager.getSkinDisplayName(girl.getGirlName(), selectedSkin);
        int maxTextWidth = boxW - 8;
        String displaySkin = this.font.plainSubstrByWidth(fullDisplayName, maxTextWidth);
        if (displaySkin.length() < fullDisplayName.length()) {
            displaySkin = this.font.plainSubstrByWidth(fullDisplayName, maxTextWidth - 8) + "...";
        }
        guiGraphics.drawCenteredString(this.font, Component.literal(displaySkin), boxX + boxW / 2, rightY + 6, 0xFFFF55);

        // Tooltip if hovered over skin name box
        if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= rightY && mouseY <= rightY + 20) {
            guiGraphics.renderTooltip(this.font, Component.literal(fullDisplayName), mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
