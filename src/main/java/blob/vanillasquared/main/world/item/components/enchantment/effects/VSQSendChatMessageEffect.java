package blob.vanillasquared.main.world.item.components.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record VSQSendChatMessageEffect(
        Component message
) implements EnchantmentEntityEffect {
    public static final MapCodec<VSQSendChatMessageEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("message", Component.empty()).forGetter(VSQSendChatMessageEffect::message)
    ).apply(instance, VSQSendChatMessageEffect::new));

    @Override
    public void apply(ServerLevel level, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        Entity affectedResolved = entity;
        Entity enchantedResolved = entity;
        String text = this.message.getString()
                .replace("$a", affectedResolved.getName().getString())
                .replace("$e", enchantedResolved.getName().getString())
                .replace("$i", item.itemStack().getHoverName().getString());

        if (affectedResolved instanceof ServerPlayer target) {
            target.sendSystemMessage(Component.literal(text));
        } else if (enchantedResolved instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.literal(text));
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return MAP_CODEC;
    }
}
