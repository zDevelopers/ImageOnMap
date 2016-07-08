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

package fr.moribus.imageonmap.ui;

import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.zlib.tools.world.FlatLocation;
import fr.zcraft.zlib.tools.world.WorldUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

public class PosterWall 
{
    
    public FlatLocation loc1;
    public FlatLocation loc2;
    
    public ItemFrame[] frames;
    
    public boolean isValid()
    {
        ItemFrame curFrame;
        FlatLocation bottomLeft = FlatLocation.minMerged(loc1, loc2);
        FlatLocation loc = bottomLeft.clone();
        
        int distX = FlatLocation.flatBlockDistanceX(loc1, loc2);
        int distY = FlatLocation.flatBlockDistanceY(loc1, loc2);
        
        frames = new ItemFrame[distX * distY];
        
        for(int x = 0; x < distX; x++)
        {
            for(int y = 0; y < distY; y++)
            {
                curFrame = getEmptyFrameAt(loc, loc.getFacing());
                if(curFrame == null) return false;
                frames[y * distX + x] = curFrame;
                loc.add(0, 1);
            }
            loc.add(1, 0);
            loc.setY(bottomLeft.getY());
        }
        
        return true;
    }
    
    public void expand()
    {
        
        
        
    }
    
    static public ItemFrame[] getMatchingMapFrames(PosterMap map, FlatLocation location, short mapId)
    {
        int mapIndex = map.getIndex(mapId);
        int x = map.getColumnAt(mapIndex), y = map.getRowAt(mapIndex);
        
        return getMatchingMapFrames(map, location.clone().add(-x + 1, y - 1));
    }
    
    static public ItemFrame[] getMatchingMapFrames(PosterMap map, FlatLocation location)
    {
        ItemFrame[] frames = new ItemFrame[map.getMapCount()];
        FlatLocation loc = location.clone();
        
        for(int y = 0; y < map.getRowCount(); ++y)
        {
            for(int x = 0; x < map.getColumnCount(); ++x)
            {
                int mapIndex = map.getIndexAt(x, y);
                ItemFrame frame = getMapFrameAt(loc, map);
                if(frame != null) frames[mapIndex] = frame;
                loc.add(1, 0);
            }
            loc.setX(location.getX());
            loc.setZ(location.getZ());
            loc.add(0, -1);
        }
        
        return frames;
    }
    
    static public ItemFrame getMapFrameAt(FlatLocation location, PosterMap map)
    {
        Entity entities[] = location.getChunk().getEntities();
        
        for(Entity entity : entities)
        {
            if(!(entity instanceof ItemFrame)) continue;
            if(!WorldUtils.blockEquals(location, entity.getLocation())) continue;
            ItemFrame frame = (ItemFrame) entity;
            if(frame.getFacing() != location.getFacing()) continue;
            ItemStack item = frame.getItem();
            if(item.getType() != Material.MAP) continue;
            if(!map.managesMap(item)) continue;
            return frame;
        }
        
        return null;
    }
    
    static public ItemFrame getEmptyFrameAt(Location location, BlockFace facing)
    {
        Entity entities[] = location.getChunk().getEntities();
        
        for(Entity entity : entities)
        {
            if(!(entity instanceof ItemFrame)) continue;
            if(!WorldUtils.blockEquals(location, entity.getLocation())) continue;
            ItemFrame frame = (ItemFrame) entity;
            if(frame.getFacing() != facing) continue;
            ItemStack item = frame.getItem();
            if(item.getType() != Material.AIR) continue;
            return frame;
        }
        
        return null;
    }
}
