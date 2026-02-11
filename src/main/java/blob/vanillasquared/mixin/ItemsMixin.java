package blob.vanillasquared.mixin;

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
        switch (string) {
            case "fishing_rod": {
                properties.durability(250);
            }
        }
    }

    @Inject(method = "registerItem(Ljava/lang/String;Lnet/minecraft/world/item/Item$Properties;)Lnet/minecraft/world/item/Item;", at = @At("HEAD"))
    private static void registerItem(String string, Item.Properties properties, CallbackInfoReturnable<Item> cir) {
        switch (string) {
            case "netherite_chestplate": {
                properties.durability(569);
            }
            case "netherite_leggings": {
                properties.durability(569);
            }
            case "netherite_boots": {
                properties.durability(569);
            }
            case "netherite_helmet": {
                properties.durability(569);
            }

            case "diamond_chestplate":  {
                properties.durability(450);
            }
            case "diamond_leggings": {
                properties.durability(450);
            }
            case "diamond_boots": {
                properties.durability(450);
            }
            case "diamond_helmet": {
                properties.durability(450);
            }

            case "iron_chestplate":  {
                properties.durability(325);
            }
            case "iron_leggings": {
                properties.durability(325);
            }
            case "iron_boots": {
                properties.durability(325);
            }
            case "iron_helmet": {
                properties.durability(325);
            }

            case "golden_chestplate":  {
                properties.durability(125);
            }
            case "golden_leggings": {
                properties.durability(125);
            }
            case "golden_boots": {
                properties.durability(125);
            }
            case "golden_helmet": {
                properties.durability(125);
            }

            case "chainmail_chestplate":  {
                properties.durability(300);
            }
            case "chainmail_leggings": {
                properties.durability(300);
            }
            case "chainmail_boots": {
                properties.durability(300);
            }
            case "chainmail_helmet": {
                properties.durability(300);
            }

            case "copper_chestplate":  {
                properties.durability(167);
            }
            case "copper_leggings": {
                properties.durability(167);
            }
            case "copper_boots": {
                properties.durability(167);
            }
            case "copper_helmet": {
                properties.durability(167);
            }

            case "leather_chestplate":  {
                properties.durability(96);
            }
            case "leather_leggings": {
                properties.durability(96);
            }
            case "leather_boots": {
                properties.durability(96);
            }
            case "leather_helmet": {
                properties.durability(96);
            }

            case "turtle_helmet":  {
                properties.durability(1);
            }
        }
    }
}
