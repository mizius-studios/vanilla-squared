package blob.vanillasquared.mixin.world.item;

import blob.vanillasquared.util.api.references.ArmorKeys;
import blob.vanillasquared.util.data.Dura;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Function;

@Mixin(Items.class)
public class ItemsMixin {

    @Unique
    private static final Map<ArmorKeys, Dura> DURABILITY = Map.of(
            ArmorKeys.LEATHER_ARMOR, new Dura(96),
            ArmorKeys.COPPER_ARMOR, new Dura(167),
            ArmorKeys.CHAINMAIL_ARMOR, new Dura(300),
            ArmorKeys.IRON_ARMOR, new Dura(325),
            ArmorKeys.GOLDEN_ARMOR, new Dura(125),
            ArmorKeys.DIAMOND_ARMOR, new Dura(450),
            ArmorKeys.NETHERITE_ARMOR, new Dura(569),
            ArmorKeys.TURTLE_ARMOR, new Dura(569)
    );

    @Inject(method = "registerItem(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;", at = @At("HEAD"))
    private static void registerItem(String string, Function<Item.Properties, Item> function, Item.Properties properties, CallbackInfoReturnable<Item> cir) {
        if ("fishing_rod".equals(string)) {
            properties.durability(250);
        }
    }

    @Inject(method = "registerItem(Ljava/lang/String;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;", at = @At("HEAD"))
    private static void registerItem(String string, Item.Properties properties, CallbackInfoReturnable<Item> cir) {
        // Dura durability = DURABILITY.getOrDefault(, Dura.DEFAULT);
        // properties.durability(durability.dura());
    }
}
