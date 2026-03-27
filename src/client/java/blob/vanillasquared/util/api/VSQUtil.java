package blob.vanillasquared.util.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class VSQUtil {
    public static class VSQ$Component {
        public List<Component> expandTooltipLines(List<Component> components) {
            List<Component> expanded = new ArrayList<>();
            for (Component component : components) {
                String[] lines = component.getString().split("\\R", -1);
                if (lines.length == 1) {
                    expanded.add(component);
                    continue;
                }
                for (String line : lines) {
                    expanded.add(Component.literal(line).withStyle(component.getStyle()));
                }
            }
            return expanded;
        }
        public List<Component> styleTooltipLines(List<Component> lines, ChatFormatting formatting) {
            return lines.stream().map(line -> (Component) line.copy().withStyle(formatting)).toList();
        }

    }
}
