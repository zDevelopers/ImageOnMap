package fr.moribus.imageonmap;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.map.SingleMap;

public class MapToolCommand implements CommandExecutor
{
	short id;
	ImageOnMap plugin;
	MapView map;
	Player joueur;
	Inventory inv;
	
	MapToolCommand(ImageOnMap p)
	{
		plugin = p;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) 
	{
		
		if(!ImgUtility.VerifierIdentite(sender))
			return false;
		
		String nomCmd = arg2;
		joueur = (Player) sender;
		inv = joueur.getInventory();
		
		if(arg3.length < 1)
		{
			joueur.sendMessage("Map tools usage:" +
					"\n/"+ ChatColor.GOLD + nomCmd+ ChatColor.RESET+ " get [id]: get the map corresponding to this id" +
					"\n/"+ ChatColor.GOLD + nomCmd+ ChatColor.RESET+ " delete [id]: remove the map corresponding to this id" +
					"\n/"+ ChatColor.GOLD + nomCmd+ ChatColor.RESET+ " list: show all ids of maps in your possession");
			return true;
		}
		
		if(arg3[0].equalsIgnoreCase("get"))
		{
			try
			{
				id = Short.parseShort(arg3[1]);
			}
			catch(NumberFormatException err)
			{
				joueur.sendMessage("you must enter a number !");
				return true;
			}
			
			SingleMap smap;
			try
			{
				smap = new SingleMap(id);
				
				if(!smap.load())
				{
					if(joueur.isOp())
						joueur.sendMessage(ChatColor.RED+ "Can't retrieve the map ! Check if map"+ id+ " exists in your maps.yml or if the dat file in the world folder exists");
					else
						joueur.sendMessage(ChatColor.RED+ "ERROR: This map doesn't exists");
					return true;
				}
				else
				{
					if(inv.firstEmpty() == -1)
					{
						joueur.sendMessage("Your inventory is full, you can't take the map !");
						return true;
					}
					
					smap.give(joueur.getInventory());
					joueur.sendMessage("Map "+ ChatColor.ITALIC+ id+ ChatColor.RESET+ " was added in your inventory.");
				}
			}
			catch (Exception e)
			{
				joueur.sendMessage(ChatColor.RED+ "ERROR while loading maps");
			}
			
			
			
			
			return true;
		}
		
		else if(arg3[0].equalsIgnoreCase("set"))
		{
			ImageMap smap;
			try
			{
				if(arg3[1].startsWith("poster"))
				{
					smap = new PosterMap(arg3[1]);
				}
				else
				{
					id = Short.parseShort(arg3[1]);
					smap = new SingleMap(id);
				}
			}
			catch(NumberFormatException err)
			{
				joueur.sendMessage("you must enter a number !");
				return true;
			} catch (Exception e)
			{
				e.printStackTrace();
				joueur.sendMessage(ChatColor.RED+ "ERROR while loading maps");
				return true;
			}
			
			
			if(!arg3[2].startsWith("http"))
			{
				joueur.sendMessage("You must enter a valid URL.");
				return true;
			}
			else if(arg3[2].startsWith("https"))
			{
				joueur.sendMessage("WARNING: you have entered a secure HTTP link, ImageOnMap can't guarantee " +
						"that the image is downloadable");
				return true;
			}
			
			
			TacheTraitementMajMap tache = new TacheTraitementMajMap(smap, arg3[2], joueur);
			tache.runTaskTimer(plugin, 0, 5);
			
			
			
			
			return true;
		}
		
		else if(arg3[0].equalsIgnoreCase("delete"))
		{
			if(!joueur.hasPermission("imageonmap.usermmap"))
			{
				joueur.sendMessage("You are not allowed to delete map !");
				return true;
			}
			
			if(arg3.length == 2 && arg3[1].startsWith("poster"))
			{
				SavedPoster poster = new SavedPoster(plugin, arg3[1]);
				boolean suppr = poster.Remove();
				if(!suppr)
					joueur.sendMessage("Unable to remove the entire poster, check the server log for more information");
				return true;
			}
			
			if(arg3.length <= 1)
			{
				if(joueur.getItemInHand().getType() == Material.MAP)
				{
					id = joueur.getItemInHand().getDurability();
				}
				else
				{
					joueur.sendMessage(ChatColor.RED+ "You must hold a map or enter an id");
				}
			}
			else
			{
				try
				{
					id = Short.parseShort(arg3[1]);
				}
				catch(NumberFormatException err)
				{
					joueur.sendMessage("you must enter a number !");
					return true;
				}
			}
			
			boolean success = ImgUtility.RemoveMap(plugin, id);
			
			if(success)
			{
				joueur.sendMessage("Map#"+ id+ " was deleted");
				return true;
			}
			else
			{
				joueur.sendMessage(ChatColor.RED+ "Can't delete delete Map#"+ id+ ": check the server log");
				return true;
			}
		}
		
		else if(arg3[0].equalsIgnoreCase("list"))
		{
			String msg = "", msg2 = "";
			int compteur = 0;
			ArrayList<String> liste = new ArrayList<String>();
			
			liste = ImgUtility.getListMapByPlayer(plugin, joueur.getName());
			
			for (; compteur < liste.size(); compteur++)
			{
				msg += liste.get(compteur)+ " ";
			}
			
			SavedPoster tmp = new SavedPoster(plugin);
			ArrayList<String> listePoster = tmp.getListMapByPlayer(plugin, joueur.getName());
			for (int i= 0; i< listePoster.size(); i++)
			{
				msg2 += listePoster.get(i)+ " ";
			}
			joueur.sendMessage(msg+ 
					"\nYou have rendered "+ ChatColor.DARK_PURPLE+ (compteur + 1)+ ChatColor.RESET+ " pictures");
			joueur.sendMessage("Your posters: \n"+ msg2);
			
		}
		
		else if(arg3[0].equalsIgnoreCase("getrest"))
		{
			if(plugin.getRemainingMaps(joueur.getName()) == null)
			{
				joueur.sendMessage("All maps have already be placed in your inventory");
				return true;
			}
			ArrayList<ItemStack> reste = plugin.getRemainingMaps(joueur.getName());
			ArrayList<ItemStack> restant = new ArrayList<ItemStack>();
			for(int i = 0; i < reste.size(); i++)
			{
				ImgUtility.AddMap(reste.get(i), inv, restant);
			}
			if(restant.isEmpty())
			{
				plugin.removeRemaingMaps(joueur.getName());
				joueur.sendMessage("All maps have been placed in your inventory");
			}
			else
			{
				plugin.setRemainingMaps(joueur.getName(), restant);
				joueur.sendMessage(restant.size()+ " maps can't be placed in your inventory. Please run "+ ChatColor.GOLD+ "/maptool getrest again");
			}
		}
		
		return true;
	}

}
