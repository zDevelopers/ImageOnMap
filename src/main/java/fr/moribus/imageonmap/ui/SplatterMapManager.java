/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2020)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2020)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
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
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.moribus.imageonmap.ui;

import com.google.common.collect.ImmutableMap;
import fr.moribus.imageonmap.image.MapInitEvent;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.nbt.NBT;
import fr.zcraft.zlib.components.nbt.NBTCompound;
import fr.zcraft.zlib.components.nbt.NBTException;
import fr.zcraft.zlib.components.nbt.NBTList;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.reflection.NMSException;
import fr.zcraft.zlib.tools.text.MessageSender;
import fr.zcraft.zlib.tools.world.FlatLocation;
import fr.zcraft.zlib.tools.world.WorldUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;


abstract public class SplatterMapManager {
    private SplatterMapManager() {
    }

    static public ItemStack makeSplatterMap(PosterMap map) {


        final ItemStack splatter = new ItemStackBuilder(Material.FILLED_MAP).title(ChatColor.GOLD, map.getName())
                .title(ChatColor.DARK_GRAY, " - ").title(ChatColor.GRAY, I.t("Splatter Map"))
                .title(ChatColor.DARK_GRAY, " - ")
                .title(ChatColor.GRAY, I.t("{0} × {1}", map.getColumnCount(), map.getRowCount()))
                .loreLine(ChatColor.GRAY, map.getId()).loreLine()
                /// Title in a splatter map tooltip
                .loreLine(ChatColor.BLUE, I.t("Item frames needed"))
                /// Size of a map stored in a splatter map
                .loreLine(ChatColor.GRAY,
                        I.t("{0} × {1} (total {2} frames)", map.getColumnCount(), map.getRowCount(),
                                map.getColumnCount() * map.getRowCount()))
                .loreLine()
                /// Title in a splatter map tooltip
                .loreLine(ChatColor.BLUE, I.t("How to use this?"))
                .longLore(
                        ChatColor.GRAY
                                + I.t("Place empty item frames on a wall, enough to host the whole map. Then, right-click on the bottom-left frame with this map."),
                        40)
                .loreLine()
                .longLore(ChatColor.GRAY
                        + I.t("Shift-click one of the placed maps to remove the whole poster in one shot."), 40)
                .hideAttributes().craftItem();

        final MapMeta meta = (MapMeta) splatter.getItemMeta();
        meta.setMapId(map.getMapIdAt(0));
        meta.setColor(Color.GREEN);
        splatter.setItemMeta(meta);

        return addSplatterAttribute(splatter);
    }

    /**
     * To identify image on maps for the auto-splattering to work, we mark the
     * items using an enchantment maps are not supposed to have (Mending).
     * <p>
     * Then we check if the map is enchanted at all to know if it's a splatter
     * map. This ensure compatibility with old splatter maps from 3.x, where
     * zLib's glow effect was used.
     * <p>
     * An AttributeModifier (using zLib's attributes system) is not used,
     * because Minecraft (or Spigot) removes them from maps in 1.14+, so that
     * wasn't stable enough (and the glowing effect of enchantments is
     * prettier).
     *
     * @param itemStack The item stack to mark as a splatter map.
     * @return The modified item stack. The instance may be different if the
     * passed item stack is not a craft item stack; that's why the
     * instance is returned.
     */
    static public ItemStack addSplatterAttribute(final ItemStack itemStack) {
        try {
            final NBTCompound nbt = NBT.fromItemStack(itemStack);
            final NBTList enchantments = new NBTList();
            final NBTCompound protection = new NBTCompound();

            protection.put("id", "minecraft:mending");
            protection.put("lvl", 1);
            enchantments.add(protection);

            nbt.put("Enchantments", enchantments);

            return NBT.addToItemStack(itemStack, nbt, false);
        } catch (NBTException | NMSException e) {
            PluginLogger.error("Unable to set Splatter Map attribute on item", e);
            return itemStack;
        }
    }

    /**
     * Checks if an item have the splatter attribute set (i.e. if the item is
     * enchanted in any way).
     *
     * @param itemStack The item to check.
     * @return True if the attribute was detected.
     */
    static public boolean hasSplatterAttributes(ItemStack itemStack) {
        try {
            final NBTCompound nbt = NBT.fromItemStack(itemStack);
            if (!nbt.containsKey("Enchantments"))
                return false;
            final Object enchantments = nbt.get("Enchantments");
            if (!(enchantments instanceof NBTList))
                return false;
            return !((NBTList) enchantments).isEmpty();
        } catch (NMSException e) {
            PluginLogger.error("Unable to get Splatter Map attribute on item", e);
            return false;
        }
    }

    /**
     * Return true if it is a platter map
     *
     * @param itemStack The item to check.
     * @return True if is a splatter map
     */
    static public boolean isSplatterMap(ItemStack itemStack) {
        if (itemStack == null) return false;
        return hasSplatterAttributes(itemStack) && MapManager.managesMap(itemStack);
    }


    //TODO doc a faire
    static public boolean hasSplatterMap(Player player, PosterMap map) {
        Inventory playerInventory = player.getInventory();

        for (int i = 0; i < playerInventory.getSize(); ++i) {
            ItemStack item = playerInventory.getItem(i);
            if (isSplatterMap(item) && map.managesMap(item))
                return true;
        }

        return false;
    }

    /**
     * Place a splatter map
     *
     * @param startFrame Frame clicked by the player
     * @param player     Player placing map
     * @return true if the map was correctly placed
     */
    static public boolean placeSplatterMap(ItemFrame startFrame, Player player, PlayerInteractEntityEvent event) {
        ImageMap map = MapManager.getMap(player.getInventory().getItemInMainHand());

        if (!(map instanceof PosterMap))
            return false;
        PosterMap poster = (PosterMap) map;
        PosterWall wall = new PosterWall();

        if (startFrame.getFacing().equals(BlockFace.DOWN) || startFrame.getFacing().equals(BlockFace.UP)) {
            // If it is on floor or ceiling
            PosterOnASurface surface = new PosterOnASurface();
            FlatLocation startLocation = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
            FlatLocation endLocation = startLocation.clone().addH(poster.getColumnCount(), poster.getRowCount(),
                    WorldUtils.get4thOrientation(player.getLocation()));

            surface.loc1 = startLocation;
            surface.loc2 = endLocation;

            if (!surface.isValid(player)) {
                MessageSender.sendActionBarMessage(player,
                        I.t("{ce}There is not enough space to place this map ({0} × {1}).", poster.getColumnCount(),
                                poster.getRowCount()));


                return false;
            }

            int i = 0;
            for (ItemFrame frame : surface.frames) {
                BlockFace bf = WorldUtils.get4thOrientation(player.getLocation());
                int id = poster.getMapIdAtReverseZ(i, bf, startFrame.getFacing());
                Rotation rot = Rotation.NONE;
                switch (frame.getFacing()) {
                    case UP:
                        break;
                    case DOWN:
                        rot = Rotation.FLIPPED;
                        break;
                }
                //Rotation management relative to player rotation the default position is North, when on ceiling we flipped the rotation
                frame.setItem(new ItemStackBuilder(Material.FILLED_MAP).nbt(ImmutableMap.of("map", id)).craftItem());
                if (i == 0) {//First map need to be rotate one time CounterClockwise
                    rot = rot.rotateCounterClockwise();
                }

                switch (bf) {
                    case NORTH:
                        if (frame.getFacing() == BlockFace.DOWN) {
                            rot = rot.rotateClockwise();
                            rot = rot.rotateClockwise();
                        }
                        frame.setRotation(rot);
                        break;
                    case EAST:
                        rot = rot.rotateClockwise();
                        frame.setRotation(rot);
                        break;
                    case SOUTH:
                        if (frame.getFacing() == BlockFace.UP) {
                            rot = rot.rotateClockwise();
                            rot = rot.rotateClockwise();
                        }
                        frame.setRotation(rot);
                        break;
                    case WEST:
                        rot = rot.rotateCounterClockwise();
                        frame.setRotation(rot);
                        break;
                }

        WorldMap
                MapInitEvent.initMap(id);
                i++;
            }
        } else {
            // If it is on a wall NSEW


            FlatLocation startLocation = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
            FlatLocation endLocation = startLocation.clone().add(poster.getColumnCount(), poster.getRowCount());
            if (player.getGameMode() == GameMode.CREATIVE)
                PosterWall.expand(startLocation,startFrame.getAttachedFace(), poster.getColumnCount(), poster.getRowCount());

            wall.loc1 = startLocation;
            wall.loc2 = endLocation;

            if (!wall.isValid()) {
                MessageSender.sendActionBarMessage(player,
                        I.t("{ce}There is not enough space to place this map ({0} × {1}).", poster.getColumnCount(),
                                poster.getRowCount()));
                return false;
            }

            int i = 0;
            for (ItemFrame frame : wall.frames) {

                int id = poster.getMapIdAtReverseY(i);
                frame.setItem(new ItemStackBuilder(Material.FILLED_MAP).nbt(ImmutableMap.of("map", id)).craftItem());

                //Force reset of rotation
                if (i == 0) {//First map need to be rotate one time Clockwise
                    frame.setRotation(Rotation.NONE.rotateCounterClockwise());
                } else {
                    frame.setRotation(Rotation.NONE);
                }
                MapInitEvent.initMap(id);
                ++i;
            }
        }
        return true;
    }

    /**
     * Remove splattermap
     *
     * @param startFrame Frame clicked by the player
     * @param player     The player removing the map
     * @return
     */
    static public PosterMap removeSplatterMap(ItemFrame startFrame, Player player) {
        final ImageMap map = MapManager.getMap(startFrame.getItem());
        if (!(map instanceof PosterMap))
            return null;
        PosterMap poster = (PosterMap) map;
        if (!poster.hasColumnData())
            return null;
        FlatLocation loc = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
        ItemFrame[] matchingFrames = null;

        switch (startFrame.getFacing()) {
            case UP:
            case DOWN:
                matchingFrames = PosterOnASurface.getMatchingMapFrames(poster, loc,
                        MapManager.getMapIdFromItemStack(startFrame.getItem()), WorldUtils.get4thOrientation(player.getLocation()));//startFrame.getFacing());
                break;

            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                matchingFrames = PosterWall.getMatchingMapFrames(poster, loc,
                        MapManager.getMapIdFromItemStack(startFrame.getItem()));
        }

        if (matchingFrames == null)
            return null;

        for (ItemFrame frame : matchingFrames) {
            if (frame != null)
                frame.setItem(null);
        }

        return poster;
    }
}
