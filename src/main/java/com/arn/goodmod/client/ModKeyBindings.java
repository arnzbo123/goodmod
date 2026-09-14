package com.arn.goodmod.client;

import com.arn.goodmod.GoodMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = GoodMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {
    public static final String CATEGORY = "key.categories.goodmod";

    public static final KeyMapping KEY_THRUST = new KeyMapping(
            "key.goodmod.thrust",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_SPACE,
            CATEGORY
    );

    public static final KeyMapping KEY_STOP_SCENE = new KeyMapping(
            "key.goodmod.stop_scene",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_SHIFT,
            CATEGORY
    );

    public static final KeyMapping KEY_FREE_LOOK = new KeyMapping(
            "key.goodmod.free_look",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_THRUST);
        event.register(KEY_STOP_SCENE);
        event.register(KEY_FREE_LOOK);
    }
}
