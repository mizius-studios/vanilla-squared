package blob.vanillasquared.main.world.item.components.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record VSQEnchantmentProfile(
        Optional<VSQEnchantmentProfileRequirement> requirement,
        VSQEnchantmentSlotType enchantmentSlot,
        HolderSet<Enchantment> exclusiveSet,
        int maxLevel,
        DataComponentMap effects,
        Optional<SpecialEnchantmentProfileConfig> special,
        SpecialEffectMetadataIndex specialEffectIndex,
        List<EquipmentSlotGroup> slots,
        Enchantment.Cost maxCost,
        Enchantment.Cost minCost
) {
    public static final Codec<VSQEnchantmentProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VSQEnchantmentProfileRequirement.CODEC.optionalFieldOf("requirement").forGetter(VSQEnchantmentProfile::requirement),
            VSQEnchantmentSlotType.CODEC.fieldOf("enchantment_slot").forGetter(VSQEnchantmentProfile::enchantmentSlot),
            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("exclusive_set", HolderSet.empty()).forGetter(VSQEnchantmentProfile::exclusiveSet),
            Codec.intRange(1, Enchantment.MAX_LEVEL).fieldOf("max_level").forGetter(VSQEnchantmentProfile::maxLevel),
            EnchantmentEffectComponents.CODEC.optionalFieldOf("effects", DataComponentMap.EMPTY).forGetter(VSQEnchantmentProfile::effects),
            Codec.PASSTHROUGH.optionalFieldOf("effects").forGetter(profile -> Optional.empty()),
            SpecialEnchantmentProfileConfig.CODEC.optionalFieldOf("special").forGetter(VSQEnchantmentProfile::special),
            EquipmentSlotGroup.CODEC.listOf().fieldOf("slots").forGetter(VSQEnchantmentProfile::slots),
            Enchantment.Cost.CODEC.fieldOf("max_cost").forGetter(VSQEnchantmentProfile::maxCost),
            Enchantment.Cost.CODEC.fieldOf("min_cost").forGetter(VSQEnchantmentProfile::minCost)
    ).apply(instance, VSQEnchantmentProfile::create));

    private static VSQEnchantmentProfile create(
            Optional<VSQEnchantmentProfileRequirement> requirement,
            VSQEnchantmentSlotType enchantmentSlot,
            HolderSet<Enchantment> exclusiveSet,
            int maxLevel,
            DataComponentMap effects,
            Optional<Dynamic<?>> rawEffects,
            Optional<SpecialEnchantmentProfileConfig> special,
            List<EquipmentSlotGroup> slots,
            Enchantment.Cost maxCost,
            Enchantment.Cost minCost
    ) {
        return new VSQEnchantmentProfile(
                requirement,
                enchantmentSlot,
                exclusiveSet,
                maxLevel,
                effects,
                special,
                SpecialEffectMetadataIndex.fromDynamic(rawEffects),
                slots,
                maxCost,
                minCost
        );
    }

    public boolean matches(ItemStack stack) {
        return this.requirement.map(requirement -> requirement.matches(stack)).orElse(true);
    }
}
