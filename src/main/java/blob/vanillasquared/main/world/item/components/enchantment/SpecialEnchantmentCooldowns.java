package blob.vanillasquared.main.world.item.components.enchantment;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.main.network.payload.SpecialEnchantmentCooldownPayload;
import blob.vanillasquared.util.api.modules.components.DataComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SpecialEnchantmentCooldowns {
    private static final Map<UUID, Map<Identifier, CooldownEntry>> COOLDOWNS = new HashMap<>();

    private SpecialEnchantmentCooldowns() {
    }

    public static Optional<SpecialEnchantmentUse> selectUsable(ServerPlayer player) {
        logInvalidSlots(player);

        Optional<SpecialEnchantmentUse> mainhand = findSpecial(player.getMainHandItem(), EquipmentSlot.MAINHAND);
        if (mainhand.isPresent() && !isOnCooldown(player, mainhand.get().enchantmentId())) {
            return mainhand;
        }

        Optional<SpecialEnchantmentUse> offhand = findSpecial(player.getOffhandItem(), EquipmentSlot.OFFHAND);
        if (offhand.isPresent() && !isOnCooldown(player, offhand.get().enchantmentId())) {
            return offhand;
        }

        mainhand.ifPresent(use -> sync(player, use.enchantmentId()));
        offhand.ifPresent(use -> sync(player, use.enchantmentId()));
        return Optional.empty();
    }

    public static void start(ServerPlayer player, SpecialEnchantmentUse use) {
        long totalTicks = use.effect().cooldownTicks();
        if (totalTicks <= 0L) {
            clear(player, use.enchantmentId());
            return;
        }

        long now = player.level().getGameTime();
        COOLDOWNS.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>())
                .put(use.enchantmentId(), new CooldownEntry(now + totalTicks, totalTicks));
        sync(player, use.enchantmentId());
    }

    public static void clear(ServerPlayer player) {
        COOLDOWNS.remove(player.getUUID());
    }

    private static boolean isOnCooldown(ServerPlayer player, Identifier enchantmentId) {
        return remainingTicks(player, enchantmentId) > 0L;
    }

    private static long remainingTicks(ServerPlayer player, Identifier enchantmentId) {
        Map<Identifier, CooldownEntry> playerCooldowns = COOLDOWNS.get(player.getUUID());
        if (playerCooldowns == null) {
            return 0L;
        }

        CooldownEntry entry = playerCooldowns.get(enchantmentId);
        if (entry == null) {
            return 0L;
        }

        long remaining = entry.endTick() - player.level().getGameTime();
        if (remaining <= 0L) {
            playerCooldowns.remove(enchantmentId);
            if (playerCooldowns.isEmpty()) {
                COOLDOWNS.remove(player.getUUID());
            }
            return 0L;
        }
        return remaining;
    }

    private static void sync(ServerPlayer player, Identifier enchantmentId) {
        Map<Identifier, CooldownEntry> playerCooldowns = COOLDOWNS.get(player.getUUID());
        CooldownEntry entry = playerCooldowns == null ? null : playerCooldowns.get(enchantmentId);
        long remaining = remainingTicks(player, enchantmentId);
        long total = entry == null ? 0L : entry.totalTicks();
        ServerPlayNetworking.send(player, new SpecialEnchantmentCooldownPayload(enchantmentId, remaining, total));
    }

    private static void clear(ServerPlayer player, Identifier enchantmentId) {
        Map<Identifier, CooldownEntry> playerCooldowns = COOLDOWNS.get(player.getUUID());
        if (playerCooldowns != null) {
            playerCooldowns.remove(enchantmentId);
            if (playerCooldowns.isEmpty()) {
                COOLDOWNS.remove(player.getUUID());
            }
        }
        ServerPlayNetworking.send(player, new SpecialEnchantmentCooldownPayload(enchantmentId, 0L, 0L));
    }

    private static Optional<SpecialEnchantmentUse> findSpecial(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        ItemEnchantments enchantments = VSQEnchantmentSlots.aggregate(stack);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            SpecialEnchantmentEffect effect = VSQEnchantmentSlots.profileEffect(stack, enchantment, DataComponents.SPECIAL_ENCHANTMENT_EFFECT);
            if (effect != null && matchesSlot(stack, enchantment, slot)) {
                Optional<ResourceKey<Enchantment>> key = enchantment.unwrapKey();
                if (key.isPresent()) {
                    return Optional.of(new SpecialEnchantmentUse(key.get().identifier(), effect));
                }
            }
        }
        return Optional.empty();
    }

    private static void logInvalidSlots(ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
                continue;
            }
            logInvalidSlot(player, player.getItemBySlot(slot), slot.getName());
        }

        if (player.getVehicle() instanceof AbstractHorse horse) {
            logInvalidSlot(player, horse.getItemBySlot(EquipmentSlot.BODY), "mount body");
            logInvalidSlot(player, horse.getItemBySlot(EquipmentSlot.SADDLE), "mount saddle");
        }
    }

    private static void logInvalidSlot(ServerPlayer player, ItemStack stack, String slotName) {
        findSpecialIgnoringSlot(stack).ifPresent(use -> VanillaSquared.LOGGER.error(
                "Player {} has special enchantment effect {} in {}; special enchantment effects only work in mainhand/offhand",
                player.getName().getString(),
                use.enchantmentId(),
                slotName
        ));
    }

    private static Optional<SpecialEnchantmentUse> findSpecialIgnoringSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        ItemEnchantments enchantments = VSQEnchantmentSlots.aggregate(stack);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            SpecialEnchantmentEffect effect = VSQEnchantmentSlots.profileEffect(stack, enchantment, DataComponents.SPECIAL_ENCHANTMENT_EFFECT);
            if (effect != null) {
                Optional<ResourceKey<Enchantment>> key = enchantment.unwrapKey();
                if (key.isPresent()) {
                    return Optional.of(new SpecialEnchantmentUse(key.get().identifier(), effect));
                }
            }
        }
        return Optional.empty();
    }

    private static boolean matchesSlot(ItemStack stack, Holder<Enchantment> enchantment, EquipmentSlot slot) {
        return VSQEnchantmentSlots.selectedProfile(stack, enchantment)
                .map(profile -> profile.slots().stream().anyMatch(group -> group.test(slot)))
                .orElseGet(() -> enchantment.value().matchingSlot(slot));
    }

    private record CooldownEntry(long endTick, long totalTicks) {
    }

    public record SpecialEnchantmentUse(Identifier enchantmentId, SpecialEnchantmentEffect effect) {
    }
}
