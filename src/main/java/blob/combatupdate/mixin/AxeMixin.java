package blob.combatupdate.mixin;

import blob.combatupdate.util.data.Axe;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;
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
import java.util.logging.Logger;

import static blob.combatupdate.CombatUpdate.MOD_ID;

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
    @Unique
    private static final Map<ToolMaterial, Axe> AXE = Map.of(
            ToolMaterial.WOOD, new Axe(0.1F, 0.25F, 0.25F,2.0F, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR),
            ToolMaterial.STONE, new Axe(0.4F, 0.4F, 0.25F,1.0F, SoundEvents.STONE_PLACE, SoundEvents.STONE_BREAK),
            ToolMaterial.COPPER, new Axe(0.5F, 0.5F, 0.35F,2.0F, SoundEvents.COPPER_BULB_PLACE, SoundEvents.COPPER_GRATE_BREAK),
            ToolMaterial.IRON, new Axe(0.85F, 0.65F, 0.5F,2.0F, SoundEvents.IRON_PLACE, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR),
            ToolMaterial.DIAMOND, new Axe(0.95F, 0.75F, 0.65F,1.0F, SoundEvents.AMETHYST_BLOCK_STEP, SoundEvents.AMETHYST_CLUSTER_BREAK),
            ToolMaterial.GOLD, new Axe(0F, 0F, 1.0F,0.5F, SoundEvents.IRON_PLACE, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR),
            ToolMaterial.NETHERITE, new Axe(1.0F, 0.8F, 0.8F,1.0F, SoundEvents.NETHERITE_BLOCK_PLACE, SoundEvents.NETHERITE_BLOCK_BREAK)
    );



    @Inject(at = @At("HEAD"), method = "axe", cancellable = true)
    public void init(ToolMaterial toolMaterial, float f, float g, CallbackInfoReturnable<Item.Properties> cir) {
    Logger LOGGER = Logger.getLogger(MOD_ID);
    Axe s = AXE.getOrDefault(toolMaterial, Axe.DEFAULT);
    float blockDelay = s.blockDelay();
    float shieldBreakCooldown = s.shieldBreakCooldown(); // default 1.0
    float damageReduction = s.damageReduction(); // 100% = 1.0
    float duraDMG = s.duraDamage(); // 1.0 = 1dmg point
        SoundEvent blockSound = s.blockSound();
        SoundEvent breakSound = s.breakSound();
        Holder<SoundEvent> BLOCK_SOUND = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(blockSound);
        Holder<SoundEvent> BREAK_SOUND = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(breakSound);
        cir.setReturnValue(this.tool(toolMaterial, BlockTags.MINEABLE_WITH_AXE, f, g, 5.0F).component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks( blockDelay,shieldBreakCooldown,
                        List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, damageReduction)),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, duraDMG),
                        Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                        Optional.of(BLOCK_SOUND),
                        Optional.of(BREAK_SOUND)
                )));
    }
}