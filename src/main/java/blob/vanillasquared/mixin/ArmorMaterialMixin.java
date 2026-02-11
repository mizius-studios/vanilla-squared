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
    private void vsq$replaceNetheriteChestplateArmor(ArmorType armorType, CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        if (!ArmorMaterials.NETHERITE.equals(this) || armorType != ArmorType.CHESTPLATE) {
            return;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(
                Attributes.ARMOR,
                new AttributeModifier(vsqArmorChestplateOverride, 1.0d, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.CHEST
        );
        cir.setReturnValue(builder.build());
    }

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private void vsq$replaceNetheriteLeggingsArmor(ArmorType armorType, CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        if (!ArmorMaterials.NETHERITE.equals(this) || armorType != ArmorType.LEGGINGS) {
            return;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(
                Attributes.ARMOR,
                new AttributeModifier(vsqArmorLeggingsOverride, 1.0d, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.LEGS
        );
        cir.setReturnValue(builder.build());
    }

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private void vsq$replaceNetheriteBootsArmor(ArmorType armorType, CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        if (!ArmorMaterials.NETHERITE.equals(this) || armorType != ArmorType.BOOTS) {
            return;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(
                Attributes.ARMOR,
                new AttributeModifier(vsqArmorBootsOverride, 1.0d, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.FEET
        );
        cir.setReturnValue(builder.build());
    }

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private void vsq$replaceNetheriteHelmetArmor(ArmorType armorType, CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        if (!ArmorMaterials.NETHERITE.equals(this) || armorType != ArmorType.HELMET) {
            return;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(
                Attributes.ARMOR,
                new AttributeModifier(vsqArmorHelmetOverride, 1.0d, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.HEAD
        );
        cir.setReturnValue(builder.build());
    }
}
