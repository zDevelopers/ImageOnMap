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

package fr.moribus.imageonmap.ui;

import fr.moribus.imageonmap.image.MapInitEvent;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.zlib.components.gui.GuiUtils;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.world.FlatLocation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

@SuppressWarnings("deprecation")
abstract public class SplatterMapManager {
	private SplatterMapManager() {
	}

	
	static public ItemStack makeSplatterMap(PosterMap map) {
		ItemStack r = new ItemStack(Material.FILLED_MAP);
		MapMeta mr = (MapMeta) r.getItemMeta();
		mr.setMapId(map.getMapIdAt(0));
		mr.setDisplayName(
				ChatColor.GOLD + map.getName() + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + I.t("Splatter Map"));
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GRAY + map.getId());
		lore.add(ChatColor.BLUE + I.t("Item frames needed"));
		lore.add(ChatColor.GRAY + I.t("{0} × {1}", map.getColumnCount(), map.getRowCount()));
		lore.add(ChatColor.BLUE + I.t("How to use this?"));
		lore.addAll(GuiUtils.generateLore(ChatColor.GRAY + I.t(
				"Place empty item frames on a wall, enough to host the whole map. Then, right-click on the bottom-left frame with this map.")));
		lore.addAll(GuiUtils.generateLore(
				ChatColor.GRAY + I.t("Shift-click one of the placed maps to remove the whole poster in one shot.")));
		mr.setLore(lore);
		mr.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
		mr.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		r.setItemMeta(mr);

		return r;

	}

	static public boolean hasSplatterAttributes(ItemStack itemStack) {
		if (itemStack != null)
			if (itemStack.getType().equals(Material.FILLED_MAP))
				if (itemStack.getItemMeta().hasEnchants())
					if (itemStack.getItemMeta().hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)
							&& itemStack.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)
							&& itemStack.getItemMeta().hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS))
						return true;
		return false;

	}

	static public boolean isSplatterMap(ItemStack itemStack) {
		return hasSplatterAttributes(itemStack) && MapManager.managesMap(itemStack);
	}

	static public boolean hasSplatterMap(Player player, PosterMap map) {
		Inventory playerInventory = player.getInventory();

		for (int i = 0; i < playerInventory.getSize(); ++i) {
			ItemStack item = playerInventory.getItem(i);
			if (isSplatterMap(item) && map.managesMap(item))
				return true;
		}

		return false;
	}

	static public boolean placeSplatterMap(ItemFrame startFrame, Player player) {
		ImageMap map = MapManager.getMap(player.getInventory().getItemInMainHand());
		if (map == null || !(map instanceof PosterMap))
			return false;
		PosterMap poster = (PosterMap) map;

		FlatLocation startLocation = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
		FlatLocation endLocation = startLocation.clone().add(poster.getColumnCount(), poster.getRowCount());
		PosterWall wall = new PosterWall();

		wall.loc1 = startLocation;
		wall.loc2 = endLocation;

		if (!wall.isValid()) {
			player.sendMessage(I.t("{ce}There is not enough space to place this map ({0} × {1}).",
					poster.getColumnCount(), poster.getRowCount()));
			return false;
		}

		int i = 0;
		for (ItemFrame frame : wall.frames) {
			int id = poster.getMapIdAtReverseY(i);
			ItemStack r = new ItemStack(Material.FILLED_MAP, 1);
			MapMeta mr = (MapMeta) r.getItemMeta();
			mr.setMapId(id);
			
			r.setItemMeta(mr);
			frame.setItem(r);
			MapInitEvent.initMap(id);
			++i;
		}

		return true;
	}

	static public PosterMap removeSplatterMap(ItemFrame startFrame) {
		ImageMap map = MapManager.getMap(startFrame.getItem());
		if (map == null || !(map instanceof PosterMap))
			return null;
		PosterMap poster = (PosterMap) map;
		if (!poster.hasColumnData())
			return null;
		FlatLocation loc = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
		ItemFrame[] matchingFrames = PosterWall.getMatchingMapFrames(poster, loc,
				((MapMeta) startFrame.getItem().getItemMeta()).getMapId());
		if (matchingFrames == null)
			return null;

		for (ItemFrame frame : matchingFrames) {
			if (frame != null)
				frame.setItem(null);
		}

		return poster;
	}
}
