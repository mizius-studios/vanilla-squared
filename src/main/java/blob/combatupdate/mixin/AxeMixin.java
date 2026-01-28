package blob.combatupdate.mixin;

import blob.combatupdate.util.data.BlockComponent;
import blob.combatupdate.util.data.GeneralWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Debug(export = true)
@Mixin(Item.Properties.class)

// !Start

public abstract class AxeMixin {

    @Shadow // Obtaining access to the this keyword because its required for this.tool(only non-static paths)
    public abstract Item.Properties tool(
            ToolMaterial material,
            net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag,
            float f,
            float g,
            float d
    );
    @Unique // Variables common to all weapons
    private static final Map<ToolMaterial, GeneralWeapon> AXE_WEAPON = Map.of( // blob.combatupdate.util.data.GeneralWeapon;
            ToolMaterial.WOOD, new GeneralWeapon(5.0d, -3.2d,-1.0d,75),
            ToolMaterial.STONE, new GeneralWeapon(6.0d, -3.4d, -1.0d, 150),
            ToolMaterial.COPPER, new GeneralWeapon(6.0d, -3.0d, -1.0d, 200),
            ToolMaterial.IRON, new GeneralWeapon(7.0d, -3.0d, -1.0d, 250),
            ToolMaterial.GOLD, new GeneralWeapon(6.0d, -2.9d, -0.5d, 100),
            ToolMaterial.DIAMOND, new GeneralWeapon(8.0d, -3.0d, -1.0d, 1550),
            ToolMaterial.NETHERITE, new GeneralWeapon(9.0d, -3.0d, -1.0d, 2069)
    );
    @Unique // Variables for the Shield Component
    private static final Map<ToolMaterial, BlockComponent> AXE = Map.of( // blob.combatupdate.util.data.BlockComponent;
            ToolMaterial.WOOD, new BlockComponent(0.1F, 0.25F, 0.25F,2.0F, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR),
            ToolMaterial.STONE, new BlockComponent(0.4F, 0.4F, 0.25F,1.0F, SoundEvents.STONE_PLACE, SoundEvents.STONE_BREAK),
            ToolMaterial.COPPER, new BlockComponent(0.5F, 0.5F, 0.35F,2.0F, SoundEvents.COPPER_BULB_PLACE, SoundEvents.COPPER_GRATE_BREAK),
            ToolMaterial.IRON, new BlockComponent(0.85F, 0.65F, 0.5F,2.0F, SoundEvents.IRON_PLACE, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR),
            ToolMaterial.GOLD, new BlockComponent(0F, 0F, 1.0F,0.5F, SoundEvents.IRON_PLACE, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR),
            ToolMaterial.DIAMOND, new BlockComponent(0.95F, 0.75F, 0.65F,1.0F, SoundEvents.AMETHYST_BLOCK_STEP, SoundEvents.AMETHYST_CLUSTER_BREAK),
            ToolMaterial.NETHERITE, new BlockComponent(1.0F, 0.8F, 0.8F,1.0F, SoundEvents.NETHERITE_BLOCK_PLACE, SoundEvents.NETHERITE_BLOCK_BREAK)
    );
    @Unique // Attribute Modifier Identifier
    private static final Identifier ATTRIBUTE_IDENTIFIER_REACH = Identifier.fromNamespaceAndPath("combatupdate", "axe_reach");
    @Unique // Attribute Modifier Identifier - for text specifically!
    private static final Identifier DUMMY = Identifier.fromNamespaceAndPath("combatupdate", "dummy");


    // !Inject Starts

    @Inject(at = @At("HEAD"), method = "axe", cancellable = true)
    public void init(ToolMaterial toolMaterial, float f, float g, CallbackInfoReturnable<Item.Properties> cir) {

        // <== Variable Stuff ==>

        BlockComponent s = AXE.getOrDefault(toolMaterial, BlockComponent.DEFAULT);
        GeneralWeapon a = AXE_WEAPON.getOrDefault(toolMaterial, GeneralWeapon.DEFAULT);
        float blockDelay = s.blockDelay(); // default 1.0
        float shieldBreakCooldown = s.shieldBreakCooldown(); // default 1.0
        float damageReduction = s.damageReduction(); // 100% = 1.0
        float duraDMG = s.duraDamage(); // 1.0 = 1dmg point
        double attackDMG = a.attackDamage();
        double attackSpeed = a.attackSpeed();
        double attackReach = a.entityReach();
        int dura = a.dura();

        // <= Sounds =>
        SoundEvent blockSound = s.blockSound();
        SoundEvent breakSound = s.breakSound();
        Holder<SoundEvent> BLOCK_SOUND = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(blockSound); // Need some extra help due to some Holder thing idk
        Holder<SoundEvent> BREAK_SOUND = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(breakSound);

        // <= Description Text =>

        Component EMPTY = Component.literal("");
        Component TOOLTIP_LINE1 = Component.literal("Shield Stats: ").withStyle(ChatFormatting.GRAY);
        Component TOOLTIP_LINE2 = Component.literal(blockDelay + " Block Delay").withStyle(ChatFormatting.DARK_GREEN);
        Component TOOLTIP_LINE3 = Component.literal(shieldBreakCooldown + " Break Cooldown").withStyle(ChatFormatting.DARK_GREEN);
        Component TOOLTIP_LINE4 = Component.literal(damageReduction + " Damage Reduction").withStyle(ChatFormatting.DARK_GREEN);


        // <== Return ==>

        cir.setReturnValue(this.tool(toolMaterial, BlockTags.MINEABLE_WITH_AXE, f, g, 5.0F) // Vanilla Part
                // <-- Shield Component -->
                .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks( blockDelay,shieldBreakCooldown,
                        List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, damageReduction)),
                        new BlocksAttacks.ItemDamageFunction(1.0F, 1.0F, duraDMG),
                        Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                        Optional.of(BLOCK_SOUND),
                        Optional.of(BREAK_SOUND)))

                // <= Attributes =>
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDMG, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ATTRIBUTE_IDENTIFIER_REACH, attackReach, AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)

                        // <-- Description -->
                        .add(Attributes.LUCK, new AttributeModifier(DUMMY, 0.0d, AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND, ItemAttributeModifiers.Display.override(EMPTY))
                        .add(Attributes.LUCK, new AttributeModifier(DUMMY, 0.0d, AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND, ItemAttributeModifiers.Display.override(TOOLTIP_LINE1))
                        .add(Attributes.LUCK, new AttributeModifier(DUMMY, 0.0d, AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND, ItemAttributeModifiers.Display.override(TOOLTIP_LINE2))
                        .add(Attributes.LUCK, new AttributeModifier(DUMMY, 0.0d, AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND, ItemAttributeModifiers.Display.override(TOOLTIP_LINE3))
                        .add(Attributes.LUCK, new AttributeModifier(DUMMY, 0.0d, AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND, ItemAttributeModifiers.Display.override(TOOLTIP_LINE4)).build())
                    .durability(dura));
    }
}