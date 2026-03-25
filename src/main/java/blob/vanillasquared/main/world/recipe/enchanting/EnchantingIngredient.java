package blob.vanillasquared.main.world.recipe.enchanting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public record EnchantingIngredient(Ingredient ingredient, int count) {
    private static final Codec<Integer> COUNT_CODEC = Codec.intRange(1, Item.ABSOLUTE_MAX_STACK_SIZE);

    public static final Codec<EnchantingIngredient> CODEC = ExtraCodecs.JSON.flatXmap(
            EnchantingIngredient::vsq$decodeIngredient,
            enchantingIngredient -> DataResult.success(enchantingIngredient.toJson())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, EnchantingIngredient::ingredient,
            ByteBufCodecs.VAR_INT, EnchantingIngredient::count,
            EnchantingIngredient::new
    );

    public EnchantingIngredient {
        if (count < 1 || count > Item.ABSOLUTE_MAX_STACK_SIZE) {
            throw new IllegalArgumentException("Enchanting ingredient count must be between 1 and " + Item.ABSOLUTE_MAX_STACK_SIZE);
        }
    }

    public boolean test(ItemStack stack) {
        return this.ingredient.test(stack) && stack.getCount() >= this.count;
    }

    public JsonElement toJson() {
        JsonElement encoded = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, this.ingredient)
                .result()
                .orElseThrow(() -> new IllegalArgumentException("Failed to encode ingredient"));
        if (this.count == 1) {
            return encoded;
        }
        if (!encoded.isJsonObject()) {
            throw new IllegalArgumentException("Counted enchanting ingredients must encode as a JSON object");
        }

        JsonObject object = encoded.getAsJsonObject().deepCopy();
        object.addProperty("count", this.count);
        return object;
    }

    private static DataResult<EnchantingIngredient> vsq$decodeIngredient(Object input) {
        if (!(input instanceof JsonElement json)) {
            return DataResult.error(() -> "Enchanting ingredient must be valid JSON");
        }

        try {
            return DataResult.success(vsq$parseIngredient(json));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(exception::getMessage);
        }
    }

    private static EnchantingIngredient vsq$parseIngredient(JsonElement json) {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.isEmpty()) {
                throw new IllegalArgumentException("Ingredient array must not be empty");
            }

            List<Holder<Item>> holders = new ArrayList<>();
            for (JsonElement element : array) {
                holders.addAll(vsq$parseIngredientEntries(element));
            }
            if (holders.isEmpty()) {
                throw new IllegalArgumentException("Ingredient array resolved to no items");
            }
            return new EnchantingIngredient(Ingredient.of(HolderSet.direct(holders)), 1);
        }

        if (!json.isJsonObject()) {
            throw new IllegalArgumentException("Ingredient must be a JSON object or array");
        }

        JsonObject object = json.getAsJsonObject();
        int count = COUNT_CODEC.parse(JsonOps.INSTANCE, object.has("count") ? object.get("count") : JsonOps.INSTANCE.createInt(1))
                .result()
                .orElseThrow(() -> new IllegalArgumentException("Ingredient count must be between 1 and " + Item.ABSOLUTE_MAX_STACK_SIZE));

        List<Holder<Item>> holders = vsq$parseIngredientEntries(json);
        if (holders.isEmpty()) {
            throw new IllegalArgumentException("Ingredient resolved to no items");
        }
        return new EnchantingIngredient(Ingredient.of(HolderSet.direct(holders)), count);
    }

    private static List<Holder<Item>> vsq$parseIngredientEntries(JsonElement json) {
        if (!json.isJsonObject()) {
            throw new IllegalArgumentException("Ingredient must be a JSON object or array");
        }

        JsonObject object = json.getAsJsonObject();
        if (object.has("fabric:type")) {
            JsonObject ingredientOnly = object.deepCopy();
            ingredientOnly.remove("count");
            DataResult<Ingredient> result = Ingredient.CODEC.parse(JsonOps.INSTANCE, ingredientOnly);
            return result.result()
                    .map(ingredient -> ingredient.items().toList())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to parse custom fabric ingredient"));
        }

        if (object.has("item")) {
            Identifier itemId = Identifier.tryParse(object.get("item").getAsString());
            if (itemId == null) {
                throw new IllegalArgumentException("Invalid item identifier: " + object.get("item"));
            }

            Item item = BuiltInRegistries.ITEM.getValue(itemId);
            if (item == null) {
                throw new IllegalArgumentException("Unknown item: " + itemId);
            }

            return List.of(BuiltInRegistries.ITEM.wrapAsHolder(item));
        }

        if (object.has("tag")) {
            Identifier tagId = Identifier.tryParse(object.get("tag").getAsString());
            if (tagId == null) {
                throw new IllegalArgumentException("Invalid tag identifier: " + object.get("tag"));
            }

            TagKey<Item> tagKey = TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagId);
            List<Holder<Item>> holders = StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tagKey).spliterator(), false).toList();
            if (holders.isEmpty()) {
                throw new IllegalArgumentException("Unknown or empty item tag: " + tagId);
            }
            return holders;
        }

        throw new IllegalArgumentException("Ingredient object must contain 'item', 'tag', or 'fabric:type'");
    }
}
