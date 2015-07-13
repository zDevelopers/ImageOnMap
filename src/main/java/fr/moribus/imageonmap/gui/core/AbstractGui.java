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

	/**
	 * This method is called when the inventory is open through the
	 * {@link GuiManager}. Use this to populate the GUI.
	 *
	 * You will have to store the inventory inside the {@link AbstractGui#inventory} protected attribute,
	 * as this attribute will be used by all other methods.
	 *
	 * @param player The player this GUI will be displayed to.
	 */
	public abstract void display(Player player);

	/**
	 * A standard way to update the GUI when it is not closed. This method is
	 * never called automatically, you have to call it when needed.
	 *
	 * @param player The player this GUI is displayed to.
	 */
	public void update(Player player)
	{

	}

	/**
	 * Call this to open the GUI. The GUI is not automatically open, allowing you
	 * to open it at the best moment (before or after populating it, as example).
	 *
	 * @param player The player this GUI will be displayed to.
	 */
	public void open(Player player)
	{
		player.openInventory(inventory);
	}

	/**
	 * This method will be called when the GUI is closed.
	 *
	 * @param player The player this GUI was displayed to.
	 */
	public void onClose(Player player)
	{

	}

	/**
	 * This method will be called when the player clicks on the GUI.
	 *
	 * @param player The player who clicked on the GUI.
	 * @param stack The clicked {@link ItemStack}.
	 * @param action The action associated with this slot using {@link AbstractGui#setSlotData(ItemStack, int, String)}
	 *                  or other similar methods.
	 * @param clickType The click.
	 * @param invAction The inventory action.
	 * @param event The full {@link InventoryClickEvent} triggered by the player.
	 */
	public void onClick(Player player, ItemStack stack, String action, ClickType clickType, InventoryAction invAction, InventoryClickEvent event)
	{
		this.onClick(player, stack, action, clickType, invAction);
	}

	/**
	 * This method will be called when the player clicks on the GUI.
	 *
	 * @param player The player who clicked on the GUI.
	 * @param stack The clicked {@link ItemStack}.
	 * @param action The action associated with this slot using {@link AbstractGui#setSlotData(ItemStack, int, String)}
	 *                  or other similar methods.
	 * @param clickType The click.
	 * @param invAction The inventory action.
	 */
	public void onClick(Player player, ItemStack stack, String action, ClickType clickType, InventoryAction invAction)
	{
		this.onClick(player, stack, action, clickType);
	}

	/**
	 * This method will be called when the player clicks on the GUI.
	 *
	 * @param player The player who clicked on the GUI.
	 * @param stack The clicked {@link ItemStack}.
	 * @param action The action associated with this slot using {@link AbstractGui#setSlotData(ItemStack, int, String)}
	 *                  or other similar methods.
	 * @param clickType The click.
	 */
	public void onClick(Player player, ItemStack stack, String action, ClickType clickType)
	{
		this.onClick(player, stack, action);
	}

	/**
	 * This method will be called when the player clicks on the GUI.
	 *
	 * @param player The player who clicked on the GUI.
	 * @param stack The clicked {@link ItemStack}.
	 * @param action The action associated with this slot using {@link AbstractGui#setSlotData(ItemStack, int, String)}
	 *                  or other similar methods.
	 */
	public void onClick(Player player, ItemStack stack, String action) {}


	/**
	 * This method will be called when the user places an item in the GUI, either by shift-click or directly.
	 *
	 * Use the {@link InventoryAction} to distinguish these cases.
	 *
	 * @param player The played who moved the stack to the GUI.
	 * @param stack The moved {@link ItemStack}.
	 * @param clickType The click.
	 * @param invAction The inventory action.
	 * @param event The full {@link InventoryClickEvent} triggered by the player.
	 */
	public void onItemDeposit(Player player, ItemStack stack, ClickType clickType, InventoryAction invAction, InventoryClickEvent event)
	{
		onItemDeposit(player, stack, clickType, invAction);
	}

	/**
	 * This method will be called when the user places an item in the GUI, either by shift-click or directly.
	 *
	 * Use the {@link InventoryAction} to distinguish these cases.
	 *
	 * @param player The played who moved the stack to the GUI.
	 * @param stack The moved {@link ItemStack}.
	 * @param clickType The click.
	 * @param invAction The inventory action.
	 */
	public void onItemDeposit(Player player, ItemStack stack, ClickType clickType, InventoryAction invAction)
	{
		onItemDeposit(player, stack, clickType);
	}

	/**
	 * This method will be called when the user places an item in the GUI, either by shift-click or directly.
	 *
	 * Use the {@link InventoryAction} to distinguish these cases.
	 *
	 * @param player The played who moved the stack to the GUI.
	 * @param stack The moved {@link ItemStack}.
	 * @param clickType The click.
	 */
	public void onItemDeposit(Player player, ItemStack stack, ClickType clickType)
	{
		onItemDeposit(player, stack);
	}

	/**
	 * This method will be called when the user places an item in the GUI, either by shift-click or directly.
	 *
	 * Use the {@link InventoryAction} to distinguish these cases.
	 *
	 * @param player The played who moved the stack to the GUI.
	 * @param stack The moved {@link ItemStack}.
	 */
	public void onItemDeposit(Player player, ItemStack stack)
	{

	}

	/**
	 * Registers a slot as a managed one.
	 *
	 * When such a slot is clicked by the player, the {@link #onClick(Player, ItemStack, String, ClickType, InventoryAction, InventoryClickEvent)}
	 * method is called (and all methods with the same name and less arguments).
	 *
	 * @param inv The inventory the item will be added to.
	 * @param name The display name of the item.
	 * @param material The material of this item.
	 * @param slot The slot this item will be added to.
	 * @param description The description (lore) of the item (one line per {@link String} in the array).
	 * @param action The action associated with this slot, retrieved with the {@link #onClick(Player, ItemStack, String)] methods.
	 */
	public void setSlotData(Inventory inv, String name, Material material, int slot, String[] description, String action)
	{
		this.setSlotData(inv, name, new ItemStack(material, 1), slot, description, action);
	}

	/**
	 * Registers a slot as a managed one, using the default inventory.
	 *
	 * When such a slot is clicked by the player, the {@link #onClick(Player, ItemStack, String, ClickType, InventoryAction, InventoryClickEvent)}
	 * method is called (and all methods with the same name and less arguments).
	 *
	 * @param name The display name of the item.
	 * @param material The material of this item.
	 * @param slot The slot this item will be added to.
	 * @param description The description (lore) of the item (one line per {@link String} in the array).
	 * @param action The action associated with this slot, retrieved with the {@link #onClick(Player, ItemStack, String)] methods.
	 */
	public void setSlotData(String name, Material material, int slot, String[] description, String action)
	{
		this.setSlotData(this.inventory, name, new ItemStack(material, 1), slot, description, action);
	}

	/**
	 * Registers a slot as a managed one, using the default inventory.
	 *
	 * When such a slot is clicked by the player, the {@link #onClick(Player, ItemStack, String, ClickType, InventoryAction, InventoryClickEvent)}
	 * method is called (and all methods with the same name and less arguments).
	 *
	 * @param name The display name of the item.
	 * @param item The {@link ItemStack} displayed in this slot.
	 * @param slot The slot this item will be added to.
	 * @param description The description (lore) of the item (one line per {@link String} in the array).
	 * @param action The action associated with this slot, retrieved with the {@link #onClick(Player, ItemStack, String)] methods.
	 */
	public void setSlotData(String name, ItemStack item, int slot, String[] description, String action)
	{
		this.setSlotData(this.inventory, name, item, slot, description, action);
	}

	/**
	 * Registers a slot as a managed one.
	 *
	 * When such a slot is clicked by the player, the {@link #onClick(Player, ItemStack, String, ClickType, InventoryAction, InventoryClickEvent)}
	 * method is called (and all methods with the same name and less arguments).
	 *
	 * @param inv The inventory the item will be added to.
	 * @param name The display name of the item.
	 * @param item The {@link ItemStack} displayed in this slot.
	 * @param slot The slot this item will be added to.
	 * @param description The description (lore) of the item (one line per {@link String} in the array).
	 * @param action The action associated with this slot, retrieved with the {@link #onClick(Player, ItemStack, String)] methods.
	 */
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

	/**
	 * Registers a slot as a managed one.
	 *
	 * When such a slot is clicked by the player, the {@link #onClick(Player, ItemStack, String, ClickType, InventoryAction, InventoryClickEvent)}
	 * method is called (and all methods with the same name and less arguments).
	 *
	 * @param inv The inventory the item will be added to.
	 * @param item The {@link ItemStack} displayed in this slot.
	 * @param slot The slot this item will be added to.
	 * @param action The action associated with this slot, retrieved with the {@link #onClick(Player, ItemStack, String)] methods.
	 */
	public void setSlotData(Inventory inv, ItemStack item, int slot, String action)
	{
		this.actions.put(slot, action);
		inv.setItem(slot, item);
	}

	/**
	 * Registers a slot as a managed one, using the default inventory.
	 *
	 * When such a slot is clicked by the player, the {@link #onClick(Player, ItemStack, String, ClickType, InventoryAction, InventoryClickEvent)}
	 * method is called (and all methods with the same name and less arguments).
	 *
	 * @param item The {@link ItemStack} displayed in this slot.
	 * @param slot The slot this item will be added to.
	 * @param action The action associated with this slot, retrieved with the {@link #onClick(Player, ItemStack, String)] methods.
	 */
	public void setSlotData(ItemStack item, int slot, String action)
	{
		setSlotData(this.inventory, item, slot, action);
	}

	/**
	 * Returns the registered action associated with the given slot.
	 *
	 * @param slot The slot.
	 *
	 * @return The action; {@code null} if there isn't any action registered to this slot.
	 */
	public String getAction(int slot)
	{
		if (!this.actions.containsKey(slot))
			return null;

		return this.actions.get(slot);
	}

	/**
	 * Returns the slot registered to the given action.
	 *
	 * @param action The action.
	 *
	 * @return The slot; {@code -1} if this action is not registered.
	 */
	public int getSlot(String action)
	{
		for (int slot : this.actions.keySet())
			if (this.actions.get(slot).equals(action))
				return slot;

		return -1;
	}

	/**
	 * Returns the default inventory.
	 *
	 * @return The inventory.
	 */
	public Inventory getInventory()
	{
		return this.inventory;
	}
}
