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

import java.util.ArrayList;
import java.util.UUID;

public class PlayerMapStore 
{
    private final UUID playerUUID;
    private final ArrayList<ImageMap> mapList = new ArrayList<ImageMap>();
    
    public PlayerMapStore(UUID playerUUID)
    {
        this.playerUUID = playerUUID;
    }
    
    public boolean managesMap(short mapID)
    {
        for(ImageMap map : mapList)
        {
            if(map.managesMap(mapID)) return true;
        }
        return false;
    }
    
    /* ===== Getters & Setters ===== */
    
    public UUID getUUID()
    {
        return playerUUID;
    }
}
