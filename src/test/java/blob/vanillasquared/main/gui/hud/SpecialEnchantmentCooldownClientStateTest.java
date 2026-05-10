package blob.vanillasquared.main.gui.hud;

import blob.vanillasquared.main.network.payload.SpecialEnchantmentCooldownPayload;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialEnchantmentCooldownClientStateTest {
    private static final Identifier SPECIAL_A = Identifier.fromNamespaceAndPath("vsq", "special_a");
    private static final Identifier SPECIAL_B = Identifier.fromNamespaceAndPath("vsq", "special_b");
    private static final Identifier SPECIAL_C = Identifier.fromNamespaceAndPath("vsq", "special_c");

    @AfterEach
    void tearDown() {
        SpecialEnchantmentCooldownClientState.clear();
    }

    @Test
    void switchingBetweenSpecialItemsBridgesPreviousCooldownForOneFrame() {
        applyCooldown(SPECIAL_A, 40L);

        assertTrue(SpecialEnchantmentCooldownClientState.shouldReserveContextualBar());
        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.empty()));
        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_B), Optional.empty()));

        applyCooldown(SPECIAL_B, 30L);

        assertEquals(Optional.of(visibleCooldown(30L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_B), Optional.empty()));
    }

    @Test
    void switchingToNonSpecialItemClearsImmediately() {
        applyCooldown(SPECIAL_A, 40L);

        assertTrue(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.empty()).isPresent());
        assertTrue(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.empty(), Optional.empty()).isPresent());

        SpecialEnchantmentCooldownClientState.advanceTickForTest(true);
        SpecialEnchantmentCooldownClientState.advanceTickForTest(true);

        assertFalse(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.empty(), Optional.empty()).isPresent());
        SpecialEnchantmentCooldownClientState.apply(SPECIAL_A, 0L, 0L, 0, SpecialEnchantmentCooldownPayload.DISPLAY_NONE, false, true);
        assertFalse(SpecialEnchantmentCooldownClientState.shouldReserveContextualBar());
    }

    @Test
    void bridgedFallbackExpiresAfterGraceWindow() {
        applyCooldown(SPECIAL_A, 40L);

        assertTrue(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.empty()).isPresent());
        assertTrue(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_B), Optional.empty()).isPresent());

        SpecialEnchantmentCooldownClientState.advanceTickForTest(true);
        assertTrue(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_B), Optional.empty()).isPresent());

        SpecialEnchantmentCooldownClientState.advanceTickForTest(true);
        assertFalse(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_B), Optional.empty()).isPresent());
    }

    @Test
    void bridgedFallbackIsNotUsedAfterUnderlyingEntryRemoval() {
        applyCooldown(SPECIAL_A, 40L);

        assertTrue(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.empty()).isPresent());

        SpecialEnchantmentCooldownClientState.apply(SPECIAL_A, 0L, 0L, 0, SpecialEnchantmentCooldownPayload.DISPLAY_NONE, false, true);

        assertFalse(SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.empty()).isPresent());
    }

    @Test
    void bridgedFallbackSurvivesOldEntryRemovalWhileSwitchingToDifferentSpecialItem() {
        applyCooldown(SPECIAL_A, 40L);

        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.empty()));

        SpecialEnchantmentCooldownClientState.apply(SPECIAL_A, 0L, 0L, 0, SpecialEnchantmentCooldownPayload.DISPLAY_NONE, false, true);

        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_B), Optional.empty()));
    }

    @Test
    void bridgedFallbackSurvivesOldEntryRemovalAndClientTickBeforeRender() {
        applyCooldown(SPECIAL_A, 40L);

        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.empty()));

        SpecialEnchantmentCooldownClientState.apply(SPECIAL_A, 0L, 0L, 0, SpecialEnchantmentCooldownPayload.DISPLAY_NONE, false, true);
        SpecialEnchantmentCooldownClientState.advanceTickForTest(true);

        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_B), Optional.empty()));
    }

    @Test
    void bridgedFallbackCoversTransientEmptyHeldSpecialDuringSwap() {
        applyCooldown(SPECIAL_A, 40L);

        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.empty()));

        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.empty(), Optional.empty()));
        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_B), Optional.empty()));
    }

    @Test
    void currentHeldCooldownStillUsesMainhandThenOffhandBeforeBridge() {
        applyCooldown(SPECIAL_A, 40L);
        applyCooldown(SPECIAL_B, 30L);

        assertEquals(Optional.of(visibleCooldown(40L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_A), Optional.of(SPECIAL_B)));
        assertEquals(Optional.of(visibleCooldown(30L)), SpecialEnchantmentCooldownClientState.visibleCooldown(Optional.of(SPECIAL_C), Optional.of(SPECIAL_B)));
    }

    private static void applyCooldown(Identifier enchantmentId, long remaining) {
        SpecialEnchantmentCooldownClientState.apply(enchantmentId, remaining, 40L, (int) Math.max(1L, (remaining + 19L) / 20L), SpecialEnchantmentCooldownPayload.DISPLAY_COOLDOWN, false, true);
    }

    private static SpecialEnchantmentCooldownClientState.VisibleCooldown visibleCooldown(long remaining) {
        return new SpecialEnchantmentCooldownClientState.VisibleCooldown(remaining, 40L, (int) Math.max(1L, (remaining + 19L) / 20L), SpecialEnchantmentCooldownPayload.DISPLAY_COOLDOWN, false);
    }
}
