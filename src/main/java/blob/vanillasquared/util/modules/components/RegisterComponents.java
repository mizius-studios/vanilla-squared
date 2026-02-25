package blob.vanillasquared.util.modules.components;

import blob.vanillasquared.util.builder.components.DualWieldComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class RegisterComponents {

    public static final DataComponentType<DualWieldComponent> dualWield = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("vsq", "dual_wield"),
            DataComponentType.<DualWieldComponent>builder()
                    .persistent(DualWieldComponent.CODEC)
                    .networkSynchronized(DualWieldComponent.STREAM_CODEC)
                    .cacheEncoding()
                    .build()
    );

    private RegisterComponents() {
    }

    public static void initialize() {
    }
}
