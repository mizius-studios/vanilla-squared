package blob.vanillasquared.main.network.payload;

import blob.vanillasquared.main.VanillaSquared;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record EnchantmentBlockCountsPayload(
        int containerId,
        List<Identifier> blockIds,
        List<Integer> blockCounts,
        List<Integer> requiredBlockCounts,
        int playerLevel,
        int levelRequirement,
        int blockRequirement,
        Component recipeName,
        Component recipeDescription
) implements CustomPacketPayload {
    public static final Type<EnchantmentBlockCountsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "enchantment_block_counts"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentBlockCountsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EnchantmentBlockCountsPayload::containerId,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), EnchantmentBlockCountsPayload::blockIds,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), EnchantmentBlockCountsPayload::blockCounts,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), EnchantmentBlockCountsPayload::requiredBlockCounts,
            ByteBufCodecs.VAR_INT, EnchantmentBlockCountsPayload::playerLevel,
            ByteBufCodecs.VAR_INT, EnchantmentBlockCountsPayload::levelRequirement,
            ByteBufCodecs.VAR_INT, EnchantmentBlockCountsPayload::blockRequirement,
            ComponentSerialization.TRUSTED_STREAM_CODEC, EnchantmentBlockCountsPayload::recipeName,
            ComponentSerialization.TRUSTED_STREAM_CODEC, EnchantmentBlockCountsPayload::recipeDescription,
            EnchantmentBlockCountsPayload::new
    );

    public EnchantmentBlockCountsPayload {
        blockIds = List.copyOf(blockIds);
        blockCounts = List.copyOf(blockCounts);
        requiredBlockCounts = List.copyOf(requiredBlockCounts);
        recipeName = recipeName.copy();
        recipeDescription = recipeDescription.copy();

        if (blockIds.size() != blockCounts.size()) {
            throw new IllegalArgumentException("blockIds and blockCounts must have the same size");
        }
        if (blockIds.size() != requiredBlockCounts.size()) {
            throw new IllegalArgumentException("blockIds and requiredBlockCounts must have the same size");
        }
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
