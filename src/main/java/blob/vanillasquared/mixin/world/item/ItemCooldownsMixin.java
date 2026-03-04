package blob.vanillasquared.mixin.world.item;

import blob.vanillasquared.util.combat.cooldown.CooldownGroupUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCooldowns.class)
public class ItemCooldownsMixin {
    @Unique
    private boolean vsq$normalizingCooldownKey;

    @Inject(method = "getCooldownGroup", at = @At("HEAD"), cancellable = true)
    private void vsq$getCooldownGroup(ItemStack stack, CallbackInfoReturnable<Identifier> cir) {
        cir.setReturnValue(CooldownGroupUtil.stackGroup(stack));
    }

    @Inject(method = "addCooldown(Lnet/minecraft/resources/Identifier;I)V", cancellable = true, at = @At("HEAD"))
    public void addCooldown(Identifier identifier, int cooldown, CallbackInfo ci) {
        if (this.vsq$normalizingCooldownKey) {
            return;
        }
        this.vsq$normalizingCooldownKey = true;
        try {
            Identifier normalized = CooldownGroupUtil.normalize(identifier);
            ((ItemCooldowns) (Object) this).addCooldown(normalized, cooldown);
            ci.cancel();
        } finally {
            this.vsq$normalizingCooldownKey = false;
        }
    }
}
