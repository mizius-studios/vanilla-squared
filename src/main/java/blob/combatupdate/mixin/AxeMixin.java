package blob.combatupdate.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.Tool;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.registries.Registries;

import java.util.List;
import java.util.Optional;

/*
    Tasks for AxeMixin:
    - I can access ToolMaterial
    - DmgReduction amt shld scale w. the material(better material -> more dmg shielded(max = 50%)), with gold axes breaking instantly but reducting 100% of all incoming dmg!
    - Block Delay shld slightly scale w. the material(better material -> longer delay(max = normal delay) | similar to the spear in concept)
    - Cooldown shld scale a lot w. the material(better material -> longer shield break cooldown(max = normal delay) | similar to the spear in concept)
    - Scale the weapon dmg attribute, idk if this needs to be done in here, but we cant have stone, copper, iron and diamond all have the same dmg
    - Lower Reach(by like 3/4 of a block decreased) this shldnt increase/decrease with tool material, although gold shld have 5 block reach
 */

@Debug(export = true)
@Mixin(Item.Properties.class)
public abstract class AxeMixin {
    @Shadow
    public abstract Item.Properties tool(
            ToolMaterial material,
            net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag,
            float f,
            float g,
            float d
    );

    @Inject(at = @At("HEAD"), method = "axe", cancellable = true)
    public void init(ToolMaterial toolMaterial, float f, float g, CallbackInfoReturnable<Item.Properties> cir) {

    String material = toolMaterial.toString();
    float blockDelay = 0.0F;
    float shieldBreakCooldown = 0.0F; // default 1.0
    float damageReduction = 1.0F; // 100% = 1.0
    float duraDMG = 1.0F; // 1.0 = 1dmg point
    switch (material) {
        case "WOOD":
            blockDelay = 0.0F;
            shieldBreakCooldown = 0.0F;
            damageReduction = 1.0F;
            duraDMG = 1.0F;
            break;
        case "STONE":
            blockDelay = 0.0F;
            shieldBreakCooldown = 0.0F;
            damageReduction = 1.0F;
            duraDMG = 1.0F;
            break;
        case "COPPER":
            blockDelay = 0.0F;
            shieldBreakCooldown = 0.0F;
            damageReduction = 1.0F;
            duraDMG = 1.0F;
            break;
        case "IRON":
            blockDelay = 0.0F;
            shieldBreakCooldown = 0.0F;
            damageReduction = 1.0F;
            duraDMG = 1.0F;
            break;
        case "DIAMOND":
            blockDelay = 0.0F;
            shieldBreakCooldown = 0.0F;
            damageReduction = 1.0F;
            duraDMG = 1.0F;
            break;
        case "NETHERITE":
            blockDelay = 0.0F;
            shieldBreakCooldown = 0.0F;
            damageReduction = 1.0F;
            duraDMG = 1.0F;
            break;

    } // I didn't have time to test the switch statement just yet!

        SoundEvent sound = SoundEvents.ANVIL_LAND;
        Holder<SoundEvent> SHIELD_HIT = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
        cir.setReturnValue(this.tool(toolMaterial, BlockTags.MINEABLE_WITH_AXE, f, g, 5.0F).component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks( blockDelay,shieldBreakCooldown,
                        List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, damageReduction)),
                        new BlocksAttacks.ItemDamageFunction(duraDMG, 1.0F, 1.0F),
                        Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                        Optional.of(SHIELD_HIT),
                        Optional.of(SoundEvents.ITEM_BREAK)
                )).component(DataComponents.BREAK_SOUND, SoundEvents.ITEM_BREAK));
    }
}