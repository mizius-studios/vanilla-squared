package blob.vanillasquared.mixin.world.loot;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;

@Mixin(LootContext.class)
public interface LootContextAccessor {
    @Accessor("vsq$lootTableStack")
    Deque<LootTable> vsq$lootTableStack();
}
