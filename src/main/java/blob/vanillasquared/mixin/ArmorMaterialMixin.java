package blob.vanillasquared.mixin;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.resources.Identifier;

@Mixin(ArmorMaterial.class)
public class ArmorMaterialMixin {

    @Unique
    private static final Identifier vsqArmorChestplateOverride =
            Identifier.fromNamespaceAndPath("vanillasquared", "armor_chestplate_override");
    @Unique
    private static final Identifier vsqArmorLeggingsOverride =
            Identifier.fromNamespaceAndPath("vanillasquared", "armor_leggings_override");
    @Unique
    private static final Identifier vsqArmorBootsOverride =
            Identifier.fromNamespaceAndPath("vanillasquared", "armor_boots_override");
    @Unique
    private static final Identifier vsqArmorHelmetOverride =
            Identifier.fromNamespaceAndPath("vanillasquared", "armor_helmet_override");

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private void vsq$replaceNetheriteArmor(ArmorType armorType, CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        if (!ArmorMaterials.NETHERITE.equals(this)) {
            return;
        }

        switch (armorType) {
            case CHESTPLATE -> cir.setReturnValue(vsq$armorModifier(vsqArmorChestplateOverride, 37.0d, EquipmentSlotGroup.CHEST));
            case LEGGINGS -> cir.setReturnValue(vsq$armorModifier(vsqArmorLeggingsOverride, 12.0d, EquipmentSlotGroup.LEGS));
            case BOOTS -> cir.setReturnValue(vsq$armorModifier(vsqArmorBootsOverride, 7.0d, EquipmentSlotGroup.FEET));
            case HELMET -> cir.setReturnValue(vsq$armorModifier(vsqArmorHelmetOverride, 3.0d, EquipmentSlotGroup.HEAD));
            default -> {
            }
        }
    }

    @Unique
    private static ItemAttributeModifiers vsq$armorModifier(Identifier id, double value, EquipmentSlotGroup slotGroup) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE), slotGroup)
                .build();
    }
}
