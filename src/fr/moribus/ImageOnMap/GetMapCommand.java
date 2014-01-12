package fr.moribus.ImageOnMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

public class GetMapCommand implements CommandExecutor
{
	short id;
	ImageOnMap plugin;
	MapView map;
	Player joueur;
	Inventory inv;
	
	GetMapCommand(ImageOnMap p)
	{
		plugin = p;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) 
	{
		
		if(!ImgUtility.VerifierIdentite(sender))
			return false;
		
		joueur = (Player) sender;
		inv = (Inventory) joueur.getInventory();
		
		try
		{
			id = Short.parseShort(arg3[0]);
		}
		catch(NumberFormatException err)
		{
			joueur.sendMessage("you must enter a number !");
			return false;
		}
		
		if(!ImgUtility.EstDansFichier(plugin, id))
		{
			joueur.sendMessage("The given id does not match any map !");
			return false;
		}
		
		if(inv.firstEmpty() == -1)
		{
			joueur.sendMessage("Your inventory is full, you can't take the map !");
			return false;
		}
		
		map = Bukkit.getMap(id);
		if(map == null)
			joueur.sendMessage("An eroor occured while getting map by ID !");
		
		inv.addItem(new ItemStack(Material.MAP, 1, map.getId()));
		joueur.sendMessage("Map "+ ChatColor.ITALIC+ id+ ChatColor.RESET+ " was added in your inventory.");
		
		return true;
	}

}
