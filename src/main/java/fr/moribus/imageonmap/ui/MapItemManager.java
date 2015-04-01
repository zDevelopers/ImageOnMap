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
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MapItemManager implements Listener
{
    static public void give(Inventory inventory, ImageMap map)
    {
        if(map instanceof PosterMap) give(inventory, (PosterMap) map);
        else if(map instanceof SingleMap) give(inventory, (SingleMap) map);
    }
    
    static public void give(Inventory inventory, SingleMap map)
    {
        inventory.addItem(createMapItem(map.getMapsIDs()[0], map.getName()));
    }
    
    static public void give(Inventory inventory, PosterMap map)
    {
        short[] mapsIDs = map.getMapsIDs();
        for(int i = 0, c = mapsIDs.length; i < c; i++)
        {
            inventory.addItem(createMapItem(mapsIDs[i], map.getName() + 
                    " (row " + map.getRowAt(i) + 
                    ", column " + map.getColumnAt(i) + ")"));
        }
    }
    
    static public ItemStack createMapItem(short mapID, String text)
    {
        ItemStack itemMap = new ItemStack(Material.MAP, 1, mapID);
        
        ItemMeta meta = itemMap.getItemMeta();
        meta.setDisplayName(text);
        itemMap.setItemMeta(meta);
        
        return itemMap;
    }
}
