package com.cnaude.purpleirc.Utilities;

import java.util.regex.Pattern;
import net.minecraft.util.EnumChatFormatting;

/**
 *
 * @author cnaude
 */
public class ChatColor {
    
    public static final char COLOR_CODE = '\u00A7';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CODE) + "[0-9A-FK-OR]");
    
    public static String stripColor(final String input) {
        if (input == null) {
            return "";
        }
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
    
    public static EnumChatFormatting valueOf(final String input) {
        return EnumChatFormatting.getValueByName(input.toUpperCase());
    }
}
