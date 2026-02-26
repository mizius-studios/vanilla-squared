package blob.vanillasquared.util.combat;

import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class DamageUtil {

    private DamageUtil() {
    }

    public static float applyMaceProtection(LivingEntity entity, DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.IS_MACE_SMASH)) {
            return Math.max(amount, 0.0F);
        }

        double protection = entity.getAttributeValue(RegisterAttributes.maceProtectionAttribute);
        protection = Math.clamp(protection, 0.0, 1.0);
        return Math.max(amount * (1.0F - (float) protection), 0.0F);
    }
}
