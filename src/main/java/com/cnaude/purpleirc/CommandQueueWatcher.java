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

import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;

/**
 *
 * @author Chris Naude Poll the command queue and dispatch to Bukkit
 */
public class CommandQueueWatcher {

    private final PurpleIRC plugin;
    Timer timer;
    private final Queue<IRCCommand> queue = new ConcurrentLinkedQueue<>();

    /**
     *
     * @param plugin
     */
    public CommandQueueWatcher(final PurpleIRC plugin) {
        this.plugin = plugin;
        timer = new Timer();
        startWatcher();
    }

    private void startWatcher() {
        plugin.logDebug("Starting command queue");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                queueAndSend();
            }
        }, 0, 5);
    }

    private void queueAndSend() {
        IRCCommand ircCommand = queue.poll();
        if (ircCommand != null) {
            try {
                String cmd = ircCommand.getGameCommand().split(" ")[0];
                plugin.logDebug("CMD: " + cmd);

                plugin.logDebug("Dispatching command as IRCCommandSender: " + ircCommand.getGameCommand());                
                MinecraftServer.getServer().getCommandManager().executeCommand(ircCommand.getIRCCommandSender(), ircCommand.getGameCommand());

            } catch (CommandException ce) {
                plugin.logError("Error running command: " + ce.getMessage());
            }
        }
    }

    public void cancel() {
        timer.cancel();
    }

    public String clearQueue() {
        int size = queue.size();
        if (!queue.isEmpty()) {
            queue.clear();
        }
        return "Elements removed from command queue: " + size;
    }

    /**
     *
     * @param command
     */
    public void add(IRCCommand command) {
        plugin.logDebug("Adding command to queue: " + command.getGameCommand());
        queue.offer(command);
    }
}
