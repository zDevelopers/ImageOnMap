package fr.moribus.imageonmap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import fr.moribus.imageonmap.map.SingleMap;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImgUtility 
{

	// Vérifie que c'est bien un joueur qui exécute la commande
	static boolean VerifierIdentite(CommandSender sender)
	{
		if (sender instanceof Player)
		{
			return true;
		}
		else if (sender instanceof ConsoleCommandSender)
			{System.out.println(ChatColor.RED + "Cette commande ne peut être utilisée dans la console !"); return false;}
		else if (sender instanceof BlockCommandSender)
			{System.out.println(ChatColor.RED + "Cette commande ne peut être utilisée par un bloc-commande !"); return false;}
		else
			{System.out.println(ChatColor.RED + "Cette commande ne peut être lancée de cette façon !"); return false;}
	}
	
	// Creation du dossier où sera stocké les images
	static boolean CreeRepImg(ImageOnMap plugin)
	{
		File dossier;
		dossier = new File(plugin.getDataFolder().getPath() + "/Image");
		if (!dossier.exists())
		{
			 return dossier.mkdirs();
		}
		else
			return true;
	}
	
	static void CreeSectionConfig(ImageOnMap plugin)
	{
		if(plugin.getConfig().get("Limit-map-by-server") == null)
			plugin.getConfig().set("Limit-map-by-server", 0);
		if(plugin.getConfig().get("Limit-map-by-player") == null)
			plugin.getConfig().set("Limit-map-by-player", 0);
		if(plugin.getConfig().get("collect-data") == null)
			plugin.getConfig().set("collect-data", true);
		if(plugin.getConfig().get("import-maps") == null)
			plugin.getConfig().set("import-maps", true);
		if(plugin.getConfig().get("send-entire-maps") == null)
			plugin.getConfig().set("send-entire-maps", 0);
		plugin.saveConfig();
		
	}
	
	static int getNombreDeMaps(ImageOnMap plugin)
	{
		int nombre = 0;
		Set<String> cle = plugin.getCustomConfig().getKeys(false);
		for (String s: cle)
		{
			if(plugin.getCustomConfig().getStringList(s).size() >= 3)
			{
				nombre++;
			}
		}
		return nombre;
	}
	
	static int getNombreDeMapsParJoueur(ImageOnMap plugin, String pseudo)
	{
		int nombre = 0;
		Set<String> cle = plugin.getCustomConfig().getKeys(false);
		for (String s: cle)
		{
			if(plugin.getCustomConfig().getStringList(s).size() >= 3 && plugin.getCustomConfig().getStringList(s).get(2).contentEquals(pseudo))
			{
				nombre++;
			}
		}
		return nombre;
	}
	
	static boolean EstDansFichier(ImageOnMap plugin, short id)
	{
		Set<String> cle = plugin.getCustomConfig().getKeys(false);
		for (String s: cle)
		{
			if(plugin.getCustomConfig().getStringList(s).size() >= 3 && Short.parseShort(plugin.getCustomConfig().getStringList(s).get(0)) == id)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean EstDansFichier(ImageOnMap plugin, short id, String pseudo) 
	{
		Set<String> cle = plugin.getCustomConfig().getKeys(false);
		for (String s: cle)
		{
			if(plugin.getCustomConfig().getStringList(s).size() >= 3 && Short.parseShort(plugin.getCustomConfig().getStringList(s).get(0)) == id && plugin.getCustomConfig().getStringList(s).get(2).contentEquals(pseudo))
			{
				return true;
			}
		}
		return false;
	}
	
	static boolean ImporterConfig(ImageOnMap plugin)
	{
		Set<String> cle = plugin.getConfig().getKeys(false);
		
		plugin.getLogger().info("Start importing maps config to maps.yml...");
		int i = 0;
		for (String s: cle)
		{
			if(plugin.getConfig().getStringList(s).size() >= 3)
			{
				//plugin.getLogger().info("Importing "+ plugin.getConfig().getStringList(s).get(1));
				ArrayList<String> liste = new ArrayList<String>();
				liste.add(String.valueOf(plugin.getConfig().getStringList(s).get(0)));
				liste.add(plugin.getConfig().getStringList(s).get(1));
				liste.add(plugin.getConfig().getStringList(s).get(2));
				plugin.getCustomConfig().set(plugin.getConfig().getStringList(s).get(1), liste);
				plugin.getConfig().set(s, null);
				i++;
			}
			
		}
		plugin.getLogger().info("Importing finished. "+ i+ "maps were imported");
		plugin.getConfig().set("import-maps", false);
		plugin.saveConfig();
		plugin.saveCustomConfig();
		return true;
	}
	
	// Fait la même chose que EstDansFichier() mais en retournant un objet MapView
	@SuppressWarnings("deprecation")
	static MapView getMap(ImageOnMap plugin, short id)
	{
		MapView map;
		if(!ImgUtility.EstDansFichier(plugin, id))
		{
			return null;
		}
		
		
		map = Bukkit.getMap(id);
		if(map == null)
		{
			plugin.getLogger().warning("Map#"+ id+ " exists in maps.yml but not in the world folder !");
			return null;
		}
		
		return map;
	}
	
	static boolean RemoveMap(ImageOnMap plugin, short id)
	{
		@SuppressWarnings("deprecation")
		MapView carte = Bukkit.getMap(id);
		
		Set<String> cle = plugin.getCustomConfig().getKeys(false);
		for (String s: cle)
		{
			if(plugin.getCustomConfig().getStringList(s).size() >= 3)
			{
				if(carte == null && id == Short.parseShort(plugin.getCustomConfig().getStringList(s).get(0)))
				{
					//joueur.sendMessage("Suppression de la map dans fichier conf");
					plugin.getCustomConfig().set(s, null);
					plugin.saveCustomConfig();
					File map = new File("./plugins/ImageOnMap/Image/" + s + ".png");
					boolean isDeleted = map.delete();
					//joueur.sendMessage("The picture have been deleted");
					
					if(isDeleted)
						return true;
					else
					{
						plugin.getLogger().warning("Picture "+ s+ ".png cannot be deleted !");
						return false;
					}
				}
				
				else if(id == Short.parseShort(plugin.getCustomConfig().getStringList(s).get(0)))
				{
					//joueur.sendMessage("Suppression de la map dans fichier conf + fichier dat");
					SingleMap.SupprRendu(carte);
					/*if(plugin.getConfig().get("delete") != null);
					{
						ArrayList<String> ListeSuppr = (ArrayList<String>) plugin.getConfig().getStringList("delete");
						ListeSuppr.add(plugin.getCustomConfig().getStringList(s).get(0));
						plugin.getConfig().set("delete", ListeSuppr);
					}*/
					plugin.getCustomConfig().set(s, null);
					plugin.saveCustomConfig();
					plugin.saveConfig();
					File map = new File("./plugins/ImageOnMap/Image/" + s + ".png");
					boolean isDeleted = map.delete();
					//joueur.sendMessage("DEBUG: booléen isDeleted :"+ isDeleted+ "; Nom de la map : "+ plugin.getServer().getWorlds().get(0).getName());
					//joueur.sendMessage("The map has been deleted");
					if(isDeleted)
						return true;
					else
					{
						plugin.getLogger().warning("Picture "+ s+ ".png cannot be deleted !");
						return false;
					}
						
				}
			}
		}
		
		//plugin.getLogger().info("No map with id"+ id+ " was found");
		return false;
	}
	
	static ArrayList<String> getListMapByPlayer(ImageOnMap plugin, String pseudo)
	{
		ArrayList<String> listeMap = new ArrayList<String>();
		Set<String> cle = plugin.getCustomConfig().getKeys(false);
		for (String s: cle)
		{
			if(plugin.getCustomConfig().getStringList(s).size() >= 3 && pseudo.equalsIgnoreCase(plugin.getCustomConfig().getStringList(s).get(2)))
			{
				listeMap.add(plugin.getCustomConfig().getStringList(s).get(0));
			}
		}
		return listeMap;
	}
	
	static void AddMap(ItemStack map, Inventory inv, ArrayList<ItemStack> restant)
	{
		HashMap<Integer,ItemStack> reste = inv.addItem(map);
		
		if(!reste.isEmpty())
		{
			restant.add(reste.get(0));
		}
	}
        
        public static BufferedImage scaleImage(Image image, int width, int height)
        {
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = newImage.createGraphics();
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();
            return newImage;
        }
}