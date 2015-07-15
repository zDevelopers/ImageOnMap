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

import fr.moribus.imageonmap.guiproko.core.ActionGui;
import org.bukkit.Material;

public class ConfirmDeleteMapGui extends ActionGui
{
    @Override
    protected void onUpdate()
    {
        setTitle("Are you sure ?");
        setSize(2);
        
        action("toto", 1, Material.DIAMOND, "Toto");
        action("tata", 2, Material.EMERALD, "Tata");
    }
    
    protected void action_toto()
    {
        getPlayer().sendMessage("toto");
    }
    
    protected void action_tata()
    {
        getPlayer().sendMessage("tata");
    }
}
