/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
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

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.MapManagerException;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.commands.WithFlags;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@CommandInfo (name =  "deleteother", usageParameters = "<player name> <map name>")
@WithFlags ({"confirm"})
public class DeleteOtherCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
    	if(!playerSender().hasPermission("imageonmap.delete.other")) {
    		warning(I.t("You do not have permission for this command. (imageonmap.delete.other)"));
    		return;
    	}
    	if(args.length < 2) warning(I.t("Not enough parameters! Usage: /maptool deleteother <playername> <mapname>"));
    	
		Player player = null;
		UUID uuid = null;
		OfflinePlayer op = null;
        player = Bukkit.getPlayer(args[0]);
        if(player == null){
        	op = Bukkit.getOfflinePlayer(args[0]);
			if(op.hasPlayedBefore()) uuid = op.getUniqueId();
			else warning(I.t("We've never seen that player before!"));
        }
        else uuid = player.getUniqueId();
        String mapName = "";
        mapName = args[1];
		if(args.length > 2) for(int i = 2; i < args.length; i++) mapName += (" " + args[i - 1]);
		
		ImageMap map = MapManager.getMap(uuid, mapName);
        
        if(player != null) MapManager.clear(player.getInventory(), map);
        
            try
            {
                MapManager.deleteMap(map);
                getPlayer().sendMessage(I.t("{gray}Map successfully deleted."));
            }
            catch (MapManagerException ex)
            {
                PluginLogger.warning(I.t("A non-existent map was requested to be deleted", ex));
                getPlayer().sendMessage(ChatColor.RED+(I.t("This map does not exist.")));
            }
        }
   
    
    @Override
    protected List<String> complete() throws CommandException
    {
        if(args.length == 1) 
            return getMatchingMapNames(playerSender(), args[0]);

        return null;
    }
    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.DELETEOTHER.grantedTo(sender);
    }
}
