package blob.vanillasquared.mixin.world.entity;

import blob.vanillasquared.main.world.util.DamageUtil;
import blob.vanillasquared.main.world.item.EnchantmentPostBlockEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private ItemStack vsq$blockingItem;

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true, name = "dmg")
    private float vsq$applyAttributeProtections(float dmg, ServerLevel level, DamageSource source) {
        return DamageUtil.applyCustomProtections((LivingEntity) (Object) this, source, dmg);
    }

    @Inject(
            method = "applyItemBlocking",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/component/BlocksAttacks;hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V"
            )
    )
    private void vsq$captureBlockingItem(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        vsq$blockingItem = ((LivingEntity) (Object) this).getItemBlockingWith();
    }

    @Inject(
            method = "applyItemBlocking",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/component/BlocksAttacks;hurtBlockingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void vsq$applyPostBlockEffects(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        ItemStack sourceItem = source.getEntity() instanceof LivingEntity attacker ? attacker.getWeaponItem() : null;
        EnchantmentPostBlockEffects.run(level, (LivingEntity) (Object) this, source, sourceItem, vsq$blockingItem);
        vsq$blockingItem = null;
    }
}
