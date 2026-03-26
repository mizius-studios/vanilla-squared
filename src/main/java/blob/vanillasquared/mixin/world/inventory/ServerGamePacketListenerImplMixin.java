package blob.vanillasquared.mixin.world.inventory;

import blob.vanillasquared.main.world.inventory.VSQEnchantmentMenu;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlaceRecipe", at = @At("HEAD"), cancellable = true)
    private void vsq$handlePlaceRecipe(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
        if (!(this.player.containerMenu instanceof VSQEnchantmentMenu menu)) {
            return;
        }
        if (menu.containerId != packet.containerId()) {
            ci.cancel();
            return;
        }
        menu.vsq$setSelectedDisplayId(packet.recipe().index());
        menu.vsq$handleRecipeBookPlacement(this.player, packet.recipe().index());
        ci.cancel();
    }
}
