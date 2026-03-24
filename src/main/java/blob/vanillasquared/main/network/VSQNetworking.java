package blob.vanillasquared.main.network;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.main.network.payload.DebugPayload;
import blob.vanillasquared.main.network.payload.EnchantmentBlockCountsPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class VSQNetworking {
    private VSQNetworking() {}

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(DebugPayload.TYPE, DebugPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EnchantmentBlockCountsPayload.TYPE, EnchantmentBlockCountsPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DebugPayload.TYPE, (payload, context) -> context.server().execute(() -> {
            var player = context.player();
            VanillaSquared.LOGGER.info(
                    "[debug-key] {} ({}) pressed special effect key",
                    player.getGameProfile().name(),
                    player.getUUID()
            );
        }));
    }
}
