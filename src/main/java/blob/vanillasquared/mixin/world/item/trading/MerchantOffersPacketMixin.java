package blob.vanillasquared.mixin.world.item.trading;

import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundMerchantOffersPacket.class)
public abstract class MerchantOffersPacketMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void vsq$filterBooksFromPacket(int containerId, MerchantOffers offers, int level, int xp, boolean showProgress, boolean canRestock, CallbackInfo ci) {
        if (offers != null) {
            offers.removeIf(offer -> offer.getResult().is(Items.ENCHANTED_BOOK) || offer.getResult().isEmpty());
        }
    }
}
