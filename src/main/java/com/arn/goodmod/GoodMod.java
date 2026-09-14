package com.arn.goodmod;

import com.arn.goodmod.config.GoodModConfig;
import com.arn.goodmod.init.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GoodMod.MODID)
public class GoodMod {
    public static final String MODID = "goodmod";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public GoodMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Goodcraft 1.21.1 NeoForge...");

        modContainer.registerConfig(ModConfig.Type.COMMON, GoodModConfig.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModEffects.POTIONS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
