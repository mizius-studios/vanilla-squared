package blob.vanillasquared.main.world.item.enchantment.effects;

import blob.vanillasquared.main.world.effect.LungingState;
import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.util.api.enchantment.VSQEnchantments;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AllOf;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record VSQBeginLungingEffect(LevelBasedValue range) implements EnchantmentEntityEffect {
    public static final MapCodec<VSQBeginLungingEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("range").forGetter(VSQBeginLungingEffect::range)
    ).apply(instance, VSQBeginLungingEffect::new));

    @Override
    public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        Holder<Enchantment> enchantment = resolveEnchantment(item, this);
        if (enchantment == null) {
            VanillaSquared.LOGGER.warn("Failed to resolve lunging enchantment holder for {}", item.itemStack().getHoverName().getString());
            return;
        }

        LungingState.start(serverLevel, enchantment, enchantmentLevel, item, livingEntity, Math.clamp(this.range.calculate(enchantmentLevel), 0.0D, 7.0D));
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return MAP_CODEC;
    }

    private static Holder<Enchantment> resolveEnchantment(EnchantedItemInUse item, VSQBeginLungingEffect effect) {
        for (var entry : VSQEnchantments.aggregate(item.itemStack()).entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            List<ConditionalEffect<EnchantmentEntityEffect>> effects =
                    VSQEnchantments.profileEffects(item.itemStack(), enchantment, EnchantmentEffectComponents.HIT_BLOCK);
            for (ConditionalEffect<EnchantmentEntityEffect> conditional : effects) {
                if (containsEffect(conditional.effect(), effect)) {
                    return enchantment;
                }
            }
        }
        return null;
    }

    private static boolean containsEffect(EnchantmentEntityEffect root, VSQBeginLungingEffect target) {
        if (root.equals(target)) {
            return true;
        }
        if (root instanceof AllOf.EntityEffects(List<EnchantmentEntityEffect> effects)) {
            for (EnchantmentEntityEffect child : effects) {
                if (containsEffect(child, target)) {
                    return true;
                }
            }
        }
        return false;
    }
}
