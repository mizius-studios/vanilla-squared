package blob.vanillasquared.mixin.client.gui;

import blob.vanillasquared.main.gui.enchantment.VSQEnchantmentTooltipState;
import blob.vanillasquared.main.world.item.components.enchantment.VSQEnchantmentComponent;
import blob.vanillasquared.util.api.modules.components.DataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void vsq$cycleTooltipSelectionWithKeys(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!VSQEnchantmentTooltipState.isActive() || !vsq$isLeftAltHeld()) {
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

        if (event.key() == GLFW.GLFW_KEY_LEFT || event.key() == GLFW.GLFW_KEY_UP) {
            cir.setReturnValue(VSQEnchantmentTooltipState.cycle(component, -1));
        } else if (event.key() == GLFW.GLFW_KEY_RIGHT || event.key() == GLFW.GLFW_KEY_DOWN) {
            cir.setReturnValue(VSQEnchantmentTooltipState.cycle(component, 1));
        }
    }

    private static boolean vsq$isLeftAltHeld() {
        return Minecraft.getInstance().screen != null
                && GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;
    }
}
