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

import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import net.minecraft.entity.player.EntityPlayerMP;
import org.pircbotx.User;

/**
 * Main class containing all message template token expanding methods
 *
 * @author cnaude
 */
public class ChatTokenizer {

    PurpleIRC plugin;

    /**
     * Class initializer
     *
     * @param plugin
     */
    public ChatTokenizer(PurpleIRC plugin) {
        this.plugin = plugin;
    }

    /**
     * IRC to game chat tokenizer without a message
     *
     * @param ircBot
     * @param user
     * @param channel
     * @param template
     * @return
     */
    public String chatIRCTokenizer(PurpleBot ircBot, User user, org.pircbotx.Channel channel, String template) {
        return plugin.colorConverter.ircColorsToGame(ircUserTokenizer(template, user, ircBot)
                .replace("%NICKPREFIX%", ircBot.getNickPrefix(user, channel))
                .replace("%CHANNEL%", channel.getName()));
    }

    public String ircUserTokenizer(String template, User user, PurpleBot ircBot) {
        String host = user.getHostmask();
        String server = user.getServer();
        String away = user.getAwayMessage();
        String ircNick = user.getNick();
        String customPrefix = ircBot.defaultCustomPrefix;
        if (host == null) {
            host = "";
        }
        if (server == null) {
            server = "";
        }
        if (away == null) {
            away = "";
        }
        plugin.logDebug("customPrefix before: " + customPrefix);
        if (!ircBot.userPrefixes.isEmpty()) {
            for (String key : ircBot.userPrefixes.keySet()) {
                if (key.equalsIgnoreCase(user.getNick()) || ircBot.checkUserMask(user, key)) {
                    customPrefix = ircBot.userPrefixes.get(key);
                    break;
                }
            }
        }
        plugin.logDebug("customPrefix after: " + customPrefix);
        return template.replace("%HOST%", host)
                .replace("%CUSTOMPREFIX%", customPrefix)
                .replace("%NAME%", ircNick)
                .replace("%SERVER%", server)
                .replace("%AWAY%", away);
    }

    public String ircUserTokenizer(String template, User recipient, User kicker, PurpleBot ircBot) {
        String host = kicker.getHostmask();
        String server = kicker.getServer();
        String away = kicker.getAwayMessage();
        String ircNick = kicker.getNick();
        if (host == null) {
            host = "";
        }
        if (server == null) {
            server = "";
        }
        if (away == null) {
            away = "";
        }
        return ircUserTokenizer(template, recipient, ircBot)
                .replace("%KICKERHOST%", host)
                .replace("%KICKER%", ircNick)
                .replace("%KICKERSERVER%", server)
                .replace("%KICKERAWAY%", away);
    }

    /**
     * Normal IRC to game chat tokenizer
     *
     * @param ircBot
     * @param user
     * @param channel
     * @param template
     * @param message
     * @return
     */
    public String ircChatToGameTokenizer(PurpleBot ircBot, User user, org.pircbotx.Channel channel, String template, String message) {
        String ircNick = user.getNick();
        String tmpl;
        EntityPlayerMP player = this.getPlayer(ircNick);
        if (player != null) {
            tmpl = playerTokenizer(player, template);
        } else {
            plugin.logDebug("ircChatToGameTokenizer: null player: " + ircNick);
            tmpl = playerTokenizer(ircNick, template);
        }
        return plugin.colorConverter.ircColorsToGame(ircUserTokenizer(tmpl, user, ircBot)
                .replace("%NICKPREFIX%", ircBot.getNickPrefix(user, channel))
                .replace("%MESSAGE%", message)
                .replace("%CHANNEL%", channel.getName()));
    }

    /**
     * IRC kick message to game
     *
     * @param ircBot
     * @param recipient
     * @param kicker
     * @param reason
     * @param channel
     * @param template
     * @return
     */
    public String ircKickTokenizer(PurpleBot ircBot, User recipient, User kicker, String reason, org.pircbotx.Channel channel, String template) {
        return plugin.colorConverter.ircColorsToGame(ircUserTokenizer(template, recipient, kicker, ircBot)
                .replace("%NICKPREFIX%", ircBot.getNickPrefix(kicker, channel))
                .replace("%REASON%", reason)
                .replace("%CHANNEL%", channel.getName()));
    }

    /**
     * IRC mode change messages
     *
     * @param ircBot
     * @param user
     * @param mode
     * @param channel
     * @param template
     * @return
     */
    public String ircModeTokenizer(PurpleBot ircBot, User user, String mode, org.pircbotx.Channel channel, String template) {
        return plugin.colorConverter.ircColorsToGame(ircUserTokenizer(template, user, ircBot)
                .replace("%MODE%", mode)
                .replace("%NICKPREFIX%", ircBot.getNickPrefix(user, channel))
                .replace("%CHANNEL%", channel.getName()));
    }

    /**
     * IRC notice change messages
     *
     * @param ircBot
     * @param user
     * @param message
     * @param notice
     * @param channel
     * @param template
     * @return
     */
    public String ircNoticeTokenizer(PurpleBot ircBot, User user, String message, String notice, org.pircbotx.Channel channel, String template) {
        return plugin.colorConverter.ircColorsToGame(ircUserTokenizer(template, user, ircBot)
                .replace("%NICKPREFIX%", ircBot.getNickPrefix(user, channel))
                .replace("%MESSAGE%", message)
                .replace("%NOTICE%", notice)
                .replace("%CHANNEL%", channel.getName()));
    }

    /**
     * Game chat to IRC
     *
     * @param pName
     * @param template
     *
     * @param message
     * @return
     */
    public String gameChatToIRCTokenizer(String pName, String template, String message) {
        return plugin.colorConverter.gameColorsToIrc(template
                .replace("%NAME%", pName)
                .replace("%MESSAGE%", plugin.colorConverter.gameColorsToIrc(message)));
    }

    /**
     * Game chat to IRC
     *
     * @param player
     * @param template
     *
     * @param message
     * @return
     */
    public String gameChatToIRCTokenizer(EntityPlayerMP player, String template, String message) {
        if (message == null) {
            message = "";
        }
        return plugin.colorConverter.gameColorsToIrc(playerTokenizer(player, template).replace("%MESSAGE%", message));
    }

    /**
     * Game chat to IRC
     *
     * @param source
     * @param name
     * @param template
     * @param message
     * @return
     */
    public String dynmapWebChatToIRCTokenizer(String source, String name,
            String template, String message) {
        if (message == null) {
            message = "";
        }

        return plugin.colorConverter.gameColorsToIrc(
                playerTokenizer(name, template)
                .replace("%SOURCE%", source)
                .replace("%MESSAGE%", message));
    }

    /**
     * Game player AFK to IRC
     *
     * @param player
     * @param template
     *
     * @return
     */
    public String gamePlayerAFKTokenizer(EntityPlayerMP player, String template) {
        return plugin.colorConverter.gameColorsToIrc(playerTokenizer(player, template));
    }

    /**
     * Game chat to IRC
     *
     * @param template
     * @param message
     * @return
     */
    public String gameChatToIRCTokenizer(String template, String message) {
        return plugin.colorConverter.gameColorsToIrc(template
                .replace("%MESSAGE%", message));
    }

    /**
     * Game kick message to IRC
     *
     * @param player
     * @param template
     * @param reason
     * @param message
     * @return
     */
    public String gameKickTokenizer(EntityPlayerMP player, String template, String message, String reason) {
        return plugin.colorConverter.gameColorsToIrc(
                gameChatToIRCTokenizer(player, template, message)
                .replace("%MESSAGE%", message)
                .replace("%REASON%", reason));
    }

    public String playerTokenizer(EntityPlayerMP player, String message) {
        String pName = player.getCommandSenderName();        
        String displayName = player.getDisplayName();
        String playerIP = player.getPlayerIP();
        String host = plugin.getPlayerHost(playerIP);
        String worldName = "";
        String worldAlias = "";
        String worldColor = "";
        String jobShort = "";
        String job = "";
        if (playerIP == null) {
            playerIP = "";
        }
        if (displayName == null) {
            displayName = "";
        }
        if (player.getServerForPlayer() != null) {
            worldName = player.getServerForPlayer().getWorldInfo().getWorldName();
        }
        plugin.logDebug("[P]Raw message: " + message);
        return message.replace("%DISPLAYNAME%", displayName)
                .replace("%JOBS%", job)
                .replace("%JOBSSHORT%", jobShort)
                .replace("%NAME%", pName)
                .replace("%PLAYERIP%", playerIP)
                .replace("%HOST%", host)
                .replace("%WORLDALIAS%", worldAlias)
                .replace("%WORLDCOLOR%", worldColor)
                .replace("%WORLD%", worldName);
    }

    private String playerTokenizer(String playerName, String message) {
        plugin.logDebug("Tokenizing " + playerName);
        String worldName = plugin.defaultPlayerWorld;

        String displayName = plugin.getDisplayName(playerName);
        plugin.logDebug("playerTokenizer: 7 ");
        plugin.logDebug("[S]Raw message: " + message);
        return message.replace("%DISPLAYNAME%", displayName)
                .replace("%NAME%", playerName)
                .replace("%WORLD%", worldName);
    }

    private EntityPlayerMP getPlayer(String name) {
        EntityPlayerMP player;
        if (plugin.exactNickMatch) {
            plugin.logDebug("Checking for exact player matching " + name);
            player = plugin.getPlayerExact(name);
        } else {
            plugin.logDebug("Checking for player matching " + name);
            player = plugin.getPlayer(name);
        }
        return player;
    }
    
    /**
     *
     * @param player
     * @param template
     * @param cmd
     * @param params
     * @return
     */
    public String gameCommandToIRCTokenizer(EntityPlayerMP player, String template, String cmd, String params) {
        return plugin.colorConverter.gameColorsToIrc(playerTokenizer(player, template)
                .replace("%COMMAND%", cmd)
                .replace("%PARAMS%", params));
    }

    public String targetChatResponseTokenizer(String target, String message, String template) {
        return plugin.colorConverter.gameColorsToIrc(template
                .replace("%TARGET%", target)
                .replace("%MESSAGE%", message)
        );
    }

    public String msgChatResponseTokenizer(String target, String message, String template) {
        return plugin.colorConverter.ircColorsToGame(template
                .replace("%TARGET%", target)
                .replace("%MESSAGE%", message)
        );
    }
}
