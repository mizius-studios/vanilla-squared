package blob.vanillasquared.mixin;

import blob.vanillasquared.util.data.Dura;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
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
    private static final Map<ToolMaterial, Dura> DURABILITY = Map.of(
            ToolMaterial.WOOD, new Dura(75),
            ToolMaterial.STONE, new Dura(150),
            ToolMaterial.COPPER, new Dura(200),
            ToolMaterial.IRON, new Dura(250),
            ToolMaterial.GOLD, new Dura(100),
            ToolMaterial.DIAMOND, new Dura(1550),
            ToolMaterial.NETHERITE, new Dura(2069)
    );

    @Inject(method = "registerItem(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;", at = @At("HEAD"))
    private static void registerItem(String string, Function<Item.Properties, Item> function, Item.Properties properties, CallbackInfoReturnable<Item> cir) {
        if ("fishing_rod".equals(string)) {
            properties.durability(250);
        }
    }

    @Inject(method = "registerItem(Ljava/lang/String;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;", at = @At("HEAD"))
    private static void registerItem(String string, Item.Properties properties, CallbackInfoReturnable<Item> cir) {



//        switch (string) {
//            case "netherite_chestplate", "netherite_leggings", "netherite_boots", "netherite_helmet" ->
//                    properties.durability(569);
//
//            case "diamond_chestplate", "diamond_leggings", "diamond_boots", "diamond_helmet" ->
//                    properties.durability(450);
//
//            case "iron_chestplate", "iron_leggings", "iron_boots", "iron_helmet" ->
//                    properties.durability(325);
//
//            case "golden_chestplate", "golden_leggings", "golden_boots", "golden_helmet" ->
//                    properties.durability(125);
//
//            case "chainmail_chestplate", "chainmail_leggings", "chainmail_boots", "chainmail_helmet" ->
//                    properties.durability(300);
//
//            case "copper_chestplate", "copper_leggings", "copper_boots", "copper_helmet" ->
//                    properties.durability(167);
//
//            case "leather_chestplate", "leather_leggings", "leather_boots", "leather_helmet" ->
//                    properties.durability(96);
//
//            case "turtle_helmet" -> properties.durability(569);
//
//            default -> {
//            }
//        }
    }
}
