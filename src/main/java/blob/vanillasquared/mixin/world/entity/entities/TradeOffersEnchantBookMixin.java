package blob.vanillasquared.mixin.world.entity.entities;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.npc.TradeOffers$EnchantBookForEmeralds")
public abstract class TradeOffersEnchantBookMixin {

    @Inject(method = "getOffer", at = @At("HEAD"), cancellable = true)
    private void vsq$remove(Entity entity, RandomSource random, CallbackInfoReturnable<?> cir) {
        cir.setReturnValue(null);
    }
}
