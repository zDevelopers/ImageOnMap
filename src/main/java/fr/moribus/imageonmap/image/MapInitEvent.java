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

package fr.moribus.imageonmap.image;

import fr.moribus.imageonmap.ImageOnMap;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

public class MapInitEvent implements Listener
{
    static public void init(Plugin plugin)
    {
        plugin.getServer().getPluginManager().registerEvents(new MapInitEvent(), plugin);
        
        for(World world : Bukkit.getWorlds())
        {
            for(ItemFrame frame : world.getEntitiesByClass(ItemFrame.class))
            {
                initMap(frame.getItem());
            }
        }
        
        for(Player player : Bukkit.getOnlinePlayers())
        {
            initMap(player.getItemInHand());
        }
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        for (Entity entity : event.getChunk().getEntities())
        {
            if (entity instanceof ItemFrame)
            {
                initMap(((ItemFrame)entity).getItem());
            }
        }
    }
    
    @EventHandler
    public void onPlayerInv(PlayerItemHeldEvent event)
    {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        initMap(item);
    }
    
    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event)
    {
        ItemStack item = event.getItem().getItemStack();
        initMap(item);
    }
    
    @EventHandler
    public void onPlayerInventoryPlace(InventoryClickEvent event)
    {
        switch(event.getAction())
        {
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
            case SWAP_WITH_CURSOR:
                initMap(event.getCursor());
        }
    }
    
    static protected void initMap(ItemStack item)
    {
        if (item != null && item.getType() == Material.MAP)
        {
            MapView map = Bukkit.getMap(item.getDurability());
            initMap(map);
        }
    }
    
    static protected void initMap(MapView map)
    {
        if(Renderer.isHandled(map)) return;
        
        File imageFile = ImageOnMap.getPlugin().getImageFile(map.getId());
        if(imageFile.isFile())
        {
            ImageIOExecutor.loadImage(imageFile, Renderer.installRenderer(map));
        }
    }
}
