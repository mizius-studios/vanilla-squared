package blob.vanillasquared.mixin.world.loot;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(LootContext.class)
public abstract class LootContextMixin {
    @Unique
    private final Deque<LootTable> vsq$lootTableStack = new ArrayDeque<>();

    @Inject(method = "pushVisitedElement", at = @At("RETURN"))
    private void vsq$trackLootTablePush(LootContext.VisitedEntry<?> visitedEntry, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && visitedEntry.type() == LootDataType.TABLE && visitedEntry.value() instanceof LootTable table) {
            this.vsq$lootTableStack.addLast(table);
        }
    }

    @Inject(method = "popVisitedElement", at = @At("HEAD"))
    private void vsq$trackLootTablePop(LootContext.VisitedEntry<?> visitedEntry, CallbackInfo ci) {
        if (visitedEntry.type() == LootDataType.TABLE
                && visitedEntry.value() instanceof LootTable table
                && !this.vsq$lootTableStack.isEmpty()
                && this.vsq$lootTableStack.peekLast() == table) {
            this.vsq$lootTableStack.removeLast();
        }
    }
}
