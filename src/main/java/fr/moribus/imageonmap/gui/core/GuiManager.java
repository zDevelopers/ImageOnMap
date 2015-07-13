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

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A tool to easily manage GUIs.
 *
 * @author IamBlueSlime and Amaury Carrade.
 */
public class GuiManager {

	protected static Map<UUID, AbstractGui> currentGUIs;

	/**
	 * Call this when the plugin is enabled.
	 *
	 * @param plugin The plugin using this.
	 */
	public static void init(Plugin plugin)
	{
		currentGUIs = new ConcurrentHashMap<>();

		GuiListener.init(plugin);
	}

	/**
	 * Opens a GUI for the given player.
	 *
	 * Closes any GUI already open for this player.
	 *
	 * @param player The player this GUI will be open to.
	 * @param gui The GUI to open.
	 */
	public static void openGui(Player player, AbstractGui gui)
	{
		if (currentGUIs.containsKey(player.getUniqueId()))
			closeGui(player);

		currentGUIs.put(player.getUniqueId(), gui);
		gui.display(player);
	}

	/**
	 * Closes the currently open GUI of this player, if it exists.
	 *
	 * Without any open GUI for this player, does nothing.
	 *
	 * @param player The player.
	 */
	public static void closeGui(Player player)
	{
		player.closeInventory();
		removeClosedGui(player);
	}

	/**
	 * Calls the {@link AbstractGui#onClose(Player)} method of the currently open GUI of the
	 * given {@link Player} and unregisters it as open.
	 *
	 * @param player The player
	 */
	public static void removeClosedGui(Player player)
	{
		if (currentGUIs.containsKey(player.getUniqueId()))
		{
			//noinspection ConstantConditions
			getPlayerGui(player).onClose(player);
			currentGUIs.remove(player.getUniqueId());
		}
	}

	/**
	 * Returns the currently open {@link AbstractGui} of the given {@link HumanEntity}.
	 *
	 * @param player The HumanEntity.
	 * @return The open GUI, or {@code null} if no GUI are open.
	 */
	public static AbstractGui getPlayerGui(HumanEntity player)
	{
		if (currentGUIs.containsKey(player.getUniqueId()))
			return currentGUIs.get(player.getUniqueId());

		return null;
	}

	/**
	 * Returns all open GUIs.
	 *
	 * @return The GUI (map: player's {@link UUID} → {@link AbstractGui}).
	 */
	public static Map<UUID, AbstractGui> getPlayersGui()
	{
		return currentGUIs;
	}
}
