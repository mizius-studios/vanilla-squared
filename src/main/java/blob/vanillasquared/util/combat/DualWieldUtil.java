package blob.vanillasquared.util.combat;

import blob.vanillasquared.util.data.DualWieldComponent;
import blob.vanillasquared.util.modules.components.RegisterComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class DualWieldUtil {
    private static final Set<Identifier> ADDITIVE_ENCHANTMENTS = Set.of(
            Identifier.parse("minecraft:fire_aspect"),
            Identifier.parse("minecraft:looting"),
            Identifier.parse("minecraft:knockback"),
            Identifier.parse("minecraft:sweeping_edge")
    );

    private DualWieldUtil() {
    }

    public static Optional<ActiveDualWield> getActiveDualWield(ItemStack mainhand, ItemStack offhand) {
        if (mainhand.isEmpty() || offhand.isEmpty()) {
            return Optional.empty();
        }
        if (!mainhand.has(RegisterComponents.dualWield) || !offhand.has(RegisterComponents.dualWield)) {
            return Optional.empty();
        }

        DualWieldComponent mainComponent = mainhand.get(RegisterComponents.dualWield);
        DualWieldComponent offComponent = offhand.get(RegisterComponents.dualWield);
        if (mainComponent == null || offComponent == null) {
            return Optional.empty();
        }
        if (!hasMatchingIdentifier(mainComponent.identifiers(), offComponent.identifiers())) {
            return Optional.empty();
        }

        return Optional.of(new ActiveDualWield(mainComponent, offComponent));
    }

    public static ItemEnchantments mergeEnchantments(ItemStack mainhand, ItemStack offhand, ActiveDualWield activeDualWield) {
        ItemEnchantments mainEnchantments = mainhand.getEnchantments();
        ItemEnchantments offEnchantments = offhand.getEnchantments();

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(mainEnchantments);
        Set<Identifier> blockedIds = blockedIds(activeDualWield.mainhand().blockedEnchantments(), activeDualWield.offhand().blockedEnchantments());

        for (Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment : offEnchantments.keySet()) {
            if (isBlocked(enchantment, blockedIds)) {
                continue;
            }

            int offLevel = offEnchantments.getLevel(enchantment);
            if (offLevel <= 0) {
                continue;
            }

            int mainLevel = mainEnchantments.getLevel(enchantment);
            int mergedLevel = isAdditive(enchantment) ? mainLevel + offLevel : Math.max(mainLevel, offLevel);
            mutable.set(enchantment, mergedLevel);
        }

        return mutable.toImmutable();
    }

    public static float getItemAttackDamage(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return (float) modifiers.compute(Attributes.ATTACK_DAMAGE, 0.0D, EquipmentSlot.MAINHAND);
    }

    public static float calculateExtraSweepDamage(float offhandAttackDamage, DualWieldComponent component, boolean critical) {
        float sweepingDamage = offhandAttackDamage * (component.sweepingDmg() / 100.0F);
        if (!critical) {
            return sweepingDamage;
        }

        return sweepingDamage + offhandAttackDamage * (component.criticalDmg() / 100.0F);
    }

    public static boolean performExtraSweepAttack(ServerLevel serverLevel, Player player, LivingEntity primaryTarget, DamageSource source, float damage) {
        boolean hitAny = false;
        primaryTarget.invulnerableTime = 0;
        primaryTarget.hurtTime = 0;
        primaryTarget.hurtDuration = 0;

        if (primaryTarget.hurtServer(serverLevel, source, damage)) {
            applySweepKnockback(player, primaryTarget);
            hitAny = true;
        }

        List<LivingEntity> nearbyTargets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                primaryTarget.getBoundingBox().inflate(1.0D, 0.25D, 1.0D)
        );

        for (LivingEntity candidate : nearbyTargets) {
            if (candidate == player || candidate == primaryTarget) {
                continue;
            }
            if (player.isAlliedTo(candidate)) {
                continue;
            }
            if (candidate instanceof ArmorStand armorStand && armorStand.isMarker()) {
                continue;
            }
            if (player.distanceToSqr(candidate) >= 9.0D) {
                continue;
            }

            if (candidate.hurtServer(serverLevel, source, damage)) {
                applySweepKnockback(player, candidate);
                hitAny = true;
            }
        }

        if (hitAny) {
            spawnSweepEffects(serverLevel, player);
        }
        return hitAny;
    }

    private static void applySweepKnockback(Player player, LivingEntity target) {
        float yawRadians = player.getYRot() * ((float) Math.PI / 180.0F);
        target.knockback(0.4D, Mth.sin(yawRadians), -Mth.cos(yawRadians));
    }

    private static void spawnSweepEffects(ServerLevel serverLevel, Player player) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP,
                player.getSoundSource(),
                1.0F,
                1.0F
        );

        float yawRadians = player.getYRot() * ((float) Math.PI / 180.0F);
        double xOffset = -Mth.sin(yawRadians);
        double zOffset = Mth.cos(yawRadians);
        serverLevel.sendParticles(
                ParticleTypes.SWEEP_ATTACK,
                player.getX() + xOffset,
                player.getY(0.5D),
                player.getZ() + zOffset,
                0,
                xOffset,
                0.0D,
                zOffset,
                0.0D
        );
    }

    private static boolean hasMatchingIdentifier(List<String> left, List<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        Set<String> normalized = new HashSet<>(left);
        for (String identifier : right) {
            if (normalized.contains(identifier)) {
                return true;
            }
        }
        return false;
    }

    private static Set<Identifier> blockedIds(List<String> mainBlocked, List<String> offBlocked) {
        Set<Identifier> blockedIds = new HashSet<>();
        addAllParsed(blockedIds, mainBlocked);
        addAllParsed(blockedIds, offBlocked);
        return blockedIds;
    }

    private static void addAllParsed(Set<Identifier> out, List<String> ids) {
        for (String rawId : ids) {
            Identifier parsed = parseIdentifier(rawId);
            if (parsed != null) {
                out.add(parsed);
            }
        }
    }

    private static Identifier parseIdentifier(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }

        Identifier parsed = Identifier.tryParse(rawId);
        if (parsed != null) {
            return parsed;
        }

        if (!rawId.contains(":")) {
            return Identifier.tryParse("minecraft:" + rawId);
        }
        return null;
    }

    private static boolean isBlocked(Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment, Set<Identifier> blockedIds) {
        for (Identifier id : blockedIds) {
            if (enchantment.is(id)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAdditive(Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        for (Identifier id : ADDITIVE_ENCHANTMENTS) {
            if (enchantment.is(id)) {
                return true;
            }
        }
        return false;
    }

    public record ActiveDualWield(DualWieldComponent mainhand, DualWieldComponent offhand) {
    }
}
