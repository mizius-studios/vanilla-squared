package blob.vanillasquared.util.builder.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record DualWieldComponent(
        List<String> identifiers,
        int cooldown,
        int criticalHits,
        List<String> blockedEnchantments,
        int sweepingDmg,
        int criticalDmg
) {
    public static final Codec<DualWieldComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().fieldOf("identifiers").forGetter(DualWieldComponent::identifiers),
                    Codec.INT.fieldOf("cooldown").forGetter(DualWieldComponent::cooldown),
                    Codec.INT.fieldOf("critical_hits").forGetter(DualWieldComponent::criticalHits),
                    Codec.STRING.listOf().fieldOf("blocked_enchantments").forGetter(DualWieldComponent::blockedEnchantments),
                    Codec.INT.fieldOf("sweeping_dmg").forGetter(DualWieldComponent::sweepingDmg),
                    Codec.INT.fieldOf("critical_dmg").forGetter(DualWieldComponent::criticalDmg)
            ).apply(instance, DualWieldComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DualWieldComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DualWieldComponent::identifiers,
            ByteBufCodecs.VAR_INT, DualWieldComponent::cooldown,
            ByteBufCodecs.VAR_INT, DualWieldComponent::criticalHits,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DualWieldComponent::blockedEnchantments,
            ByteBufCodecs.VAR_INT, DualWieldComponent::sweepingDmg,
            ByteBufCodecs.VAR_INT, DualWieldComponent::criticalDmg,
            DualWieldComponent::new
    );

    public DualWieldComponent {
        identifiers = List.copyOf(identifiers);
        blockedEnchantments = List.copyOf(blockedEnchantments);
        cooldown = Math.max(0, cooldown);
        criticalHits = Math.max(0, criticalHits);
        sweepingDmg = Math.max(0, sweepingDmg);
        criticalDmg = Math.max(0, criticalDmg);
    }
}
