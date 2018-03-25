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
import fr.moribus.imageonmap.ui.MapItemManager;
import fr.zcraft.zlib.components.i18n.I;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ImageMap implements ConfigurationSerializable
{
    public enum Type
    {
        SINGLE, POSTER
    }
    
    static public final int WIDTH = 128;
    static public final int HEIGHT = 128;

    /// The default display name of a map
    static public final String DEFAULT_NAME = I.t("Map");
    
    private String id;
    private final UUID userUUID;
    private final Type mapType;
    private String name;
    protected ImageMap(UUID userUUID, Type mapType)
    {
        this(userUUID, mapType, null, null);
    }
    
    protected ImageMap(UUID userUUID, Type mapType, String id, String name)
    {
        this.userUUID = userUUID;
        this.mapType = mapType;
        this.id = id;
        this.name = name;
        
        if(this.id == null)
        {
            if(this.name == null) this.name = DEFAULT_NAME;
            this.id = MapManager.getNextAvailableMapID(this.name, userUUID);
        }
    }
    
    
    public abstract int[] getMapsIDs();
    public abstract boolean managesMap(int mapID);
    public abstract int getMapCount();
    
    public boolean managesMap(ItemStack item)
    {
        if(item == null) return false;
        if(item.getType() != Material.FILLED_MAP) return false;
        return managesMap(MapManager.getMapIdFromItemStack(item));
    }
    
    public boolean give(Player player)
    {
        return MapItemManager.give(player, this);
    }
    
    public static File getFullImageFile(short mapIDstart, short mapIDend)
    {
        return new File(ImageOnMap.getPlugin().getImagesDirectory(), "_"+mapIDstart+"-"+mapIDend+".png");
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
        this(userUUID, mapType,
                (String) getNullableFieldValue(map, "id"),
                (String) getNullableFieldValue(map, "name"));
        
    }
    
    protected abstract void postSerialize(Map<String, Object> map);
    
    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", getId());
        map.put("type", mapType.toString());
        map.put("name", getName());
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

    public synchronized String getName()
    {
        return name;
    }
    
    public synchronized String getId()
    {
        return id;
    }

    public synchronized Type getType()
    {
        return mapType;
    }

    public synchronized void rename(String id, String name)
    {
        this.id = id;
        this.name = name;
    }
    
    public void rename(String name)
    {
        if(getName().equals(name)) return;
        rename(MapManager.getNextAvailableMapID(name, getUserUUID()), name);
    }
}
