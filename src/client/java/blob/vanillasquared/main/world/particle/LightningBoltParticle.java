package blob.vanillasquared.main.world.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class LightningBoltParticle extends Particle {
    private static final float LIGHTNING_SCALE = 1.0F / 16.0F;

    private final long seed;
    private final float yaw;
    private final float pitch;
    private final int variant;

    protected LightningBoltParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, LightningBoltParticleOptions options) {
        super(level, x, y, z, xd, yd, zd);
        this.seed = this.random.nextLong();
        this.yaw = (float) options.yaw();
        this.pitch = (float) options.pitch();
        this.variant = options.variant();
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.lifetime = 2;
        this.setSize(1.0F, 1.0F);
    }

    @Override
    public ParticleRenderType getGroup() {
        return VSQParticleRenderTypes.LIGHTNING_BOLT;
    }

    public ParticleGroupRenderState createRenderState(Camera camera, float tickDelta) {
        var cameraPosition = camera.position();
        float renderX = (float) (Mth.lerp(tickDelta, this.xo, this.x) - cameraPosition.x);
        float renderY = (float) (Mth.lerp(tickDelta, this.yo, this.y) - cameraPosition.y);
        float renderZ = (float) (Mth.lerp(tickDelta, this.zo, this.z) - cameraPosition.z);

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(renderX, renderY, renderZ);
        poseStack.mulPose(new Quaternionf().rotationY(this.yaw * Mth.DEG_TO_RAD));
        poseStack.mulPose(new Quaternionf().rotationX(this.pitch * Mth.DEG_TO_RAD));
        poseStack.scale(LIGHTNING_SCALE, LIGHTNING_SCALE, LIGHTNING_SCALE);
        return new LightningBoltParticleRenderState(poseStack, this.seed, this.variant);
    }

    private static void renderBolt(Matrix4f matrix, VertexConsumer consumer, long seed) {
        float[] xs = new float[8];
        float[] zs = new float[8];
        float x = 0.0F;
        float z = 0.0F;
        RandomSource random = RandomSource.create(seed);

        for (int index = 7; index >= 0; --index) {
            xs[index] = x;
            zs[index] = z;
            x += random.nextInt(11) - 5;
            z += random.nextInt(11) - 5;
        }

        for (int layer = 0; layer < 4; ++layer) {
            RandomSource branchRandom = RandomSource.create(seed);

            for (int branch = 0; branch < 3; ++branch) {
                int start = 7;
                int end = 0;
                if (branch > 0) {
                    start = 7 - branch;
                    end = start - 2;
                }

                float prevX = xs[start] - x;
                float prevZ = zs[start] - z;

                for (int segment = start; segment >= end; --segment) {
                    float nextPrevX = prevX;
                    float nextPrevZ = prevZ;
                    if (branch == 0) {
                        prevX += branchRandom.nextInt(11) - 5;
                        prevZ += branchRandom.nextInt(11) - 5;
                    } else {
                        prevX += branchRandom.nextInt(31) - 15;
                        prevZ += branchRandom.nextInt(31) - 15;
                    }

                    float innerRadius = 0.1F + layer * 0.2F;
                    if (branch == 0) {
                        innerRadius *= segment * 0.1F + 1.0F;
                    }

                    float outerRadius = 0.1F + layer * 0.2F;
                    if (branch == 0) {
                        outerRadius *= (segment - 1.0F) * 0.1F + 1.0F;
                    }

                    quad(matrix, consumer, prevX, prevZ, segment, nextPrevX, nextPrevZ, 0.45F, 0.45F, 0.5F, innerRadius, outerRadius, false, false, true, false);
                    quad(matrix, consumer, prevX, prevZ, segment, nextPrevX, nextPrevZ, 0.45F, 0.45F, 0.5F, innerRadius, outerRadius, true, false, true, true);
                    quad(matrix, consumer, prevX, prevZ, segment, nextPrevX, nextPrevZ, 0.45F, 0.45F, 0.5F, innerRadius, outerRadius, true, true, false, true);
                    quad(matrix, consumer, prevX, prevZ, segment, nextPrevX, nextPrevZ, 0.45F, 0.45F, 0.5F, innerRadius, outerRadius, false, true, false, false);
                }
            }
        }
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer, float x1, float z1, int y, float x2, float z2, float red, float green, float blue, float innerRadius, float outerRadius, boolean x1Positive, boolean z1Positive, boolean x2Positive, boolean z2Positive) {
        consumer.addVertex(matrix, x1 + (x1Positive ? outerRadius : -outerRadius), y * 16.0F, z1 + (z1Positive ? outerRadius : -outerRadius))
                .setColor(red, green, blue, 0.3F);
        consumer.addVertex(matrix, x2 + (x1Positive ? innerRadius : -innerRadius), (y + 1) * 16.0F, z2 + (z1Positive ? innerRadius : -innerRadius))
                .setColor(red, green, blue, 0.3F);
        consumer.addVertex(matrix, x2 + (x2Positive ? innerRadius : -innerRadius), (y + 1) * 16.0F, z2 + (z2Positive ? innerRadius : -innerRadius))
                .setColor(red, green, blue, 0.3F);
        consumer.addVertex(matrix, x1 + (x2Positive ? outerRadius : -outerRadius), y * 16.0F, z1 + (z2Positive ? outerRadius : -outerRadius))
                .setColor(red, green, blue, 0.3F);
    }

    public static class Provider implements ParticleProvider<LightningBoltParticleOptions> {
        @Override
        public Particle createParticle(LightningBoltParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            return new LightningBoltParticle(level, x, y, z, xd, yd, zd, options);
        }
    }

    public record LightningBoltParticleRenderState(PoseStack poseStack, long seed, int variant) implements ParticleGroupRenderState {
        @Override
        public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
            submitNodeCollector.submitCustomGeometry(this.poseStack, RenderTypes.lightning(), (pose, consumer) -> renderBolt(pose.pose(), consumer, this.seed + this.variant));
        }
    }
}
