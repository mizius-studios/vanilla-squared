package blob.vanillasquared.main.world.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class VoidedMobEffect extends MobEffect {
    private static final int VOIDED_COLOR = 0xFF2B2B;

    public VoidedMobEffect() {
        super(MobEffectCategory.HARMFUL, VOIDED_COLOR);
    }
}
