package blob.vanillasquared.main.world.recipe.enchanting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public record EnchantingComponentModifier(Map<Identifier, JsonElement> componentModifier) {
    private static final Codec<Map<Identifier, JsonElement>> COMPONENT_MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, ExtraCodecs.JSON);

    public static final Codec<EnchantingComponentModifier> CODEC = COMPONENT_MAP_CODEC.xmap(
            EnchantingComponentModifier::new,
            EnchantingComponentModifier::componentModifier
    ).flatXmap(
            EnchantingComponentModifier::vsq$validate,
            DataResult::success
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingComponentModifier> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EnchantingComponentModifier decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Map<Identifier, JsonElement> components = new LinkedHashMap<>(size);
            for (int index = 0; index < size; index++) {
                Identifier componentId = Identifier.STREAM_CODEC.decode(buf);
                JsonElement json = JsonParser.parseString(buf.readUtf());
                components.put(componentId, json);
            }
            return new EnchantingComponentModifier(components);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, EnchantingComponentModifier value) {
            buf.writeVarInt(value.componentModifier.size());
            value.componentModifier.forEach((componentId, json) -> {
                Identifier.STREAM_CODEC.encode(buf, componentId);
                buf.writeUtf(json.toString());
            });
        }
    };

    public EnchantingComponentModifier {
        componentModifier = Map.copyOf(componentModifier);
    }

    public ItemStack apply(ItemStack originalStack, HolderLookup.Provider registries) {
        ItemStack result = originalStack.copy();
        RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);

        for (Map.Entry<Identifier, JsonElement> entry : this.componentModifier.entrySet()) {
            vsq$applyComponent(result, ops, entry.getKey(), entry.getValue());
        }

        return result;
    }

    public boolean modifies(ItemStack originalStack, HolderLookup.Provider registries) {
        return !ItemStack.matches(originalStack, this.apply(originalStack, registries));
    }

    private static DataResult<EnchantingComponentModifier> vsq$validate(EnchantingComponentModifier modifier) {
        for (Map.Entry<Identifier, JsonElement> entry : modifier.componentModifier.entrySet()) {
            DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(entry.getKey());
            if (componentType == null) {
                return DataResult.error(() -> "Unknown data component: " + entry.getKey());
            }

            try {
                componentType.codecOrThrow();
            } catch (IllegalStateException exception) {
                return DataResult.error(() -> "Data component does not support persistent JSON: " + entry.getKey());
            }
        }

        return DataResult.success(modifier);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void vsq$applyComponent(ItemStack stack, RegistryOps<JsonElement> ops, Identifier componentId, JsonElement modifierJson) {
        DataComponentType componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentId);
        if (componentType == null) {
            throw new IllegalArgumentException("Unknown data component: " + componentId);
        }

        Codec componentCodec = componentType.codecOrThrow();
        Object currentValue = stack.get(componentType);
        JsonElement mergedJson = currentValue == null
                ? modifierJson.deepCopy()
                : vsq$mergeJson(
                        vsq$encodeComponent(componentCodec, ops, currentValue, componentId),
                        modifierJson
                );

        var decodedValue = componentCodec.parse(ops, mergedJson).result();
        Object mergedValue = decodedValue.isPresent()
                ? decodedValue.get()
                : vsq$throwInvalidComponent("Failed to decode component " + componentId + " from " + mergedJson);
        stack.set(componentType, mergedValue);
    }

    @SuppressWarnings("rawtypes")
    private static JsonElement vsq$encodeComponent(Codec componentCodec, RegistryOps<JsonElement> ops, Object currentValue, Identifier componentId) {
        var encodedValue = componentCodec.encodeStart(ops, currentValue).result();
        if (encodedValue.isEmpty()) {
            return vsq$throwInvalidComponent("Failed to encode component " + componentId);
        }

        Object encoded = encodedValue.get();
        if (encoded instanceof JsonElement jsonElement) {
            return jsonElement;
        }
        return vsq$throwInvalidComponent("Encoded component " + componentId + " was not a JsonElement");
    }

    private static <T> T vsq$throwInvalidComponent(String message) {
        throw new IllegalArgumentException(message);
    }

    private static JsonElement vsq$mergeJson(JsonElement base, JsonElement modifier) {
        if (base == null || base.isJsonNull()) {
            return modifier.deepCopy();
        }
        if (modifier == null || modifier.isJsonNull()) {
            return modifier == null ? null : modifier.deepCopy();
        }
        if (base.isJsonObject() && modifier.isJsonObject()) {
            JsonObject merged = base.getAsJsonObject().deepCopy();
            for (Map.Entry<String, JsonElement> entry : modifier.getAsJsonObject().entrySet()) {
                JsonElement existing = merged.get(entry.getKey());
                merged.add(entry.getKey(), existing == null ? entry.getValue().deepCopy() : vsq$mergeJson(existing, entry.getValue()));
            }
            return merged;
        }
        if (base.isJsonArray() && modifier.isJsonArray()) {
            JsonArray merged = base.getAsJsonArray().deepCopy();
            for (JsonElement element : modifier.getAsJsonArray()) {
                merged.add(element.deepCopy());
            }
            return merged;
        }
        return modifier.deepCopy();
    }
}
