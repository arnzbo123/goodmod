package com.arn.goodmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class GoodModConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue BED_RANGE;
    public static final ModConfigSpec.BooleanValue FREE_LOOK_TOGGLE;
    public static final ModConfigSpec.BooleanValue AI_CHAT_ENABLED;
    public static final ModConfigSpec.ConfigValue<String> GROQ_API_KEY;
    public static final ModConfigSpec.EnumValue<com.arn.goodmod.ai.GroqModel> GROQ_MODEL;
    public static final ModConfigSpec.ConfigValue<String> CUSTOM_MODEL;
    public static final ModConfigSpec.IntValue AI_CHAT_RADIUS;

    static {
        BUILDER.push("general");
        BED_RANGE = BUILDER
                .comment("Search radius in blocks for nearby beds when initiating bed animations.")
                .translation("goodmod.configuration.bedRange")
                .defineInRange("bedRange", 5, 1, 32);

        FREE_LOOK_TOGGLE = BUILDER
                .comment("Whether pressing the Free-look key acts as a toggle (true) or hold-to-free-look (false).")
                .translation("goodmod.configuration.freeLookToggle")
                .define("freeLookToggle", true);
        BUILDER.pop();

        BUILDER.push("ai");
        AI_CHAT_ENABLED = BUILDER
                .comment("Enable or disable AI chatting with Jenny, Ellie, Bia, and Luna.")
                .translation("goodmod.configuration.aiChatEnabled")
                .define("aiChatEnabled", true);

        GROQ_API_KEY = BUILDER
                .comment("Groq API key for AI chatting with girls (e.g. gsk_...). Get a free key at https://console.groq.com")
                .translation("goodmod.configuration.groqApiKey")
                .define("groqApiKey", "");

        GROQ_MODEL = BUILDER
                .comment("Groq AI model to use. Click in config to cycle through available Groq models.")
                .translation("goodmod.configuration.groqModel")
                .defineEnum("groqModel", com.arn.goodmod.ai.GroqModel.GPT_OSS_20B);

        CUSTOM_MODEL = BUILDER
                .comment("Custom Groq model ID (used when 'Custom Model' is selected above).")
                .translation("goodmod.configuration.customModel")
                .define("customModel", "");

        AI_CHAT_RADIUS = BUILDER
                .comment("Radius in blocks within which girls can hear and respond to chat messages.")
                .translation("goodmod.configuration.aiChatRadius")
                .defineInRange("aiChatRadius", 16, 2, 64);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static String getEffectiveModel() {
        try {
            com.arn.goodmod.ai.GroqModel selected = GROQ_MODEL.get();
            if (selected == com.arn.goodmod.ai.GroqModel.CUSTOM) {
                String custom = CUSTOM_MODEL.get();
                if (custom != null && !custom.trim().isEmpty()) {
                    return custom.trim();
                }
                return com.arn.goodmod.ai.GroqModel.GPT_OSS_20B.getModelId();
            }
            return selected.getModelId();
        } catch (Exception e) {
            return com.arn.goodmod.ai.GroqModel.GPT_OSS_20B.getModelId();
        }
    }

    public static void saveConfig() {
        try {
            if (SPEC.isLoaded()) {
                SPEC.save();
            }
        } catch (Exception ignored) {}
    }
}
