package blob.vanillasquared.util.modules.attributes;

import blob.vanillasquared.VanillaSquared;
import blob.vanillasquared.util.api.other.vsqIdentifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class RegisterAttributes {

    public static final Holder<Attribute> maceProtectionAttribute = register(
            vsqIdentifiers.maceProtectionAttribute.identifier(),
            new RangedAttribute(
                    "vsq.attributes.mace_protection",
                    0.0, 0.0, 1.0
            ).setSyncable(true)
    );
    public static final Holder<Attribute> magicProtectionAttribute = register(
            vsqIdentifiers.magicProtectionAttribute.identifier(),
            new RangedAttribute(
                    "Magic Protection",
                    0.0, 0.0, 1.0
            ).setSyncable(true)
    );
    public static final Holder<Attribute> dripstoneProtectionAttribute = register(
            vsqIdentifiers.dripstoneProtectionAttribute.identifier(),
            new RangedAttribute(
                    "Dripstone Protection",
                    0.0, 0.0, 1.0
            ).setSyncable(true)
    );
    public static final Holder<Attribute> spearProtectionAttribute = register(
            vsqIdentifiers.spearProtectionAttribute.identifier(),
            new RangedAttribute(
                    "Spear Protection",
                    0.0, 0.0, 1.0
            ).setSyncable(true)
    );

    private static Holder<Attribute> register(Identifier identifier, Attribute attribute) {
        Registry.register(
                BuiltInRegistries.ATTRIBUTE,
                identifier,
                attribute
        );
        return BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
    }
    public static void initialize() {

    }
}
