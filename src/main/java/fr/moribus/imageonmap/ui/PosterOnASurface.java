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
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.world.FlatLocation;
import fr.zcraft.zlib.tools.world.WorldUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PosterOnASurface {

	public FlatLocation loc1;
	public FlatLocation loc2;

	public ItemFrame[] frames;

	public boolean isValid(Player p) {
		ItemFrame curFrame;


		FlatLocation l = loc1.clone();

		BlockFace bf = WorldUtils.get4thOrientation(p.getLocation());

		l.subtract(loc2);


		int distX = Math.abs(l.getBlockX());
		int distZ = Math.abs(l.getBlockZ());

		frames = new ItemFrame[distX * distZ];
		l = loc1.clone();
		for (int x = 0; x < distX; x++) {
			for (int z = 0; z < distZ; z++) {

				curFrame = getEmptyFrameAt(l, l.getFacing());

				if (curFrame == null)
					return false;

				frames[z * distX + x] = curFrame;

				switch (bf) {
				case NORTH:
				case SOUTH:
					l.addH(0, 1, bf);
					break;
				case EAST:
				case WEST:
					l.addH(1, 0, bf);
					break;
				}

			}

			switch (bf) {
			case NORTH:
			case SOUTH:
				l.addH(1, -distZ, bf);
				break;
			case EAST:
			case WEST:
				l.addH(-distZ, 1, bf);
				break;
			}
		}
		return true;
	}

	public void expand() {

	}

	/**
	 * Return the list of map Frames associated with a specific map
	 * */
	static public ItemFrame[] getMatchingMapFrames(PosterMap map, FlatLocation location, int mapId, BlockFace bf) {
		int mapIndex = map.getIndex(mapId);
		//int x = map.getColumnAt(mapIndex), y = map.getRowAt(mapIndex);
		int x=0,y=0;
		switch(bf){
			case EAST:
			case WEST:
				y=map.getColumnCount()-1;
				break;
			case NORTH:
			case SOUTH:
				y=map.getRowCount()-1;
				break;
		}
		return getMatchingMapFrames(map, location.clone().addH(x, y,bf),bf).clone();
	}

	static public ItemFrame[] getMatchingMapFrames(PosterMap map, FlatLocation location, BlockFace bf) {
		ItemFrame[] frames = new ItemFrame[map.getMapCount()];
		FlatLocation loc = location.clone();

		PluginLogger.info("loc = "+location+ " bf = "+bf);

		int X=0;
		int Y=0;
		switch (bf){
			case EAST:
			case WEST:
				/*X=map.getRowCount();
				Y=map.getColumnCount();
				break;*/
			case NORTH:
			case SOUTH:

				Y=map.getRowCount();
				X=map.getColumnCount();
				break;

		}

		PluginLogger.info("max: X: "+X+" Y:"+Y);
		for (int y = 0; y < Y; ++y) {
			for (int x = 0; x < X; ++x) {
				PluginLogger.info("x: "+x+" y:"+y);
				int mapIndex = map.getIndexAt(x, y);
				PluginLogger.info("map index "+mapIndex);

				ItemFrame frame = getMapFrameAt(loc, map);
				PluginLogger.info("frame= "+frame.getLocation());
				if (frame != null)
					frames[mapIndex] = frame;
				switch (bf){
					case EAST:
					case WEST:
						loc.addH( 0,-1,bf);
						break;
					case NORTH:
					case SOUTH:
						loc.addH( 1,0,bf);
						break;
				}


			}

			switch (bf){
				case EAST:
				case WEST:
					loc.addH( 1,map.getColumnCount(),bf);//test

					break;
				case NORTH:
				case SOUTH:
					loc.addH(-map.getColumnCount(), -1,bf);
					break;
			}

		}

		return frames;
	}

	static public ItemFrame getMapFrameAt(FlatLocation location, PosterMap map) {
		Entity entities[] = location.getChunk().getEntities();

		for (Entity entity : entities) {
			if (!(entity instanceof ItemFrame))
				continue;
			if (!WorldUtils.blockEquals(location, entity.getLocation()))
				continue;
			ItemFrame frame = (ItemFrame) entity;
			if (frame.getFacing() != location.getFacing())
				continue;
			ItemStack item = frame.getItem();
			if (item.getType() != Material.FILLED_MAP)
				continue;
			if (!map.managesMap(item))
				continue;
			return frame;
		}

		return null;
	}

	static public ItemFrame getEmptyFrameAt(Location location, BlockFace facing) {
		Entity entities[] = location.getChunk().getEntities();

		for (Entity entity : entities) {
			if (!(entity instanceof ItemFrame))
				continue;
			if (!WorldUtils.blockEquals(location, entity.getLocation()))
				continue;
			ItemFrame frame = (ItemFrame) entity;
			if (frame.getFacing() != facing)
				continue;
			ItemStack item = frame.getItem();
			if (item.getType() != Material.AIR)
				continue;
			return frame;
		}

		return null;
	}
}
