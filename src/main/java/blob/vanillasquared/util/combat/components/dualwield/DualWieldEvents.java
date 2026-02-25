package blob.vanillasquared.util.combat.components.dualwield;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class DualWieldEvents {

    private DualWieldEvents() {
    }

    public static void initialize() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (hand != InteractionHand.OFF_HAND) {
                return InteractionResult.PASS;
            }

            ItemStack mainhand = player.getMainHandItem();
            ItemStack offhand = player.getOffhandItem();
            Optional<DualWieldUtil.ActiveDualWield> active = DualWieldUtil.getActiveDualWield(mainhand, offhand);
            if (active.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (player.getCooldowns().isOnCooldown(offhand)) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                DualWieldUtil.ActiveDualWield config = active.get();
                if (config.offhand().cooldown() > 0) {
                    player.getCooldowns().addCooldown(offhand, config.offhand().cooldown());
                }
                ((DualWieldPlayerData) player).vsq$setDualWieldCritCharges(config.offhand().criticalHits());
            }

            return InteractionResult.SUCCESS;
        });
    }
}
