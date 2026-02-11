package blob.vanillasquared.mixin;

import net.minecraft.world.damagesource.CombatRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CombatRules.class)
public class CombatRulesMixin {
    private static final float VSQ_MAX_ARMOR_EFFECT = 1024.0F;

    @ModifyConstant(method = "getDamageAfterAbsorb", constant = @Constant(floatValue = 20.0F))
    private static float vsq$uncapArmorAbsorb(float original) {
        return VSQ_MAX_ARMOR_EFFECT;
    }

    @ModifyConstant(method = "getDamageAfterMagicAbsorb", constant = @Constant(floatValue = 20.0F))
    private static float vsq$uncapMagicAbsorb(float original) {
        return VSQ_MAX_ARMOR_EFFECT;
    }
}
