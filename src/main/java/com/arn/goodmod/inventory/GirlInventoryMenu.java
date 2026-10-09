package com.arn.goodmod.inventory;

import com.arn.goodmod.entity.BaseGirlEntity;
import com.arn.goodmod.init.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.ProjectileWeaponItem;

public class GirlInventoryMenu extends AbstractContainerMenu {
    public static final int GIRL_ARMOR_SLOTS_START = 0;
    public static final int GIRL_ARMOR_SLOTS_COUNT = 4;
    public static final int GIRL_TOOL_SLOT = 4;
    public static final int GIRL_OFFHAND_SLOT = 5;
    public static final int GIRL_STORAGE_START = 6;
    public static final int GIRL_STORAGE_COUNT = 8;
    public static final int GIRL_TOTAL_SLOTS = 14;

    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS
    };

    private final BaseGirlEntity girl;
    private final Container girlContainer;

    // Client constructor called by IMenuTypeExtension
    public GirlInventoryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getGirlFromBuf(playerInventory, extraData));
    }

    private static BaseGirlEntity getGirlFromBuf(Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        if (extraData != null) {
            int entityId = extraData.readInt();
            Entity entity = playerInventory.player.level().getEntity(entityId);
            if (entity instanceof BaseGirlEntity baseGirl) {
                return baseGirl;
            }
        }
        return null;
    }

    // Server constructor
    public GirlInventoryMenu(int containerId, Inventory playerInventory, BaseGirlEntity girl) {
        super(ModMenuTypes.GIRL_INVENTORY.get(), containerId);
        this.girl = girl;
        this.girlContainer = (girl != null) ? girl.getGirlInventory() : new net.minecraft.world.SimpleContainer(GIRL_TOTAL_SLOTS);

        // 1. Girl Armor Slots (0: Head, 1: Chest, 2: Legs, 3: Feet)
        int armorX = 8;
        int armorStartY = 18;
        for (int i = 0; i < 4; i++) {
            final EquipmentSlot slotType = ARMOR_SLOTS[i];
            final ResourceLocation emptyTex = ARMOR_SLOT_TEXTURES[i];
            this.addSlot(new Slot(this.girlContainer, i, armorX, armorStartY + (i * 18)) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (stack.getItem() instanceof ArmorItem armorItem) {
                        return armorItem.getEquipmentSlot() == slotType;
                    }
                    if (stack.getItem() instanceof Equipable equipable) {
                        return equipable.getEquipmentSlot() == slotType;
                    }
                    return false;
                }

                @Override
                public com.mojang.datafixers.util.Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return com.mojang.datafixers.util.Pair.of(InventoryMenu.BLOCK_ATLAS, emptyTex);
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    if (girl != null && !girl.level().isClientSide) {
                        girl.setItemSlot(slotType, getItem());
                    }
                }
            });
        }

        // 2. Girl Mainhand/Tool Slot (Slot 4) and Offhand Slot (Slot 5)
        int toolX = 80;
        int toolY = 18;
        this.addSlot(new Slot(this.girlContainer, GIRL_TOOL_SLOT, toolX, toolY) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (girl != null && !girl.level().isClientSide) {
                    girl.setItemSlot(EquipmentSlot.MAINHAND, getItem());
                }
            }
        });

        this.addSlot(new Slot(this.girlContainer, GIRL_OFFHAND_SLOT, toolX + 18, toolY) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (girl != null && !girl.level().isClientSide) {
                    girl.setItemSlot(EquipmentSlot.OFFHAND, getItem());
                }
            }

            @Override
            public com.mojang.datafixers.util.Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return com.mojang.datafixers.util.Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        // 3. Girl Storage Slots (Slots 6..13: 2 rows of 4 slots)
        int storageX = 80;
        int storageStartY = 38;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                int slotIndex = GIRL_STORAGE_START + (row * 4) + col;
                this.addSlot(new Slot(this.girlContainer, slotIndex, storageX + (col * 18), storageStartY + (row * 18)));
            }
        }

        // 4. Player Main Inventory (3 rows of 9 slots)
        int playerInvX = 8;
        int playerInvY = 100;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + (row * 9) + 9, playerInvX + (col * 18), playerInvY + (row * 18)));
            }
        }

        // 5. Player Hotbar (9 slots)
        int hotbarY = 158;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, playerInvX + (col * 18), hotbarY));
        }
    }

    public BaseGirlEntity getGirl() {
        return this.girl;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.girl == null || !this.girl.isAlive()) {
            return false;
        }
        return this.girl.distanceTo(player) <= 8.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            // Clicked in Girl's slots (0..13) -> Transfer to Player Inventory (14..49)
            if (index < GIRL_TOTAL_SLOTS) {
                if (!this.moveItemStackTo(stackInSlot, GIRL_TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Clicked in Player's slots (14..49) -> Transfer to Girl's slots
            else {
                boolean placed = false;

                // A. Check Armor
                if (stackInSlot.getItem() instanceof ArmorItem armorItem) {
                    EquipmentSlot targetSlot = armorItem.getEquipmentSlot();
                    for (int i = 0; i < 4; i++) {
                        if (ARMOR_SLOTS[i] == targetSlot) {
                            if (this.moveItemStackTo(stackInSlot, i, i + 1, false)) {
                                placed = true;
                                break;
                            }
                        }
                    }
                }

                // B. Check Tool / Weapon
                if (!placed && (stackInSlot.getItem() instanceof TieredItem || stackInSlot.getItem() instanceof ProjectileWeaponItem)) {
                    if (this.moveItemStackTo(stackInSlot, GIRL_TOOL_SLOT, GIRL_TOOL_SLOT + 1, false)) {
                        placed = true;
                    }
                }

                // C. Otherwise put into Girl Storage (6..13)
                if (!placed) {
                    if (!this.moveItemStackTo(stackInSlot, GIRL_STORAGE_START, GIRL_TOTAL_SLOTS, false)) {
                        // D. Move between player main inventory and hotbar
                        if (index < GIRL_TOTAL_SLOTS + 27) {
                            if (!this.moveItemStackTo(stackInSlot, GIRL_TOTAL_SLOTS + 27, this.slots.size(), false)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            if (!this.moveItemStackTo(stackInSlot, GIRL_TOTAL_SLOTS, GIRL_TOTAL_SLOTS + 27, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return itemstack;
    }
}
