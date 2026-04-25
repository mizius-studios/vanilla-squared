package blob.vanillasquared.mixin.world.item.trading;

import blob.vanillasquared.main.world.recipe.enchanting.EnchantingRecipeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerTrade.class)
public abstract class VillagerTradeMixin {
    @Inject(method = "getOffer", at = @At("RETURN"), cancellable = true)
    private void vsq$replaceEnchantedBookOffer(LootContext context, CallbackInfoReturnable<MerchantOffer> cir) {
        MerchantOffer offer = cir.getReturnValue();
        if (offer == null || !offer.getResult().is(Items.ENCHANTED_BOOK)) {
            return;
        }

        ItemStack replacement = EnchantingRecipeTags.createRandomStack(EnchantingRecipeTags.DEFAULT_LIBRARIAN_TAG, context.getRandom());
        if (replacement.isEmpty()) {
            return;
        }

        cir.setReturnValue(new MerchantOffer(
                offer.getItemCostA(),
                offer.getItemCostB(),
                replacement,
                offer.getMaxUses(),
                offer.getXp(),
                offer.getPriceMultiplier()
        ));
    }
}
