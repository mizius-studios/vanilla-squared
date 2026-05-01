package blob.vanillasquared.main.world.item.enchantment.effects;

import net.minecraft.world.item.enchantment.effects.ApplyEntityImpulse;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class ApplyImpulseSpeedState {
    private static final Map<ApplyEntityImpulse, Double> SPEEDS = Collections.synchronizedMap(new WeakHashMap<>());

    private ApplyImpulseSpeedState() {
    }

    public static ApplyEntityImpulse remember(ApplyEntityImpulse effect, double speed) {
        SPEEDS.put(effect, speed);
        return effect;
    }

    public static double speed(ApplyEntityImpulse effect) {
        return SPEEDS.getOrDefault(effect, 1.0D);
    }
}
