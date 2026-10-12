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
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * A location on a flat surface (a wall, a floor or a ceiling) facing a given direction.
 */
public class FlatLocation extends Location {
    private final BlockFace facing;

    public FlatLocation(World world, double x, double y, double z, float yaw, float pitch, BlockFace facing) {
        super(world, x, y, z, yaw, pitch);
        this.facing = facing;
    }

    public FlatLocation(World world, double x, double y, double z, BlockFace facing) {
        this(world, x, y, z, 0, 0, facing);
    }

    public FlatLocation(World world, BlockFace facing) {
        this(world, 0, 0, 0, facing);
    }

    public FlatLocation(Location loc, BlockFace facing) {
        this(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), facing);
    }

    public FlatLocation(FlatLocation other) {
        this(other, other.facing);
    }

    /**
     * Computes the horizontal distance between two flat locations, in blocks, along their surface.
     *
     * @param loc1 The first location.
     * @param loc2 The second location.
     * @return The distance.
     */
    public static int flatBlockDistanceX(FlatLocation loc1, FlatLocation loc2) {
        checkSimilarLocations(loc1, loc2);
        switch (loc1.getFacing()) {
            case NORTH:
            case SOUTH:
                return Math.abs(loc1.getBlockX() - loc2.getBlockX());
            case EAST:
            case WEST:
                return Math.abs(loc1.getBlockZ() - loc2.getBlockZ());
            default:
                throw new UnsupportedOperationException("Non-N/S/E/W orientations are not supported.");
        }
    }

    /**
     * Computes the vertical distance between two flat locations, in blocks.
     *
     * @param loc1 The first location.
     * @param loc2 The second location.
     * @return The distance.
     */
    public static int flatBlockDistanceY(FlatLocation loc1, FlatLocation loc2) {
        checkSimilarLocations(loc1, loc2);
        return Math.abs(loc1.getBlockY() - loc2.getBlockY());
    }

    protected static void checkSimilarLocations(FlatLocation loc1, FlatLocation loc2) throws IllegalArgumentException {
        if (loc1.getFacing() != loc2.getFacing()) {
            throw new IllegalArgumentException("Trying to compare two FlatLocations on different axes.");
        }
        if (loc1.getWorld() != loc2.getWorld()) {
            throw new IllegalArgumentException("Trying to compare two Locations from different worlds.");
        }
    }

    /**
     * Computes the bottom-left corner of the rectangle defined by two flat locations.
     *
     * @param loc1 The first location.
     * @param loc2 The second location.
     * @return The bottom-left corner.
     */
    public static FlatLocation minMerged(FlatLocation loc1, FlatLocation loc2) {
        checkSimilarLocations(loc1, loc2);
        final FlatLocation loc = new FlatLocation(loc1.getWorld(), loc1.getFacing());
        loc.setY(Math.min(loc1.getY(), loc2.getY()));

        switch (loc1.getFacing()) {
            case NORTH:
                loc.setX(Math.max(loc1.getX(), loc2.getX()));
                loc.setZ(loc1.getZ());
                break;
            case SOUTH:
                loc.setX(Math.min(loc1.getX(), loc2.getX()));
                loc.setZ(loc1.getZ());
                break;
            case EAST:
                loc.setZ(Math.max(loc1.getZ(), loc2.getZ()));
                loc.setX(loc1.getX());
                break;
            case WEST:
                loc.setZ(Math.min(loc1.getZ(), loc2.getZ()));
                loc.setX(loc1.getX());
                break;
            default:
                break;
        }

        return loc;
    }

    public BlockFace getFacing() {
        return facing;
    }

    @Override
    public @NotNull FlatLocation clone() {
        return new FlatLocation(this);
    }

    /**
     * Moves this location on a horizontal surface, relative to the given direction.
     *
     * @param x  The offset to the right.
     * @param z  The offset forward.
     * @param bf The reference direction.
     * @return This location, for method chaining.
     */
    public FlatLocation addH(double x, double z, BlockFace bf) {
        switch (bf) {
            case NORTH:
                add(x, 0, -z);
                break;
            case EAST:
                add(x, 0, z);
                break;
            case WEST:
                add(-x, 0, -z);
                break;
            case SOUTH:
                add(-x, 0, z);
                break;
            default:
                break;
        }
        return this;
    }

    /**
     * Moves this location along its vertical surface.
     *
     * @param x The horizontal offset.
     * @param y The vertical offset.
     * @return This location, for method chaining.
     */
    public FlatLocation add(double x, double y) {
        switch (facing) {
            case NORTH:
                add(-x, y, 0);
                break;
            case SOUTH:
                add(x, y, 0);
                break;
            case EAST:
                add(0, y, -x);
                break;
            case WEST:
                add(0, y, x);
                break;
            default:
                throw new UnsupportedOperationException("addToLocation called with non-N/S/E/W orientation.");
        }
        return this;
    }
}
