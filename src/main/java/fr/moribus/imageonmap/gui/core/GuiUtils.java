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

import fr.moribus.imageonmap.PluginLogger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class GuiUtils {

	private static boolean supported = true;

	private static Object[] itemFlags;

	static
	{
		try {
			Class<?> itemFlagEnumClass = Class.forName("org.bukkit.inventory.ItemFlag");

			Method valuesMethod = itemFlagEnumClass.getDeclaredMethod("values");
			itemFlags = (Object[]) valuesMethod.invoke(null);

		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
			// Not supported :c
			supported = false;
		} catch (InvocationTargetException e) {
			PluginLogger.error("Exception occurred while loading the ItemFlags.", e);
		}
	}

	/**
	 * Removes the vanilla informations displayed on the tooltips of the given item,
	 * like enchantments, map infos, etc.
	 *
	 * @param stack The item.
	 */
	public static void removeVanillaInfos(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		removeVanillaInfos(meta);
		stack.setItemMeta(meta);
	}

	/**
	 * Removes the vanilla informations displayed on the tooltips of the given item,
	 * like enchantments, map infos, etc.
	 *
	 * @param meta The item's metadata.
	 */
	public static void removeVanillaInfos(ItemMeta meta)
	{
		if(!supported) return;

		try {
			Method addItemFlagsMethod = meta.getClass().getMethod("addItemFlags", itemFlags.getClass());
			addItemFlagsMethod.setAccessible(true);
			addItemFlagsMethod.invoke(meta, (Object) itemFlags);

		} catch (NoSuchMethodException | IllegalAccessException ignored) {
			// Should never happens, or only with breaking changes in the Bukkit API.
		} catch (InvocationTargetException e) {
			PluginLogger.error("Exception occurred while invoking the ItemMeta.addItemFlags method.", e);
		}
	}
}
