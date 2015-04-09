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

package fr.moribus.imageonmap.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public abstract class CommandPermission 
{
    abstract public boolean hasPermission(CommandSender sender);
    
    static public final CommandPermission OP_ONLY = new CommandPermission()
    {
        @Override
        public boolean hasPermission(CommandSender sender)
        {
            return sender.isOp();
        }
    };
    
    static public CommandPermission bukkitPermission(final String permission)
    {
        return new CommandPermission()
        {
            @Override
            public boolean hasPermission(CommandSender sender)
            {
                return sender.hasPermission(permission);
            }
        };
    }
    
    static public CommandPermission bukkitPermission(Plugin plugin, String permission)
    {
        final String permissionName = plugin.getName().toLowerCase()
                + "." + permission.toLowerCase();
        return bukkitPermission(permissionName);
    }
}
