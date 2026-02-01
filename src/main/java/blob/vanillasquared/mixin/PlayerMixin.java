package blob.vanillasquared.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void vsq$prioritizeRod(Level level,
                                   InteractionHand hand,
                                   CallbackInfoReturnable<InteractionResult> cir) {

        Player player = (Player)(Object)this;

        ItemStack main = player.getMainHandItem();
        ItemStack off  = player.getOffhandItem();

        boolean mainBlocks = main.has(DataComponents.BLOCKS_ATTACKS);
        boolean offRod    = off.getItem() instanceof FishingRodItem;

        // If main would block but offhand is rod → force rod instead
        if (hand == InteractionHand.MAIN_HAND && mainBlocks && offRod) {

            InteractionResult result =
                    off.use(level, player, InteractionHand.OFF_HAND);

            cir.setReturnValue(result);
        }
    }
}








