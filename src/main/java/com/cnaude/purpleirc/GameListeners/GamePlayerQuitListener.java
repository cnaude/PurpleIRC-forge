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
package com.cnaude.purpleirc.GameListeners;

import com.cnaude.purpleirc.PurpleBot;
import com.cnaude.purpleirc.PurpleIRC;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 * @author cnaude
 */
public class GamePlayerQuitListener {

    private final PurpleIRC plugin;

    /**
     *
     * @param plugin
     */
    public GamePlayerQuitListener(PurpleIRC plugin) {
        this.plugin = plugin;
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        final EntityPlayerMP player = (EntityPlayerMP) event.player;
        final String playerName = player.getCommandSenderName();
        final String displayName = player.getDisplayName();
        plugin.logDebug("QUIT: " + playerName);
        if (plugin.kickedPlayers.contains(playerName)) {
            plugin.kickedPlayers.remove(playerName);
            plugin.logDebug("Player " + playerName + " was in the recently kicked list. Not sending quit message.");
            return;
        }
        for (PurpleBot ircBot : plugin.ircBots.values()) {
            ircBot.gameQuit(player, displayName + " has left the game.");
        }
    }
}
