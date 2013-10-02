package fr.moribus.ImageOnMap;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
		plugin.saveConfig();
		
	}
}