package blob.vanillasquared.util.api.other;

import net.minecraft.resources.Identifier;

public enum vsqIdentifiers {
    vsqArmorChestplateOverride(Identifier.fromNamespaceAndPath("vanilla-squared", "armor_chestplate_override")),
    vsqArmorLeggingsOverride(Identifier.fromNamespaceAndPath("vanilla-squared", "armor_leggings_override")),
    vsqArmorBootsOverride(Identifier.fromNamespaceAndPath("vanilla-squared", "armor_boots_override")),
    vsqArmorHelmetOverride(Identifier.fromNamespaceAndPath("vanilla-squared", "armor_helmet_override")),
    vsqSwordOverride(Identifier.fromNamespaceAndPath("vanilla-squared", "sword_override")),
    vsqAxeOverride(Identifier.fromNamespaceAndPath("vanilla-squared", "axe_override")),
    vsqTridentOverride(Identifier.fromNamespaceAndPath("vanilla-squared", "trident_override"));

    private final Identifier identifier;

    vsqIdentifiers(Identifier identifier) {
        this.identifier = identifier;
    }
    public Identifier identifier() {
        return identifier;
    }
}
