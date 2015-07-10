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

package fr.moribus.imageonmap.gui.list;

import fr.moribus.imageonmap.gui.core.*;
import fr.moribus.imageonmap.map.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;


public class CategorySelectionGui extends AbstractGui {

	@Override
	public void display(Player player)
	{

		inventory = Bukkit.createInventory(player, 3 * 9, ChatColor.BLACK + "Maps » Choose a category");


		ItemStack singleMaps = new ItemStack(Material.MAP);
		ItemMeta meta = singleMaps.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Single maps");
		singleMaps.setItemMeta(meta);


		ItemStack postersMaps = new ItemStack(Material.PAINTING);
		meta = postersMaps.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Posters");
		postersMaps.setItemMeta(meta);


		setSlotData(singleMaps, 11, "single");
		setSlotData(postersMaps, 15, "poster");

		player.openInventory(getInventory());
	}

	@Override
	public void onClick(Player player, ItemStack stack, String action)
	{

		switch (action)
		{
			case "poster":
				GuiManager.openGui(player, new MapListGui(ImageMap.Type.POSTER));
				break;

			case "single":
				GuiManager.openGui(player, new MapListGui(ImageMap.Type.SINGLE));
				break;
		}
	}
}
