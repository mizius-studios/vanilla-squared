package blob.vanillasquared.mixin.world.item;

import blob.vanillasquared.util.data.BlockComponent;
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

    @Inject(method = "registerItem(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;", at = @At("HEAD"))
    private static void registerItem(String string, Function<Item.Properties, Item> function, Item.Properties properties, CallbackInfoReturnable<Item> cir) {
        BlockComponent shieldBlockComponent = new BlockComponent(1.0f, 1.0f, 0.82f, 3.0f);
        switch(string) {
            case "fishing_rod": properties.durability(250); break;
            case "potion": properties.stacksTo(16); break;
            case "splash_potion", "lingering_potion": properties.stacksTo(8); break;
            case "shield": properties.component(DataComponents.BLOCKS_ATTACKS, shieldBlockComponent.build()); break;
        }
    }
}
