package blob.vanillasquared.mixin.world;

import blob.vanillasquared.main.world.VSQExperiments;
import com.mojang.serialization.Codec;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FeatureFlags.class)
public abstract class FeatureFlagsMixin {

    @Shadow
    @Final
    @Mutable
    public static FeatureFlag VANILLA;
    @Shadow
    @Final
    @Mutable
    public static FeatureFlag TRADE_REBALANCE;
    @Shadow
    @Final
    @Mutable
    public static FeatureFlag REDSTONE_EXPERIMENTS;
    @Shadow
    @Final
    @Mutable
    public static FeatureFlag MINECART_IMPROVEMENTS;
    @Shadow
    @Final
    @Mutable
    public static FeatureFlagRegistry REGISTRY;
    @Shadow
    @Final
    @Mutable
    public static Codec<FeatureFlagSet> CODEC;
    @Shadow
    @Final
    @Mutable
    public static FeatureFlagSet VANILLA_SET;
    @Shadow
    @Final
    @Mutable
    public static FeatureFlagSet DEFAULT_FLAGS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void vsq$registerPreviewFeatureFlag(CallbackInfo ci) {
        FeatureFlagRegistry.Builder builder = new FeatureFlagRegistry.Builder("main");

        VANILLA = builder.createVanilla("vanilla");
        TRADE_REBALANCE = builder.createVanilla("trade_rebalance");
        REDSTONE_EXPERIMENTS = builder.createVanilla("redstone_experiments");
        MINECART_IMPROVEMENTS = builder.createVanilla("minecart_improvements");
        FeatureFlag previewFlag = builder.create(VSQExperiments.PREVIEW_FEATURE_ID);

        REGISTRY = builder.build();
        CODEC = REGISTRY.codec();
        VANILLA_SET = FeatureFlagSet.of(VANILLA);
        DEFAULT_FLAGS = VANILLA_SET;

        VSQExperiments.vsq$setPreviewFlag(previewFlag);
    }
}
