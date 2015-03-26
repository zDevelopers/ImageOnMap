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
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class ImageMap implements ConfigurationSerializable
{
    static public enum Type 
    {
        SINGLE, POSTER;
    };
    
    static public final int WIDTH = 128;
    static public final int HEIGHT = 128;
    
    private final UUID userUUID;
    private final Type mapType;
    private String imageName;
    
    protected ImageMap(UUID userUUID, Type mapType)
    {
        this.userUUID = userUUID;
        this.mapType = mapType;
    }
    
    
    public abstract short[] getMapsIDs();
    public abstract boolean managesMap(short mapID);
    
    public void give(Inventory inventory)
    {
        short[] mapsIDs = getMapsIDs();
        for(short mapID : mapsIDs)
        {
            ItemStack itemMap = new ItemStack(Material.MAP, 1, mapID);
            inventory.addItem(itemMap);
        }
    }
    
    /* ====== Serialization methods ====== */
    
    static public ImageMap fromConfig(Map<String, Object> map, UUID userUUID) throws InvalidConfigurationException
    {
        Type mapType;
        try
        {
            mapType = Type.valueOf((String) map.get("type"));
        }
        catch(ClassCastException ex)
        {
            throw new InvalidConfigurationException(ex);
        }
        
        switch(mapType)
        {
            case SINGLE: return new SingleMap(map, userUUID);
            case POSTER: return new PosterMap(map, userUUID);
            default: throw new IllegalArgumentException("Unhandled map type given");
        }
    }
    
    protected ImageMap(Map<String, Object> map, UUID userUUID, Type mapType) throws InvalidConfigurationException
    {
        this(userUUID, mapType);
        this.imageName = getNullableFieldValue(map, "name");
    }
    
    protected abstract void postSerialize(Map<String, Object> map);
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", mapType.toString());
        map.put("name", imageName);
        this.postSerialize(map);
        return map;
    }
    
    static protected <T> T getFieldValue(Map<String, Object> map, String fieldName) throws InvalidConfigurationException
    {
        T value = getNullableFieldValue(map, fieldName);
        if(value == null) throw new InvalidConfigurationException("Field value not found for \"" + fieldName + "\"");
        return value;
    }
    
    static protected <T> T getNullableFieldValue(Map<String, Object> map, String fieldName) throws InvalidConfigurationException
    {
        try
        {
            return (T)map.get(fieldName);
        }
        catch(ClassCastException ex)
        {
            throw new InvalidConfigurationException("Invalid field \"" + fieldName + "\"", ex);
        }
    }

    
    /* ====== Getters & Setters ====== */
    
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
