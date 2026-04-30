package blob.vanillasquared.mixin.world.item.items;

import blob.vanillasquared.util.api.builder.components.BlockAttacksComponentBuilder;
import blob.vanillasquared.util.api.builder.general.WeaponAttributeBuilder;
import blob.vanillasquared.util.api.combat.VSQCombatPresets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.Properties.class)
public abstract class AxeMixin {

    @Shadow
    public abstract Item.Properties tool(ToolMaterial material, TagKey<Block> tag, float attackDamage, float attackSpeed, float weaponDamage);

    @Inject(at = @At("HEAD"), method = "axe", cancellable = true)
    public void init(ToolMaterial toolMaterial, float attackDamage, float attackSpeed, CallbackInfoReturnable<Item.Properties> cir) {
        BlockAttacksComponentBuilder blockComponent = VSQCombatPresets.axeBlockComponent(toolMaterial);
        WeaponAttributeBuilder generalWeapon = VSQCombatPresets.axeAttributes(toolMaterial);
        if (blockComponent == null || generalWeapon == null) {
            return;
        }

        cir.setReturnValue(
                this.tool(toolMaterial, BlockTags.MINEABLE_WITH_AXE, attackDamage, attackSpeed, 5.0F)
                        .component(DataComponents.BLOCKS_ATTACKS, blockComponent.build())
                        .component(DataComponents.WEAPON, new Weapon(1, 5.0F))
                        .attributes(generalWeapon.build())
        );
    }
}
