package blob.vanillasquared.main.world.inventory;

import blob.vanillasquared.main.network.payload.EnchantingRecipeBookSyncPayload;
import blob.vanillasquared.main.network.payload.EnchantingRecipeStatePayload;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingBlockRequirement;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipe;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeInput;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class VSQEnchantmentMenu extends RecipeBookMenu implements VSQEnchantmentMenuProperties {
    public static final int INPUT_SLOT = 0;
    public static final int MATERIAL_SLOT = 1;
    public static final int FIRST_CROSS_SLOT = 2;
    public static final int CROSS_SLOT_COUNT = 4;
    public static final int TABLE_SLOT_COUNT = 6;
    private static final int PLAYER_INV_START = TABLE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int PROPERTY_PLAYER_LEVEL = 0;
    private static final int PROPERTY_BLOCK_COUNT = 1;
    private static final int PROPERTY_LEVEL_REQUIREMENT = 2;
    private static final int PROPERTY_BLOCK_REQUIREMENT = 3;
    private static final Identifier ENCHANTABLE_TAG_ID = Identifier.fromNamespaceAndPath("vsq", "enchanting/enchantable");
    private static final Identifier MATERIAL_TAG_ID = Identifier.fromNamespaceAndPath("vsq", "enchanting/material");
    private static final TagKey<Item> ENCHANTABLE_TAG = TagKey.create(net.minecraft.core.registries.Registries.ITEM, ENCHANTABLE_TAG_ID);
    private static final TagKey<Item> MATERIAL_TAG = TagKey.create(net.minecraft.core.registries.Registries.ITEM, MATERIAL_TAG_ID);

    private final Container enchantSlots = new SimpleContainer(TABLE_SLOT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            VSQEnchantmentMenu.this.slotsChanged(this);
        }
    };
    private final ContainerLevelAccess access;
    private final Player player;
    private final ContainerData properties = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case PROPERTY_PLAYER_LEVEL -> VSQEnchantmentMenu.this.playerLevel;
                case PROPERTY_BLOCK_COUNT -> VSQEnchantmentMenu.this.nearbyBlockCount;
                case PROPERTY_LEVEL_REQUIREMENT -> VSQEnchantmentMenu.this.levelRequirement;
                case PROPERTY_BLOCK_REQUIREMENT -> VSQEnchantmentMenu.this.blockRequirement;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case PROPERTY_PLAYER_LEVEL -> VSQEnchantmentMenu.this.playerLevel = value;
                case PROPERTY_BLOCK_COUNT -> VSQEnchantmentMenu.this.nearbyBlockCount = value;
                case PROPERTY_LEVEL_REQUIREMENT -> VSQEnchantmentMenu.this.levelRequirement = value;
                case PROPERTY_BLOCK_REQUIREMENT -> VSQEnchantmentMenu.this.blockRequirement = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    private int playerLevel = -1;
    private int nearbyBlockCount = -1;
    private int levelRequirement = -1;
    private int blockRequirement = -1;
    private boolean hasRequiredXp;
    private boolean hasRequiredBlocks;
    private List<Component> detectedBlockTooltipLines = List.of();
    private List<Component> bookTooltipLines = List.of();
    private int selectedDisplayId = -1;
    private final Map<Integer, RecipeHolder<EnchantingRecipe>> displayRecipes = new LinkedHashMap<>();

    public VSQEnchantmentMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, BlockPos.ZERO);
    }

    public VSQEnchantmentMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(VSQMenuTypes.ENCHANTING, containerId);
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos);
        this.player = playerInventory.player;
        this.addDataSlots(this.properties);
        this.vsq$rebuildRecipeBookIndex();
        this.addSlot(new Slot(this.enchantSlots, INPUT_SLOT, 26, 23));
        this.addSlot(new Slot(this.enchantSlots, MATERIAL_SLOT, 80, 36));
        this.addSlot(new Slot(this.enchantSlots, FIRST_CROSS_SLOT, 80, 18));
        this.addSlot(new Slot(this.enchantSlots, FIRST_CROSS_SLOT + 1, 62, 36));
        this.addSlot(new Slot(this.enchantSlots, FIRST_CROSS_SLOT + 2, 98, 36));
        this.addSlot(new Slot(this.enchantSlots, FIRST_CROSS_SLOT + 3, 80, 54));
        this.vsq$addPlayerSlots(playerInventory);
        if (blockPos == BlockPos.ZERO) {
            this.selectedDisplayId = -1;
        }
        if (this.player instanceof ServerPlayer serverPlayer) {
            this.vsq$sendRecipeBookSync(serverPlayer, true);
            this.vsq$refresh(serverPlayer);
        }
    }

    @Override
    public PostPlaceAction handlePlacement(boolean placeAll, boolean creative, RecipeHolder<?> recipe, net.minecraft.server.level.ServerLevel level, Inventory inventory) {
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return PostPlaceAction.NOTHING;
        }
        if (!(recipe.value() instanceof EnchantingRecipe enchantingRecipe)) {
            return PostPlaceAction.NOTHING;
        }
        Optional<Integer> displayId = this.displayRecipes.entrySet().stream()
                .filter(entry -> entry.getValue().id().equals(recipe.id()))
                .map(Map.Entry::getKey)
                .findFirst();
        if (displayId.isEmpty()) {
            return PostPlaceAction.NOTHING;
        }

        this.vsq$setSelectedDisplayId(displayId.get());
        return this.vsq$handleRecipeBookPlacement(serverPlayer, displayId.get()) ? PostPlaceAction.NOTHING : PostPlaceAction.PLACE_GHOST_RECIPE;
    }

    @Override
    public void fillCraftSlotsStackedContents(net.minecraft.world.entity.player.StackedItemContents stackedItemContents) {
        for (int slotIndex = 0; slotIndex < this.enchantSlots.getContainerSize(); slotIndex++) {
            stackedItemContents.accountStack(this.enchantSlots.getItem(slotIndex));
        }
        Inventory inventory = this.player.getInventory();
        for (int slotIndex = 0; slotIndex < inventory.getContainerSize(); slotIndex++) {
            stackedItemContents.accountStack(inventory.getItem(slotIndex));
        }
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (this.player instanceof ServerPlayer serverPlayer) {
            this.vsq$refresh(serverPlayer);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.enchantSlots);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < TABLE_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ENCHANTABLE_TAG)) {
            if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(MATERIAL_TAG)) {
            if (!this.moveItemStackTo(stack, MATERIAL_SLOT, MATERIAL_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, FIRST_CROSS_SLOT, FIRST_CROSS_SLOT + CROSS_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    @Override
    public int vsq$getPlayerLevel() {
        return this.playerLevel;
    }

    @Override
    public int vsq$getBlockAmount() {
        return this.nearbyBlockCount;
    }

    @Override
    public int vsq$getLevelRequirement() {
        return this.levelRequirement;
    }

    @Override
    public int vsq$getBlockRequirement() {
        return this.blockRequirement;
    }

    @Override
    public boolean vsq$hasRequiredXp() {
        return this.hasRequiredXp;
    }

    @Override
    public boolean vsq$hasRequiredBlocks() {
        return this.hasRequiredBlocks;
    }

    @Override
    public List<Component> vsq$getDetectedBlockTooltipLines() {
        return this.detectedBlockTooltipLines;
    }

    @Override
    public List<Component> vsq$getBookTooltipLines() {
        return this.bookTooltipLines;
    }

    @Override
    public void vsq$applyRecipeState(int containerId, List<Identifier> blockIds, List<Integer> counts, List<Integer> requiredBlockCounts, int levelRequirement, int blockRequirement, int playerLevel, Component recipeName, Component recipeDescription) {
        if (this.containerId != containerId) {
            return;
        }

        this.playerLevel = playerLevel;
        this.levelRequirement = levelRequirement;
        this.nearbyBlockCount = blockRequirement == -1 ? -1 : counts.stream().mapToInt(Integer::intValue).sum();
        this.blockRequirement = blockRequirement;
        this.hasRequiredXp = this.levelRequirement != -1 && this.playerLevel >= this.levelRequirement;
        this.hasRequiredBlocks = this.blockRequirement != -1;
        this.bookTooltipLines = this.levelRequirement == -1 || this.blockRequirement == -1 ? List.of() : List.of(recipeName.copy(), recipeDescription.copy());
        this.detectedBlockTooltipLines = this.vsq$buildDetectedBlockTooltipLines(blockIds, counts, requiredBlockCounts);

        if (this.blockRequirement == -1) {
            this.hasRequiredBlocks = false;
            this.bookTooltipLines = List.of();
            this.detectedBlockTooltipLines = List.of(Component.translatable("vsq.gui.container.enchantment_table.blocks.tooltip.none"));
        } else if (blockIds.isEmpty()) {
            this.hasRequiredBlocks = true;
            this.detectedBlockTooltipLines = List.of(Component.translatable("vsq.gui.container.enchantment_table.blocks.tooltip.none"));
        }
    }

    @Override
    public boolean vsq$tryCraftEnchantingRecipe(ServerPlayer player) {
        if (player.containerMenu != this) {
            return false;
        }

        EnchantingRecipeInput input = this.vsq$createRecipeInput();
        Map<Identifier, Integer> detectedBlocks = this.vsq$collectDetectedBlocks();
        Optional<RecipeHolder<EnchantingRecipe>> recipeHolder = this.vsq$getCraftingRecipe(input, detectedBlocks, player.registryAccess(), player.experienceLevel);
        if (recipeHolder.isEmpty()) {
            return false;
        }

        EnchantingRecipe recipe = recipeHolder.get().value();
        Optional<EnchantingRecipe.Match> match = recipe.findMatch(input);
        if (match.isEmpty()) {
            return false;
        }

        ItemStack result = recipe.assemble(input, player.registryAccess());
        if (result.isEmpty()) {
            return false;
        }

        this.getSlot(MATERIAL_SLOT).remove(recipe.material().count());
        for (int ingredientIndex = 0; ingredientIndex < recipe.ingredients().size(); ingredientIndex++) {
            int matchedSlotIndex = match.get().matchedCrossSlots().get(ingredientIndex);
            this.getSlot(FIRST_CROSS_SLOT + matchedSlotIndex).remove(recipe.ingredients().get(ingredientIndex).count());
        }
        if (recipe.consumedLevels() > 0) {
            player.giveExperienceLevels(-recipe.consumedLevels());
        }
        this.getSlot(INPUT_SLOT).set(result);
        this.broadcastChanges();
        this.vsq$refresh(player);
        return true;
    }

    public void vsq$setSelectedDisplayId(int selectedDisplayId) {
        this.selectedDisplayId = selectedDisplayId;
        if (this.player instanceof ServerPlayer serverPlayer) {
            this.vsq$refresh(serverPlayer);
        }
    }

    public boolean vsq$handleRecipeBookPlacement(ServerPlayer player, int displayId) {
        RecipeHolder<EnchantingRecipe> recipeHolder = this.displayRecipes.get(displayId);
        if (recipeHolder == null) {
            return false;
        }

        EnchantingRecipe recipe = recipeHolder.value();
        if (!this.vsq$returnInputsToInventory()) {
            return false;
        }
        if (!this.vsq$fillRecipeSlot(INPUT_SLOT, recipe.input(), false)) {
            this.vsq$returnInputsToInventory();
            return false;
        }
        if (!this.vsq$fillRecipeSlot(MATERIAL_SLOT, recipe.material(), false)) {
            this.vsq$returnInputsToInventory();
            return false;
        }
        for (int index = 0; index < recipe.ingredients().size(); index++) {
            if (!this.vsq$fillRecipeSlot(FIRST_CROSS_SLOT + index, recipe.ingredients().get(index), false)) {
                this.vsq$returnInputsToInventory();
                return false;
            }
        }

        this.broadcastChanges();
        this.vsq$refresh(player);
        return true;
    }

    private boolean vsq$returnInputsToInventory() {
        for (int slotIndex = 0; slotIndex < TABLE_SLOT_COUNT; slotIndex++) {
            ItemStack stack = this.enchantSlots.getItem(slotIndex);
            if (stack.isEmpty()) {
                continue;
            }
            this.player.getInventory().placeItemBackInInventory(stack.copy(), false);
            this.enchantSlots.setItem(slotIndex, ItemStack.EMPTY);
        }
        return true;
    }

    private boolean vsq$fillRecipeSlot(int slotIndex, blob.vanillasquared.main.world.recipe.enchanting.EnchantingIngredient ingredient, boolean fromContainerOnly) {
        ItemStack extracted = this.vsq$extractMatchingItems(ingredient);
        if (extracted.isEmpty()) {
            return false;
        }
        this.enchantSlots.setItem(slotIndex, extracted);
        return true;
    }

    private ItemStack vsq$extractMatchingItems(blob.vanillasquared.main.world.recipe.enchanting.EnchantingIngredient ingredient) {
        Inventory inventory = this.player.getInventory();
        ItemStack extracted = ItemStack.EMPTY;
        int remaining = ingredient.count();
        for (int slotIndex = 0; slotIndex < inventory.getContainerSize(); slotIndex++) {
            ItemStack stack = inventory.getItem(slotIndex);
            if (!ingredient.matchesIgnoringCount(stack)) {
                continue;
            }

            int taken = Math.min(remaining, stack.getCount());
            if (taken <= 0) {
                continue;
            }

            ItemStack split = stack.split(taken);
            if (extracted.isEmpty()) {
                extracted = split;
            } else {
                extracted.grow(split.getCount());
            }
            remaining -= taken;
            if (remaining == 0) {
                extracted.setCount(ingredient.count());
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    private void vsq$refresh(ServerPlayer player) {
        this.playerLevel = player.experienceLevel;
        Map<Identifier, Integer> detectedBlocks = this.vsq$collectDetectedBlocks();
        EnchantingRecipeInput input = this.vsq$createRecipeInput();
        Optional<RecipeHolder<EnchantingRecipe>> recipeHolder = this.vsq$getPreviewRecipe(input, player.registryAccess());
        if (recipeHolder.isEmpty()) {
            this.levelRequirement = -1;
            this.blockRequirement = -1;
            this.nearbyBlockCount = -1;
            this.vsq$sendDetectedBlockCounts(List.of(), -1, -1, player.experienceLevel, Component.empty(), Component.empty(), player);
            return;
        }

        EnchantingRecipe recipe = recipeHolder.get().value();
        List<EnchantingRecipe.BlockRequirementDisplay> blockDisplay = recipe.blockRequirementDisplay(detectedBlocks);
        this.levelRequirement = recipe.level();
        this.blockRequirement = recipe.blocks().stream().mapToInt(EnchantingBlockRequirement::count).sum();
        this.nearbyBlockCount = this.blockRequirement == -1 ? -1 : blockDisplay.stream().mapToInt(EnchantingRecipe.BlockRequirementDisplay::placedCount).sum();
        this.vsq$sendDetectedBlockCounts(blockDisplay, this.levelRequirement, this.blockRequirement, player.experienceLevel, recipe.name(), recipe.description(), player);
    }

    private Optional<RecipeHolder<EnchantingRecipe>> vsq$getDisplayedRecipe(EnchantingRecipeInput input, Map<Identifier, Integer> detectedBlocks, net.minecraft.core.HolderLookup.Provider registries) {
        return this.vsq$getPreviewRecipe(input, registries);
    }

    private Optional<RecipeHolder<EnchantingRecipe>> vsq$getPreviewRecipe(EnchantingRecipeInput input, net.minecraft.core.HolderLookup.Provider registries) {
        if (this.selectedDisplayId != -1) {
            RecipeHolder<EnchantingRecipe> selected = this.displayRecipes.get(this.selectedDisplayId);
            if (selected != null && this.vsq$canSatisfyRecipe(selected.value())) {
                return Optional.of(selected);
            }
            this.selectedDisplayId = -1;
        }
        return EnchantingRecipeRegistry.findFirstStructuralMatch(input, registries);
    }

    private Optional<RecipeHolder<EnchantingRecipe>> vsq$getCraftingRecipe(EnchantingRecipeInput input, Map<Identifier, Integer> detectedBlocks, net.minecraft.core.HolderLookup.Provider registries, int playerLevel) {
        if (this.selectedDisplayId != -1) {
            RecipeHolder<EnchantingRecipe> selected = this.displayRecipes.get(this.selectedDisplayId);
            if (selected != null
                    && selected.value().findMatch(input).isPresent()
                    && selected.value().isCompatibleInput(input, registries)
                    && selected.value().wouldModifyInput(input, registries)
                    && selected.value().canPlayerCraft(playerLevel)
                    && selected.value().hasRequiredBlocks(detectedBlocks)) {
                return Optional.of(selected);
            }
            return Optional.empty();
        }

        return EnchantingRecipeRegistry.findFirstCraftableMatch(input, playerLevel, detectedBlocks, registries);
    }

    private boolean vsq$canSatisfyRecipe(EnchantingRecipe recipe) {
        List<ItemStack> available = new ArrayList<>(this.player.getInventory().getContainerSize() + TABLE_SLOT_COUNT);
        for (int slotIndex = 0; slotIndex < this.player.getInventory().getContainerSize(); slotIndex++) {
            ItemStack stack = this.player.getInventory().getItem(slotIndex);
            if (!stack.isEmpty()) {
                available.add(stack.copy());
            }
        }
        for (int slotIndex = 0; slotIndex < TABLE_SLOT_COUNT; slotIndex++) {
            ItemStack stack = this.enchantSlots.getItem(slotIndex);
            if (!stack.isEmpty()) {
                available.add(stack.copy());
            }
        }

        if (!this.vsq$consumeAvailable(available, recipe.input())) {
            return false;
        }
        if (!this.vsq$consumeAvailable(available, recipe.material())) {
            return false;
        }
        for (blob.vanillasquared.main.world.recipe.enchanting.EnchantingIngredient ingredient : recipe.ingredients()) {
            if (!this.vsq$consumeAvailable(available, ingredient)) {
                return false;
            }
        }
        return true;
    }

    private boolean vsq$consumeAvailable(List<ItemStack> available, blob.vanillasquared.main.world.recipe.enchanting.EnchantingIngredient ingredient) {
        int remaining = ingredient.count();
        for (ItemStack stack : available) {
            if (stack.isEmpty() || !ingredient.matchesIgnoringCount(stack)) {
                continue;
            }

            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (remaining == 0) {
                return true;
            }
        }
        return false;
    }

    private Map<Identifier, Integer> vsq$collectDetectedBlocks() {
        Map<Identifier, Integer> detectedBlocks = new TreeMap<>(Identifier::compareTo);
        this.access.execute((level, tablePos) -> detectedBlocks.putAll(this.vsq$collectDetectedBlocks(level, tablePos)));
        return detectedBlocks;
    }

    private Map<Identifier, Integer> vsq$collectDetectedBlocks(Level level, BlockPos tablePos) {
        Map<Identifier, Integer> blockCounts = new TreeMap<>(Identifier::compareTo);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    BlockPos pos = tablePos.offset(dx, dy, dz);
                    if (pos.equals(tablePos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    Identifier key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    if (key != null) {
                        blockCounts.merge(key, 1, Integer::sum);
                    }
                }
            }
        }
        return blockCounts;
    }

    private List<Component> vsq$buildDetectedBlockTooltipLines(List<Identifier> blockIds, List<Integer> blockCounts, List<Integer> requiredBlockCounts) {
        List<Component> tooltipLines = new ArrayList<>(blockIds.size());
        this.hasRequiredBlocks = this.blockRequirement != -1;
        for (int index = 0; index < blockIds.size(); index++) {
            int requiredCount = requiredBlockCounts.get(index);
            int detectedCount = blockCounts.get(index);
            Block block = BuiltInRegistries.BLOCK.getValue(blockIds.get(index));
            if (detectedCount < requiredCount) {
                this.hasRequiredBlocks = false;
            }
            MutableComponent line = Component.translatable(
                    "vsq.gui.container.enchantment_table.blocks.tooltip.entry",
                    detectedCount,
                    requiredCount,
                    block.getName()
            );
            tooltipLines.add(line);
        }
        return Collections.unmodifiableList(tooltipLines);
    }

    private void vsq$sendDetectedBlockCounts(List<EnchantingRecipe.BlockRequirementDisplay> blockDisplay, int levelRequirement, int blockRequirement, int playerLevel, Component recipeName, Component recipeDescription, ServerPlayer player) {
        List<Identifier> blockIds = new ArrayList<>(blockDisplay.size());
        List<Integer> blockCounts = new ArrayList<>(blockDisplay.size());
        List<Integer> requiredBlockCounts = new ArrayList<>(blockDisplay.size());
        for (EnchantingRecipe.BlockRequirementDisplay entry : blockDisplay) {
            blockIds.add(entry.blockId());
            blockCounts.add(entry.placedCount());
            requiredBlockCounts.add(entry.requiredCount());
        }
        ServerPlayNetworking.send(player, new EnchantingRecipeStatePayload(this.containerId, blockIds, blockCounts, requiredBlockCounts, playerLevel, levelRequirement, blockRequirement, recipeName, recipeDescription));
    }

    private EnchantingRecipeInput vsq$createRecipeInput() {
        return new EnchantingRecipeInput(
                this.enchantSlots.getItem(INPUT_SLOT).copy(),
                this.enchantSlots.getItem(MATERIAL_SLOT).copy(),
                this.vsq$getCrossSlotItems()
        );
    }

    private List<ItemStack> vsq$getCrossSlotItems() {
        List<ItemStack> items = new ArrayList<>(CROSS_SLOT_COUNT);
        for (int index = 0; index < CROSS_SLOT_COUNT; index++) {
            items.add(this.enchantSlots.getItem(FIRST_CROSS_SLOT + index).copy());
        }
        return List.copyOf(items);
    }

    private void vsq$addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
        }
    }

    private void vsq$rebuildRecipeBookIndex() {
        this.displayRecipes.clear();
        int displayId = 0;
        for (RecipeHolder<EnchantingRecipe> holder : EnchantingRecipeRegistry.recipes()) {
            this.displayRecipes.put(displayId++, holder);
        }
    }

    private void vsq$sendRecipeBookSync(ServerPlayer player, boolean replace) {
        this.vsq$rebuildRecipeBookIndex();
        ServerPlayNetworking.send(player, EnchantingRecipeBookSyncPayload.create(this.containerId, replace, this.displayRecipes.values(), player.registryAccess()));
    }

    public List<Slot> vsq$getEnchantingSlots() {
        return this.slots.subList(0, TABLE_SLOT_COUNT);
    }

    public Map<Integer, RecipeHolder<EnchantingRecipe>> vsq$getDisplayRecipes() {
        return Map.copyOf(this.displayRecipes);
    }
}
