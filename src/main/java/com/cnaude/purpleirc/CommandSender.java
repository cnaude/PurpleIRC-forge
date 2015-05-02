package com.cnaude.purpleirc;

import com.cnaude.purpleirc.Utilities.ChatColor;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

/**
 *
 * @author cnaude
 */
public class CommandSender {
    
    ICommandSender sender;    
    PurpleIRC plugin;
    
    public CommandSender(ICommandSender sender, PurpleIRC plugin) {
        this.sender = sender;
        this.plugin = plugin;        
    }
    
    public void sendMessage(String message) {
        if (sender instanceof EntityPlayer) {
            sender.addChatMessage(new ChatComponentText(message));
        } else {
            sender.addChatMessage(new ChatComponentText(ChatColor.stripColor(message)));
        }
        
    }
    
    public boolean hasPermission(String permission) {
        if (sender instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) sender;
            return MinecraftServer.getServer().getConfigurationManager().func_152596_g(ep.getGameProfile());
        }
        return true;
    }
    
    public EntityPlayer getPlayer() {
        return plugin.getPlayer(sender.getCommandSenderName());
    }
    
    public boolean isPlayer() {
        return sender instanceof EntityPlayerMP;
    }
}
