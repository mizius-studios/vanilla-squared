package blob.vanillasquared.main.world.item.enchantment;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.util.api.modules.components.ComponentRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.enchantment.TargetedConditionalEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;

public final class VSQEnchantmentEffectComponents {
    public static final DataComponentType<List<TargetedConditionalEffect<EnchantmentEntityEffect>>> POST_BLOCK = registerEffectComponent(
            Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "post_block"),
            validatedListCodec(TargetedConditionalEffect.codec(EnchantmentEntityEffect.CODEC), LootContextParamSets.ENCHANTED_DAMAGE)
    );
    public static final DataComponentType<List<TargetedConditionalEffect<EnchantmentLocationBasedEffect>>> CHANNELING_PATH = registerEffectComponent(
            Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "channeling_path"),
            validatedListCodec(TargetedConditionalEffect.codec(EnchantmentLocationBasedEffect.CODEC), LootContextParamSets.ENCHANTED_DAMAGE)
    );

    private VSQEnchantmentEffectComponents() {
    }

    public static void initialize() {
    }

    private static <T extends Validatable> Codec<List<T>> validatedListCodec(Codec<T> elementCodec, ContextKeySet paramSet) {
        return elementCodec.listOf().validate(Validatable.listValidatorForContext(paramSet));
    }

    private static <T> DataComponentType<T> registerEffectComponent(Identifier id, Codec<T> codec) {
        return ComponentRegistry.registerPersistent(id, codec, BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE);
    }
}
