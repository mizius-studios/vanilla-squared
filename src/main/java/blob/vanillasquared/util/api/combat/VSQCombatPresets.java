package blob.vanillasquared.util.api.combat;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.util.api.builder.components.BlockAttacksComponentBuilder;
import blob.vanillasquared.util.api.builder.components.HitThroughComponentBuilder;
import blob.vanillasquared.util.api.builder.durability.Durability;
import blob.vanillasquared.util.api.builder.general.ArmorAttributeBuilder;
import blob.vanillasquared.util.api.builder.general.WeaponAttributeBuilder;
import blob.vanillasquared.util.api.references.armor.Armor;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ToolMaterial;

import java.util.Map;

public final class VSQCombatPresets {
    private static final Map<ToolMaterial, WeaponAttributeBuilder> SWORD_ATTRIBUTES = Map.of(
            ToolMaterial.WOOD, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.SWORD, EquipmentSlotGroup.MAINHAND, 4.0D, -2.4D, 0.0D),
            ToolMaterial.STONE, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.SWORD, EquipmentSlotGroup.MAINHAND, 5.0D, -2.4D, 0.0D),
            ToolMaterial.COPPER, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.SWORD, EquipmentSlotGroup.MAINHAND, 5.0D, -2.4D, 0.0D),
            ToolMaterial.IRON, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.SWORD, EquipmentSlotGroup.MAINHAND, 6.0D, -2.4D, 0.0D),
            ToolMaterial.GOLD, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.SWORD, EquipmentSlotGroup.MAINHAND, 6.0D, -2.4D, 0.0D),
            ToolMaterial.DIAMOND, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.SWORD, EquipmentSlotGroup.MAINHAND, 7.0D, -2.4D, 0.0D),
            ToolMaterial.NETHERITE, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.SWORD, EquipmentSlotGroup.MAINHAND, 10.0D, -2.4D, 0.0D)
    );

    private static final Map<ToolMaterial, WeaponAttributeBuilder> AXE_ATTRIBUTES = Map.of(
            ToolMaterial.WOOD, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.AXE, EquipmentSlotGroup.MAINHAND, 6.0D, -3.2D, -0.5D),
            ToolMaterial.STONE, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.AXE, EquipmentSlotGroup.MAINHAND, 7.0D, -3.3D, -0.5D),
            ToolMaterial.COPPER, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.AXE, EquipmentSlotGroup.MAINHAND, 7.0D, -3.0D, -0.5D),
            ToolMaterial.IRON, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.AXE, EquipmentSlotGroup.MAINHAND, 8.0D, -3.0D, -0.5D),
            ToolMaterial.GOLD, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.AXE, EquipmentSlotGroup.MAINHAND, 7.0D, -2.9D, 0.0D),
            ToolMaterial.DIAMOND, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.AXE, EquipmentSlotGroup.MAINHAND, 11.0D, -3.0D, -0.5D),
            ToolMaterial.NETHERITE, new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.AXE, EquipmentSlotGroup.MAINHAND, 12.0D, -3.0D, -0.5D)
    );

    private static final Map<ToolMaterial, BlockAttacksComponentBuilder> AXE_BLOCKS = Map.of(
            ToolMaterial.WOOD, new BlockAttacksComponentBuilder(0.25F, 0.25F, 0.2F, 2.0F),
            ToolMaterial.STONE, new BlockAttacksComponentBuilder(0.4F, 0.3F, 0.2F, 1.0F),
            ToolMaterial.COPPER, new BlockAttacksComponentBuilder(0.5F, 0.25F, 0.25F, 2.0F),
            ToolMaterial.IRON, new BlockAttacksComponentBuilder(0.6F, 0.4F, 0.4F, 2.0F),
            ToolMaterial.GOLD, new BlockAttacksComponentBuilder(0F, 0.0F, 1.0F, 0.5F),
            ToolMaterial.DIAMOND, new BlockAttacksComponentBuilder(0.65F, 0.5F, 0.5F, 1.0F),
            ToolMaterial.NETHERITE, new BlockAttacksComponentBuilder(0.7F, 0.55F, 0.55F, 1.0F)
    );

    private static final Map<ToolMaterial, Durability> TOOL_DURABILITY = Map.of(
            ToolMaterial.WOOD, new Durability(75),
            ToolMaterial.STONE, new Durability(150),
            ToolMaterial.COPPER, new Durability(200),
            ToolMaterial.IRON, new Durability(250),
            ToolMaterial.GOLD, new Durability(100),
            ToolMaterial.DIAMOND, new Durability(1550),
            ToolMaterial.NETHERITE, new Durability(2069)
    );

    private static final Map<Armor, ArmorAttributeBuilder> ARMOR_ATTRIBUTES = Map.ofEntries(
            Map.entry(Armor.LEATHER_HELMET, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.HELMET, EquipmentSlotGroup.HEAD, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.LEATHER_CHESTPLATE, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.CHESTPLATE, EquipmentSlotGroup.CHEST, 3.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.LEATHER_LEGGINGS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.LEGGINGS, EquipmentSlotGroup.LEGS, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.LEATHER_BOOTS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.BOOTS, EquipmentSlotGroup.FEET, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.COPPER_HELMET, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.HELMET, EquipmentSlotGroup.HEAD, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.COPPER_CHESTPLATE, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.CHESTPLATE, EquipmentSlotGroup.CHEST, 4.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.COPPER_LEGGINGS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.LEGGINGS, EquipmentSlotGroup.LEGS, 3.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.COPPER_BOOTS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.BOOTS, EquipmentSlotGroup.FEET, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.CHAINMAIL_HELMET, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.HELMET, EquipmentSlotGroup.HEAD, 3.0D, 0.0D, 0.0D, 0.2D, 0.0D, 0.2D, 0.2D)),
            Map.entry(Armor.CHAINMAIL_CHESTPLATE, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.CHESTPLATE, EquipmentSlotGroup.CHEST, 5.0D, 0.0D, 0.0D, 0.2D, 0.0D, 0.2D, 0.2D)),
            Map.entry(Armor.CHAINMAIL_LEGGINGS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.LEGGINGS, EquipmentSlotGroup.LEGS, 4.0D, 0.0D, 0.0D, 0.2D, 0.0D, 0.2D, 0.2D)),
            Map.entry(Armor.CHAINMAIL_BOOTS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.BOOTS, EquipmentSlotGroup.FEET, 1.0D, 0.0D, 0.0D, 0.2D, 0.0D, 0.2D, 0.2D)),
            Map.entry(Armor.IRON_HELMET, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.HELMET, EquipmentSlotGroup.HEAD, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.IRON_CHESTPLATE, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.CHESTPLATE, EquipmentSlotGroup.CHEST, 6.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.IRON_LEGGINGS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.LEGGINGS, EquipmentSlotGroup.LEGS, 5.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.IRON_BOOTS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.BOOTS, EquipmentSlotGroup.FEET, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.GOLD_HELMET, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.HELMET, EquipmentSlotGroup.HEAD, 2.0D, 0.0D, 0.0D, 0.0D, 0.2D, 0.0D, 0.0D)),
            Map.entry(Armor.GOLD_CHESTPLATE, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.CHESTPLATE, EquipmentSlotGroup.CHEST, 6.0D, 0.0D, 0.0D, 0.0D, 0.2D, 0.0D, 0.0D)),
            Map.entry(Armor.GOLD_LEGGINGS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.LEGGINGS, EquipmentSlotGroup.LEGS, 5.0D, 0.0D, 0.0D, 0.0D, 0.2D, 0.0D, 0.0D)),
            Map.entry(Armor.GOLD_BOOTS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.BOOTS, EquipmentSlotGroup.FEET, 1.0D, 0.0D, 0.0D, 0.0D, 0.2D, 0.0D, 0.0D)),
            Map.entry(Armor.DIAMOND_HELMET, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.HELMET, EquipmentSlotGroup.HEAD, 4.0D, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.DIAMOND_CHESTPLATE, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.CHESTPLATE, EquipmentSlotGroup.CHEST, 7.0D, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.DIAMOND_LEGGINGS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.LEGGINGS, EquipmentSlotGroup.LEGS, 6.0D, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.DIAMOND_BOOTS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.BOOTS, EquipmentSlotGroup.FEET, 3.0D, 2.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.NETHERITE_HELMET, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.HELMET, EquipmentSlotGroup.HEAD, 5.0D, 3.0D, 0.1D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.NETHERITE_CHESTPLATE, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.CHESTPLATE, EquipmentSlotGroup.CHEST, 8.0D, 3.0D, 0.1D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.NETHERITE_LEGGINGS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.LEGGINGS, EquipmentSlotGroup.LEGS, 7.0D, 3.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.NETHERITE_BOOTS, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.BOOTS, EquipmentSlotGroup.FEET, 4.0D, 3.0D, 0.1D, 0.0D, 0.0D, 0.0D, 0.0D)),
            Map.entry(Armor.TURTLE_HELMET, new ArmorAttributeBuilder(ArmorAttributeBuilder.ModifierIds.HELMET, EquipmentSlotGroup.HEAD, 4.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D))
    );

    private static final WeaponAttributeBuilder TRIDENT_ATTRIBUTES =
            new WeaponAttributeBuilder(WeaponAttributeBuilder.ModifierIds.TRIDENT, EquipmentSlotGroup.MAINHAND, 10.0D, -2.875D, 0.5D);

    private static final HitThroughComponentBuilder HIT_THROUGH_PLANTS =
            new HitThroughComponentBuilder(VanillaSquared.MOD_ID, "hit_through");

    private VSQCombatPresets() {
    }

    public static WeaponAttributeBuilder swordAttributes(ToolMaterial material) {
        return SWORD_ATTRIBUTES.get(material);
    }

    public static WeaponAttributeBuilder axeAttributes(ToolMaterial material) {
        return AXE_ATTRIBUTES.get(material);
    }

    public static BlockAttacksComponentBuilder axeBlockComponent(ToolMaterial material) {
        return AXE_BLOCKS.get(material);
    }

    public static Durability toolDurability(ToolMaterial material) {
        return TOOL_DURABILITY.getOrDefault(material, Durability.DEFAULT);
    }

    public static ArmorAttributeBuilder armorAttributes(Armor armor) {
        return ARMOR_ATTRIBUTES.get(armor);
    }

    public static WeaponAttributeBuilder tridentAttributes() {
        return TRIDENT_ATTRIBUTES;
    }

    public static HitThroughComponentBuilder hitThroughPlants() {
        return HIT_THROUGH_PLANTS;
    }
}
