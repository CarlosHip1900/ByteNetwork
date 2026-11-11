package com.carloship.craft.sdk.utils;

import com.carloship.craft.sdk.server.group.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class TextUtils {

    public static final MiniMessage MM = MiniMessage.builder()
            .editTags(builder -> {
                builder.tag("use", Tag.preProcessParsed("<red>Use: <gray>"));
                builder.tag("skywars", Tag.preProcessParsed("<yellow>Sky Wars: <gray>"));
                builder.tag("sheepwars", Tag.preProcessParsed("<light_purple>Sheep Wars: <gray>"));
            })
            .build();

    public static final String CLICK = "\uD83D\uDDB0";
    public static final String LEFT_CLICK = "'\uD83D\uDDB0"; // '🖰
    public static final String RIGHT_CLICK = "\uD83D\uDDB0'"; // 🖰'
    public static final String SNEAK = sneak("");
    public static final String SNEAK_CLICK = sneak(CLICK);
    public static final String SNEAK_LEFT_CLICK = sneak(LEFT_CLICK);
    public static final String SNEAK_RIGHT_CLICK = sneak(RIGHT_CLICK);

    public static final String CLICK_GREEN = "<green>\uD83D\uDDB0";
    public static final String LEFT_CLICK_GREEN = "<green>'\uD83D\uDDB0"; // '🖰
    public static final String RIGHT_CLICK_GREEN = "<green>\uD83D\uDDB0'"; // 🖰'
    public static final String SNEAK_CLICK_GREEN = sneak(CLICK_GREEN);
    public static final String SNEAK_LEFT_CLICK_GREEN = sneak(LEFT_CLICK_GREEN);
    public static final String SNEAK_RIGHT_CLICK_GREEN = sneak(RIGHT_CLICK_GREEN);

    public static final String DOUBLE_RIGHT_ARROW = "»";

    public static final String KEYBOARD = "\uD83D\uDDAE"; // 🖮

    public static final Component COMMAND_ONLY_PLAYERS = DMM("<red>This command is only available to players");
    public static final Component INTERNAL_ERROR = DMM("<dark_red>An internal error occurred!");

    public static String sneak(String input) {
        return "\uD83D\uDEBC" + input;
    }

    public static boolean isEmpty(Component component) {
        if (component == null) {
            return true;
        }

        return PlainTextComponentSerializer.plainText()
                .serialize(component)
                .isBlank();
    }

    public static Component DMM(String input) {
        return MM.deserialize("<!italic>" + input);
    }

    public static Component NEED_GROUP_ACTION(Group group) {
        return DMM("<red>You must be in the ").append(group.getTag()).append(DMM("<red> group to perform this action."));
    }

    public static Component SDMM(String input) {

        String converted = Pattern.compile("<[^>]+>|[^<]+")
                .matcher(input)
                .replaceAll(match -> {

                    String part = match.group();

                    if (part.startsWith("<") && part.endsWith(">")) {
                        return part;
                    }

                    return SmallCaps.convert(part);
                });

        return MM.deserialize("<!italic>" + converted);
    }

    public static Component SP(TextColor color, String subject, Component details) {
        Component prefix = Component.text(subject).color(color).appendSpace();
        return prefix.append(details);
    }

    public static Component SP(String tag, TextColor color, String txt) {
        return TAG(tag, color).appendSpace().append(Component.text("/", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                .appendSpace()
                .append(DMM("<gray>" + txt));
    }

    public static Component USE(String cmd) {
        return DMM("<red><bold>USE: </bold></red><gray>" + cmd);
    }

    public static Component TAG(String txt, TextColor color) {
        return Component.text(txt.toUpperCase(), color);
    }

    public static Component ERROR(String tag, TextColor color, String txt) {
        return ERROR(TAG(tag, color), txt);
    }

    public static Component ERROR(Component tag, String txt) {
        return tag.appendSpace()
                .append(Component.text("§8§l[§c§l!§8§l]"))
                .appendSpace()
                .append().append(DMM("<gray>" + txt));
    }

    public static Component ERROR(Component details) {
        return SP(NamedTextColor.DARK_RED, "ERROR!", details);
    }

    public static Component FMT_MSG(String tag, TextColor color, Component msg) {
        return Component.text(tag, color)
                .appendSpace()
                .append(Component.text("»", NamedTextColor.DARK_GRAY))
                .appendSpace()
                .append(msg);
    }

    public static Component noPermission() {
        return DMM("<red>You don't have permission...");
    }

    public static @NotNull String formatTime(long remainingTime) {
        long totalSeconds = remainingTime / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder builder = new StringBuilder();
        if (hours > 0) builder.append(hours).append("h ");
        if (minutes > 0) builder.append(minutes).append("m ");
        builder.append(seconds).append("s");

        return builder.toString().trim();
    }

    public static @NotNull String formateDateDMY_HMS(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH'h' mm'm' ss's'").withZone(ZoneId.systemDefault());

        return formatter.format(Instant.ofEpochMilli(timestamp));
    }

    public static @NotNull String formateDateDMY(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

        return formatter.format(Instant.ofEpochMilli(timestamp));
    }

    public static @NotNull String formatDateAgo(long time) {
        long now = System.currentTimeMillis();
        long diff = Math.max(0, now - time);

        long totalSeconds = diff / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) builder.append(days).append("d ");
        if (hours > 0 || days > 0) builder.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) builder.append(minutes).append("m ");
        builder.append(seconds).append("s atrás");

        return builder.toString().trim();
    }

    public static @NotNull Component createRandom(String message, boolean bold) {
        Random random = new Random();
        int red = random.nextInt(1, 255);
        int green = random.nextInt(1, 255);
        int blue = random.nextInt(1, 255);

        return Component.text(bold ? "§l" + message : message, TextColor.color(red, green, blue));
    }

    public static String formatNumberBrazil(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        return decimalFormat.format(number);
    }

    public static List<Component> createNoItalicList(Component... components) {
        List<Component> list = new ArrayList<>();
        Arrays.stream(components).toList().forEach(c -> list.add(c.decoration(TextDecoration.ITALIC, false)));
        return list;
    }
}
