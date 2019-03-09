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

import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.InvalidConfigurationException;

public class SingleMap extends ImageMap
{
    protected final int mapID;
    
    public SingleMap(UUID ownerUUID, int mapID, String id, String name)
    {
        super(ownerUUID, Type.SINGLE, id, name);
        this.mapID = mapID;
    }
    
    public SingleMap(UUID ownerUUID, int mapID)
    {
        this(ownerUUID, mapID, null, null);
    }
    
    @Override
    public int[] getMapsIDs()
    {
        return new int[]{mapID};
    }

    @Override
    public boolean managesMap(int mapID)
    {
        return this.mapID == mapID;
    }
    
    @Override
    public int getMapCount()
    {
        return 1;
    }
    
    /* ====== Serialization methods ====== */
    
    public SingleMap(Map<String, Object> map, UUID userUUID) throws InvalidConfigurationException
    {
        super(map, userUUID, Type.SINGLE);
        int _mapID = getFieldValue(map, "mapID");
        mapID = (short) _mapID;//Meh
    }
    
    @Override
    protected void postSerialize(Map<String, Object> map)
    {
        map.put("mapID", mapID);
    }

}
