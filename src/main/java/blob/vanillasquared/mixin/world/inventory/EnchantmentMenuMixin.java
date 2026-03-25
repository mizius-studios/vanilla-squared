package blob.vanillasquared.mixin.world.inventory;

import blob.vanillasquared.main.network.payload.EnchantmentBlockCountsPayload;
import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.main.world.inventory.VSQEnchantmentMenuProperties;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingBlockRequirement;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipe;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeInput;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin extends AbstractContainerMenu implements VSQEnchantmentMenuProperties {
    @Unique
    private static final int VSQ$PROPERTY_PLAYER_LEVEL = 0;
    @Unique
    private static final int VSQ$PROPERTY_BLOCK_COUNT = 1;
    @Unique
    private static final int VSQ$PROPERTY_LEVEL_REQUIREMENT = 2;
    @Unique
    private static final int VSQ$PROPERTY_BLOCK_REQUIREMENT = 3;
    @Unique
    private static final int VSQ$INPUT_SLOT = 0;
    @Unique
    private static final int VSQ$MATERIAL_SLOT = 1;
    @Unique
    private static final int VSQ$FIRST_CROSS_SLOT = 2;
    @Unique
    private static final int VSQ$CROSS_SLOT_COUNT = 4;

    @Shadow
    @Final
    @Mutable
    private Container enchantSlots;

    @Unique
    private ContainerLevelAccess vsq$access = ContainerLevelAccess.NULL;

    @Unique
    private ServerPlayer vsq$serverPlayer;

    @Unique
    private int vsq$playerLevel;
    @Unique
    private int vsq$nearbyBlockCount;
    @Unique
    private int vsq$levelRequirement = -1;
    @Unique
    private int vsq$blockRequirement = -1;
    @Unique
    private boolean vsq$hasRequiredXp;
    @Unique
    private boolean vsq$hasRequiredBlocks;
    @Unique
    private List<Component> vsq$detectedBlockTooltipLines = List.of();
    @Unique
    private List<Component> vsq$bookTooltipLines = List.of();

    @Unique
    private Player vsq$player;

    @Unique
    private final ContainerData vsq$properties = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case VSQ$PROPERTY_PLAYER_LEVEL -> {
                    if (EnchantmentMenuMixin.this.vsq$player != null && !EnchantmentMenuMixin.this.vsq$player.level().isClientSide()) {
                        EnchantmentMenuMixin.this.vsq$playerLevel = EnchantmentMenuMixin.this.vsq$player.experienceLevel;
                    }
                    yield EnchantmentMenuMixin.this.vsq$playerLevel;
                }
                case VSQ$PROPERTY_BLOCK_COUNT -> EnchantmentMenuMixin.this.vsq$nearbyBlockCount;
                case VSQ$PROPERTY_LEVEL_REQUIREMENT -> EnchantmentMenuMixin.this.vsq$levelRequirement;
                case VSQ$PROPERTY_BLOCK_REQUIREMENT -> EnchantmentMenuMixin.this.vsq$blockRequirement;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case VSQ$PROPERTY_PLAYER_LEVEL -> EnchantmentMenuMixin.this.vsq$playerLevel = value;
                case VSQ$PROPERTY_BLOCK_COUNT -> EnchantmentMenuMixin.this.vsq$nearbyBlockCount = value;
                case VSQ$PROPERTY_LEVEL_REQUIREMENT -> EnchantmentMenuMixin.this.vsq$levelRequirement = value;
                case VSQ$PROPERTY_BLOCK_REQUIREMENT -> EnchantmentMenuMixin.this.vsq$blockRequirement = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    protected EnchantmentMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;)V", at = @At("TAIL"))
    private void vsq$rebuildSlotLayoutClient(int containerId, Inventory playerInventory, CallbackInfo ci) {
        this.vsq$player = playerInventory.player;
        this.vsq$rebuildSlotLayout(playerInventory);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void vsq$rebuildSlotLayoutServer(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        this.vsq$player = playerInventory.player;
        this.vsq$rebuildSlotLayout(playerInventory);
        this.vsq$access = access;
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            this.vsq$serverPlayer = serverPlayer;
        }
        this.vsq$refresh();
    }

    @Inject(method = "slotsChanged(Lnet/minecraft/world/Container;)V", at = @At("TAIL"))
    private void vsq$refreshNearbyBlockCount(Container container, CallbackInfo ci) {
        this.vsq$refresh();
    }

    @Unique
    private void vsq$refresh() {
        if (this.vsq$serverPlayer == null) {
            return;
        }

        this.vsq$access.execute((Level level, BlockPos tablePos) -> {
            if (level.isClientSide()) {
                return;
            }

            Map<Identifier, Integer> detectedBlocks = this.vsq$collectDetectedBlocks(level, tablePos);
            EnchantingRecipeInput recipeInput = this.vsq$createRecipeInput();
            Optional<RecipeHolder<EnchantingRecipe>> structuralMatch = EnchantingRecipeRegistry.findFirstStructuralMatch(recipeInput, this.vsq$serverPlayer.registryAccess());
            List<EnchantingRecipe.BlockRequirementDisplay> blockDisplay = structuralMatch
                    .map(holder -> holder.value().blockRequirementDisplay(detectedBlocks))
                    .orElse(List.of());
            Component recipeName = structuralMatch.map(holder -> holder.value().name()).orElse(Component.empty());
            Component recipeDescription = structuralMatch.map(holder -> holder.value().description()).orElse(Component.empty());
            this.vsq$levelRequirement = structuralMatch.map(holder -> holder.value().level()).orElse(-1);
            this.vsq$blockRequirement = structuralMatch.map(holder -> holder.value().blocks().stream().mapToInt(EnchantingBlockRequirement::count).sum()).orElse(-1);
            this.vsq$nearbyBlockCount = this.vsq$blockRequirement == -1 ? -1 : blockDisplay.stream().mapToInt(EnchantingRecipe.BlockRequirementDisplay::placedCount).sum();
            this.vsq$sendDetectedBlockCounts(blockDisplay, this.vsq$levelRequirement, this.vsq$blockRequirement, this.vsq$player.experienceLevel, recipeName, recipeDescription);
        });
    }


    @Unique
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

                    Block block = state.getBlock();
                    Identifier key = BuiltInRegistries.BLOCK.getKey(block);
                    if (key == null) {
                        continue;
                    }

                    blockCounts.merge(key, 1, Integer::sum);
                }
            }
        }
        return blockCounts;
    }

    @Unique
    private void vsq$sendDetectedBlockCounts(List<EnchantingRecipe.BlockRequirementDisplay> blockDisplay, int levelRequirement, int blockRequirement, int playerLevel, Component recipeName, Component recipeDescription) {
        if (this.vsq$serverPlayer == null) {
            return;
        }

        List<Identifier> blockIds = new ArrayList<>(blockDisplay.size());
        List<Integer> blockCounts = new ArrayList<>(blockDisplay.size());
        List<Integer> requiredBlockCounts = new ArrayList<>(blockDisplay.size());
        for (EnchantingRecipe.BlockRequirementDisplay entry : blockDisplay) {
            blockIds.add(entry.blockId());
            blockCounts.add(entry.placedCount());
            requiredBlockCounts.add(entry.requiredCount());
        }

        ServerPlayNetworking.send(this.vsq$serverPlayer, new EnchantmentBlockCountsPayload(this.containerId, blockIds, blockCounts, requiredBlockCounts, playerLevel, levelRequirement, blockRequirement, recipeName, recipeDescription));
    }

    @Unique
    private void vsq$rebuildSlotLayout(Inventory playerInventory) {
        AbstractContainerMenuAccessor accessor = (AbstractContainerMenuAccessor) this;

        this.enchantSlots = new SimpleContainer(8) {
            @Override
            public void setChanged() {
                super.setChanged();
                EnchantmentMenuMixin.this.slotsChanged(this);
            }
        };

        this.slots.clear();
        accessor.vsq$getLastSlots().clear();
        accessor.vsq$getRemoteSlots().clear();

        this.addDataSlots(this.vsq$properties);

        this.addSlot(new Slot(this.enchantSlots, VSQ$INPUT_SLOT, 26, 23)); // Input Slot
        this.addSlot(new Slot(this.enchantSlots, VSQ$MATERIAL_SLOT, 80, 36)); // Middle Material Slot
        this.addSlot(new Slot(this.enchantSlots, VSQ$FIRST_CROSS_SLOT, 80, 18)); // Cross Slot Top
        this.addSlot(new Slot(this.enchantSlots, VSQ$FIRST_CROSS_SLOT + 1, 62, 36)); // Cross Slot Left
        this.addSlot(new Slot(this.enchantSlots, VSQ$FIRST_CROSS_SLOT + 2, 98, 36)); // Cross Slot Right
        this.addSlot(new Slot(this.enchantSlots, VSQ$FIRST_CROSS_SLOT + 3, 80, 54)); // Cross Slot Bottom

        this.vsq$addPlayerSlots(playerInventory);
    }

    @Unique
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

    @Override
    public int vsq$getPlayerLevel() {
        return this.vsq$properties.get(VSQ$PROPERTY_PLAYER_LEVEL);
    }

    @Override
    public int vsq$getBlockAmount() {
        return this.vsq$properties.get(VSQ$PROPERTY_BLOCK_COUNT);
    }

    @Override
    public int vsq$getLevelRequirement() {
        return this.vsq$properties.get(VSQ$PROPERTY_LEVEL_REQUIREMENT);
    }

    @Override
    public int vsq$getBlockRequirement() {
        return this.vsq$properties.get(VSQ$PROPERTY_BLOCK_REQUIREMENT);
    }

    @Override
    public boolean vsq$hasRequiredXp() {
        return this.vsq$hasRequiredXp;
    }

    @Override
    public boolean vsq$hasRequiredBlocks() {
        return this.vsq$hasRequiredBlocks;
    }

    @Override
    public List<Component> vsq$getDetectedBlockTooltipLines() {
        return this.vsq$detectedBlockTooltipLines;
    }

    @Override
    public List<Component> vsq$getBookTooltipLines() {
        return this.vsq$bookTooltipLines;
    }

    @Override
    public void vsq$setDetectedBlockCounts(int containerId, List<Identifier> blockIds, List<Integer> blockCounts, List<Integer> requiredBlockCounts, int levelRequirement, int blockRequirement, int playerLevel, Component recipeName, Component recipeDescription) {
        if (this.containerId != containerId) {
            return;
        }

        if (!vsq$hasMatchingBlockCounts(blockIds, blockCounts, requiredBlockCounts)) {
            this.vsq$detectedBlockTooltipLines = List.of();
            return;
        }

        this.vsq$playerLevel = playerLevel;
        this.vsq$levelRequirement = levelRequirement;
        this.vsq$nearbyBlockCount = blockRequirement == -1 ? -1 : blockCounts.stream().mapToInt(Integer::intValue).sum();
        this.vsq$blockRequirement = blockRequirement;
        this.vsq$hasRequiredXp = this.vsq$levelRequirement != -1 && this.vsq$playerLevel >= this.vsq$levelRequirement;
        this.vsq$hasRequiredBlocks = this.vsq$blockRequirement != -1;
        this.vsq$bookTooltipLines = this.vsq$levelRequirement == -1 || this.vsq$blockRequirement == -1
                ? List.of()
                : List.of(recipeName.copy(), recipeDescription.copy());

        this.vsq$detectedBlockTooltipLines = this.vsq$buildDetectedBlockTooltipLines(blockIds, blockCounts, requiredBlockCounts);
        if (this.vsq$blockRequirement == -1) {
            this.vsq$hasRequiredBlocks = false;
            this.vsq$bookTooltipLines = List.of();
            this.vsq$detectedBlockTooltipLines = List.of(Component.translatable("vsq.gui.container.enchantment_table.blocks.tooltip.none"));
        } else if (blockIds.isEmpty()) {
            this.vsq$hasRequiredBlocks = true;
            this.vsq$detectedBlockTooltipLines = List.of(Component.translatable("vsq.gui.container.enchantment_table.blocks.tooltip.none"));
        }
    }

    @Override
    public boolean vsq$tryCraftEnchantingRecipe(ServerPlayer player) {
        if (player.containerMenu != this) {
            return false;
        }

        EnchantingRecipeInput recipeInput = this.vsq$createRecipeInput();
        List<RecipeHolder<EnchantingRecipe>> enchantingRecipes = List.copyOf(EnchantingRecipeRegistry.recipes());
        Map<Identifier, Integer> detectedBlocks = this.vsq$collectDetectedBlocks();
        Optional<RecipeHolder<EnchantingRecipe>> recipeHolder = EnchantingRecipeRegistry.findFirstCraftableMatch(recipeInput, player.experienceLevel, detectedBlocks, player.registryAccess());
        if (recipeHolder.isEmpty()) {
            VanillaSquared.LOGGER.info(
                    "No Enchanting recipe matched. loadedEnchantingRecipes={}, input={}, material={}, cross=[{},{},{},{}]",
                    enchantingRecipes.size(),
                    this.getSlot(VSQ$INPUT_SLOT).getItem(),
                    this.getSlot(VSQ$MATERIAL_SLOT).getItem(),
                    this.getSlot(VSQ$FIRST_CROSS_SLOT).getItem(),
                    this.getSlot(VSQ$FIRST_CROSS_SLOT + 1).getItem(),
                    this.getSlot(VSQ$FIRST_CROSS_SLOT + 2).getItem(),
                    this.getSlot(VSQ$FIRST_CROSS_SLOT + 3).getItem()
            );
            return false;
        }

        RecipeHolder<EnchantingRecipe> holder = recipeHolder.get();
        EnchantingRecipe recipe = holder.value();
        if (!recipe.hasRequiredBlocks(detectedBlocks)) {
            return false;
        }
        Optional<EnchantingRecipe.Match> match = recipe.findMatch(recipeInput);
        if (match.isEmpty()) {
            VanillaSquared.LOGGER.warn("Enchanting recipe {} was selected but no longer matches at craft time", holder.id());
            return false;
        }

        ItemStack result = recipe.assemble(recipeInput, player.registryAccess());
        if (result.isEmpty()) {
            VanillaSquared.LOGGER.warn("Enchanting recipe {} assembled to an empty stack", holder.id());
            return false;
        }

        this.getSlot(VSQ$MATERIAL_SLOT).remove(recipe.material().count());
        for (int ingredientIndex = 0; ingredientIndex < recipe.ingredients().size(); ingredientIndex++) {
            int matchedSlotIndex = match.get().matchedCrossSlots().get(ingredientIndex);
            int containerSlotIndex = matchedSlotIndex + VSQ$FIRST_CROSS_SLOT;
            this.getSlot(containerSlotIndex).remove(recipe.ingredients().get(ingredientIndex).count());
        }
        if (recipe.consumedLevels() > 0) {
            player.giveExperienceLevels(-recipe.consumedLevels());
        }
        this.getSlot(VSQ$INPUT_SLOT).set(result);
        this.slotsChanged(this.enchantSlots);
        this.broadcastChanges();
        this.vsq$refresh();
        VanillaSquared.LOGGER.info("Applied Enchanting recipe {} -> {}", holder.id(), result);
        return true;
    }

    @Unique
    private static boolean vsq$hasMatchingBlockCounts(List<Identifier> blockIds, List<Integer> blockCounts, List<Integer> requiredBlockCounts) {
        return blockIds.size() == blockCounts.size() && blockIds.size() == requiredBlockCounts.size();
    }

    @Unique
    private Map<Identifier, Integer> vsq$collectDetectedBlocks() {
        Map<Identifier, Integer> detectedBlocks = new TreeMap<>(Identifier::compareTo);
        this.vsq$access.execute((level, tablePos) -> detectedBlocks.putAll(this.vsq$collectDetectedBlocks(level, tablePos)));
        return detectedBlocks;
    }

    @Unique
    private List<Component> vsq$buildDetectedBlockTooltipLines(List<Identifier> blockIds, List<Integer> blockCounts, List<Integer> requiredBlockCounts) {
        List<Component> tooltipLines = new ArrayList<>(blockIds.size());
        for (int index = 0; index < blockIds.size(); index++) {
            int countRequired = requiredBlockCounts.get(index);
            int detectedCount = blockCounts.get(index);
            Block block = BuiltInRegistries.BLOCK.getValue(blockIds.get(index));
            if (detectedCount < countRequired) {
                this.vsq$hasRequiredBlocks = false;
            }

            MutableComponent line = Component.translatable(
                    "vsq.gui.container.enchantment_table.blocks.tooltip.entry",
                    detectedCount,
                    countRequired,
                    block.getName()
            );
            tooltipLines.add(line);
        }
        return Collections.unmodifiableList(tooltipLines);
    }

    @Unique
    private EnchantingRecipeInput vsq$createRecipeInput() {
        return new EnchantingRecipeInput(
                this.getSlot(VSQ$INPUT_SLOT).getItem().copy(),
                this.getSlot(VSQ$MATERIAL_SLOT).getItem().copy(),
                this.vsq$getCrossSlotItems()
        );
    }

    @Unique
    private List<ItemStack> vsq$getCrossSlotItems() {
        List<ItemStack> crossSlotItems = new ArrayList<>(VSQ$CROSS_SLOT_COUNT);
        for (int index = 0; index < VSQ$CROSS_SLOT_COUNT; index++) {
            crossSlotItems.add(this.getSlot(VSQ$FIRST_CROSS_SLOT + index).getItem().copy());
        }
        return List.copyOf(crossSlotItems);
    }
}
