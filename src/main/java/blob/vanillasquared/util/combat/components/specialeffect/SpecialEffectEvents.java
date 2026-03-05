package blob.vanillasquared.util.combat.components.specialeffect;

import blob.vanillasquared.util.api.combat.cooldown.CooldownGroupUtil;
import blob.vanillasquared.util.modules.components.DataComponents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public final class SpecialEffectEvents {
    private SpecialEffectEvents() {
    }

    public static void initialize() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            SpecialEffectComponent specialEffect = stack.get(DataComponents.SPECIAL_EFFECT);
            if (specialEffect == null) {
                return InteractionResult.PASS;
            }

            Identifier vanillaGroup = CooldownGroupUtil.stackGroup(stack);
            Identifier cooldownGroup = SpecialEffectCooldownKeyUtil.cooldownGroup(stack, specialEffect);
            boolean vanillaCooldownActive = SpecialEffectCooldownKeyUtil.isOnCooldown(player.getCooldowns(), vanillaGroup);
            boolean secondaryCooldownActive = SpecialEffectCooldownKeyUtil.isOnCooldown(player.getCooldowns(), cooldownGroup);
            if (vanillaCooldownActive && secondaryCooldownActive) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide() && specialEffect.cooldown() > 0 && !vanillaCooldownActive) {
                player.getCooldowns().addCooldown(cooldownGroup, specialEffect.cooldown());
            }

            return InteractionResult.PASS;
        });
    }
}
