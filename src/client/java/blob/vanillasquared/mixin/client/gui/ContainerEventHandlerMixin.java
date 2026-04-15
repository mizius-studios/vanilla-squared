package blob.vanillasquared.mixin.client.gui;

import blob.vanillasquared.main.gui.enchantment.VSQEnchantmentTooltipState;
import blob.vanillasquared.main.world.item.components.enchantment.VSQEnchantmentComponent;
import blob.vanillasquared.util.api.modules.components.DataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {
    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void vsq$cycleTooltipSelectionWithScroll(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (!VSQEnchantmentTooltipState.isActive() || !vsq$isLeftAltHeld() || verticalAmount == 0.0D) {
            return;
        }

        ItemStack stack = VSQEnchantmentTooltipState.hoveredStack();
        if (stack.isEmpty()) {
            return;
        }

        VSQEnchantmentComponent component = stack.get(DataComponents.VSQ_ENCHANTMENT);
        if (component == null) {
            return;
        }

        cir.setReturnValue(VSQEnchantmentTooltipState.cycle(component, verticalAmount > 0.0D ? -1 : 1));
    }

    private static boolean vsq$isLeftAltHeld() {
        return Minecraft.getInstance().screen != null
                && GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
    }
}
