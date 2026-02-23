package blob.vanillasquared.mixin.world.entity.entities;

import blob.vanillasquared.util.combat.DamageUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float vsq$applyMaceProtection(float amount, ServerLevel level, DamageSource source) {
        return DamageUtil.applyMaceProtection((Player) (Object) this, source, amount);
    }
}
