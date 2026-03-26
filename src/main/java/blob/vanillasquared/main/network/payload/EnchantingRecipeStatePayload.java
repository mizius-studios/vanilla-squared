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

public record EnchantingRecipeStatePayload(
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
    public static final Type<EnchantingRecipeStatePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "enchanting_recipe_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingRecipeStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EnchantingRecipeStatePayload::containerId,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), EnchantingRecipeStatePayload::blockIds,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), EnchantingRecipeStatePayload::blockCounts,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), EnchantingRecipeStatePayload::requiredBlockCounts,
            ByteBufCodecs.VAR_INT, EnchantingRecipeStatePayload::playerLevel,
            ByteBufCodecs.VAR_INT, EnchantingRecipeStatePayload::levelRequirement,
            ByteBufCodecs.VAR_INT, EnchantingRecipeStatePayload::blockRequirement,
            ComponentSerialization.TRUSTED_STREAM_CODEC, EnchantingRecipeStatePayload::recipeName,
            ComponentSerialization.TRUSTED_STREAM_CODEC, EnchantingRecipeStatePayload::recipeDescription,
            EnchantingRecipeStatePayload::new
    );

    public EnchantingRecipeStatePayload {
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
