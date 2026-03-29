package blob.vanillasquared.main.world.recipe.enchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public record EnchantingRecipeIcon(Optional<Item> item, Optional<Identifier> tag, boolean enchanted, Optional<Identifier> itemModel) {
    public static final Codec<EnchantingRecipeIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("item")
                    .forGetter(icon -> icon.item.map(BuiltInRegistries.ITEM::getKey)),
            Identifier.CODEC.optionalFieldOf("tag").forGetter(EnchantingRecipeIcon::tag),
            Codec.BOOL.optionalFieldOf("enchanted", false).forGetter(EnchantingRecipeIcon::enchanted),
            Identifier.CODEC.optionalFieldOf("item_model").forGetter(EnchantingRecipeIcon::itemModel)
    ).apply(instance, (itemId, tag, enchanted, itemModel) -> new EnchantingRecipeIcon(
            itemId.map(BuiltInRegistries.ITEM::getValue),
            tag,
            enchanted,
            itemModel
    )));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantingRecipeIcon> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EnchantingRecipeIcon decode(RegistryFriendlyByteBuf buf) {
            Optional<Item> item = ByteBufCodecs.optional(Identifier.STREAM_CODEC)
                    .decode(buf)
                    .map(BuiltInRegistries.ITEM::getValue);
            Optional<Identifier> tag = ByteBufCodecs.optional(Identifier.STREAM_CODEC).decode(buf);
            boolean enchanted = ByteBufCodecs.BOOL.decode(buf);
            Optional<Identifier> itemModel = ByteBufCodecs.optional(Identifier.STREAM_CODEC).decode(buf);
            return new EnchantingRecipeIcon(item, tag, enchanted, itemModel);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, EnchantingRecipeIcon value) {
            ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buf, value.item.map(BuiltInRegistries.ITEM::getKey));
            ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buf, value.tag);
            ByteBufCodecs.BOOL.encode(buf, value.enchanted);
            ByteBufCodecs.optional(Identifier.STREAM_CODEC).encode(buf, value.itemModel);
        }
    };

    public EnchantingRecipeIcon {
        if (item == null) {
            item = Optional.empty();
        }
        if (tag == null) {
            tag = Optional.empty();
        }
        if (itemModel == null) {
            itemModel = Optional.empty();
        }
        if (item.isPresent() == tag.isPresent()) {
            throw new IllegalArgumentException("Enchanting recipe icon must define exactly one of item or tag");
        }
    }

    public ItemStack createStack(Component name) {
        if (this.item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return this.vsq$createStack(this.item.get(), name);
    }

    public SlotDisplay createDisplay(Component name) {
        if (this.item.isPresent()) {
            return this.vsq$stackDisplay(this.vsq$createStack(this.item.get(), name));
        }

        TagKey<Item> tagKey = TagKey.create(net.minecraft.core.registries.Registries.ITEM, this.tag.orElseThrow());
        List<SlotDisplay> displays = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tagKey)) {
            ItemStack stack = this.vsq$createStack(holder.value(), name);
            if (!stack.isEmpty()) {
                displays.add(this.vsq$stackDisplay(stack));
            }
        }

        if (displays.isEmpty()) {
            return SlotDisplay.Empty.INSTANCE;
        }
        if (displays.size() == 1) {
            return displays.get(0);
        }
        return new SlotDisplay.Composite(List.copyOf(displays));
    }

    public boolean isTag() {
        return this.tag.isPresent();
    }

    public List<ItemStack> createStacks(Component name) {
        if (this.item.isPresent()) {
            return List.of(this.vsq$createStack(this.item.get(), name));
        }

        TagKey<Item> tagKey = TagKey.create(net.minecraft.core.registries.Registries.ITEM, this.tag.orElseThrow());
        return StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tagKey).spliterator(), false)
                .map(Holder::value)
                .map(item -> this.vsq$createStack(item, name))
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    private ItemStack vsq$createStack(Item item, Component name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.ITEM_NAME, name.copy());
        if (this.enchanted) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        this.itemModel.ifPresent(identifier -> stack.set(DataComponents.ITEM_MODEL, identifier));
        return stack;
    }

    private SlotDisplay vsq$stackDisplay(ItemStack stack) {
        return stack.isEmpty() ? SlotDisplay.Empty.INSTANCE : new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
    }
}
