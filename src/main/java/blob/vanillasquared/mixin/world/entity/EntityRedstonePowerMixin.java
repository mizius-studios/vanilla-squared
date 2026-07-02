package blob.vanillasquared.mixin.world.entity;

import blob.vanillasquared.main.world.redstone.VSQEntityRedstonePower;
import blob.vanillasquared.main.world.redstone.VSQEntityRedstonePowerAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityRedstonePowerMixin implements VSQEntityRedstonePowerAccess {
    @Unique
    private AABB vsq$previousRedstoneSourceBounds;
    @Unique
    private int vsq$previousRedstonePower;
    @Unique
    private int vsq$redstonePower;
    @Unique
    private boolean vsq$redstonePowerCounted;
    @Unique
    private ServerLevel vsq$redstonePowerCountedLevel;

    @Inject(method = "load", at = @At("TAIL"))
    private void vsq$loadEntityRedstonePower(ValueInput input, CallbackInfo ci) {
        input.getInt(VSQEntityRedstonePower.POWER_REDSTONE_KEY)
                .ifPresent(power -> this.vsq$redstonePower = Mth.clamp(power, 0, 15));
        this.vsq$reconcileRedstonePowerCount();
    }

    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void vsq$saveEntityRedstonePower(ValueOutput output, CallbackInfo ci) {
        if (this.vsq$redstonePower > 0) {
            output.putInt(VSQEntityRedstonePower.POWER_REDSTONE_KEY, this.vsq$redstonePower);
        }
    }

    @Override
    public int vsq$getRedstonePower() {
        return this.vsq$redstonePower;
    }

    @Override
    public void vsq$setRedstonePower(int power) {
        this.vsq$redstonePower = Mth.clamp(power, 0, 15);
        this.vsq$reconcileRedstonePowerCount();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void vsq$tickEntityRedstonePower(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        this.vsq$reconcileRedstonePowerCount();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }

        AABB currentBounds = entity.getBoundingBox();
        int currentPower = VSQEntityRedstonePower.getPower(entity);
        boolean boundsChanged = this.vsq$previousRedstoneSourceBounds != null
                && !this.vsq$previousRedstoneSourceBounds.equals(currentBounds);
        boolean powerChanged = this.vsq$previousRedstonePower != currentPower;

        if (this.vsq$previousRedstonePower > 0 && this.vsq$previousRedstoneSourceBounds != null
                && (currentPower <= 0 || boundsChanged)) {
            VSQEntityRedstonePower.updateNeighbors(level, this.vsq$previousRedstoneSourceBounds);
        }

        if (currentPower > 0) {
            if (this.vsq$previousRedstoneSourceBounds == null || boundsChanged || powerChanged) {
                VSQEntityRedstonePower.updateNeighbors(level, currentBounds);
            }
            this.vsq$previousRedstoneSourceBounds = currentBounds;
        } else {
            this.vsq$previousRedstoneSourceBounds = null;
        }
        this.vsq$previousRedstonePower = currentPower;
    }

    @Inject(method = "onRemoval", at = @At("TAIL"))
    private void vsq$removeEntityRedstonePower(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.level() instanceof ServerLevel level) {
            if (this.vsq$previousRedstonePower > 0 && this.vsq$previousRedstoneSourceBounds != null) {
                VSQEntityRedstonePower.updateNeighbors(level, this.vsq$previousRedstoneSourceBounds);
            } else if (VSQEntityRedstonePower.getPower(entity) > 0) {
                VSQEntityRedstonePower.updateNeighbors(level, entity.getBoundingBox());
            }
        }

        this.vsq$unregisterRedstonePowerCount();
        this.vsq$previousRedstoneSourceBounds = null;
        this.vsq$previousRedstonePower = 0;
    }

    @Unique
    private void vsq$reconcileRedstonePowerCount() {
        Entity entity = (Entity) (Object) this;
        if (this.vsq$redstonePower > 0 && !entity.isRemoved() && entity.level() instanceof ServerLevel level) {
            if (!this.vsq$redstonePowerCounted) {
                VSQEntityRedstonePower.incrementPoweredEntityCount(level);
                this.vsq$redstonePowerCounted = true;
                this.vsq$redstonePowerCountedLevel = level;
            } else if (this.vsq$redstonePowerCountedLevel != level) {
                this.vsq$unregisterRedstonePowerCount();
                VSQEntityRedstonePower.incrementPoweredEntityCount(level);
                this.vsq$redstonePowerCounted = true;
                this.vsq$redstonePowerCountedLevel = level;
            }
        } else {
            this.vsq$unregisterRedstonePowerCount();
        }
    }

    @Unique
    private void vsq$unregisterRedstonePowerCount() {
        if (this.vsq$redstonePowerCounted && this.vsq$redstonePowerCountedLevel != null) {
            VSQEntityRedstonePower.decrementPoweredEntityCount(this.vsq$redstonePowerCountedLevel);
        }
        this.vsq$redstonePowerCounted = false;
        this.vsq$redstonePowerCountedLevel = null;
    }
}
