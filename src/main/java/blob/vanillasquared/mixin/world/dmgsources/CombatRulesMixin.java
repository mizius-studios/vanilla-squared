package blob.vanillasquared.mixin.world.dmgsources;

import net.minecraft.world.damagesource.CombatRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CombatRules.class)
public class CombatRulesMixin {
    @Unique
    private static final float maxArmorEffect = 1024.0F;

    @ModifyConstant(method = "getDamageAfterAbsorb", constant = @Constant(floatValue = 20.0F))
    private static float vsq$uncapArmorAbsorb(float original) {
        return maxArmorEffect;
    }

    @ModifyConstant(method = "getDamageAfterMagicAbsorb", constant = @Constant(floatValue = 20.0F))
    private static float vsq$uncapMagicAbsorb(float original) {
        return maxArmorEffect;
    }
}
