package blob.vanillasquared.mixin;

import blob.vanillasquared.util.modules.attributes.RegisterAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float vsq$applyMaceProtection(float amount, ServerLevel level, DamageSource source) {
        Player player = (Player) (Object) this;

        if (source.is(DamageTypeTags.IS_MACE_SMASH)) {
            double protection = player.getAttributeValue(RegisterAttributes.maceProtection);
            protection = Math.max(0.0, Math.min(1.0, protection));
            return Math.max(amount * (1.0F - (float) protection), 0.0F);
        }

        return Math.max(amount, 0.0F);
    }
}
