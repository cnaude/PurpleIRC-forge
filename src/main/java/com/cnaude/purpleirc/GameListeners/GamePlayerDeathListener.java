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
import com.cnaude.purpleirc.TemplateName;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/**
 *
 * @author cnaude
 */
public class GamePlayerDeathListener {

    private final PurpleIRC plugin;

    /**
     *
     * @param plugin
     */
    public GamePlayerDeathListener(PurpleIRC plugin) {
        this.plugin = plugin;
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            final EntityPlayerMP player = (EntityPlayerMP) event.entity;
            for (PurpleBot ircBot : plugin.ircBots.values()) {
                ircBot.gameDeath(player, event.entityLiving.func_110142_aN().func_151521_b().getUnformattedText(), TemplateName.GAME_DEATH);
            }

        }
    }

}
