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

package fr.moribus.imageonmap.worker;

import fr.moribus.imageonmap.PluginLogger;
import java.util.ArrayDeque;

public abstract class Worker
{
    private final String name;
    private final ArrayDeque<WorkerRunnable> runQueue = new ArrayDeque<>();
    
    private final WorkerCallbackManager callbackManager;
    private Thread thread;
    
    protected Worker(String name)
    {
        this.name = name;
        this.callbackManager = new WorkerCallbackManager(name);
    }
    
    public void init()
    {
        if(thread != null && thread.isAlive())
        {
            PluginLogger.LogWarning("Restarting " + name + " thread.");
            exit();
        }
        callbackManager.init();
        thread = createThread();
        thread.start();
    }
    
    public void exit()
    {
        thread.interrupt();
        callbackManager.exit();
        thread = null;
    }
    
    private void run()
    {
        WorkerRunnable currentRunnable;
        
        while(!Thread.interrupted())
        {
            synchronized(runQueue)
            {
                try
                {
                    while(runQueue.isEmpty()) runQueue.wait();
                }
                catch(InterruptedException ex)
                {
                    break;
                }
                currentRunnable = runQueue.pop();
            }
            
            try
            {
                currentRunnable.run();
                callbackManager.callback(currentRunnable);
            }
            catch(Throwable ex)
            {
                callbackManager.callback(currentRunnable, ex);
            }
        }
    }
    
    protected void submitQuery(WorkerRunnable runnable)
    {
        synchronized(runQueue)
        {
            runQueue.add(runnable);
            runQueue.notify();
        }
    }
    
    protected void submitQuery(WorkerRunnable runnable, WorkerCallback callback, Object... args)
    {
        callbackManager.setupCallback(runnable, callback, args);
        submitQuery(runnable);
    }
    
    
    private Thread createThread()
    {
        return new Thread()
        {
            @Override
            public void run()
            {
                Worker.this.run();
            }
        };
    }
    
    
}
