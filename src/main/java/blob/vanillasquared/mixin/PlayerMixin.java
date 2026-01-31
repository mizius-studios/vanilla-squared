package blob.vanillasquared.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void vsq$rodPriority(Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {

        if (hand != InteractionHand.MAIN_HAND) return;

        Player player = (Player)(Object)this;

        ItemStack main = player.getMainHandItem();
        ItemStack off  = player.getOffhandItem();

        // offhand rod present
        if (!(off.getItem() instanceof FishingRodItem)) return;

        // mainhand blocks (shield component)
        if (main.has(DataComponents.BLOCKS_ATTACKS)) {

            // PASS = skip mainhand entirely → offhand gets used
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}



