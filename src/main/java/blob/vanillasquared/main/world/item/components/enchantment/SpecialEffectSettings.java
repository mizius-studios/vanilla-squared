package blob.vanillasquared.main.world.item.components.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.enchantment.LevelBasedValue;

public record SpecialEffectSettings(
        LevelBasedValue limit
) {
    public static final Codec<SpecialEffectSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("limit", LevelBasedValue.constant(1.0F)).forGetter(SpecialEffectSettings::limit)
    ).apply(instance, SpecialEffectSettings::new));
}
