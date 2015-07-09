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

package fr.moribus.imageonmap.gui.core;

import fr.moribus.imageonmap.*;
import org.bukkit.entity.*;

import java.util.*;
import java.util.concurrent.*;


/**
 * @author IamBlueSlime (thanks c:)
 *
 * Changes by Amaury Carrade to use statics (beh, code style, these things).
 */
public class GuiManager {
	protected static ConcurrentHashMap<UUID, AbstractGui> currentGUIs;

	public static void init(ImageOnMap plugin)
	{
		currentGUIs = new ConcurrentHashMap<>();

		GuiListener.init(plugin);
	}

	public static void openGui(Player player, AbstractGui gui)
	{
		if (currentGUIs.containsKey(player.getUniqueId()))
			closeGui(player);

		currentGUIs.put(player.getUniqueId(), gui);
		gui.display(player);
	}

	public static void closeGui(Player player)
	{
		player.closeInventory();
		removeClosedGui(player);
	}

	public static void removeClosedGui(Player player)
	{
		if (currentGUIs.containsKey(player.getUniqueId()))
		{
			getPlayerGui(player).onClose(player);
			currentGUIs.remove(player.getUniqueId());
		}
	}

	public static AbstractGui getPlayerGui(HumanEntity player)
	{
		if (currentGUIs.containsKey(player.getUniqueId()))
			return currentGUIs.get(player.getUniqueId());

		return null;
	}

	public static ConcurrentHashMap<UUID, AbstractGui> getPlayersGui()
	{
		return currentGUIs;
	}
}
