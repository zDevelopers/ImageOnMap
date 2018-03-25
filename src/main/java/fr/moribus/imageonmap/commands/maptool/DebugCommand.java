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

import fr.moribus.imageonmap.PluginConfiguration;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import org.bukkit.entity.Player;

@CommandInfo (name =  "debug")
public class DebugCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
    	Player p = playerSender();
    	p.sendMessage("Limit Map Size X" + PluginConfiguration.LIMIT_SIZE_X.get().toString());
    	p.sendMessage("Limit Map Size Y" + PluginConfiguration.LIMIT_SIZE_Y.get().toString());
    	p.sendMessage("Global Map Limit" + PluginConfiguration.MAP_GLOBAL_LIMIT.get().toString());
    	p.sendMessage("Player Map Limit" + PluginConfiguration.MAP_PLAYER_LIMIT.get().toString());
    	p.sendMessage("Collect Data" + PluginConfiguration.COLLECT_DATA.get().toString());
    	p.sendMessage("Save Full Image" + PluginConfiguration.SAVE_FULL_IMAGE.get().toString());
    }
}
