package blob.vanillasquared.mixin;

import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float vsq$applyAttributeProtections(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity)(Object)this;

        float damage = amount;

        // Mace Protection
        Entity attacker = source.getDirectEntity();
        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack weapon = livingAttacker.getMainHandItem();
            if (weapon.is(Items.MACE)) {
                double protection = entity.getAttributeValue(RegisterAttributes.maceProtection);
                damage *= (1.0F - (float) protection);
            }
        }

        return Math.max(damage, 0);
    }
}
