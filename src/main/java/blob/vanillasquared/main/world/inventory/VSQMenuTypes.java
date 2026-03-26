package blob.vanillasquared.main.world.inventory;

import blob.vanillasquared.main.VanillaSquared;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public final class VSQMenuTypes {
    public static final MenuType<VSQEnchantmentMenu> ENCHANTING = new ExtendedMenuType<>(
            VSQEnchantmentMenu::new,
            BlockPos.STREAM_CODEC
    );

    private VSQMenuTypes() {
    }

    public static void initialize() {
        net.minecraft.core.Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(VanillaSquared.MOD_ID, "enchanting"), ENCHANTING);
    }
}
