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
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.*;


public class MapDetailGui extends ExplorerGui<Void>
{
    private final ImageMap map;

    public MapDetailGui(ImageMap map)
    {
        this.map = map;
    }

    @Override
    protected ItemStack getViewItem(int x, int y)
    {
        Material partMaterial = Material.PAPER;
        if((y % 2 == 0 && x % 2 == 0) || (y % 2 == 1 && x % 2 == 1))
            partMaterial = Material.EMPTY_MAP;
        
        ItemStack part = new ItemStack(partMaterial);
        ItemMeta meta = part.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "Map part");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Column: " + ChatColor.WHITE + (y + 1),
                ChatColor.GRAY + "Row: " + ChatColor.WHITE + (x + 1),
                "",
                ChatColor.GRAY + "» Click to get only this part"
        ));

        part.setItemMeta(meta);
        return part;
    }

    @Override
    protected ItemStack getEmptyViewItem()
    {
        if(map instanceof SingleMap)
        {
            return getViewItem(0, 0);
        }
        else return super.getEmptyViewItem();
    }
    
    @GuiAction
    private void rename()
    {
        PromptGui.prompt(getPlayer(), new Callback<String>()
        {
            @Override
            public void call(String newName)
            {
                if(newName == null || newName.isEmpty())
                {
                    getPlayer().sendMessage(ChatColor.RED + "Map names can't be empty.");
                    return;
                }
                map.rename(newName);
                getPlayer().sendMessage(ChatColor.GRAY + "Map successfuly renamed.");
            }
        }, map.getName());
    }
    
    @GuiAction
    private void delete()
    {
        Gui.open(getPlayer(), new ConfirmDeleteMapGui(map, getCurrentPageX()));
    }

    @Override
    protected void onUpdate()
    {
        ItemStack back, rename, delete;
        
        setTitle("Your maps » " + ChatColor.BLACK + map.getName());
        setKeepHorizontalScrollingSpace(true);

        if(map instanceof PosterMap)
            setDataShape(((PosterMap) map).getColumnCount(), ((PosterMap) map).getRowCount());
        else
            setData(null); // Fallback to the empty view item.

        back = GuiUtils.makeItem(Material.EMERALD, 
                ChatColor.GREEN + "« Back", 
                
                ChatColor.GRAY + "Go back to the list.");
        
        rename = GuiUtils.makeItem(Material.BOOK_AND_QUILL, 
                ChatColor.BLUE + "Rename", 
                
                ChatColor.GRAY + "Renames this image");
        
        delete = GuiUtils.makeItem(Material.BARRIER, 
                ChatColor.RED + "Delete", 
                
                ChatColor.GRAY + "Deletes this map " + ChatColor.WHITE + "forever" + ChatColor.GRAY + ".",
                ChatColor.GRAY + "This action cannot be undone!");

        action("rename", getSize() - 7, rename);
        action("delete", getSize() - 6, delete);


        // To keep the controls centered, the back button is shifted to the right when the
        // arrow isn't displayed, so when the map fit on the grid without sliders.
        int backSlot = getSize() - 4;

        if(map instanceof PosterMap && ((PosterMap) map).getColumnCount() <= INVENTORY_ROW_SIZE)
            backSlot++;

        action("back", backSlot, back);
    }
}
