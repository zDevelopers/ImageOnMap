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

import fr.moribus.imageonmap.*;
import fr.moribus.imageonmap.gui.core.*;
import fr.moribus.imageonmap.map.*;
import fr.moribus.imageonmap.ui.MapItemManager;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.text.*;
import java.util.*;


public class MapListGui extends AbstractGui
{
	private final Integer MAPS_PER_PAGE = 7 * 3;

	private final NumberFormat bigNumbersFormatter = new DecimalFormat("###,###,###,###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));


	private List<ImageMap> maps = new ArrayList<>();

	private int currentPage = 0;
	private int lastPage = 0;


	public MapListGui() {

	}

	public MapListGui(Integer initialPage) {
		currentPage = initialPage;
	}

	@Override
	public void display(Player player) {

		inventory = Bukkit.createInventory(player, 6 * 9, ChatColor.BLACK + "Your maps");

		player.openInventory(getInventory());


		/* ** Statistics ** */

		int imagesCount = MapManager.getMapList(player.getUniqueId()).size();
		int mapPartCount = MapManager.getMapPartCount(player.getUniqueId());

		int mapGlobalLimit = PluginConfiguration.MAP_GLOBAL_LIMIT.getInteger();
		int mapPersonalLimit = PluginConfiguration.MAP_PLAYER_LIMIT.getInteger();

		int mapPartGloballyLeft = mapGlobalLimit - MapManager.getMapCount();
		int mapPartPersonallyLeft = mapPersonalLimit - mapPartCount;

		int mapPartLeft;
		if(mapGlobalLimit <= 0 && mapPersonalLimit <= 0)
			mapPartLeft = -1;
		else if(mapGlobalLimit <= 0)
			mapPartLeft = mapPartPersonallyLeft;
		else if(mapPersonalLimit <= 0)
			mapPartLeft = mapPartGloballyLeft;
		else
			mapPartLeft = Math.min(mapPartGloballyLeft, mapPartPersonallyLeft);

		double percentageUsed = mapPartLeft < 0 ? 0 : ((double) mapPartCount) / ((double) (mapPartCount + mapPartLeft)) * 100;


		ItemStack statistics = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta meta = statistics.getItemMeta();

		meta.setDisplayName(ChatColor.BLUE + "Usage statistics");
		meta.setLore(Arrays.asList(
				"",
				getStatisticText("Images rendered", imagesCount),
				getStatisticText("Minecraft maps used", mapPartCount)
		));

		if(mapPartLeft >= 0)
		{
			List<String> lore = meta.getLore();

			lore.add("");
			lore.add(ChatColor.BLUE + "Minecraft maps limits");

			lore.add("");
			lore.add(getStatisticText("Server-wide limit", mapGlobalLimit, true));
			lore.add(getStatisticText("Per-player limit", mapPersonalLimit, true));

			lore.add("");
			lore.add(getStatisticText("Current consumption", ((int) Math.rint(percentageUsed)) + " %"));
			lore.add(getStatisticText("Maps left", mapPartLeft));
			lore.add("");

			meta.setLore(lore);
		}

		GuiUtils.removeVanillaInfos(meta);

		statistics.setItemMeta(meta);
		setSlotData(statistics, inventory.getSize() - 5, "");


		/* ** Maps ** */

		update(player);
	}

	@Override
	public void update(Player player) {
		update(player, false);
	}

	public void update(final Player player, final Boolean noCache) {
		if (maps == null || maps.isEmpty() || noCache) {
			updateMapCache(player);
		}


		if(maps.isEmpty()) {
			ItemStack empty = new ItemStack(Material.BARRIER);
			ItemMeta meta = empty.getItemMeta();
			meta.setDisplayName(ChatColor.RED + "Nothing to display here");
			meta.setLore(Arrays.asList(
					ChatColor.GRAY + "You don't have any map.",
					ChatColor.GRAY + "Get started by creating a new one",
					ChatColor.GRAY + "with " + ChatColor.WHITE + "/tomap <URL> [resize]" + ChatColor.GRAY + "!"
			));

			empty.setItemMeta(meta);

			setSlotData(empty, 13, "");

			return;
		}


		int index = currentPage * MAPS_PER_PAGE;
		int lastIndex = index + MAPS_PER_PAGE;
		int slot = 10;

		ImageMap map;

		for (; index < lastIndex; index++) {
			try {
				map = maps.get(index);
				setSlotData(getMapIcon(map), slot, map.getId());
			} catch(IndexOutOfBoundsException e) {
				setSlotData(new ItemStack(Material.AIR), slot, "");
			}

			if (slot % 9 == 7) slot += 3;
			else slot++;
		}

		if (currentPage != 0)
			setSlotData(getPageIcon(currentPage - 1), inventory.getSize() - 9, "previousPage");
		else
			setSlotData(new ItemStack(Material.AIR), inventory.getSize() - 9, "");

		if (currentPage != lastPage)
			setSlotData(getPageIcon(currentPage + 1), inventory.getSize() - 1, "nextPage");
		else
			setSlotData(new ItemStack(Material.AIR), inventory.getSize() - 1, "");
	}

	@Override
	public void onClick(Player player, ItemStack stack, String action, ClickType clickType, InventoryAction invAction, InventoryClickEvent ev)
	{
		switch (action)
		{
			case "previousPage":
				previousPage(player);
				return;

			case "nextPage":
				nextPage(player);
				return;

			default:
				// The action is the map ID
				ImageMap map = null;
				for(ImageMap lookupMap : maps)
				{
					if(lookupMap.getId().equals(action))
					{
						map = lookupMap;
						break;
					}
				}

				if(map == null) return;

				switch (clickType)
				{
					case LEFT:
					case SHIFT_LEFT:

						if(map.getType() == ImageMap.Type.SINGLE)
						{
							if(invAction == InventoryAction.MOVE_TO_OTHER_INVENTORY)
							{
								map.give(player);
							}
							else
							{
								ev.setCursor(MapItemManager.createMapItem(map.getMapsIDs()[0], map.getName()));
							}
						}

						else
						{
							if (map.give(player)) {
								player.sendMessage(ChatColor.GRAY + "The requested map was too big to fit in your inventory.");
								player.sendMessage(ChatColor.GRAY + "Use '/maptool getremaining' to get the remaining maps.");
							}
						}

						break;

					case RIGHT:
					case SHIFT_RIGHT:
						GuiManager.openGui(player, new MapDetailGui(map, currentPage));
						break;
				}
		}
	}

	@Override
	public void onItemDeposit(Player player, ItemStack stack, ClickType clickType, InventoryAction invAction, InventoryClickEvent ev) {
		ev.setCancelled(true);

		if (stack.getType() == Material.MAP && MapManager.managesMap(stack))
		{
			ImageMap map = MapManager.getMap(stack);

			if (map != null)
			{
				MapManager.clear(player.getInventory(), map);

				// Deprecated? Yes. Alternatives? No, as usual...
				ev.setCursor(new ItemStack(Material.AIR));
			}
		}
	}

	private void nextPage(Player player)
	{
		if(currentPage < lastPage) currentPage++;

		update(player);
	}

	private void previousPage(Player player)
	{
		if(currentPage > 0) currentPage--;

		update(player);
	}


	private void updateMapCache(Player player)
	{
		maps = MapManager.getMapList(player.getUniqueId());

		lastPage = (int) Math.ceil(((double) maps.size()) / ((double) MAPS_PER_PAGE)) - 1;

		if(currentPage > lastPage)
			currentPage = lastPage;
	}

	private ItemStack getMapIcon(ImageMap map)
	{
		ItemStack icon = new ItemStack(Material.MAP);


		ItemMeta meta = icon.getItemMeta();

		meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + map.getName());

		meta.setLore(Arrays.asList(
				ChatColor.WHITE + "" + map.getMapCount() + " map" + (map.getMapCount() != 1 ? "s" : ""),
				"",
				ChatColor.GRAY + "Map ID: " + map.getId(),
				"",
				ChatColor.GRAY + "» Left-click to get this map",
				ChatColor.GRAY + "» Right-click for details"
		));

		GuiUtils.removeVanillaInfos(meta);

		icon.setItemMeta(meta);


		return icon;
	}

	private ItemStack getPageIcon(Integer targetPage)
	{
		ItemStack icon = new ItemStack(Material.ARROW);
		ItemMeta meta = icon.getItemMeta();


		if(currentPage < targetPage) { // next page
			meta.setDisplayName(ChatColor.GREEN + "Next page");
		}
		else {
			meta.setDisplayName(ChatColor.GREEN + "Previous page");
		}

		meta.setLore(Collections.singletonList(
				ChatColor.GRAY + "Go to page " + ChatColor.WHITE + (targetPage + 1) + ChatColor.GRAY + " of " + ChatColor.WHITE + (lastPage + 1)
		));


		icon.setItemMeta(meta);

		return icon;
	}

	private String getStatisticText(String title, Integer value)
	{
		return getStatisticText(title, value, false);
	}

	private String getStatisticText(String title, Integer value, boolean zeroIsUnlimited)
	{
		return getStatisticText(title, zeroIsUnlimited && value <= 0 ? "unlimited" : bigNumbersFormatter.format(value));
	}

	private String getStatisticText(String title, String value)
	{
		return ChatColor.GRAY + title + ": " + ChatColor.WHITE + value;
	}
}
