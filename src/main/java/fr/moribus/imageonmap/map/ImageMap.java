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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public abstract class ImageMap implements ConfigurationSerializable
{
    static public final int WIDTH = 128;
    static public final int HEIGHT = 128;
    
    private final UUID userUUID;
    private String imageName;
    
    protected ImageMap(UUID userUUID)
    {
        this.userUUID = userUUID;
    }
    
    
    public abstract short[] getMapsIDs();
    public abstract boolean managesMap(short mapID);
    
    /*** Serialization methods ***/
    
    protected ImageMap(Map<String, Object> map, UUID userUUID) throws IllegalArgumentException
    {
        try
        {
            this.userUUID = userUUID;
            this.imageName = (String) map.get("name");
        }
        catch(ClassCastException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
    protected abstract void postSerialize(Map<String, Object> map);
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", imageName);
        return map;
    }

    
    /*** Getters & Setters ***/
    
    public UUID getUserUUID()
    {
        return userUUID;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }
}
