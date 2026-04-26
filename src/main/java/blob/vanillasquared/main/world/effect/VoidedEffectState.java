package blob.vanillasquared.main.world.effect;

import blob.vanillasquared.main.world.particle.VSQParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

public final class VoidedEffectState {
    private static final int ACTIVE_CLOUD_PARTICLE_INTERVAL = 14;
    private static final int ACTIVE_PIXEL_PARTICLE_INTERVAL = 3;
    private static final int INCREMENT_CLOUD_PARTICLE_COUNT = 5;
    private static final int INFINITE_DURATION_INCREMENT_INTERVAL = 100;
    private static final Map<LivingEntity, State> STATES = new WeakHashMap<>();

    private VoidedEffectState() {
    }

    public static void tick(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        MobEffectInstance effect = entity.getEffect(VSQMobEffects.VOIDED);
        if (effect == null) {
            clear(entity);
            return;
        }

        State state = STATES.get(entity);
        if (state == null) {
            refresh(entity, effect);
            return;
        }

        if (effect.isVisible()) {
            spawnActiveParticles(serverLevel, entity);
        }

        if (state.multiplier >= state.maxMultiplier) {
            return;
        }

        state.ticksUntilNextIncrement--;
        if (state.ticksUntilNextIncrement > 0) {
            return;
        }

        state.multiplier = Math.min(state.multiplier + 0.1F, state.maxMultiplier);
        state.ticksUntilNextIncrement = state.incrementInterval;
        if (effect.isVisible()) {
            spawnBurst(serverLevel, entity);
        }
    }

    public static void refresh(LivingEntity entity, MobEffectInstance effect) {
        int initialDuration = effect.getDuration();
        int stepCount = effect.getAmplifier() + 1;
        int incrementInterval = initialDuration < 0
                ? INFINITE_DURATION_INCREMENT_INTERVAL
                : Math.max(initialDuration / stepCount, 1);
        float maxMultiplier = 1.0F + (stepCount * 0.1F);
        float previousMultiplier = getMultiplier(entity);
        float multiplier = Math.min(Math.max(1.1F, previousMultiplier), maxMultiplier);

        STATES.put(entity, new State(multiplier, incrementInterval, incrementInterval, maxMultiplier));

        if (entity.level() instanceof ServerLevel serverLevel && multiplier > previousMultiplier && effect.isVisible()) {
            spawnBurst(serverLevel, entity);
        }
    }

    public static float consume(LivingEntity entity) {
        State state = STATES.remove(entity);
        if (state == null) {
            return 1.0F;
        }
        return state.multiplier;
    }

    public static void clear(LivingEntity entity) {
        STATES.remove(entity);
    }

    public static void clearRemoved(LivingEntity entity, Collection<MobEffectInstance> effects) {
        for (MobEffectInstance effect : effects) {
            if (effect.is(VSQMobEffects.VOIDED)) {
                clear(entity);
                return;
            }
        }
    }

    public static float getMultiplier(LivingEntity entity) {
        State state = STATES.get(entity);
        return state == null ? 1.0F : state.multiplier;
    }

    private static void spawnBurst(ServerLevel level, LivingEntity entity) {
        level.sendParticles(
                VSQParticleTypes.VOID_CLOUD,
                entity.getX(),
                entity.getY(0.18),
                entity.getZ(),
                INCREMENT_CLOUD_PARTICLE_COUNT,
                entity.getBbWidth() * 0.24,
                entity.getBbHeight() * 0.14,
                entity.getBbWidth() * 0.24,
                0.01
        );
    }

    private static void spawnActiveParticles(ServerLevel level, LivingEntity entity) {
        if (entity.tickCount % ACTIVE_CLOUD_PARTICLE_INTERVAL == 0) {
            level.sendParticles(
                    VSQParticleTypes.VOID_CLOUD,
                    entity.getX(),
                    entity.getY(0.08),
                    entity.getZ(),
                    1,
                    entity.getBbWidth() * 0.14,
                    entity.getBbHeight() * 0.12,
                    entity.getBbWidth() * 0.14,
                    0.006
            );
        }

        if (entity.tickCount % ACTIVE_PIXEL_PARTICLE_INTERVAL == 0) {
            level.sendParticles(
                    VSQParticleTypes.VOID_PIXEL,
                    entity.getX(),
                    entity.getY(0.5),
                    entity.getZ(),
                    1,
                    entity.getBbWidth() * 0.55,
                    entity.getBbHeight() * 0.4,
                    entity.getBbWidth() * 0.55,
                    0.015
            );
        }
    }

    private static final class State {
        private float multiplier;
        private final int incrementInterval;
        private int ticksUntilNextIncrement;
        private final float maxMultiplier;

        private State(float multiplier, int incrementInterval, int ticksUntilNextIncrement, float maxMultiplier) {
            this.multiplier = multiplier;
            this.incrementInterval = incrementInterval;
            this.ticksUntilNextIncrement = ticksUntilNextIncrement;
            this.maxMultiplier = maxMultiplier;
        }
    }
}
