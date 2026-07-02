package blob.vanillasquared.main.world.redstone;

public interface VSQEntityRedstonePowerLevelAccess {
    int vsq$getPoweredEntityCount();

    void vsq$incrementPoweredEntityCount();

    void vsq$decrementPoweredEntityCount();
}
