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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public abstract class Worker
{
    private final String name;
    private final ArrayDeque<WorkerRunnable> runQueue = new ArrayDeque<>();
    
    private final WorkerCallbackManager callbackManager;
    private final WorkerMainThreadExecutor mainThreadExecutor;
    private Thread thread;
    
    protected Worker(String name)
    {
        this(name, false);
    }
    
    protected Worker(String name, boolean runMainThreadExecutor)
    {
        this.name = name;
        this.callbackManager = new WorkerCallbackManager(name);
        this.mainThreadExecutor = runMainThreadExecutor ? new WorkerMainThreadExecutor(name) : null;
    }
    
    protected void init()
    {
        if(thread != null && thread.isAlive())
        {
            PluginLogger.LogWarning("Restarting " + name + " thread.");
            exit();
        }
        callbackManager.init();
        if(mainThreadExecutor != null) mainThreadExecutor.init();
        thread = createThread();
        thread.start();
    }
    
    protected void exit()
    {
        thread.interrupt();
        callbackManager.exit();
        if(mainThreadExecutor != null) mainThreadExecutor.exit();
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
                callbackManager.callback(currentRunnable, currentRunnable.run());
            }
            catch(Throwable ex)
            {
                callbackManager.callback(currentRunnable, null, ex);
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
    
    protected void submitQuery(WorkerRunnable runnable, WorkerCallback callback)
    {
        callbackManager.setupCallback(runnable, callback);
        submitQuery(runnable);
    }
    
    protected <T> Future<T> submitToMainThread(Callable<T> callable)
    {
        if(mainThreadExecutor != null) return mainThreadExecutor.submit(callable);
        return null;
    }
    
    private Thread createThread()
    {
        return new Thread("ImageOnMap " + name)
        {
            @Override
            public void run()
            {
                Worker.this.run();
            }
        };
    }
    
    
}
