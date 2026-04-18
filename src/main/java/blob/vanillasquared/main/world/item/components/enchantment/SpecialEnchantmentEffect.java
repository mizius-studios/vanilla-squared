package blob.vanillasquared.main.world.item.components.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record SpecialEnchantmentEffect(long cooldown) {
    public static final Codec<SpecialEnchantmentEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("cooldown").forGetter(SpecialEnchantmentEffect::cooldown)
    ).apply(instance, SpecialEnchantmentEffect::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpecialEnchantmentEffect> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(SpecialEnchantmentEffect::new, SpecialEnchantmentEffect::cooldown)
                    .mapStream(buf -> buf);

    public long cooldownTicks() {
        return Math.max(0L, this.cooldown) * 20L;
    }
}
