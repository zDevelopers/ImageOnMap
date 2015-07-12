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

import fr.moribus.imageonmap.gui.core.AbstractGui;
import fr.moribus.imageonmap.gui.core.GuiManager;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.ui.MapItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;


public class MapDetailGui extends AbstractGui
{

	/**
	 * The max width of the window open on the image.
	 */
	private final int MAX_WINDOW_WIDTH = 7;

	/**
	 * The max height of the window open on the image.
	 */
	private final int MAX_WINDOW_HEIGHT = 3;



	/**
	 * The map displayed in this GUI.
	 */
	ImageMap map;

	/**
	 * The previously-viewed page of the list GUI.
	 * Used to be able to bring the user back to the same page.
	 */
	int currentPage;

	/**
	 * The row currently at the top of the window open on the displayed map.
	 */
	int topRow = 0;

	/**
	 * The column currently at the top of the window open on the displayed map.
	 */
	int topColumn = 0;


	/**
	 *
	 * @param map The map displayed in this GUI.
	 */
	public MapDetailGui(ImageMap map)
	{
		this(map, 0);
	}

	/**
	 *
	 * @param map The map displayed in this GUI.
	 * @param currentPage The previously-viewed page of the list GUI.
	 */
	public MapDetailGui(ImageMap map, int currentPage)
	{
		this.map = map;
		this.currentPage = currentPage;
	}


	@Override
	public void display(Player player)
	{
		inventory = Bukkit.createInventory(player, 6 * 9, ChatColor.BLACK + "Your maps");

		ItemStack back = new ItemStack(Material.EMERALD);
		ItemMeta meta = back.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "« Back");
		meta.setLore(Collections.singletonList(
				ChatColor.GRAY + "Go back to the list."
		));
		back.setItemMeta(meta);

		setSlotData(back, inventory.getSize() - 5, "back");

		update(player);

		player.openInventory(inventory);
	}

	@Override
	public void update(Player player)
	{
		/* ** The map itself ** */

		if(map instanceof PosterMap && ((PosterMap) map).hasColumnData()) {
			int slot = 10;

			for (int row = topRow; row < topRow + MAX_WINDOW_HEIGHT; row++)
			{
				for (int col = topColumn; col < topColumn + MAX_WINDOW_WIDTH; col++)
				{
					if(col < ((PosterMap) map).getColumnCount() && row < ((PosterMap) map).getRowCount())
					{
						setSlotData(getMapPartRepresentation(col, row), slot, col + ";" + row);
					}
					else
					{
						setSlotData(new ItemStack(Material.AIR), slot, "");
					}

					slot++;
				}

				slot += 2;
			}

			placeArrowSlider('\u2B07', canGoDown(), 35, "down");
			placeArrowSlider('\u2B06', canGoUp()   , 17, "up"   );
			placeArrowSlider('«'     , canGoLeft() , 37, "left" );
			placeArrowSlider('»'     , canGoRight(), 43, "right");
		}

		else
		{
			setSlotData(getMapPartRepresentation(0, 0), 13, "0;0");
		}
	}

	private ItemStack getMapPartRepresentation(int col, int row)
	{
		ItemStack part = new ItemStack(Material.MAP);
		ItemMeta meta = part.getItemMeta();

		meta.setDisplayName(ChatColor.GREEN + "Map part");
		meta.setLore(Arrays.asList(
				ChatColor.GRAY + "Column: " + ChatColor.WHITE + (col + 1),
				ChatColor.GRAY + "Row: " + ChatColor.WHITE + (row + 1),
				"",
				ChatColor.GRAY + "» Click to get only this part"
		));

		part.setItemMeta(meta);
		return part;
	}

	private void placeArrowSlider(Character nameCharacter, boolean active, int slot, String action)
	{
		/* ** Item ** */

		ItemStack slider = new ItemStack(Material.ARROW);
		ItemMeta meta = slider.getItemMeta();

		String title = "";
		for(int i = 0; i < 5; i++)
			title += nameCharacter;

		meta.setDisplayName((active ? ChatColor.GREEN : ChatColor.GRAY) + title);

		slider.setItemMeta(meta);


		/* ** Placement ** */

		setSlotData(slider, slot, active ? action : "");
	}


	@Override
	public void onClick(Player player, ItemStack stack, String action, ClickType click, InventoryAction invAction, InventoryClickEvent ev)
	{
		switch (action)
		{
			case "down":
				goDown(player);
				return;

			case "up":
				goUp(player);
				return;

			case "left":
				goLeft(player);
				return;

			case "right":
				goRight(player);
				return;

			case "back":
				goBack(player);
				return;

			default:

				Integer col;
				Integer row;

				if(map instanceof PosterMap && ((PosterMap) map).hasColumnData()) {
					String[] coords = action.split(";");

					if (coords.length != 2) return; // Other unhandled action.

					try {
						col = Integer.valueOf(coords[0]);
						row = Integer.valueOf(coords[1]);

					} catch (NumberFormatException e) {
						return; // Other unhandled action.
					}
				}
				else
				{
					col = row = 0;
				}

				ev.setCursor(MapItemManager.createSubMapItem(map, col, row));
		}
	}


	private void goDown(Player player)
	{
		if (!(map instanceof PosterMap)) return;

		if(canGoDown())
		{
			topRow++;
			update(player);
		}
	}

	private void goUp(Player player)
	{
		if (!(map instanceof PosterMap)) return;

		if(canGoUp())
		{
			topRow--;
			update(player);
		}
	}

	private void goRight(Player player)
	{
		if (!(map instanceof PosterMap)) return;

		if(canGoRight())
		{
			topColumn++;
			update(player);
		}
	}

	private void goLeft(Player player)
	{
		if (!(map instanceof PosterMap)) return;

		if(canGoLeft())
		{
			topColumn--;
			update(player);
		}
	}

	private void goBack(Player player)
	{
		GuiManager.openGui(player, new MapListGui(currentPage));
	}


	private boolean canGoDown()
	{
		return topRow + MAX_WINDOW_HEIGHT < ((PosterMap) map).getRowCount();
	}

	private boolean canGoUp()
	{
		return topRow > 0;
	}

	private boolean canGoRight()
	{
		return topColumn + MAX_WINDOW_WIDTH < ((PosterMap) map).getColumnCount();
	}

	private boolean canGoLeft()
	{
		return topColumn > 0;
	}
}
