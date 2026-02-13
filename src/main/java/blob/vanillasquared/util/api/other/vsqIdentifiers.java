package blob.vanillasquared.util.api.other;

import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Unique;

public enum vsqIdentifiers {
    vsqArmorChestplateOverride(Identifier.fromNamespaceAndPath("vanillasquared", "armor_chestplate_override")),
    vsqArmorLeggingsOverride(Identifier.fromNamespaceAndPath("vanillasquared", "armor_leggings_override")),
    vsqArmorBootsOverride(Identifier.fromNamespaceAndPath("vanillasquared", "armor_boots_override")),
    vsqArmorHelmetOverride(Identifier.fromNamespaceAndPath("vanillasquared", "armor_helmet_override"));

    private final Identifier identifier;

    vsqIdentifiers(Identifier identifier) {
        this.identifier = identifier;
    }
    public Identifier identifier() {
        return identifier;
    }
}
