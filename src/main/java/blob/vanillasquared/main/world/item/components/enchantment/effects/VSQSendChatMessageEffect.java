package blob.vanillasquared.main.world.item.components.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record VSQSendChatMessageEffect(
        Component message
) implements EnchantmentEntityEffect {
    private static final Pattern PLACEHOLDERS = Pattern.compile("\\$(a|e|i)");

    public static final MapCodec<VSQSendChatMessageEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("message", Component.empty()).forGetter(VSQSendChatMessageEffect::message)
    ).apply(instance, VSQSendChatMessageEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        Entity affectedResolved = entity;
        Entity enchantedResolved = item.owner() != null ? item.owner() : entity;
        Map<String, String> replacements = Map.of(
                "a", affectedResolved.getName().getString(),
                "e", enchantedResolved.getName().getString(),
                "i", item.itemStack().getHoverName().getString()
        );
        MutableComponent text = Component.empty();
        this.message.visit((Style style, String content) -> {
            Matcher matcher = PLACEHOLDERS.matcher(content);
            StringBuffer replaced = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(replaced, Matcher.quoteReplacement(replacements.get(matcher.group(1))));
            }
            matcher.appendTail(replaced);
            text.append(Component.literal(replaced.toString()).withStyle(style));
            return java.util.Optional.empty();
        }, Style.EMPTY);

        if (affectedResolved instanceof ServerPlayer target) {
            target.sendSystemMessage(text);
        } else if (enchantedResolved instanceof ServerPlayer player) {
            player.sendSystemMessage(text);
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return MAP_CODEC;
    }
}
