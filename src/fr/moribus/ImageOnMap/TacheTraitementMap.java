package fr.moribus.ImageOnMap;

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
 	ImageRenderer renduImg;
 	PlayerInventory inv;
 	ItemStack map;
 	ImageOnMap plugin;
	
 	TacheTraitementMap(Player j, String u, ImageOnMap plug)
 	{
 		i = 0;
 		joueur = j;
 		renduImg = new ImageRenderer(u);
 		renduImg.start();
 		inv = joueur.getInventory();
 		plugin = plug;
 	}
 	
	@SuppressWarnings("deprecation")
	@Override
	public void run() 
	{
		if(!renduImg.getStatut())
		{
			//joueur.sendMessage("Nombre d'exécution depuis le lancement du timer : " + i);
			i++;
			if(i > 42)
			{
				joueur.sendMessage("TIMEOUT: the render took too many time");
				cancel();
			}
		}
		else
		{
			cancel();
			int nbImage = renduImg.getImg().getPoster().length;
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
			
			for (int i = 0; i < nbImage; i++)
			{
				carte = Bukkit.createMap(joueur.getWorld());
				ImageRenderer.SupprRendu(carte);
				carte.addRenderer(new Rendu(renduImg.getImg().getPoster()[i]));
				map = new ItemStack(Material.MAP, 1, carte.getId());
				ItemMeta meta = map.getItemMeta();
				meta.setDisplayName("Map (" +renduImg.getImg().NumeroMap.get(i) +")");
				map.setItemMeta(meta);
				inv.addItem(map);
				
				//Svg de la map
				SavedMap svg = new SavedMap(plugin, joueur.getName(), carte.getId(), renduImg.getImg().getPoster()[i]);
				svg.SaveMap();
			}
			joueur.sendMessage("Rendu de l'image fini");
		}
	}

}
