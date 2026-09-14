package com.arn.goodmod.init;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoodMod.MODID);

    public static final DeferredItem<AlliesLampItem> ALLIES_LAMP = ITEMS.register("allies_lamp", () -> new AlliesLampItem(new Item.Properties()));
    public static final DeferredItem<DragonStaffItem> DRAGON_STAFF = ITEMS.register("dragon_staff", () -> new DragonStaffItem(new Item.Properties()));
    public static final DeferredItem<GalathCoinItem> GALATH_COIN = ITEMS.register("galath_coin", () -> new GalathCoinItem(new Item.Properties()));
    public static final DeferredItem<KoboldEggItem> KOBOLD_EGG_ITEM = ITEMS.register("kobold_egg_item", () -> new KoboldEggItem(new Item.Properties()));
    public static final DeferredItem<LunaRodItem> LUNA_ROD = ITEMS.register("luna_rod", () -> new LunaRodItem(new Item.Properties()));
    public static final DeferredItem<NpcEditorWandItem> NPC_EDITOR_WAND = ITEMS.register("npc_editor_wand", () -> new NpcEditorWandItem(new Item.Properties()));
    public static final DeferredItem<TribeEggItem> TRIBE_EGG = ITEMS.register("tribe_egg", () -> new TribeEggItem(new Item.Properties()));
    public static final DeferredItem<WinchesterItem> WINCHESTER = ITEMS.register("winchester", () -> new WinchesterItem(new Item.Properties()));

    // Spawn Eggs
    public static final DeferredItem<Item> JENNY_SPAWN_EGG = ITEMS.register("jenny_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.JENNY, 3286592, 12655237, new Item.Properties()));
    public static final DeferredItem<Item> ELLIE_SPAWN_EGG = ITEMS.register("ellie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ELLIE, 1447446, 9961472, new Item.Properties()));
    public static final DeferredItem<Item> SLIME_SPAWN_EGG = ITEMS.register("slime_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SLIME, 13167780, 8244330, new Item.Properties()));
    public static final DeferredItem<Item> BIA_SPAWN_EGG = ITEMS.register("bia_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BIA, 7488816, 7254603, new Item.Properties()));
    public static final DeferredItem<Item> BEE_SPAWN_EGG = ITEMS.register("bee_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BEE, 16701032, 4400155, new Item.Properties()));
    public static final DeferredItem<Item> LUNA_SPAWN_EGG = ITEMS.register("luna_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.LUNA, 7881787, 7940422, new Item.Properties()));
    public static final DeferredItem<Item> ALLIE_SPAWN_EGG = ITEMS.register("allie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ALLIE, 4674237, 16766720, new Item.Properties()));
    public static final DeferredItem<Item> KOBOLD_SPAWN_EGG = ITEMS.register("kobold_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.KOBOLD, 4674237, 11488032, new Item.Properties()));
    public static final DeferredItem<Item> GOBLIN_SPAWN_EGG = ITEMS.register("goblin_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GOBLIN, 39424, 19456, new Item.Properties()));
    public static final DeferredItem<Item> GALATH_SPAWN_EGG = ITEMS.register("galath_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GALATH, 16711680, 16711680, new Item.Properties()));
    public static final DeferredItem<Item> MANGLELIE_SPAWN_EGG = ITEMS.register("manglelie_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MANGLELIE, 16382457, 8485574, new Item.Properties()));
}
