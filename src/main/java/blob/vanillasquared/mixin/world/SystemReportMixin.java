package blob.vanillasquared.mixin.world;

import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SystemReport.class)
public abstract class SystemReportMixin {
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/SystemReport;ignoreErrors(Ljava/lang/String;Ljava/lang/Runnable;)V"
            )
    )
    private void vsq$skipHardwareProbe(SystemReport report, String group, Runnable action) {
        report.setDetail("Hardware", "Skipped to avoid Windows WMI startup hang");
    }
}
