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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

class WorkerMainThreadExecutor implements Runnable
{
    static private final int WATCH_LOOP_DELAY = 1;
    
    private final String name;
    private final ArrayDeque<WorkerFuture> mainThreadQueue = new ArrayDeque<>();
    private BukkitTask mainThreadTask;
    
    public WorkerMainThreadExecutor(String name)
    {
        this.name = name;
    }
    
    public void init()
    {
        mainThreadTask = Bukkit.getScheduler().runTaskTimer(ImageOnMap.getPlugin(), this, 0, WATCH_LOOP_DELAY);
    }
    
    public void exit()
    {
        mainThreadTask.cancel();
        mainThreadTask = null;
    }
    
    public <T> Future<T> submit(Callable<T> callable)
    {
        WorkerFuture<T> future = new WorkerFuture<T>(callable);
        synchronized(mainThreadQueue)
        {
            mainThreadQueue.add(future);
        }
        return future;
    }

    @Override
    public void run()
    {
        WorkerFuture currentFuture;
        synchronized(mainThreadQueue)
        {
            if(mainThreadQueue.isEmpty()) return;
            currentFuture = mainThreadQueue.pop();
        }
        
        currentFuture.runCallable();
    }
    
    private class WorkerFuture<T> implements Future<T>
    {
        private final Callable<T> callable;
        private boolean isCancelled;
        private boolean isDone;
        private Exception executionException;
        private T value;
        
        public WorkerFuture(Callable<T> callable)
        {
            this.callable = callable;
        }
        
        public void runCallable()
        {
            try
            {
                value = callable.call();
            }
            catch(Exception ex)
            {
                executionException = ex;
            }
            finally
            {
                isDone = true;
                synchronized(this){this.notifyAll();}
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
        {
            if(this.isCancelled || this.isDone) return false;
            this.isCancelled = true;
            this.isDone = true;
            return true;
        }

        @Override
        public boolean isCancelled()
        {
            return this.isCancelled;
        }

        @Override
        public boolean isDone()
        {
            return this.isDone;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException
        {
            waitForCompletion();
            if(executionException != null) throw new ExecutionException(executionException);
            return value;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
        {
            waitForCompletion(timeout, unit);
            if(executionException != null) throw new ExecutionException(executionException);
            return value;
        }
        
        private void waitForCompletion(long timeout) throws InterruptedException, TimeoutException
        {
            synchronized(this)
            {
                long remainingTime;
                long timeoutTime = System.currentTimeMillis() + timeout;
                while(!isDone) 
                {
                    remainingTime = timeoutTime - System.currentTimeMillis();
                    if(remainingTime <= 0) throw new TimeoutException();
                    this.wait(remainingTime);
                }
            }
        }
        
        private void waitForCompletion() throws InterruptedException
        {
            synchronized(this)
            {
                while(!isDone) this.wait();
            }
        }
        
        private void waitForCompletion(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException
        {
            long millis = 0;
            switch(unit)
            {
                case NANOSECONDS:
                    millis = timeout / 10^6;
                    break;
                case MICROSECONDS:
                    millis = timeout / 10^3;
                    break;
                case MILLISECONDS: 
                    millis = timeout;
                    break;
                case SECONDS:
                    millis = timeout * 10^3;
                    break;
                case MINUTES: 
                    millis = timeout * 10^3 * 60;
                    break;
                case HOURS: 
                    millis = timeout * 10^3 * 3600;
                    break;
                case DAYS:
                    millis = timeout * 10^3 * 3600 * 24;
            }
            waitForCompletion(millis);
        }
        
        
    }
}
