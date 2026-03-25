package blob.vanillasquared.main.world.recipe.enchanting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record EnchantingRecipe(
        EnchantingIngredient input,
        EnchantingIngredient material,
        List<EnchantingIngredient> ingredients,
        EnchantingComponentModifier componentModifier
) implements Recipe<EnchantingRecipeInput> {
    public static final MapCodec<EnchantingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EnchantingIngredient.CODEC.fieldOf("input").forGetter(EnchantingRecipe::input),
            EnchantingIngredient.CODEC.fieldOf("material").forGetter(EnchantingRecipe::material),
            EnchantingIngredient.CODEC.listOf().fieldOf("ingredients").forGetter(EnchantingRecipe::ingredients),
            EnchantingComponentModifier.CODEC.fieldOf("component_modifier").forGetter(EnchantingRecipe::componentModifier)
    ).apply(instance, EnchantingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingRecipe> STREAM_CODEC = StreamCodec.composite(
            EnchantingIngredient.STREAM_CODEC, EnchantingRecipe::input,
            EnchantingIngredient.STREAM_CODEC, EnchantingRecipe::material,
            EnchantingIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), EnchantingRecipe::ingredients,
            EnchantingComponentModifier.STREAM_CODEC, EnchantingRecipe::componentModifier,
            EnchantingRecipe::new
    );

    public EnchantingRecipe {
        ingredients = List.copyOf(ingredients);
        if (ingredients.size() != 4) {
            throw new IllegalArgumentException("Enchanting recipes require exactly 4 cross ingredients");
        }
    }

    @Override
    public boolean matches(EnchantingRecipeInput input, Level level) {
        return this.findMatch(input).isPresent();
    }

    @Override
    public ItemStack assemble(EnchantingRecipeInput input) {
        return input.input().copy();
    }

    public ItemStack assemble(EnchantingRecipeInput input, net.minecraft.core.HolderLookup.Provider registries) {
        return this.componentModifier.apply(input.input(), registries);
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        List<Ingredient> placementIngredients = new ArrayList<>(6);
        placementIngredients.add(this.input.ingredient());
        placementIngredients.add(this.material.ingredient());
        for (EnchantingIngredient ingredient : this.ingredients) {
            placementIngredients.add(ingredient.ingredient());
        }
        return PlacementInfo.create(placementIngredients);
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<EnchantingRecipe> getSerializer() {
        return VSQRecipeTypes.ENCHANTING_SERIALIZER;
    }

    @Override
    public RecipeType<EnchantingRecipe> getType() {
        return VSQRecipeTypes.ENCHANTING_TYPE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return new RecipeBookCategory();
    }

    public Optional<Match> findMatch(EnchantingRecipeInput input) {
        if (!this.input.test(input.input()) || !this.material.test(input.material())) {
            return Optional.empty();
        }

        List<ItemStack> crossStacks = input.ingredients();
        boolean[] usedSlots = new boolean[crossStacks.size()];
        List<Integer> matchedCrossSlots = new ArrayList<>(this.ingredients.size());

        for (ItemStack stack : crossStacks) {
            if (stack.isEmpty()) {
                return Optional.empty();
            }
        }

        for (EnchantingIngredient ingredient : this.ingredients) {
            boolean matched = false;

            for (int i = 0; i < crossStacks.size(); i++) {
                if (usedSlots[i] || !ingredient.test(crossStacks.get(i))) {
                    continue;
                }

                usedSlots[i] = true;
                matchedCrossSlots.add(i);
                matched = true;
                break;
            }

            if (!matched) {
                return Optional.empty();
            }
        }

        return Optional.of(new Match(matchedCrossSlots));
    }

    public record Match(List<Integer> matchedCrossSlots) {
        public Match {
            matchedCrossSlots = List.copyOf(matchedCrossSlots);
        }
    }
}
