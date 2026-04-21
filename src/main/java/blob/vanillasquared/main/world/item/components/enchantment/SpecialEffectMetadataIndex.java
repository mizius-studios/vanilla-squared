package blob.vanillasquared.main.world.item.components.enchantment;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record SpecialEffectMetadataIndex(Map<String, List<SpecialEffectMetadata>> byComponent) {
    public static final SpecialEffectMetadataIndex EMPTY = new SpecialEffectMetadataIndex(Map.of());
    public static final Codec<SpecialEffectMetadataIndex> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("_noop", Map.of()).forGetter(index -> Map.of())
    ).apply(instance, ignored -> EMPTY));

    public Optional<SpecialEffectMetadata> metadata(String componentKey, int index) {
        List<SpecialEffectMetadata> entries = this.byComponent.get(componentKey);
        if (entries == null || index < 0 || index >= entries.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(entries.get(index));
    }

    public List<SpecialEffectMetadata> all() {
        List<SpecialEffectMetadata> combined = new ArrayList<>();
        this.byComponent.values().forEach(combined::addAll);
        return List.copyOf(combined);
    }

    public static SpecialEffectMetadataIndex fromDynamic(Optional<Dynamic<?>> dynamic) {
        if (dynamic.isEmpty()) {
            return EMPTY;
        }

        JsonElement effectsJson = dynamic.get().convert(JsonOps.INSTANCE).getValue();
        if (!(effectsJson instanceof JsonObject effectsObject)) {
            return EMPTY;
        }

        ImmutableMap.Builder<String, List<SpecialEffectMetadata>> builder = ImmutableMap.builder();
        for (Map.Entry<String, JsonElement> entry : effectsObject.entrySet()) {
            String componentKey = entry.getKey().trim();
            if (componentKey.isEmpty()) {
                continue;
            }
            List<SpecialEffectMetadata> list = parseEntries(entry.getValue());
            if (!list.isEmpty()) {
                builder.put(componentKey, List.copyOf(list));
            }
        }
        return new SpecialEffectMetadataIndex(builder.build());
    }

    private static List<SpecialEffectMetadata> parseEntries(JsonElement arrayElement) {
        if (!(arrayElement instanceof JsonArray entries)) {
            return List.of();
        }

        List<SpecialEffectMetadata> parsed = new ArrayList<>(entries.size());
        for (JsonElement entryElement : entries) {
            if (!(entryElement instanceof JsonObject entry)) {
                continue;
            }

            String id = entry.has("id") ? entry.get("id").getAsString().trim() : "";
            if (id.isEmpty()) {
                continue;
            }
            Optional<SpecialEffectSettings> special = parseSpecial(entry);
            parsed.add(new SpecialEffectMetadata(id, special));
        }
        return parsed.isEmpty() ? List.of() : List.copyOf(parsed);
    }

    private static Optional<SpecialEffectSettings> parseSpecial(JsonObject entry) {
        if (!entry.has("special")) {
            return Optional.empty();
        }

        JsonElement json = entry.get("special");
        if (!(json instanceof JsonObject object)) {
            return Optional.empty();
        }
        return SpecialEffectSettings.CODEC.parse(JsonOps.INSTANCE, object).result();
    }
}
