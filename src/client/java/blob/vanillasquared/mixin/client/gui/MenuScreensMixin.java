package blob.vanillasquared.mixin.client.gui;

import blob.vanillasquared.main.gui.enchantment.VSQEnchantmentScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MenuScreens.class)
public abstract class MenuScreensMixin {

    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> void vsq$openCustomEnchantScreen(
        MenuType<T> menuType,
        Minecraft minecraft,
        int containerId,
        Component title,
        CallbackInfo ci
    ) {
        if (menuType != MenuType.ENCHANTMENT || minecraft.player == null) {
            return;
        }

        EnchantmentMenu menu = MenuType.ENCHANTMENT.create(containerId, minecraft.player.getInventory());
        if (menu == null) {
            return;
        }
        VSQEnchantmentScreen screen = new VSQEnchantmentScreen(menu, minecraft.player.getInventory(), title);

        minecraft.player.containerMenu = screen.getMenu();
        minecraft.setScreen(screen);
        ci.cancel();
    }
}
