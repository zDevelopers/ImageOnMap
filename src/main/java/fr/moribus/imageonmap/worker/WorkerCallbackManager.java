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

import java.util.ArrayDeque;
import java.util.HashMap;
import org.bukkit.Bukkit;

class WorkerCallbackManager implements Runnable
{
    static private final int WATCH_LOOP_DELAY = 5;
    
    private final HashMap<WorkerRunnable, WorkerCallback> callbacks;
    private final ArrayDeque<WorkerCallback> callbackQueue;
    
    private final String name;
    
    public WorkerCallbackManager(String name)
    {
        callbacks = new HashMap<>();
        callbackQueue = new ArrayDeque<>();
        this.name = name;
    }
    
    public void init()
    {
        //Bukkit.getScheduler().runTaskTimer(null, this, 0, WATCH_LOOP_DELAY);
    }
    
    public void exit()
    {
        
    }
    
    @Override
    public void run()
    {
    }
}
