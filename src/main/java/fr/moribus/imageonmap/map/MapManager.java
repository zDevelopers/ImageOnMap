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

package fr.moribus.imageonmap.map;

import fr.moribus.imageonmap.ImageOnMap;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

abstract public class MapManager 
{
    static private final long SAVE_DELAY = 200;
    static private final ArrayList<PlayerMapStore> playerMaps = new ArrayList<PlayerMapStore>();
    static private BukkitTask autosaveTask;
    
    static public void init()
    {
        
    }
    
    static public void exit()
    {
        playerMaps.clear();
        save();
        if(autosaveTask != null) autosaveTask.cancel();
    }
    
    static public boolean managesMap(short mapID)
    {
        synchronized(playerMaps)
        {
            for(PlayerMapStore mapStore : playerMaps)
            {
                if(mapStore.managesMap(mapID)) return true;
            }
        }
        return false;
    }
    
    static public void notifyModification(UUID playerUUID)
    {
        getPlayerMapStore(playerUUID).notifyModification();
        if(autosaveTask == null) 
            Bukkit.getScheduler().runTaskLater(ImageOnMap.getPlugin(), new AutosaveRunnable(), SAVE_DELAY);
    }
    
    static public void save()
    {
        synchronized(playerMaps)
        {
            for(PlayerMapStore tStore : playerMaps)
            {
                tStore.saveMapsFile();
            }
        }
    }
    
    static private PlayerMapStore getPlayerMapStore(UUID playerUUID)
    {
        PlayerMapStore store = getExistingPlayerMapStore(playerUUID);
        if(store == null)
        {
            store = new PlayerMapStore(playerUUID);
            synchronized(playerMaps){playerMaps.add(store);}
        }
        return store;
    }
    
    static private PlayerMapStore getExistingPlayerMapStore(UUID playerUUID)
    {
        synchronized(playerMaps)
        {
            for(PlayerMapStore mapStore : playerMaps)
            {
                if(mapStore.getUUID().equals(playerUUID)) return mapStore;
            }
        }
        return null;
    }
    
    static private class AutosaveRunnable implements Runnable
    {
        @Override
        public void run() 
        {
            synchronized(playerMaps)
            {
                for(PlayerMapStore toolStore : playerMaps)
                {
                    if(toolStore.isModified()) toolStore.saveMapsFile();
                }
                autosaveTask = null;
            }
        }
        
    }
}
