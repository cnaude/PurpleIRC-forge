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
package com.cnaude.purpleirc;

import com.cnaude.purpleirc.IRCListeners.ActionListener;
import com.cnaude.purpleirc.IRCListeners.ConnectListener;
import com.cnaude.purpleirc.IRCListeners.DisconnectListener;
import com.cnaude.purpleirc.IRCListeners.JoinListener;
import com.cnaude.purpleirc.IRCListeners.KickListener;
import com.cnaude.purpleirc.IRCListeners.MessageListener;
import com.cnaude.purpleirc.IRCListeners.ModeListener;
import com.cnaude.purpleirc.IRCListeners.MotdListener;
import com.cnaude.purpleirc.IRCListeners.NickChangeListener;
import com.cnaude.purpleirc.IRCListeners.NoticeListener;
import com.cnaude.purpleirc.IRCListeners.PartListener;
import com.cnaude.purpleirc.IRCListeners.PrivateMessageListener;
import com.cnaude.purpleirc.IRCListeners.QuitListener;
import com.cnaude.purpleirc.IRCListeners.ServerResponseListener;
import com.cnaude.purpleirc.IRCListeners.TopicListener;
import com.cnaude.purpleirc.IRCListeners.WhoisListener;
import com.cnaude.purpleirc.Utilities.CaseInsensitiveMap;
import com.cnaude.purpleirc.Utilities.ChatColor;
import com.cnaude.purpleirc.Utilities.PurpleConfiguration;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSortedSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.cap.TLSCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.yaml.snakeyaml.scanner.ScannerException;

/**
 *
 * @author Chris Naude
 */
public final class PurpleBot {

    protected PircBotX bot;

    protected boolean goodBot;
    public final PurpleIRC plugin;
    private final File file;
    private PurpleConfiguration config;
    private boolean connected;
    public boolean autoConnect;
    public boolean ssl;
    public boolean tls;
    public boolean trustAllCerts;
    public boolean sendRawMessageOnConnect;
    public boolean showMOTD;
    public boolean channelCmdNotifyEnabled;
    public boolean relayPrivateChat;
    public boolean partInvalidChannels;
    public boolean pingFix;
    public int botServerPort;
    public long chatDelay;
    public String botServer;
    public String bindAddress;
    public String nick;
    public String botNick;
    public List<String> altNicks;
    int nickIndex = 0;
    public String botLogin;
    public String botRealName;
    public int ircMaxLineLength;
    public String botServerPass;
    public String charSet;
    public String commandPrefix;
    public String quitMessage;
    public String botIdentPassword;
    public String rawMessage;
    public String channelCmdNotifyMode;
    public String partInvalidChannelsMsg;
    private String connectMessage;
    public ArrayList<String> botChannels;
    public CaseInsensitiveMap<Collection<String>> channelNicks;
    public CaseInsensitiveMap<Collection<String>> tabIgnoreNicks;
    public CaseInsensitiveMap<Collection<String>> filters;
    public CaseInsensitiveMap<String> channelPassword;
    public CaseInsensitiveMap<String> channelTopic;
    public CaseInsensitiveMap<Boolean> channelTopicChanserv;
    public CaseInsensitiveMap<String> activeTopic;
    public CaseInsensitiveMap<String> channelModes;
    public CaseInsensitiveMap<String> joinMsg;
    public CaseInsensitiveMap<Boolean> msgOnJoin;
    public CaseInsensitiveMap<Boolean> channelTopicProtected;
    public CaseInsensitiveMap<Boolean> channelAutoJoin;
    public long channelAutoJoinDelay;
    public CaseInsensitiveMap<Boolean> ignoreIRCChat;
    public CaseInsensitiveMap<Boolean> hideJoinWhenVanished;
    public CaseInsensitiveMap<Boolean> hideListWhenVanished;
    public CaseInsensitiveMap<Boolean> hideQuitWhenVanished;
    public CaseInsensitiveMap<Boolean> invalidCommandPrivate;
    public CaseInsensitiveMap<Boolean> invalidCommandCTCP;
    public CaseInsensitiveMap<Boolean> logIrcToHeroChat;
    public CaseInsensitiveMap<Boolean> enableMessageFiltering;
    private final CaseInsensitiveMap<Boolean> shortify;
    public CaseInsensitiveMap<String> heroChannel;
    public CaseInsensitiveMap<String> townyChannel;
    public CaseInsensitiveMap<Collection<String>> opsList;
    public CaseInsensitiveMap<Collection<String>> voicesList;
    public CaseInsensitiveMap<Collection<String>> worldList;
    public CaseInsensitiveMap<Collection<String>> muteList;
    public CaseInsensitiveMap<Collection<String>> enabledMessages;
    public CaseInsensitiveMap<String> userPrefixes;
    public CaseInsensitiveMap<CaseInsensitiveMap<String>> firstOccurrenceReplacements;
    public String defaultCustomPrefix;
    public CaseInsensitiveMap<CaseInsensitiveMap<CaseInsensitiveMap<String>>> commandMap;
    public CaseInsensitiveMap<CaseInsensitiveMap<List<String>>> extraCommandMap;
    public CaseInsensitiveMap<Long> joinNoticeCooldownMap;
    public ArrayList<CommandSender> whoisSenders;
    public List<String> channelCmdNotifyRecipients;
    public List<String> channelCmdNotifyIgnore;
    private final ArrayList<ListenerAdapter> ircListeners;
    public IRCMessageQueueWatcher messageQueue;
    private final String fileName;
    int joinNoticeCoolDown;
    boolean joinNoticeEnabled;
    boolean joinNoticePrivate;
    boolean joinNoticeCtcp;
    String joinNoticeMessage;
    String version;
    String finger;
    private int reconnectCount;
    private final ReadWriteLock rwl;
    private final Lock wl;

    /**
     *
     * @param file
     * @param plugin
     */
    public PurpleBot(File file, PurpleIRC plugin) {
        this.rwl = new ReentrantReadWriteLock();
        this.wl = rwl.writeLock();
        fileName = file.getName();
        this.altNicks = new ArrayList<>();
        this.connected = false;
        this.botChannels = new ArrayList<>();
        this.ircListeners = new ArrayList<>();
        this.channelCmdNotifyRecipients = new ArrayList<>();
        this.channelCmdNotifyIgnore = new ArrayList<>();
        this.commandMap = new CaseInsensitiveMap<>();
        this.extraCommandMap = new CaseInsensitiveMap<>();
        this.joinNoticeCooldownMap = new CaseInsensitiveMap<>();
        this.enabledMessages = new CaseInsensitiveMap<>();
        this.firstOccurrenceReplacements = new CaseInsensitiveMap<>();
        this.userPrefixes = new CaseInsensitiveMap<>();
        this.muteList = new CaseInsensitiveMap<>();
        this.worldList = new CaseInsensitiveMap<>();
        this.opsList = new CaseInsensitiveMap<>();
        this.voicesList = new CaseInsensitiveMap<>();
        this.heroChannel = new CaseInsensitiveMap<>();
        this.townyChannel = new CaseInsensitiveMap<>();
        this.invalidCommandCTCP = new CaseInsensitiveMap<>();
        this.logIrcToHeroChat = new CaseInsensitiveMap<>();
        this.shortify = new CaseInsensitiveMap<>();
        this.invalidCommandPrivate = new CaseInsensitiveMap<>();
        this.hideQuitWhenVanished = new CaseInsensitiveMap<>();
        this.hideListWhenVanished = new CaseInsensitiveMap<>();
        this.hideJoinWhenVanished = new CaseInsensitiveMap<>();
        this.ignoreIRCChat = new CaseInsensitiveMap<>();
        this.channelAutoJoin = new CaseInsensitiveMap<>();
        this.channelTopicProtected = new CaseInsensitiveMap<>();
        this.channelModes = new CaseInsensitiveMap<>();
        this.activeTopic = new CaseInsensitiveMap<>();
        this.channelTopic = new CaseInsensitiveMap<>();
        this.channelPassword = new CaseInsensitiveMap<>();
        this.tabIgnoreNicks = new CaseInsensitiveMap<>();
        this.filters = new CaseInsensitiveMap<>();
        this.channelNicks = new CaseInsensitiveMap<>();
        this.channelTopicChanserv = new CaseInsensitiveMap<>();
        this.joinMsg = new CaseInsensitiveMap<>();
        this.msgOnJoin = new CaseInsensitiveMap<>();
        this.enableMessageFiltering = new CaseInsensitiveMap<>();
        this.plugin = plugin;
        this.file = file;
        this.reconnectCount = 0;
        whoisSenders = new ArrayList<>();
        try {
            config = new PurpleConfiguration(file, false);
        } catch (FileNotFoundException ex) {
            plugin.logError("Bot load: " + ex.getMessage());
        } catch (IOException ex) {
            plugin.logError("Bot load: " + ex.getMessage());
        }
        goodBot = loadConfig();
        plugin.logDebug("GoodBot: " + goodBot);
        if (goodBot) {
            addListeners();

            version = plugin.getDescription().getFullName() + ", "
                    + plugin.getDescription().getDescription() + " - "
                    + plugin.getDescription().getWebsite();

            buildBot(false);

        } else {
            plugin.logError("Error loading " + this.fileName);
        }

        messageQueue = new IRCMessageQueueWatcher(this, plugin);

    }

    public void buildBot(final boolean reload) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Configuration.Builder configBuilder = new Configuration.Builder()
                        .setName(botNick)
                        .setLogin(botLogin)
                        .setAutoNickChange(true)
                        .setVersion(version)
                        .setFinger(finger)
                        .setCapEnabled(true)
                        .setMessageDelay(chatDelay)
                        .setRealName(botRealName)
                        .setMaxLineLength(ircMaxLineLength)
                        //.setAutoReconnect(autoConnect) // Why doesn't this work?
                        .setServer(botServer, botServerPort, botServerPass);
                //addAutoJoinChannels(configBuilder);
                for (ListenerAdapter ll : ircListeners) {
                    configBuilder.addListener(ll);
                }
                if (!botIdentPassword.isEmpty()) {
                    if (!reload) {
                        plugin.logInfo("Setting IdentPassword ...");
                    }
                    configBuilder.setNickservPassword(botIdentPassword);
                }
                if (tls) {
                    plugin.logInfo("Enabling TLS ...");
                    configBuilder.addCapHandler(new TLSCapHandler());
                } else if (ssl) {
                    UtilSSLSocketFactory socketFactory = new UtilSSLSocketFactory();
                    socketFactory.disableDiffieHellman();
                    if (trustAllCerts) {
                        plugin.logInfo("Enabling SSL and trusting all certificates ...");
                        socketFactory.trustAllCertificates();
                    } else {
                        plugin.logInfo("Enabling SSL ...");
                    }
                    configBuilder.setSocketFactory(socketFactory);
                }

                if (charSet.isEmpty()) {
                    if (!reload) {
                        plugin.logInfo("Using default character set: " + Charset.defaultCharset());
                    }
                } else if (Charset.isSupported(charSet)) {
                    if (!reload) {
                        plugin.logInfo("Using character set: " + charSet);
                    }
                    configBuilder.setEncoding(Charset.forName(charSet));
                } else {
                    plugin.logError("Invalid character set: " + charSet);
                    if (!reload) {
                        plugin.logInfo("Available character sets: " + Joiner.on(", ").join(Charset.availableCharsets().keySet()));
                        plugin.logInfo("Using default character set: " + Charset.defaultCharset());
                    }
                }
                if (!bindAddress.isEmpty()) {
                    if (!reload) {
                        plugin.logInfo("Binding to " + bindAddress);
                    }
                    try {
                        configBuilder.setLocalAddress(InetAddress.getByName(bindAddress));
                    } catch (UnknownHostException ex) {
                        plugin.logError(ex.getMessage());
                    }
                }
                Configuration configuration = configBuilder.buildConfiguration();
                bot = new PircBotX(configuration);
                if (autoConnect) {
                    asyncConnect(reload);
                } else {
                    plugin.logInfo("Auto-connect is disabled. To connect: /irc connect " + bot.getNick());
                }
                plugin.logDebug("Max line length: " + configBuilder.getMaxLineLength());
            }
        }).start();

    }

    private void addListeners() {
        ircListeners.add(new ActionListener(plugin, this));
        ircListeners.add(new ConnectListener(plugin, this));
        ircListeners.add(new DisconnectListener(plugin, this));
        ircListeners.add(new JoinListener(plugin, this));
        ircListeners.add(new KickListener(plugin, this));
        ircListeners.add(new MessageListener(plugin, this));
        ircListeners.add(new ModeListener(plugin, this));
        ircListeners.add(new NickChangeListener(plugin, this));
        ircListeners.add(new NoticeListener(plugin, this));
        ircListeners.add(new PartListener(plugin, this));
        ircListeners.add(new PrivateMessageListener(plugin, this));
        ircListeners.add(new QuitListener(plugin, this));
        ircListeners.add(new TopicListener(plugin, this));
        ircListeners.add(new WhoisListener(plugin, this));
        ircListeners.add(new MotdListener(plugin, this));
        ircListeners.add(new ServerResponseListener(plugin, this));
    }

    public void autoJoinChannels() {
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (String channelName : botChannels) {
                    if (channelAutoJoin.containsKey(channelName)) {
                        if (channelAutoJoin.get(channelName)) {
                            if (bot.isConnected()) {
                                if (channelPassword.get(channelName).isEmpty()) {
                                    bot.sendIRC().joinChannel(channelName);
                                } else {
                                    bot.sendIRC().joinChannel(channelName, channelPassword.get(channelName));
                                }
                            }
                        }
                    }
                }
            }
        }, channelAutoJoinDelay);

    }

    public void reload(CommandSender sender) {
        sender.sendMessage("Reloading bot: " + botNick);
        reload();
    }

    public void reload() {
        asyncQuit(true);
    }

    /**
     *
     * @param sender
     */
    public void reloadConfig(CommandSender sender) {
        goodBot = loadConfig();
        if (goodBot) {
            sender.sendMessage("[PurpleIRC] [" + botNick + "] IRC bot configuration reloaded.");
        } else {
            sender.sendMessage("[PurpleIRC] [" + botNick + "] " + EnumChatFormatting.RED + "Error loading bot configuration!");
        }
    }

    /**
     *
     * @param channelName
     * @param sender
     * @param user
     */
    public void mute(String channelName, CommandSender sender, String user) {
        if (muteList.get(channelName).contains(user)) {
            sender.sendMessage("User '" + user + "' is already muted.");
        } else {
            sender.sendMessage("User '" + user + "' is now muted.");
            muteList.get(channelName).add(user);
            saveConfig("channels." + encodeChannel(getConfigChannelName(channelName)) + ".muted", muteList.get(channelName));
        }
    }

    /**
     *
     * @param channelName
     * @param sender
     */
    public void muteList(String channelName, CommandSender sender) {
        if (muteList.get(channelName).isEmpty()) {
            sender.sendMessage("There are no users muted for " + channelName);
        } else {
            sender.sendMessage("Muted users for " + channelName
                    + ": " + Joiner.on(", ").join(muteList.get(channelName)));
        }
    }

    /**
     *
     * @param channelName
     * @param sender
     * @param user
     */
    public void unMute(String channelName, CommandSender sender, String user) {
        if (muteList.get(channelName).contains(user)) {
            sender.sendMessage("User '" + user + "' is no longer muted.");
            muteList.get(channelName).remove(user);
            saveConfig("channels." + encodeChannel(getConfigChannelName(channelName)) + ".muted", muteList.get(channelName));
        } else {
            sender.sendMessage("User '" + user + "' is not muted.");
        }
    }

    public void asyncConnect(CommandSender sender) {
        sender.sendMessage(connectMessage);
        asyncConnect(false);
    }

    public boolean isShortifyEnabled(String channelName) {
        if (shortify.containsKey(channelName)) {
            return shortify.get(channelName);
        }
        return false;
    }

    /**
     *
     * @param reload - true if this is a result of auto reconnect
     */
    public void asyncConnect(final boolean reload) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!reload) {
                        plugin.logInfo(connectMessage);
                    }
                    bot.startBot();
                    reconnectCount = 0;
                } catch (IOException | IrcException ex) {
                    if (reconnectCount <= plugin.reconnectSuppression) {
                        plugin.logError("Problem connecting to " + botServer
                                + " [Nick: " + botNick + "] [Error: " + ex.getMessage() + "]");
                    }
                    reconnectCount++;
                }
            }
        }).start();
    }

    public void asyncIRCMessage(final String target, final String message) {
        plugin.logDebug("Entering aysncIRCMessage");
        messageQueue.add(new IRCMessage(target, plugin.colorConverter.
                gameColorsToIrc(message), false));
    }

    public void asyncCTCPMessage(final String target, final String message) {
        plugin.logDebug("Entering asyncCTCPMessage");
        messageQueue.add(new IRCMessage(target, plugin.colorConverter.gameColorsToIrc(message), true));
    }

    public void blockingIRCMessage(final String target, final String message) {
        if (!this.isConnected()) {
            return;
        }
        plugin.logDebug("[blockingIRCMessage] About to send IRC message to " + target);
        try {
            bot.sendIRC().message(target, plugin.colorConverter.gameColorsToIrc(message));
        } catch (Exception ex) {
            plugin.logError("Problem sending IRC message: " + ex.getMessage());
        }
        plugin.logDebug("[blockingIRCMessage] Message sent to " + target);
    }

    public void blockingCTCPMessage(final String target, final String message) {
        if (!this.isConnected()) {
            return;
        }
        plugin.logDebug("[blockingCTCPMessage] About to send IRC message to " + target);
        try {
            bot.sendIRC().ctcpResponse(target, plugin.colorConverter.gameColorsToIrc(message));
        } catch (Exception ex) {
            plugin.logError("Problem sending IRC CTCP message: " + ex.getMessage());
        }
        plugin.logDebug("[blockingCTCPMessage] Message sent to " + target);
    }

    public void asyncCTCPCommand(final String target, final String command) {
        if (!this.isConnected()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().ctcpCommand(target, command);
            }
        }).start();
    }

    /**
     *
     * @param sender
     */
    public void saveConfig(CommandSender sender) {
        if (goodBot) {
            try {
                config.save();
                sender.sendMessage(plugin.LOG_HEADER_F
                        + " Saving bot \"" + botNick + "\" to " + file.getName());
            } catch (IOException ex) {
                plugin.logError(ex.getMessage());
                sender.sendMessage(ex.getMessage());
            }
        } else {
            sender.sendMessage(plugin.LOG_HEADER_F
                    + EnumChatFormatting.RED + " Not saving bot \"" + botNick + "\" to " + file.getName());
        }
    }

    /**
     *
     */
    public void saveConfig() {
        try {
            config.save();
        } catch (IOException ex) {
            plugin.logError(ex.getMessage());
        }
    }

    /**
     *
     * @param section
     * @param obj
     */
    public void saveConfig(String section, Object obj) {
        plugin.logDebug("Saving [" + section + "]: " + obj.toString());
        config.set(section, obj);
        saveConfig();
    }

    /**
     *
     * @param sender
     * @param newNick
     */
    public void asyncChangeNick(final CommandSender sender, final String newNick) {
        if (!this.isConnected()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().changeNick(newNick);
            }
        }).start();
        sender.sendMessage("Setting nickname to " + newNick);
        saveConfig("nick", newNick);

    }

    public void asyncJoinChannel(final String channelName, final String password) {
        if (!this.isConnected()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().joinChannel(channelName, password);
            }
        }).start();
    }

    public void asyncNotice(final String target, final String message) {
        if (!this.isConnected()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().notice(target, message);
            }
        }).start();
    }

    public void asyncRawlineNow(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                bot.sendRaw().rawLineNow(message);
            }
        }).start();
    }

    public void asyncIdentify(final String password) {
        if (!this.isConnected()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                bot.sendIRC().identify(password);
            }
        }).start();
    }

    /**
     *
     * @param sender
     * @param newLogin
     */
    public void changeLogin(CommandSender sender, String newLogin) {
        sender.sendMessage(EnumChatFormatting.DARK_PURPLE
                + "Login set to " + EnumChatFormatting.WHITE
                + newLogin + EnumChatFormatting.DARK_PURPLE
                + ". Reload the bot for the change to take effect.");
        saveConfig("login", newLogin);
    }

    private void sanitizeServerName() {
        botServer = botServer.replace("^.*\\/\\/", "");
        botServer = botServer.replace(":\\d+$", "");
        saveConfig("server", botServer);
    }

    private boolean loadConfig() {
        try {
            config.load();
            autoConnect = config.getOption("autoconnect", true);
            tls = config.getOption("tls", false);
            ssl = config.getOption("ssl", false);
            trustAllCerts = config.getOption("trust-all-certs", false);
            sendRawMessageOnConnect = config.getOption("raw-message-on-connect", false);
            rawMessage = config.getOption("raw-message", "");
            relayPrivateChat = config.getOption("relay-private-chat", false);
            partInvalidChannels = config.getOption("part-invalid-channels", false);
            pingFix = config.getOption("zero-width-space", false);
            partInvalidChannelsMsg = config.getOption("part-invalid-channels-message", "");
            nick = config.getOption("nick", "");
            botNick = nick;
            altNicks = config.getOption("alt-nicks", new ArrayList<String>());
            plugin.loadTemplates(config, botNick);
            botLogin = config.getOption("login", "PircBot");
            botRealName = config.getOption("realname", "PircBot");
            ircMaxLineLength = config.getOption("max-line-length", 512);
            if (botRealName.isEmpty()) {
                botRealName = plugin.getDescription().getWebsite();
            }
            botServer = config.getOption("server", "");
            bindAddress = config.getOption("bind", "");
            channelAutoJoinDelay = config.getOption("channel-auto-join-delay", 20L);
            charSet = config.getOption("charset", "");
            sanitizeServerName();
            showMOTD = config.getOption("show-motd", false);
            botServerPort = config.getOption("port", 6667);
            botServerPass = config.getOption("password", "");
            botIdentPassword = config.getOption("ident-password", "");
            commandPrefix = config.getOption("command-prefix", ".");
            chatDelay = config.getOption("message-delay", 1000);
            finger = config.getOption("finger-reply", "PurpleIRC");
            plugin.logDebug("Message Delay => " + chatDelay);
            quitMessage = plugin.colorConverter.translateAlternateColorCodes('&', config.getOption("quit-message", ""));
            plugin.logDebug("Nick => " + botNick);
            plugin.logDebug("Login => " + botLogin);
            plugin.logDebug("Server => " + botServer);
            plugin.logDebug("Channel Auto Join Delay => " + channelAutoJoinDelay);
            plugin.logDebug(("Bind => ") + bindAddress);
            plugin.logDebug("SSL => " + ssl);
            plugin.logDebug("TLS => " + tls);
            plugin.logDebug("Trust All Certs => " + trustAllCerts);
            plugin.logDebug("Port => " + botServerPort);
            plugin.logDebug("Command Prefix => " + commandPrefix);
            //plugin.logDebug("Server Password => " + botServerPass);
            plugin.logDebug("Quit Message => " + quitMessage);
            botChannels.clear();
            opsList.clear();
            voicesList.clear();
            muteList.clear();
            enabledMessages.clear();
            userPrefixes.clear();
            firstOccurrenceReplacements.clear();
            worldList.clear();
            commandMap.clear();
            extraCommandMap.clear();

            for (String s : config.getOption("custom-prefixes", new ArrayList<String>())) {
                String pair[] = s.split(" ", 2);
                if (pair.length > 0) {
                    userPrefixes.put(pair[0], plugin.colorConverter.translateAlternateColorCodes('&', pair[1]));
                }
            }
            for (String key : userPrefixes.keySet()) {
                plugin.logDebug(" CustomPrefix: " + key + " => " + userPrefixes.get(key));
            }
            defaultCustomPrefix = plugin.colorConverter.translateAlternateColorCodes('&', config.getOption("custom-prefix-default", "[IRC]"));

            for (String s : config.getOption("replace-first-occurrences", new ArrayList<String>())) {
                String pair[] = s.split(" ", 3);
                if (pair.length > 2) {
                    CaseInsensitiveMap rfo = new CaseInsensitiveMap<>();
                    rfo.put(pair[1], pair[2]);
                    firstOccurrenceReplacements.put(pair[0], rfo);
                    plugin.logDebug("ReplaceFirstOccurence: " + pair[0] + " => " + pair[1] + " => " + pair[2]);
                }
            }

            for (String s : config.getOption("command-notify", "recipients", new ArrayList<String>())) {
                if (!channelCmdNotifyRecipients.contains(s)) {
                    channelCmdNotifyRecipients.add(s);
                }
                plugin.logDebug(" Command Notify Recipient => " + s);
            }

            for (String s : config.getOption("command-notify", "ignore", new ArrayList<String>())) {
                if (!channelCmdNotifyIgnore.contains(s)) {
                    channelCmdNotifyIgnore.add(s);
                }
                plugin.logDebug(" Command Notify Ignore => " + s);
            }

            channelCmdNotifyEnabled = config.getOption("command-notify", "enabled", false);
            plugin.logDebug(" CommandNotifyEnabled => " + channelCmdNotifyEnabled);

            channelCmdNotifyMode = config.getOption("command-notify", "mode", "msg");
            plugin.logDebug(" CommandNotifyMode => " + channelCmdNotifyMode);

            if (!config.contains("channels")) {
                plugin.logError("No channels found!");
                return false;
            } else {
                for (String enChannelName : config.getMap("channels").keySet()) {
                    String channelName = decodeChannel(enChannelName);
                    if (isValidChannel(channelName)) {
                        plugin.logError("Ignoring duplicate channel: " + channelName);
                        continue;
                    }
                    plugin.logDebug("Channel  => " + channelName);
                    botChannels.add(channelName);

                    channelAutoJoin.put(channelName, config.getChannelOption(enChannelName, "autojoin", true));
                    plugin.logDebug("  Autojoin => " + channelAutoJoin.get(channelName));

                    channelPassword.put(channelName, config.getChannelOption(enChannelName, "password", ""));

                    channelTopic.put(channelName, config.getChannelOption(enChannelName, "topic", ""));
                    plugin.logDebug("  Topic => " + channelTopic.get(channelName));

                    channelModes.put(channelName, config.getChannelOption(enChannelName, "modes", ""));
                    plugin.logDebug("  Channel Modes => " + channelModes.get(channelName));

                    channelTopicProtected.put(channelName, config.getChannelOption(enChannelName, "topic-protect", false));
                    plugin.logDebug("  Topic Protected => " + channelTopicProtected.get(channelName).toString());

                    channelTopicChanserv.put(channelName, config.getChannelOption(enChannelName, "topic-chanserv", false));
                    plugin.logDebug("  Topic Chanserv Mode => " + channelTopicChanserv.get(channelName).toString());

                    ignoreIRCChat.put(channelName, config.getChannelOption(enChannelName, "ignore-irc-chat", false));
                    plugin.logDebug("  IgnoreIRCChat => " + ignoreIRCChat.get(channelName));

                    Map<String, Object> icm = config.getChannelOption(enChannelName, "invalid-command", new HashMap<String, Object>());
                    invalidCommandPrivate.put(channelName, false);
                    if (icm.containsKey("private")) {
                        if (icm.get("private") instanceof Boolean) {
                            invalidCommandPrivate.put(channelName, (Boolean) icm.get("private"));
                        }
                    }
                    plugin.logDebug("  InvalidCommandPrivate => " + invalidCommandPrivate.get(channelName));
                    invalidCommandCTCP.put(channelName, false);
                    if (icm.containsKey("ctcp")) {
                        if (icm.get("ctcp") instanceof Boolean) {
                            invalidCommandCTCP.put(channelName, (Boolean) icm.get("ctcp"));
                        }
                    }
                    plugin.logDebug("  InvalidCommandCTCP => " + invalidCommandCTCP.get(channelName));
                    icm.clear();

                    joinMsg.put(channelName, config.getChannelOption(enChannelName, "raw-message", ""));
                    plugin.logDebug("  JoinMessage => " + joinMsg.get(channelName));

                    msgOnJoin.put(channelName, config.getChannelOption(enChannelName, "raw-message-on-joint", false));
                    plugin.logDebug("  SendMessageOnJoin => " + msgOnJoin.get(channelName));

                    enableMessageFiltering.put(channelName, config.getChannelOption(enChannelName, "enable-filtering", false));
                    plugin.logDebug("  EnableMessageFiltering => " + enableMessageFiltering.get(channelName));

                    opsList.put(channelName, config.getChannelOption(enChannelName, "ops", new ArrayList<String>()));
                    voicesList.put(channelName, config.getChannelOption(enChannelName, "voices", new ArrayList<String>()));
                    muteList.put(channelName, config.getChannelOption(enChannelName, "muted", new ArrayList<String>()));
                    enabledMessages.put(channelName, config.getChannelOption(enChannelName, "enabled-messages", new ArrayList<String>()));
                    worldList.put(channelName, config.getChannelOption(enChannelName, "worlds", new ArrayList<String>()));
                    filters.put(channelName, config.getChannelOption(enChannelName, "filter-list", new ArrayList<String>()));

                    // build join notice
                    joinNoticeCoolDown = config.getJoinNoticeOption(enChannelName, "cooledown", 60);
                    joinNoticeEnabled = config.getJoinNoticeOption(enChannelName, "enabled", false);
                    joinNoticePrivate = config.getJoinNoticeOption(enChannelName, "private", true);
                    joinNoticeCtcp = config.getJoinNoticeOption(enChannelName, "ctcp", true);
                    joinNoticeMessage = config.getJoinNoticeOption(enChannelName, "message", "");

                    plugin.logDebug("join-notice.cooldown: " + joinNoticeCoolDown);
                    plugin.logDebug("join-notice.enabled: " + joinNoticeEnabled);
                    plugin.logDebug("join-notice.private: " + joinNoticePrivate);
                    plugin.logDebug("join-notice.ctcp: " + joinNoticeCtcp);
                    plugin.logDebug("join-notice.message: " + joinNoticeMessage);

                    // build command map
                    CaseInsensitiveMap<CaseInsensitiveMap<String>> map = new CaseInsensitiveMap<>();
                    CaseInsensitiveMap<List<String>> extraMap = new CaseInsensitiveMap<>();
                    try {
                        plugin.logDebug("Custom commands for " + channelName);
                        Map<String, Object> cmap = config.getChannelCommands(enChannelName);
                        for (String command : cmap.keySet()) {
                            plugin.logDebug("  " + commandPrefix + command);

                            CaseInsensitiveMap<String> optionPair = new CaseInsensitiveMap<>();
                            optionPair.put("modes", config.getChannelCommandOption(cmap, command, "modes", "*"));
                            optionPair.put("private", config.getChannelCommandOption(cmap, command, "private", "false"));
                            optionPair.put("ctcp", config.getChannelCommandOption(cmap, command, "ctcp", "false"));
                            optionPair.put("game_command", config.getChannelCommandOption(cmap, command, "game_command", ""));
                            optionPair.put("sender", config.getChannelCommandOption(cmap, command, "sender", "CONSOLE"));
                            optionPair.put("private_listen", config.getChannelCommandOption(cmap, command, "private_listen", "true"));
                            optionPair.put("channel_listen", config.getChannelCommandOption(cmap, command, "channel_listen", "true"));
                            optionPair.put("perm", config.getChannelCommandOption(cmap, command, "perm", ""));

                            for (String s : optionPair.keySet()) {
                                plugin.logDebug("    " + s + " => " + optionPair.get(s));
                            }

                            List<String> extraCommands = new ArrayList<>();
                            extraCommands.addAll(config.getChannelCommandOption(cmap, command, "extra_commands", new ArrayList<String>()));

                            plugin.logDebug("    extra_commands: " + extraCommands.toString());

                            map.put(command, optionPair);
                            extraMap.put(command, extraCommands);
                        }
                    } catch (Exception ex) {
                        plugin.logError("No commands found for channel " + enChannelName);
                    }
                    commandMap.put(channelName, map);
                    extraCommandMap.put(channelName, extraMap);
                    if (map.isEmpty()) {
                        plugin.logInfo("No commands specified!");
                    }
                    connectMessage = "Connecting to " + botServer + ":"
                            + botServerPort + ": [Nick: " + botNick
                            + "] [SSL: " + ssl + "]" + " [TrustAllCerts: "
                            + trustAllCerts + "] [TLS: " + tls + "]";
                }
            }
        } catch (IOException | ScannerException ex) {
            plugin.logError(ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     *
     * @param sender
     * @param delay
     */
    public void setIRCDelay(CommandSender sender, long delay) {
        saveConfig("message-delay", delay);
        sender.sendMessage(EnumChatFormatting.DARK_PURPLE
                + "IRC message delay changed to "
                + EnumChatFormatting.WHITE + delay + EnumChatFormatting.DARK_PURPLE + " ms. "
                + "Reload for the change to take effect.");
    }

    private boolean isPlayerInValidWorld(EntityPlayerMP player, String channelName) {
        if (worldList.containsKey(channelName)) {
            if (worldList.get(channelName).contains("*")) {
                return true;
            }
            if (worldList.get(channelName).contains(player.worldObj.getWorldInfo().getWorldName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called from normal game chat listener
     *
     * @param player
     * @param message
     */
    public void gameChat(EntityPlayerMP player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (!isPlayerInValidWorld(player, channelName)) {
                continue;
            }
            if (isMessageEnabled(channelName, TemplateName.GAME_CHAT)) {
                plugin.logDebug("[" + TemplateName.GAME_CHAT + "] => "
                        + channelName + " => " + message);
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(botNick, TemplateName.GAME_CHAT), message));
            } else {
                plugin.logDebug("Ignoring message due to "
                        + TemplateName.GAME_CHAT + " not being listed.");
            }
        }
    }

    // Called from /irc send
    /**
     *
     * @param player
     * @param channelName
     * @param message
     */
    public void gameChat(EntityPlayerMP player, String channelName, String message) {
        if (!this.isConnected()) {
            return;
        }
        if (isValidChannel(channelName)) {
            asyncIRCMessage(channelName, plugin.tokenizer
                    .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                            botNick, TemplateName.GAME_SEND), message));
        }
    }

    // Called from CleverEvent
    /**
     *
     * @param cleverBotName
     * @param message
     */
    public void cleverChat(String cleverBotName, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, "clever-chat")) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(cleverBotName, plugin.getMsgTemplate(botNick, "clever-send"), message));
            }
        }
    }

    /**
     *
     * @param channelName
     * @param message
     */
    public void consoleChat(String channelName, String message) {
        if (!this.isConnected()) {
            return;
        }
        if (isValidChannel(channelName)) {
            asyncIRCMessage(channelName, plugin.tokenizer
                    .gameChatToIRCTokenizer("CONSOLE", message, plugin.getMsgTemplate(
                            botNick, TemplateName.GAME_SEND)));
        }
    }

    /**
     *
     * @param message
     */
    public void consoleChat(String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.CONSOLE_CHAT)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(plugin.getMsgTemplate(botNick,
                                TemplateName.CONSOLE_CHAT), plugin.colorConverter.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameBroadcast(EntityPlayerMP player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.BROADCAST_MESSAGE)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin
                                .getMsgTemplate(botNick, TemplateName.BROADCAST_MESSAGE),
                                plugin.colorConverter.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param name
     * @param message
     * @param source
     */
    public void dynmapWebChat(String source, String name, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.DYNMAP_WEB_CHAT)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .dynmapWebChatToIRCTokenizer(source, name, plugin.getMsgTemplate(
                                botNick, TemplateName.DYNMAP_WEB_CHAT),
                                plugin.colorConverter.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param message
     */
    public void consoleBroadcast(String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.BROADCAST_CONSOLE_MESSAGE)) {
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(plugin.getMsgTemplate(botNick,
                                TemplateName.BROADCAST_CONSOLE_MESSAGE), plugin.colorConverter.translateAlternateColorCodes('&', message)));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameJoin(EntityPlayerMP player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_JOIN)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                botNick, TemplateName.GAME_JOIN), message));
            } else {
                plugin.logDebug("Not sending join message due to " + TemplateName.GAME_JOIN + " being disabled");
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameQuit(EntityPlayerMP player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_QUIT)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                botNick, TemplateName.GAME_QUIT), message));
            }
        }
    }

    /**
     *
     * @param player
     * @param achievement
     */
    public void gameAchievement(EntityPlayerMP player, Achievement achievement) {
        if (!this.isConnected()) {
            return;
        }
        String message = achievement.func_150951_e().getUnformattedText();
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_ACHIEVEMENT)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                botNick, TemplateName.GAME_ACHIEVEMENT), message));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     * @param reason
     */
    public void gameKick(EntityPlayerMP player, String message, String reason) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_KICK)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameKickTokenizer(player, plugin.getMsgTemplate(
                                botNick, TemplateName.GAME_KICK), message, reason));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     */
    public void gameAction(EntityPlayerMP player, String message) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, TemplateName.GAME_ACTION)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                botNick, TemplateName.GAME_ACTION), message));
            }
        }
    }

    /**
     *
     * @param player
     * @param message
     * @param templateName
     */
    public void gameDeath(EntityPlayerMP player, String message, String templateName) {
        if (!this.isConnected()) {
            return;
        }
        for (String channelName : botChannels) {
            if (isMessageEnabled(channelName, templateName)) {
                if (!isPlayerInValidWorld(player, channelName)) {
                    return;
                }
                asyncIRCMessage(channelName, plugin.tokenizer
                        .gameChatToIRCTokenizer(player, plugin.getMsgTemplate(
                                botNick, templateName), message));
            }
        }
    }

    /**
     *
     * @param channelName
     * @param topic
     * @param sender
     */
    public void changeTopic(String channelName, String topic, CommandSender sender) {
        Channel channel = this.getChannel(channelName);
        String tTopic = tokenizedTopic(topic);
        if (channel != null) {
            setTheTopic(channel, tTopic);
            saveConfig("channels." + encodeChannel(getConfigChannelName(channelName)) + ".topic", topic);
            channelTopic.put(channelName, topic);
            sender.sendMessage("IRC topic for " + channelName + " changed to \"" + topic + "\"");
        } else {
            sender.sendMessage("Invalid channel: " + channelName);
        }
    }

    public String getConfigChannelName(String channelName) {
        for (String s : botChannels) {
            if (channelName.equalsIgnoreCase(s)) {
                return s;
            }
        }
        return channelName;
    }

    public Channel getChannel(String channelName) {
        Channel channel = null;
        for (Channel c : getChannels()) {
            if (c.getName().equalsIgnoreCase(channelName)) {
                return c;
            }
        }
        return channel;
    }

    /**
     *
     * @param sender
     * @param botServer
     */
    public void setServer(CommandSender sender, String botServer) {
        setServer(sender, botServer, autoConnect);
    }

    /**
     *
     * @param sender
     * @param server
     * @param auto
     */
    public void setServer(CommandSender sender, String server, Boolean auto) {

        if (server.contains(":")) {
            botServerPort = Integer.parseInt(server.split(":")[1]);
            botServer = server.split(":")[0];
        } else {
            botServer = server;
        }
        sanitizeServerName();
        autoConnect = auto;
        saveConfig("server", botServer);
        saveConfig("port", botServerPort);
        saveConfig("autoconnect", autoConnect);

        sender.sendMessage("IRC server changed to \"" + botServer + ":"
                + botServerPort + "\". (AutoConnect: "
                + autoConnect + ")");
    }

    /**
     *
     * @param channelName
     * @param userMask
     * @param sender
     */
    public void addOp(String channelName, String userMask, CommandSender sender) {
        if (opsList.get(channelName).contains(userMask)) {
            sender.sendMessage("User mask " + EnumChatFormatting.WHITE + userMask
                    + EnumChatFormatting.RESET + " is already in the ops list.");
        } else {
            sender.sendMessage("User mask " + EnumChatFormatting.WHITE + userMask
                    + EnumChatFormatting.RESET + " has been added to the ops list.");
            opsList.get(channelName).add(userMask);
        }
        saveConfig("channels." + encodeChannel(getConfigChannelName(channelName)) + ".ops", opsList.get(channelName));
    }

    /**
     *
     * @param channelName
     * @param userMask
     * @param sender
     */
    public void addVoice(String channelName, String userMask, CommandSender sender) {
        if (voicesList.get(channelName).contains(userMask)) {
            sender.sendMessage("User mask " + EnumChatFormatting.WHITE + userMask
                    + EnumChatFormatting.RESET + " is already in the voices list.");
        } else {
            sender.sendMessage("User mask " + EnumChatFormatting.WHITE + userMask
                    + EnumChatFormatting.RESET + " has been added to the voices list.");
            voicesList.get(channelName).add(userMask);
        }
        saveConfig("channels." + encodeChannel(getConfigChannelName(channelName)) + ".voices", voicesList.get(channelName));
    }

    /**
     *
     * @param channelName
     * @param userMask
     * @param sender
     */
    public void removeOp(String channelName, String userMask, CommandSender sender) {
        if (opsList.get(channelName).contains(userMask)) {
            sender.sendMessage("User mask " + EnumChatFormatting.WHITE + userMask
                    + EnumChatFormatting.RESET + " has been removed to the ops list.");
            opsList.get(channelName).remove(userMask);
        } else {
            sender.sendMessage("User mask " + EnumChatFormatting.WHITE + userMask
                    + EnumChatFormatting.RESET + " is not in the ops list.");
        }
        saveConfig("channels." + encodeChannel(getConfigChannelName(channelName)) + ".ops", opsList.get(channelName));
    }

    /**
     *
     * @param channelName
     * @param userMask
     * @param sender
     */
    public void removeVoice(String channelName, String userMask, CommandSender sender) {
        if (voicesList.get(channelName).contains(userMask)) {
            sender.sendMessage("User mask " + EnumChatFormatting.WHITE + userMask
                    + EnumChatFormatting.RESET + " has been removed to the voices list.");
            voicesList.get(channelName).remove(userMask);
        } else {
            sender.sendMessage("User mask " + EnumChatFormatting.WHITE + userMask
                    + EnumChatFormatting.RESET + " is not in the voices list.");
        }
        saveConfig("channels." + encodeChannel(getConfigChannelName(channelName)) + ".voices", voicesList.get(channelName));

    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void op(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().op(user);
                    return;
                }
            }
        }
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void voice(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().voice(user);
                    return;
                }
            }
        }
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void deOp(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().deOp(user);
                    return;
                }
            }
        }
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void deVoice(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().deVoice(user);
                    return;
                }
            }
        }
    }

    /**
     *
     * @param channelName
     * @param nick
     */
    public void kick(String channelName, String nick) {
        Channel channel;
        channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                if (user.getNick().equals(nick)) {
                    channel.send().kick(user);
                    return;
                }
            }
        }
    }

    private String encodeChannel(String s) {
        return s.replace(".", "%2E");
    }

    private String decodeChannel(String s) {
        return s.replace("%2E", ".");
    }

    /**
     *
     * @param channel
     * @param topic
     * @param setBy
     */
    public void fixTopic(Channel channel, String topic, String setBy) {
        String channelName = channel.getName();
        String tTopic = tokenizedTopic(topic);
        if (setBy.equals(botNick)) {
            return;
        }

        if (channelTopic.containsKey(channelName)) {
            if (channelTopicProtected.containsKey(channelName)) {
                if (channelTopicProtected.get(channelName)) {
                    plugin.logDebug("[" + channel.getName() + "] Topic protected.");
                    String myTopic = tokenizedTopic(channelTopic.get(channelName));
                    plugin.logDebug("rTopic: " + channelTopic.get(channelName));
                    plugin.logDebug("tTopic: " + tTopic);
                    plugin.logDebug("myTopic: " + myTopic);
                    if (!tTopic.equals(myTopic)) {
                        plugin.logDebug("Topic is not correct. Fixing it.");
                        setTheTopic(channel, myTopic);
                    } else {
                        plugin.logDebug("Topic is correct.");
                    }
                }
            }
        }
    }

    private void setTheTopic(Channel channel, String topic) {
        String myChannel = channel.getName();
        if (channelTopicChanserv.containsKey(myChannel)) {
            if (channelTopicChanserv.get(myChannel)) {
                String msg = String.format("TOPIC %s %s", myChannel, topic);
                plugin.logDebug("Sending chanserv rmessage: " + msg);
                asyncIRCMessage("chanserv", msg);
                return;
            }
        }
        channel.send().setTopic(topic);
    }

    private String tokenizedTopic(String topic) {
        return plugin.colorConverter
                .gameColorsToIrc(topic.replace("%MOTD%", MinecraftServer.getServer().getMotd()));
    }

    /**
     *
     * @param sender
     */
    public void asyncQuit(CommandSender sender) {
        sender.sendMessage("Disconnecting " + bot.getNick() + " from IRC server " + botServer);
        asyncQuit(false);
    }

    /**
     *
     * @param reload
     */
    public void asyncQuit(final Boolean reload) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                quit();
                if (reload) {
                    buildBot(true);
                }
            }
        }).start();
    }

    public void quit() {
        if (this.isConnected()) {
            plugin.logDebug("Q: " + quitMessage);
            if (quitMessage.isEmpty()) {
                bot.sendIRC().quitServer();
            } else {
                bot.sendIRC().quitServer(plugin.colorConverter.gameColorsToIrc(quitMessage));
            }
        }
    }

    /**
     *
     * @param sender
     */
    public void sendTopic(CommandSender sender) {
        for (String channelName : botChannels) {
            if (commandMap.containsKey(channelName)) {
                sender.sendMessage(EnumChatFormatting.WHITE + "[" + EnumChatFormatting.DARK_PURPLE
                        + botNick + EnumChatFormatting.WHITE + "]" + EnumChatFormatting.RESET
                        + " IRC topic for " + EnumChatFormatting.WHITE + channelName
                        + EnumChatFormatting.RESET + ": \""
                        + EnumChatFormatting.WHITE + plugin.colorConverter
                        .ircColorsToGame(activeTopic.get(channelName))
                        + EnumChatFormatting.RESET + "\"");
            }
        }
    }

    /**
     *
     * @param sender
     * @param nick
     */
    public void sendUserWhois(CommandSender sender, String nick) {
        User user = null;
        for (Channel channel : getChannels()) {
            for (User u : channel.getUsers()) {
                if (u.getNick().equals(nick)) {
                    user = u;
                }
            }
        }

        if (user == null) {
            sender.sendMessage(EnumChatFormatting.RED + "Invalid user: " + EnumChatFormatting.WHITE + nick);
        } else {
            bot.sendRaw().rawLineNow(String.format("WHOIS %s %s", nick, nick));
            whoisSenders.add(sender);
        }
    }

    /**
     *
     * @param sender
     * @param channelName
     */
    public void sendUserList(CommandSender sender, String channelName) {
        String invalidChannel = EnumChatFormatting.RED + "Invalid channel: "
                + EnumChatFormatting.WHITE + channelName;
        if (!isValidChannel(channelName)) {
            sender.sendMessage(invalidChannel);
            return;
        }
        Channel channel = getChannel(channelName);
        if (channel != null) {
            sendUserList(sender, channel);
        } else {
            sender.sendMessage(invalidChannel);
        }
    }

    /**
     *
     * @param sender
     * @param channel
     */
    public void sendUserList(CommandSender sender, Channel channel) {
        String channelName = channel.getName();
        if (!isValidChannel(channelName)) {
            sender.sendMessage(EnumChatFormatting.RED + "Invalid channel: "
                    + EnumChatFormatting.WHITE + channelName);
            return;
        }
        sender.sendMessage(EnumChatFormatting.DARK_PURPLE + "-----[  " + EnumChatFormatting.WHITE + channelName
                + EnumChatFormatting.DARK_PURPLE + " - " + EnumChatFormatting.WHITE + bot.getNick() + EnumChatFormatting.DARK_PURPLE + " ]-----");
        if (!this.isConnected()) {
            sender.sendMessage(EnumChatFormatting.RED + " Not connected!");
            return;
        }
        List<String> channelUsers = new ArrayList<>();
        for (User user : channel.getUsers()) {
            String n = user.getNick();
            n = getNickPrefix(user, channel) + n;
            if (user.isAway()) {
                n = n + EnumChatFormatting.GRAY + " | Away | " + user.getAwayMessage();
            }
            if (n.equals(bot.getNick())) {
                n = EnumChatFormatting.DARK_PURPLE + n;
            }
            channelUsers.add(n);
        }
        Collections.sort(channelUsers, Collator.getInstance());
        for (String userName : channelUsers) {
            sender.sendMessage("  " + EnumChatFormatting.WHITE + userName);
        }
    }

    public String getNickPrefix(User user, Channel channel) {
        try {
            if (user.getChannels() != null) {
                if (user.isIrcop()) {
                    return plugin.ircNickPrefixIrcOp;
                } else if (user.getChannelsOwnerIn().contains(channel)) {
                    return plugin.ircNickPrefixOwner;
                } else if (user.getChannelsSuperOpIn().contains(channel)) {
                    return plugin.ircNickPrefixSuperOp;
                } else if (user.getChannelsOpIn().contains(channel)) {
                    return plugin.ircNickPrefixOp;
                } else if (user.getChannelsHalfOpIn().contains(channel)) {
                    return plugin.ircNickPrefixHalfOp;
                } else if (user.getChannelsVoiceIn().contains(channel)) {
                    return plugin.ircNickPrefixVoice;
                }
            }
        } catch (Exception ex) {
            plugin.logDebug(ex.getMessage());
        }
        return "";
    }

    /**
     *
     * @param sender
     */
    public void sendUserList(CommandSender sender) {
        for (Channel channel : getChannels()) {
            if (isValidChannel(channel.getName())) {
                sendUserList(sender, channel);
            }
        }
    }

    /**
     *
     */
    public void updateNickList() {
        if (!this.isConnected()) {
            return;
        }
        for (Channel channel : this.getChannels()) {
            this.updateNickList(channel);
        }
    }

    /**
     *
     * @param channel
     */
    public void updateNickList(Channel channel) {
        if (!this.isConnected()) {
            return;
        }
        // Build current list of names in channel
        ArrayList<String> users = new ArrayList<>();
        for (User user : channel.getUsers()) {
            //plugin.logDebug("N: " + user.getNick());
            users.add(user.getNick());
        }
        try {
            wl.tryLock(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            plugin.logDebug("Lock Error: " + ex.getMessage());
            return;
        }
        try {
            String channelName = channel.getName();
            if (channelNicks.containsKey(channelName)) {
                channelNicks.remove(channelName);
            }
            channelNicks.put(channelName, users);
        } finally {
            wl.unlock();
        }
    }

    /**
     *
     * @param channel
     */
    public void opIrcUsers(Channel channel) {
        for (User user : channel.getUsers()) {
            opIrcUser(channel, user);
        }
    }

    /**
     *
     * @param channelName
     */
    public void opIrcUsers(String channelName) {
        Channel channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                opIrcUser(channel, user);
            }
        }
    }

    /**
     *
     * @param channel
     */
    public void voiceIrcUsers(Channel channel) {
        for (User user : channel.getUsers()) {
            voiceIrcUser(channel, user);
        }
    }

    /**
     *
     * @param channelName
     */
    public void voiceIrcUsers(String channelName) {
        Channel channel = getChannel(channelName);
        if (channel != null) {
            for (User user : channel.getUsers()) {
                voiceIrcUser(channel, user);
            }
        }
    }

    /**
     *
     * @param user
     * @param userMask
     * @return
     */
    public boolean checkUserMask(User user, String userMask) {
        String mask[] = userMask.split("[\\!\\@]", 3);
        if (mask.length == 3) {
            String gUser = plugin.regexGlobber.createRegexFromGlob(mask[0]);
            String gLogin = plugin.regexGlobber.createRegexFromGlob(mask[1]);
            String gHost = plugin.regexGlobber.createRegexFromGlob(mask[2]);
            return (user.getNick().matches(gUser)
                    && user.getLogin().matches(gLogin)
                    && user.getHostmask().matches(gHost));
        }
        return false;
    }

    /**
     *
     * @param channel
     * @param user
     */
    public void opIrcUser(Channel channel, User user) {
        String channelName = channel.getName();
        if (user.getNick().equals(botNick)) {
            return;
        }
        if (channel.getOps().contains(user)) {
            plugin.logInfo("User " + user.getNick() + " is already an operator on " + channelName);
            return;
        }
        for (String userMask : opsList.get(channelName)) {
            if (checkUserMask(user, userMask)) {
                plugin.logInfo("Giving operator status to " + user.getNick() + " on " + channelName);
                channel.send().op(user);
                break;
            }
        }
    }

    /**
     *
     * @param channel
     * @param user
     */
    public void voiceIrcUser(Channel channel, User user) {
        String channelName = channel.getName();
        if (user.getNick().equals(botNick)) {
            return;
        }
        if (channel.getVoices().contains(user)) {
            plugin.logInfo("User " + user.getNick() + " is already a voice on " + channelName);
            return;
        }
        for (String userMask : voicesList.get(channelName)) {
            if (checkUserMask(user, userMask)) {
                plugin.logInfo("Giving voice status to " + user.getNick() + " on " + channelName);
                channel.send().voice(user);
                break;
            }
        }
    }

    public String filterMessage(String message, String myChannel) {
        final String regex = ".*(https?|ftp|file)://.*";
        if (filters.containsKey(myChannel)) {
            if (!filters.get(myChannel).isEmpty()) {
                for (String filter : filters.get(myChannel)) {
                    if (filter.startsWith("/") && filter.endsWith("/")) {
                        filter = filter.substring(1, filter.length() - 1);
                        plugin.logDebug("Regex filtering " + filter + " from " + message);
                        message = message.replaceAll(filter, "");
                    } else {
                        plugin.logDebug("Filtering " + filter + " from " + message);
                        message = message.replace(filter, "");
                    }
                }
            }
        }
        String strings[] = message.split(" ");
        for (int x = 0; x < strings.length; x++) {
            if (strings[x].matches(regex)) {
                strings[x] = ChatColor.stripColor(strings[x]);
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(string);
        }

        return builder.toString();
    }

    // Broadcast chat messages from IRC
    /**
     *
     * @param user
     * @param channel
     * @param target
     * @param message
     * @param override
     * @param ctcpResponse
     */
    public void broadcastChat(User user, org.pircbotx.Channel channel, String target, String message, boolean override, boolean ctcpResponse) {
        boolean messageSent = false;
        String myChannel = channel.getName();

        /*
         First occurrence replacements
         */
        if (!firstOccurrenceReplacements.isEmpty()) {
            for (String key : firstOccurrenceReplacements.keySet()) {
                if (user.getNick().equalsIgnoreCase(key) || checkUserMask(user, key)) {
                    CaseInsensitiveMap cm = firstOccurrenceReplacements.get(key);
                    for (Object obj : cm.keySet()) {
                        message = message.replaceFirst((String) obj, plugin.colorConverter.translateAlternateColorCodes('&', (String) cm.get(obj)));
                    }
                }
            }
        }

        /*
         Send messages to Dynmap if enabled
         */
        if (plugin.dynmapHook != null) {
            plugin.logDebug("Checking if " + TemplateName.IRC_DYNMAP_WEB_CHAT + " is enabled ...");
            if (enabledMessages.get(myChannel).contains(TemplateName.IRC_DYNMAP_WEB_CHAT)) {
                plugin.logDebug("Yes, " + TemplateName.IRC_DYNMAP_WEB_CHAT + " is enabled...");
                plugin.logDebug("broadcastChat [DW]: " + message);
                String template = plugin.getMsgTemplate(botNick, TemplateName.IRC_DYNMAP_WEB_CHAT);
                String rawDWMessage = filterMessage(
                        plugin.tokenizer.ircChatToGameTokenizer(this, user, channel, template, message), myChannel);
                String nickTmpl = plugin.getMsgTemplate(botNick, TemplateName.IRC_DYNMAP_NICK);
                String rawNick = nickTmpl.replace("%NICK%", user.getNick());
                plugin.dynmapHook.sendMessage(rawNick, rawDWMessage);
                messageSent = true;
            } else {
                plugin.logDebug("Nope, " + TemplateName.IRC_DYNMAP_WEB_CHAT + " is NOT enabled...");
            }
        }


        /*
         Send messages to players if enabled
         */
        plugin.logDebug("Checking if " + TemplateName.IRC_CHAT + " is enabled before broadcasting chat from IRC");
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_CHAT) || override) {
            plugin.logDebug("Yup we can broadcast due to " + TemplateName.IRC_CHAT + " enabled");
            String newMessage = filterMessage(
                    plugin.tokenizer.ircChatToGameTokenizer(this, user, channel, plugin.getMsgTemplate(
                            botNick, TemplateName.IRC_CHAT), message), myChannel);
            if (!newMessage.isEmpty()) {
                plugin.broadcastToGame(newMessage, "irc.message.chat");
                messageSent = true;
            }
        } else {
            plugin.logDebug("NOPE we can't broadcast due to " + TemplateName.IRC_CHAT + " disabled");
        }

        /*
         Send messages to console if enabled
         */
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_CONSOLE_CHAT)) {
            String tmpl = plugin.getMsgTemplate(botNick, TemplateName.IRC_CONSOLE_CHAT);
            plugin.logDebug("broadcastChat [Console]: " + tmpl);
            plugin.logInfo(plugin.tokenizer.ircChatToGameTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_CONSOLE_CHAT), message));
            messageSent = true;
        }

        /*
         Notify IRC user that message was sent.
         */
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_CHAT_RESPONSE) && messageSent && target != null) {
            // Let the sender know the message was sent
            String responseTemplate = plugin.getMsgTemplate(botNick, TemplateName.IRC_CHAT_RESPONSE);
            if (!responseTemplate.isEmpty()) {
                if (ctcpResponse) {
                    asyncCTCPMessage(target, plugin.tokenizer.targetChatResponseTokenizer(target, message, responseTemplate));
                } else {
                    asyncIRCMessage(target, plugin.tokenizer.targetChatResponseTokenizer(target, message, responseTemplate));
                }
            }
        }

    }

    // Send chat messages from IRC to player
    /**
     *
     * @param user
     * @param channel
     * @param target
     * @param message
     */
    public void playerChat(User user, org.pircbotx.Channel channel, String target, String message) {
        String myChannel = channel.getName();
        if (message == null) {
            plugin.logDebug("H: NULL MESSAGE");
            asyncIRCMessage(target, "No player specified!");
            return;
        }
        if (message.contains(" ")) {
            String pName;
            String msg;
            pName = message.split(" ", 2)[0];
            msg = message.split(" ", 2)[1];
            plugin.logDebug("Check if " + TemplateName.IRC_PCHAT + " is enabled before broadcasting chat from IRC");
            if (enabledMessages.get(myChannel).contains(TemplateName.IRC_PCHAT)) {
                plugin.logDebug("Yup we can broadcast due to " + TemplateName.IRC_PCHAT
                        + " enabled... Checking if " + pName + " is a valid player...");
                EntityPlayer player = plugin.getPlayer(pName);
                if (player != null) {
                    plugin.logDebug("Yup, " + pName + " is a valid player...");
                    String template = plugin.getMsgTemplate(botNick, TemplateName.IRC_PCHAT);
                    String t = plugin.tokenizer.ircChatToGameTokenizer(this, user,
                            channel, template, msg);
                    String responseTemplate = plugin.getMsgTemplate(botNick,
                            TemplateName.IRC_PCHAT_RESPONSE);
                    if (!responseTemplate.isEmpty()) {
                        asyncIRCMessage(target, plugin.tokenizer
                                .targetChatResponseTokenizer(pName, msg, responseTemplate));
                    }
                    plugin.logDebug("Tokenized message: " + t);
                    player.addChatComponentMessage(new ChatComponentText(t));

                } else {
                    asyncIRCMessage(target, "Player not found (possibly offline): " + pName);
                }
            } else {
                plugin.logDebug("NOPE we can't broadcast due to " + TemplateName.IRC_PCHAT + " disabled");
            }
        } else {
            asyncIRCMessage(target, "No message specified.");
        }
    }

// Broadcast action messages from IRC
    /**
     *
     * @param user
     * @param channel
     * @param message
     */
    public void broadcastAction(User user, org.pircbotx.Channel channel, String message) {
        String myChannel = channel.getName();
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_ACTION)) {
            plugin.broadcastToGame(plugin.tokenizer.ircChatToGameTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick,
                            TemplateName.IRC_ACTION), message), "irc.message.action");
        } else {
            plugin.logDebug("Ignoring action due to "
                    + TemplateName.IRC_ACTION + " is false");
        }

        if (plugin.dynmapHook != null) {
            plugin.logDebug("xChecking if " + TemplateName.IRC_ACTION_DYNMAP_WEB_CHAT + " is enabled ...");
            if (enabledMessages.get(myChannel).contains(TemplateName.IRC_ACTION_DYNMAP_WEB_CHAT)) {
                plugin.logDebug("Yes, " + TemplateName.IRC_ACTION_DYNMAP_WEB_CHAT + " is enabled...");
                String template = plugin.getMsgTemplate(botNick, TemplateName.IRC_ACTION_DYNMAP_WEB_CHAT);
                String rawDWMessage = filterMessage(
                        plugin.tokenizer.ircChatToGameTokenizer(this, user, channel, template, message), myChannel);
                String nickTmpl = plugin.getMsgTemplate(botNick, TemplateName.IRC_DYNMAP_ACTION_NICK);
                String rawNick = nickTmpl.replace("%NICK%", user.getNick());
                plugin.dynmapHook.sendMessage(rawNick, rawDWMessage);
            } else {
                plugin.logDebug("Nope, " + TemplateName.IRC_ACTION_DYNMAP_WEB_CHAT + " is NOT enabled...");
            }
        }
    }

    /**
     *
     * @param recipient
     * @param kicker
     * @param reason
     * @param channel
     */
    public void broadcastIRCKick(User recipient, User kicker, String reason, org.pircbotx.Channel channel) {
        String myChannel = channel.getName();
        if (enabledMessages.get(myChannel).contains(TemplateName.IRC_KICK)) {
            plugin.broadcastToGame(plugin.tokenizer.ircKickTokenizer(
                    this, recipient, kicker, reason, channel, plugin.getMsgTemplate(
                            botNick, TemplateName.IRC_KICK)),
                    "irc.message.kick");
        }
    }

    /**
     *
     * @return
     */
    public boolean isConnectedBlocking() {
        if (bot != null) {
            return bot.isConnected();
        }
        return false;
    }

    /**
     *
     * @param user
     * @param mode
     * @param channel
     */
    public void broadcastIRCMode(User user, String mode, org.pircbotx.Channel channel) {
        if (isMessageEnabled(channel, TemplateName.IRC_MODE)) {
            plugin.broadcastToGame(plugin.tokenizer.ircModeTokenizer(this, user, mode,
                    channel, plugin.getMsgTemplate(botNick,
                            TemplateName.IRC_MODE)), "irc.message.mode");
        }
    }

    /**
     *
     * @param user
     * @param message
     * @param notice
     * @param channel
     */
    public void broadcastIRCNotice(User user, String message, String notice, org.pircbotx.Channel channel) {
        if (isMessageEnabled(channel, TemplateName.IRC_NOTICE)) {
            plugin.broadcastToGame(plugin.tokenizer.ircNoticeTokenizer(this, user,
                    message, notice, channel, plugin.getMsgTemplate(botNick,
                            TemplateName.IRC_NOTICE)), "irc.message.notice");
        }
    }

    /**
     *
     * @param user
     * @param channel
     */
    public void broadcastIRCJoin(User user, org.pircbotx.Channel channel) {
        if (isMessageEnabled(channel, TemplateName.IRC_JOIN)) {
            plugin.logDebug("[broadcastIRCJoin] Broadcasting join message because " + TemplateName.IRC_JOIN + " is true.");
            plugin.broadcastToGame(plugin.tokenizer.chatIRCTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_JOIN)), "irc.message.join");
        } else {
            plugin.logDebug("[broadcastIRCJoin] NOT broadcasting join message because " + TemplateName.IRC_JOIN + " is false.");
        }
    }

    public void broadcastIRCPart(User user, org.pircbotx.Channel channel) {
        if (isMessageEnabled(channel, TemplateName.IRC_PART)) {
            String message = plugin.tokenizer.chatIRCTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_PART));
            plugin.logDebug("[broadcastIRCPart]  Broadcasting part message because "
                    + TemplateName.IRC_PART + " is true: " + message);
            plugin.broadcastToGame(message, "irc.message.part");
        } else {
            plugin.logDebug("[broadcastIRCPart] NOT broadcasting part message because "
                    + TemplateName.IRC_PART + " is false.");
        }
    }

    public void broadcastIRCQuit(User user, org.pircbotx.Channel channel, String reason) {
        if (isMessageEnabled(channel, TemplateName.IRC_QUIT)) {
            plugin.logDebug("[broadcastIRCQuit] Broadcasting quit message because "
                    + TemplateName.IRC_QUIT + " is true.");
            plugin.broadcastToGame(plugin.tokenizer.chatIRCTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_QUIT))
                    .replace("%REASON%", reason), "irc.message.quit");
        } else {
            plugin.logDebug("[broadcastIRCQuit] NOT broadcasting quit message because "
                    + TemplateName.IRC_QUIT + " is false.");
        }
    }

    /**
     * Broadcast topic changes from IRC
     *
     * @param user
     * @param channel
     * @param message
     */
    public void broadcastIRCTopic(User user, org.pircbotx.Channel channel, String message) {
        if (isMessageEnabled(channel, TemplateName.IRC_TOPIC)) {
            plugin.broadcastToGame(plugin.tokenizer.chatIRCTokenizer(
                    this, user, channel, plugin.getMsgTemplate(botNick, TemplateName.IRC_TOPIC)), "irc.message.topic");
        }
    }

    /**
     *
     * @param channelName
     * @param templateName
     * @return
     */
    public boolean isMessageEnabled(String channelName, String templateName) {
        return enabledMessages.get(channelName).contains(templateName);
    }

    /**
     *
     * @param channel
     * @param templateName
     * @return
     */
    public boolean isMessageEnabled(Channel channel, String templateName) {
        return isMessageEnabled(channel.getName(), templateName);
    }

    /**
     * Broadcast disconnect messages from IRC
     *
     * @param nick
     */
    public void broadcastIRCDisconnect(String nick) {
        plugin.broadcastToGame("[" + nick + "] Disconnected from IRC server.", "irc.message.disconnect");
    }

    /**
     * Broadcast connect messages from IRC
     *
     * @param nick
     */
    public void broadcastIRCConnect(String nick) {
        plugin.broadcastToGame("[" + nick + "] Connected to IRC server.", "irc.message.connect");
    }

    /**
     * Notify when players use commands
     *
     * @param player
     * @param cmd
     * @param params
     */
    public void commandNotify(EntityPlayerMP player, String cmd, String params) {
        if (!this.isConnected()) {
            return;
        }
        String msg = plugin.tokenizer.gameCommandToIRCTokenizer(player,
                plugin.getMsgTemplate(botNick, TemplateName.GAME_COMMAND), cmd, params);
        if (channelCmdNotifyMode.equalsIgnoreCase("msg")) {
            for (String recipient : channelCmdNotifyRecipients) {
                asyncIRCMessage(recipient, msg);
            }
        } else if (channelCmdNotifyMode.equalsIgnoreCase("ctcp")) {
            for (String recipient : channelCmdNotifyRecipients) {
                asyncCTCPMessage(recipient, msg);
            }
        }
    }

    /**
     *
     * @param sender
     * @param nick
     * @param message
     */
    public void msgPlayer(EntityPlayerMP sender, String nick, String message) {
        String msg = plugin.tokenizer.gameChatToIRCTokenizer(sender,
                plugin.getMsgTemplate(botNick, TemplateName.GAME_PCHAT), message);
        asyncIRCMessage(nick, msg);
    }

    /**
     *
     * @param nick
     * @param message
     */
    public void consoleMsgPlayer(String nick, String message) {
        String msg = plugin.tokenizer.gameChatToIRCTokenizer("console",
                plugin.getMsgTemplate(botNick, TemplateName.CONSOLE_CHAT), message);
        asyncIRCMessage(nick, msg);
    }

    public boolean isConnected() {
        return connected;
    }

    public ImmutableSortedSet<Channel> getChannels() {
        if (bot.getNick().isEmpty()) {
            return ImmutableSortedSet.<Channel>naturalOrder().build();
        }
        return bot.getUserBot().getChannels();
    }

    public long getMessageDelay() {
        return bot.getConfiguration().getMessageDelay();
    }

    public String getMotd() {
        return bot.getServerInfo().getMotd();
    }

    public boolean isValidChannel(String channelName) {
        for (String s : botChannels) {
            if (channelName.equalsIgnoreCase(s)) {
                return true;
            }
        }
        plugin.logDebug("Channel " + channelName + " is not valid.");
        return false;
    }

    public PircBotX getBot() {
        return bot;
    }

    /**
     *
     * @param connected
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getFileName() {
        return fileName;
    }

    public void joinNotice(Channel channel, User user) {
        if (user.getNick().equalsIgnoreCase(botNick)) {
            return;
        }
        if (joinNoticeEnabled) {
            if (joinNoticeCooldownMap.containsKey(user.getHostmask())) {
                long prevTime = joinNoticeCooldownMap.get(user.getHostmask());
                long currentTime = System.currentTimeMillis();
                long diff = currentTime - prevTime;
                if (diff < (joinNoticeCoolDown * 1000)) {
                    plugin.logDebug("joinNotice: " + diff);
                    return;
                }
            } else {
                joinNoticeCooldownMap.put(user.getHostmask(), System.currentTimeMillis());
            }
            String target = channel.getName();
            if (joinNoticePrivate) {
                target = user.getNick();
            }
            String myMessage = plugin.colorConverter.translateAlternateColorCodes('&', plugin.colorConverter.gameColorsToIrc(joinNoticeMessage.replace("%NAME%", user.getNick())));
            if (joinNoticeMessage.startsWith("/")) {
                plugin.commandQueue.add(new IRCCommand(
                        new IRCCommandSender(this, target, plugin, joinNoticeCtcp, "CONSOLE"),
                        myMessage.trim().substring(1)));
            } else if (joinNoticeCtcp) {
                asyncCTCPMessage(target, myMessage);
            } else {
                asyncIRCMessage(target, myMessage);
            }
        }
    }

    public void altNickChange() {
        if (altNicks.isEmpty()) {
            return;
        }
        if (nickIndex >= 0 && nickIndex < altNicks.size()) {
            botNick = altNicks.get(nickIndex).replace("%NICK%", nick);
            nickIndex++;
        } else {
            nickIndex = 0;
        }
        plugin.logInfo("Trying alternate nick " + botNick);
        bot.sendIRC().changeNick(botNick);
    }

}
