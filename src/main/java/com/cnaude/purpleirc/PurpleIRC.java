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

import com.cnaude.purpleirc.GameListeners.DynmapListener;
import com.cnaude.purpleirc.GameListeners.GamePlayerChatListener;
import com.cnaude.purpleirc.GameListeners.GamePlayerDeathListener;
import com.cnaude.purpleirc.GameListeners.GamePlayerJoinListener;
import com.cnaude.purpleirc.GameListeners.GamePlayerPlayerAchievementAwardedListener;
import com.cnaude.purpleirc.GameListeners.GamePlayerQuitListener;
import com.cnaude.purpleirc.GameListeners.GameServerCommandListener;
import com.cnaude.purpleirc.Hooks.DynmapHook;
import com.cnaude.purpleirc.Utilities.CaseInsensitiveMap;
import com.cnaude.purpleirc.Utilities.ChatTokenizer;
import com.cnaude.purpleirc.Utilities.ColorConverter;
import com.cnaude.purpleirc.Utilities.Query;
import com.cnaude.purpleirc.Utilities.RegexGlobber;
import com.cnaude.purpleirc.Utilities.UpdateChecker;
import com.cnaude.purpleirc.Utilities.PurpleConfiguration;
import com.google.common.base.Joiner;
import cpw.mods.fml.common.Mod;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.pircbotx.IdentServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Chris Naudé
 */
@Mod(modid = PurpleIRC.MOD_ID, version = Version.VER, acceptableRemoteVersions = "*")
public class PurpleIRC {

    public static final String MOD_ID = "PurpleIRC";    

    @Mod.Instance(MOD_ID)
    public static PurpleIRC instance;

    private final Description description;
    private PurpleConfiguration config;

    public static Logger log = LogManager.getLogger(MOD_ID);
    public String LOG_HEADER;
    public String LOG_HEADER_F;
    private final String sampleFileName;
    private final String configFileName;
    private final String MAINCONFIG;
    private final File pluginFolder;
    public final File botsFolder;
    private final File configFile;
    public static long startTime;

    public boolean identServerEnabled;
    private final CaseInsensitiveMap<HashMap<String, String>> messageTmpl;
    private final Map<String, String> hostCache;
    public String listFormat,
            listSeparator,
            listPlayer,
            ircNickPrefixIrcOp,
            ircNickPrefixSuperOp,
            ircNickPrefixOp,
            ircNickPrefixHalfOp,
            ircNickPrefixVoice,
            defaultPlayerWorld;
    private final CaseInsensitiveMap<String> displayNameCache;
    public CaseInsensitiveMap<UUID> uuidCache;

    public ArrayList<String> kickedPlayers = new ArrayList<>();

    public final String invalidBotName = EnumChatFormatting.RED + "Invalid bot name: "
            + EnumChatFormatting.WHITE + "%BOT%"
            + EnumChatFormatting.RED + ". Type '" + EnumChatFormatting.WHITE + "/irc listbots"
            + EnumChatFormatting.RED + "' to see valid bots.";

    public final String invalidChannelName = EnumChatFormatting.RED + "Invalid channel name: "
            + EnumChatFormatting.WHITE + "%CHANNEL%";

    public final String invalidChannel = EnumChatFormatting.RED + "Invalid channel: "
            + EnumChatFormatting.WHITE + "%CHANNEL%";
    public final String noPermission = EnumChatFormatting.RED + "You do not have permission to use this command.";

    private boolean updateCheckerEnabled;
    private String updateCheckerMode;
    private boolean debugEnabled;
    private boolean stripGameColors;
    private boolean stripIRCColors;
    private boolean stripIRCBackgroundColors;
    public boolean customTabList;
    public String customTabGamemode;
    private boolean listSortByName;
    public boolean exactNickMatch;
    public boolean ignoreChatCancel;
    public Long ircConnCheckInterval;
    public Long ircChannelCheckInterval;
    public ChannelWatcher channelWatcher;
    public ColorConverter colorConverter;
    public RegexGlobber regexGlobber;
    public CaseInsensitiveMap<PurpleBot> ircBots;
    public DynmapHook dynmapHook;
    public CommandHandlers commandHandlers;
    private BotWatcher botWatcher;
    public IRCMessageHandler ircMessageHandler;

    public CommandQueueWatcher commandQueue;
    public UpdateChecker updateChecker;
    public ChatTokenizer tokenizer;
    private final File cacheFile;
    private final File uuidCacheFile;
    public int reconnectSuppression;

    List<String> hookList = new ArrayList<>();

    public PurpleIRC() {
        this.LOG_HEADER = "[" + MOD_ID + "]";
        this.LOG_HEADER_F = EnumChatFormatting.DARK_PURPLE + "[" + MOD_ID + "]" + EnumChatFormatting.RESET;
        this.MAINCONFIG = "MAIN-CONFIG";
        this.pluginFolder = new File("config/" + MOD_ID);
        this.botsFolder = new File(pluginFolder, "bots");
        this.sampleFileName = "SampleBot.yml";
        this.configFileName = "config.yml";
        this.configFile = new File(pluginFolder, configFileName);
        this.cacheFile = new File(pluginFolder, "displayName.cache");
        this.uuidCacheFile = new File(pluginFolder, "uuid.cache");

        this.ircBots = new CaseInsensitiveMap<>();
        this.messageTmpl = new CaseInsensitiveMap<>();
        this.displayNameCache = new CaseInsensitiveMap<>();
        this.uuidCache = new CaseInsensitiveMap<>();
        this.hostCache = new HashMap<>();
        this.reconnectSuppression = 0;
        this.description = new Description(MOD_ID, "PurpleIRC Forge Mod v" + Version.VER, "http://jenkins.cnaude.org", Version.VER);
    }

    public Description getDescription() {
        return description;
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) throws FileNotFoundException, IOException {

        createConfigDirs();
        createConfig();
        config = new PurpleConfiguration(configFile, false);
        loadConfig();
        loadDisplayNameCache();
        loadUuidCache();
        if (identServerEnabled) {
            logInfo("Starting Ident Server ...");
            try {
                IdentServer.startServer();
            } catch (Exception ex) {
                logError(ex.getMessage());
            }
        }

        commandHandlers = new CommandHandlers(this);
        registerCommands((CommandHandler) event.getServer().getCommandManager(), commandHandlers);
        regexGlobber = new RegexGlobber();
        tokenizer = new ChatTokenizer(this);
        loadBots();
        createSampleBot();
        channelWatcher = new ChannelWatcher(this);
        botWatcher = new BotWatcher(this);
        ircMessageHandler = new IRCMessageHandler(this);
        commandQueue = new CommandQueueWatcher(this);
        updateChecker = new UpdateChecker(this);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        MinecraftForge.EVENT_BUS.register(new GamePlayerDeathListener(this));
        MinecraftForge.EVENT_BUS.register(new GameServerCommandListener(this));
        
        MinecraftForge.EVENT_BUS.register(new GamePlayerChatListener(this));
        
        FMLCommonHandler.instance().bus().register(new GamePlayerJoinListener(this));
        FMLCommonHandler.instance().bus().register(new GamePlayerQuitListener(this));
        FMLCommonHandler.instance().bus().register(new GamePlayerPlayerAchievementAwardedListener(this));

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {        
        if (Loader.isModLoaded("Dynmap")) {            
            FMLCommonHandler.instance().bus().register(new DynmapListener(this));
        }
    }

    @Mod.EventHandler
    public void serverStop(FMLServerStoppingEvent event) {
        if (channelWatcher != null) {
            logDebug("Disabling channelWatcher ...");
            channelWatcher.cancel();
        }
        if (botWatcher != null) {
            logDebug("Disabling botWatcher ...");
            botWatcher.cancel();
        }
        if (updateChecker != null) {
            logDebug("Disabling updateChecker ...");
            updateChecker.cancel();
        }
        if (ircBots.isEmpty()) {
            logInfo("No IRC bots to disconnect.");
        } else {
            logInfo("Disconnecting IRC bots.");
            for (PurpleBot ircBot : ircBots.values()) {
                commandQueue.cancel();
                ircBot.saveConfig();
                ircBot.quit();
            }
            ircBots.clear();
        }
        if (identServerEnabled) {
            logInfo("Stopping Ident Server");
            try {
                IdentServer.stopServer();
            } catch (IOException ex) {
                logError(ex.getMessage());
            }
        }
        saveDisplayNameCache();
        saveUuidCache();
    }

    /**
     *
     * @param debug
     */
    public void debugMode(boolean debug) {
        debugEnabled = debug;
        getConfig().set("Debug", debug);
        try {
            getConfig().save();
        } catch (IOException ex) {
            logError("Problem saving to " + configFile.getName() + ": " + ex.getMessage());
        }
    }

    public PurpleConfiguration getConfig() {
        return config;
    }

    /**
     *
     * @return
     */
    public boolean debugMode() {
        return debugEnabled;
    }

    public String getMsgTemplate(String botName, String tmpl) {
        if (messageTmpl.containsKey(botName)) {
            if (messageTmpl.get(botName).containsKey(tmpl)) {
                return messageTmpl.get(botName).get(tmpl);
            }
        }
        if (messageTmpl.get(MAINCONFIG).containsKey(tmpl)) {
            return messageTmpl.get(MAINCONFIG).get(tmpl);
        }
        return "INVALID TEMPLATE";
    }

    public String getMsgTemplate(String tmpl) {
        return getMsgTemplate(MAINCONFIG, tmpl);
    }

    public void loadCustomColors(PurpleConfiguration config) {
        final String IRC_COLOR_MAP = "irc-color-map";
        final String GAME_COLOR_MAP = "game-color-map";
        for (String key : config.getMap(IRC_COLOR_MAP).keySet()) {
            colorConverter.addIrcColorMap(key, (String) config.getMap(IRC_COLOR_MAP).get(key));
        }
        for (String t : config.getMap(GAME_COLOR_MAP).keySet()) {
            colorConverter.addGameColorMap(t, (String) config.getMap(GAME_COLOR_MAP).get(t));
        }
    }

    public void loadTemplates(PurpleConfiguration config, String configName) {
        messageTmpl.put(configName, new HashMap<String, String>());

        Map<String, String> map = config.getMessageTemplates();
        for (String key : map.keySet()) {
            if (map.get(key) instanceof String) {
                String value = colorConverter.translateAlternateColorCodes('&', map.get(key));
                messageTmpl.get(configName).put(key, value);
                logDebug("message-format: " + key + " => " + value);
            }
        }
    }

    private void loadConfig() throws IOException {
        try {
            getConfig().load();
        } catch (FileNotFoundException ex) {
            logError(ex.getMessage());
        }
        debugEnabled = getConfig().getOption("Debug", false);
        logDebug("Debug enabled");
        updateCheckerEnabled = getConfig().getOption("update-checker", true);
        updateCheckerMode = getConfig().getOption("update-checker-mode", "stable");        
        identServerEnabled = getConfig().getOption("enable-ident-server", false);        
        stripGameColors = getConfig().getOption("strip-game-colors", false);
        stripIRCColors = getConfig().getOption("strip-irc-colors", false);
        stripIRCBackgroundColors = getConfig().getOption("strip-irc-bg-colors", true);
        exactNickMatch = getConfig().getOption("nick-exact-match", true);
        ignoreChatCancel = getConfig().getOption("ignore-chat-cancel", false);
        colorConverter = new ColorConverter(this, stripGameColors, stripIRCColors, stripIRCBackgroundColors);

        loadTemplates((PurpleConfiguration) this.getConfig(), MAINCONFIG);
        loadCustomColors((PurpleConfiguration) this.getConfig());

        defaultPlayerWorld = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("message-format", "default-player-world", ""));

        ircNickPrefixIrcOp = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("nick-prefixes", "ircop", "~"));
        ircNickPrefixSuperOp = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("nick-prefixes", "ircsuperop", "&&"));
        ircNickPrefixOp = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("nick-prefixes", "op", "@"));
        ircNickPrefixHalfOp = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("nick-prefixes", "halfop", "%"));
        ircNickPrefixVoice = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("nick-prefixes", "voice", "+"));

        listFormat = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("list-format", ""));
        listSeparator = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("list-separator", ""));
        listPlayer = colorConverter.translateAlternateColorCodes('&', getConfig().getOption("list-player", ""));
        listSortByName = getConfig().getOption("list-sort-by-name", true);

        ircConnCheckInterval = getConfig().getOption("conn-check-interval", 1000L);
        reconnectSuppression = getConfig().getOption("reconnect-fail-message-count", 10);
        ircChannelCheckInterval = getConfig().getOption("channel-check-interval", 100L);

    }

    private void loadBots() {
        if (botsFolder.exists()) {
            logInfo("Checking for bot files in " + botsFolder);
            for (final File file : botsFolder.listFiles()) {
                if (file.getName().toLowerCase().endsWith(".yml")) {
                    logInfo("Loading bot file: " + file.getName());
                    PurpleBot ircBot = new PurpleBot(file, this);
                    if (ircBot.goodBot) {
                        ircBots.put(file.getName(), ircBot);
                        logInfo("Loaded bot: " + file.getName() + " [" + ircBot.botNick + "]");
                    } else {
                        logError("Bot not loaded: " + file.getName());
                    }
                }
            }
        }
    }

    private void createSampleBot() {
        File file = new File(pluginFolder, sampleFileName);
        logInfo("Creating sample bot file: " + file.getAbsolutePath());
        try {
            try (InputStream in = PurpleIRC.class.getResourceAsStream("/" + sampleFileName)) {
                byte[] buf = new byte[1024];
                int len;
                try (OutputStream out = new FileOutputStream(file)) {
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException ex) {
            logError("Problem creating sample bot: " + ex.getMessage());
        }
    }

    private void createConfig() {
        if (configFile.exists()) {
            return;
        }
        logInfo("Creating config file: " + configFile.getAbsolutePath());
        try {
            try (InputStream in = PurpleIRC.class.getResourceAsStream("/" + configFileName)) {
                byte[] buf = new byte[1024];
                int len;
                try (OutputStream out = new FileOutputStream(configFile)) {
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException ex) {
            logError("Problem creating config: " + ex.getMessage());
        }
    }

    /**
     *
     * @param sender
     * @throws java.io.IOException
     */
    public void reloadMainConfig(CommandSender sender) throws IOException {
        sender.sendMessage(LOG_HEADER_F + " Reloading config.yml ...");
        loadConfig();
        sender.sendMessage(LOG_HEADER_F + " Done.");
    }

    private void createConfigDirs() {
        if (!pluginFolder.exists()) {
            try {
                logInfo("Creating " + pluginFolder.getAbsolutePath());
                pluginFolder.mkdir();
            } catch (Exception e) {
                logError(e.getMessage());
            }
        }

        if (!botsFolder.exists()) {
            try {
                logInfo("Creating " + botsFolder.getAbsolutePath());
                botsFolder.mkdir();
            } catch (Exception e) {
                logError(e.getMessage());
            }
        }
    }

    /**
     *
     * @param message
     */
    public void logInfo(String message) {
        log.info(message);
    }

    /**
     *
     * @param message
     */
    public void logError(String message) {
        log.error(message);
    }

    /**
     *
     * @param message
     */
    public void logDebug(String message) {
        if (debugEnabled) {
            log.debug("[DEBUG] " + message);
        }
    }

    /**
     *
     * @return
     */
    public String getMCUptime() {
        long jvmUptime = ManagementFactory.getRuntimeMXBean().getUptime();
        String msg = "Server uptime: " + (int) (jvmUptime / 86400000L) + " days"
                + " " + (int) (jvmUptime / 3600000L % 24L) + " hours"
                + " " + (int) (jvmUptime / 60000L % 60L) + " minutes"
                + " " + (int) (jvmUptime / 1000L % 60L) + " seconds.";
        return msg;
    }

    public String getServerMotd() {
        return "MOTD: " + MinecraftServer.getServer().getMOTD();
    }

    /**
     *
     * @param ircBot
     * @param channelName
     * @return
     */
    public String getMCPlayers(PurpleBot ircBot, String channelName) {
        Map<String, String> playerList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayerMP ep = (EntityPlayerMP) obj;
            String pName = tokenizer.playerTokenizer(ep, listPlayer);
            playerList.put(ep.getCommandSenderName(), pName);
        }

        String pList;
        if (!listSortByName) {
            // sort as before
            ArrayList<String> tmp = new ArrayList<>(playerList.values());
            Collections.sort(tmp, Collator.getInstance());
            pList = Joiner.on(listSeparator).join(tmp);
        } else {
            // sort without nick prefixes
            pList = Joiner.on(listSeparator).join(playerList.values());
        }

        String msg = listFormat
                .replace("%COUNT%", Integer.toString(playerList.size()))
                .replace("%MAX%", Integer.toString(MinecraftServer.getServer().getMaxPlayers()))
                .replace("%PLAYERS%", pList);
        logDebug("L: " + msg);
        return colorConverter.gameColorsToIrc(msg);
    }

    public String getRemotePlayers(String commandArgs) {
        if (commandArgs != null) {
            String host;
            int port = 25565;
            if (commandArgs.contains(":")) {
                host = commandArgs.split(":")[0];
                port = Integer.parseInt(commandArgs.split(":")[1]);
            } else {
                host = commandArgs;
            }
            Query query = new Query(host, port);
            try {
                query.sendQuery();
            } catch (IOException ex) {
                return ex.getMessage();
            }
            String players[] = query.getOnlineUsernames();
            String m;
            if (players.length == 0) {
                m = "There are no players on " + host
                        + ":" + port;
            } else {
                m = "Players on " + host + "("
                        + players.length
                        + "): " + Joiner.on(", ")
                        .join(players);
            }
            return m;
        } else {
            return "Invalid host.";
        }
    }

    /**
     *
     * @param player
     * @return
     */
    public UUID getPlayerUuid(String player) {
        if (uuidCache.containsKey(player)) {
            return uuidCache.get(player);
        }
        return null;
    }

    /**
     *
     * @param pName
     * @return
     */
    public String getDisplayName(String pName) {
        String displayName = null;
        EntityPlayerMP player = getPlayer(pName);
        logDebug("player: " + player);
        if (player != null) {
            displayName = player.getDisplayName();
        }
        if (displayName != null) {
            logDebug("Caching displayName for " + pName + " = " + displayName);
            displayNameCache.put(pName, displayName);
        } else if (displayNameCache.containsKey(pName)) {
            displayName = displayNameCache.get(pName);
        } else {
            displayName = pName;
        }
        return displayName;
    }

    /**
     *
     * @param player
     */
    public void updateDisplayNameCache(EntityPlayerMP player) {
        logDebug("Caching displayName for " + player.getCommandSenderName() + " = " + player.getDisplayName());
        displayNameCache.put(player.getCommandSenderName(), player.getDisplayName());
    }

    /**
     *
     * @param player
     * @param displayName
     */
    public void updateDisplayNameCache(String player, String displayName) {
        logDebug("Caching displayName for " + player + " = " + displayName);
        displayNameCache.put(player, displayName);
    }

    /**
     *
     * @param player
     */
    public void updateUuidCache(EntityPlayerMP player) {
        logDebug("Caching UUID for " + player.getCommandSenderName() + " = " + player.getUniqueID().toString());
        uuidCache.put(player.getCommandSenderName(), player.getUniqueID());
    }

    /**
     *
     * @param player
     * @param uuid
     */
    public void updateUuidCache(String player, UUID uuid) {
        logDebug("Caching UUID for " + player + " = " + uuid.toString());
        uuidCache.put(player, uuid);
    }

    public void saveDisplayNameCache() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(cacheFile));
        } catch (IOException ex) {
            logError(ex.getMessage());
            return;
        }

        try {
            for (String s : displayNameCache.keySet()) {
                logDebug("Saving to displayName.cache: " + s + "\t" + displayNameCache.get(s));
                writer.write(s + "\t" + displayNameCache.get(s) + "\n");
            }
            writer.close();
        } catch (IOException ex) {
            logError(ex.getMessage());
        }
    }

    public void loadDisplayNameCache() {
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(cacheFile))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("\n")) {
                        continue;
                    }
                    String[] parts = line.split("\t", 2);
                    updateDisplayNameCache(parts[0], parts[1]);
                }
            }
        } catch (IOException | NumberFormatException e) {
            logError(e.getMessage());
        }
    }

    public void saveUuidCache() {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(uuidCacheFile));
        } catch (IOException ex) {
            logError(ex.getMessage());
            return;
        }

        try {
            for (String s : uuidCache.keySet()) {
                logDebug("Saving to uuid.cache: " + s + "\t" + uuidCache.get(s).toString());
                writer.write(s + "\t" + uuidCache.get(s).toString() + "\n");
            }
            writer.close();
        } catch (IOException ex) {
            logError(ex.getMessage());
        }
    }

    public void loadUuidCache() {
        try {
            try (BufferedReader in = new BufferedReader(new FileReader(uuidCacheFile))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("\n")) {
                        continue;
                    }
                    String[] parts = line.split("\t", 2);
                    updateUuidCache(parts[0], UUID.fromString(parts[1]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            logError(e.getMessage());
        }
    }

    public String getPlayerHost(final String playerIP) {
        if (playerIP == null) {
            return "unknown";
        }
        if (hostCache.containsKey(playerIP)) {
            return hostCache.get(playerIP);
        } else {
            Timer timer = new Timer();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    long a = System.currentTimeMillis();
                    InetAddress addr = null;
                    try {
                        addr = InetAddress.getByName(playerIP);
                    } catch (UnknownHostException ex) {
                        logError(ex.getMessage());
                    }
                    String host;
                    if (addr != null) {
                        host = addr.getHostName();
                    } else {
                        host = playerIP;
                    }
                    hostCache.put(playerIP, host);
                    logDebug("getPlayerHost[" + (System.currentTimeMillis() - a) + "ms] " + playerIP + " = " + host);
                }
            }, 0);
            return playerIP;
        }
    }

    public void clearHostCache(final EntityPlayerMP player) {
        String playerIP = player.getPlayerIP();
        if (hostCache.containsKey(playerIP)) {
            hostCache.remove(playerIP);
        }
    }

    public String botify(String bot) {
        if (bot.toLowerCase().endsWith("yml")) {
            return bot;
        } else {
            return bot + ".yml";
        }
    }

    public boolean isUpdateCheckerEnabled() {
        return updateCheckerEnabled;
    }

    public String updateCheckerMode() {
        return updateCheckerMode;
    }

    public void broadcastToGame(final String message, final String permission) {
        MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(message));
    }

    public EntityPlayerMP getPlayerExact(String name) {
        return (EntityPlayerMP) MinecraftServer.getServer().getEntityWorld().getPlayerEntityByName(name);
    }

    public EntityPlayerMP getPlayer(String name) {
        for (Object obj : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayerMP ep = (EntityPlayerMP) obj;
            if (ep.getDisplayName().equalsIgnoreCase(name)) {
                return ep;
            }
        }
        return null;
    }

    public void registerCommands(CommandHandler handler, CommandHandlers handlers) {
        handler.registerCommand(handlers);
    }

    public String getModID() {
        return MOD_ID;
    }

}
