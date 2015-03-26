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
    protected short mapID;
    
    public SingleMap(UUID ownerUUID, short mapID)
    {
        super(ownerUUID, Type.SINGLE);
        this.mapID = mapID;
    }
    
    @Override
    public short[] getMapsIDs()
    {
        return new short[]{mapID};
    }

    @Override
    public boolean managesMap(short mapID)
    {
        return this.mapID == mapID;
    }
    
    /* ====== Serialization methods ====== */
    
    public SingleMap(Map<String, Object> map, UUID userUUID) throws InvalidConfigurationException
    {
        super(map, userUUID, Type.SINGLE);
        mapID = getFieldValue(map, "mapID");
    }
    
    @Override
    protected void postSerialize(Map<String, Object> map)
    {
        map.put("mapID", mapID);
    }

}
