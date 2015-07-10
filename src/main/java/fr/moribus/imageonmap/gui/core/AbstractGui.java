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
import org.bukkit.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.*;


/**
 * @author IamBlueSlime, Amaury Carrade
 */
public abstract class AbstractGui {
	protected TreeMap<Integer, String> actions = new TreeMap<>();
	protected Inventory inventory;

	public abstract void display(Player player);

	public void update(Player player) {}

	public void onClose(Player player) {}

	public void onClick(Player player, ItemStack stack, String action, ClickType clickType, InventoryClickEvent event)
	{
		this.onClick(player, stack, action, clickType);
	}

	public void onClick(Player player, ItemStack stack, String action, ClickType clickType)
	{
		this.onClick(player, stack, action);
	}

	public void onClick(Player player, ItemStack stack, String action) {}


	public void onItemDeposit(Player player, ItemStack stack, ClickType clickType, InventoryClickEvent event)
	{
		onItemDeposit(player, stack, clickType);
	}

	public void onItemDeposit(Player player, ItemStack stack, ClickType clickType)
	{
		onItemDeposit(player, stack);
	}

	public void onItemDeposit(Player player, ItemStack stack) {}


	public void setSlotData(Inventory inv, String name, Material material, int slot, String[] description, String action)
	{
		this.setSlotData(inv, name, new ItemStack(material, 1), slot, description, action);
	}

	public void setSlotData(String name, Material material, int slot, String[] description, String action)
	{
		this.setSlotData(this.inventory, name, new ItemStack(material, 1), slot, description, action);
	}

	public void setSlotData(String name, ItemStack item, int slot, String[] description, String action)
	{
		this.setSlotData(this.inventory, name, item, slot, description, action);
	}

	public void setSlotData(Inventory inv, String name, ItemStack item, int slot, String[] description, String action)
	{
		this.actions.put(slot, action);
		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(name);

		if (description != null)
			meta.setLore(Arrays.asList(description));

		item.setItemMeta(meta);
		inv.setItem(slot, item);
	}

	public void setSlotData(Inventory inv, ItemStack item, int slot, String action)
	{
		this.actions.put(slot, action);
		inv.setItem(slot, item);
	}

	public void setSlotData(ItemStack item, int slot, String action)
	{
		setSlotData(this.inventory, item, slot, action);
	}

	public String getAction(int slot)
	{
		if (!this.actions.containsKey(slot))
			return null;

		return this.actions.get(slot);
	}

	public int getSlot(String action)
	{
		for (int slot : this.actions.keySet())
			if (this.actions.get(slot).equals(action))
				return slot;

		return 0;
	}

	public Inventory getInventory()
	{
		return this.inventory;
	}
}
