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
package fr.moribus.imageonmap.gui;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.PluginConfiguration;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.map.SingleMap;
import fr.moribus.imageonmap.ui.MapItemManager;
import fr.moribus.imageonmap.ui.SplatterMapManager;
import fr.zcraft.zlib.components.gui.ExplorerGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class MapListGui extends ExplorerGui<ImageMap>
{
    @Override
    protected ItemStack getViewItem(ImageMap map)
    {
        String mapDescription;
        if (map instanceof SingleMap)
        {
            /// Displayed subtitle description of a single map on the list GUI
            mapDescription = I.t(getPlayerLocale(), "{white}Single map");
        }
        else
        {
            PosterMap poster = (PosterMap) map;
            if(poster.hasColumnData())
            {
                /// Displayed subtitle description of a poster map on the list GUI (columns × rows in english)
                mapDescription = I.t(getPlayerLocale(), "{white}Poster map ({0} × {1})", poster.getColumnCount(), poster.getRowCount());
            }
            else
            {
                /// Displayed subtitle description of a poster map without column data on the list GUI
                mapDescription = I.t(getPlayerLocale(), "{white}Poster map ({0} parts)", poster.getMapCount());
            }
        }
        ItemStackBuilder builder = new ItemStackBuilder(Material.MAP)
                /// Displayed title of a map on the list GUI
                .title(I.t(getPlayerLocale(), "{green}{bold}{0}", map.getName()))

                .lore(mapDescription)
                .loreLine()
                /// Map ID displayed in the tooltip of a map on the list GUI
                .lore(I.t(getPlayerLocale(), "{gray}Map ID: {0}", map.getId()))
                .loreLine();

        if (Permissions.GET.grantedTo(getPlayer()))
            builder.lore(I.t(getPlayerLocale(), "{gray}» {white}Left-click{gray} to get this map"));

        builder.lore(I.t(getPlayerLocale(), "{gray}» {white}Right-click{gray} for details and options"));

        return builder.item();
    }

    @Override
    protected ItemStack getEmptyViewItem()
    {
        ItemStackBuilder builder = new ItemStackBuilder(Material.BARRIER)
                .title(I.t(getPlayerLocale(), "{red}You don't have any map."));

        if (Permissions.NEW.grantedTo(getPlayer()))
            builder.longLore(I.t(getPlayerLocale(), "{gray}Get started by creating a new one using {white}/tomap <URL> [resize]{gray}!"));
        else
            builder.longLore(I.t(getPlayerLocale(), "{gray}Unfortunately, you are not allowed to create one."));

        return builder.item();
    }

    @Override
    protected void onRightClick(ImageMap data)
    {
        Gui.open(getPlayer(), new MapDetailGui(data), this);
    }

    @Override
    protected ItemStack getPickedUpItem(ImageMap map)
    {
        if (!Permissions.GET.grantedTo(getPlayer()))
            return null;

        if (map instanceof SingleMap)
        {
            return MapItemManager.createMapItem(map.getMapsIDs()[0], map.getName());
        }
        else if (map instanceof PosterMap)
        {
            PosterMap poster = (PosterMap) map;
            
            if(poster.hasColumnData())
                return SplatterMapManager.makeSplatterMap((PosterMap) map);
            
            MapItemManager.giveParts(getPlayer(), poster);
            return null;
        }

        MapItemManager.give(getPlayer(), map);
        return null;
    }

    @Override
    protected void onUpdate()
    {
        ImageMap[] maps = MapManager.getMaps(getPlayer().getUniqueId());
        setData(maps);

        /// The maps list GUI title
        setTitle(I.t(getPlayerLocale(), "{black}Your maps {reset}({0})", maps.length));

        setKeepHorizontalScrollingSpace(true);


        /* ** Statistics ** */

        int imagesCount = MapManager.getMapList(getPlayer().getUniqueId()).size();
        int mapPartCount = MapManager.getMapPartCount(getPlayer().getUniqueId());

        int mapGlobalLimit = PluginConfiguration.MAP_GLOBAL_LIMIT.get();
        int mapPersonalLimit = PluginConfiguration.MAP_PLAYER_LIMIT.get();

        int mapPartGloballyLeft = mapGlobalLimit - MapManager.getMapCount();
        int mapPartPersonallyLeft = mapPersonalLimit - mapPartCount;

        int mapPartLeft;
        if (mapGlobalLimit <= 0 && mapPersonalLimit <= 0)
            mapPartLeft = -1;
        else if (mapGlobalLimit <= 0)
            mapPartLeft = mapPartPersonallyLeft;
        else if (mapPersonalLimit <= 0)
            mapPartLeft = mapPartGloballyLeft;
        else
            mapPartLeft = Math.min(mapPartGloballyLeft, mapPartPersonallyLeft);

        double percentageUsed = mapPartLeft < 0 ? 0 : ((double) mapPartCount) / ((double) (mapPartCount + mapPartLeft)) * 100;

        ItemStackBuilder statistics = new ItemStackBuilder(Material.ENCHANTED_BOOK)
                .title(I.t(getPlayerLocale(), "{blue}Usage statistics"))
                .loreLine()
                .lore(I.tn(getPlayerLocale(), "{white}{0}{gray} image rendered", "{white}{0}{gray} images rendered", imagesCount))
                .lore(I.tn(getPlayerLocale(), "{white}{0}{gray} Minecraft map used", "{white}{0}{gray} Minecraft maps used", mapPartCount));

        if(mapPartLeft >= 0)
        {
            statistics
                    .lore("", I.t(getPlayerLocale(), "{blue}Minecraft maps limits"), "")
                    .lore(mapGlobalLimit == 0
                            ? I.t(getPlayerLocale(), "{gray}Server-wide limit: {white}unlimited")
                            : I.t(getPlayerLocale(), "{gray}Server-wide limit: {white}{0}", mapGlobalLimit))
                    .lore(mapPersonalLimit == 0
                            ? I.t(getPlayerLocale(), "{gray}Per-player limit: {white}unlimited")
                            : I.t(getPlayerLocale(), "{gray}Per-player limit: {white}{0}", mapPersonalLimit))
                    .loreLine()
                    .lore(I.t(getPlayerLocale(), "{white}{0} %{gray} of your quota used", (int) Math.rint(percentageUsed)))
                    .lore(I.tn(getPlayerLocale(), "{white}{0}{gray} map left", "{white}{0}{gray} maps left", mapPartLeft));
        }

        statistics.hideAttributes();

        action("", getSize() - 5, statistics);
    }
}
