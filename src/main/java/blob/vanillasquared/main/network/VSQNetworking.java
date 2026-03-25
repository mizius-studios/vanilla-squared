package blob.vanillasquared.main.network;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.main.network.payload.DebugPayload;
import blob.vanillasquared.main.network.payload.EnchantingBookClickPayload;
import blob.vanillasquared.main.network.payload.EnchantmentBlockCountsPayload;
import blob.vanillasquared.main.world.inventory.VSQEnchantmentMenuProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class VSQNetworking {
    private VSQNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(DebugPayload.TYPE, DebugPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EnchantingBookClickPayload.TYPE, EnchantingBookClickPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EnchantmentBlockCountsPayload.TYPE, EnchantmentBlockCountsPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DebugPayload.TYPE, (payload, context) ->
                context.server().execute(() -> vsq$handleDebugPayload(context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(EnchantingBookClickPayload.TYPE, (payload, context) ->
                context.server().execute(() -> vsq$handleEnchantingBookClick(payload, context.player()))
        );
    }

    private static void vsq$handleDebugPayload(ServerPlayer player) {
        VanillaSquared.LOGGER.info(
                "[debug-key] {} ({}) pressed special effect key",
                player.getGameProfile().name(),
                player.getUUID()
        );
    }

    private static void vsq$handleEnchantingBookClick(EnchantingBookClickPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof EnchantmentMenu menu)) {
            return;
        }
        if (menu.containerId != payload.containerId()) {
            return;
        }
        if (menu instanceof VSQEnchantmentMenuProperties properties) {
            properties.vsq$tryCraftEnchantingRecipe(player);
        }
    }
}
