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
            int sweepingDmg,
            int criticalDmg
    ) {
        this.dualWieldComponent = new DualWieldComponent(
                identifiers,
                cooldown,
                criticalHits,
                blockedEnchantments,
                sweepingDmg,
                criticalDmg
        );
    }

    @SafeVarargs
    public DualWieldBuilder(
            List<String> identifiers,
            int cooldown,
            int criticalHits,
            int sweepingDmg,
            int criticalDmg,
            ResourceKey<Enchantment>... blockedEnchantments
    ) {
        this(
                identifiers,
                cooldown,
                criticalHits,
                Arrays.stream(blockedEnchantments).map(key -> key.identifier().toString()).toList(),
                sweepingDmg,
                criticalDmg
        );
    }

    public DualWieldComponent build() {
        return dualWieldComponent;
    }
}
