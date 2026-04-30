package blob.vanillasquared.main.world;

import blob.vanillasquared.main.VanillaSquared;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlag;

public final class VSQExperiments {
    public static final Identifier PREVIEW_FEATURE_ID = Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "preview");
    public static final Identifier BUILTIN_PACK_ID = Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "vsq_preview");

    private static FeatureFlag previewFlag;

    private VSQExperiments() {
    }

    public static void initialize() {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer("vanilla-squared")
                .orElseThrow(() -> new IllegalStateException("Missing mod container for vanilla-squared"));

        if (!ResourceLoader.registerBuiltinPack(
                BUILTIN_PACK_ID,
                modContainer,
                Component.translatable("vsq.gui.experiments.vsq"),
                PackActivationType.NORMAL
        )) {
            VanillaSquared.LOGGER.warn("Failed to register VSQ Preview builtin datapack {}", BUILTIN_PACK_ID);
        }

        ServerPlayerEvents.JOIN.register(VSQExperiments::grantPreviewRecipes);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> grantPreviewRecipes(newPlayer));
    }

    public static boolean isPreviewEnabled(ServerLevel level) {
        return previewFlag != null && level.enabledFeatures().contains(previewFlag);
    }

    public static void grantPreviewRecipes(ServerPlayer player) {
        if (!isPreviewEnabled(player.level())) {
            return;
        }
    }

    public static void vsq$setPreviewFlag(FeatureFlag flag) {
        previewFlag = flag;
    }
}
