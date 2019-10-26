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

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.map.SingleMap;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.core.ZLib;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import fr.zcraft.zlib.tools.items.ItemUtils;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

public class MapItemManager implements Listener
{
    static private HashMap<UUID, Queue<ItemStack>> mapItemCache;

    static public void init()
    {
        mapItemCache = new HashMap<>();
        ZLib.registerEvents(new MapItemManager());
    }

    static public void exit()
    {
        if (mapItemCache != null) mapItemCache.clear();
        mapItemCache = null;
    }

    static public boolean give(Player player, ImageMap map)
    {
        if (map instanceof PosterMap) return give(player, (PosterMap) map);
        else if (map instanceof SingleMap) return give(player, (SingleMap) map);
        return false;
    }

    static public boolean give(Player player, SingleMap map)
    {
        return give(player, createMapItem(map));
    }

    static public boolean give(Player player, PosterMap map)
    {
        if (!map.hasColumnData())
            return giveParts(player, map);
        return give(player, SplatterMapManager.makeSplatterMap(map));
    }

    static public boolean giveParts(Player player, PosterMap map)
    {
        boolean inventoryFull = false;

        ItemStack mapPartItem;
        for (int i = 0, c = map.getMapCount(); i < c; i++)
        {
            mapPartItem = map.hasColumnData() ? createMapItem(map, map.getColumnAt(i), map.getRowAt(i)) : createMapItem(map, i);
            inventoryFull = give(player, mapPartItem) || inventoryFull;
        }

        return inventoryFull;
    }

    static public int giveCache(Player player)
    {
        Queue<ItemStack> cache = getCache(player);
        Inventory inventory = player.getInventory();
        int givenItemsCount = 0;

        while (inventory.firstEmpty() >= 0 && !cache.isEmpty())
        {
            give(player, cache.poll());
            givenItemsCount++;
        }

        return givenItemsCount;
    }

    static private boolean give(final Player player, final ItemStack item)
    {
        boolean given = ItemUtils.give(player, item);

        if (given)
        {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1, 1);
        }

        return !given;
    }

    static public ItemStack createMapItem(SingleMap map)
    {
        return createMapItem(map.getMapsIDs()[0], map.getName(), false);
    }

    static public ItemStack createMapItem(PosterMap map, int index)
    {
        return createMapItem(map.getMapIdAt(index), getMapTitle(map, index), true);
    }

    static public ItemStack createMapItem(PosterMap map, int x, int y)
    {
        return createMapItem(map.getMapIdAt(x, y), getMapTitle(map, y, x), true);
    }

    static public String getMapTitle(PosterMap map, int row, int column)
    {
        /// The name of a map item given to a player, if splatter maps are not used. 0 = map name; 1 = row; 2 = column.
        return I.t("{0} (row {1}, column {2})", map.getName(), row + 1, column + 1);
    }

    static public String getMapTitle(PosterMap map, int index)
    {
        /// The name of a map item given to a player, if splatter maps are not used. 0 = map name; 1 = index.
        return I.t("{0} (part {1})", map.getName(), index + 1);
    }

    static public ItemStack createMapItem(int mapID, String text, boolean isMapPart)
    {
        final ItemStack mapItem = new ItemStackBuilder(Material.FILLED_MAP)
                .title(text)
                .hideAttributes()
                .item();

        final MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setMapId(mapID);
        meta.setColor(isMapPart ? Color.LIME : Color.GREEN);
        mapItem.setItemMeta(meta);

        return mapItem;
    }

    /**
     * Returns the item to place to display the (col;row) part of the given poster.
     *
     * @param map The map to take the part from.
     * @param x   The x coordinate of the part to display. Starts at 0.
     * @param y   The y coordinate of the part to display. Starts at 0.
     * @return The map.
     * @throws ArrayIndexOutOfBoundsException If x;y is not inside the map.
     */
    static public ItemStack createSubMapItem(ImageMap map, int x, int y)
    {
        if (map instanceof PosterMap && ((PosterMap) map).hasColumnData())
        {
            return MapItemManager.createMapItem((PosterMap) map, x, y);
        }
        else
        {
            if (x != 0 || y != 0)
            {
                throw new ArrayIndexOutOfBoundsException(); // Coherence
            }

            return createMapItem(map.getMapsIDs()[0], map.getName(), false);
        }
    }

    static public int getCacheSize(Player player)
    {
        return getCache(player).size();
    }

    static private Queue<ItemStack> getCache(Player player)
    {
        Queue<ItemStack> cache = mapItemCache.get(player.getUniqueId());
        if (cache == null)
        {
            cache = new ArrayDeque<>();
            mapItemCache.put(player.getUniqueId(), cache);
        }
        return cache;
    }

    static private String getMapTitle(ItemStack item)
    {
        ImageMap map = MapManager.getMap(item);
        if (map instanceof SingleMap)
        {
            return map.getName();
        }
        else
        {
            PosterMap poster = (PosterMap) map;
            int index = poster.getIndex(MapManager.getMapIdFromItemStack(item));
            if (poster.hasColumnData())
                return getMapTitle(poster, poster.getRowAt(index), poster.getColumnAt(index));

            return getMapTitle(poster, index);
        }
    }

    static private void onItemFramePlace(ItemFrame frame, Player player, PlayerInteractEntityEvent event)
    {
        final ItemStack mapItem = player.getInventory().getItemInMainHand();

        if (frame.getItem().getType() != Material.AIR) return;
        if (!MapManager.managesMap(mapItem)) return;

        event.setCancelled(true);

        if (SplatterMapManager.hasSplatterAttributes(mapItem))
        {
            if (!SplatterMapManager.placeSplatterMap(frame, player))
                return;
        }
        else
        {
            frame.setItem(mapItem);
        }

        ItemUtils.consumeItem(player);
    }

    static private void onItemFrameRemove(ItemFrame frame, Player player, EntityDamageByEntityEvent event)
    {
        ItemStack item = frame.getItem();
        if (frame.getItem().getType() != Material.FILLED_MAP) return;

        if (player.isSneaking())
        {
            PosterMap poster = SplatterMapManager.removeSplatterMap(frame);
            if (poster != null)
            {
                event.setCancelled(true);

                if (player.getGameMode() != GameMode.CREATIVE || !SplatterMapManager.hasSplatterMap(player, poster))
                    poster.give(player);

                return;
            }
        }

        if (!MapManager.managesMap(frame.getItem())) return;

        frame.setItem(new ItemStackBuilder(item)
                .title(getMapTitle(item))
                .hideAttributes()
                .item());

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    static public void onEntityDamage(EntityDamageByEntityEvent event)
    {
        if (!(event.getEntity() instanceof ItemFrame)) return;
        if (!(event.getDamager() instanceof Player)) return;

        onItemFrameRemove((ItemFrame) event.getEntity(), (Player) event.getDamager(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    static public void onEntityInteract(PlayerInteractEntityEvent event)
    {
        if (!(event.getRightClicked() instanceof ItemFrame)) return;
        onItemFramePlace((ItemFrame) event.getRightClicked(), event.getPlayer(), event);
    }
}
