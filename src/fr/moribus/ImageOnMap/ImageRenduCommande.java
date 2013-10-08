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
	boolean renderName, imgSvg;
	ImageOnMap ca;
	int nbMapServeur, nbMapJoueur;
	
	public ImageRenduCommande(ImageOnMap plugin)
	{
		ca = plugin;
		nbMapServeur = ca.getConfig().getInt("Limit-map-by-server");
		nbMapJoueur = ca.getConfig().getInt("Limit-map-by-player");
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
		
		if(joueur.hasPermission("imageonmap.userender"))
		{
			
		}
		else
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
		
		if (arg3.length == 2)
		{
			if (arg3[1].contains("true"))
				renderName = true;
			else
				renderName = false;
		}
		else
			renderName = false;
		
		// on crée une carte
		MapView carte = Bukkit.getMap(joueur.getItemInHand().getDurability());
		
		// On vérifie que le nombre limite de map par serveur n'a pas été atteint.
		// Si la carte est déjà inscrite dans le fichier (== contient une image), on outrepasse la vérification
		// vu que le nb de map n'augmentera pas.
		// Si la limite = 0, on ignore cette vérification.
		if(!ImgUtility.EstDansFichier(ca, carte.getId()) && nbMapServeur != 0 && ImgUtility.getNombreDeMaps(ca) >= nbMapServeur)
		{
			joueur.sendMessage(ChatColor.RED + "The limit of map's number per server (" + nbMapServeur + ") have been reached !");
			return true;
		}
		
		// Même chose, mais par joueur cette fois
		if(!ImgUtility.EstDansFichier(ca, carte.getId(), joueur.getName()) && nbMapJoueur != 0 && ImgUtility.getNombreDeMapsParJoueur(ca, joueur.getName()) >= nbMapJoueur)
		{
			joueur.sendMessage(ChatColor.RED + "You've reached the limit of maps per player (" + nbMapJoueur + ") ! Please delete or reuse one of your custom map.");
			return true;
		}
		
		imgSvg = true;
		// On ajoute un rendu
		Rendu ren = new Rendu(arg3[0], imgSvg, ca, renderName);
		Rendu.SupprRendu(carte);
		carte.addRenderer(ren);
		joueur.setItemInHand(new ItemStack(Material.MAP, 1, carte.getId()));
		
		return true;
	}

}