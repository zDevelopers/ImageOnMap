package fr.moribus.ImageOnMap;

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

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) 
	{
		
		if(!ImgUtility.VerifierIdentite(sender))
			return false;
		
		String nomCmd = arg2;
		joueur = (Player) sender;
		inv = (Inventory) joueur.getInventory();
		
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
			
			map = ImgUtility.getMap(plugin, id);
			
			if(map == null)
			{
				if(joueur.isOp())
					joueur.sendMessage(ChatColor.RED+ "Can't retrieve the map ! Check if map"+ id+ " exists in your maps.yml or if the dat file in the world folder exists");
				else
					joueur.sendMessage(ChatColor.RED+ "ERROR: This map doesn't exists");
				return true;
			}
			
			if(inv.firstEmpty() == -1)
			{
				joueur.sendMessage("Your inventory is full, you can't take the map !");
				return true;
			}
			
			inv.addItem(new ItemStack(Material.MAP, 1, map.getId()));
			joueur.sendMap(map);
			joueur.sendMessage("Map "+ ChatColor.ITALIC+ id+ ChatColor.RESET+ " was added in your inventory.");
			
			return true;
		}
		
		else if(arg3[0].equalsIgnoreCase("delete"))
		{
			if(!joueur.hasPermission("imageonmap.usermmap"))
			{
				joueur.sendMessage("You are not allowed to delete map !");
				return true;
			}
			
			try
			{
				id = Short.parseShort(arg3[1]);
			}
			catch(NumberFormatException err)
			{
				joueur.sendMessage("you must enter a number !");
				return true;
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
			String msg = "";
			int compteur = 0;
			ArrayList<String> liste = new ArrayList<String>();
			
			liste = ImgUtility.getListMapByPlayer(plugin, joueur.getName());
			
			for (; compteur < liste.size(); compteur++)
			{
				msg += liste.get(compteur)+ " ";
			}
			joueur.sendMessage(msg+ 
					"\nYou have rendered "+ ChatColor.DARK_PURPLE+ (compteur + 1)+ ChatColor.RESET+ " pictures");
		}
		
		return true;
	}

}
