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

import static fr.moribus.imageonmap.guiproko.core.Gui.getOpenGui;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;

/**
 * This class provides the basic needs for chest-type GUIs.
 * It allows you to create custom GUIs by simply providing an inventory
 * to fill, as well as rerouting basic events to it.
 */
abstract public class InventoryGui extends Gui
{
    static protected final int INVENTORY_ROW_SIZE = 9;
    static protected final int MAX_INVENTORY_COLUMN_SIZE = 6;
    static protected final int MAX_INVENTORY_SIZE = INVENTORY_ROW_SIZE * MAX_INVENTORY_COLUMN_SIZE;
    static protected final int MAX_TITLE_LENGTH = 32;
    
    public InventoryGui()
    {
        registerListener(GuiListener.class);
    }
    
    /**
     * The size of the inventory.
     */
    private int size = 0;
    
    /**
     * The title of the inventory.
     */
    private String title;
    
    /**
     * The current Bukkit inventory.
     */
    private Inventory inventory;

    /* ===== Public API ===== */
    
    /**
     * Asks the GUI to update its data, and refresh its view accordingly.
     * The inventory may be regenerated when calling this method.
     */
    @Override
    public void update()
    {
        super.update();
        Player player = getPlayer();
        
        //If inventory does not need to be regenerated
        if(inventory != null && inventory.getTitle().equals(title) && inventory.getSize() == size)
        {
            refresh();
        }
        else
        {
            inventory = Bukkit.createInventory(player, size, title);
            populate(inventory);

            if(isOpen()) // Reopening the inventory
            {
                player.closeInventory();
                player.openInventory(inventory);
            }
        }
    }
    
    /**
     * Asks the GUI to recreate its view.
     * The inventory is cleared, but never regenerated when calling this method.
     */
    public void refresh()
    {
        inventory.clear();
        populate(inventory);
    }
    
    /* ===== Protected API ===== */
    
    @Override
    protected void open(Player player)
    {
        super.open(player);
        player.openInventory(inventory);
    }
    
    /**
     * Closes this inventory.
     */
    @Override
    public void close()
    {
        super.close();
        getPlayer().closeInventory();
    }
    
    /**
     * Called when the inventory needs to be (re)populated.
     *
     * @param inventory The inventory to populate
     */
    abstract protected void populate(Inventory inventory);
    
    /**
     * Raised when an action is performed on an item in the inventory.
     *
     * @param event The click event data.
     */
    abstract protected void onClick(InventoryClickEvent event);


    /**
     * Raised when an drag is performed on the inventory.
     * The default behaviour is to cancel any event that affects the GUI.
     *
     * @param event The drag event data.
     */
    protected void onDrag(InventoryDragEvent event)
    {
        if(affectsGui(event)) event.setCancelled(true);
    }
    
    /**
     * Returns if the given event affects the GUI's inventory.
     *
     * @param event The event to test
     * @return {@code true} if the event's slot is in the GUI's inventory,
     *         {@code false} otherwise.
     */
    static protected boolean affectsGui(InventoryClickEvent event)
    {
        return event.getRawSlot() < event.getInventory().getSize();
    }

    /**
     * Returns if the given event affects the GUI's inventory.
     *
     * @param event The event to test
     * @return true if any of the event's slots is in the GUI's inventory, 
     *         false otherwise.
     */
    static protected boolean affectsGui(InventoryDragEvent event)
    {
        for(int slot : event.getRawSlots())
        {
            if(slot < event.getInventory().getSize())
            {
                return true;
            }
        }
        return false;
    }

    /* ===== Getters & Setters ===== */
    
    /** @return The size of the inventory. */
    protected int getSize() { return size; }
    
    /**
     * Sets the new size of the inventory.
     * The given value is raised to be a multiple of the size of an inventory's
     * row, and is capped to the maximal size of an inventory.
     * It will be applied on the next GUI update.
     * @param size The new size of the inventory.
     */
    protected void setSize(int size)
    {
        this.size = Math.min(((int)(Math.ceil((double) size / INVENTORY_ROW_SIZE))) * INVENTORY_ROW_SIZE, MAX_INVENTORY_SIZE);
    }
    
    /** @return The title of the inventory. */
    protected String getTitle() { return title; }

    /**
     * Sets the new title of the inventory.
     * It will be applied on the next GUI update.
     * @param title The new title of the inventory
     */
    protected void setTitle(String title)
    {
        if(title != null && title.length() > MAX_TITLE_LENGTH)
        {
            title = title.substring(0, MAX_TITLE_LENGTH - 4) + "...";
        }
        this.title = title;
    }
    
    /** @return The underlying inventory, or null if the Gui has not been opened yet. */
    public Inventory getInventory() { return inventory; }
    
    /**
     * Implements a Bukkit listener for all GUI-related events.
     */
    static private class GuiListener implements Listener
    {
        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event)
        {
            InventoryGui openGui = getOpenGui(event.getWhoClicked(), InventoryGui.class);
            if(openGui == null) return;
            
            openGui.onDrag(event);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event)
        {
            InventoryGui openGui = getOpenGui(event.getWhoClicked(), InventoryGui.class);
            if(openGui == null) return;
            
            openGui.onClick(event);
        }
        
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event)
        {
            Gui openGui = getOpenGui(event.getPlayer());
            if(openGui == null) return;
            
            if(!openGui.isOpen());
                openGui.setClosed();
        }
    }
    
}
