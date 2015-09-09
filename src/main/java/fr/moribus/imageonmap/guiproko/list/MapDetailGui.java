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

import fr.moribus.imageonmap.guiproko.core.ExplorerGui;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.ui.MapItemManager;
import org.bukkit.inventory.ItemStack;

public class MapDetailGui extends ExplorerGui<Void>
{
    private final PosterMap map;
    public MapDetailGui(PosterMap map)
    {
        this.map = map;
    }
    
    @Override
    protected ItemStack getViewItem(int x, int y)
    {
        return MapItemManager.createSubMapItem(map, x, y);
    }

    @Override
    protected void onUpdate()
    {
        setTitle("Details for map " + map.getName());
        setDataShape(map.getColumnCount(), map.getRowCount());
    }

}
