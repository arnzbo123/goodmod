package com.arn.goodmod.ai;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.TranslatableEnum;

public enum GroqModel implements TranslatableEnum {
    GPT_OSS_20B("openai/gpt-oss-20b", "GPT-OSS 20B (Fast & Balanced)"),
    GPT_OSS_120B("openai/gpt-oss-120b", "GPT-OSS 120B (Smartest)"),
    QWEN_3_8_27B("qwen/qwen3.8-27b", "Qwen 3.8 27B (Best Roleplay)"),
    QWEN_3_6_27B("qwen/qwen3.6-27b", "Qwen 3.6 27B"),
    COMPOUND_MINI("groq/compound-mini", "Groq Compound Mini (Fastest)"),
    COMPOUND("groq/compound", "Groq Compound"),
    CANOPY_ORPHEUS("canopylabs/orpheus-v1-english", "Canopy Orpheus English"),
    CUSTOM("custom", "Custom Model (Type Below)");

    private final String modelId;
    private final String displayName;

    GroqModel(String modelId, String displayName) {
        this.modelId = modelId;
        this.displayName = displayName;
    }

    public String getModelId() {
        return modelId;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Component getTranslatedName() {
        return Component.literal(this.displayName);
    }
}
