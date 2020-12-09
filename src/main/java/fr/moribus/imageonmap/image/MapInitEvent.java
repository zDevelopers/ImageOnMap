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

package fr.moribus.imageonmap.image;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.quartzlib.core.QuartzLib;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
public class MapInitEvent implements Listener
{
    static public void init()
    {
        QuartzLib.registerEvents(new MapInitEvent());
        
        for(World world : Bukkit.getWorlds())
        {
            for(ItemFrame frame : world.getEntitiesByClass(ItemFrame.class))
            {
                initMap(frame.getItem());
            }
        }
        
        for(Player player : Bukkit.getOnlinePlayers())
        {
            initMap(player.getInventory().getItemInMainHand());
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
    public void onPlayerPickup(EntityPickupItemEvent event)
    {
        if (!(event.getEntity() instanceof HumanEntity)) return;
        initMap(event.getItem().getItemStack());
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
    
    static public void initMap(ItemStack item)
    {
        if (item != null && item.getType() == Material.FILLED_MAP)
        {
            initMap(MapManager.getMapIdFromItemStack(item));
        }
    }
    
    static public void initMap(int id)
    {
        initMap(Bukkit.getServer().getMap(id));
    }
    
    static public void initMap(MapView map)
    {
        if(map == null) {
            return;}
        if(Renderer.isHandled(map)) {
            return;}
        
        File imageFile = ImageOnMap.getPlugin().getImageFile(map.getId());
        if(imageFile.isFile())
        {
            ImageIOExecutor.loadImage(imageFile, Renderer.installRenderer(map));
        }
    }
}
