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

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.SingleMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;

class OldSavedMap 
{
    private final short mapId;
    private final String mapName;
    private final String userName;
    
    public OldSavedMap(Object rawData) throws InvalidConfigurationException
    {
        List<String> data;
        try
        {
            data = (List<String>) rawData;
        }
        catch(ClassCastException ex)
        {
            throw new InvalidConfigurationException("Invalid map data : " + ex.getMessage());
        }
        
        if(data.size() < 3) 
            throw new InvalidConfigurationException("Map data too short (given : " + data.size() + ", expected 3)");
        try
        {
            mapId = Short.parseShort(data.get(0));
        }
        catch(NumberFormatException ex)
        {
            throw new InvalidConfigurationException("Invalid map ID : " + ex.getMessage());
        }
        
        mapName = data.get(1);
        userName = data.get(2);
    }
    
    public ImageMap toImageMap(UUID userUUID)
    {
        return new SingleMap(userUUID, mapId, null, mapName);
    }
    
    public void serialize(Configuration configuration)
    {
        ArrayList<String> data = new ArrayList<String>();
        data.add(Short.toString(mapId));
        data.add(mapName);
        data.add(userName);
        configuration.set(mapName, data);
    }
    
    public boolean isMapValid()
    {
        return MapManager.mapIdExists(mapId);
    }
    
    public short getMapId() {return mapId;}
    public String getUserName() {return userName;}
}
