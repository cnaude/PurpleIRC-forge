/*
 * Copyright (C) 2014 cnaude
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cnaude.purpleirc.Utilities;

import com.cnaude.purpleirc.PurpleIRC;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.pircbotx.Colors;
import net.minecraft.util.EnumChatFormatting;

/**
 *
 * @author cnaude
 */
public class ColorConverter {

    PurpleIRC plugin;
    private final boolean stripGameColors;
    private final boolean stripIRCColors;
    private final boolean stripIRCBackgroundColors;
    private final HashMap<EnumChatFormatting, String> ircColorMap = new HashMap<>();
    private final HashMap<String, EnumChatFormatting> gameColorMap = new HashMap<>();
    private final Pattern bgColorPattern;
    private final Pattern singleDigitColorPattern;
    private final Pattern stripColorPattern;
    private final Pattern colorHack;
    
    public final char COLOR_CHAR = '\u00A7';
    


    /**
     *
     * @param plugin
     * @param stripGameColors
     * @param stripIRCColors
     * @param stripIRCBackgroundColors
     */
    public ColorConverter(PurpleIRC plugin, boolean stripGameColors, boolean stripIRCColors, boolean stripIRCBackgroundColors) {
        this.stripGameColors = stripGameColors;
        this.stripIRCColors = stripIRCColors;
        this.stripIRCBackgroundColors = stripIRCBackgroundColors;
        this.plugin = plugin;
        buildDefaultColorMaps();
        this.bgColorPattern = Pattern.compile("((\\u0003\\d+),\\d+)");
        this.singleDigitColorPattern = Pattern.compile("((\\u0003)(\\d))\\D+");
        this.colorHack = Pattern.compile("((\\u0003\\d+)(,\\d+))\\D");
        this.stripColorPattern = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");
    }

    /**
     *
     * @param message
     * @return
     */
    public String gameColorsToIrc(String message) {
        if (stripGameColors) {
            return ChatColor.stripColor(message);
        } else {
            String newMessage = message;
            for (EnumChatFormatting gameColor : ircColorMap.keySet()) {
                newMessage = newMessage.replace(gameColor.toString(), ircColorMap.get(gameColor));
            }
            // We return the message with the remaining MC color codes stripped out
            return ChatColor.stripColor(newMessage);
        }
    }

    /**
     *
     * @param message
     * @return
     */
    public String ircColorsToGame(String message) {
        Matcher matcher;
        if (stripIRCBackgroundColors) {
            matcher = bgColorPattern.matcher(message);
            while (matcher.find()) {
                plugin.logDebug("Strip bg color: " + matcher.group(1) + " => " + matcher.group(2));
                message = message.replace(matcher.group(1), matcher.group(2));
            }
        }
        matcher = singleDigitColorPattern.matcher(message);
        while (matcher.find()) {
            plugin.logDebug("Single to double: " + matcher.group(3) + " => "
                    + matcher.group(2) + "0" + matcher.group(3));
            // replace \u0003N with \u00030N
            message = message.replace(matcher.group(1), matcher.group(2) + "0" + matcher.group(3));
        }
        matcher = colorHack.matcher(message);
        while (matcher.find()) {
            plugin.logDebug("Silly IRC colors: " + matcher.group(1) + " => "
                    + matcher.group(2));
            // replace \u0003N,N with \u00030N
            message = message.replace(matcher.group(1), matcher.group(2));
        }

        if (stripIRCColors) {
            return Colors.removeFormattingAndColors(message);
        } else {
            String newMessage = message;
            for (String ircColor : gameColorMap.keySet()) {
                newMessage = newMessage.replace(ircColor, gameColorMap.get(ircColor).toString());
            }
            // We return the message with the remaining IRC color codes stripped out
            return Colors.removeFormattingAndColors(newMessage);
        }
    }

    public void addIrcColorMap(String gameColor, String ircColor) {
        EnumChatFormatting chatColor;
        try {
            chatColor = ChatColor.valueOf(gameColor.toUpperCase());
            if (ircColor.equalsIgnoreCase("strip") && ircColorMap.containsKey(chatColor)) {
                plugin.logDebug("addIrcColorMap: " + ircColor + " => " + gameColor);
                ircColorMap.remove(chatColor);
                return;
            }
        } catch (Exception ex) {
            plugin.logError("Invalid game color: " + gameColor);
            return;
        }
        if (chatColor != null) {
            plugin.logDebug("addIrcColorMap: " + gameColor + " => " + ircColor);
            ircColorMap.put(chatColor, getIrcColor(ircColor));
        }
    }

    public void addGameColorMap(String ircColor, String gameColor) {
        if (gameColor.equalsIgnoreCase("strip") && gameColorMap.containsKey(getIrcColor(ircColor))) {
            plugin.logDebug("addGameColorMap: " + ircColor + " => " + gameColor);
            gameColorMap.remove(getIrcColor(ircColor));
            return;
        }
        EnumChatFormatting chatColor;
        try {
            chatColor = EnumChatFormatting.getValueByName(gameColor.toUpperCase());
        } catch (Exception ex) {
            plugin.logError("Invalid game color: " + gameColor);
            return;
        }
        plugin.logDebug("addGameColorMap: " + ircColor + " => " + gameColor);
        gameColorMap.put(getIrcColor(ircColor), chatColor);
    }

    private String getIrcColor(String ircColor) {
        String s = "";
        try {
            s = (String) Colors.class.getField(ircColor.toUpperCase()).get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            plugin.logError(ex.getMessage());
        }
        if (s.isEmpty()) {
            plugin.logError("Invalid IRC color: " + ircColor);
        }
        return s;
    }

    private void buildDefaultColorMaps() {
        ircColorMap.put(EnumChatFormatting.AQUA, Colors.CYAN);
        ircColorMap.put(EnumChatFormatting.BLACK, Colors.BLACK);
        ircColorMap.put(EnumChatFormatting.BLUE, Colors.BLUE);
        ircColorMap.put(EnumChatFormatting.BOLD, Colors.BOLD);
        ircColorMap.put(EnumChatFormatting.DARK_AQUA, Colors.TEAL);
        ircColorMap.put(EnumChatFormatting.DARK_BLUE, Colors.DARK_BLUE);
        ircColorMap.put(EnumChatFormatting.DARK_GRAY, Colors.DARK_GRAY);
        ircColorMap.put(EnumChatFormatting.DARK_GREEN, Colors.DARK_GREEN);
        ircColorMap.put(EnumChatFormatting.DARK_PURPLE, Colors.PURPLE);
        ircColorMap.put(EnumChatFormatting.DARK_RED, Colors.RED);
        ircColorMap.put(EnumChatFormatting.GOLD, Colors.OLIVE);
        ircColorMap.put(EnumChatFormatting.GRAY, Colors.LIGHT_GRAY);
        ircColorMap.put(EnumChatFormatting.GREEN, Colors.GREEN);
        ircColorMap.put(EnumChatFormatting.LIGHT_PURPLE, Colors.MAGENTA);
        ircColorMap.put(EnumChatFormatting.RED, Colors.RED);
        ircColorMap.put(EnumChatFormatting.UNDERLINE, Colors.UNDERLINE);
        ircColorMap.put(EnumChatFormatting.YELLOW, Colors.YELLOW);
        ircColorMap.put(EnumChatFormatting.WHITE, Colors.WHITE);
        ircColorMap.put(EnumChatFormatting.RESET, Colors.NORMAL);

        gameColorMap.put(Colors.BLACK, EnumChatFormatting.BLACK);
        gameColorMap.put(Colors.BLUE, EnumChatFormatting.BLUE);
        gameColorMap.put(Colors.BOLD, EnumChatFormatting.BOLD);
        gameColorMap.put(Colors.BROWN, EnumChatFormatting.GRAY);
        gameColorMap.put(Colors.CYAN, EnumChatFormatting.AQUA);
        gameColorMap.put(Colors.DARK_BLUE, EnumChatFormatting.DARK_BLUE);
        gameColorMap.put(Colors.DARK_GRAY, EnumChatFormatting.DARK_GRAY);
        gameColorMap.put(Colors.DARK_GREEN, EnumChatFormatting.DARK_GREEN);
        gameColorMap.put(Colors.GREEN, EnumChatFormatting.GREEN);
        gameColorMap.put(Colors.LIGHT_GRAY, EnumChatFormatting.GRAY);
        gameColorMap.put(Colors.MAGENTA, EnumChatFormatting.LIGHT_PURPLE);
        gameColorMap.put(Colors.NORMAL, EnumChatFormatting.RESET);
        gameColorMap.put(Colors.OLIVE, EnumChatFormatting.GOLD);
        gameColorMap.put(Colors.PURPLE, EnumChatFormatting.DARK_PURPLE);
        gameColorMap.put(Colors.RED, EnumChatFormatting.RED);
        gameColorMap.put(Colors.TEAL, EnumChatFormatting.DARK_AQUA);
        gameColorMap.put(Colors.UNDERLINE, EnumChatFormatting.UNDERLINE);
        gameColorMap.put(Colors.WHITE, EnumChatFormatting.WHITE);
        gameColorMap.put(Colors.YELLOW, EnumChatFormatting.YELLOW);
    }
    
    public String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = '\u00A7';
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }
    
}
