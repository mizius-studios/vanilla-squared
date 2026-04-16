package blob.vanillasquared.main.world.item.components.dualwield;

import blob.vanillasquared.util.api.references.RegistryReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Objects;

public record DualWieldComponent(
        List<String> identifiers,
        int cooldown,
        int criticalHits,
        RegistryReference blockedEnchantments,
        int sweepingDamage,
        int criticalDamage
) {
    public static final Codec<DualWieldComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().fieldOf("identifiers").forGetter(DualWieldComponent::identifiers),
                    Codec.INT.fieldOf("cooldown").forGetter(DualWieldComponent::cooldown),
                    Codec.INT.fieldOf("critical_hits").forGetter(DualWieldComponent::criticalHits),
                    RegistryReference.CODEC.fieldOf("blocked_enchantments").forGetter(DualWieldComponent::blockedEnchantments),
                    Codec.INT.fieldOf("sweeping_dmg").forGetter(DualWieldComponent::sweepingDamage),
                    Codec.INT.fieldOf("critical_dmg").forGetter(DualWieldComponent::criticalDamage)
            ).apply(instance, DualWieldComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DualWieldComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DualWieldComponent::identifiers,
            ByteBufCodecs.VAR_INT, DualWieldComponent::cooldown,
            ByteBufCodecs.VAR_INT, DualWieldComponent::criticalHits,
            ByteBufCodecs.BOOL, component -> component.blockedEnchantments().tag(),
            Identifier.STREAM_CODEC, component -> component.blockedEnchantments().id(),
            ByteBufCodecs.VAR_INT, DualWieldComponent::sweepingDamage,
            ByteBufCodecs.VAR_INT, DualWieldComponent::criticalDamage,
            (identifiers, cooldown, criticalHits, blockedTag, blockedId, sweepingDamage, criticalDamage) ->
                    new DualWieldComponent(identifiers, cooldown, criticalHits, new RegistryReference(blockedId, blockedTag), sweepingDamage, criticalDamage)
    );

    public DualWieldComponent(
            List<String> identifiers,
            int cooldown,
            int criticalHits,
            Identifier blockedEnchantmentsTag,
            int sweepingDamage,
            int criticalDamage
    ) {
        this(identifiers, cooldown, criticalHits, RegistryReference.tag(blockedEnchantmentsTag), sweepingDamage, criticalDamage);
    }

    public DualWieldComponent {
        identifiers = List.copyOf(identifiers);
        blockedEnchantments = Objects.requireNonNull(blockedEnchantments, "blockedEnchantments");
        cooldown = Math.max(0, cooldown);
        criticalHits = Math.max(0, criticalHits);
        sweepingDamage = Math.max(0, sweepingDamage);
        criticalDamage = Math.max(0, criticalDamage);
    }

    public Identifier blockedEnchantmentsTag() {
        if (!this.blockedEnchantments.tag()) {
            throw new IllegalStateException("blockedEnchantments is not a tag reference");
        }
        return this.blockedEnchantments.id();
    }
}
