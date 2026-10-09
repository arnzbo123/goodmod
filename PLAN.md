# Implementation Plan: Customizable Personality, Companion Inventory & Armor, and Autonomous AI Mind

## 1. Overview & Architecture
This plan delivers three core interconnected features for `goodmod` on NeoForge 1.21.1:
1. **Customizable AI Personality**: Replace hardcoded personality prompts with an interactive in-game editor tab where players can write and save custom personalities per girl or per entity instance.
2. **Companion Inventory & Armor Support**: Add a complete inventory system for Jenny, Ellie, Bia, Luna, and Slime, featuring:
   - Armor slots (Head, Chest, Legs, Feet) that equip onto the entity and trigger the armored model/texture rendering.
   - Companion storage & tool slots.
   - Dual-column layout integrating inventory on the left and animation/action/companion buttons on the right.
3. **Advanced AI Autonomous Mind**: Enable voice/chat-directed tasks (e.g., "go get me wood", "mine birch wood", "mine iron ore"):
   - Natural language intent parsing and feedback via Groq / AI chat.
   - Tool requirement gating (requires axes for wood, pickaxes for stone, tiered pickaxes for ores).
   - Autonomous AI goal (`GirlHarvestTaskGoal`) to locate nearest blocks, pathfind, equip the tool, mine with animations/particles, collect drops into her inventory, and return to the player.

---

## 2. Component Details

### A. AI Personality Customization System
- **Storage & Persistence**:
  - `BaseGirlEntity`: Add persistent string `customPersonality` synced via EntityData or saved to entity NBT (`saveWithoutId` / `readAdditionalSaveData`).
  - Global default fallback: If entity personality is blank, check `config/goodmod/personalities/<girl>.txt`. If absent, fallback to original default prompt.
- **Dynamic Prompt Construction**:
  - Update `GirlPersonality.buildSystemPrompt(BaseGirlEntity girl, String playerName)`:
    - Injects `girl.getCustomPersonality()` if present.
    - Appends situational context (scene state, current equipped tools/armor, current autonomous task, home status).
- **GUI Integration**:
  - A dedicated "Personality" tab in the girl's main interface.
  - Multi-line edit box allowing text editing, scrolling, "Save", and "Reset to Default" buttons.
  - Sends `UpdateGirlPersonalityPayload` to update entity data and persist to NBT.

### B. Companion Inventory & Armor System
- **Applicable Entities**: Jenny, Ellie, Bia, Luna, Slime (`hasInventory()`).
- **Inventory Container**:
  - 14 slots:
    - 4 Armor Slots: Helmet (slot 0), Chestplate (slot 1), Leggings (slot 2), Boots (slot 3).
    - 1 Mainhand / Tool Slot (slot 4).
    - 1 Offhand Slot (slot 5).
    - 8 General Storage Slots (slots 6–13).
  - Syncs with vanilla `EquipmentSlot` so equipping armor immediately triggers vanilla equipment tracking packets.
- **Menu & Network Architecture**:
  - `ModMenuTypes.GIRL_INVENTORY`: Registered deferred `MenuType<GirlInventoryMenu>`.
  - `GirlInventoryMenu`: Extends `AbstractContainerMenu`:
    - Handles slot placement, shift-click quick-transfer (`quickMoveStack`).
    - Syncs armor slot changes to `girl.setItemSlot(EquipmentSlot, stack)`.
  - Server-side opening on right-click:
    - When player right-clicks Jenny, Ellie, Bia, Luna, or Slime without items: opens `GirlInventoryMenu`.
    - Other girls without inventory retain standard dialogue screen.
- **Armored Rendering**:
  - `GirlGeoRenderer` and `GirlGeoModel` already contain armor bone mappings (`armorHelmet`, `armorChest`, `armorBoobs`, `armorHip`, `armorShoesL`, etc.) and `EquipmentSlot` queries.
  - Integrate texture UV offsets / material tinting for Gold, Chain, Iron, Leather, Diamond materials based on equipped armor items.
  - Slime properly switches to `slime/armored.geo.json` when armored.

### C. Advanced Autonomous AI Mind (Wood & Ore Gathering)
- **Chat Intent Recognition**:
  - In `GirlChatHandler.onServerChat`:
    - Detects commands addressed to the girl:
      - "get wood", "chop birch", "mine stone", "mine iron", "mine diamond", "stop".
    - Evaluates required tool:
      - Wood: checks inventory for `AxeItem` / `#minecraft:axes`.
      - Stone / Ores: checks inventory for `PickaxeItem` / `#minecraft:pickaxes` with appropriate harvest level.
    - If tool missing: Girl responds in-character: "I need an axe in my inventory to chop wood!"
    - If tool present: Girl responds with confirmation, equips tool to main hand, and starts task.
- **AI Task Goal (`GirlHarvestTaskGoal`)**:
  - Extends `Goal`:
    - Searches radius (up to 32 blocks) for matching target blocks (`BlockTags.LOGS`, specific log IDs like `BIRCH_LOG`, `BlockTags.BASE_STONE_OVERWORLD`, ore tags).
    - Uses path navigation to move within reach (2.5 blocks).
    - Swings main hand with animation, spawns mining particles and sounds.
    - Breaks block via `level.destroyBlock(...)`.
    - Collects drops directly into girl's storage slots.
    - If inventory full or quota reached, navigates back to player with notification.
    - Immediately cancellable via "stop" chat command or GUI button.

### D. Unified Screen Layout (`GirlScreen`)
- **Tab 1: Inventory & Companion Actions**:
  - Left panel: Girl 3D preview, 4 Armor slots, Tool slot, Storage slots, Player inventory & hotbar.
  - Right panel: Scrollable/paged Action Buttons (adult animations, scene options), Strip/Dress toggle, Companion actions (Follow, Set Home, Go Home), Current Task status (Idle / Chopping Wood / etc.).
- **Tab 2: AI Personality**:
  - Text area displaying and editing current system prompt.
  - "Save Changes" and "Restore Default" buttons.
  - Character counter & help guidelines.

---

## 3. Files to Create and Modify

| Action | File Path | Purpose |
|---|---|---|
| Create | `src/main/java/com/arn/goodmod/inventory/GirlInventoryMenu.java` | Server/client container menu for girl inventory & armor |
| Create | `src/main/java/com/arn/goodmod/init/ModMenuTypes.java` | NeoForge registry for `MenuType` |
| Create | `src/main/java/com/arn/goodmod/client/gui/GirlScreen.java` | Unified tabbed Screen (Inventory, Animation buttons, Personality editor) |
| Create | `src/main/java/com/arn/goodmod/entity/ai/GirlHarvestTaskGoal.java` | Autonomous AI goal for pathfinding, tool using, and mining |
| Create | `src/main/java/com/arn/goodmod/network/payload/UpdatePersonalityPayload.java` | Packet syncing custom personality from client to server |
| Create | `src/main/java/com/arn/goodmod/network/payload/GirlTaskCommandPayload.java` | Packet for starting/stopping autonomous tasks |
| Modify | `src/main/java/com/arn/goodmod/entity/BaseGirlEntity.java` | Add inventory container, NBT save/load, equipment listeners, task state |
| Modify | `src/main/java/com/arn/goodmod/ai/GirlPersonality.java` | Support dynamic personality prompts with custom override |
| Modify | `src/main/java/com/arn/goodmod/ai/GirlChatHandler.java` | Command intent recognition, tool checking, conversational response |
| Modify | `src/main/java/com/arn/goodmod/client/ClientSetup.java` | Register MenuScreens (`GirlInventoryMenu` -> `GirlScreen`) |
| Modify | `src/main/java/com/arn/goodmod/network/ModPackets.java` | Register new network payloads |
| Modify | `src/main/java/com/arn/goodmod/GoodMod.java` | Register `ModMenuTypes` to mod event bus |

---

## 4. Build & Validation Order
1. Implement `ModMenuTypes` and `GirlInventoryMenu`.
2. Update `BaseGirlEntity` with inventory capability, NBT persistence, and right-click menu opening for Jenny, Ellie, Bia, Luna, Slime.
3. Build `GirlScreen` with inventory slots, action buttons, and personality editor tab.
4. Add network payloads in `ModPackets`.
5. Implement `GirlHarvestTaskGoal` and integrate with `GirlChatHandler` intent handling.
6. Verify compilation via `./gradlew compileJava`.
