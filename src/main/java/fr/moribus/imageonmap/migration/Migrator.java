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
import fr.moribus.imageonmap.worker.Worker;
import fr.moribus.imageonmap.worker.WorkerCallback;
import fr.moribus.imageonmap.worker.WorkerRunnable;

public class Migrator extends Worker
{
    static private Migrator instance;
    static private V3Migrator migrator;
    
    static public void startWorker()
    {
        if(instance != null) stopWorker();
        instance = new Migrator();
        instance.init();
    }
    
    static public void stopWorker()
    {
        instance.exit();
        instance = null;
    }
    
    private Migrator()
    {
        super("Migration");
    }
    
    static public boolean isMigrationStarted()
    {
        return migrator != null;
    }
    
    static public boolean isMigrationRunning()
    {
        return migrator != null && migrator.isRunning();
    }
    
    static public boolean runMigration(WorkerCallback<Void> callback)
    {
        if(isMigrationRunning()) return false;
        if(!isMigrationStarted()) migrator = new V3Migrator(ImageOnMap.getPlugin());
        instance.submitQuery(new WorkerRunnable<Void>()
        {
            @Override
            public Void run() throws Throwable
            {
                migrator.run();
                return null;
            }
        }, callback);
        return true;
    }
}
