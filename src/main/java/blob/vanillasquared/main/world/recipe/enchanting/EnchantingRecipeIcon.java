package blob.vanillasquared.main.world.recipe.enchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record EnchantingRecipeIcon(Item item, boolean enchanted, Optional<Identifier> itemModel) {
    public static final Codec<EnchantingRecipeIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.xmap(
                    identifier -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(identifier),
                    item -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item)
            ).fieldOf("item").forGetter(icon -> icon.item == null ? Item.byId(0) : icon.item),
            Codec.BOOL.optionalFieldOf("enchanted", false).forGetter(EnchantingRecipeIcon::enchanted),
            Identifier.CODEC.optionalFieldOf("item_model").forGetter(EnchantingRecipeIcon::itemModel)
    ).apply(instance, EnchantingRecipeIcon::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingRecipeIcon> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EnchantingRecipeIcon decode(RegistryFriendlyByteBuf buf) {
            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(Identifier.STREAM_CODEC.decode(buf));
            boolean enchanted = ByteBufCodecs.BOOL.decode(buf);
            Optional<Identifier> itemModel = ByteBufCodecs.optional(Identifier.STREAM_CODEC).decode(buf);
            return new EnchantingRecipeIcon(item, enchanted, itemModel);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, EnchantingRecipeIcon value) {
            Identifier.STREAM_CODEC.encode(buf, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(value.item));
            ByteBufCodecs.BOOL.encode(buf, value.enchanted);
            ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buf, value.itemModel);
        }
    };

    public ItemStack createStack(Component name) {
        ItemStack stack = new ItemStack(this.item);
        stack.set(DataComponents.ITEM_NAME, name.copy());
        if (this.enchanted) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        this.itemModel.ifPresent(identifier -> stack.set(DataComponents.ITEM_MODEL, identifier));
        return stack;
    }
}
