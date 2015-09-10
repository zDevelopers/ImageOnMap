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

import fr.moribus.imageonmap.guiproko.core.*;
import fr.moribus.imageonmap.map.*;
import fr.moribus.imageonmap.ui.*;
import org.bukkit.*;
import org.bukkit.inventory.*;

public class MapListGui extends ExplorerGui<ImageMap>
{

    @Override
    protected ItemStack getViewItem(ImageMap data)
    {
        if(data instanceof SingleMap)
        {
            return GuiUtils.makeItem(Material.EMPTY_MAP, data.getName(), "Single map", "#" + data.getId());
        }
        PosterMap map = (PosterMap) data;
        return GuiUtils.makeItem(Material.MAP, data.getName(), 
                "Poster map ("+map.getColumnCount()+"x"+map.getRowCount()+")", "#" + data.getId());
    }
    
    @Override
    protected void onRightClick(ImageMap data)
    {
        if(data instanceof SingleMap) return;
        Gui.open(getPlayer(), new MapDetailGui((PosterMap)data));
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
        setTitle("Your maps (" + maps.length + " total)");

        setKeepHorizontalScrollingSpace(true);
    }
}
