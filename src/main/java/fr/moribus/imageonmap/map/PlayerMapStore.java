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
import fr.moribus.imageonmap.PluginLogger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class PlayerMapStore implements ConfigurationSerializable
{
    private final UUID playerUUID;
    private final ArrayList<ImageMap> mapList = new ArrayList<ImageMap>();
    private boolean modified = false;
    
    public PlayerMapStore(UUID playerUUID)
    {
        this.playerUUID = playerUUID;
        loadMapsFile();
    }
    
    public boolean managesMap(short mapID)
    {
        for(ImageMap map : mapList)
        {
            if(map.managesMap(mapID)) return true;
        }
        return false;
    }
    
    public void addMap(ImageMap map)
    {
        mapList.add(map);
        notifyModification();
    }
    
    /* ===== Getters & Setters ===== */
    
    public UUID getUUID()
    {
        return playerUUID;
    }
    
    public boolean isModified()
    {
        return modified;
    }
    
    public void notifyModification()
    {
        this.modified = true;
    }
    
    /* ****** Serializing ***** */
    
    @Override
    public Map<String, Object> serialize() 
    {
        Map<String, Object> map = new HashMap<String, Object>();
        ArrayList<Map> list = new ArrayList<Map>();
        synchronized(mapList)
        {
            for(ImageMap tMap : mapList)
            {
                list.add(tMap.serialize());
            }
        }
        map.put("mapList", list);
        return map;
    }
    
    private void loadFromConfig(ConfigurationSection section)
    {
        if(section == null) return;
        List<Map<String, Object>> list = (List<Map<String, Object>>) section.getList("mapList");
        if(list == null) return;
        synchronized(mapList)
        {
            for(Map<String, Object> tMap : list)
            {
                try
                {
                    mapList.add(ImageMap.fromConfig(tMap, playerUUID));
                }
                catch(InvalidConfigurationException ex)
                {
                    PluginLogger.LogWarning("Could not load map data : " + ex.getMessage());
                }
            }
        }
    }
    
    /* ****** Configuration Files management ***** */
    
    private FileConfiguration mapConfig = null;
    private File mapsFile = null;
    
    private FileConfiguration getToolConfig()
    {
        if(mapConfig == null) loadMapsFile();
        
        return mapConfig;
    }
    
    private void loadMapsFile()
    {
        if(mapsFile == null)
        {
            mapsFile = new File(ImageOnMap.getPlugin().getMapsDirectory(), playerUUID.toString() + ".yml");
            if(!mapsFile.exists()) saveMapsFile();
        }
        mapConfig = YamlConfiguration.loadConfiguration(mapsFile);
        loadFromConfig(getToolConfig().getConfigurationSection("PlayerMapStore"));
    }
    
    public void saveMapsFile()
    {
        if(mapsFile == null || mapConfig == null) return;
        getToolConfig().set("PlayerMapStore", this.serialize());
        try 
        {
            getToolConfig().save(mapsFile);
        } 
        catch (IOException ex) 
        {
            PluginLogger.LogError("Could not save maps file for player " + playerUUID.toString(), ex);
        }
        PluginLogger.LogInfo("Saving maps file for " + playerUUID.toString());
        modified = false;
    }
}
