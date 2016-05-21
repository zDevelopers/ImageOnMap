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
import fr.moribus.imageonmap.migration.MigratorExecutor;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.command.CommandSender;

@CommandInfo (name = "migrate")
public class MigrateCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
        if(MigratorExecutor.isRunning())
        {
            error(I.t("A migration process is already running. Check console for details."));
        }
        else
        {
            info(I.t("Migration started. See console for details."));
            MigratorExecutor.migrate();
        }
    }
    
    @Override
    public boolean canExecute(CommandSender sender)
    {
        return sender.isOp();
    }
}
