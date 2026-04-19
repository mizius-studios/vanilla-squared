package blob.vanillasquared.main.world.item.components.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;

public record SpecialEnchantmentEffect(
        long cooldown,
        Optional<LootItemCondition> requirements,
        Optional<String> cooldownAfterLimit,
        Optional<String> displayLimit,
        List<Action> effects
) {
    public static final Codec<SpecialEnchantmentEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("cooldown").forGetter(SpecialEnchantmentEffect::cooldown),
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("requirements").forGetter(SpecialEnchantmentEffect::requirements),
            Codec.STRING.optionalFieldOf("cooldown_after_limit").forGetter(SpecialEnchantmentEffect::cooldownAfterLimit),
            Codec.STRING.optionalFieldOf("display_limit").forGetter(SpecialEnchantmentEffect::displayLimit),
            Action.CODEC.listOf().optionalFieldOf("effect", List.of()).forGetter(SpecialEnchantmentEffect::effects)
    ).apply(instance, SpecialEnchantmentEffect::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpecialEnchantmentEffect> STREAM_CODEC =
            ByteBufCodecs.VAR_LONG.map(
                    cooldown -> new SpecialEnchantmentEffect(cooldown, Optional.empty(), Optional.empty(), Optional.empty(), List.of()),
                    SpecialEnchantmentEffect::cooldown
            ).mapStream(buf -> buf);

    public long cooldownTicks() {
        return Math.max(0L, this.cooldown) * 20L;
    }

    public record Action(
            String id,
            EffectType type,
            EntityReference affected,
            EntityReference enchanted,
            Optional<LootItemCondition> requirements,
            int limit,
            Trigger trigger,
            Optional<Loop> loop,
            Component message
    ) {
        public static final Codec<Action> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Action::id),
                EffectType.CODEC.fieldOf("type").forGetter(Action::type),
                EntityReference.CODEC.optionalFieldOf("affected", EntityReference.ENCHANTED).forGetter(Action::affected),
                EntityReference.CODEC.optionalFieldOf("enchanted", EntityReference.ENCHANTED).forGetter(Action::enchanted),
                LootItemCondition.DIRECT_CODEC.optionalFieldOf("requirements").forGetter(Action::requirements),
                Codec.INT.optionalFieldOf("limit", 1).forGetter(Action::limit),
                Trigger.CODEC.optionalFieldOf("trigger", Trigger.FIRST_ACTIVATION).forGetter(Action::trigger),
                Loop.CODEC.optionalFieldOf("loop").forGetter(Action::loop),
                ComponentSerialization.CODEC.optionalFieldOf("message", Component.empty()).forGetter(Action::message)
        ).apply(instance, Action::new));

        public boolean hasFiniteLimit() {
            return this.limit > 0 && this.loop.orElse(null) != Loop.FIRST_ACTIVATION;
        }
    }

    public enum EffectType implements StringRepresentable {
        SEND_CHAT_MSG("send_chat_msg");

        public static final Codec<EffectType> CODEC = StringRepresentable.fromEnum(EffectType::values);
        private final String name;

        EffectType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum EntityReference implements StringRepresentable {
        CLOSEST_ENTITY("closest_entity"),
        ENCHANTED("enchanted");

        public static final Codec<EntityReference> CODEC = StringRepresentable.fromEnum(EntityReference::values);
        private final String name;

        EntityReference(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum Trigger implements StringRepresentable {
        FIRST_ACTIVATION("first_activation"),
        ENCHANTMENT_EFFECT_HOTKEY("enchantment_effect_hotkey");

        public static final Codec<Trigger> CODEC = StringRepresentable.fromEnum(Trigger::values);
        private final String name;

        Trigger(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum Loop implements StringRepresentable {
        TOGGLE("toggle"),
        FIRST_ACTIVATION("first_activation"),
        FIRST_ACTIVATION_TOGGLE("first_activation_toggle");

        public static final Codec<Loop> CODEC = StringRepresentable.fromEnum(Loop::values);
        private final String name;

        Loop(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
