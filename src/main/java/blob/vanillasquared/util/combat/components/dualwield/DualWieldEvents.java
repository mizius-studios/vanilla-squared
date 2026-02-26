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

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            Optional<DualWieldUtil.ActiveDualWield> active = DualWieldUtil.getActiveDualWield(mainHand, offHand);
            if (active.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (player.getCooldowns().isOnCooldown(offHand)) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide()) {
                DualWieldUtil.ActiveDualWield config = active.get();
                if (config.offHand().cooldown() > 0) {
                    player.getCooldowns().addCooldown(offHand, config.offHand().cooldown());
                }
                ((DualWieldPlayerData) player).vsq$setDualWieldCritCharges(config.offHand().criticalHits());
            }

            return InteractionResult.SUCCESS;
        });
    }
}
