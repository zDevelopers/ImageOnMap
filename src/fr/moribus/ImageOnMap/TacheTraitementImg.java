package fr.moribus.ImageOnMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

public class TacheTraitementImg extends BukkitRunnable
{
	int i;
	Player joueur;
 	ImageRenderer renduImg;
 	PlayerInventory inv;
 	ItemStack map;
	
 	TacheTraitementImg(Player j, String u)
 	{
 		i = 0;
 		joueur = j;
 		renduImg = new ImageRenderer(u);
 		renduImg.start();
 		inv = joueur.getInventory();
 	}
 	
	@SuppressWarnings("deprecation")
	@Override
	public void run() 
	{
		if(!renduImg.getStatut())
		{
			joueur.sendMessage("Nombre d'exécution depuis le lancement du timer : " + i);
			i++;
		}
		else
		{
			cancel();
			int nbImage = renduImg.getImg().getPoster().length;
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
			}
			joueur.sendMessage("Rendu de l'image fini");
		}
	}

}
