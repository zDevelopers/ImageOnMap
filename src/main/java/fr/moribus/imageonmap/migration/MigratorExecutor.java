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

package fr.moribus.imageonmap.migration;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.PluginLogger;

public class MigratorExecutor
{
    static private Thread migratorThread;
    
    static public void migrate()
    {
        if(isRunning())
        {
            PluginLogger.error("Migration is already running.");
            return;
        }
        migratorThread = new Thread(new V3Migrator(ImageOnMap.getPlugin()), "ImageOnMap-Migration");
        migratorThread.start();
    }
    
    static public boolean isRunning()
    {
        return migratorThread != null && migratorThread.isAlive();
    }
    
    static public void waitForMigration()
    {
        if(isRunning())
        {
            PluginLogger.info("Waiting for migration to finish ...");
            try
            {
                migratorThread.join();
            }
            catch(InterruptedException ex)
            {
                PluginLogger.error("Migration thread has been interrupted while wating to finish. It may not have ended correctly.");
            }
        }
    }
}
