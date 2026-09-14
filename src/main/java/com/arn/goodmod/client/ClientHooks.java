package com.arn.goodmod.client;

import com.arn.goodmod.client.gui.GirlDialogueScreen;
import com.arn.goodmod.entity.BaseGirlEntity;
import net.minecraft.client.Minecraft;

import java.util.Optional;
import java.util.UUID;

public class ClientHooks {
    public static void openDialogue(BaseGirlEntity girl) {
        Minecraft.getInstance().setScreen(new GirlDialogueScreen(girl));
    }

    public static void openNpcEditor(BaseGirlEntity girl) {
        Minecraft.getInstance().setScreen(new com.arn.goodmod.client.gui.NpcEditorScreen(girl));
    }

    public static void copyGirlInfoToClipboard(BaseGirlEntity girl, net.minecraft.world.entity.player.Player player) {
        String clothStr = girl.getClothState() == 1 ? "Nude" : "Dressed";
        String skinStr = girl.getCustomSkin();
        String info = girl.getGirlName() + "'s Skin: " + skinStr + " | Clothing: " + clothStr;
        try {
            Minecraft.getInstance().keyboardHandler.setClipboard(info);
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6[Girl Wand] §e" + info + " §7(Copied to clipboard)"), true);
        } catch (Exception ignored) {}
    }

    public static void handleSceneSync(int entityId, String actionName, Optional<UUID> partnerUUID) {
        ClientGameEvents.onSceneSync(entityId, actionName, partnerUUID);
    }
}
