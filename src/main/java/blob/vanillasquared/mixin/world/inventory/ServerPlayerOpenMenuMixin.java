package blob.vanillasquared.mixin.world.inventory;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.main.world.inventory.EnchantmentMenuRedirectProvider;
import blob.vanillasquared.main.world.inventory.VSQEnchantmentMenu;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerOpenMenuMixin extends Player {
    @Shadow
    private int containerCounter;

    protected ServerPlayerOpenMenuMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", at = @At("HEAD"), cancellable = true)
    private void vsq$redirectVanillaEnchantmentMenu(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        if (provider == null
                || provider instanceof EnchantmentMenuRedirectProvider
                || !(provider instanceof ExtendedMenuProvider<?>)) {
            return;
        }
        BlockPos openingPos = this.vsq$resolveOpeningPos(provider);
        if (VSQEnchantmentMenu.SYNTHETIC_OPEN_POS.equals(openingPos)) {
            return;
        }

        Component title = provider.getDisplayName();
        VanillaSquared.LOGGER.debug("Redirecting vanilla EnchantmentMenu open to VSQEnchantmentMenu via {}", provider.getClass().getName());
        cir.setReturnValue(((ServerPlayer) (Object) this).openMenu(new EnchantmentMenuRedirectProvider(title, openingPos)));
    }

    private BlockPos vsq$resolveOpeningPos(MenuProvider provider) {
        if (provider instanceof ExtendedMenuProvider<?> extendedMenuProvider) {
            Object openingData = extendedMenuProvider.getScreenOpeningData((ServerPlayer) (Object) this);
            if (openingData instanceof BlockPos blockPos) {
                return blockPos;
            }
        }

        return VSQEnchantmentMenu.SYNTHETIC_OPEN_POS;
    }
}
