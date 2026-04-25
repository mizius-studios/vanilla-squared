package blob.vanillasquared.mixin.world.loot;

import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(LootContext.class)
public interface LootContextAccessor {
    @Accessor("visitedElements")
    Set<LootContext.VisitedEntry<?>> vsq$visitedElements();
}
