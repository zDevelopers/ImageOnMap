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

public class PosterMap extends ImageMap
{
    protected short[] mapsIDs;
    protected int columnCount;
    protected int rowCount;
    
    public PosterMap(UUID userUUID, short[] mapsIDs, int columnCount, int rowCount)
    {
        super(userUUID);
        this.mapsIDs = mapsIDs;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
    }
    
    @Override
    public short[] getMapsIDs()
    {
        return mapsIDs;
    }

    @Override
    public boolean managesMap(short mapID)
    {
        for(int i = 0; i < mapsIDs.length; i++)
        {
            if(mapsIDs[i] == mapID) return true;
        }
        
        return false;
    }

    @Override
    protected void postSerialize(Map<String, Object> map)
    {
        map.put("columns", columnCount);
        map.put("rows", rowCount);
        map.put("mapsIDs", mapsIDs);
    }

}
