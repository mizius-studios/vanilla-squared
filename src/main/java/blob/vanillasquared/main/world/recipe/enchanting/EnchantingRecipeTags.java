package blob.vanillasquared.main.world.recipe.enchanting;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.main.world.item.VSQItems;
import blob.vanillasquared.util.api.modules.components.DataComponents;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class EnchantingRecipeTags {
    public static final Identifier DEFAULT_LOOT_TAG = Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "default");
    public static final Identifier DEFAULT_LIBRARIAN_TAG = Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "villager/librarian/default");
    private static final Identifier RELOAD_LISTENER_ID = Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "enchanting_recipe_tag_loader");
    private static final FileToIdConverter TAG_CONVERTER = FileToIdConverter.json("tags/recipes");
    private static volatile Map<Identifier, List<ResourceKey<Recipe<?>>>> TAGS = Map.of();

    private EnchantingRecipeTags() {
    }

    public static void initialize() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(RELOAD_LISTENER_ID, ReloadListener::new);
    }

    public static boolean isValidRecipe(ResourceKey<Recipe<?>> recipeKey) {
        return EnchantingRecipeRegistry.contains(recipeKey);
    }

    public static Optional<ResourceKey<Recipe<?>>> randomRecipe(Identifier tagId, RandomSource random) {
        List<ResourceKey<Recipe<?>>> entries = TAGS.getOrDefault(tagId, List.of()).stream()
                .filter(EnchantingRecipeRegistry::contains)
                .toList();
        if (entries.isEmpty()) {
            VanillaSquared.LOGGER.warn("Enchant recipe tag {} is missing, empty, or contains no valid enchanting recipes", tagId);
            return Optional.empty();
        }
        return Optional.of(entries.get(random.nextInt(entries.size())));
    }

    public static ItemStack createStack(ResourceKey<Recipe<?>> recipeKey) {
        ItemStack stack = new ItemStack(VSQItems.ENCHANT_RECIPE);
        stack.set(DataComponents.ENCHANT_RECIPE, recipeKey);
        return stack;
    }

    public static ItemStack createRandomStack(Identifier tagId, RandomSource random) {
        return randomRecipe(tagId, random)
                .map(EnchantingRecipeTags::createStack)
                .orElse(ItemStack.EMPTY);
    }

    public static Identifier tag(String path) {
        return Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, path);
    }

    private static final class ReloadListener implements SimpleResourceReloadListener<Map<Identifier, List<ResourceKey<Recipe<?>>>>>, IdentifiableResourceReloadListener {
        private ReloadListener(HolderLookup.Provider registries) {
        }

        @Override
        public Identifier getFabricId() {
            return RELOAD_LISTENER_ID;
        }

        @Override
        public CompletableFuture<Map<Identifier, List<ResourceKey<Recipe<?>>>>> load(ResourceManager resourceManager, Executor executor) {
            return CompletableFuture.supplyAsync(() -> {
                Map<Identifier, List<ResourceKey<Recipe<?>>>> loaded = new LinkedHashMap<>();
                for (Map.Entry<Identifier, Resource> entry : TAG_CONVERTER.listMatchingResources(resourceManager).entrySet()) {
                    Identifier fileId = entry.getKey();
                    Identifier tagId = TAG_CONVERTER.fileToId(fileId);
                    try (Reader reader = entry.getValue().openAsReader()) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        List<ResourceKey<Recipe<?>>> values = new ArrayList<>();
                        for (JsonElement value : json.getAsJsonArray("values")) {
                            if (!value.isJsonPrimitive()) {
                                continue;
                            }
                            Identifier recipeId = Identifier.tryParse(value.getAsString());
                            if (recipeId != null) {
                                values.add(ResourceKey.create(Registries.RECIPE, recipeId));
                            }
                        }
                        loaded.merge(tagId, List.copyOf(values), (left, right) -> {
                            List<ResourceKey<Recipe<?>>> merged = new ArrayList<>(left);
                            merged.addAll(right);
                            return List.copyOf(merged);
                        });
                    } catch (Exception exception) {
                        VanillaSquared.LOGGER.error("Failed to load enchant recipe tag {} from {}", tagId, fileId, exception);
                    }
                }
                return loaded;
            }, executor);
        }

        @Override
        public CompletableFuture<Void> apply(Map<Identifier, List<ResourceKey<Recipe<?>>>> data, ResourceManager resourceManager, Executor executor) {
            return CompletableFuture.runAsync(() -> {
                TAGS = Map.copyOf(data);
                VanillaSquared.LOGGER.info("Loaded {} enchant recipe tags", TAGS.size());
            }, executor);
        }
    }
}
