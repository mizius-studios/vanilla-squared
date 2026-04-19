package blob.vanillasquared.mixin.client;

import com.mojang.blaze3d.platform.GLX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GLX.class)
public abstract class GLXMixin {
    @Inject(method = "_getCpuInfo", at = @At("HEAD"), cancellable = true)
    private static void vsq$skipCpuInfoProbe(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(System.getProperty("os.arch", "unknown"));
    }
}
