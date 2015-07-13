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

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;


/**
 * @author IamBlueSlime, Amaury Carrade
 *
 * Changes by Amaury Carrade to use statics (beh, code style, these things).
 */
public class GuiListener implements Listener {

	public static void init(Plugin plugin)
	{
		plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (event.getWhoClicked() instanceof Player)
		{
			Player player = (Player) event.getWhoClicked();
			AbstractGui gui = GuiManager.getPlayerGui(player);

			if(gui == null)
				return;

			if (event.getInventory() instanceof PlayerInventory)
				return;


			/* *** Click from player inventory (with shift) *** */

			if(event.getRawSlot() != event.getSlot())
			{
				if(event.isShiftClick())
				{
					gui.onItemDeposit(player, event.getCurrentItem(), event.getClick(), event.getAction(), event);
				}
				return;
			}


			/* *** Click on the GUI inventory *** */

			if(event.getCursor() != null && event.getCursor().getType() != Material.AIR)
			{
				gui.onItemDeposit(player, event.getCursor(), event.getClick(), event.getAction(), event);
			}

			else
			{
				String action = gui.getAction(event.getSlot());

				if (action != null)
					gui.onClick(player, event.getCurrentItem(), action, event.getClick(), event.getAction(), event);
			}

			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event)
	{
		Player player = (Player) event.getWhoClicked();
		AbstractGui gui = GuiManager.getPlayerGui(player);

		if (gui != null)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (GuiManager.getPlayerGui(event.getPlayer()) != null)
			GuiManager.removeClosedGui((Player) event.getPlayer());
	}
}
