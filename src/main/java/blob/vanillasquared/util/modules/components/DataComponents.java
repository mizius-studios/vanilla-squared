package blob.vanillasquared.util.modules.components;

import blob.vanillasquared.util.combat.components.dualwield.DualWieldComponent;
import blob.vanillasquared.util.combat.components.hitthrough.HitThroughComponent;
import net.minecraft.core.component.DataComponentType;

public final class DataComponents {
    public static final DataComponentType<DualWieldComponent> DUAL_WIELD = RegisterComponents.dualWieldComponent;
    public static final DataComponentType<HitThroughComponent> HIT_THROUGH = RegisterComponents.hitThroughComponent;

    private DataComponents() {
    }
}
