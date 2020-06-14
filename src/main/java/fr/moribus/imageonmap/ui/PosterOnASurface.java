/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2022)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2022)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap.ui;

import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.quartzlib.tools.world.FlatLocation;
import fr.zcraft.quartzlib.tools.world.WorldUtils;
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

    /**
     * Return the list of map Frames associated with a specific map
     */
    public static ItemFrame[] getMatchingMapFrames(PosterMap map, FlatLocation location, int mapId, BlockFace bf) {
        int mapIndex = map.getIndex(mapId);
        //int x = map.getColumnAt(mapIndex), y = map.getRowAt(mapIndex);
        int x = 0;
        int y = 0;
        switch (bf) {
            case EAST:
            case WEST:
                y = map.getColumnCount() - 1;
                break;
            case NORTH:
            case SOUTH:
                y = map.getRowCount() - 1;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + bf);
        }
        return getMatchingMapFrames(map, location.clone().addH(x, y, bf), bf).clone();
    }

    public static ItemFrame[] getMatchingMapFrames(PosterMap map, FlatLocation location, BlockFace bf) {
        ItemFrame[] frames = new ItemFrame[map.getMapCount()];
        FlatLocation loc = location.clone();


        int x = 0;
        int y = 0;
        switch (bf) {
            case EAST:
            case WEST:
                //X=map.getRowCount();
                //Y=map.getColumnCount();
                //break;
            case NORTH:
            case SOUTH:

                y = map.getRowCount();
                x = map.getColumnCount();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + bf);
        }

        for (int j = 0; j < y; ++j) {
            for (int i = 0; i < x; ++i) {
                int mapIndex = map.getIndexAt(i, j);

                ItemFrame frame = getMapFrameAt(loc, map);
                if (frame != null) {
                    frames[mapIndex] = frame;
                }
                switch (bf) {
                    case EAST:
                    case WEST:
                        loc.addH(0, -1, bf);
                        break;
                    case NORTH:
                    case SOUTH:
                        loc.addH(1, 0, bf);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + bf);
                }


            }

            switch (bf) {
                case EAST:
                case WEST:
                    loc.addH(1, map.getColumnCount(), bf);//test

                    break;
                case NORTH:
                case SOUTH:
                    loc.addH(-map.getColumnCount(), -1, bf);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + bf);
            }

        }

        return frames;
    }

    public static ItemFrame getMapFrameAt(FlatLocation location, PosterMap map) {
        Entity[] entities = location.getChunk().getEntities();

        for (Entity entity : entities) {
            if (!(entity instanceof ItemFrame)) {
                continue;
            }
            if (!WorldUtils.blockEquals(location, entity.getLocation())) {
                continue;
            }
            ItemFrame frame = (ItemFrame) entity;
            if (frame.getFacing() != location.getFacing()) {
                continue;
            }
            ItemStack item = frame.getItem();
            if (item.getType() != Material.FILLED_MAP) {
                continue;
            }
            if (!map.managesMap(item)) {
                continue;
            }
            return frame;
        }

        return null;
    }

    public static ItemFrame getEmptyFrameAt(Location location, BlockFace facing) {
        Entity[] entities = location.getChunk().getEntities();

        for (Entity entity : entities) {
            if (!(entity instanceof ItemFrame)) {
                continue;
            }
            if (!WorldUtils.blockEquals(location, entity.getLocation())) {
                continue;
            }
            ItemFrame frame = (ItemFrame) entity;
            if (frame.getFacing() != facing) {
                continue;
            }
            ItemStack item = frame.getItem();
            if (item.getType() != Material.AIR) {
                continue;
            }
            return frame;
        }

        return null;
    }

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

                if (curFrame == null) {
                    return false;
                }

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
                    default:
                        throw new IllegalStateException("Unexpected value: " + bf);
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
                default:
                    throw new IllegalStateException("Unexpected value: " + bf);
            }
        }
        return true;
    }

    public void expand() {

    }
}
