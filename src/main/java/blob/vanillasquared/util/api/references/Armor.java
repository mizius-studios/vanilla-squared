package blob.vanillasquared.util.api.references;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterial;

public enum Armor {
    LEATHER_HELMET(ArmorMaterials.LEATHER, ArmorType.HELMET),
    LEATHER_CHESTPLATE(ArmorMaterials.LEATHER, ArmorType.CHESTPLATE),
    LEATHER_LEGGINGS(ArmorMaterials.LEATHER, ArmorType.LEGGINGS),
    LEATHER_BOOTS(ArmorMaterials.LEATHER, ArmorType.BOOTS),

    CHAINMAIL_HELMET(ArmorMaterials.CHAINMAIL, ArmorType.HELMET),
    CHAINMAIL_CHESTPLATE(ArmorMaterials.CHAINMAIL, ArmorType.CHESTPLATE),
    CHAINMAIL_LEGGINGS(ArmorMaterials.CHAINMAIL, ArmorType.LEGGINGS),
    CHAINMAIL_BOOTS(ArmorMaterials.CHAINMAIL, ArmorType.BOOTS),

    IRON_HELMET(ArmorMaterials.IRON, ArmorType.HELMET),
    IRON_CHESTPLATE(ArmorMaterials.IRON, ArmorType.CHESTPLATE),
    IRON_LEGGINGS(ArmorMaterials.IRON, ArmorType.LEGGINGS),
    IRON_BOOTS(ArmorMaterials.IRON, ArmorType.BOOTS),

    GOLD_HELMET(ArmorMaterials.GOLD, ArmorType.HELMET),
    GOLD_CHESTPLATE(ArmorMaterials.GOLD, ArmorType.CHESTPLATE),
    GOLD_LEGGINGS(ArmorMaterials.GOLD, ArmorType.LEGGINGS),
    GOLD_BOOTS(ArmorMaterials.GOLD, ArmorType.BOOTS),

    DIAMOND_HELMET(ArmorMaterials.DIAMOND, ArmorType.HELMET),
    DIAMOND_CHESTPLATE(ArmorMaterials.DIAMOND, ArmorType.CHESTPLATE),
    DIAMOND_LEGGINGS(ArmorMaterials.DIAMOND, ArmorType.LEGGINGS),
    DIAMOND_BOOTS(ArmorMaterials.DIAMOND, ArmorType.BOOTS),

    NETHERITE_HELMET(ArmorMaterials.NETHERITE, ArmorType.HELMET),
    NETHERITE_CHESTPLATE(ArmorMaterials.NETHERITE, ArmorType.CHESTPLATE),
    NETHERITE_LEGGINGS(ArmorMaterials.NETHERITE, ArmorType.LEGGINGS),
    NETHERITE_BOOTS(ArmorMaterials.NETHERITE, ArmorType.BOOTS),

    COPPER_HELMET(ArmorMaterials.COPPER, ArmorType.HELMET),
    COPPER_CHESTPLATE(ArmorMaterials.COPPER, ArmorType.CHESTPLATE),
    COPPER_LEGGINGS(ArmorMaterials.COPPER, ArmorType.LEGGINGS),
    COPPER_BOOTS(ArmorMaterials.COPPER, ArmorType.BOOTS),

    TURTLE_HELMET(ArmorMaterials.TURTLE_SCUTE, ArmorType.HELMET);

    private static final Map<ArmorMaterial, Map<ArmorType, Armor>> BY_MATERIAL_AND_TYPE = createLookup();

    private final ArmorMaterial material;
    private final ArmorType type;

    Armor(ArmorMaterial material, ArmorType type) {
        this.material = material;
        this.type = type;
    }

    public ArmorMaterial material() {
        return material;
    }

    public ArmorType type() {
        return type;
    }

    public static Optional<Armor> find(ArmorMaterial material, ArmorType type) {
        Map<ArmorType, Armor> byType = BY_MATERIAL_AND_TYPE.get(material);
        if (byType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byType.get(type));
    }

    public static Armor of(ArmorMaterial material, ArmorType type) {
        return find(material, type)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No Armor constant for material " + material + " and type " + type));
    }

    private static Map<ArmorMaterial, Map<ArmorType, Armor>> createLookup() {
        Map<ArmorMaterial, Map<ArmorType, Armor>> lookup = new HashMap<>();
        for (Armor armor : values()) {
            lookup.computeIfAbsent(armor.material, ignored -> new HashMap<>())
                    .put(armor.type, armor);
        }
        return lookup;
    }

}
