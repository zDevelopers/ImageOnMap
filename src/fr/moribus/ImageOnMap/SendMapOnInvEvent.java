package fr.moribus.ImageOnMap;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;


public class SendMapOnInvEvent implements Listener
{
	
	ImageOnMap plugin;

	SendMapOnInvEvent(ImageOnMap p)
	{
		plugin = p;
	}

	@EventHandler
	public void onPlayerInv(PlayerItemHeldEvent event)
	{
		Player joueur = event.getPlayer();
		int slot = event.getNewSlot();
		ItemStack stack = joueur.getInventory().getItem(slot);

			if(stack != null && stack.getType() == Material.MAP)
			{
				
				ArrayList<Short> listeId = plugin.mapChargee;
				Set<String> cle = plugin.getCustomConfig().getKeys(false);
				for (String s: cle)
				{
					
					
					if(!listeId.contains(stack.getDurability()))
					{
						if(plugin.getCustomConfig().getStringList(s).get(0).equals(String.valueOf(stack.getDurability())))
						{
							SavedMap map = new SavedMap(plugin, Short.valueOf(plugin.getCustomConfig().getStringList(s).get(0)));
							
							if(!map.LoadMap())
								plugin.getLogger().log(Level.WARNING, "Unable to load map " +stack.getDurability());
							else
							{
								break;
							}
								
						}
					}
					
					
				}
			}
			
		
	}
}
