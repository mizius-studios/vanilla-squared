package blob.vanillasquared.mixin.world.item;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.item.EnchantedBookItem")
public abstract class EnchantedBookEliminatorMixin {

    @Inject(method = "createForEnchantment", at = @At("HEAD"), cancellable = true, remap = true)
    private static void vsq$neutralizeBookCreation(Object instance, CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(ItemStack.EMPTY);
    }
}
