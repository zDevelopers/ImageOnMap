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
import fr.moribus.imageonmap.PluginConfiguration;
import fr.moribus.imageonmap.image.ImageIOExecutor;
import fr.moribus.imageonmap.image.PosterImage;
import fr.moribus.imageonmap.map.MapManagerException.Reason;
import fr.zcraft.zlib.tools.PluginLogger;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

abstract public class MapManager
{
    static private final long SAVE_DELAY = 200;
    static private final ArrayList<PlayerMapStore> playerMaps = new ArrayList<PlayerMapStore>();
    static private BukkitTask autosaveTask;
    
    static public void init()
    {
        load();
    }
    
    static public void exit()
    {
        save();
        playerMaps.clear();
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

    static public boolean managesMap(ItemStack item)
    {
        if(item == null) return false;
        if(item.getType() != Material.MAP) return false;
        
        synchronized(playerMaps)
        {
            for(PlayerMapStore mapStore : playerMaps)
            {
                if(mapStore.managesMap(item)) return true;
            }
        }
        return false;
    }

    static public ImageMap createMap(UUID playerUUID, short mapID) throws MapManagerException
    {
        ImageMap newMap = new SingleMap(playerUUID, mapID);
        addMap(newMap);
        return newMap;
    }
    
    static public ImageMap createMap(PosterImage image, UUID playerUUID, short[] mapsIDs) throws MapManagerException
    {
        ImageMap newMap;
        if(image.getImagesCount() == 1)
        {
            newMap = new SingleMap(playerUUID, mapsIDs[0]);
        }
        else
        {
            newMap = new PosterMap(playerUUID, mapsIDs, image.getColumns(), image.getLines());
        }
        addMap(newMap);
        return newMap;
    }
    
    static public short[] getNewMapsIds(int amount)
    {
        short[] mapsIds = new short[amount];
        for(int i = 0; i < amount; i++)
        {
            mapsIds[i] = Bukkit.createMap(Bukkit.getWorlds().get(0)).getId();
        }
        return mapsIds;
    }
    
    static public void addMap(ImageMap map) throws MapManagerException
    {
        getPlayerMapStore(map.getUserUUID()).addMap(map);
    }
    
    static public void insertMap(ImageMap map)
    {
        getPlayerMapStore(map.getUserUUID()).insertMap(map);
    }
    
    static public void deleteMap(ImageMap map) throws MapManagerException
    {
        getPlayerMapStore(map.getUserUUID()).deleteMap(map);
        ImageIOExecutor.deleteImage(map);
    }
    
    static public void notifyModification(UUID playerUUID)
    {
        getPlayerMapStore(playerUUID).notifyModification();
        if(autosaveTask == null) 
            Bukkit.getScheduler().runTaskLater(ImageOnMap.getPlugin(), new AutosaveRunnable(), SAVE_DELAY);
    }
    
    static public String getNextAvailableMapID(String mapId, UUID playerUUID)
    {
        return getPlayerMapStore(playerUUID).getNextAvailableMapID(mapId);
    }
    
    static public List<ImageMap> getMapList(UUID playerUUID)
    {
        return getPlayerMapStore(playerUUID).getMapList();
    }
    
    static public ImageMap[] getMaps(UUID playerUUID)
    {
        return getPlayerMapStore(playerUUID).getMaps();
    }

    /**
     * Returns the number of minecraft maps used by the images rendered by the given player.
     *
     * @param playerUUID The player's UUID.
     *
     * @return The count.
     */
    static public int getMapPartCount(UUID playerUUID)
    {
        return getPlayerMapStore(playerUUID).getMapCount();
    }
    
    static public ImageMap getMap(UUID playerUUID, String mapId)
    {
        return getPlayerMapStore(playerUUID).getMap(mapId);
    }

    /**
     * Returns the {@link ImageMap} this map belongs to.
     *
     * @param mapId The ID of the Minecraft map.
     * @return The {@link ImageMap}.
     */
    static public ImageMap getMap(short mapId)
    {
        synchronized(playerMaps)
        {
            for(PlayerMapStore mapStore : playerMaps)
            {
                if(mapStore.managesMap(mapId))
                {
                    for(ImageMap map : mapStore.getMapList())
                    {
                        if(map.managesMap(mapId))
                        {
                            return map;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the {@link ImageMap} this map belongs to.
     *
     * @param item The map, as an {@link ItemStack}.
     * @return The {@link ImageMap}.
     */
    static public ImageMap getMap(ItemStack item)
    {
        if(item == null) return null;
        if(item.getType() != Material.MAP) return null;
        return getMap(item.getDurability());
    }
    
    static public void clear(Inventory inventory)
    {
        for(int i = 0, c = inventory.getSize(); i < c; i++)
        {
            if(managesMap(inventory.getItem(i)))
            {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }
    
    static public void clear(Inventory inventory, ImageMap map)
    {
        for(int i = 0, c = inventory.getSize(); i < c; i++)
        {
            if(map.managesMap(inventory.getItem(i)))
            {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }
    
    static private UUID getUUIDFromFile(File file)
    {
        String fileName = file.getName();
        int fileExtPos = fileName.lastIndexOf('.');
        if(fileExtPos <= 0) return null;
        
        String fileExt = fileName.substring(fileExtPos + 1);
        if(!fileExt.equals("yml")) return null;
        
        try
        {
            return UUID.fromString(fileName.substring(0, fileExtPos));
        }
        catch(IllegalArgumentException ex)
        {
            return null;
        }
    }
    
    static public void load()
    {
        int loadedFilesCount = 0;
        for(File file : ImageOnMap.getPlugin().getMapsDirectory().listFiles())
        {
            UUID uuid = getUUIDFromFile(file);
            if(uuid == null) continue;
            getPlayerMapStore(uuid);
            ++loadedFilesCount;
        }
        
        PluginLogger.info("Loaded {0} player map files.", loadedFilesCount);
    }
    
    static public void save()
    {
        synchronized(playerMaps)
        {
            for(PlayerMapStore tStore : playerMaps)
            {
                tStore.save();
            }
        }
    }
    
    static public void checkMapLimit(ImageMap map) throws MapManagerException
    {
        checkMapLimit(map.getMapCount(), map.getUserUUID());
    }
    
    static public void checkMapLimit(int newMapsCount, UUID userUUID) throws MapManagerException
    {
        int limit = PluginConfiguration.MAP_GLOBAL_LIMIT.getInteger();
        if(limit > 0)
        {
            if(getMapCount() + newMapsCount > limit)
                throw new MapManagerException(Reason.MAXIMUM_SERVER_MAPS_EXCEEDED);
        }
        getPlayerMapStore(userUUID).checkMapLimit(newMapsCount);
    }

    /**
     * Returns the total number of minecraft maps used by ImageOnMap images.
     *
     * @return The count.
     */
    static public int getMapCount()
    {
        int mapCount = 0;
        synchronized(playerMaps)
        {
            for(PlayerMapStore tStore : playerMaps)
            {
                mapCount += tStore.getMapCount();
            }
        }
        return mapCount;
    }
    
    static private PlayerMapStore getPlayerMapStore(UUID playerUUID)
    {
        PlayerMapStore store;
        synchronized(playerMaps)
        {
            store = getExistingPlayerMapStore(playerUUID);
            if(store == null)
            {
                store = new PlayerMapStore(playerUUID);

                playerMaps.add(store);
                store.load();
            }
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
                    if(toolStore.isModified()) toolStore.save();
                }
                autosaveTask = null;
            }
        }
        
    }
}
