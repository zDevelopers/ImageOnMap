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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;

class OldSavedPoster 
{
    private final String userName;
    private final String posterName;
    private final int[] mapsIds;
    
    @SuppressWarnings("unchecked")
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
        mapsIds = new int[data.size() - 1];
        
        for(int i = 1, c = data.size(); i < c; i++)
        {
            try
            {
                mapsIds[i - 1] = Integer.parseInt(data.get(i));
            }
            catch(NumberFormatException ex)
            {
                throw new InvalidConfigurationException("Invalid map ID : " + ex.getMessage());
            }
        }
    }
    
    public boolean contains(OldSavedMap map)
    {
        int mapId = map.getMapId();
        
        for(int i = 0, c = mapsIds.length; i < c; i++)
        {
            if(mapsIds[i] == mapId) return true;
        }
        
        return false;
    }
    
    public ImageMap toImageMap(UUID userUUID)
    {
        return new PosterMap(userUUID, mapsIds, null, "poster", 0, 0);
    }
    
    public void serialize(Configuration configuration)
    {
        ArrayList<String> data = new ArrayList<String>();
        data.add(userName);
        
        for(int mapId : mapsIds)
        {
            data.add(mapId + "");
        }
        
        configuration.set(posterName, data);
        
    }
    
    public boolean isMapValid()
    {
        for(int mapId : mapsIds)
        {
            if(!MapManager.mapIdExists(mapId))
                return false;
        }
        return true;
    }
    
    public String getUserName() {return userName;}
    public int[] getMapsIds() {return mapsIds;}
}
