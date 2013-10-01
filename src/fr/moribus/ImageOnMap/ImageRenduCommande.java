package fr.moribus.ImageOnMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

public class ImageRenduCommande implements CommandExecutor
{

	Player joueur;
	ImageOnMap ca;
	boolean imgSvg;
	
	public ImageRenduCommande(ImageOnMap plugin)
	{
		ca = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] arg3) 
	{
		// On vérifie si celui qui exécute la commande est bien un joueur
		if (!ImgUtility.VerifierIdentite(sender))
			return false;
		
		joueur = (Player) sender;
		
		if(!joueur.hasPermission("imageonmap.userender") || !joueur.isOp())
		{
			joueur.sendMessage("You are not allowed to use this command ( " + arg1.getName() + " )!");
			return false;
		}

			
		if(joueur.getItemInHand().getType() != Material.MAP)
		{
			joueur.sendMessage(ChatColor.RED + "Vous devez tenir une map en main !!");
			return false;
		}
		
		if (arg3.length < 1)
		{
			joueur.sendMessage(ChatColor.RED + "You must enter image url");
		    return false;
		}
		
		imgSvg = true;
		// On crée une map
		Rendu ren = new Rendu(arg3[0], imgSvg, ca);
		MapView carte = Bukkit.getMap(joueur.getItemInHand().getDurability());
		Rendu.SupprRendu(carte);
		carte.addRenderer(ren);
		joueur.setItemInHand(new ItemStack(Material.MAP, 1, carte.getId()));
		
		return true;
	}

}