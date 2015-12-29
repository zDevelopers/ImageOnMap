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
package fr.moribus.imageonmap.commands;

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandException;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public abstract class IoMCommand extends Command
{
	protected ImageMap getMapFromArgs() throws CommandException
	{
		return getMapFromArgs(playerSender(), 0, true);
	}

	protected ImageMap getMapFromArgs(Player player, int index, boolean expand) throws CommandException
	{
		if(args.length <= index) throwInvalidArgument("You need to give a map name.");

		ImageMap map;
		String mapName = args[index];

		if(expand)
		{
			for(int i = index + 1, c = args.length; i < c; i++)
			{
				mapName += " " + args[i];
			}
		}

		mapName = mapName.trim();

		map = MapManager.getMap(player.getUniqueId(), mapName);

		if(map == null) error("This map does not exist.");

		return map;
	}

	protected List<String> getMatchingMapNames(Player player, String prefix)
	{
		return getMatchingMapNames(MapManager.getMapList(player.getUniqueId()), prefix);
	}

	protected List<String> getMatchingMapNames(Iterable<? extends ImageMap> maps, String prefix)
	{
		List<String> matches = new ArrayList<>();

		for(ImageMap map : maps)
		{
			if(map.getId().startsWith(prefix)) matches.add(map.getId());
		}

		return matches;
	}
}
