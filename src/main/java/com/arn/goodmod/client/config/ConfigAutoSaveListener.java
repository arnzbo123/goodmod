package com.arn.goodmod.client.config;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.config.GoodModConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

@EventBusSubscriber(modid = GoodMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ConfigAutoSaveListener {

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof ConfigurationScreen
                || event.getScreen() instanceof ConfigurationScreen.ConfigurationSectionScreen) {
            GoodModConfig.saveConfig();
        }
    }
}
