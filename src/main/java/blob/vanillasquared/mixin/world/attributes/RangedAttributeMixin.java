package blob.vanillasquared.mixin.world.attributes;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedAttribute.class)
public class RangedAttributeMixin {
    @Shadow
    @Final
    @Mutable
    private double maxValue;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vsq$expandArmorRange(String descriptionId, double defaultValue, double minValue, double maxValue, CallbackInfo ci) {
        if ("attribute.name.armor".equals(descriptionId) || "attribute.name.armor_toughness".equals(descriptionId)) {
            this.maxValue = 1024.0;
        }
    }
}
