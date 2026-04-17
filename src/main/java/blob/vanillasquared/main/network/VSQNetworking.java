package blob.vanillasquared.main.network;

import blob.vanillasquared.main.network.payload.EnchantingBookClickPayload;
import blob.vanillasquared.main.network.payload.EnchantingRecipeBookSyncPayload;
import blob.vanillasquared.main.network.payload.EnchantingRecipeSelectionPayload;
import blob.vanillasquared.main.network.payload.EnchantingRecipeStatePayload;
import blob.vanillasquared.main.network.payload.SpecialEnchantmentHotkeyPayload;
import blob.vanillasquared.main.world.item.components.enchantment.VSQEnchantmentSlots;
import blob.vanillasquared.main.world.inventory.VSQEnchantmentMenu;
import blob.vanillasquared.util.api.modules.components.DataComponents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class VSQNetworking {
    private VSQNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(EnchantingBookClickPayload.TYPE, EnchantingBookClickPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EnchantingRecipeSelectionPayload.TYPE, EnchantingRecipeSelectionPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SpecialEnchantmentHotkeyPayload.TYPE, SpecialEnchantmentHotkeyPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EnchantingRecipeStatePayload.TYPE, EnchantingRecipeStatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EnchantingRecipeBookSyncPayload.TYPE, EnchantingRecipeBookSyncPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EnchantingBookClickPayload.TYPE, (payload, context) ->
                context.server().execute(() -> vsq$handleEnchantingBookClick(payload, context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(EnchantingRecipeSelectionPayload.TYPE, (payload, context) ->
                context.server().execute(() -> vsq$handleEnchantingRecipeSelection(payload, context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(SpecialEnchantmentHotkeyPayload.TYPE, (payload, context) ->
                context.server().execute(() -> vsq$handleSpecialEnchantmentHotkey(context.player()))
        );
    }

    private static void vsq$handleEnchantingBookClick(EnchantingBookClickPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof VSQEnchantmentMenu menu)) {
            return;
        }
        if (menu.containerId != payload.containerId()) {
            return;
        }
        menu.vsq$tryCraftEnchantingRecipe(player);
    }

    private static void vsq$handleEnchantingRecipeSelection(EnchantingRecipeSelectionPayload payload, ServerPlayer player) {
        if (!(player.containerMenu instanceof VSQEnchantmentMenu menu)) {
            return;
        }
        if (menu.containerId != payload.containerId()) {
            return;
        }
        menu.vsq$setSelectedDisplayId(payload.displayId());
    }

    private static void vsq$handleSpecialEnchantmentHotkey(ServerPlayer player) {
        if (vsq$hasSpecialEnchantmentEffect(player)) {
            player.sendSystemMessage(Component.literal(player.getName().getString() + " used a special enchantment effect"));
        }
    }

    private static boolean vsq$hasSpecialEnchantmentEffect(ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            ItemEnchantments enchantments = VSQEnchantmentSlots.aggregate(stack);
            for (Holder<Enchantment> enchantment : enchantments.keySet()) {
                if (VSQEnchantmentSlots.profileEffect(stack, enchantment, DataComponents.SPECIAL_ENCHANTMENT_EFFECT) != null) {
                    return true;
                }
            }
        }
        return false;
    }
}
