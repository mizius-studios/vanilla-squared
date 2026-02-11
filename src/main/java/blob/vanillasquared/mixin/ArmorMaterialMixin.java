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
    private static final Identifier vsqArmorOverride =
            Identifier.fromNamespaceAndPath("vanillasquared", "armor");

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private void vsq$replaceNetheriteChestplateArmor(ArmorType armorType, CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        if ((Object) this != ArmorMaterials.NETHERITE || armorType != ArmorType.CHESTPLATE) {
            return;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        builder.add(
                Attributes.ARMOR,
                new AttributeModifier(vsqArmorOverride, 1.0d, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.CHEST
        );
        cir.setReturnValue(builder.build());
    }
}