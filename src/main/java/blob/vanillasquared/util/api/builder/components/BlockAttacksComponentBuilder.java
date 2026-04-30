package blob.vanillasquared.util.api.builder.components;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.component.BlocksAttacks;

import java.util.List;
import java.util.Optional;

public final class BlockAttacksComponentBuilder {
    private final BlocksAttacks component;

    public BlockAttacksComponentBuilder(float blockDelay, float shieldBreakCooldown, float damageReduction, float durabilityDamage) {
        this.component = new BlocksAttacks(
                blockDelay,
                shieldBreakCooldown,
                List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, damageReduction)),
                new BlocksAttacks.ItemDamageFunction(1.0F, 1.0F, durabilityDamage),
                bypassesShieldTag(),
                Optional.of(SoundEvents.SHIELD_BLOCK),
                Optional.of(SoundEvents.SHIELD_BREAK)
        );
    }

    public BlocksAttacks build() {
        return component;
    }

    private static Optional<HolderSet<DamageType>> bypassesShieldTag() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                .lookup(Registries.DAMAGE_TYPE)
                .flatMap(registry -> registry.get(DamageTypeTags.BYPASSES_SHIELD))
                .map(holderSet -> (HolderSet<DamageType>) holderSet);
    }
}
