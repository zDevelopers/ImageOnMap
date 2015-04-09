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

import java.util.List;
import org.bukkit.configuration.InvalidConfigurationException;

class OldSavedPoster 
{
    private String userName;
    private short[] mapsIds;
    
    public OldSavedPoster(Object rawData) throws InvalidConfigurationException
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
    
    public String getUserName() {return userName;}
}
