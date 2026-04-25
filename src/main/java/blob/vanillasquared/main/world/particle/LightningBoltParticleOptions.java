package blob.vanillasquared.main.world.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record LightningBoltParticleOptions(double yaw, double pitch, int variant) implements ParticleOptions {
    public static final LightningBoltParticleOptions DEFAULT = new LightningBoltParticleOptions(0.0D, 0.0D, 0);
    public static final int MIN_VARIANT = 0;
    public static final int MAX_VARIANT = 3;

    public static final MapCodec<LightningBoltParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            rotationCodec().optionalFieldOf("rotation", List.of(DEFAULT.yaw(), DEFAULT.pitch())).forGetter(LightningBoltParticleOptions::rotationValues),
            variantCodec().optionalFieldOf("variant", DEFAULT.variant()).forGetter(LightningBoltParticleOptions::variant)
    ).apply(instance, LightningBoltParticleOptions::fromCodecValues));

    public static final StreamCodec<ByteBuf, LightningBoltParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE,
            LightningBoltParticleOptions::yaw,
            ByteBufCodecs.DOUBLE,
            LightningBoltParticleOptions::pitch,
            ByteBufCodecs.VAR_INT,
            LightningBoltParticleOptions::variant,
            LightningBoltParticleOptions::new
    );

    public LightningBoltParticleOptions {
        validateVariant(variant).error().ifPresent(error -> {
            throw new IllegalArgumentException(error.message());
        });
    }

    @Override
    public ParticleType<?> getType() {
        return VSQParticleTypes.LIGHTNING_BOLT;
    }

    public List<Double> rotationValues() {
        return List.of(this.yaw, this.pitch);
    }

    public static DataResult<Integer> validateVariant(int variant) {
        if (variant < MIN_VARIANT || variant > MAX_VARIANT) {
            return DataResult.error(() -> "Lightning bolt particle variant must be between " + MIN_VARIANT + " and " + MAX_VARIANT + ", got " + variant);
        }
        return DataResult.success(variant);
    }

    private static Codec<Integer> variantCodec() {
        return Codec.INT.comapFlatMap(LightningBoltParticleOptions::validateVariant, value -> value);
    }

    private static Codec<List<Double>> rotationCodec() {
        return Codec.DOUBLE.listOf().comapFlatMap(values -> {
            if (values.size() != 2) {
                return DataResult.error(() -> "Lightning bolt particle rotation must contain exactly 2 doubles [yaw, pitch], got " + values.size());
            }
            return DataResult.success(values);
        }, values -> values);
    }

    private static LightningBoltParticleOptions fromCodecValues(List<Double> rotation, int variant) {
        return new LightningBoltParticleOptions(rotation.get(0), rotation.get(1), variant);
    }
}
