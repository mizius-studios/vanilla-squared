package blob.vanillasquared.mixin;

import blob.vanillasquared.util.data.GeneralWeapon;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Debug(export = true)
@Mixin(ToolMaterial.class)
public class ToolMaterialMixin {

    @Unique
    private static final Map<ToolMaterial, GeneralWeapon> SWORD = Map.of(
            ToolMaterial.WOOD, new GeneralWeapon(4.0d, -2.4d, 0.0d, 75),
            ToolMaterial.STONE, new GeneralWeapon(5.0d, -2.4d, 0.0d, 150),
            ToolMaterial.COPPER, new GeneralWeapon(5.0d, -2.4d, 0.0d, 200),
            ToolMaterial.IRON, new GeneralWeapon(6.0d, -2.4d, 0.0d, 250),
            ToolMaterial.GOLD, new GeneralWeapon(4.0d, -2.4d, 0.0d, 100),
            ToolMaterial.DIAMOND, new GeneralWeapon(7.0d, -2.4d, 0.0d, 1550),
            ToolMaterial.NETHERITE, new GeneralWeapon(8.0d, -2.4d, 0.0d, 2069)
    );

    @Unique
    private static final Identifier ATTRIBUTE_IDENTIFIER_REACH = Identifier.fromNamespaceAndPath("vanillasquared", "sword_reach");

    @Inject(method = "createSwordAttributes", at = @At("HEAD"), cancellable = true)
    private void createSwordAttributes(float f, float g, CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        ToolMaterial material = (ToolMaterial) (Object) this;
        GeneralWeapon weapon = SWORD.getOrDefault(material, GeneralWeapon.DEFAULT);

        double attackDamage = weapon.attackDamage();
        double attackSpeed = weapon.attackSpeed();
        double attackReach = weapon.entityReach();

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);

        if (attackReach != 0.0d) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ATTRIBUTE_IDENTIFIER_REACH, attackReach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }

        cir.setReturnValue(builder.build());
    }
}
