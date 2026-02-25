package blob.vanillasquared.util.builder.general;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class GeneralWeapon {
    private final ItemAttributeModifiers modifiers;

    public GeneralWeapon(Identifier identifier, EquipmentSlotGroup slotGroup, double attackDMG, double attackSpeed, double attackRange) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        if (attackDMG != 0)
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(identifier, attackDMG, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        if (attackSpeed != 0)
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(identifier, attackSpeed, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        if (attackRange != 0)
            builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(identifier, attackRange, AttributeModifier.Operation.ADD_VALUE), slotGroup);
        this.modifiers = builder.build();
    }

    public ItemAttributeModifiers build() {
        return modifiers;
    }
}
