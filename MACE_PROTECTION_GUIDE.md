# Mace Protection Attribute Guide

This guide explains how to use and apply the `maceProtection` attribute in your mod.

## What is maceProtection?

`maceProtection` is a custom entity attribute that reduces damage from mace attacks. The attribute value ranges from 0.0 to 1.0:

- **0.0** = No protection (full damage from maces)
- **0.5** = 50% damage reduction from maces
- **1.0** = 100% damage reduction (immune to mace damage)

## How It Works

The protection is applied in `LivingEntityMixin.java` by checking if the attacker is wielding a mace, then reducing the damage based on the victim's `maceProtection` attribute value.

---

## Method 1: Add to Armor Pieces (Direct Attribute Modifiers)

You can add maceProtection directly to armor pieces by modifying `ArmorMaterialMixin.java`.

### Example: Add Mace Protection to Armor

```java
// In ArmorMaterialMixin.java
case CHESTPLATE -> {
    ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
    vsq$armorModifier(builder, vsqArmorChestplateOverride, 8.0d, EquipmentSlotGroup.CHEST);
    vsq$armorToughnessModifier(builder, vsqArmorChestplateOverride, 3.0d, EquipmentSlotGroup.CHEST);
    vsq$armorKnockbackResistanceModifier(builder, vsqArmorChestplateOverride, 0.1d, EquipmentSlotGroup.CHEST);
    
    // Add 20% mace protection
    vsq$maceProtectionModifier(builder, vsqArmorChestplateOverride, 0.2d, EquipmentSlotGroup.CHEST);
    
    cir.setReturnValue(builder.build());
}
```

### Suggested Protection Values by Armor Tier

```
Netherite Full Set: 0.8 total (80% protection)
- Helmet:     0.15
- Chestplate: 0.25
- Leggings:   0.20
- Boots:      0.20

Diamond Full Set: 0.6 total (60% protection)
- Helmet:     0.10
- Chestplate: 0.20
- Leggings:   0.15
- Boots:      0.15

Iron Full Set: 0.4 total (40% protection)
- Helmet:     0.05
- Chestplate: 0.15
- Leggings:   0.10
- Boots:      0.10
```

---

## Method 2: Create an Enchantment

To create a "Mace Protection" enchantment, you need to:

### Step 1: Create the Enchantment Effect

Create a new file: `src/main/java/blob/vanillasquared/enchantment/MaceProtectionEnchantmentEffect.java`

```java
package blob.vanillasquared.enchantment;

import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;

public class MaceProtectionEnchantmentEffect {
    
    public static void registerEffect(Enchantment.Builder builder, EquipmentSlotGroup slotGroup) {
        // Each level adds 0.1 (10%) mace protection
        builder.withEffect(
            EnchantmentEffectComponents.ATTRIBUTES,
            new EnchantmentAttributeEffect(
                RegisterAttributes.maceProtection,
                RegisterAttributes.maceProtection,
                AttributeModifier.Operation.ADD_VALUE,
                0.1f  // Base value per level
            )
        );
    }
}
```

### Step 2: Register the Enchantment

You'll need to register this as a custom enchantment in your enchantment registry. The exact implementation depends on how you're registering enchantments in your mod.

Example data pack enchantment JSON (`data/vanillasquared/enchantment/mace_protection.json`):

```json
{
  "description": {
    "translate": "enchantment.vanillasquared.mace_protection"
  },
  "supported_items": "#minecraft:armor",
  "weight": 5,
  "max_level": 4,
  "min_cost": {
    "base": 5,
    "per_level_above_first": 8
  },
  "max_cost": {
    "base": 21,
    "per_level_above_first": 8
  },
  "anvil_cost": 4,
  "slots": [
    "head",
    "chest",
    "legs",
    "feet"
  ],
  "effects": {
    "minecraft:attributes": [
      {
        "id": "vanillasquared:mace_protection",
        "attribute": "vanillasquared:mace_protection",
        "operation": "add_value",
        "amount": {
          "type": "minecraft:linear",
          "base": 0.1,
          "per_level_above_first": 0.1
        }
      }
    ]
  }
}
```

This creates an enchantment where:
- Level I:   10% mace protection
- Level II:  20% mace protection
- Level III: 30% mace protection
- Level IV:  40% mace protection

---

## Method 3: Apply via Commands

You can apply the attribute to any entity using commands:

```
/attribute @p vanillasquared:mace_protection base set 0.5
```

This gives the nearest player 50% mace protection.

### Add Attribute Modifier:

```
/attribute @p vanillasquared:mace_protection modifier add mace_armor 0.3 add_value
```

This adds a modifier named "mace_armor" that adds 0.3 (30%) protection.

---

## Method 4: Apply Programmatically in Code

You can add the attribute to entities in your code:

```java
import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ApplyMaceProtection {
    
    public static void addMaceProtection(LivingEntity entity, double protectionValue) {
        AttributeInstance attribute = entity.getAttribute(RegisterAttributes.maceProtection);
        if (attribute != null) {
            attribute.setBaseValue(protectionValue);
        }
    }
    
    public static void addMaceProtectionModifier(LivingEntity entity, String modifierId, double value) {
        AttributeInstance attribute = entity.getAttribute(RegisterAttributes.maceProtection);
        if (attribute != null) {
            AttributeModifier modifier = new AttributeModifier(
                Identifier.fromNamespaceAndPath("vanillasquared", modifierId),
                value,
                AttributeModifier.Operation.ADD_VALUE
            );
            attribute.addPermanentModifier(modifier);
        }
    }
}
```

---

## Testing the Attribute

### In Creative Mode:

1. Give yourself mace protection: `/attribute @s vanillasquared:mace_protection base set 0.5`
2. Summon a zombie with a mace: `/summon zombie ~ ~ ~ {HandItems:[{id:"minecraft:mace",count:1},{}]}`
3. Let the zombie hit you - you should take 50% less damage

### Check Current Value:

```
/attribute @s vanillasquared:mace_protection get
```

---

## Notes

- Attribute values stack additively (0.2 from helmet + 0.3 from chestplate = 0.5 total)
- Maximum effective value is 1.0 (100% protection)
- The attribute only affects damage from mace attacks specifically
- All living entities have this attribute by default with a base value of 0.0