package blob.vanillasquared.main.world.item.components.enchantment;

import blob.vanillasquared.main.VanillaSquared;
import blob.vanillasquared.main.network.payload.SpecialEnchantmentCooldownPayload;
import blob.vanillasquared.util.api.modules.components.DataComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class SpecialEnchantmentCooldowns {
    private static final double CLOSEST_ENTITY_RANGE = 16.0D;
    private static final ContextKeySet SPECIAL_PREDICATE_PARAMS = new ContextKeySet.Builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ENCHANTMENT_LEVEL)
            .required(LootContextParams.ORIGIN)
            .optional(LootContextParams.ATTACKING_ENTITY)
            .optional(LootContextParams.DIRECT_ATTACKING_ENTITY)
            .build();
    private static final Map<UUID, Map<Identifier, ActivationState>> STATES = new HashMap<>();

    private SpecialEnchantmentCooldowns() {
    }

    public static Optional<SpecialEnchantmentUse> selectUsable(ServerPlayer player) {
        logInvalidSlots(player);

        Optional<SpecialEnchantmentUse> mainhand = findSpecial(player.getMainHandItem(), EquipmentSlot.MAINHAND);
        if (mainhand.isPresent() && canProcess(player, mainhand.get())) {
            return mainhand;
        }

        Optional<SpecialEnchantmentUse> offhand = findSpecial(player.getOffhandItem(), EquipmentSlot.OFFHAND);
        if (offhand.isPresent() && canProcess(player, offhand.get())) {
            return offhand;
        }

        mainhand.ifPresent(use -> sync(player, use));
        offhand.ifPresent(use -> sync(player, use));
        return Optional.empty();
    }

    public static void processHotkey(ServerPlayer player, SpecialEnchantmentUse use) {
        ActivationState state = state(player, use);
        if (state == null) {
            if (!matches(use.effect().requirements(), player, player, player, use.level())) {
                VanillaSquared.LOGGER.debug("Special enchantment {} skipped: top-level requirements failed on activation for player {}", use.enchantmentId(), player.getName().getString());
                return;
            }
            state = createState(use);
            putState(player, use.enchantmentId(), state);
            runFirstActivation(player, use, state);
        } else {
            if (!matches(use.effect().requirements(), player, player, player, use.level())) {
                VanillaSquared.LOGGER.debug("Special enchantment {} skipped: top-level requirements failed on hotkey for player {}", use.enchantmentId(), player.getName().getString());
                sync(player, use);
                return;
            }
            processHotkeyActivation(player, use, state);
        }

        updateCooldownStart(player, use, state);
        if (!state.cooldownStarted && !isBlocked(use.effect(), state)) {
            removeState(player, use.enchantmentId());
        }
        sync(player, use);
    }

    public static void tickPlayer(ServerPlayer player) {
        Map<Identifier, ActivationState> playerStates = STATES.get(player.getUUID());
        if (playerStates == null || playerStates.isEmpty()) {
            return;
        }

        Set<Identifier> enchantmentIds = Set.copyOf(playerStates.keySet());
        for (Identifier enchantmentId : enchantmentIds) {
            ActivationState state = playerStates.get(enchantmentId);
            if (state == null) {
                continue;
            }

            if (isFinished(player, state)) {
                playerStates.remove(enchantmentId);
                continue;
            }

            Optional<SpecialEnchantmentUse> use = findUseInHands(player, enchantmentId);
            if (use.isEmpty()) {
                continue;
            }

            SpecialEnchantmentUse specialUse = use.get();
            if (!matches(specialUse.effect().requirements(), player, player, player, specialUse.level())) {
                sync(player, specialUse);
                continue;
            }

            runTickLoops(player, specialUse, state);
            sync(player, specialUse);
        }

        if (playerStates.isEmpty()) {
            STATES.remove(player.getUUID());
        }
    }

    public static void clear(ServerPlayer player) {
        STATES.remove(player.getUUID());
    }

    private static boolean canProcess(ServerPlayer player, SpecialEnchantmentUse use) {
        ActivationState state = state(player, use);
        if (state == null) {
            return true;
        }
        return hasHotkeyWork(state, use.effect());
    }

    private static ActivationState state(ServerPlayer player, SpecialEnchantmentUse use) {
        Map<Identifier, ActivationState> playerStates = STATES.get(player.getUUID());
        if (playerStates == null) {
            return null;
        }

        ActivationState state = playerStates.get(use.enchantmentId());
        if (state == null) {
            return null;
        }

        if (isFinished(player, state)) {
            playerStates.remove(use.enchantmentId());
            if (playerStates.isEmpty()) {
                STATES.remove(player.getUUID());
            }
            return null;
        }
        return state;
    }

    private static void putState(ServerPlayer player, Identifier enchantmentId, ActivationState state) {
        STATES.computeIfAbsent(player.getUUID(), uuid -> new HashMap<>()).put(enchantmentId, state);
    }

    private static void removeState(ServerPlayer player, Identifier enchantmentId) {
        Map<Identifier, ActivationState> playerStates = STATES.get(player.getUUID());
        if (playerStates != null) {
            playerStates.remove(enchantmentId);
            if (playerStates.isEmpty()) {
                STATES.remove(player.getUUID());
            }
        }
    }

    private static ActivationState createState(SpecialEnchantmentUse use) {
        ActivationState state = new ActivationState(use.effect().cooldownTicks());
        for (int index = 0; index < use.effect().effects().size(); index++) {
            SpecialEnchantmentEffect.Action action = use.effect().effects().get(index);
            if (action.hasFiniteLimit()) {
                state.remainingLimits.put(index, action.limit());
                state.totalLimits.put(index, action.limit());
            }
            if (action.loop().orElse(null) == SpecialEnchantmentEffect.Loop.FIRST_ACTIVATION_TOGGLE) {
                state.activeLoops.add(index);
            }
        }
        return state;
    }

    private static void runFirstActivation(ServerPlayer player, SpecialEnchantmentUse use, ActivationState state) {
        for (int index = 0; index < use.effect().effects().size(); index++) {
            SpecialEnchantmentEffect.Action action = use.effect().effects().get(index);
            SpecialEnchantmentEffect.Loop loop = action.loop().orElse(null);
            if (loop == SpecialEnchantmentEffect.Loop.FIRST_ACTIVATION) {
                runAction(player, use, action);
                continue;
            }
            if (action.trigger() == SpecialEnchantmentEffect.Trigger.FIRST_ACTIVATION && consumeLimit(state, index, action)) {
                runAction(player, use, action);
            }
        }
    }

    private static void processHotkeyActivation(ServerPlayer player, SpecialEnchantmentUse use, ActivationState state) {
        for (int index = 0; index < use.effect().effects().size(); index++) {
            SpecialEnchantmentEffect.Action action = use.effect().effects().get(index);
            if (action.trigger() != SpecialEnchantmentEffect.Trigger.ENCHANTMENT_EFFECT_HOTKEY) {
                continue;
            }

            SpecialEnchantmentEffect.Loop loop = action.loop().orElse(null);
            if (loop == SpecialEnchantmentEffect.Loop.TOGGLE) {
                if (state.activeLoops.remove(index)) {
                    continue;
                }
                if (consumeLimit(state, index, action)) {
                    state.activeLoops.add(index);
                    runAction(player, use, action);
                }
            } else if (loop == SpecialEnchantmentEffect.Loop.FIRST_ACTIVATION_TOGGLE) {
                state.activeLoops.remove(index);
                consumeLimit(state, index, action);
                runAction(player, use, action);
            } else if (consumeLimit(state, index, action)) {
                runAction(player, use, action);
            }
        }
    }

    private static void runTickLoops(ServerPlayer player, SpecialEnchantmentUse use, ActivationState state) {
        if (!state.cooldownStarted) {
            return;
        }

        for (int index = 0; index < use.effect().effects().size(); index++) {
            SpecialEnchantmentEffect.Action action = use.effect().effects().get(index);
            if (action.loop().orElse(null) == SpecialEnchantmentEffect.Loop.FIRST_ACTIVATION) {
                runAction(player, use, action);
            }
        }
    }

    private static boolean consumeLimit(ActivationState state, int index, SpecialEnchantmentEffect.Action action) {
        if (action.limit() == 0) {
            return false;
        }
        if (action.limit() < 0 || action.loop().orElse(null) == SpecialEnchantmentEffect.Loop.FIRST_ACTIVATION) {
            return true;
        }

        int remaining = state.remainingLimits.getOrDefault(index, action.limit());
        if (remaining <= 0) {
            return false;
        }
        state.remainingLimits.put(index, remaining - 1);
        return true;
    }

    private static boolean hasHotkeyWork(ActivationState state, SpecialEnchantmentEffect effect) {
        for (int index = 0; index < effect.effects().size(); index++) {
            SpecialEnchantmentEffect.Action action = effect.effects().get(index);
            if (action.trigger() != SpecialEnchantmentEffect.Trigger.ENCHANTMENT_EFFECT_HOTKEY) {
                continue;
            }
            SpecialEnchantmentEffect.Loop loop = action.loop().orElse(null);
            if (loop == SpecialEnchantmentEffect.Loop.TOGGLE && state.activeLoops.contains(index)) {
                return true;
            }
            if (loop == SpecialEnchantmentEffect.Loop.FIRST_ACTIVATION_TOGGLE && state.activeLoops.contains(index)) {
                return true;
            }
            if (action.limit() < 0 || state.remainingLimits.getOrDefault(index, action.limit()) > 0) {
                return true;
            }
        }
        return false;
    }

    private static void updateCooldownStart(ServerPlayer player, SpecialEnchantmentUse use, ActivationState state) {
        if (state.cooldownStarted || isBlocked(use.effect(), state)) {
            return;
        }

        long totalTicks = use.effect().cooldownTicks();
        if (totalTicks <= 0L) {
            return;
        }
        state.cooldownStarted = true;
        state.cooldownEndTick = player.level().getGameTime() + totalTicks;
    }

    private static boolean isBlocked(SpecialEnchantmentEffect effect, ActivationState state) {
        return isLoopBlocked(state) || isCooldownAfterLimitBlocked(effect, state);
    }

    private static boolean isLoopBlocked(ActivationState state) {
        return !state.activeLoops.isEmpty();
    }

    private static boolean isCooldownAfterLimitBlocked(SpecialEnchantmentEffect effect, ActivationState state) {
        return effect.cooldownAfterLimit()
                .map(id -> groupRemaining(effect, state, id) > 0)
                .orElse(false);
    }

    private static boolean isFinished(ServerPlayer player, ActivationState state) {
        if (!state.cooldownStarted) {
            return false;
        }
        return state.cooldownEndTick - player.level().getGameTime() <= 0L;
    }

    private static void runAction(ServerPlayer player, SpecialEnchantmentUse use, SpecialEnchantmentEffect.Action action) {
        Optional<net.minecraft.world.entity.LivingEntity> affected = resolveEntity(player, action.affected());
        Optional<net.minecraft.world.entity.LivingEntity> enchanted = resolveEntity(player, action.enchanted());
        if (affected.isEmpty() || enchanted.isEmpty()) {
            VanillaSquared.LOGGER.debug(
                    "Special enchantment {} action {} skipped: missing entity resolution (affected={}, enchanted={}) for player {}",
                    use.enchantmentId(),
                    action.id(),
                    affected.isPresent(),
                    enchanted.isPresent(),
                    player.getName().getString()
            );
            return;
        }
        if (!matches(action.requirements(), player, affected.get(), enchanted.get(), use.level())) {
            VanillaSquared.LOGGER.debug(
                    "Special enchantment {} action {} skipped: action requirements failed for player {}",
                    use.enchantmentId(),
                    action.id(),
                    player.getName().getString()
            );
            return;
        }

        if (action.type() == SpecialEnchantmentEffect.EffectType.SEND_CHAT_MSG) {
            sendChatMessage(action, affected.get(), enchanted.get(), use);
        }
    }

    private static Optional<net.minecraft.world.entity.LivingEntity> resolveEntity(ServerPlayer player, SpecialEnchantmentEffect.EntityReference reference) {
        if (reference == SpecialEnchantmentEffect.EntityReference.ENCHANTED) {
            return Optional.of(player);
        }

        UUID casterUuid = player.getUUID();
        ServerLevel level = (ServerLevel) player.level();
        AABB box = player.getBoundingBox().inflate(CLOSEST_ENTITY_RANGE);
        Optional<net.minecraft.world.entity.LivingEntity> closest = level.getEntitiesOfClass(
                        net.minecraft.world.entity.LivingEntity.class,
                        box,
                        entity -> entity.isAlive() && !entity.getUUID().equals(casterUuid)
                )
                .stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)));
        if (closest.isEmpty()) {
            VanillaSquared.LOGGER.debug(
                    "Special enchantment closest_entity resolution failed for player {} (range={})",
                    player.getName().getString(),
                    CLOSEST_ENTITY_RANGE
            );
        }
        return closest;
    }

    private static boolean matches(Optional<net.minecraft.world.level.storage.loot.predicates.LootItemCondition> requirement, ServerPlayer player, Entity affected, Entity enchanted, int level) {
        if (requirement.isEmpty()) {
            return true;
        }

        LootParams params = new LootParams.Builder((ServerLevel) player.level())
                .withParameter(LootContextParams.THIS_ENTITY, affected)
                .withParameter(LootContextParams.ENCHANTMENT_LEVEL, level)
                .withParameter(LootContextParams.ORIGIN, affected.position())
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, enchanted)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, enchanted)
                .create(SPECIAL_PREDICATE_PARAMS);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        return requirement.get().test(context);
    }

    private static void sendChatMessage(SpecialEnchantmentEffect.Action action, Entity affected, Entity enchanted, SpecialEnchantmentUse use) {
        String message = action.message().getString()
                .replace("$a", affected.getName().getString())
                .replace("$e", enchanted.getName().getString())
                .replace("$i", Component.translatable("enchantment." + use.enchantmentId().getNamespace() + "." + use.enchantmentId().getPath()).getString());
        if (affected instanceof ServerPlayer target) {
            target.sendSystemMessage(Component.literal(message));
        } else if (enchanted instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.literal(message));
            VanillaSquared.LOGGER.debug(
                    "Special enchantment {} delivered chat message to enchanted player {} for affected entity {}: {}",
                    use.enchantmentId(),
                    player.getName().getString(),
                    affected.getName().getString(),
                    message
            );
        } else {
            VanillaSquared.LOGGER.debug("Special enchantment {} generated chat message for non-player affected entity {}: {}", use.enchantmentId(), affected.getName().getString(), message);
        }
    }

    private static void sync(ServerPlayer player, SpecialEnchantmentUse use) {
        ActivationState state = state(player, use);
        if (state == null) {
            ServerPlayNetworking.send(player, new SpecialEnchantmentCooldownPayload(use.enchantmentId(), 0L, 0L, 0, SpecialEnchantmentCooldownPayload.DISPLAY_NONE, false, false));
            return;
        }
        ServerPlayNetworking.send(player, snapshot(player, use, state));
    }

    private static SpecialEnchantmentCooldownPayload snapshot(ServerPlayer player, SpecialEnchantmentUse use, ActivationState state) {
        SpecialEnchantmentEffect effect = use.effect();
        boolean blockedByLoop = isLoopBlocked(state);
        boolean blockedByLimit = isCooldownAfterLimitBlocked(effect, state);
        boolean blocked = blockedByLoop || blockedByLimit;
        Optional<String> displayRef = effect.displayLimit().filter(id -> groupTotal(effect, state, id) > 0);
        int displayRemaining = displayRef.map(id -> groupRemaining(effect, state, id)).orElse(0);
        int displayTotal = displayRef.map(id -> groupTotal(effect, state, id)).orElse(0);

        if (state.cooldownStarted) {
            long remaining = Math.max(0L, state.cooldownEndTick - player.level().getGameTime());
            if (remaining <= 0L) {
                return new SpecialEnchantmentCooldownPayload(use.enchantmentId(), 0L, 0L, 0, SpecialEnchantmentCooldownPayload.DISPLAY_NONE, false, false);
            }
            if (displayRemaining > 0) {
                return new SpecialEnchantmentCooldownPayload(use.enchantmentId(), remaining, state.cooldownTotalTicks, displayRemaining, SpecialEnchantmentCooldownPayload.DISPLAY_LIMIT, false, true);
            }
            return new SpecialEnchantmentCooldownPayload(use.enchantmentId(), remaining, state.cooldownTotalTicks, ticksToSeconds(remaining), SpecialEnchantmentCooldownPayload.DISPLAY_COOLDOWN, false, true);
        }

        if (blocked && displayRemaining > 0 && effect.cooldownAfterLimit().equals(displayRef) && blockedByLimit) {
            return new SpecialEnchantmentCooldownPayload(use.enchantmentId(), displayRemaining, Math.max(1, displayTotal), displayRemaining, SpecialEnchantmentCooldownPayload.DISPLAY_LIMIT, false, false);
        }
        if (blocked && displayRemaining > 0) {
            return new SpecialEnchantmentCooldownPayload(use.enchantmentId(), 1L, 1L, displayRemaining, SpecialEnchantmentCooldownPayload.DISPLAY_LIMIT, true, false);
        }
        if (blocked) {
            return new SpecialEnchantmentCooldownPayload(use.enchantmentId(), 1L, 1L, ticksToSeconds(state.cooldownTotalTicks), SpecialEnchantmentCooldownPayload.DISPLAY_COOLDOWN, true, false);
        }
        return new SpecialEnchantmentCooldownPayload(use.enchantmentId(), 0L, 0L, 0, SpecialEnchantmentCooldownPayload.DISPLAY_NONE, false, false);
    }

    private static int ticksToSeconds(long ticks) {
        return (int) Math.max(1L, (ticks + 19L) / 20L);
    }

    private static int groupRemaining(SpecialEnchantmentEffect effect, ActivationState state, String id) {
        int total = 0;
        for (int index = 0; index < effect.effects().size(); index++) {
            SpecialEnchantmentEffect.Action action = effect.effects().get(index);
            if (action.id().equals(id) && action.hasFiniteLimit()) {
                total += Math.max(0, state.remainingLimits.getOrDefault(index, action.limit()));
            }
        }
        return total;
    }

    private static int groupTotal(SpecialEnchantmentEffect effect, ActivationState state, String id) {
        int total = 0;
        for (int index = 0; index < effect.effects().size(); index++) {
            SpecialEnchantmentEffect.Action action = effect.effects().get(index);
            if (action.id().equals(id) && action.hasFiniteLimit()) {
                total += Math.max(0, state.totalLimits.getOrDefault(index, action.limit()));
            }
        }
        return total;
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
                    return Optional.of(new SpecialEnchantmentUse(key.get().identifier(), enchantment, enchantments.getLevel(enchantment), stack, slot, effect));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<SpecialEnchantmentUse> findUseInHands(ServerPlayer player, Identifier enchantmentId) {
        Optional<SpecialEnchantmentUse> mainhand = findSpecial(player.getMainHandItem(), EquipmentSlot.MAINHAND)
                .filter(use -> use.enchantmentId().equals(enchantmentId));
        if (mainhand.isPresent()) {
            return mainhand;
        }
        return findSpecial(player.getOffhandItem(), EquipmentSlot.OFFHAND)
                .filter(use -> use.enchantmentId().equals(enchantmentId));
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
                    return Optional.of(new SpecialEnchantmentUse(key.get().identifier(), enchantment, enchantments.getLevel(enchantment), stack, EquipmentSlot.MAINHAND, effect));
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

    private static final class ActivationState {
        private final long cooldownTotalTicks;
        private final Map<Integer, Integer> remainingLimits = new HashMap<>();
        private final Map<Integer, Integer> totalLimits = new HashMap<>();
        private final Set<Integer> activeLoops = new HashSet<>();
        private boolean cooldownStarted;
        private long cooldownEndTick;

        private ActivationState(long cooldownTotalTicks) {
            this.cooldownTotalTicks = cooldownTotalTicks;
        }
    }

    public record SpecialEnchantmentUse(Identifier enchantmentId, Holder<Enchantment> enchantment, int level, ItemStack stack, EquipmentSlot slot, SpecialEnchantmentEffect effect) {
    }
}
