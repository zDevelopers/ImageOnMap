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

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import org.bukkit.entity.Player;

import java.util.List;

@CommandInfo (name = "list")
public class ListCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
        Player player = playerSender();
        List<ImageMap> mapList = MapManager.getMapList(player.getUniqueId());
        
        if(mapList.isEmpty())
        {
            info("No map found.");
            return;
        }
        
        info(mapList.size() + " maps found.");
        
        String sMapList = mapList.get(0).getId();
        for(int i = 1, c = mapList.size(); i < c; i++)
        {
            sMapList += "§7,§r" + mapList.get(i).getId();
        }
        player.sendMessage(sMapList);
    }
}
