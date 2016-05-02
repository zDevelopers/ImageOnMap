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
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.zlib.tools.items.GlowEffect;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.world.FlatLocation;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

abstract public class SplatterMapManager 
{
    private SplatterMapManager() {}
    
    static public ItemStack makeSplatterMap(PosterMap map)
    {
        return new ItemStackBuilder(Material.MAP)
                .data(map.getMapIdAt(0))
                .title(ChatColor.GOLD, map.getName())
                .loreLine(ChatColor.GOLD, "Splatter Map")
                .glow()
                .hideAttributes()
                .item();
    }
    
    static public boolean hasSplatterAttributes(ItemStack itemStack)
    {
        return GlowEffect.hasGlow(itemStack);
    }
    
    static public boolean isSplatterMap(ItemStack itemStack)
    {
        return hasSplatterAttributes(itemStack) && MapManager.managesMap(itemStack);
    }
    
    static public boolean hasSplatterMap(Player player, PosterMap map)
    {
        Inventory playerInventory = player.getInventory();
        
        for(int i = 0; i < playerInventory.getSize(); ++i)
        {
            ItemStack item = playerInventory.getItem(i);
            if(isSplatterMap(item) && map.managesMap(item))
                return true;
        }
        
        return false;
    }
    
    static public boolean placeSplatterMap(ItemFrame startFrame, Player player)
    {
        ImageMap map = MapManager.getMap(player.getItemInHand());
        if(map == null || !(map instanceof PosterMap)) return false;
        PosterMap poster = (PosterMap) map;
        
        FlatLocation startLocation = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
        FlatLocation endLocation = startLocation.clone().add(poster.getColumnCount(), poster.getRowCount());
        PosterWall wall = new PosterWall();
        
        wall.loc1 = startLocation;
        wall.loc2 = endLocation;
        
        if(!wall.isValid())
        {
            player.sendMessage(ChatColor.RED + "There is not enough space to place this map (" + poster.getColumnCount() + "x" + poster.getRowCount() + ")");
            return false;
        }
        
        int i = 0;
        for(ItemFrame frame : wall.frames)
        {
            frame.setItem(new ItemStack(Material.MAP, 1, poster.getMapIdAtReverseY(i)));
            ++i;
        }
        
        return true;
    }
    
    static public PosterMap removeSplatterMap(ItemFrame startFrame)
    {
        ImageMap map = MapManager.getMap(startFrame.getItem());
        if(map == null || !(map instanceof PosterMap)) return null;
        PosterMap poster = (PosterMap) map;
        FlatLocation loc = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
        ItemFrame[] matchingFrames = PosterWall.getMatchingMapFrames(poster, loc, startFrame.getItem().getDurability());
        if(matchingFrames == null) return null;
        
        for(ItemFrame frame : matchingFrames)
        {
            if(frame != null) frame.setItem(null);
        }
        
        return poster;
    }
}
