package blob.vanillasquared.mixin.world.item.items;

import blob.vanillasquared.util.api.builder.durability.Durability;
import blob.vanillasquared.util.api.builder.general.WeaponAttributeBuilder;
import blob.vanillasquared.util.api.combat.VSQCombatPresets;
import blob.vanillasquared.util.api.modules.components.VSQDataComponents;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
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

    @Inject(method = "applySwordProperties", at = @At("HEAD"), cancellable = true)
    private void applySwordProperties(Item.Properties properties, float attackDamage, float attackSpeed, CallbackInfoReturnable<Item.Properties> cir) {
        ToolMaterial material = (ToolMaterial) (Object) this;
        WeaponAttributeBuilder swordAttributes = VSQCombatPresets.swordAttributes(material);
        if (swordAttributes == null) {
            return;
        }

        cir.setReturnValue(buildSwordProperties(properties, swordAttributes));
    }

    @Inject(method = "applyToolProperties", at = @At("HEAD"), cancellable = true)
    private void applyToolProperties(Item.Properties properties, TagKey<Block> tagKey, float attackDamage, float attackSpeed, float weaponDamage, CallbackInfoReturnable<Item.Properties> cir) {
        ToolMaterial material = (ToolMaterial) (Object) this;
        Durability durability = VSQCombatPresets.toolDurability(material);
        cir.setReturnValue(buildToolProperties(properties, tagKey, attackDamage, attackSpeed, weaponDamage, durability));
    }

    @Unique
    private Item.Properties buildSwordProperties(Item.Properties properties, WeaponAttributeBuilder swordAttributes) {
        HolderGetter<Block> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        Tool toolComponent = new Tool(createSwordRules(holderGetter), 1.0F, 2, false);

        return this.applyCommonProperties(properties)
                .component(net.minecraft.core.component.DataComponents.TOOL, toolComponent)
                .attributes(swordAttributes.build())
                .component(net.minecraft.core.component.DataComponents.WEAPON, new Weapon(1))
                .component(VSQDataComponents.HIT_THROUGH, VSQCombatPresets.hitThroughPlants().build());
    }

    @Unique
    private Item.Properties buildToolProperties(
            Item.Properties properties,
            TagKey<Block> tagKey,
            float attackDamage,
            float attackSpeed,
            float weaponDamage,
            Durability durability
    ) {
        HolderGetter<Block> holderGetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        Tool toolComponent = new Tool(
                List.of(
                        Tool.Rule.deniesDrops(holderGetter.getOrThrow(this.incorrectBlocksForDrops)),
                        Tool.Rule.minesAndDrops(holderGetter.getOrThrow(tagKey), this.speed)
                ),
                1.0F,
                1,
                true
        );

        return this.applyCommonProperties(properties)
                .component(net.minecraft.core.component.DataComponents.TOOL, toolComponent)
                .attributes(this.createToolAttributes(attackDamage, attackSpeed))
                .component(net.minecraft.core.component.DataComponents.WEAPON, new Weapon(2, weaponDamage))
                .durability(durability.durability());
    }

    @Unique
    private List<Tool.Rule> createSwordRules(HolderGetter<Block> holderGetter) {
        return List.of(
                Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                Tool.Rule.overrideSpeed(holderGetter.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE),
                Tool.Rule.overrideSpeed(holderGetter.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
        );
    }
}
