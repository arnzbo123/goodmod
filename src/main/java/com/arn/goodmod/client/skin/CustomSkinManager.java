package com.arn.goodmod.client.skin;

import com.arn.goodmod.GoodMod;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CustomSkinManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomSkinManager.class);
    public static final String DEFAULT_SKIN = "default";

    private static final Map<String, List<String>> GIRL_ALIASES = Map.ofEntries(
            Map.entry("cat", List.of("cat", "luna", "neko")),
            Map.entry("jenny", List.of("jenny")),
            Map.entry("ellie", List.of("ellie")),
            Map.entry("bia", List.of("bia")),
            Map.entry("slime", List.of("slime", "slime_girl", "slimegirl")),
            Map.entry("bee", List.of("bee", "bee_girl", "beegirl")),
            Map.entry("allie", List.of("allie")),
            Map.entry("kobold", List.of("kobold")),
            Map.entry("goblin", List.of("goblin")),
            Map.entry("galath", List.of("galath")),
            Map.entry("manglelie", List.of("manglelie"))
    );

    private static final Set<String> IGNORED_TEXTURE_NAMES = Set.of(
            "hand", "pack", "icon", "gui", "cursor", "shadow", "light", "brush"
    );

    // Cache of discovered skins per girl: canonicalGirl -> Map<skinId, CustomSkin>
    private static final Map<String, Map<String, CustomSkin>> DISCOVERED_SKINS = new HashMap<>();
    private static final Map<ResourceLocation, BakedGeoModel> BAKED_MODEL_REGISTRY = new HashMap<>();
    private static final Map<ResourceLocation, BakedAnimations> BAKED_ANIMATION_REGISTRY = new HashMap<>();
    private static final Map<ResourceLocation, DynamicTexture> ACTIVE_DYNAMIC_TEXTURES = new HashMap<>();
    private static boolean scanned = false;

    public static class CustomScene {
        public final String name;
        public final com.arn.goodmod.entity.ActionState actionState;
        public final List<String> introAnims;
        public final List<String> slowAnims;
        public final List<String> fastAnims;
        public final String cumAnim;
        public final boolean needsToStrip;
        public final boolean isBedScene;
        public final double bedOffset;

        public CustomScene(String name, com.arn.goodmod.entity.ActionState actionState,
                           List<String> introAnims, List<String> slowAnims,
                           List<String> fastAnims, String cumAnim, boolean needsToStrip,
                           boolean isBedScene, double bedOffset) {
            this.name = name;
            this.actionState = actionState;
            this.introAnims = introAnims != null ? introAnims : Collections.emptyList();
            this.slowAnims = slowAnims != null ? slowAnims : Collections.emptyList();
            this.fastAnims = fastAnims != null ? fastAnims : Collections.emptyList();
            this.cumAnim = cumAnim;
            this.needsToStrip = needsToStrip;
            this.isBedScene = isBedScene;
            this.bedOffset = bedOffset;
        }

        public CustomScene(String name, com.arn.goodmod.entity.ActionState actionState,
                           List<String> introAnims, List<String> slowAnims,
                           List<String> fastAnims, String cumAnim, boolean needsToStrip) {
            this(name, actionState, introAnims, slowAnims, fastAnims, cumAnim, needsToStrip, false, 0.0D);
        }

        public boolean matchesAction(com.arn.goodmod.entity.ActionState action) {
            if (this.actionState == action) return true;
            String aName = action.name();
            String myName = this.actionState.name();
            if (myName.contains("BLOWJOB") && aName.contains("BLOWJOB")) return true;
            if (myName.contains("PRONE_DOGGY") && aName.contains("PRONE_DOGGY")) return true;
            if (!myName.contains("PRONE_DOGGY") && myName.contains("DOGGY") && !aName.contains("PRONE_DOGGY") && aName.contains("DOGGY")) return true;
            if (myName.contains("COWGIRL_SITTING") && aName.contains("COWGIRL_SITTING")) return true;
            if (!myName.contains("COWGIRL_SITTING") && myName.contains("COWGIRL") && !aName.contains("COWGIRL_SITTING") && aName.contains("COWGIRL")) return true;
            if (myName.contains("MISSIONARY") && aName.contains("MISSIONARY")) return true;
            if (myName.contains("HUG") && aName.contains("HUG")) return true;
            if (myName.contains("ANAL") && aName.contains("ANAL")) return true;
            if (myName.contains("PAIZURI") && aName.contains("PAIZURI")) return true;
            return false;
        }
    }

    public static class CustomSkin {
        public final String id;
        public final String displayName;
        public final String girlName;
        public final File packFile;

        public final String textureEntry;
        public final String dressedModelEntry;
        public final String nudeModelEntry;
        public final String animationEntry;

        public final List<CustomScene> customScenes = new ArrayList<>();
        private final Map<String, Animation> animationCache = new HashMap<>();

        private ResourceLocation textureLocation;
        private ResourceLocation dressedModelLocation;
        private ResourceLocation nudeModelLocation;
        private ResourceLocation animationLocation;

        private BakedGeoModel bakedDressedModel;
        private BakedGeoModel bakedNudeModel;
        private BakedAnimations bakedAnimations;

        public CustomSkin(String id, String displayName, String girlName, File packFile,
                          String textureEntry, String dressedModelEntry, String nudeModelEntry, String animationEntry) {
            this.id = id;
            this.displayName = displayName;
            this.girlName = girlName;
            this.packFile = packFile;
            this.textureEntry = textureEntry;
            this.dressedModelEntry = dressedModelEntry;
            this.nudeModelEntry = nudeModelEntry;
            this.animationEntry = animationEntry;
        }

        public boolean hasCustomScenes() {
            return !customScenes.isEmpty();
        }

        public CustomScene getCustomSceneForAction(com.arn.goodmod.entity.ActionState action) {
            for (CustomScene scene : customScenes) {
                if (scene.matchesAction(action)) {
                    return scene;
                }
            }
            return null;
        }

        public String resolveAnimation(com.arn.goodmod.entity.ActionState action, boolean isThrusting, float horny, int sceneTicks) {
            CustomScene scene = getCustomSceneForAction(action);
            if (scene != null) {
                String aName = action.name();
                if (aName.contains("CUM") || horny >= 100.0F) {
                    if (scene.cumAnim != null && !scene.cumAnim.isEmpty()) {
                        return scene.cumAnim;
                    }
                }
                // Fast mode only triggers permanently once horny threshold is met or action state advances to fast
                boolean fast = horny >= 50.0F || aName.contains("FAST") || aName.contains("THRUST") || aName.contains("HARD");
                if (fast && !scene.fastAnims.isEmpty()) {
                    return scene.fastAnims.get(0);
                }

                // Intro animation plays during start/intro state until completed or until player thrusts
                if (!scene.introAnims.isEmpty() && (aName.contains("START") || aName.contains("INTRO")) && horny == 0.0F) {
                    String introAnimName = scene.introAnims.get(0);
                    int introLengthTicks = 200; // default 10 seconds
                    Animation bakedIntro = getAnimation(introAnimName);
                    if (bakedIntro != null && bakedIntro.length() > 0) {
                        introLengthTicks = (int) Math.ceil(bakedIntro.length() * 20.0);
                    }
                    if (sceneTicks < introLengthTicks) {
                        return introAnimName;
                    }
                }

                if (!scene.slowAnims.isEmpty()) {
                    return scene.slowAnims.get(0);
                }
                if (!scene.fastAnims.isEmpty()) {
                    return scene.fastAnims.get(0);
                }
            }
            return null;
        }

        public ResourceLocation getTextureLocation() {
            if (textureLocation == null) {
                loadTexture();
            }
            return textureLocation != null ? textureLocation :
                    ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/entity/" + girlName + "/" + girlName + ".png");
        }

        public ResourceLocation getModelLocation(boolean isNude) {
            if (isNude && nudeModelEntry != null) {
                if (nudeModelLocation == null) loadNudeModel();
                return nudeModelLocation;
            }
            if (dressedModelEntry != null) {
                if (dressedModelLocation == null) loadDressedModel();
                return dressedModelLocation;
            }
            if (nudeModelEntry != null) {
                if (nudeModelLocation == null) loadNudeModel();
                return nudeModelLocation;
            }
            return null;
        }

        public boolean hasCustomModel() {
            return dressedModelEntry != null || nudeModelEntry != null;
        }

        public boolean hasCustomAnimations() {
            return animationEntry != null;
        }

        public ResourceLocation getAnimationLocation() {
            if (animationEntry != null && animationLocation == null) {
                loadAnimations();
            }
            return animationLocation;
        }

        public Animation getAnimation(String name) {
            if (animationEntry == null) return null;
            if (bakedAnimations == null) loadAnimations();
            if (bakedAnimations == null) return null;

            if (animationCache.containsKey(name)) {
                return animationCache.get(name);
            }

            // 1. Direct lookup
            Animation anim = bakedAnimations.getAnimation(name);
            if (anim != null) {
                animationCache.put(name, anim);
                return anim;
            }

            // 2. Extract pure name without "animation." and without any character prefix
            String pureName = name;
            if (pureName.startsWith("animation.")) {
                pureName = pureName.substring("animation.".length());
            }
            int dotIndex = pureName.indexOf('.');
            if (dotIndex >= 0) {
                pureName = pureName.substring(dotIndex + 1);
            }
            int lastDot = pureName.lastIndexOf('.');
            if (lastDot >= 0) {
                pureName = pureName.substring(lastDot + 1);
            }

            // 3. Try animation.<skinId>.<pureName> (e.g. animation.power.blowjobsuck)
            anim = bakedAnimations.getAnimation("animation." + this.id + "." + pureName);
            if (anim != null) {
                animationCache.put(name, anim);
                return anim;
            }

            // 4. Try animation.<cleanDisplayName>.<pureName>
            String cleanDisplay = this.displayName.toLowerCase().replaceAll("[^a-z0-9_]", "");
            anim = bakedAnimations.getAnimation("animation." + cleanDisplay + "." + pureName);
            if (anim != null) {
                animationCache.put(name, anim);
                return anim;
            }

            // 5. Try short names: animation.<pureName> or pureName
            anim = bakedAnimations.getAnimation("animation." + pureName);
            if (anim != null) {
                animationCache.put(name, anim);
                return anim;
            }
            anim = bakedAnimations.getAnimation(pureName);
            if (anim != null) {
                animationCache.put(name, anim);
                return anim;
            }

            // 6. Try synonym prefix replacement: e.g. animation.cat.idle <-> animation.luna.idle
            for (Map.Entry<String, List<String>> e : GIRL_ALIASES.entrySet()) {
                List<String> aliases = e.getValue();
                for (String a1 : aliases) {
                    if (name.contains("." + a1 + ".")) {
                        for (String a2 : aliases) {
                            if (!a1.equals(a2)) {
                                String altName = name.replace("." + a1 + ".", "." + a2 + ".");
                                anim = bakedAnimations.getAnimation(altName);
                                if (anim != null) {
                                    animationCache.put(name, anim);
                                    return anim;
                                }
                            }
                        }
                    }
                }
            }

            // 7. Suffix matching across all baked animations in the pack
            String targetSuffix = "." + pureName;
            for (Map.Entry<String, Animation> entry : bakedAnimations.animations().entrySet()) {
                String k = entry.getKey();
                if (k.endsWith(targetSuffix) || k.equals(pureName) || k.equals("animation." + pureName)) {
                    animationCache.put(name, entry.getValue());
                    return entry.getValue();
                }
            }

            return null;
        }

        private void loadTexture() {
            if (textureEntry == null) return;
            try {
                byte[] data = readEntryBytes(packFile, textureEntry);
                try (InputStream is = new ByteArrayInputStream(data)) {
                    NativeImage image = NativeImage.read(is);
                    DynamicTexture dynTex = new DynamicTexture(image);
                    String safeId = id.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
                    this.textureLocation = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID,
                            "textures/entity/" + girlName + "/custom_" + safeId + "_" + (System.currentTimeMillis() % 100000));
                    if (Minecraft.getInstance() != null && Minecraft.getInstance().getTextureManager() != null) {
                        Minecraft.getInstance().getTextureManager().register(this.textureLocation, dynTex);
                    }
                    ACTIVE_DYNAMIC_TEXTURES.put(this.textureLocation, dynTex);
                    LOGGER.info("Loaded custom texture for {}: {} -> {}", girlName, displayName, this.textureLocation);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load custom texture for {}: {}", id, e.getMessage());
            }
        }

        private void loadDressedModel() {
            if (dressedModelEntry == null) return;
            try {
                String json = readEntryString(packFile, dressedModelEntry);
                JsonObject geoJson = GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, json, JsonObject.class);
                Model rawModel = KeyFramesAdapter.GEO_GSON.fromJson(geoJson, Model.class);
                GeometryTree tree = GeometryTree.fromModel(rawModel);
                this.bakedDressedModel = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(tree);

                String safeId = id.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
                this.dressedModelLocation = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID,
                        "geo/custom/" + girlName + "/" + safeId + "_dressed.geo.json");
                BAKED_MODEL_REGISTRY.put(this.dressedModelLocation, this.bakedDressedModel);
                try {
                    GeckoLibCache.getBakedModels().put(this.dressedModelLocation, this.bakedDressedModel);
                } catch (Throwable ignored) {}
                LOGGER.info("Loaded custom dressed model for {}: {} -> {}", girlName, displayName, this.dressedModelLocation);
            } catch (Exception e) {
                LOGGER.error("Failed to bake dressed model for {}: {}", id, e.getMessage(), e);
            }
        }

        private void loadNudeModel() {
            if (nudeModelEntry == null) return;
            try {
                String json = readEntryString(packFile, nudeModelEntry);
                JsonObject geoJson = GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, json, JsonObject.class);
                Model rawModel = KeyFramesAdapter.GEO_GSON.fromJson(geoJson, Model.class);
                GeometryTree tree = GeometryTree.fromModel(rawModel);
                this.bakedNudeModel = BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(tree);

                String safeId = id.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
                this.nudeModelLocation = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID,
                        "geo/custom/" + girlName + "/" + safeId + "_nude.geo.json");
                BAKED_MODEL_REGISTRY.put(this.nudeModelLocation, this.bakedNudeModel);
                try {
                    GeckoLibCache.getBakedModels().put(this.nudeModelLocation, this.bakedNudeModel);
                } catch (Throwable ignored) {}
                LOGGER.info("Loaded custom nude model for {}: {} -> {}", girlName, displayName, this.nudeModelLocation);
            } catch (Exception e) {
                LOGGER.error("Failed to bake nude model for {}: {}", id, e.getMessage(), e);
            }
        }

        private void loadAnimations() {
            if (animationEntry == null) return;
            try {
                String json = readEntryString(packFile, animationEntry);
                JsonObject animJson = GsonHelper.fromJson(KeyFramesAdapter.GEO_GSON, json, JsonObject.class);
                JsonObject animationsObj = GsonHelper.getAsJsonObject(animJson, "animations");
                this.bakedAnimations = KeyFramesAdapter.GEO_GSON.fromJson(animationsObj, BakedAnimations.class);

                String safeId = id.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
                this.animationLocation = ResourceLocation.fromNamespaceAndPath(GoodMod.MODID,
                        "animations/custom/" + girlName + "/" + safeId + ".animation.json");
                BAKED_ANIMATION_REGISTRY.put(this.animationLocation, this.bakedAnimations);
                try {
                    GeckoLibCache.getBakedAnimations().put(this.animationLocation, this.bakedAnimations);
                } catch (Throwable ignored) {}
                LOGGER.info("Loaded custom animations for {}: {} -> {} ({} animations)",
                        girlName, displayName, this.animationLocation, this.bakedAnimations.animations().size());
            } catch (Exception e) {
                LOGGER.error("Failed to bake animations for {}: {}", id, e.getMessage(), e);
            }
        }
    }

    public static File getGameDirectory() {
        try {
            if (Minecraft.getInstance() != null && Minecraft.getInstance().gameDirectory != null) {
                return Minecraft.getInstance().gameDirectory;
            }
        } catch (Throwable ignored) {}
        return new File(".");
    }

    public static File getPrimarySkinsDirectory() {
        File gameDir = getGameDirectory();
        File dir = new File(gameDir, "custom_skin");
        if (!dir.exists()) {
            File workspaceDir = new File("custom_skin");
            if (workspaceDir.exists() && workspaceDir.isDirectory()) {
                dir = workspaceDir;
            } else {
                dir.mkdirs();
                createReadme(dir);
            }
        }
        // Ensure character subdirectories exist for clarity
        for (String girl : GIRL_ALIASES.keySet()) {
            File gDir = new File(dir, girl);
            if (!gDir.exists()) {
                gDir.mkdirs();
                createGirlReadme(gDir, girl);
            }
        }
        return dir;
    }

    private static void createReadme(File dir) {
        File readme = new File(dir, "README.txt");
        if (!readme.exists()) {
            try (FileWriter writer = new FileWriter(readme)) {
                writer.write("=== GoodMod Custom Skins & Models ===\n\n");
                writer.write("Drop your custom skin resource packs (.zip or folders or .png) here!\n");
                writer.write("You can drop packs directly into this folder, or into character-specific\n");
                writer.write("subfolders (e.g. cat/, jenny/, ellie/, etc.).\n\n");
                writer.write("Packs can contain custom .geo.json models, .animation.json animations,\n");
                writer.write("and textures (.png).\n\n");
                writer.write("In-game, right-click any character with the Girl Wand to preview and select skins!\n");
            } catch (Exception ignored) {}
        }
    }

    private static void createGirlReadme(File girlDir, String canonical) {
        File readme = new File(girlDir, "README.txt");
        if (!readme.exists()) {
            try (FileWriter writer = new FileWriter(readme)) {
                String girlName = canonical.substring(0, 1).toUpperCase() + canonical.substring(1);
                writer.write("=== " + girlName + " Custom Skins & Models ===\n\n");
                writer.write("Drop skins, models, or packs (.zip or folders or .png) made specifically for " + girlName + " here!\n");
                writer.write("These skins will only appear when customizing " + girlName + " with the Girl Wand.\n");
            } catch (Exception ignored) {}
        }
    }

    private static String canonicalGirl(String girlName) {
        if (girlName == null) return "default";
        String lower = girlName.toLowerCase();
        for (Map.Entry<String, List<String>> entry : GIRL_ALIASES.entrySet()) {
            if (entry.getKey().equals(lower) || entry.getValue().contains(lower)) {
                return entry.getKey();
            }
        }
        return lower;
    }

    public static synchronized void ensureScanned() {
        if (scanned) return;
        scanAllDirectories();
        scanned = true;
    }

    public static synchronized void reload() {
        for (Map.Entry<ResourceLocation, DynamicTexture> entry : ACTIVE_DYNAMIC_TEXTURES.entrySet()) {
            try {
                if (Minecraft.getInstance() != null && Minecraft.getInstance().getTextureManager() != null) {
                    Minecraft.getInstance().getTextureManager().release(entry.getKey());
                }
            } catch (Exception ignored) {}
        }
        ACTIVE_DYNAMIC_TEXTURES.clear();
        BAKED_MODEL_REGISTRY.clear();
        BAKED_ANIMATION_REGISTRY.clear();
        DISCOVERED_SKINS.clear();
        scanned = false;
        ensureScanned();
        LOGGER.info("Custom skins reloaded successfully.");
    }

    public static List<String> getAvailableSkins(String girlName) {
        ensureScanned();
        String canonical = canonicalGirl(girlName);
        List<String> list = new ArrayList<>();
        list.add(DEFAULT_SKIN);

        Map<String, CustomSkin> map = DISCOVERED_SKINS.get(canonical);
        if (map != null) {
            Set<CustomSkin> seenSkins = new HashSet<>();
            for (CustomSkin skin : map.values()) {
                if (seenSkins.add(skin)) {
                    list.add(skin.id);
                }
            }
        }
        return list;
    }

    public static String resolveCustomAnimation(String girlName, String skinId, com.arn.goodmod.entity.ActionState action, boolean isThrusting, float horny, int sceneTicks) {
        if (skinId == null || skinId.equalsIgnoreCase(DEFAULT_SKIN)) return null;
        CustomSkin skin = getSkinData(girlName, skinId);
        if (skin != null) {
            return skin.resolveAnimation(action, isThrusting, horny, sceneTicks);
        }
        return null;
    }

    public static boolean isCustomBedScene(String girlName, String skinId, com.arn.goodmod.entity.ActionState action) {
        if (skinId == null || skinId.equalsIgnoreCase(DEFAULT_SKIN)) return false;
        CustomSkin skin = getSkinData(girlName, skinId);
        if (skin != null) {
            CustomScene scene = skin.getCustomSceneForAction(action);
            if (scene != null) {
                return scene.isBedScene;
            }
        }
        return false;
    }

    public static String getSkinDisplayName(String girlName, String skinId) {
        if (skinId == null || skinId.isEmpty() || skinId.equalsIgnoreCase(DEFAULT_SKIN)) {
            return "Default";
        }
        CustomSkin skin = getSkinData(girlName, skinId);
        if (skin != null) {
            return skin.displayName;
        }
        return skinId;
    }

    public static CustomSkin getSkinData(String girlName, String skinId) {
        if (skinId == null || skinId.equalsIgnoreCase(DEFAULT_SKIN)) return null;
        ensureScanned();
        String canonical = canonicalGirl(girlName);
        Map<String, CustomSkin> map = DISCOVERED_SKINS.get(canonical);
        if (map != null) {
            return map.get(skinId);
        }
        return null;
    }

    public static ResourceLocation getSkinTexture(String girlName, String skinId) {
        if (skinId == null || skinId.isEmpty() || skinId.equalsIgnoreCase(DEFAULT_SKIN)) {
            String canonical = canonicalGirl(girlName);
            return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/entity/" + canonical + "/" + canonical + ".png");
        }
        CustomSkin skin = getSkinData(girlName, skinId);
        if (skin != null) {
            return skin.getTextureLocation();
        }
        String canonical = canonicalGirl(girlName);
        return ResourceLocation.fromNamespaceAndPath(GoodMod.MODID, "textures/entity/" + canonical + "/" + canonical + ".png");
    }

    public static ResourceLocation getSkinModel(String girlName, String skinId, boolean isNude) {
        CustomSkin skin = getSkinData(girlName, skinId);
        if (skin != null && skin.hasCustomModel()) {
            return skin.getModelLocation(isNude);
        }
        return null;
    }

    public static BakedGeoModel getBakedModel(ResourceLocation location) {
        return BAKED_MODEL_REGISTRY.get(location);
    }

    public static BakedAnimations getBakedAnimations(ResourceLocation location) {
        return BAKED_ANIMATION_REGISTRY.get(location);
    }

    public static boolean isValidSkinForGirl(String girlName, String skinId) {
        if (skinId == null || skinId.isEmpty() || skinId.equalsIgnoreCase(DEFAULT_SKIN)) return true;
        ensureScanned();
        String canonical = canonicalGirl(girlName);
        Map<String, CustomSkin> map = DISCOVERED_SKINS.get(canonical);
        return map != null && map.containsKey(skinId);
    }

    public static void openSkinsFolder(String girlName) {
        File baseDir = getPrimarySkinsDirectory();
        if (girlName != null && !girlName.isEmpty()) {
            String canonical = canonicalGirl(girlName);
            if (canonical != null && !canonical.isEmpty()) {
                File girlDir = new File(baseDir, canonical);
                if (!girlDir.exists()) {
                    girlDir.mkdirs();
                    createGirlReadme(girlDir, canonical);
                }
                Util.getPlatform().openFile(girlDir);
                return;
            }
        }
        Util.getPlatform().openFile(baseDir);
    }

    // --- Scanning Implementation ---

    private static void scanAllDirectories() {
        List<File> directoriesToScan = new ArrayList<>();
        File gameDir = getGameDirectory();

        // 1. Primary custom_skin folder (gameDir and workspace)
        File csDir1 = new File(gameDir, "custom_skin");
        if (csDir1.exists() && csDir1.isDirectory()) directoriesToScan.add(csDir1);
        File csDir2 = new File("custom_skin");
        if (csDir2.exists() && csDir2.isDirectory() && !csDir2.equals(csDir1)) directoriesToScan.add(csDir2);

        // 2. goodmod/skins
        File gmDir = new File(gameDir, "goodmod/skins");
        if (gmDir.exists() && gmDir.isDirectory()) directoriesToScan.add(gmDir);

        // 3. config/goodmod/skins
        File cfgDir = new File(gameDir, "config/goodmod/skins");
        if (cfgDir.exists() && cfgDir.isDirectory()) directoriesToScan.add(cfgDir);

        // 4. sexmod/custom_models (legacy)
        File legacyDir = new File(gameDir, "sexmod/custom_models");
        if (legacyDir.exists() && legacyDir.isDirectory()) directoriesToScan.add(legacyDir);

        // 5. resourcepacks
        File rpDir = new File(gameDir, "resourcepacks");
        if (rpDir.exists() && rpDir.isDirectory()) directoriesToScan.add(rpDir);

        Set<String> processedPackNames = new HashSet<>();

        for (File dir : directoriesToScan) {
            File[] files = dir.listFiles();
            if (files == null) continue;

            for (File file : files) {
                String name = file.getName();
                String lower = name.toLowerCase();

                // Check if this file/folder is a character subfolder: e.g. custom_skin/cat/ or custom_skin/jenny/
                String girlSubfolder = canonicalGirl(lower);
                if (file.isDirectory() && girlSubfolder != null && GIRL_ALIASES.containsKey(girlSubfolder)) {
                    File[] girlFiles = file.listFiles();
                    if (girlFiles != null) {
                        for (File gf : girlFiles) {
                            String gfBase = gf.getName();
                            if (gf.isFile() && gfBase.toLowerCase().endsWith(".zip")) {
                                gfBase = gfBase.substring(0, gfBase.length() - 4);
                            }
                            String gfNorm = (girlSubfolder + "/" + gfBase).toLowerCase();
                            if (gf.isFile() && gf.getName().toLowerCase().endsWith(".zip")) {
                                if (new File(file, gfBase).isDirectory()) {
                                    continue;
                                }
                            }
                            if (processedPackNames.contains(gfNorm)) continue;

                            if (gf.isDirectory() || (gf.isFile() && gf.getName().toLowerCase().endsWith(".zip"))) {
                                scanPack(gf, gfBase, girlSubfolder);
                                processedPackNames.add(gfNorm);
                            } else if (gf.isFile() && gf.getName().toLowerCase().endsWith(".png")) {
                                scanLoosePng(gf, girlSubfolder);
                            }
                        }
                    }
                    continue;
                }

                // Normal pack or file in primary directory
                String baseName = file.getName();
                if (file.isFile() && baseName.toLowerCase().endsWith(".zip")) {
                    baseName = baseName.substring(0, baseName.length() - 4);
                }
                String normalizedBase = baseName.toLowerCase();

                // If unzipped folder exists, avoid duplicate zip of the same pack
                if (file.isFile() && file.getName().toLowerCase().endsWith(".zip")) {
                    if (new File(dir, baseName).isDirectory()) {
                        continue; // Skip zip if directory version is present
                    }
                }
                if (processedPackNames.contains(normalizedBase)) {
                    continue;
                }

                if (file.isDirectory()) {
                    scanPack(file, baseName, null);
                    processedPackNames.add(normalizedBase);
                } else if (file.isFile()) {
                    if (file.getName().toLowerCase().endsWith(".zip")) {
                        scanPack(file, baseName, null);
                        processedPackNames.add(normalizedBase);
                    } else if (file.getName().toLowerCase().endsWith(".png")) {
                        scanLoosePng(file, null);
                    }
                }
            }
        }
    }

    private static void scanLoosePng(File pngFile, String forcedGirl) {
        String fileName = pngFile.getName();
        String nameWithoutExt = fileName.substring(0, fileName.length() - 4);
        if (nameWithoutExt.equalsIgnoreCase(DEFAULT_SKIN)) return;
        if (isIgnoredTextureName(nameWithoutExt)) return;

        String targetGirl = forcedGirl;
        if (targetGirl == null) {
            File parent = pngFile.getParentFile();
            if (parent != null) {
                targetGirl = canonicalGirl(parent.getName());
            }
        }
        if (targetGirl == null) {
            for (Map.Entry<String, List<String>> entry : GIRL_ALIASES.entrySet()) {
                for (String alias : entry.getValue()) {
                    if (nameWithoutExt.toLowerCase().contains(alias)) {
                        targetGirl = entry.getKey();
                        break;
                    }
                }
                if (targetGirl != null) break;
            }
        }

        if (targetGirl != null) {
            CustomSkin skin = new CustomSkin(
                    nameWithoutExt,
                    formatDisplayName(nameWithoutExt),
                    targetGirl,
                    pngFile.getParentFile(),
                    pngFile.getName(),
                    null, null, null
            );
            DISCOVERED_SKINS.computeIfAbsent(targetGirl, k -> new LinkedHashMap<>()).put(skin.id, skin);
            LOGGER.info("Registered loose custom skin for {}: {}", targetGirl, skin.displayName);
        }
    }

    private static void scanPack(File packFile, String packName, String forcedGirl) {
        List<String> entries = getPackEntries(packFile);
        if (entries.isEmpty()) return;

        // Parse optional pack manifest (e.g. power.json in PleasureCraft / PleasureHorizons packs)
        String manifestId = null;
        String manifestName = null;
        List<CustomScene> parsedScenes = new ArrayList<>();

        for (String entryPath : entries) {
            String lower = entryPath.toLowerCase();
            if (lower.endsWith(".json") && !lower.endsWith(".geo.json") && !lower.endsWith(".animation.json")
                    && !lower.endsWith("sounds.json") && !lower.contains("keyframe_events") && !lower.contains("pack.mcmeta")) {
                try {
                    String jsonStr = readEntryString(packFile, entryPath);
                    JsonObject root = GsonHelper.fromJson(new com.google.gson.Gson(), jsonStr, JsonObject.class);
                    if (root != null) {
                        if (root.has("id") && root.get("id").isJsonPrimitive()) {
                            manifestId = root.get("id").getAsString();
                        }
                        if (root.has("name") && root.get("name").isJsonPrimitive()) {
                            manifestName = root.get("name").getAsString();
                        }
                        if (root.has("scenes") && root.get("scenes").isJsonArray()) {
                            JsonArray scenesArr = root.getAsJsonArray("scenes");
                            for (JsonElement scEl : scenesArr) {
                                if (!scEl.isJsonObject()) continue;
                                JsonObject scObj = scEl.getAsJsonObject();
                                String scName = scObj.has("name") ? scObj.get("name").getAsString() : "Custom Scene";
                                List<String> introList = parseAnimList(scObj, "intro_anim");
                                List<String> slowList = parseAnimList(scObj, "slow_anim");
                                List<String> fastList = parseAnimList(scObj, "fast_anim");
                                String cumAnim = parseSingleAnim(scObj, "cum_anim");
                                boolean needsStrip = scObj.has("needs_to_strip") && scObj.get("needs_to_strip").getAsBoolean();
                                boolean isBedScene = (scObj.has("is_bed_scene") && scObj.get("is_bed_scene").getAsBoolean())
                                        || (scObj.has("scene_type") && "on_bed".equalsIgnoreCase(scObj.get("scene_type").getAsString()));
                                double bedOffset = (scObj.has("bed_offset") && scObj.get("bed_offset").isJsonPrimitive())
                                        ? scObj.get("bed_offset").getAsDouble() : 0.0D;
                                com.arn.goodmod.entity.ActionState mappedAction = mapSceneToActionState(scName, introList, slowList, fastList, cumAnim);
                                parsedScenes.add(new CustomScene(scName, mappedAction, introList, slowList, fastList, cumAnim, needsStrip, isBedScene, bedOffset));
                            }
                        }
                        if (manifestId != null || !parsedScenes.isEmpty()) {
                            break;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Determine which girls this pack is designed for
        Set<String> targetGirls = new HashSet<>();
        if (forcedGirl != null) {
            targetGirls.add(forcedGirl);
        } else {
            // Check explicit directory entries in pack (e.g. /cat/, /jenny/, etc.)
            Map<String, Integer> explicitCounts = new HashMap<>();
            for (String entry : entries) {
                String girl = getEntryExplicitGirl(entry);
                if (girl != null) {
                    explicitCounts.put(girl, explicitCounts.getOrDefault(girl, 0) + 1);
                }
            }

            if (!explicitCounts.isEmpty()) {
                targetGirls.addAll(explicitCounts.keySet());
            } else {
                // Check pack name
                String packNameGirl = detectGirlFromName(packName);
                if (packNameGirl != null) {
                    targetGirls.add(packNameGirl);
                } else {
                    // Check inside geo.json or animation.json
                    Set<String> jsonGirls = detectGirlsFromPackContents(packFile, entries);
                    if (!jsonGirls.isEmpty()) {
                        targetGirls.addAll(jsonGirls);
                    }
                }
            }
        }

        if (targetGirls.isEmpty()) {
            return;
        }

        for (String canonical : targetGirls) {
            List<String> aliases = GIRL_ALIASES.get(canonical);
            if (aliases == null) continue;

            // Find matching entries strictly for this girl
            List<String> modelEntries = new ArrayList<>();
            List<String> animEntries = new ArrayList<>();
            List<String> textureEntries = new ArrayList<>();

            for (String entryPath : entries) {
                String lower = entryPath.toLowerCase();

                // If this entry explicitly belongs to ANOTHER girl, skip it (strict isolation!)
                String explicitGirl = getEntryExplicitGirl(entryPath);
                if (explicitGirl != null && !explicitGirl.equals(canonical)) {
                    continue;
                }

                if (lower.endsWith(".geo.json")) {
                    modelEntries.add(entryPath);
                } else if (lower.endsWith(".animation.json")) {
                    animEntries.add(entryPath);
                } else if (lower.endsWith(".png")) {
                    String texName = getFileNameWithoutExt(entryPath).toLowerCase();
                    if (!isIgnoredTextureName(texName)) {
                        textureEntries.add(entryPath);
                    }
                }
            }

            if (textureEntries.isEmpty() && modelEntries.isEmpty()) {
                continue;
            }

            // Determine models (prioritize non-armor base models)
            String dressedModel = null;
            String nudeModel = null;
            String fallbackModel = null;
            for (String m : modelEntries) {
                String lower = m.toLowerCase();
                if (lower.contains("nude")) {
                    nudeModel = m;
                } else if (lower.contains("dress") || (!lower.contains("armor") && lower.contains(canonical))) {
                    dressedModel = m;
                } else if (!lower.contains("armor") && dressedModel == null) {
                    dressedModel = m;
                } else if (fallbackModel == null) {
                    fallbackModel = m;
                }
            }
            if (dressedModel == null) {
                dressedModel = fallbackModel;
            }

            // Determine animation
            String animation = animEntries.isEmpty() ? null : animEntries.get(0);

            String baseId = (manifestId != null && !manifestId.isEmpty()) ? manifestId : packName;
            String baseDisplayName = (manifestName != null && !manifestName.isEmpty()) ? manifestName : formatDisplayName(packName);

            // Register skin entries for each texture variant found
            if (!textureEntries.isEmpty()) {
                for (String texEntry : textureEntries) {
                    String texBaseName = getFileNameWithoutExt(texEntry);
                    String skinId;
                    String displayName;

                    boolean isMainGirlTex = aliases.contains(texBaseName.toLowerCase()) || texBaseName.equalsIgnoreCase(packName)
                            || (manifestId != null && texBaseName.equalsIgnoreCase(manifestId));
                    if (textureEntries.size() == 1 || isMainGirlTex) {
                        skinId = baseId;
                        displayName = baseDisplayName;
                    } else {
                        skinId = baseId + ":" + texBaseName;
                        displayName = baseDisplayName + " (" + texBaseName + ")";
                    }

                    CustomSkin skin = new CustomSkin(
                            skinId,
                            displayName,
                            canonical,
                            packFile,
                            texEntry,
                            dressedModel,
                            nudeModel,
                            animation
                    );
                    skin.customScenes.addAll(parsedScenes);

                    Map<String, CustomSkin> skinMap = DISCOVERED_SKINS.computeIfAbsent(canonical, k -> new LinkedHashMap<>());
                    skinMap.put(skin.id, skin);
                    if (!skin.id.equalsIgnoreCase(packName)) {
                        skinMap.put(packName, skin);
                    }
                    if (manifestId != null && !skin.id.equalsIgnoreCase(manifestId)) {
                        skinMap.put(manifestId, skin);
                    }
                    LOGGER.info("Registered custom skin pack entry for {}: {} (model={}, anim={}, tex={}, scenes={})",
                            canonical, displayName, dressedModel != null, animation != null, texEntry, skin.customScenes.size());
                }
            } else if (dressedModel != null || nudeModel != null) {
                // Model-only pack (uses default texture)
                String skinId = baseId;
                String displayName = baseDisplayName;
                CustomSkin skin = new CustomSkin(
                        skinId,
                        displayName,
                        canonical,
                        packFile,
                        null,
                        dressedModel,
                        nudeModel,
                        animation
                );
                skin.customScenes.addAll(parsedScenes);

                Map<String, CustomSkin> skinMap = DISCOVERED_SKINS.computeIfAbsent(canonical, k -> new LinkedHashMap<>());
                skinMap.put(skin.id, skin);
                if (!skin.id.equalsIgnoreCase(packName)) {
                    skinMap.put(packName, skin);
                }
                if (manifestId != null && !skin.id.equalsIgnoreCase(manifestId)) {
                    skinMap.put(manifestId, skin);
                }
                LOGGER.info("Registered model-only custom skin for {}: {} (scenes={})", canonical, displayName, skin.customScenes.size());
            }
        }
    }

    private static List<String> parseAnimList(JsonObject obj, String key) {
        List<String> list = new ArrayList<>();
        if (obj.has(key)) {
            JsonElement el = obj.get(key);
            if (el.isJsonArray()) {
                for (JsonElement item : el.getAsJsonArray()) {
                    if (item.isJsonPrimitive()) list.add(item.getAsString());
                }
            } else if (el.isJsonPrimitive()) {
                list.add(el.getAsString());
            }
        }
        return list;
    }

    private static String parseSingleAnim(JsonObject obj, String key) {
        if (obj.has(key)) {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive()) return el.getAsString();
            if (el.isJsonArray() && !el.getAsJsonArray().isEmpty()) {
                return el.getAsJsonArray().get(0).getAsString();
            }
        }
        return null;
    }

    private static com.arn.goodmod.entity.ActionState mapSceneToActionState(String sceneName, List<String> intro, List<String> slow, List<String> fast, String cum) {
        String lower = sceneName.toLowerCase().trim();
        if (lower.contains("blow") || lower.contains("oral") || lower.contains("bj") || lower.contains("suck")) {
            return com.arn.goodmod.entity.ActionState.STARTBLOWJOB;
        }
        if (lower.contains("prone")) {
            return com.arn.goodmod.entity.ActionState.PRONE_DOGGY_INTRO;
        }
        if (lower.contains("doggy")) {
            return com.arn.goodmod.entity.ActionState.STARTDOGGY;
        }
        if (lower.contains("hug")) {
            return com.arn.goodmod.entity.ActionState.HUG_MANG;
        }
        if (lower.contains("cowgirl 2") || lower.contains("cowgirl sitting") || lower.contains("sit cowgirl")) {
            return com.arn.goodmod.entity.ActionState.COWGIRL_SITTING_INTRO;
        }
        if (lower.contains("reverse cowgirl")) {
            return com.arn.goodmod.entity.ActionState.REVERSE_COWGIRL_START;
        }
        if (lower.contains("cowgirl")) {
            return com.arn.goodmod.entity.ActionState.COWGIRLSTART;
        }
        if (lower.contains("missionary")) {
            return com.arn.goodmod.entity.ActionState.MISSIONARY_START;
        }
        if (lower.contains("anal")) {
            return com.arn.goodmod.entity.ActionState.ANAL_START;
        }
        if (lower.contains("boob") || lower.contains("paizuri") || lower.contains("tit")) {
            return com.arn.goodmod.entity.ActionState.PAIZURI_START;
        }
        if (lower.contains("deepthroat") || lower.contains("deep")) {
            return com.arn.goodmod.entity.ActionState.DEEPTHROAT_START;
        }
        if (lower.contains("mating") || lower.contains("press")) {
            return com.arn.goodmod.entity.ActionState.MATING_PRESS_START;
        }
        if (lower.contains("nelson")) {
            return com.arn.goodmod.entity.ActionState.NELSON_INTRO;
        }
        if (lower.contains("breed")) {
            return com.arn.goodmod.entity.ActionState.BREEDING_INTRO_0;
        }
        if (lower.contains("three")) {
            return com.arn.goodmod.entity.ActionState.THREESOME_SLOW;
        }
        if (lower.contains("headpat") || lower.contains("pat")) {
            return com.arn.goodmod.entity.ActionState.HEAD_PAT;
        }
        if (lower.contains("talk") || lower.contains("chat")) {
            return com.arn.goodmod.entity.ActionState.TALK_IDLE;
        }
        if (lower.contains("corrupt")) {
            return com.arn.goodmod.entity.ActionState.CORRUPT_INTRO;
        }
        if (lower.contains("force") || lower.contains("rape")) {
            return com.arn.goodmod.entity.ActionState.RAPE_INTRO;
        }
        if (lower.contains("carry")) {
            return com.arn.goodmod.entity.ActionState.CARRY_INTRO;
        }
        if (lower.contains("touch") || lower.contains("grope")) {
            return com.arn.goodmod.entity.ActionState.TOUCH_BOOBS_INTRO;
        }

        // Inspect animation names
        String combinedAnims = (intro.toString() + " " + slow.toString() + " " + fast.toString() + " " + (cum != null ? cum : "")).toLowerCase();
        if (combinedAnims.contains("blowjob") || combinedAnims.contains("suck")) return com.arn.goodmod.entity.ActionState.STARTBLOWJOB;
        if (combinedAnims.contains("prone")) return com.arn.goodmod.entity.ActionState.PRONE_DOGGY_INTRO;
        if (combinedAnims.contains("doggy")) return com.arn.goodmod.entity.ActionState.STARTDOGGY;
        if (combinedAnims.contains("hug")) return com.arn.goodmod.entity.ActionState.HUG_MANG;
        if (combinedAnims.contains("cowgirl")) return com.arn.goodmod.entity.ActionState.COWGIRLSTART;
        if (combinedAnims.contains("mslow") || combinedAnims.contains("mstart") || combinedAnims.contains("missionary")) return com.arn.goodmod.entity.ActionState.MISSIONARY_START;
        if (combinedAnims.contains("anal")) return com.arn.goodmod.entity.ActionState.ANAL_START;
        if (combinedAnims.contains("paizuri")) return com.arn.goodmod.entity.ActionState.PAIZURI_START;

        return com.arn.goodmod.entity.ActionState.CITIZEN_START;
    }

    private static String getEntryExplicitGirl(String entryPath) {
        String lower = entryPath.toLowerCase();
        for (Map.Entry<String, List<String>> entry : GIRL_ALIASES.entrySet()) {
            String girl = entry.getKey();
            for (String alias : entry.getValue()) {
                if (lower.contains("/" + alias + "/") || lower.contains("/" + alias + ".") || lower.startsWith(alias + "/")) {
                    return girl;
                }
            }
        }
        return null;
    }

    private static String detectGirlFromName(String name) {
        String lower = name.toLowerCase();
        for (Map.Entry<String, List<String>> entry : GIRL_ALIASES.entrySet()) {
            String girl = entry.getKey();
            for (String alias : entry.getValue()) {
                if (lower.contains(alias)) {
                    return girl;
                }
            }
        }
        return null;
    }

    private static Set<String> detectGirlsFromPackContents(File packFile, List<String> entries) {
        Set<String> detected = new HashSet<>();
        // Check .geo.json files for "geometry.<girl>"
        for (String entry : entries) {
            if (entry.toLowerCase().endsWith(".geo.json")) {
                try {
                    String snippet = readEntryHeader(packFile, entry, 4096);
                    String lowerSnippet = snippet.toLowerCase();
                    for (Map.Entry<String, List<String>> ge : GIRL_ALIASES.entrySet()) {
                        for (String alias : ge.getValue()) {
                            if (lowerSnippet.contains("geometry." + alias)) {
                                detected.add(ge.getKey());
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        // Check .animation.json files for "animation.<girl>."
        for (String entry : entries) {
            if (entry.toLowerCase().endsWith(".animation.json")) {
                try {
                    String snippet = readEntryHeader(packFile, entry, 4096);
                    String lowerSnippet = snippet.toLowerCase();
                    for (Map.Entry<String, List<String>> ge : GIRL_ALIASES.entrySet()) {
                        for (String alias : ge.getValue()) {
                            if (lowerSnippet.contains("animation." + alias + ".")) {
                                detected.add(ge.getKey());
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return detected;
    }

    private static String readEntryHeader(File packFile, String entryPath, int maxBytes) throws IOException {
        if (packFile.isDirectory()) {
            File f = new File(packFile, entryPath);
            if (!f.exists()) {
                f = findFileCaseInsensitive(packFile, entryPath);
            }
            if (f != null && f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte[] buf = new byte[maxBytes];
                    int read = fis.read(buf);
                    return new String(buf, 0, Math.max(0, read), StandardCharsets.UTF_8);
                }
            }
        } else if (packFile.isFile() && packFile.getName().toLowerCase().endsWith(".zip")) {
            try (ZipFile zip = new ZipFile(packFile)) {
                String normalized = entryPath.replace('\\', '/');
                ZipEntry ze = zip.getEntry(normalized);
                if (ze == null) {
                    Enumeration<? extends ZipEntry> en = zip.entries();
                    while (en.hasMoreElements()) {
                        ZipEntry next = en.nextElement();
                        if (next.getName().replace('\\', '/').equalsIgnoreCase(normalized)) {
                            ze = next;
                            break;
                        }
                    }
                }
                if (ze != null) {
                    try (InputStream is = zip.getInputStream(ze)) {
                        byte[] buf = new byte[maxBytes];
                        int read = is.read(buf);
                        return new String(buf, 0, Math.max(0, read), StandardCharsets.UTF_8);
                    }
                }
            }
        }
        return "";
    }

    private static boolean isIgnoredTextureName(String name) {
        String lower = name.toLowerCase();
        for (String ignored : IGNORED_TEXTURE_NAMES) {
            if (lower.equals(ignored) || lower.endsWith("_" + ignored) || lower.startsWith(ignored + "_")) {
                return true;
            }
        }
        return false;
    }

    private static String getFileNameWithoutExt(String path) {
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String name = (slash >= 0) ? path.substring(slash + 1) : path;
        int dot = name.lastIndexOf('.');
        return (dot >= 0) ? name.substring(0, dot) : name;
    }

    private static String formatDisplayName(String name) {
        String clean = name.replace('-', ' ').replace('_', ' ').trim();
        return clean.isEmpty() ? name : clean;
    }

    private static List<String> getPackEntries(File packFile) {
        List<String> list = new ArrayList<>();
        if (packFile.isDirectory()) {
            collectDirEntries(packFile, "", list);
        } else if (packFile.isFile() && packFile.getName().toLowerCase().endsWith(".zip")) {
            try (ZipFile zip = new ZipFile(packFile)) {
                Enumeration<? extends ZipEntry> en = zip.entries();
                while (en.hasMoreElements()) {
                    ZipEntry ze = en.nextElement();
                    if (!ze.isDirectory()) {
                        list.add(ze.getName().replace('\\', '/'));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to read zip pack {}: {}", packFile.getName(), e.getMessage());
            }
        }
        return list;
    }

    private static void collectDirEntries(File dir, String prefix, List<String> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            String entryPath = prefix.isEmpty() ? f.getName() : prefix + "/" + f.getName();
            if (f.isDirectory()) {
                collectDirEntries(f, entryPath, result);
            } else if (f.isFile()) {
                result.add(entryPath.replace('\\', '/'));
            }
        }
    }

    public static byte[] readEntryBytes(File packFile, String entryPath) throws IOException {
        if (packFile.isDirectory()) {
            File file = new File(packFile, entryPath);
            if (!file.exists()) {
                // Try case-insensitive search
                file = findFileCaseInsensitive(packFile, entryPath);
            }
            if (file != null && file.exists()) {
                return Files.readAllBytes(file.toPath());
            }
            throw new FileNotFoundException("Entry not found: " + entryPath + " in " + packFile.getAbsolutePath());
        } else {
            try (ZipFile zip = new ZipFile(packFile)) {
                String normalized = entryPath.replace('\\', '/');
                ZipEntry entry = zip.getEntry(normalized);
                if (entry == null) {
                    Enumeration<? extends ZipEntry> en = zip.entries();
                    while (en.hasMoreElements()) {
                        ZipEntry ze = en.nextElement();
                        if (ze.getName().replace('\\', '/').equalsIgnoreCase(normalized)) {
                            entry = ze;
                            break;
                        }
                    }
                }
                if (entry == null) {
                    throw new FileNotFoundException("Entry not found in zip: " + entryPath);
                }
                try (InputStream is = zip.getInputStream(entry)) {
                    return is.readAllBytes();
                }
            }
        }
    }

    public static String readEntryString(File packFile, String entryPath) throws IOException {
        byte[] bytes = readEntryBytes(packFile, entryPath);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static File findFileCaseInsensitive(File parent, String relPath) {
        String[] parts = relPath.replace('\\', '/').split("/");
        File curr = parent;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            File[] children = curr.listFiles();
            if (children == null) return null;
            File matched = null;
            for (File c : children) {
                if (c.getName().equalsIgnoreCase(part)) {
                    matched = c;
                    break;
                }
            }
            if (matched == null) return null;
            curr = matched;
        }
        return curr;
    }
}
