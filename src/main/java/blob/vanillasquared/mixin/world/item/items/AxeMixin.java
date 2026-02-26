package blob.vanillasquared.mixin.world.item.items;

import blob.vanillasquared.util.builder.components.BlockBuilder;
import blob.vanillasquared.util.builder.general.GeneralWeapon;
import blob.vanillasquared.util.api.other.vsqIdentifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Weapon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Item.Properties.class)
public abstract class AxeMixin {

    @Shadow
    public abstract Item.Properties tool(ToolMaterial material, net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag, float f, float g, float d);

    @Unique
    private static final Map<ToolMaterial, GeneralWeapon> AXE_WEAPON = Map.of(
            ToolMaterial.WOOD, new GeneralWeapon(vsqIdentifiers.vsqAxeOverride.identifier(), EquipmentSlotGroup.MAINHAND,5.0d, -3.2d, -0.5d),
            ToolMaterial.STONE, new GeneralWeapon(vsqIdentifiers.vsqAxeOverride.identifier(), EquipmentSlotGroup.MAINHAND,6.0d, -3.3d, -0.5d),
            ToolMaterial.COPPER, new GeneralWeapon(vsqIdentifiers.vsqAxeOverride.identifier(), EquipmentSlotGroup.MAINHAND,6.0d, -3.0d, -0.5d),
            ToolMaterial.IRON, new GeneralWeapon(vsqIdentifiers.vsqAxeOverride.identifier(), EquipmentSlotGroup.MAINHAND,8.0d, -3.0d, -0.5d),
            ToolMaterial.GOLD, new GeneralWeapon(vsqIdentifiers.vsqAxeOverride.identifier(), EquipmentSlotGroup.MAINHAND,6.0d, -2.9d, 0.0d),
            ToolMaterial.DIAMOND, new GeneralWeapon(vsqIdentifiers.vsqAxeOverride.identifier(), EquipmentSlotGroup.MAINHAND,10.0d, -3.0d, -0.5d),
            ToolMaterial.NETHERITE, new GeneralWeapon(vsqIdentifiers.vsqAxeOverride.identifier(), EquipmentSlotGroup.MAINHAND,11.0d, -3.0d, -0.5d)
    );

    @Unique
    private static final Map<ToolMaterial, BlockBuilder> BLOCK_COMPONENT = Map.of(
            ToolMaterial.WOOD, new BlockBuilder(0.25F, 0.25F, 0.2F, 2.0F),
            ToolMaterial.STONE, new BlockBuilder(0.4F, 0.3F, 0.2F, 1.0F),
            ToolMaterial.COPPER, new BlockBuilder(0.5F, 0.25F, 0.25F, 2.0F),
            ToolMaterial.IRON, new BlockBuilder(0.6F, 0.4F, 0.4F, 2.0F),
            ToolMaterial.GOLD, new BlockBuilder(0F, 0.0F, 1.0F, 0.5F),
            ToolMaterial.DIAMOND, new BlockBuilder(0.65F, 0.5F, 0.5F, 1.0F),
            ToolMaterial.NETHERITE, new BlockBuilder(0.7F, 0.55F, 0.55F, 1.0F)
    );

    @Inject(at = @At("HEAD"), method = "axe", cancellable = true)
    public void init(ToolMaterial toolMaterial, float f, float g, CallbackInfoReturnable<Item.Properties> cir) {
        BlockBuilder blockComponent = BLOCK_COMPONENT.get(toolMaterial);
        GeneralWeapon generalWeapon = AXE_WEAPON.get(toolMaterial);
        if (blockComponent == null || generalWeapon == null) {
            return;
        }

        cir.setReturnValue(this.tool(toolMaterial, BlockTags.MINEABLE_WITH_AXE, f, g, 5.0F).component(DataComponents.BLOCKS_ATTACKS, blockComponent.build()).component(DataComponents.WEAPON, new Weapon(1, 5.0F)).attributes(generalWeapon.build()));
    }
}
