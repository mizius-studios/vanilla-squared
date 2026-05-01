package blob.vanillasquared.main.world.item.enchantment.effects;

import net.minecraft.world.item.enchantment.effects.ApplyEntityImpulse;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ApplyImpulseSpeedState {
    private static final Map<Key, Double> SPEEDS = new ConcurrentHashMap<>();

    private ApplyImpulseSpeedState() {
    }

    public static ApplyEntityImpulse remember(ApplyEntityImpulse effect, double speed) {
        SPEEDS.put(Key.of(effect), speed);
        return effect;
    }

    public static double speed(ApplyEntityImpulse effect) {
        return SPEEDS.getOrDefault(Key.of(effect), 1.0D);
    }

    private record Key(Vec3 direction, Vec3 coordinateScale, LevelBasedValue magnitude) {
        private static Key of(ApplyEntityImpulse effect) {
            return new Key(effect.direction(), effect.coordinateScale(), effect.magnitude());
        }
    }
}
