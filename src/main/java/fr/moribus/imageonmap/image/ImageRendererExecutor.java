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

package fr.moribus.imageonmap.image;

import fr.moribus.imageonmap.worker.Worker;
import fr.moribus.imageonmap.worker.WorkerCallback;
import fr.moribus.imageonmap.worker.WorkerRunnable;

public class ImageRendererExecutor extends Worker
{
    static private ImageRendererExecutor instance;
    
    static public void start()
    {
        if(instance != null) stop();
        instance = new ImageRendererExecutor();
        instance.init();
    }
    
    static public void stop()
    {
        instance.exit();
        instance = null;
    }
    
    private ImageRendererExecutor()
    {
        super("Image IO");
    }
    
    static public void Test(WorkerCallback callback)
    {
        instance.submitQuery(new WorkerRunnable()
        {
            @Override
            public void run() throws Throwable
            {
                Thread.sleep(5000);
            }
        }, callback);
    }
    
}
