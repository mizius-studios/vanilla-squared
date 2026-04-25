package blob.vanillasquared.mixin.world.loot;

import blob.vanillasquared.main.world.loot.RandomizeEnchantmentSlotsFunction;
import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(LootPool.class)
public abstract class LootPoolMixin {

    @ModifyVariable(method = "addRandomItems", at = @At("HEAD"), argsOnly = true, name = "result")
    // Mixin requires the captured variable followed by the original method arguments for this target signature.
    private Consumer<ItemStack> vsq$sanitizeLootEnchantments(Consumer<ItemStack> original, Consumer<ItemStack> originalArgument, LootContext context) {
        return stack -> {
            if (stack.is(Items.ENCHANTED_BOOK)) {
                Identifier tagId = vsq$resolveLootTag(context);
                ItemStack recipeStack = EnchantingRecipeTags.createRandomStack(tagId, context.getRandom());
                if (!recipeStack.isEmpty()) {
                    original.accept(recipeStack);
                }
                return;
            }
            stack.remove(DataComponents.ENCHANTMENTS);
            stack.remove(DataComponents.STORED_ENCHANTMENTS);
            if (!stack.has(blob.vanillasquared.util.api.modules.components.DataComponents.VSQ_ENCHANTMENT)) {
                stack = RandomizeEnchantmentSlotsFunction.DEFAULT_LOOT_RANDOMIZATION.apply(stack, context);
            }
            original.accept(stack);
        };
    }

    @Unique
    private static Identifier vsq$resolveLootTag(LootContext context) {
        return vsq$findCurrentLootTableId(context)
                .map(EnchantingRecipeTags::lootTagForTable)
                .orElse(EnchantingRecipeTags.DEFAULT_LOOT_TAG);
    }

    @Unique
    private static Optional<Identifier> vsq$findCurrentLootTableId(LootContext context) {
        LootTable activeTable = ((LootContextAccessor) context).vsq$lootTableStack().peekLast();
        if (activeTable == null || !(context.getResolver() instanceof net.minecraft.core.HolderLookup.Provider provider)) {
            return Optional.empty();
        }

        LootTable targetTable = activeTable;
        return provider.lookupOrThrow(net.minecraft.core.registries.Registries.LOOT_TABLE)
                .listElements()
                .filter(holder -> holder.value() == targetTable)
                .map(holder -> holder.key().identifier())
                .findFirst();
    }
}
