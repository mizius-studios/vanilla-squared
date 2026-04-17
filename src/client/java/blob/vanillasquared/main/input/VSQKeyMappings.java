package blob.vanillasquared.main.input;

import blob.vanillasquared.mixin.client.input.OptionsAccessor;
import blob.vanillasquared.main.network.payload.SpecialEnchantmentHotkeyPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.Arrays;

public final class VSQKeyMappings {
    private static final KeyMapping ENCHANTMENT_HOTKEY = new KeyMapping(
            "key.vsq.enchantment_hotkey",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LALT,
            KeyMapping.Category.MISC
    );
    private static boolean registered;

    private VSQKeyMappings() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!registered && client.options != null) {
                vsq$registerKeyMapping(client, ENCHANTMENT_HOTKEY);
                registered = true;
            }
            while (ENCHANTMENT_HOTKEY.consumeClick()) {
                if (client.player != null && client.getConnection() != null) {
                    ClientPlayNetworking.send(SpecialEnchantmentHotkeyPayload.INSTANCE);
                }
            }
        });
    }

    private static void vsq$registerKeyMapping(Minecraft client, KeyMapping keyMapping) {
        KeyMapping[] keyMappings = Arrays.copyOf(client.options.keyMappings, client.options.keyMappings.length + 1);
        keyMappings[keyMappings.length - 1] = keyMapping;
        ((OptionsAccessor) client.options).vsq$setKeyMappings(keyMappings);
        KeyMapping.resetMapping();
    }
}
