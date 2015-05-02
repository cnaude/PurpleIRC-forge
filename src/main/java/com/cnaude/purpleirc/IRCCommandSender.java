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

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

/**
 *
 * @author Chris Naude We have to implement our own ICommandSender so that we can
 * receive output from the command dispatcher.
 */
public class IRCCommandSender implements ICommandSender {

    private final PurpleBot ircBot;
    private final String target;
    private final PurpleIRC plugin;
    private final boolean ctcpResponse;
    private final String name;

    private void addMessageToQueue(String message) {
        plugin.logDebug("addMessageToQueue: " + message);
        ircBot.messageQueue.add(new IRCMessage(target,
                plugin.colorConverter.gameColorsToIrc(message), ctcpResponse));
    }

    /**
     *
     * @param ircBot
     * @param target
     * @param plugin
     * @param ctcpResponse
     * @param name
     */
    public IRCCommandSender(PurpleBot ircBot, String target, PurpleIRC plugin, boolean ctcpResponse, String name) {
        super();
        this.target = target;
        this.ircBot = ircBot;
        this.plugin = plugin;
        this.ctcpResponse = ctcpResponse;
        this.name = name;
    }

    @Override
    public String getCommandSenderName() {
        return this.name;
    }

    @Override
    public IChatComponent func_145748_c_() {
        return new ChatComponentText(this.getCommandSenderName());
    }

    @Override
    public void addChatMessage(IChatComponent chatComponent) {
        addMessageToQueue(chatComponent.getUnformattedText());
    }

    @Override
    public boolean canCommandSenderUseCommand(int level, String var2) {
        return true;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates(0, 0, 0);
    }

    @Override
    public World getEntityWorld() {
        return MinecraftServer.getServer().getEntityWorld();
    }

}
