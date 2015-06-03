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

import fr.moribus.imageonmap.ImageOnMap;
import java.util.ArrayDeque;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

class WorkerCallbackManager implements Runnable
{
    static private final int WATCH_LOOP_DELAY = 5;
    
    private final HashMap<WorkerRunnable, WorkerRunnableInfo> callbacks;
    private final ArrayDeque<WorkerRunnableInfo> callbackQueue;
    
    private final String name;
    
    private BukkitTask selfTask;
    
    public WorkerCallbackManager(String name)
    {
        callbacks = new HashMap<>();
        callbackQueue = new ArrayDeque<>();
        this.name = name;
    }
    
    public void init()
    {
        selfTask = Bukkit.getScheduler().runTaskTimer(ImageOnMap.getPlugin(), this, 0, WATCH_LOOP_DELAY);
    }
    
    public void setupCallback(WorkerRunnable runnable, WorkerCallback callback)
    {
        synchronized(callbacks)
        {
            callbacks.put(runnable, new WorkerRunnableInfo(callback));
        }
    }
    
    public <T> void callback(WorkerRunnable<T> runnable, T result)
    {
        callback(runnable, result, null);
    }
    
    public <T> void callback(WorkerRunnable<T> runnable, T result, Throwable exception)
    {
        WorkerRunnableInfo<T> runnableInfo;
        synchronized(callbacks)
        {
            runnableInfo = callbacks.get(runnable);
        }
        if(runnableInfo == null) return;
        runnableInfo.setRunnableException(exception);
        runnableInfo.setResult(result);
        
        enqueueCallback(runnableInfo);
    }
    
    public void exit()
    {
        if(selfTask != null) selfTask.cancel();
    }
    
    private void enqueueCallback(WorkerRunnableInfo runnableInfo)
    {
        synchronized(callbackQueue)
        {
            callbackQueue.add(runnableInfo);
        }
    }
    
    @Override
    public void run()
    {
        WorkerRunnableInfo currentRunnableInfo;
        synchronized(callbackQueue)
        {
            if(callbackQueue.isEmpty()) return;
            currentRunnableInfo = callbackQueue.pop();
        }
        
        currentRunnableInfo.runCallback();
    }
    
    private class WorkerRunnableInfo<T>
    {
        private final WorkerCallback<T> callback;
        private T result;
        private Throwable runnableException;
        
        public WorkerRunnableInfo(WorkerCallback callback)
        {
            this.callback = callback;
            this.runnableException = null;
        }

        public WorkerCallback getCallback()
        {
            return callback;
        }
        
        public void runCallback()
        {
            if(runnableCrashed())
            {
                callback.errored(runnableException);
            }
            else
            {
                callback.finished(result);
            }
        }
        
        public void setResult(T result)
        {
            this.result = result;
        }

        public Throwable getRunnableException()
        {
            return runnableException;
        }

        public void setRunnableException(Throwable runnableException)
        {
            this.runnableException = runnableException;
        }
        
        public boolean runnableCrashed()
        {
            return this.runnableException != null;
        }
    }
}
