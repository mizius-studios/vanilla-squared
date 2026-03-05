package blob.vanillasquared.util.api.other;

import blob.vanillasquared.VanillaSquared;
import net.minecraft.resources.Identifier;

public enum vsqIdentifiers {
    armorChestplateOverride(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "armor_chestplate_override")),
    armorLeggingsOverride(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "armor_leggings_override")),
    armorBootsOverride(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "armor_boots_override")),
    armorHelmetOverride(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "armor_helmet_override")),
    swordOverride(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "sword_override")),
    axeOverride(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "axe_override")),
    tridentOverride(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "trident_override")),
    maceProtectionAttribute(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "mace_protection_attribute")),
    magicProtectionAttribute(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "magic_protection_attribute")),
    dripstoneProtectionAttribute(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "dripstone_protection_attribute")),
    spearProtectionAttribute(Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "spear_protection_attribute"));

    private final Identifier identifier;

    vsqIdentifiers(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier identifier() {
        return identifier;
    }
}
