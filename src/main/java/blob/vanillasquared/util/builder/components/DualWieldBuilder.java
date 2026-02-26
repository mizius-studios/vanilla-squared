package blob.vanillasquared.util.builder.components;

import blob.vanillasquared.util.combat.components.dualwield.DualWieldComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Arrays;
import java.util.List;

public class DualWieldBuilder {
    private final DualWieldComponent dualWieldComponent;

    public DualWieldBuilder(
            List<String> identifiers,
            int cooldown,
            int criticalHits,
            List<String> blockedEnchantments,
            int sweepingDamage,
            int criticalDamage
    ) {
        this.dualWieldComponent = new DualWieldComponent(
                identifiers,
                cooldown,
                criticalHits,
                blockedEnchantments,
                sweepingDamage,
                criticalDamage
        );
    }

    @SafeVarargs
    public DualWieldBuilder(
            List<String> identifiers,
            int cooldown,
            int criticalHits,
            int sweepingDamage,
            int criticalDamage,
            ResourceKey<Enchantment>... blockedEnchantments
    ) {
        this(
                identifiers,
                cooldown,
                criticalHits,
                Arrays.stream(blockedEnchantments).map(key -> key.identifier().toString()).toList(),
                sweepingDamage,
                criticalDamage
        );
    }

    public DualWieldComponent build() {
        return dualWieldComponent;
    }
}
