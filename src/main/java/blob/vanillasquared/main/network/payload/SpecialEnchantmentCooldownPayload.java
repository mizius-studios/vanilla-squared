package blob.vanillasquared.main.network.payload;

import blob.vanillasquared.main.VanillaSquared;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record SpecialEnchantmentCooldownPayload(Identifier enchantmentId, long remainingTicks, long totalTicks) implements CustomPacketPayload {
    public static final Type<SpecialEnchantmentCooldownPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "special_enchantment_cooldown"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpecialEnchantmentCooldownPayload> CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.mapStream(buf -> buf),
            SpecialEnchantmentCooldownPayload::enchantmentId,
            ByteBufCodecs.VAR_LONG.mapStream(buf -> buf),
            SpecialEnchantmentCooldownPayload::remainingTicks,
            ByteBufCodecs.VAR_LONG.mapStream(buf -> buf),
            SpecialEnchantmentCooldownPayload::totalTicks,
            SpecialEnchantmentCooldownPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
