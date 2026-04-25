package blob.vanillasquared.util.api.modules.components;

import blob.vanillasquared.main.world.item.components.enchantment.VSQEnchantmentComponent;
import blob.vanillasquared.main.world.item.components.hitthrough.HitThroughComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public final class DataComponents {
    public static final DataComponentType<VSQEnchantmentComponent> VSQ_ENCHANTMENT = RegisterComponents.enchantmentComponent;
    public static final DataComponentType<HitThroughComponent> HIT_THROUGH = RegisterComponents.hitThroughComponent;
    public static final DataComponentType<ResourceKey<Recipe<?>>> ENCHANT_RECIPE = RegisterComponents.enchantRecipeComponent;

    private DataComponents() {
    }
}
