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

import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.*;


public class GuiUtils {

	private static boolean supported = true;

	private static Object[] itemFlags;

	static
	{
		try {
			Class<?> itemFlagEnumClass = Class.forName("org.bukkit.inventory.ItemFlag");

			Method valuesMethod = itemFlagEnumClass.getDeclaredMethod("values");
			itemFlags = (Object[]) valuesMethod.invoke(null);

		} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			// Not supported :c
			supported = false;
		}
	}

	public static void removeVanillaInfos(ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		removeVanillaInfos(meta);
		stack.setItemMeta(meta);

	}

	public static void removeVanillaInfos(ItemMeta meta)
	{
		if(!supported) return;

		try {
			Method addItemFlagsMethod = meta.getClass().getMethod("addItemFlags", itemFlags.getClass());
			addItemFlagsMethod.setAccessible(true);
			addItemFlagsMethod.invoke(meta, (Object) itemFlags);

		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
			// Should never happens, or only with breaking changes in the Bukkit API.
		}
	}
}
