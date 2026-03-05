package blob.vanillasquared.mixin.world.item.components;

import blob.vanillasquared.util.api.other.vsqIdentifiers;
import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemAttributeModifiers.Display.Default.class)
public class ItemAttributeModifiersDisplayDefaultMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void apply(Consumer<Component> consumer, @Nullable Player player, Holder<Attribute> holder, AttributeModifier attributeModifier, CallbackInfo ci) {
        boolean bl = false;
        double d = attributeModifier.amount();
        if (player != null) {
            if (attributeModifier.is(vsqIdentifiers.maceProtectionAttribute.identifier())) {
                d += player.getAttributeBaseValue(RegisterAttributes.maceProtectionAttribute);
                bl = true;
            } else if (attributeModifier.is(vsqIdentifiers.magicProtectionAttribute.identifier())) {
                d += player.getAttributeBaseValue(RegisterAttributes.magicProtectionAttribute);
                bl = true;
            } else if (attributeModifier.is(vsqIdentifiers.dripstoneProtectionAttribute.identifier())) {
                d += player.getAttributeBaseValue(RegisterAttributes.dripstoneProtectionAttribute);
                bl = true;
            } else if (attributeModifier.is(vsqIdentifiers.spearProtectionAttribute.identifier())) {
                d += player.getAttributeBaseValue(RegisterAttributes.spearProtectionAttribute);
                bl = true;
            }
        }
    }
}
