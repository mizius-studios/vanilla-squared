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
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.registries.Registries;

import java.util.List;
import java.util.Optional;

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
        SoundEvent sound = SoundEvents.ANVIL_LAND;
        Holder<SoundEvent> SHIELD_HIT = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
        cir.setReturnValue(this.tool(toolMaterial, BlockTags.MINEABLE_WITH_AXE, f, g, 5.0F).component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks( 0.3F,1.0F,
                        List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 0.5F)),
                        new BlocksAttacks.ItemDamageFunction(1.0F, 1.0F, 1.0F),
                        Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                        Optional.of(SHIELD_HIT),
                        Optional.of(SoundEvents.ITEM_BREAK)
                )).component(DataComponents.BREAK_SOUND, SoundEvents.ITEM_BREAK));
    }
}