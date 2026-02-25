package blob.vanillasquared.util.builder.general;

import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class GeneralArmor {

    private final ItemAttributeModifiers modifiers;

    public GeneralArmor(Identifier identifier, EquipmentSlotGroup slotGroup, double attributeArmor, double attributeArmorToughness, double attributeKnockbackResistance, double attributeMaceProtection) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        if (attributeArmor != 0)
            builder.add(Attributes.ARMOR, new AttributeModifier(identifier, attributeArmor, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        if (attributeArmorToughness != 0)
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(identifier, attributeArmorToughness, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        if (attributeKnockbackResistance != 0)
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(identifier, attributeKnockbackResistance, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        if (attributeMaceProtection != 0)
            builder.add(RegisterAttributes.maceProtection, new AttributeModifier(identifier, attributeMaceProtection, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        this.modifiers = builder.build();
    }

    public ItemAttributeModifiers build() {
        return modifiers;
    }
}
