package blob.vanillasquared.mixin.world.entity;

import blob.vanillasquared.util.combat.DamageUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float vsq$applyAttributeProtections(float amount, ServerLevel level, DamageSource source) {
        return DamageUtil.applyMaceProtection((LivingEntity) (Object) this, source, amount);
    }

    @Inject(method = "startUsingItem", at = @At("HEAD"), cancellable = true)
    private void vsq$useShieldBeforeAxeBlock(InteractionHand hand, CallbackInfo ci) {
        if (hand != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!((Object) this instanceof Player player)) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof AxeItem) || !player.getOffhandItem().is(Items.SHIELD)) {
            return;
        }

        player.startUsingItem(InteractionHand.OFF_HAND);
        ci.cancel();
    }
}
