package blob.vanillasquared.main.world.item.components.enchantment;

import java.util.Optional;

public record SpecialEffectMetadata(
        String id,
        Optional<SpecialEffectSettings> special
) {
}
