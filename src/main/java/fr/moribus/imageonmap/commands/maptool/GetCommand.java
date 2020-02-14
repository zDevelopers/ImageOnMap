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

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;

@CommandInfo (name = "get")
public class GetCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
        Player player = playerSender();
        if(getMapFromArgs().give(player))
        {
            info(I.t("The requested map was too big to fit in your inventory."));
            info(I.t("Use '/maptool getremaining' to get the remaining maps."));
        }
    }
    
    @Override
    protected List<String> complete() throws CommandException
    {
        if(args.length == 1) 
            return getMatchingMapNames(playerSender(), args[0]);
        return null;
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.GET.grantedTo(sender);
    }
}
