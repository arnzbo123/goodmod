package com.arn.goodmod.client.gui;

import com.arn.goodmod.ai.GirlPersonality;
import com.arn.goodmod.client.skin.CustomSkinManager;
import com.arn.goodmod.entity.ActionState;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.inventory.GirlInventoryMenu;
import com.arn.goodmod.network.ModPackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class GirlScreen extends AbstractContainerScreen<GirlInventoryMenu> {
    private final BaseGirlEntity girl;
    private int currentTab = 0; // 0 = Inventory & Actions, 1 = AI Personality

    // Tabs
    private Button tabInventoryBtn;
    private Button tabPersonalityBtn;

    // Tab 0 Widgets (Inventory & Companion Actions)
    private final List<AbstractWidget> tab0Widgets = new ArrayList<>();
    private Button clothToggleBtn;
    private Button followToggleBtn;
    private int actionPage = 0;
    private final List<BaseGirlEntity.DialogueOption> allActionOptions = new ArrayList<>();
    private final List<CustomSkinManager.CustomScene> activeCustomScenes = new ArrayList<>();
    private final List<Button> actionButtons = new ArrayList<>();
    private Button prevActionPageBtn;
    private Button nextActionPageBtn;

    // Tab 1 Widgets (Personality Editor)
    private final List<AbstractWidget> tab1Widgets = new ArrayList<>();
    private MultiLineEditBox personalityEditBox;
    private Button savePersonalityBtn;
    private Button resetPersonalityBtn;
    private Button clearPersonalityBtn;

    public GirlScreen(GirlInventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.girl = menu.getGirl();
        this.imageWidth = 346;
        this.imageHeight = 188;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 90;
    }

    @Override
    protected void init() {
        super.init();

        this.tab0Widgets.clear();
        this.tab1Widgets.clear();
        this.actionButtons.clear();
        this.allActionOptions.clear();
        this.activeCustomScenes.clear();

        // Adaptive topPos and leftPos ensuring tabs stay on screen regardless of GUI scale
        int desiredTop = (this.height - this.imageHeight) / 2;
        this.topPos = Math.max(22, desiredTop);
        if (this.topPos + this.imageHeight > this.height) {
            this.topPos = Math.max(20, this.height - this.imageHeight - 2);
        }
        int desiredLeft = (this.width - this.imageWidth) / 2;
        this.leftPos = Math.max(2, desiredLeft);

        int left = this.leftPos;
        int top = this.topPos;

        // 1. Tab Navigation Header Buttons
        int tabW = 120;
        int tabH = 17;
        int tabY = Math.max(2, top - 20);
        this.tabInventoryBtn = Button.builder(Component.literal("Inventory & Actions"), btn -> switchTab(0))
                .bounds(left + 2, tabY, tabW, tabH).build();
        this.tabPersonalityBtn = Button.builder(Component.literal("AI Mind Profile"), btn -> switchTab(1))
                .bounds(left + tabW + 6, tabY, tabW, tabH).build();

        // 2. Tab 0: Right Panel Widgets (x starts at left + 180, width = 164)
        int rightX = left + 180;
        int innerX = rightX + 4;
        int innerW = 156;
        int halfW = (innerW - 4) / 2;
        int thirdW = (innerW - 6) / 3;
        int btnH = 15;

        int rightY = top + 15;

        // Outfit Toggle Button (if girl supports clothing toggle)
        if (girl != null && girl.hasOutfitToggle()) {
            this.clothToggleBtn = Button.builder(getClothButtonText(), btn -> {
                int newState = (girl.getClothState() == 1) ? 0 : 1;
                girl.setClothState(newState);
                PacketDistributor.sendToServer(new ModPackets.OutfitChangePayload(girl.getId(), newState));
                btn.setMessage(getClothButtonText());
            }).bounds(innerX, rightY, innerW, btnH).build();
            this.tab0Widgets.add(this.clothToggleBtn);
            rightY += btnH + 2;
        } else {
            rightY += btnH + 2;
        }

        // Companion Toggles: Follow & Set Home
        if (girl != null) {
            this.followToggleBtn = Button.builder(getFollowButtonText(), btn -> {
                PacketDistributor.sendToServer(new ModPackets.ToggleFollowPayload(girl.getId()));
                girl.setFollowing(!girl.isFollowing());
                btn.setMessage(getFollowButtonText());
            }).bounds(innerX, rightY, halfW, btnH).build();
            this.tab0Widgets.add(this.followToggleBtn);

            Button setHomeBtn = Button.builder(Component.literal("Set Home"), btn -> {
                PacketDistributor.sendToServer(new ModPackets.SetHomePayload(girl.getId()));
            }).bounds(innerX + halfW + 4, rightY, halfW, btnH).build();
            this.tab0Widgets.add(setHomeBtn);
            rightY += btnH + 2;

            // Go Home & Stop Task
            Button goHomeBtn = Button.builder(Component.literal("Go Home"), btn -> {
                PacketDistributor.sendToServer(new ModPackets.GoHomePayload(girl.getId()));
            }).bounds(innerX, rightY, halfW, btnH).build();
            this.tab0Widgets.add(goHomeBtn);

            Button stopTaskBtn = Button.builder(Component.literal("Stop Task"), btn -> {
                PacketDistributor.sendToServer(new ModPackets.GirlTaskCommandPayload(girl.getId(), "stop", ""));
                girl.stopHarvestTask();
            }).bounds(innerX + halfW + 4, rightY, halfW, btnH).build();
            this.tab0Widgets.add(stopTaskBtn);
            rightY += btnH + 2;

            // Autonomous Harvest Tasks (Wood, Stone, Ores)
            Button chopWoodBtn = Button.builder(Component.literal("Wood"), btn -> {
                PacketDistributor.sendToServer(new ModPackets.GirlTaskCommandPayload(girl.getId(), "harvest", "wood"));
                girl.startHarvestTask("wood", null);
            }).bounds(innerX, rightY, thirdW, btnH).build();
            this.tab0Widgets.add(chopWoodBtn);

            Button mineStoneBtn = Button.builder(Component.literal("Stone"), btn -> {
                PacketDistributor.sendToServer(new ModPackets.GirlTaskCommandPayload(girl.getId(), "harvest", "stone"));
                girl.startHarvestTask("stone", null);
            }).bounds(innerX + thirdW + 3, rightY, thirdW, btnH).build();
            this.tab0Widgets.add(mineStoneBtn);

            Button mineOreBtn = Button.builder(Component.literal("Ores"), btn -> {
                PacketDistributor.sendToServer(new ModPackets.GirlTaskCommandPayload(girl.getId(), "harvest", "ore"));
                girl.startHarvestTask("ore", null);
            }).bounds(innerX + (thirdW * 2) + 6, rightY, thirdW, btnH).build();
            this.tab0Widgets.add(mineOreBtn);
        }

        // Action Options (Scenes & Animations)
        gatherActionOptions();
        int actionStartY = top + 104;
        int actionBtnH = 15;
        for (int i = 0; i < 4; i++) {
            final int slotIdx = i;
            Button actBtn = Button.builder(Component.empty(), btn -> triggerAction(slotIdx))
                    .bounds(innerX, actionStartY + (i * (actionBtnH + 2)), innerW, actionBtnH).build();
            this.actionButtons.add(actBtn);
            this.tab0Widgets.add(actBtn);
        }

        // Action pagination buttons (compact footer)
        int pageBtnW = 18;
        int pageControlsY = top + 171;
        this.prevActionPageBtn = Button.builder(Component.literal("<"), btn -> {
            if (actionPage > 0) {
                actionPage--;
                updateActionButtons();
            }
        }).bounds(innerX, pageControlsY, pageBtnW, 12).build();
        this.tab0Widgets.add(this.prevActionPageBtn);

        this.nextActionPageBtn = Button.builder(Component.literal(">"), btn -> {
            if ((actionPage + 1) * 4 < allActionOptions.size()) {
                actionPage++;
                updateActionButtons();
            }
        }).bounds(innerX + innerW - pageBtnW, pageControlsY, pageBtnW, 12).build();
        this.tab0Widgets.add(this.nextActionPageBtn);

        // 3. Tab 1: AI Mind Profile Widgets
        int editBoxX = left + 10;
        int editBoxY = top + 27;
        int editBoxW = imageWidth - 20;
        int editBoxH = 98;

        String currentPersonality = (girl != null) ? girl.getCustomPersonality() : "";
        if (currentPersonality.isEmpty() && girl != null) {
            currentPersonality = GirlPersonality.getDefaultPersonality(girl);
        }

        this.personalityEditBox = new MultiLineEditBox(this.font, editBoxX, editBoxY, editBoxW, editBoxH,
                Component.literal("Enter custom system personality prompt..."), Component.empty());
        this.personalityEditBox.setCharacterLimit(2500);
        this.personalityEditBox.setValue(currentPersonality);
        this.tab1Widgets.add(this.personalityEditBox);

        int bottomBtnY = top + 142;
        int pBtnW = (imageWidth - 28) / 3;

        this.savePersonalityBtn = Button.builder(Component.literal("Save Profile"), btn -> {
            if (girl != null) {
                String text = personalityEditBox.getValue().trim();
                girl.setCustomPersonality(text);
                PacketDistributor.sendToServer(new ModPackets.UpdatePersonalityPayload(girl.getId(), text));
            }
        }).bounds(editBoxX, bottomBtnY, pBtnW, 18).build();
        this.tab1Widgets.add(this.savePersonalityBtn);

        this.resetPersonalityBtn = Button.builder(Component.literal("Reset to Default"), btn -> {
            if (girl != null) {
                personalityEditBox.setValue(GirlPersonality.getDefaultPersonality(girl));
            }
        }).bounds(editBoxX + pBtnW + 4, bottomBtnY, pBtnW, 18).build();
        this.tab1Widgets.add(this.resetPersonalityBtn);

        this.clearPersonalityBtn = Button.builder(Component.literal("Clear & Use Default"), btn -> {
            if (girl != null) {
                personalityEditBox.setValue("");
                girl.setCustomPersonality("");
                PacketDistributor.sendToServer(new ModPackets.UpdatePersonalityPayload(girl.getId(), ""));
            }
        }).bounds(editBoxX + (pBtnW * 2) + 8, bottomBtnY, pBtnW, 18).build();
        this.tab1Widgets.add(this.clearPersonalityBtn);

        switchTab(this.currentTab);
    }

    private void switchTab(int tab) {
        this.currentTab = tab;
        this.clearWidgets();

        if (this.tabInventoryBtn != null) {
            this.tabInventoryBtn.active = (tab != 0);
            this.addRenderableWidget(this.tabInventoryBtn);
        }
        if (this.tabPersonalityBtn != null) {
            this.tabPersonalityBtn.active = (tab != 1);
            this.addRenderableWidget(this.tabPersonalityBtn);
        }

        if (tab == 0) {
            for (AbstractWidget w : this.tab0Widgets) {
                this.addRenderableWidget(w);
            }
            updateActionButtons();
        } else {
            for (AbstractWidget w : this.tab1Widgets) {
                this.addRenderableWidget(w);
            }
        }
    }

    private void gatherActionOptions() {
        this.allActionOptions.clear();
        this.activeCustomScenes.clear();
        if (girl == null) return;

        String customSkin = girl.getCustomSkin();
        if (customSkin != null && !customSkin.isEmpty() && !customSkin.equalsIgnoreCase(CustomSkinManager.DEFAULT_SKIN)) {
            CustomSkinManager.CustomSkin skinData = CustomSkinManager.getSkinData(girl.getGirlName(), customSkin);
            if (skinData != null && skinData.hasCustomScenes()) {
                this.activeCustomScenes.addAll(skinData.customScenes);
                for (CustomSkinManager.CustomScene scene : skinData.customScenes) {
                    this.allActionOptions.add(new BaseGirlEntity.DialogueOption(Component.literal(scene.name), scene.actionState));
                }
            }
        }
        if (this.allActionOptions.isEmpty()) {
            this.allActionOptions.addAll(girl.getDialogueOptions());
        }
    }

    private void updateActionButtons() {
        int total = allActionOptions.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) total / 4));
        if (actionPage >= maxPages) actionPage = maxPages - 1;
        if (actionPage < 0) actionPage = 0;

        for (int i = 0; i < 4; i++) {
            int optIdx = (actionPage * 4) + i;
            Button btn = actionButtons.get(i);
            if (currentTab == 0 && optIdx < total) {
                btn.visible = true;
                btn.active = true;
                btn.setMessage(allActionOptions.get(optIdx).label());
            } else {
                btn.visible = false;
                btn.active = false;
                btn.setMessage(Component.empty());
            }
        }

        boolean hasMultiplePages = (total > 4);
        if (prevActionPageBtn != null) {
            prevActionPageBtn.active = (currentTab == 0 && actionPage > 0);
            prevActionPageBtn.visible = (currentTab == 0 && hasMultiplePages);
        }
        if (nextActionPageBtn != null) {
            nextActionPageBtn.active = (currentTab == 0 && (actionPage + 1) * 4 < total);
            nextActionPageBtn.visible = (currentTab == 0 && hasMultiplePages);
        }
    }

    private void triggerAction(int buttonIndex) {
        if (girl == null) return;
        int optIdx = (actionPage * 4) + buttonIndex;
        if (optIdx >= allActionOptions.size()) return;

        BaseGirlEntity.DialogueOption opt = allActionOptions.get(optIdx);
        if (optIdx < activeCustomScenes.size()) {
            CustomSkinManager.CustomScene cs = activeCustomScenes.get(optIdx);
            if (cs.needsToStrip && girl.hasOutfitToggle() && girl.getClothState() == 0) {
                girl.setClothState(1);
                PacketDistributor.sendToServer(new ModPackets.OutfitChangePayload(girl.getId(), 1));
            }
        }

        com.arn.goodmod.client.ClientGameEvents.startLocalScene(girl, opt.actionState());
        PacketDistributor.sendToServer(new ModPackets.StartScenePayload(girl.getId(), opt.actionState().name()));
        this.onClose();
    }

    private Component getClothButtonText() {
        if (girl == null) return Component.literal("Clothing");
        String stateStr = (girl.getClothState() == 1) ? "Nude" : "Dressed";
        return Component.literal("Outfit: " + stateStr);
    }

    private Component getFollowButtonText() {
        if (girl == null) return Component.literal("Follow");
        return Component.literal("Follow: " + (girl.isFollowing() ? "ON" : "OFF"));
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (this.currentTab == 0) {
            super.renderSlot(guiGraphics, slot);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.currentTab == 0) {
            super.renderTooltip(guiGraphics, x, y);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.currentTab != 0) {
            return this.getChildAt(mouseX, mouseY)
                    .filter(listener -> listener.mouseClicked(mouseX, mouseY, button))
                    .isPresent();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.currentTab == 1 && this.personalityEditBox != null && this.personalityEditBox.isFocused()) {
            if (keyCode == 256) { // ESC key
                this.onClose();
                return true;
            }
            return this.personalityEditBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.currentTab == 1 && this.personalityEditBox != null && this.personalityEditBox.isFocused()) {
            return this.personalityEditBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.currentTab == 0) {
            guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xAAAAAA, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        if (this.currentTab == 0) {
            // Left Panel (Inventory): 176x184
            guiGraphics.fill(left, top, left + 176, top + 184, 0xEE161622);
            guiGraphics.renderOutline(left, top, 176, 184, 0xFF4A4A66);

            // Right Panel (Actions & Companion): 164x184
            guiGraphics.fill(left + 180, top, left + 344, top + 184, 0xEE161622);
            guiGraphics.renderOutline(left + 180, top, 164, 184, 0xFF4A4A66);

            // Render Slot Background Insets
            for (Slot slot : this.menu.slots) {
                int sx = left + slot.x;
                int sy = top + slot.y;
                guiGraphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF2A2A38);
                guiGraphics.fill(sx, sy, sx + 16, sy + 16, 0xFF12121A);
                guiGraphics.renderOutline(sx - 1, sy - 1, 18, 18, 0xFF424258);
            }

            // 3D Girl Preview Inset Frame (between Armor x=26 and Tool x=80)
            int previewX = left + 28;
            int previewY = top + 18;
            int previewW = 49;
            int previewH = 72;
            guiGraphics.fill(previewX, previewY, previewX + previewW, previewY + previewH, 0x900C0C12);
            guiGraphics.renderOutline(previewX, previewY, previewW, previewH, 0xFF404058);

            if (girl != null) {
                girl.tickCount++;
                try {
                    InventoryScreen.renderEntityInInventoryFollowsMouse(
                            guiGraphics, previewX, previewY, previewX + previewW, previewY + previewH,
                            34, 0.0625F, mouseX, mouseY, girl
                    );
                } catch (Exception ignored) {}
            }

            // Section Headers on Right Panel
            int rightX = left + 180;
            guiGraphics.drawString(this.font, Component.literal("COMPANION & AI"), rightX + 6, top + 4, 0xFFFF55, false);

            // Task Status Label
            if (girl != null) {
                String taskStr = "Idle";
                if (girl.hasActiveHarvestTask()) {
                    taskStr = girl.getHarvestTaskType().toUpperCase() +
                            (girl.getHarvestTaskSubtype().isEmpty() ? "" : (" (" + girl.getHarvestTaskSubtype() + ")"));
                }
                guiGraphics.drawString(this.font, Component.literal("Status: " + taskStr), rightX + 6, top + 83, 0x88FFAA, false);
                guiGraphics.drawString(this.font, Component.literal("ACTIONS & SCENES:"), rightX + 6, top + 93, 0xFFAAAA, false);

                if (!allActionOptions.isEmpty() && allActionOptions.size() > 4) {
                    int maxPages = (int) Math.ceil((double) allActionOptions.size() / 4);
                    String pageStr = (actionPage + 1) + "/" + maxPages;
                    guiGraphics.drawCenteredString(this.font, Component.literal(pageStr), rightX + 82, top + 173, 0xAAAAAA);
                }
            }
        } else {
            // Tab 1: AI Mind Profile Full Panel
            guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, 0xF014141E);
            guiGraphics.renderOutline(left, top, imageWidth, imageHeight, 0xFF555577);

            // Header Banner
            String girlTitle = (girl != null ? girl.getGirlName().toUpperCase() : "GIRL") + " AI PERSONALITY SYSTEM PROMPT";
            guiGraphics.drawCenteredString(this.font, Component.literal(girlTitle), left + imageWidth / 2, top + 6, 0xFFFF55);

            Component hint = Component.literal("Customize how she speaks and behaves. Leave blank to use her default personality.");
            guiGraphics.drawCenteredString(this.font, hint, left + imageWidth / 2, top + 16, 0x8888AA);

            // Character count display
            if (personalityEditBox != null) {
                int len = personalityEditBox.getValue().length();
                guiGraphics.drawString(this.font, Component.literal(len + "/2500 characters"), left + imageWidth - 110, top + 128, 0x888888, false);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
