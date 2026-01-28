package blob.combatupdate.util.data;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public record Axe(
        float blockDelay,
        float shieldBreakCooldown,
        float damageReduction,
        float duraDamage,
        SoundEvent blockSound,
        SoundEvent breakSound
) {
    public static final Axe DEFAULT = new Axe(0F, 0F, 0F, 1F, SoundEvents.ANVIL_LAND, SoundEvents.ANVIL_BREAK);
}
