package blob.vanillasquared.main.gui.hud;

import blob.vanillasquared.main.world.item.components.enchantment.SpecialEnchantmentEffect;
import blob.vanillasquared.main.world.item.components.enchantment.VSQEnchantmentSlots;
import blob.vanillasquared.util.api.modules.components.DataComponents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SpecialEnchantmentCooldownClientState {
    private static final Map<Identifier, CooldownEntry> COOLDOWNS = new HashMap<>();

    private SpecialEnchantmentCooldownClientState() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    public static void apply(Identifier enchantmentId, long remainingTicks, long totalTicks) {
        if (remainingTicks <= 0L || totalTicks <= 0L) {
            COOLDOWNS.remove(enchantmentId);
            return;
        }
        COOLDOWNS.put(enchantmentId, new CooldownEntry(remainingTicks, totalTicks));
    }

    public static void clear() {
        COOLDOWNS.clear();
    }

    public static Optional<VisibleCooldown> visibleCooldown(LocalPlayer player) {
        if (player == null) {
            return Optional.empty();
        }

        Optional<Identifier> mainhand = findSpecialEnchantmentId(player.getMainHandItem(), EquipmentSlot.MAINHAND);
        if (mainhand.isPresent()) {
            return visibleCooldown(mainhand.get());
        }

        Optional<Identifier> offhand = findSpecialEnchantmentId(player.getOffhandItem(), EquipmentSlot.OFFHAND);
        return offhand.flatMap(SpecialEnchantmentCooldownClientState::visibleCooldown);
    }

    public static boolean hasVisibleCooldown(LocalPlayer player) {
        return visibleCooldown(player).isPresent();
    }

    private static Optional<VisibleCooldown> visibleCooldown(Identifier enchantmentId) {
        CooldownEntry entry = COOLDOWNS.get(enchantmentId);
        if (entry == null || entry.remainingTicks() <= 0L || entry.totalTicks() <= 0L) {
            return Optional.empty();
        }
        return Optional.of(new VisibleCooldown(entry.remainingTicks(), entry.totalTicks()));
    }

    private static Optional<Identifier> findSpecialEnchantmentId(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        ItemEnchantments enchantments = VSQEnchantmentSlots.aggregate(stack);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            SpecialEnchantmentEffect effect = VSQEnchantmentSlots.profileEffect(stack, enchantment, DataComponents.SPECIAL_ENCHANTMENT_EFFECT);
            if (effect == null || !matchesSlot(stack, enchantment, slot)) {
                continue;
            }

            Optional<ResourceKey<Enchantment>> key = enchantment.unwrapKey();
            if (key.isPresent()) {
                return Optional.of(key.get().identifier());
            }
        }
        return Optional.empty();
    }

    private static boolean matchesSlot(ItemStack stack, Holder<Enchantment> enchantment, EquipmentSlot slot) {
        return VSQEnchantmentSlots.selectedProfile(stack, enchantment)
                .map(profile -> profile.slots().stream().anyMatch(group -> group.test(slot)))
                .orElseGet(() -> enchantment.value().matchingSlot(slot));
    }

    private static void tick() {
        COOLDOWNS.entrySet().removeIf(entry -> entry.getValue().remainingTicks <= 1L);
        COOLDOWNS.replaceAll((id, entry) -> new CooldownEntry(entry.remainingTicks() - 1L, entry.totalTicks()));
    }

    private record CooldownEntry(long remainingTicks, long totalTicks) {
    }

    public record VisibleCooldown(long remainingTicks, long totalTicks) {
        public int progressWidth() {
            if (this.totalTicks <= 0L) {
                return 0;
            }
            double elapsed = (double) (this.totalTicks - this.remainingTicks) / (double) this.totalTicks;
            return Math.max(0, Math.min(182, (int) Math.round(elapsed * 182.0D)));
        }

        public String displayText() {
            long seconds = Math.max(1L, (this.remainingTicks + 19L) / 20L);
            return seconds + "s";
        }
    }
}
