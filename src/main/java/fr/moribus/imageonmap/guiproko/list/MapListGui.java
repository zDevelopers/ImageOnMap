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

package fr.moribus.imageonmap.guiproko.list;

import fr.moribus.imageonmap.*;
import fr.moribus.imageonmap.guiproko.core.*;
import fr.moribus.imageonmap.map.*;
import fr.moribus.imageonmap.ui.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.text.*;
import java.util.*;


public class MapListGui extends ExplorerGui<ImageMap>
{
    private final NumberFormat bigNumbersFormatter = new DecimalFormat("###,###,###,###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));


    @Override
    protected ItemStack getViewItem(ImageMap map)
    {
        ItemStack icon = new ItemStack(Material.MAP);


        ItemMeta meta = icon.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + map.getName());

        String mapDescription;
        if(map instanceof SingleMap)
            mapDescription = "Single map";
        else
            mapDescription = "Poster map (" + ((PosterMap) map).getColumnCount() + "×" + ((PosterMap) map).getRowCount() + ")";

        meta.setLore(Arrays.asList(
                ChatColor.WHITE + mapDescription,
                "",
                ChatColor.GRAY + "Map ID: " + map.getId(),
                "",
                ChatColor.GRAY + "» Left-click to get this map",
                ChatColor.GRAY + "» Right-click for details and options"
        ));

        GuiUtils.hideItemAttributes(meta);

        icon.setItemMeta(meta);


        return icon;
    }

    @Override
    protected ItemStack getEmptyViewItem()
    {
        ItemStack empty = new ItemStack(Material.BARRIER);
        ItemMeta meta = empty.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "You don't have any map.");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Get started by creating a new one",
                ChatColor.GRAY + "using " + ChatColor.WHITE + "/tomap <URL> [resize]" + ChatColor.GRAY + "!"
        ));

        empty.setItemMeta(meta);
        return empty;
    }

    @Override
    protected void onRightClick(ImageMap data)
    {
        Gui.open(getPlayer(), new MapDetailGui(data));
    }
    
    @Override
    protected ItemStack getPickedUpItem(ImageMap map)
    {
        if(map instanceof SingleMap)
        {
            return MapItemManager.createMapItem(map.getMapsIDs()[0], map.getName());
        }
        
        MapItemManager.give(getPlayer(), map);
        return null;
    }

    @Override
    protected void onUpdate()
    {
        ImageMap[] maps = MapManager.getMaps(getPlayer().getUniqueId());
        setData(maps);
        setTitle(ChatColor.BLACK + "Your maps " + ChatColor.RESET + "(" + maps.length + ")");

        setKeepHorizontalScrollingSpace(true);


        /* ** Statistics ** */

        int imagesCount = MapManager.getMapList(getPlayer().getUniqueId()).size();
        int mapPartCount = MapManager.getMapPartCount(getPlayer().getUniqueId());

        int mapGlobalLimit = PluginConfiguration.MAP_GLOBAL_LIMIT.getInteger();
        int mapPersonalLimit = PluginConfiguration.MAP_PLAYER_LIMIT.getInteger();

        int mapPartGloballyLeft = mapGlobalLimit - MapManager.getMapCount();
        int mapPartPersonallyLeft = mapPersonalLimit - mapPartCount;

        int mapPartLeft;
        if(mapGlobalLimit <= 0 && mapPersonalLimit <= 0)
            mapPartLeft = -1;
        else if(mapGlobalLimit <= 0)
            mapPartLeft = mapPartPersonallyLeft;
        else if(mapPersonalLimit <= 0)
            mapPartLeft = mapPartGloballyLeft;
        else
            mapPartLeft = Math.min(mapPartGloballyLeft, mapPartPersonallyLeft);

        double percentageUsed = mapPartLeft < 0 ? 0 : ((double) mapPartCount) / ((double) (mapPartCount + mapPartLeft)) * 100;


        ItemStack statistics = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = statistics.getItemMeta();

        meta.setDisplayName(ChatColor.BLUE + "Usage statistics");
        meta.setLore(Arrays.asList(
                "",
                getStatisticText("Images rendered", imagesCount),
                getStatisticText("Minecraft maps used", mapPartCount)
        ));

        if(mapPartLeft >= 0)
        {
            List<String> lore = meta.getLore();

            lore.add("");
            lore.add(ChatColor.BLUE + "Minecraft maps limits");

            lore.add("");
            lore.add(getStatisticText("Server-wide limit", mapGlobalLimit, true));
            lore.add(getStatisticText("Per-player limit", mapPersonalLimit, true));

            lore.add("");
            lore.add(getStatisticText("Current consumption", ((int) Math.rint(percentageUsed)) + " %"));
            lore.add(getStatisticText("Maps left", mapPartLeft));

            meta.setLore(lore);
        }

        GuiUtils.hideItemAttributes(meta);

        statistics.setItemMeta(meta);
        action("", getSize() - 5, statistics);
    }

    private String getStatisticText(String title, Integer value)
    {
        return getStatisticText(title, value, false);
    }

    private String getStatisticText(String title, Integer value, boolean zeroIsUnlimited)
    {
        return getStatisticText(title, zeroIsUnlimited && value <= 0 ? "unlimited" : bigNumbersFormatter.format(value));
    }

    private String getStatisticText(String title, String value)
    {
        return ChatColor.GRAY + title + ": " + ChatColor.WHITE + value;
    }
}
