package blob.vanillasquared.mixin;

import blob.vanillasquared.VanillaSquared;
import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import net.minecraft.server.level.ServerLevel;
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

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float vsq$applyAttributeProtections(float amount, ServerLevel level, DamageSource source) {
        LivingEntity entity = (LivingEntity)(Object)this;

        VanillaSquared.LOGGER.info("=== actuallyHurt called for entity: {} ===", entity.getName().getString());
        VanillaSquared.LOGGER.info("Original damage: {}", amount);

        float damage = amount;

        // Mace Protection
        Entity attacker = source.getDirectEntity();
        VanillaSquared.LOGGER.info("Attacker: {}", attacker != null ? attacker.getName().getString() : "null");
        
        if (attacker instanceof LivingEntity livingAttacker) {
            VanillaSquared.LOGGER.info("Attacker is LivingEntity");
            ItemStack weapon = livingAttacker.getMainHandItem();
            VanillaSquared.LOGGER.info("Weapon in hand: {}", weapon.getItem());
            
            if (weapon.is(Items.MACE)) {
                VanillaSquared.LOGGER.info("Weapon is MACE!");
                
                // Check if entity has the attribute
                if (entity.getAttributes().hasAttribute(RegisterAttributes.maceProtection)) {
                    VanillaSquared.LOGGER.info("Entity HAS maceProtection attribute");
                } else {
                    VanillaSquared.LOGGER.info("Entity DOES NOT HAVE maceProtection attribute!");
                }
                
                double protection = entity.getAttributeValue(RegisterAttributes.maceProtection);
                VanillaSquared.LOGGER.info("Mace Protection value: {}", protection);
                
                // Log equipment
                VanillaSquared.LOGGER.info("Chest armor: {}", entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST));
                
                damage *= (1.0F - (float) protection);
                VanillaSquared.LOGGER.info("Damage after protection ({}): {}", protection, damage);
            } else {
                VanillaSquared.LOGGER.info("Weapon is NOT a mace");
            }
        } else {
            VanillaSquared.LOGGER.info("Attacker is NOT a LivingEntity");
        }

        VanillaSquared.LOGGER.info("=== Final damage: {} ===", Math.max(damage, 0));
        return Math.max(damage, 0);
    }
}
