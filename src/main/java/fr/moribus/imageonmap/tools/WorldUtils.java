/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * Parts of this file are adapted from QuartzLib (https://github.com/zDevelopers/QuartzLib),
 * Copyright or © or Copr. ZDevelopers (2015 – 2021), governed by the CeCILL-B license.
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

package fr.moribus.imageonmap.tools;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

/**
 * Location-related utilities.
 */
public final class WorldUtils {
    private WorldUtils() {
    }

    /**
     * Checks if two locations are in the same block.
     *
     * @param loc1 The first location.
     * @param loc2 The second location.
     * @return {@code true} if both locations are in the same block.
     */
    public static boolean blockEquals(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Gets the horizontal cardinal direction the given location is facing.
     *
     * @param loc The location.
     * @return The direction (north, east, south or west).
     */
    public static BlockFace get4thOrientation(Location loc) {
        // The yaw is normalized to [0, 360), as the server now gives it in [-180, 180): else, east and west would
        // both be detected as west.
        final float normalizedYaw = ((loc.getYaw() % 360f) + 360f) % 360f;
        final float yaw = normalizedYaw - 180f;

        if (yaw <= 45 && yaw > -45) {
            return BlockFace.NORTH;
        }
        if (yaw <= -45 && yaw > -135) {
            return BlockFace.WEST;
        }
        if (yaw <= -135 || yaw > 135) {
            return BlockFace.SOUTH;
        }
        if (yaw <= 135 && yaw > 45) {
            return BlockFace.EAST;
        }
        return BlockFace.SELF;
    }
}
