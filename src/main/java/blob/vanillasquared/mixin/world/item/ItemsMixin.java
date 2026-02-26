package blob.vanillasquared.mixin.world.item;

import blob.vanillasquared.util.builder.components.BlockBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(Items.class)
public class ItemsMixin {
    private static final BlockBuilder SHIELD_BLOCK_COMPONENT = new BlockBuilder(1.0F, 1.0F, 0.82F, 3.0F);

    @Inject(method = "registerItem(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;", at = @At("HEAD"))
    private static void registerItem(String itemName, Function<Item.Properties, Item> factory, Item.Properties properties, CallbackInfoReturnable<Item> cir) {
        switch (itemName) {
            case "fishing_rod" -> properties.durability(250);
            case "potion" -> properties.stacksTo(16);
            case "splash_potion", "lingering_potion" -> properties.stacksTo(8);
            case "shield" -> properties.component(DataComponents.BLOCKS_ATTACKS, SHIELD_BLOCK_COMPONENT.build());
            default -> {
            }
        }
    }
}
