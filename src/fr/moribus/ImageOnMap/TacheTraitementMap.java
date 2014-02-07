package fr.moribus.ImageOnMap;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

public class TacheTraitementMap extends BukkitRunnable
{
	int i;
	Player joueur;
 	ImageRendererThread renduImg;
 	PlayerInventory inv;
 	ItemStack map;
 	ImageOnMap plugin;
 	boolean resized;
	
 	TacheTraitementMap(Player j, String u, ImageOnMap plug, boolean r)
 	{
 		i = 0;
 		joueur = j;
 		renduImg = new ImageRendererThread(u, r);
 		renduImg.start();
 		inv = joueur.getInventory();
 		plugin = plug;
 		resized = r;
 	}
 	
	@SuppressWarnings("deprecation")
	@Override
	public void run() 
	{
		if(!renduImg.getStatut())
		{
			//joueur.sendMessage("Nombre d'exécution depuis le lancement du timer : " + i);
			i++;
			if(renduImg.isErreur() || i > 42)
			{
				joueur.sendMessage("TIMEOUT: the render took too many time");
				cancel();
			}
		}
		else
		{
			cancel();
			int nbImage = renduImg.getImg().length;
			if (plugin.getConfig().getInt("Limit-map-by-server") != 0 && nbImage + ImgUtility.getNombreDeMaps(plugin) > plugin.getConfig().getInt("Limit-map-by-server"))
			{
				joueur.sendMessage("ERROR: cannot render "+ nbImage +" picture(s): the limit of maps per server would be exceeded.");
				return;
			}
			if (plugin.getConfig().getInt("Limit-map-by-player") != 0 && nbImage + ImgUtility.getNombreDeMapsParJoueur(plugin, joueur.getName()) > plugin.getConfig().getInt("Limit-map-by-player"))
			{
				joueur.sendMessage(ChatColor.RED +"ERROR: cannot render "+ nbImage +" picture(s): the limit of maps allowed for you (per player) would be exceeded.");
				return;
			}
			MapView carte;
			
			ArrayList<ItemStack> restant = new ArrayList<ItemStack>();
			for (int i = 0; i < nbImage; i++)
			{
				carte = Bukkit.createMap(joueur.getWorld());
				ImageRendererThread.SupprRendu(carte);
				carte.addRenderer(new Rendu(renduImg.getImg()[i]));
				map = new ItemStack(Material.MAP, 1, carte.getId());
				if(!resized)
				{
					ItemMeta meta = map.getItemMeta();
					meta.setDisplayName("Map (" +renduImg.getNumeroMap().get(i) +")");
					map.setItemMeta(meta);
				}
				
				
				ImgUtility.AddMap(map, inv, restant);
				
				//Svg de la map
				SavedMap svg = new SavedMap(plugin, joueur.getName(), carte.getId(), renduImg.getImg()[i], joueur.getWorld().getName());
				svg.SaveMap();
				joueur.sendMap(carte);
			}
			if(!restant.isEmpty())
				joueur.sendMessage(restant.size()+ " maps can't be place in your inventory. Please make free space in your inventory and run "+ ChatColor.GOLD+  "/maptool rest");
			plugin.setRemainingMaps(joueur.getName(), restant);
			joueur.sendMessage("Render finished");
		}
	}

}
