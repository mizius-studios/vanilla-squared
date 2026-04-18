package blob.vanillasquared.util.api.modules.components;

import blob.vanillasquared.main.world.item.components.enchantment.SpecialEnchantmentEffect;
import blob.vanillasquared.main.world.item.components.enchantment.VSQEnchantmentComponent;
import blob.vanillasquared.main.world.item.components.hitthrough.HitThroughComponent;
import net.minecraft.core.component.DataComponentType;

public final class DataComponents {
    public static final DataComponentType<VSQEnchantmentComponent> VSQ_ENCHANTMENT = RegisterComponents.enchantmentComponent;
    public static final DataComponentType<HitThroughComponent> HIT_THROUGH = RegisterComponents.hitThroughComponent;
    public static final DataComponentType<SpecialEnchantmentEffect> SPECIAL_ENCHANTMENT_EFFECT = RegisterComponents.specialEnchantmentEffect;

    private DataComponents() {
    }
}
