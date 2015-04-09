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

package fr.moribus.imageonmap.commands.migration;

import fr.moribus.imageonmap.commands.*;
import fr.moribus.imageonmap.migration.Migrator;
import fr.moribus.imageonmap.worker.WorkerCallback;
import org.bukkit.command.CommandSender;

@CommandInfo(name = "start")
public class StartCommand extends Command
{
    public StartCommand(Commands commandGroup) {
        super(commandGroup);
    }
    
    @Override
    protected void run() throws CommandException
    {
        final CommandSender cmdSender = sender;
        if(Migrator.isMigrationRunning())
        {
            error("A migration process is already running. Check console for details.");
        }
        else
        {
            Migrator.runMigration(new WorkerCallback<Void>()
            {
                @Override
                public void finished(Void result)
                {
                    info(cmdSender, "Migration finished. See console for details.");
                }

                @Override
                public void errored(Throwable exception)
                {
                    warning(cmdSender, "Migration ended unexpectedly. See console for details.");
                }
            });
            
            info("Migration started. See console for details.");
        }
    }
}
