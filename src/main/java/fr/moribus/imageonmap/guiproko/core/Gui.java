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

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

/**
 * This class provides the basic needs for chest-type GUIs.
 * It allows you to create custom GUIs by simply providing an inventory
 * to fill, as well as rerouting basic events to it.
 */
abstract public class Gui 
{
    static protected final int INVENTORY_ROW_SIZE = 9;
    static protected final int MAX_INVENTORY_SIZE = INVENTORY_ROW_SIZE * 6;
    
    /**
     * The player this Gui instance is associated to.
     */
    private HumanEntity player;
    
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
    
    /**
     * If the inventory is currently open.
     */
    private boolean open = false;
    
    private void open(HumanEntity player)
    {
        this.player = player;
        openGuis.put(player, this);
        update();
        player.openInventory(inventory);
        this.open = true;
    }
    
    /* ===== Public API ===== */
    
    /**
     * Asks the GUI to update its data, and recreate its view accordingly.
     * The inventory is regenerated when calling this method.
     */
    public void update()
    {
        onUpdate();
        
        //If inventory does not need to be regenerated
        if(inventory != null && inventory.getTitle().equals(title) && inventory.getSize() == size)
        {
            inventory.clear();
            populate(inventory);
        }
        else
        {
            inventory = Bukkit.createInventory(player, size, title);
            populate(inventory);
            if(isOpen())//Reopening the inventory
            {
                player.closeInventory();
                player.openInventory(inventory);
            }
        }
    }
    
    /**
     * Closes this inventory.
     */
    public void close()
    {
        this.open = false;
        player.closeInventory();
        openGuis.remove(player);
    }
    
    /* ===== Protected API ===== */
    
    /**
     * Raised when the {@link Gui#update() } method is called.
     * Use this method to update your internal data.
     */
    protected void onUpdate(){};
    
    /**
     * Called when the inventory needs to be (re)populated.
     * @param inventory The inventory to populate
     */
    abstract protected void populate(Inventory inventory);
    
    /**
     * Raised when an action is performed on an item in the inventory.
     * @param event The click event data.
     */
    abstract protected void onClick(InventoryClickEvent event);
    
    /**
     * Raised when the GUI is being closed.
     * Use this method to cleanup data.
     */
    protected void onClose(){};
    
    /* ===== Getters & Setters ===== */
    
    /** @return If the GUI is currently open or not.*/
    public boolean isOpen(){return open;}
    
    /** @return The player this Gui instance is associated to.*/
    protected HumanEntity getPlayer(){return player;}
    
    /** @return The size of the inventory.*/
    protected int getSize(){return size;}
    
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
    
    /** @return The title of the inventory.*/
    protected String getTitle(){return title;}
    /**
     * Sets the new title of the inventory.
     * It will be applied on the next GUI update.
     * @param title The new title of the inventory
     */
    protected void setTitle(String title){this.title = title;}
    
    /* ===== Static API ===== */
    
    /**
     * A map of all the currently open GUIs, associated to the HumanEntity
     * that requested it.
     */
    static private HashMap<HumanEntity, Gui> openGuis = null;
    
    /**
     * The Bukkit listener for all GUI-related events.
     */
    static private GuiListener listener = null;
    
    /**
     * Initializes the GUI listeners.
     * This method must be called on plugin enabling.
     * @param plugin The plugin the GUI listeners will be registered on
     */
    static public void init(Plugin plugin)
    {
        openGuis = new HashMap<>();
        listener = new GuiListener();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
    
    /**
     * Cleans up the GUI states.
     * This method must be called on plugin disabling.
     */
    static public void exit()
    {
        openGuis.clear();
        openGuis = null;
        listener = null;
    }
    
    /**
     * Opens a GUI for a player.
     * @param <T> A GUI type.
     * @param owner The player the GUI will be shown to.
     * @param gui The GUI.
     * @return The opened GUI.
     */
    static public <T extends Gui> T open(HumanEntity owner, T gui)
    {
        close(owner);
        ((Gui)gui).open(owner);/* JAVA GENERICS Y U NO WORK */
        return gui;
    }
    
    /**
     * Closes any open GUI for a given player.
     * @param owner The player.
     */
    static public void close(HumanEntity owner)
    {
        Gui openGui = openGuis.get(owner);
        if(openGui == null) return;
        
        openGui.close();
    }
    
    /**
     * Implements a Bukkit listener for all GUI-related events.
     */
    static private class GuiListener implements Listener
    {
        @EventHandler
	public void onInventoryClick(InventoryClickEvent event)
        {
            HumanEntity owner = event.getWhoClicked();
            Gui openGui = openGuis.get(owner);
            if(openGui == null) return;
            
            openGui.onClick(event);
        }
        
        @EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
        {
            HumanEntity owner = event.getPlayer();
            Gui openGui = openGuis.get(owner);
            if(openGui == null) return;
            if(!openGui.isOpen()) return;
            
            openGui.onClose();
            openGuis.remove(owner);
        }
    }
}
