/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
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

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.i18n.I;
import fr.moribus.imageonmap.image.MapInitEvent;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.tools.ActionBar;
import fr.moribus.imageonmap.tools.ChatColor;
import fr.moribus.imageonmap.tools.FlatLocation;
import fr.moribus.imageonmap.tools.ItemStackBuilder;
import fr.moribus.imageonmap.tools.RunTask;
import fr.moribus.imageonmap.tools.WorldUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

//TODO rework splatter effect, using ID is far more stable than nbt tags.
// To update when adding small picture previsualization.
public abstract class SplatterMapManager {
    /**
     * The key of the persistent data marking an item as a splatter map.
     */
    private static NamespacedKey splatterKey = null;

    private SplatterMapManager() {
    }

    public static void init(Plugin plugin) {
        splatterKey = new NamespacedKey(plugin, "splatter_map");
    }

    public static ItemStack makeSplatterMap(PosterMap map) {


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
                                +
                                I.t("Place empty item frames on a wall, enough to host the whole map."
                                        + " Then, right-click on the bottom-left frame with this map."),
                        40)
                .loreLine()
                .longLore(ChatColor.GRAY
                        + I.t("Shift-click one of the placed maps to remove the whole poster in one shot."), 40)
                .hideAllAttributes()
                .item();

        final MapMeta meta = (MapMeta) splatter.getItemMeta();
        MapItemManager.setMapId(meta, map.getMapIdAt(0));
        splatter.setItemMeta(meta);

        return addSplatterAttribute(splatter);
    }

    /**
     * To identify image on maps for the auto-splattering to work, we mark the items with a persistent data tag, and
     * make them glow like enchanted items.
     *
     * @param itemStack The item stack to mark as a splatter map.
     * @return The modified item stack.
     */
    public static ItemStack addSplatterAttribute(final ItemStack itemStack) {
        final ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(splatterKey, PersistentDataType.BOOLEAN, true);
        meta.setEnchantmentGlintOverride(true);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Checks if an item have the splatter attribute set.
     *
     * <p>Splatter maps created by older versions of ImageOnMap were marked with an enchantment, so enchanted maps
     * are also considered as splatter maps.</p>
     *
     * @param itemStack The item to check.
     * @return True if the attribute was detected.
     */
    public static boolean hasSplatterAttributes(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }

        final ItemMeta meta = itemStack.getItemMeta();
        return meta.getPersistentDataContainer().has(splatterKey, PersistentDataType.BOOLEAN) || meta.hasEnchants();
    }

    /**
     * Return true if it is a splatter map
     *
     * @param itemStack The item to check.
     * @return True if is a splatter map
     */
    public static boolean isSplatterMap(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        return hasSplatterAttributes(itemStack) && MapManager.managesMap(itemStack);
    }


    /**
     * Return true if it has a specified splatter map
     *
     * @param player The player to check.
     * @param map    The map to check.
     * @return True if the player has this map
     */
    public static boolean hasSplatterMap(Player player, PosterMap map) {
        Inventory playerInventory = player.getInventory();

        for (int i = 0; i < playerInventory.getSize(); ++i) {
            ItemStack item = playerInventory.getItem(i);
            if (isSplatterMap(item) && map.managesMap(item)) {
                return true;
            }
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
    public static boolean placeSplatterMap(ItemFrame startFrame, Player player, PlayerInteractEntityEvent event) {
        ImageMap map = MapManager.getMap(player.getInventory().getItemInMainHand());

        if (!(map instanceof PosterMap)) {
            return false;
        }
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
                ActionBar.sendMessage(player,
                        I.t("{ce}There is not enough space to place this map ({0} × {1}).",
                                poster.getColumnCount(),
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
                    default:
                        //throw new IllegalStateException("Unexpected value: " + frame.getFacing());
                }
                //Rotation management relative to player rotation the default position is North,
                // when on ceiling we flipped the rotation
                RunTask.later(() -> {
                    addPropertiesToFrames(player, frame);
                    frame.setItem(
                            createFrameMapItem(id));
                }, 5L);

                if (i == 0) {
                    //First map need to be rotate one time CounterClockwise
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
                    default:
                        throw new IllegalStateException("Unexpected value: " + bf);
                }


                MapInitEvent.initMap(id);
                i++;
            }
        } else {
            // If it is on a wall NSEW
            FlatLocation startLocation = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
            FlatLocation endLocation = startLocation.clone().add(poster.getColumnCount(), poster.getRowCount());

            wall.loc1 = startLocation;
            wall.loc2 = endLocation;

            if (!wall.isValid()) {
                ActionBar.sendMessage(player,
                        I.t("{ce}There is not enough space to place this map ({0} × {1}).", poster.getColumnCount(),
                                poster.getRowCount()));
                return false;
            }

            int i = 0;
            for (ItemFrame frame : wall.frames) {

                int id = poster.getMapIdAtReverseY(i);

                RunTask.later(() -> {
                    addPropertiesToFrames(player, frame);
                    frame.setItem(
                            createFrameMapItem(id));
                }, 5L);


                //Force reset of rotation
                frame.setRotation(Rotation.NONE);
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
     **/
    public static PosterMap removeSplatterMap(ItemFrame startFrame, Player player) {
        final ImageMap map = MapManager.getMap(startFrame.getItem());
        if (!(map instanceof PosterMap)) {
            return null;
        }
        PosterMap poster = (PosterMap) map;
        if (!poster.hasColumnData()) {
            return null;
        }
        FlatLocation loc = new FlatLocation(startFrame.getLocation(), startFrame.getFacing());
        ItemFrame[] matchingFrames = null;

        switch (startFrame.getFacing()) {
            case UP:
            case DOWN:
                matchingFrames = PosterOnASurface.getMatchingMapFrames(poster, loc,
                        MapManager.getMapIdFromItemStack(startFrame.getItem()),
                        WorldUtils.get4thOrientation(player.getLocation()));//startFrame.getFacing());
                break;

            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                matchingFrames = PosterWall.getMatchingMapFrames(poster, loc,
                        MapManager.getMapIdFromItemStack(startFrame.getItem()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + startFrame.getFacing());
        }

        if (matchingFrames == null) {
            return null;
        }

        for (ItemFrame frame : matchingFrames) {
            if (frame != null) {
                removePropertiesFromFrames(player, frame);
                frame.setItem(null);
            }
        }

        return poster;
    }

    public static void addPropertiesToFrames(Player player, ItemFrame frame) {
        if (Permissions.PLACE_INVISIBLE_SPLATTER_MAP.grantedTo(player)) {
            frame.setVisible(false);
        }
    }

    public static void removePropertiesFromFrames(Player player, ItemFrame frame) {
        frame.setVisible(true);
    }

    private static ItemStack createFrameMapItem(int mapId) {
        final ItemStack item = new ItemStack(Material.FILLED_MAP);
        final MapMeta meta = (MapMeta) item.getItemMeta();
        MapItemManager.setMapId(meta, mapId);
        item.setItemMeta(meta);
        return item;
    }
}
