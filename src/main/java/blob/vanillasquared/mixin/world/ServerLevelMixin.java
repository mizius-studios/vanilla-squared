package blob.vanillasquared.mixin.world;

import blob.vanillasquared.main.world.effect.ChannelingState;
import blob.vanillasquared.main.world.effect.LungingState;
import blob.vanillasquared.main.world.effect.SwirlingState;
import blob.vanillasquared.main.world.redstone.VSQEntityRedstonePowerLevelAccess;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements VSQEntityRedstonePowerLevelAccess {
    @Unique
    private int vsq$poweredEntityCount;

    @Inject(method = "tick", at = @At("TAIL"))
    private void vsq$tickChanneling(BooleanSupplier haveTime, CallbackInfo ci) {
        ChannelingState.tick((ServerLevel) (Object) this);
        LungingState.tick((ServerLevel) (Object) this);
        SwirlingState.tick((ServerLevel) (Object) this);
    }

    @Override
    public int vsq$getPoweredEntityCount() {
        return this.vsq$poweredEntityCount;
    }

    @Override
    public void vsq$incrementPoweredEntityCount() {
        this.vsq$poweredEntityCount++;
    }

    @Override
    public void vsq$decrementPoweredEntityCount() {
        if (this.vsq$poweredEntityCount > 0) {
            this.vsq$poweredEntityCount--;
        }
    }
}
