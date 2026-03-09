package blob.vanillasquared.main.gui.enchantment;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;

public class VSQEnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {
    private static final Identifier GUI_TEXTURE =
        Identifier.fromNamespaceAndPath("vsq", "textures/gui/containers/enchantment_table.png");
    private static final int TEX_W = 256;
    private static final int TEX_H = 256;
    private static final int OPTIONS_X = 60;
    private static final int OPTIONS_Y = 14;
    private static final int OPTION_W = 108;
    private static final int OPTION_H = 19;
    private static final int OPTION_GAP = 19;
    private static final int OPTION_U = 0;
    private static final int OPTION_V_ENABLED = 166;
    private static final int OPTION_V_HOVER = 185;
    private static final int OPTION_V_DISABLED = 204;

    public VSQEnchantmentScreen(EnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 10;
        this.titleLabelY = 5;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && this.minecraft != null && this.minecraft.player != null && this.minecraft.gameMode != null) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();

            for (int i = 0; i < 3; i++) {
                if (!this.isHoveringOption(i, mouseX, mouseY)) {
                    continue;
                }

                if (this.menu.costs[i] <= 0) {
                    return true;
                }

                if (this.menu.clickMenuButton(this.minecraft.player, i)) {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
                }

                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x0 = this.leftPos;
        int y0 = this.topPos;

        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            GUI_TEXTURE,
            x0,
            y0,
            0.0F,
            0.0F,
            this.imageWidth,
            this.imageHeight,
            TEX_W,
            TEX_H
        );

        int optionsTop = y0 + OPTIONS_Y;
        for (int i = 0; i < 3; i++) {
            int rowY = optionsTop + i * (OPTION_H + OPTION_GAP);
            boolean hovered = this.isHoveringOption(i, mouseX, mouseY) && this.menu.costs[i] > 0;
            boolean disabled = !this.canUseOption(i);
            int optionV = disabled ? OPTION_V_DISABLED : (hovered ? OPTION_V_HOVER : OPTION_V_ENABLED);

            int rowX = x0 + OPTIONS_X;
            guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GUI_TEXTURE,
                rowX,
                rowY,
                OPTION_U,
                optionV,
                OPTION_W,
                OPTION_H,
                TEX_W,
                TEX_H
            );

            this.renderOptionText(guiGraphics, i, rowX, rowY, !disabled);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        for (int i = 0; i < 3; i++) {
            if (this.isHoveringOption(i, mouseX, mouseY)) {
                List<Component> tooltip = this.buildOptionTooltip(i);
                if (!tooltip.isEmpty()) {
                    guiGraphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
                }
                break;
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderOptionText(GuiGraphics guiGraphics, int option, int rowX, int rowY, boolean available) {
        int cost = this.menu.costs[option];
        int textColor = available ? 0xFFDCE8FF : 0xFF667389;

        if (!available) {
            guiGraphics.drawString(this.font, Component.translatable("container.enchant.level.requirement", 0), rowX + 6, rowY + 7, textColor, false);
            return;
        }

        EnchantmentNames names = EnchantmentNames.getInstance();
        names.initSeed(this.menu.getEnchantmentSeed() + option);

        FormattedText randomName = names.getRandomName(this.font, 95);
        List<FormattedCharSequence> glyphLines = this.font.split(randomName, 95);
        if (!glyphLines.isEmpty()) {
            guiGraphics.drawString(this.font, glyphLines.getFirst(), rowX + 6, rowY + 4, textColor, false);
        }

        Component costText = Component.literal(cost + " XP  |  " + (option + 1) + " Lapis");
        int costColor = this.canAfford(option) ? 0xFF99FF9C : 0xFFFF8C8C;
        guiGraphics.drawString(this.font, costText, rowX + 6, rowY + 13, costColor, false);
    }

    private List<Component> buildOptionTooltip(int option) {
        List<Component> lines = new ArrayList<>();
        int cost = this.menu.costs[option];

        if (cost <= 0) {
            lines.add(Component.literal("No enchantment available"));
            return lines;
        }

        lines.add(Component.literal("Required: " + cost + " XP, " + (option + 1) + " lapis"));

        if (!this.hasEnoughXp(option)) {
            lines.add(Component.literal("Not enough XP levels"));
        }

        if (!this.hasEnoughLapis(option)) {
            lines.add(Component.literal("Not enough lapis"));
        }

        return lines;
    }

    private boolean hasEnoughXp(int option) {
        return this.minecraft != null && this.minecraft.player != null && this.minecraft.player.experienceLevel >= this.menu.costs[option];
    }

    private boolean hasEnoughLapis(int option) {
        return this.menu.getGoldCount() >= option + 1;
    }

    private boolean canAfford(int option) {
        return this.hasEnoughXp(option) && this.hasEnoughLapis(option);
    }

    private boolean canUseOption(int option) {
        return this.menu.costs[option] > 0 && (this.canAfford(option) || (this.minecraft != null && this.minecraft.player != null && this.minecraft.player.hasInfiniteMaterials()));
    }

    private boolean isHoveringOption(int option, int mouseX, int mouseY) {
        int x = this.leftPos + OPTIONS_X;
        int y = this.topPos + OPTIONS_Y + option * (OPTION_H + OPTION_GAP);
        return mouseX >= x && mouseX < x + OPTION_W && mouseY >= y && mouseY < y + OPTION_H;
    }
}
