package com.arn.goodmod.init;

import com.arn.goodmod.GoodMod;
import com.arn.goodmod.inventory.GirlInventoryMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, GoodMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GirlInventoryMenu>> GIRL_INVENTORY =
            MENUS.register("girl_inventory", () -> IMenuTypeExtension.create(GirlInventoryMenu::new));
}
