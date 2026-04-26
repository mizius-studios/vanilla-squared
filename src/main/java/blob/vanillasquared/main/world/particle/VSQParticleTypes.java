package blob.vanillasquared.main.world.particle;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.main.world.particle.particles.LightningBoltParticleOptions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public final class VSQParticleTypes {
    public static final ParticleType<LightningBoltParticleOptions> LIGHTNING_BOLT = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "lightning_bolt"),
            new ParticleType<>(false) {
                @Override
                public MapCodec<LightningBoltParticleOptions> codec() {
                    return LightningBoltParticleOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, LightningBoltParticleOptions> streamCodec() {
                    return LightningBoltParticleOptions.STREAM_CODEC;
                }
            }
    );

    private VSQParticleTypes() {
    }

    public static void initialize() {
    }
}
