package blob.vanillasquared.main.world.item.enchantment.effects;

import blob.vanillasquared.util.api.enchantment.VSQEnchantmentEffects;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;

public final class VSQEnchantmentEntityEffects {
    public static final MapCodec<? extends EnchantmentEntityEffect> CHANNELING = VSQEnchantmentEffects.CHANNELING;
    public static final MapCodec<? extends EnchantmentEntityEffect> SEND_CHAT_MSG = VSQEnchantmentEffects.SEND_CHAT_MESSAGE;

    private VSQEnchantmentEntityEffects() {
    }

    public static void initialize() {
    }
}
