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

package fr.moribus.imageonmap.guiproko.core;

import fr.moribus.imageonmap.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.*;
import java.util.*;


/**
 * Various utility methods for GUIs.
 */
abstract public class GuiUtils
{
	static private Method addItemFlagsMethod = null;
	static private Object[] itemFlagValues = null;

	/**
	 * Initializes the GUI utilities. This method must be called on plugin enabling.
	 */
	static public void init()
	{
		try
		{
			Class<?> itemFlagClass = Class.forName("org.bukkit.inventory.ItemFlag");
			Method valuesMethod = itemFlagClass.getDeclaredMethod("values");
			itemFlagValues = (Object[]) valuesMethod.invoke(null);
			addItemFlagsMethod = ItemMeta.class.getMethod("addItemFlags", itemFlagClass);
			addItemFlagsMethod.setAccessible(true);
		}
		catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e)
		{
			// Not supported :c
		}
		catch (InvocationTargetException e)
		{
			PluginLogger.error("Exception occurred while looking for the ItemFlag API.", e);
		}
	}

	/**
	 * Hides all the item attributes of the given {@link ItemMeta}.
	 *
	 * @param meta The {@link ItemMeta} to hide attributes from.
	 */
	static public void hideItemAttributes(ItemMeta meta)
	{
		if (addItemFlagsMethod == null) return;
		try
		{
			addItemFlagsMethod.invoke(meta, itemFlagValues);
		}
		catch (IllegalAccessException | InvocationTargetException ex)
		{
			PluginLogger.error("Exception occurred while invoking the ItemMeta.addItemFlags method.", ex);
		}
	}


	/**
	 * Stores the ItemStack at the given index of a GUI's inventory. The inventory is only updated
	 * the next time the Bukkit Scheduler runs (i.e. next server tick).
	 *
	 * @param gui  The GUI to update
	 * @param slot The slot where to put the ItemStack
	 * @param item The ItemStack to set
	 */
	static public void setItemLater(Gui gui, int slot, ItemStack item)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(ImageOnMap.getPlugin(),
				new CreateDisplayItemTask(gui.getInventory(), item, slot));
	}

	/**
	 * One-liner to construct an {@link ItemStack}.
	 *
	 * @param material The stack's material.
	 *
	 * @return The constructed {@link ItemStack}.
	 */
	static public ItemStack makeItem(Material material)
	{
		return makeItem(material, null, (List<String>) null);
	}

	/**
	 * One-liner to construct an {@link ItemStack}.
	 *
	 * @param material The stack's material.
	 * @param title The stack's title.
	 *
	 * @return The constructed {@link ItemStack}.
	 */
	static public ItemStack makeItem(Material material, String title)
	{
		return makeItem(material, title, (List<String>) null);
	}

	/**
	 * One-liner to construct an {@link ItemStack}.
	 *
	 * @param material The stack's material.
	 * @param title The stack's title.
	 * @param loreLines The stack's lore lines.
	 *
	 * @return The constructed {@link ItemStack}.
	 */
	static public ItemStack makeItem(Material material, String title, String... loreLines)
	{
		return makeItem(material, title, Arrays.asList(loreLines));
	}

	/**
	 * One-liner to construct an {@link ItemStack}.
	 *
	 * @param material The stack's material.
	 * @param title The stack's title.
	 * @param loreLines The stack's lore lines.
	 *
	 * @return The constructed {@link ItemStack}.
	 */
	static public ItemStack makeItem(Material material, String title, List<String> loreLines)
	{
		return makeItem(new ItemStack(material), title, loreLines);
	}

	/**
	 * One-liner to update an {@link ItemStack}'s {@link ItemMeta}.
	 *
	 * If the stack is a map, it's attributes will be hidden.
	 *
	 * @param itemStack The original {@link ItemStack}. This stack will be directly modified.
	 * @param title The stack's title.
	 * @param loreLines A list containing the stack's lines.
	 *
	 * @return The same {@link ItemStack}, but with an updated {@link ItemMeta}.
	 */
	static public ItemStack makeItem(ItemStack itemStack, String title, List<String> loreLines)
	{
		ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(title);
		meta.setLore(loreLines);

		if (itemStack.getType().equals(Material.MAP))
			hideItemAttributes(meta);

		itemStack.setItemMeta(meta);
		return itemStack;
	}

	/**
	 * Implements a bukkit runnable that updates an inventory slot later.
	 */
	static private class CreateDisplayItemTask implements Runnable
	{
		private final Inventory inventory;
		private final ItemStack item;
		private final int slot;

		public CreateDisplayItemTask(Inventory inventory, ItemStack item, int slot)
		{
			this.inventory = inventory;
			this.item = item;
			this.slot = slot;
		}

		@Override
		public void run()
		{
			inventory.setItem(slot, item);
			for (HumanEntity player : inventory.getViewers())
			{
				((Player) player).updateInventory();
			}
		}

	}
}
