package blob.vanillasquared.mixin.client.gui;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EditBox.class)
public abstract class EditBoxMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void vsq$ignoreVerticalArrows(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.key() == GLFW.GLFW_KEY_UP || event.key() == GLFW.GLFW_KEY_DOWN) {
            cir.setReturnValue(false);
        }
    }
}
