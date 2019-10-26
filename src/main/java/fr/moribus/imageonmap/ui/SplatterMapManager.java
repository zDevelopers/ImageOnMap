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

import com.google.common.collect.ImmutableMap;
import fr.moribus.imageonmap.image.MapInitEvent;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.zlib.components.attributes.Attributes;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.nbt.NBTException;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.reflection.NMSException;
import fr.zcraft.zlib.tools.world.FlatLocation;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.util.UUID;

abstract public class SplatterMapManager 
{
    private SplatterMapManager() {}
    
    static public ItemStack makeSplatterMap(PosterMap map)
    {
        final ItemStack splatter = new ItemStackBuilder(Material.FILLED_MAP)
                .title(ChatColor.GOLD, map.getName()).title(ChatColor.DARK_GRAY, " - ").title(ChatColor.GRAY, I.t("Splatter Map"))
                .loreLine(ChatColor.GRAY, map.getId())
                .loreLine()
                 /// Title in a splatter map tooltip
                .loreLine(ChatColor.BLUE, I.t("Item frames needed"))
                 /// Size of a map stored in a splatter map
                .loreLine(ChatColor.GRAY, I.t("{0} × {1}", map.getColumnCount(), map.getRowCount()))
                .loreLine()
                 /// Title in a splatter map tooltip
                .loreLine(ChatColor.BLUE, I.t("How to use this?"))
                .longLore(ChatColor.GRAY + I.t("Place empty item frames on a wall, enough to host the whole map. Then, right-click on the bottom-left frame with this map."))
                .loreLine()
                .longLore(ChatColor.GRAY + I.t("Shift-click one of the placed maps to remove the whole poster in one shot."))
                .hideAttributes()
                .craftItem();

        final MapMeta meta = (MapMeta) splatter.getItemMeta();
        meta.setMapId(map.getMapIdAt(0));
        meta.setColor(Color.GREEN);
        splatter.setItemMeta(meta);

        try
        {
            Attributes.set(splatter, new Attribute());
        }
        catch (NBTException | NMSException e)
        {
            PluginLogger.error("Unable to set Splatter Map attribute on item", e);
        }

        return splatter;
    }
    
    static public boolean hasSplatterAttributes(ItemStack itemStack)
    {
        return Attribute.hasAttribute(itemStack);
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
        ImageMap map = MapManager.getMap(player.getInventory().getItemInMainHand());

        if(!(map instanceof PosterMap)) return false;
        PosterMap poster = (PosterMap) map;

        FlatLocation startLocation = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
        FlatLocation endLocation = startLocation.clone().add(poster.getColumnCount(), poster.getRowCount());
        PosterWall wall = new PosterWall();
        
        wall.loc1 = startLocation;
        wall.loc2 = endLocation;
        
        if(!wall.isValid())
        {
            player.sendMessage(I.t("{ce}There is not enough space to place this map ({0} × {1}).", poster.getColumnCount(), poster.getRowCount()));
            return false;
        }
        
        int i = 0;
        for(ItemFrame frame : wall.frames)
        {
            int id = poster.getMapIdAtReverseY(i);
            frame.setItem(new ItemStackBuilder(Material.FILLED_MAP).nbt(ImmutableMap.of("map", id)).craftItem());
            MapInitEvent.initMap(id);
            ++i;
        }
        
        return true;
    }
    
    static public PosterMap removeSplatterMap(ItemFrame startFrame)
    {
        final ImageMap map = MapManager.getMap(startFrame.getItem());
        if(!(map instanceof PosterMap)) return null;
        PosterMap poster = (PosterMap) map;
        if(!poster.hasColumnData()) return null;
        FlatLocation loc = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
        ItemFrame[] matchingFrames = PosterWall.getMatchingMapFrames(poster, loc, MapManager.getMapIdFromItemStack(startFrame.getItem()));
        if(matchingFrames == null) return null;
        
        for(ItemFrame frame : matchingFrames)
        {
            if(frame != null) frame.setItem(null);
        }
        
        return poster;
    }


    static public class Attribute extends fr.zcraft.zlib.components.attributes.Attribute
    {
        static public final UUID SPLATTER_MAP_UUID = UUID.fromString("260ae1b3-4b24-40a0-a30d-67ad84b7bdb2");

        Attribute()
        {
            setUUID(SPLATTER_MAP_UUID);
            setCustomData("splatter-map");
        }

        static boolean hasAttribute(final ItemStack item)
        {
            try
            {
                return Attributes.get(item, SPLATTER_MAP_UUID) != null;
            }
            catch (NMSException | NBTException ex)
            {
                PluginLogger.warning("Error while retrieving SplatterMap Attribute data", ex);
                return false;
            }
        }
    }
}
