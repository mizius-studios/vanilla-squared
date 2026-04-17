package blob.vanillasquared.mixin.world.loot;

import blob.vanillasquared.main.world.loot.RandomizeEnchantmentSlotsFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(LootPool.class)
public abstract class LootPoolMixin {

    @ModifyVariable(method = "addRandomItems", at = @At("HEAD"), argsOnly = true)
    private Consumer<ItemStack> vsq$filterBooks(Consumer<ItemStack> original, Consumer<ItemStack> result, LootContext context) {
        return stack -> {
            if (!stack.is(Items.ENCHANTED_BOOK)) {
                original.accept(RandomizeEnchantmentSlotsFunction.DEFAULT_LOOT_RANDOMIZATION.apply(stack, context));
            }
        };
    }
}
