package blob.vanillasquared.main.world.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public final class VSQEntityRedstonePower {
    public static final String POWER_REDSTONE_KEY = "vsq:powerRedstone";

    private VSQEntityRedstonePower() {
    }

    public static int getPower(Entity entity) {
        if (entity instanceof VSQEntityRedstonePowerAccess access) {
            return access.vsq$getRedstonePower();
        }
        return 0;
    }

    public static boolean hasPoweredEntities(ServerLevel level) {
        return level instanceof VSQEntityRedstonePowerLevelAccess access && access.vsq$getPoweredEntityCount() > 0;
    }

    public static void incrementPoweredEntityCount(ServerLevel level) {
        if (level instanceof VSQEntityRedstonePowerLevelAccess access) {
            access.vsq$incrementPoweredEntityCount();
        }
    }

    public static void decrementPoweredEntityCount(ServerLevel level) {
        if (level instanceof VSQEntityRedstonePowerLevelAccess access) {
            access.vsq$decrementPoweredEntityCount();
        }
    }

    public static int getSignal(ServerLevel level, BlockPos pos) {
        if (!hasPoweredEntities(level)) {
            return 0;
        }

        AABB blockBounds = new AABB(pos);
        int signal = 0;
        for (Entity entity : level.getEntities((Entity) null, blockBounds, entity -> !entity.isRemoved())) {
            signal = Math.max(signal, getPower(entity));
            if (signal >= 15) {
                return 15;
            }
        }
        return signal;
    }

    public static void updateNeighbors(ServerLevel level, AABB sourceBounds) {
        AABB updateBounds = sourceBounds.inflate(1.0D);
        for (BlockPos pos : BlockPos.betweenClosed(updateBounds)) {
            level.updateNeighborsAt(pos, Blocks.AIR, null);
        }
    }
}
