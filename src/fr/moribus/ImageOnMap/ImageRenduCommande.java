package fr.moribus.ImageOnMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ImageRenduCommande implements CommandExecutor
{

	Player joueur;
	boolean renderName, imgSvg;
	ImageOnMap ca;
	
	public ImageRenduCommande(ImageOnMap plugin)
	{
		ca = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] arg3) 
	{
		// On vérifie si celui qui exécute la commande est bien un joueur
		if (!ImgUtility.VerifierIdentite(sender))
			return false;
		
		joueur = (Player) sender;
		
		if(joueur.hasPermission("imageonmap.userender"))
		{
			
		}
		else
		{
			joueur.sendMessage("You are not allowed to use this command ( " + arg1.getName() + " )!");
			return false;
		}
		
		if (arg3.length < 1)
		{
			joueur.sendMessage(ChatColor.RED + "You must enter image url");
		    return false;
		}
		
		TacheTraitementMap tache = new TacheTraitementMap(joueur, arg3[0], ca);
		tache.runTaskTimer(ca, 0, 10);
		
		return true;
	}

}