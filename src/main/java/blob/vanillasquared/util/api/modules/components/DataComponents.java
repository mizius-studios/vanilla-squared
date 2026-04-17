package blob.vanillasquared.util.api.modules.components;

import blob.vanillasquared.main.world.item.components.enchantment.VSQEnchantmentComponent;
import blob.vanillasquared.main.world.item.components.hitthrough.HitThroughComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.Unit;

public final class DataComponents {
    public static final DataComponentType<VSQEnchantmentComponent> VSQ_ENCHANTMENT = RegisterComponents.enchantmentComponent;
    public static final DataComponentType<HitThroughComponent> HIT_THROUGH = RegisterComponents.hitThroughComponent;
    public static final DataComponentType<Unit> SPECIAL_ENCHANTMENT_EFFECT = RegisterComponents.specialEnchantmentEffect;

    private DataComponents() {
    }
}
