package fr.moribus.ImageOnMap;

import java.io.File;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

public class ImageSupprCommande implements CommandExecutor
{

	Plugin plugin;
	Player joueur;
	MapView carte;
	
	public ImageSupprCommande(Plugin p)
	{
		plugin = p;
	}
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
			String[] arg3)
	{
		if (!ImgUtility.VerifierIdentite(arg0))
			return false;
		joueur = (Player) arg0;
		
		if(!joueur.hasPermission("imageonmap.usermmap"))
		{
			joueur.sendMessage("You are not allowed to use this command ( " + arg1.getName() + " )!");
			return false;
		}
		
		if(joueur.getItemInHand().getType() != Material.MAP)
		{
			joueur.sendMessage(ChatColor.RED + "you're not holding a map !");
			return false;
		}
		
		carte = Bukkit.getMap(joueur.getItemInHand().getDurability());
		
		Set<String> cle = plugin.getConfig().getKeys(false);
		for (String s: cle)
		{
			if(plugin.getConfig().getStringList(s).size() >= 3)
			{
				if(carte.getId() == Short.parseShort(plugin.getConfig().getStringList(s).get(0)))
				{
					Rendu.SupprRendu(carte);
					plugin.getConfig().set(s, null);
					plugin.saveConfig();
					new File("./plugins/ImageOnMap/" + s + ".png").delete();
					joueur.sendMessage("The picture have been deleted");
					return true;
				}
			}
		}
		
		return false;
	}

}