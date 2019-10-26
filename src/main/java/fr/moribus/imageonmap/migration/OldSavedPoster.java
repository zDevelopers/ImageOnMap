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
import fr.moribus.imageonmap.map.PosterMap;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class OldSavedPoster 
{
    private final String userName;
    private final String posterName;
    private final short[] mapsIds;
    
    public OldSavedPoster(Object rawData, String key) throws InvalidConfigurationException
    {
        posterName = key;
        List<String> data;
        try
        {
            data = (List<String>) rawData;
        }
        catch(ClassCastException ex)
        {
            throw new InvalidConfigurationException("Invalid map data : " + ex.getMessage());
        }
        
        if(data.size() < 2) 
            throw new InvalidConfigurationException("Poster data too short (given : " + data.size() + ", expected at least 2)");
        userName = data.get(0);
        mapsIds = new short[data.size() - 1];
        
        for(int i = 1, c = data.size(); i < c; i++)
        {
            try
            {
                mapsIds[i - 1] = Short.parseShort(data.get(i));
            }
            catch(NumberFormatException ex)
            {
                throw new InvalidConfigurationException("Invalid map ID : " + ex.getMessage());
            }
        }
    }
    
    public boolean contains(OldSavedMap map)
    {
        short mapId = map.getMapId();
        
        for(int i = 0, c = mapsIds.length; i < c; i++)
        {
            if(mapsIds[i] == mapId) return true;
        }
        
        return false;
    }
    
    public ImageMap toImageMap(UUID userUUID)
    {
        // Converts the maps IDs to int as MC 1.13.2+ uses integer ids
        final int[] mapsIdsInt = new int[mapsIds.length];
        Arrays.setAll(mapsIdsInt, i -> mapsIds[i]);

        return new PosterMap(userUUID, mapsIdsInt, null, "poster", 0, 0);
    }
    
    public void serialize(Configuration configuration)
    {
        ArrayList<String> data = new ArrayList<String>();
        data.add(userName);
        
        for(short mapId : mapsIds)
        {
            data.add(Short.toString(mapId));
        }
        
        configuration.set(posterName, data);
        
    }
    
    public boolean isMapValid()
    {
        for(short mapId : mapsIds)
        {
            if(!MapManager.mapIdExists(mapId))
                return false;
        }
        return true;
    }
    
    public String getUserName() {return userName;}
    public short[] getMapsIds() {return mapsIds;}
}
