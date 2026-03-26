package blob.vanillasquared.main.world.recipe.enchanting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
import java.util.List;
import java.util.Map;

public record EnchantingIncompatibleComponents(Map<Identifier, JsonElement> componentPatterns) {
    private static final Codec<Map<Identifier, JsonElement>> COMPONENT_MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, ExtraCodecs.JSON);

    public static final EnchantingIncompatibleComponents EMPTY = new EnchantingIncompatibleComponents(Map.of());

    public static final Codec<EnchantingIncompatibleComponents> CODEC = COMPONENT_MAP_CODEC.xmap(
            EnchantingIncompatibleComponents::new,
            EnchantingIncompatibleComponents::componentPatterns
    ).flatXmap(
            EnchantingIncompatibleComponents::vsq$validate,
            DataResult::success
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingIncompatibleComponents> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EnchantingIncompatibleComponents decode(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Map<Identifier, JsonElement> components = new LinkedHashMap<>(size);
            for (int index = 0; index < size; index++) {
                Identifier componentId = Identifier.STREAM_CODEC.decode(buf);
                JsonElement json = JsonParser.parseString(buf.readUtf());
                components.put(componentId, json);
            }
            return new EnchantingIncompatibleComponents(components);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, EnchantingIncompatibleComponents value) {
            buf.writeVarInt(value.componentPatterns.size());
            value.componentPatterns.forEach((componentId, json) -> {
                Identifier.STREAM_CODEC.encode(buf, componentId);
                buf.writeUtf(json.toString());
            });
        }
    };

    public static final Codec<List<EnchantingIncompatibleComponents>> LIST_CODEC = Codec.list(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, List<EnchantingIncompatibleComponents>> LIST_STREAM_CODEC = STREAM_CODEC.apply(net.minecraft.network.codec.ByteBufCodecs.list());

    public EnchantingIncompatibleComponents {
        componentPatterns = Map.copyOf(componentPatterns);
    }

    public boolean matches(ItemStack stack, HolderLookup.Provider registries) {
        RegistryOps<JsonElement> ops = registries.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);

        for (Map.Entry<Identifier, JsonElement> entry : this.componentPatterns.entrySet()) {
            if (!vsq$matchesComponent(stack, ops, entry.getKey(), entry.getValue())) {
                return false;
            }
        }

        return !this.componentPatterns.isEmpty();
    }

    private static DataResult<EnchantingIncompatibleComponents> vsq$validate(EnchantingIncompatibleComponents incompatible) {
        for (Map.Entry<Identifier, JsonElement> entry : incompatible.componentPatterns.entrySet()) {
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

        return DataResult.success(incompatible);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean vsq$matchesComponent(ItemStack stack, RegistryOps<JsonElement> ops, Identifier componentId, JsonElement patternJson) {
        DataComponentType componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(componentId);
        if (componentType == null) {
            throw new IllegalArgumentException("Unknown data component: " + componentId);
        }

        Object currentValue = stack.get(componentType);
        if (currentValue == null) {
            return false;
        }

        Codec componentCodec = componentType.codecOrThrow();
        JsonElement currentJson = vsq$encodeComponent(componentCodec, ops, currentValue, componentId);
        return vsq$matchesJson(currentJson, patternJson);
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

    private static boolean vsq$matchesJson(JsonElement current, JsonElement pattern) {
        if (pattern == null || pattern.isJsonNull()) {
            return current == null || current.isJsonNull();
        }
        if (current == null || current.isJsonNull()) {
            return false;
        }
        if (pattern.isJsonObject()) {
            if (!current.isJsonObject()) {
                return false;
            }

            JsonObject currentObject = current.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : pattern.getAsJsonObject().entrySet()) {
                JsonElement currentChild = currentObject.get(entry.getKey());
                if (currentChild == null || !vsq$matchesJson(currentChild, entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        if (pattern.isJsonArray()) {
            if (!current.isJsonArray()) {
                return false;
            }

            JsonArray currentArray = current.getAsJsonArray();
            JsonArray patternArray = pattern.getAsJsonArray();
            if (currentArray.size() != patternArray.size()) {
                return false;
            }

            for (int index = 0; index < patternArray.size(); index++) {
                if (!vsq$matchesJson(currentArray.get(index), patternArray.get(index))) {
                    return false;
                }
            }
            return true;
        }

        return current.equals(pattern);
    }

    private static <T> T vsq$throwInvalidComponent(String message) {
        throw new IllegalArgumentException(message);
    }
}
