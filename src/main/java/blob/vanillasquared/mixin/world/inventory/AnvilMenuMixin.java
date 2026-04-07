package blob.vanillasquared.mixin.world.inventory;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends AbstractContainerMenu {

    protected AnvilMenuMixin(net.minecraft.world.inventory.MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void vsq$stopEnchantedBookCrafting(CallbackInfo ci) {
        if (this.slots.size() < 3) return;

        Slot secondaryInput = (Slot) this.slots.get(1);
        Slot resultSlot = (Slot) this.slots.get(2);

        if (secondaryInput.getItem().is(Items.ENCHANTED_BOOK)) {
            resultSlot.set(ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
