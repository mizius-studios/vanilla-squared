package blob.vanillasquared.main.gui.enchantment;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;

public class VSQEnchantmentScreen extends AbstractContainerScreen<EnchantmentMenu> {
    private static final int BG_COLOR = 0xEE101420;
    private static final int PANEL_COLOR = 0xEE1A2233;
    private static final int PANEL_BORDER = 0xFF3F5D86;
    private static final int SLOT_HINT_COLOR = 0x66FFFFFF;
    private static final int OPTION_ENABLED = 0xFF2A3F5C;
    private static final int OPTION_HOVER = 0xFF34527A;
    private static final int OPTION_DISABLED = 0xFF1B2330;

    private static final int OPTIONS_X = 74;
    private static final int OPTIONS_Y = 18;
    private static final int OPTION_W = 156;
    private static final int OPTION_H = 22;
    private static final int OPTION_GAP = 4;

    public VSQEnchantmentScreen(EnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 238;
        this.imageHeight = 182;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 10;
        this.titleLabelY = 8;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
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

        guiGraphics.fill(x0, y0, x0 + this.imageWidth, y0 + this.imageHeight, BG_COLOR);
        guiGraphics.renderOutline(x0, y0, this.imageWidth, this.imageHeight, PANEL_BORDER);

        guiGraphics.fill(x0 + 4, y0 + 4, x0 + 70, y0 + 76, PANEL_COLOR);
        guiGraphics.renderOutline(x0 + 4, y0 + 4, 66, 72, PANEL_BORDER);

        guiGraphics.fill(x0 + 8, y0 + 12, x0 + 66, y0 + 16, SLOT_HINT_COLOR);

        int optionsTop = y0 + OPTIONS_Y;
        for (int i = 0; i < 3; i++) {
            int rowY = optionsTop + i * (OPTION_H + OPTION_GAP);
            boolean hovered = this.isHoveringOption(i, mouseX, mouseY);
            boolean available = this.menu.costs[i] > 0;

            int optionColor;
            if (!available) {
                optionColor = OPTION_DISABLED;
            } else if (hovered) {
                optionColor = OPTION_HOVER;
            } else {
                optionColor = OPTION_ENABLED;
            }

            int rowX = x0 + OPTIONS_X;
            guiGraphics.fill(rowX, rowY, rowX + OPTION_W, rowY + OPTION_H, optionColor);
            guiGraphics.renderOutline(rowX, rowY, OPTION_W, OPTION_H, PANEL_BORDER);

            this.renderOptionText(guiGraphics, i, rowX, rowY, available);
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

    private boolean isHoveringOption(int option, int mouseX, int mouseY) {
        int x = this.leftPos + OPTIONS_X;
        int y = this.topPos + OPTIONS_Y + option * (OPTION_H + OPTION_GAP);
        return mouseX >= x && mouseX < x + OPTION_W && mouseY >= y && mouseY < y + OPTION_H;
    }
}
