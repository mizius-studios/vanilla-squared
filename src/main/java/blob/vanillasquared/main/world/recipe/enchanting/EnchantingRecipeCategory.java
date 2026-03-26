package blob.vanillasquared.main.world.recipe.enchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.item.crafting.RecipeBookCategory;

import java.util.Locale;

public enum EnchantingRecipeCategory {
    WEAPONS("weapons", VSQEnchantmentRecipeBookCategories.WEAPONS),
    TOOLS("tools", VSQEnchantmentRecipeBookCategories.TOOLS),
    ARMOR("armor", VSQEnchantmentRecipeBookCategories.ARMOR),
    UTIL("util", VSQEnchantmentRecipeBookCategories.UTIL);

    public static final Codec<EnchantingRecipeCategory> CODEC = Codec.STRING.flatXmap(
            EnchantingRecipeCategory::vsq$byName,
            category -> DataResult.success(category.serializedName)
    );

    private final String serializedName;
    private final RecipeBookCategory recipeBookCategory;

    EnchantingRecipeCategory(String serializedName, RecipeBookCategory recipeBookCategory) {
        this.serializedName = serializedName;
        this.recipeBookCategory = recipeBookCategory;
    }

    public String serializedName() {
        return this.serializedName;
    }

    public RecipeBookCategory recipeBookCategory() {
        return this.recipeBookCategory;
    }

    public static EnchantingRecipeCategory fromSerializedName(String name) {
        return vsq$byName(name).result().orElseThrow(() -> new IllegalArgumentException("Unknown enchanting recipe category: " + name));
    }

    private static DataResult<EnchantingRecipeCategory> vsq$byName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);
        for (EnchantingRecipeCategory category : values()) {
            if (category.serializedName.equals(normalized)) {
                return DataResult.success(category);
            }
        }
        return DataResult.error(() -> "Unknown enchanting recipe category: " + name);
    }
}
