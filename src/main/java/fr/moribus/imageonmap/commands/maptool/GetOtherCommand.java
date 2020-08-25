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


import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;


@CommandInfo (name = "getother", usageParameters = "<PlayerName> <MapName>")
public class GetOtherCommand extends IoMCommand
{
	@SuppressWarnings("deprecation")
	@Override
	protected void run() throws CommandException
	{
    	if(args.length < 2) warning(I.t("Not enough parameters! Usage: /maptool getother <playername> <mapname>"));
		//Deny those who do not have permission.
		if(!playerSender().hasPermission("imageonmap.get.other")) {
    		warning(I.t("You do not have permission for this command. (imageonmap.get.other)"));
    		return;
    	}
		
		Player player = null;
		UUID uuid = null;
        player = Bukkit.getPlayer(args[0]);
        if(player == null){
        	OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
			if(op.hasPlayedBefore()) uuid = op.getUniqueId();
			else warning(I.t("We've never seen that player before!"));
        }
        else {
        	uuid = player.getUniqueId();        	
        }
		ImageMap map = null;
		String mapName = "";
		mapName = args[1];
		if(args.length > 2) {
			for(int i = 2; i < args.length; i++) {
				mapName += (" " + args[i - 1]);
			}
		}
		map = MapManager.getMap(uuid, mapName);
		map.give(playerSender());
		return;
	}
    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.GETOTHER.grantedTo(sender);
    }
}
