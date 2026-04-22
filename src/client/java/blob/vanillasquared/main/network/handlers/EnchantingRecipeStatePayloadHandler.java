package blob.vanillasquared.main.network.handlers;

import blob.vanillasquared.main.network.payload.EnchantingRecipeStatePayload;
import blob.vanillasquared.main.world.inventory.VSQEnchantmentMenuProperties;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EnchantingRecipeStatePayloadHandler {
    private static final Map<Integer, CachedRecipeState> PENDING_RECIPE_STATES = new HashMap<>();

    private EnchantingRecipeStatePayloadHandler() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(EnchantingRecipeStatePayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    PENDING_RECIPE_STATES.put(payload.containerId(), new CachedRecipeState(
                            payload.blockIds(),
                            payload.blockCounts(),
                            payload.requiredBlockCounts(),
                            payload.levelRequirement(),
                            payload.blockRequirement(),
                            payload.playerLevel(),
                            payload.recipeName(),
                            payload.recipeDescription()
                    ));

                    if (context.client().player == null) {
                        return;
                    }
                    if (!(Objects.requireNonNull(context.client().player).containerMenu instanceof VSQEnchantmentMenuProperties properties)) {
                        return;
                    }

                    properties.vsq$applyRecipeState(
                            payload.containerId(),
                            payload.blockIds(),
                            payload.blockCounts(),
                            payload.requiredBlockCounts(),
                            payload.levelRequirement(),
                            payload.blockRequirement(),
                            payload.playerLevel(),
                            payload.recipeName(),
                            payload.recipeDescription(),
                            payload.selectionCleared()
                    );
                })
        );
    }

    public static void applyCached(int containerId, VSQEnchantmentMenuProperties properties) {
        CachedRecipeState cachedRecipeState = PENDING_RECIPE_STATES.get(containerId);
        if (cachedRecipeState == null) {
            return;
        }

        properties.vsq$applyRecipeState(
                containerId,
                cachedRecipeState.blockIds(),
                cachedRecipeState.blockCounts(),
                cachedRecipeState.requiredBlockCounts(),
                cachedRecipeState.levelRequirement(),
                cachedRecipeState.blockRequirement(),
                cachedRecipeState.playerLevel(),
                cachedRecipeState.recipeName(),
                cachedRecipeState.recipeDescription(),
                false
        );
    }

    public static void clearContainer(int containerId) {
        PENDING_RECIPE_STATES.remove(containerId);
    }

    public static void clearAll() {
        PENDING_RECIPE_STATES.clear();
    }

    private record CachedRecipeState(
            List<Identifier> blockIds,
            List<Integer> blockCounts,
            List<Integer> requiredBlockCounts,
            int levelRequirement,
            int blockRequirement,
            int playerLevel,
            Component recipeName,
            Component recipeDescription
    ) {
        private CachedRecipeState {
            blockIds = List.copyOf(blockIds);
            blockCounts = List.copyOf(blockCounts);
            requiredBlockCounts = List.copyOf(requiredBlockCounts);
            recipeName = recipeName.copy();
            recipeDescription = recipeDescription.copy();
        }
    }
}
