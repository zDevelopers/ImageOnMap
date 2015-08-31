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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialGui extends ExplorerGui<Material>
{

    @Override
    protected ItemStack getViewItem(Material data)
    {
        return new ItemStack(data);
    }
    
    @Override
    protected void onRightClick(Material data)
    {
        getPlayer().sendMessage("You clicked : " + data.toString());
    }

    @Override
    protected void onUpdate()
    {
        setTitle("All da materials");
        setData(Material.values());
    }

}
