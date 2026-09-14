package com.arn.goodmod.client;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.client.renderer.FriendlySlimeRenderer;
import com.arn.goodmod.client.renderer.GirlGeoRenderer;
import com.arn.goodmod.client.renderer.KoboldEggRenderer;
import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.init.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import com.arn.goodmod.client.renderer.ModItemRenderers;
import com.arn.goodmod.init.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = GoodMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            BaseGirlEntity.dialogueOpener = ClientHooks::openDialogue;
            BaseGirlEntity.npcEditorOpener = ClientHooks::openNpcEditor;
            BaseGirlEntity.clipboardCopier = ClientHooks::copyGirlInfoToClipboard;
        });
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ModItemRenderers.getGalathCoinRenderer();
            }
        }, ModItems.GALATH_COIN.get());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ModItemRenderers.getDragonStaffRenderer();
            }
        }, ModItems.DRAGON_STAFF.get());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ModItemRenderers.getAlliesLampRenderer();
            }
        }, ModItems.ALLIES_LAMP.get());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ModItemRenderers.getKoboldEggRenderer();
            }
        }, ModItems.KOBOLD_EGG_ITEM.get());
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.JENNY.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.ELLIE.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.SLIME.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.BIA.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.BEE.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.LUNA.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.ALLIE.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.KOBOLD.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.GOBLIN.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.GALATH.get(), GirlGeoRenderer::new);
        event.registerEntityRenderer(ModEntities.MANGLELIE.get(), GirlGeoRenderer::new);

        event.registerEntityRenderer(ModEntities.ENERGY_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.KOBOLD_EGG.get(), KoboldEggRenderer::new);
        event.registerEntityRenderer(ModEntities.FRIENDLY_SLIME.get(), FriendlySlimeRenderer::new);
    }
}
