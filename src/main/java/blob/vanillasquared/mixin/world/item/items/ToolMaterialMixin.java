package blob.vanillasquared.mixin.world.item.items;

import blob.vanillasquared.util.builder.components.DualWieldComponent;
import blob.vanillasquared.util.builder.components.HitThroughComponent;
import blob.vanillasquared.util.builder.general.GeneralWeapon;
import blob.vanillasquared.util.builder.durability.Durability;
import blob.vanillasquared.util.api.other.vsqIdentifiers;
import blob.vanillasquared.util.modules.components.DataComponents;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
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

import java.util.Collections;
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
    private static final Map<ToolMaterial, Durability> DURABILITY = Map.of(
            ToolMaterial.WOOD, new Durability(75),
            ToolMaterial.STONE, new Durability(150),
            ToolMaterial.COPPER, new Durability(200),
            ToolMaterial.IRON, new Durability(250),
            ToolMaterial.GOLD, new Durability(100),
            ToolMaterial.DIAMOND, new Durability(1550),
            ToolMaterial.NETHERITE, new Durability(2069)
    );

    @Unique
    private static final Map<ToolMaterial, DualWieldComponent> DUAL_WIELD = Map.of(
            ToolMaterial.WOOD, new DualWieldComponent(Collections.singletonList("vsq$sword"), 1000, 1, Collections.singletonList("minecraft:sharpness"), 50, 200),
            ToolMaterial.STONE, new DualWieldComponent(Collections.singletonList("vsq$sword"), 1000, 1, Collections.singletonList("minecraft:sharpness"), 50, 200),
            ToolMaterial.COPPER, new DualWieldComponent(Collections.singletonList("vsq$sword"), 1000, 1, Collections.singletonList("minecraft:sharpness"), 50, 200),
            ToolMaterial.IRON, new DualWieldComponent(Collections.singletonList("vsq$sword"), 1000, 1, Collections.singletonList("minecraft:sharpness"), 50, 200),
            ToolMaterial.GOLD, new DualWieldComponent(Collections.singletonList("vsq$sword"), 1000, 1, Collections.singletonList("minecraft:sharpness"), 50, 200),
            ToolMaterial.DIAMOND, new DualWieldComponent(Collections.singletonList("vsq$sword"), 1000, 1, Collections.singletonList("minecraft:sharpness"), 50, 200),
            ToolMaterial.NETHERITE, new DualWieldComponent(Collections.singletonList("vsq$sword"), 1000, 1, Collections.singletonList("minecraft:sharpness"), 50, 200)
    );

    @Unique
    private static final HitThroughComponent HIT_THROUGH_PLANTS = new HitThroughComponent(Identifier.fromNamespaceAndPath("vsq", "hit_through"));

    @Inject(method = "applySwordProperties", at = @At("HEAD"), cancellable = true)
    private void applySwordProperties(Item.Properties properties, float f, float g, CallbackInfoReturnable<Item.Properties> cir) {
        ToolMaterial material = (ToolMaterial) (Object) this;
        GeneralWeapon swordAttributes = SWORD.get(material);
        DualWieldComponent dualWieldSword = DUAL_WIELD.get(material);
        if (swordAttributes == null) {
            return;
        }

        HolderGetter<Block> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        cir.setReturnValue(this.applyCommonProperties(properties).component(net.minecraft.core.component.DataComponents.TOOL, new Tool(List.of(Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F), Tool.Rule.overrideSpeed(holderGetter.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE), Tool.Rule.overrideSpeed(holderGetter.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)), 1.0F, 2, false)).attributes(swordAttributes.build()).component(net.minecraft.core.component.DataComponents.WEAPON, new Weapon(1)).component(DataComponents.HIT_THROUGH, HIT_THROUGH_PLANTS));
    }
    @Inject(method = "applyToolProperties", at = @At("HEAD"), cancellable = true)
    private void applyToolProperties (Item.Properties properties, TagKey<Block> tagKey, float f, float g, float h, CallbackInfoReturnable<Item.Properties> cir) {
        ToolMaterial material = (ToolMaterial) (Object) this;
        Durability durability = DURABILITY.getOrDefault(material, Durability.DEFAULT);
        HolderGetter<Block> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);

        cir.setReturnValue(this.applyCommonProperties(properties).component(net.minecraft.core.component.DataComponents.TOOL, new Tool(List.of(Tool.Rule.deniesDrops(holderGetter.getOrThrow(this.incorrectBlocksForDrops)), Tool.Rule.minesAndDrops(holderGetter.getOrThrow(tagKey), this.speed)), 1.0F, 1, true)).attributes(this.createToolAttributes(f, g)).component(net.minecraft.core.component.DataComponents.WEAPON, new Weapon(2, h)).durability(durability.dura()));
    }
}
