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

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.map.SingleMap;
import fr.zcraft.zlib.components.gui.GuiUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

public class MapItemManager implements Listener
{
    static private HashMap<UUID, Queue<ItemStack>> mapItemCache;
    
    static public void init()
    {
        mapItemCache = new HashMap();
    }
    
    static public void exit()
    {
        if(mapItemCache != null) mapItemCache.clear();
        mapItemCache = null;
    }
    
    static public boolean give(Player player, ImageMap map)
    {
        if(map instanceof PosterMap) return give(player, (PosterMap) map);
        else if(map instanceof SingleMap) return give(player, (SingleMap) map);
        return false;
    }
    
    static public boolean give(Player player, SingleMap map)
    {
        return give(player, createMapItem(map));
    }
    
    static public boolean give(Player player, PosterMap map)
    {
        short[] mapsIDs = map.getMapsIDs();
        boolean inventoryFull = false;
        
        String mapName;
        for(int i = 0, c = mapsIDs.length; i < c; i++)
        {
            if(map.hasColumnData())
            {
                mapName = map.getName() +
                    " (row " + map.getRowAt(i) + 
                    ", column " + map.getColumnAt(i) + ")";
            }
            else
            {
                mapName = map.getName();
            }
            inventoryFull = give(player, createMapItem(mapsIDs[i], mapName)) || inventoryFull;
        }
        
        return inventoryFull;
    }
    
    static public int giveCache(Player player)
    {
        Queue<ItemStack> cache = getCache(player);
        Inventory inventory = player.getInventory();
        int givenItemsCount = 0;
        
        while(inventory.firstEmpty() >= 0 && !cache.isEmpty())
        {
            inventory.addItem(cache.poll());
            givenItemsCount++;
        }
        
        return givenItemsCount;
    }
    
    static private boolean give(Player player, ItemStack item)
    {
        if(player.getInventory().firstEmpty() <= -1)
        {
            getCache(player).add(item);
            return true;
        }
        else
        {
            player.getInventory().addItem(item);
            return false;
        }
    }
    
    static public ItemStack createMapItem(SingleMap map)
    {
        return createMapItem(map.getMapsIDs()[0], map.getName());
    }
    
    static public ItemStack createMapItem(PosterMap map, int x, int y)
    {
        String mapName;
        if(map.hasColumnData())
        {
            mapName = map.getName() +
                " (row " + x + 
                ", column " + y + ")";
        }
        else
        {
            mapName = map.getName();
        }
        return createMapItem(map.getMapIdAt(x, y), mapName);
    }
    
    static public ItemStack createMapItem(short mapID, String text)
    {
        ItemStack itemMap = new ItemStack(Material.MAP, 1, mapID);
        
        ItemMeta meta = itemMap.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + text);
        GuiUtils.hideItemAttributes(meta);
        itemMap.setItemMeta(meta);
        
        return itemMap;
    }

    /**
     * Returns the item to place to display the (col;row) part of the given poster.
     *
     * @param col The column to display. Starts at 0.
     * @param row The row to display. Starts at 0.
     *
     * @return The map.
     *
     * @throws ArrayIndexOutOfBoundsException If col;row is not inside the map.
     */
    static public ItemStack createSubMapItem(ImageMap map, int col, int row)
    {
        if(map instanceof PosterMap && ((PosterMap) map).hasColumnData())
        {
            return MapItemManager.createMapItem(
                    ((PosterMap) map).getMapIdAt(row, col),
                    map.getName() +
                            " (row " + (row + 1) +
                            ", column " + (col + 1) + ")"
            );
        }
        else
        {
            if(row != 0 || col != 0)
            {
                throw new ArrayIndexOutOfBoundsException(); // Coherence
            }

            return MapItemManager.createMapItem(map.getMapsIDs()[0], map.getName());
        }
    }
    
    static public int getCacheSize(Player player)
    {
        return getCache(player).size();
    }
    
    static private Queue<ItemStack> getCache(Player player)
    {
        Queue<ItemStack> cache = mapItemCache.get(player.getUniqueId());
        if(cache == null)
        {
            cache = new ArrayDeque<>();
            mapItemCache.put(player.getUniqueId(), cache);
        }
        return cache;
    }
}
