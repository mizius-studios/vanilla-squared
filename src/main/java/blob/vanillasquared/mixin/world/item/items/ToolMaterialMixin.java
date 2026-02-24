package blob.vanillasquared.mixin.world.item.items;

import blob.vanillasquared.util.data.GeneralWeapon;
import blob.vanillasquared.util.data.Dura;
import blob.vanillasquared.util.api.other.vsqIdentifiers;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialMixin {

    @Shadow
    @Final
    private TagKey<Block> incorrectBlocksForDrops;

    @Shadow
    @Final
    private float speed;

    @Shadow
    private Item.Properties applyCommonProperties(Item.Properties properties) {
        throw new AssertionError();
    }
    @Shadow
    private ItemAttributeModifiers createToolAttributes(float f, float g) {
        throw new AssertionError();
    }
    @Unique
    private static final Map<ToolMaterial, GeneralWeapon> SWORD = Map.of(
            ToolMaterial.WOOD, new GeneralWeapon(vsqIdentifiers.vsqSwordOverride.identifier(), EquipmentSlotGroup.MAINHAND,3.0d, -2.4d, 0.0d),
            ToolMaterial.STONE, new GeneralWeapon(vsqIdentifiers.vsqSwordOverride.identifier(), EquipmentSlotGroup.MAINHAND,4.0d, -2.4d, 0.0d),
            ToolMaterial.COPPER, new GeneralWeapon(vsqIdentifiers.vsqSwordOverride.identifier(), EquipmentSlotGroup.MAINHAND,4.0d, -2.4d, 0.0d),
            ToolMaterial.IRON, new GeneralWeapon(vsqIdentifiers.vsqSwordOverride.identifier(), EquipmentSlotGroup.MAINHAND,5.0d, -2.4d, 0.0d),
            ToolMaterial.GOLD, new GeneralWeapon(vsqIdentifiers.vsqSwordOverride.identifier(), EquipmentSlotGroup.MAINHAND,5.0d, -2.4d, 0.0d),
            ToolMaterial.DIAMOND, new GeneralWeapon(vsqIdentifiers.vsqSwordOverride.identifier(), EquipmentSlotGroup.MAINHAND,6.0d, -2.4d, 0.0d),
            ToolMaterial.NETHERITE, new GeneralWeapon(vsqIdentifiers.vsqSwordOverride.identifier(), EquipmentSlotGroup.MAINHAND,9.0d, -2.4d, 0.0d)
    );

    @Unique
    private static final Map<ToolMaterial, Dura> DURABILITY = Map.of(
            ToolMaterial.WOOD, new Dura(75),
            ToolMaterial.STONE, new Dura(150),
            ToolMaterial.COPPER, new Dura(200),
            ToolMaterial.IRON, new Dura(250),
            ToolMaterial.GOLD, new Dura(100),
            ToolMaterial.DIAMOND, new Dura(1550),
            ToolMaterial.NETHERITE, new Dura(2069)
    );
    @Inject(method = "applySwordProperties", at = @At("HEAD"), cancellable = true)
    private void applySwordProperties(Item.Properties properties, float f, float g, CallbackInfoReturnable<Item.Properties> cir) {
        ToolMaterial material = (ToolMaterial) (Object) this;
        GeneralWeapon swordAttributes = SWORD.get(material);
        if (swordAttributes == null) {
            return;
        }

        HolderGetter<Block> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        cir.setReturnValue(this.applyCommonProperties(properties).component(DataComponents.TOOL, new Tool(List.of(Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F), Tool.Rule.overrideSpeed(holderGetter.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE), Tool.Rule.overrideSpeed(holderGetter.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)), 1.0F, 2, false)).attributes(swordAttributes.build()).component(DataComponents.WEAPON, new Weapon(1)));
    }
    @Inject(method = "applyToolProperties", at = @At("HEAD"), cancellable = true)
    private void applyToolProperties (Item.Properties properties, TagKey<Block> tagKey, float f, float g, float h, CallbackInfoReturnable<Item.Properties> cir) {
        ToolMaterial material = (ToolMaterial) (Object) this;
        Dura durability = DURABILITY.getOrDefault(material, Dura.DEFAULT);
        HolderGetter<Block> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);

        cir.setReturnValue(this.applyCommonProperties(properties).component(DataComponents.TOOL, new Tool(List.of(Tool.Rule.deniesDrops(holderGetter.getOrThrow(this.incorrectBlocksForDrops)), Tool.Rule.minesAndDrops(holderGetter.getOrThrow(tagKey), this.speed)), 1.0F, 1, true)).attributes(this.createToolAttributes(f, g)).component(DataComponents.WEAPON, new Weapon(2, h)).durability(durability.dura()));
    }
}
