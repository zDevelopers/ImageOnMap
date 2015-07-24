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

import fr.moribus.imageonmap.ImageOnMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Various utility methods for GUIs.
 */
abstract public class GuiUtils 
{
    
    /**
     * Stores the ItemStack at the given index of a GUI's inventory.
     * The inventory is only updated the next time the Bukkit Scheduler runs (i.e. next server tick).
     * 
     * @param gui The GUI to update
     * @param slot The slot where to put the ItemStack
     * @param item The ItemStack to set
     */
    static public void setItemLater(Gui gui, int slot, ItemStack item)
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(ImageOnMap.getPlugin(), 
                new CreateDisplayItemTask(gui.getInventory(), item, slot));
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
            for(HumanEntity player : inventory.getViewers())
            {
                ((Player)player).updateInventory();
            }
        }
        
    }
}
