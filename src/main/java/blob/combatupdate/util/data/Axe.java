package blob.combatupdate.util.data;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public record Axe(
        float blockDelay,
        float shieldBreakCooldown,
        float damageReduction,
        float duraDamage,
        double attackDamage,
        double attackSpeed,
        SoundEvent blockSound,
        SoundEvent breakSound
) {
    public static final Axe DEFAULT = new Axe(0F, 0F, 0F, 1F, 1.0d, 1.0d, SoundEvents.ANVIL_LAND, SoundEvents.ANVIL_BREAK);
}
