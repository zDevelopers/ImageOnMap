package fr.moribus.ImageOnMap;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;

public class ImageCommande implements CommandExecutor
{

	Player joueur;
	Plugin ca;
	boolean imgSvg;
	
	public ImageCommande(Plugin plugin)
	{
		ca = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] arg3) 
	{
		// On vérifie si celui qui exécute la commande est bien un joueur
		if (sender instanceof Player)
			joueur = (Player) sender;
		else if (sender instanceof ConsoleCommandSender)
			{System.out.println(ChatColor.RED + "Cette commande ne peut être utilisée dans la console !"); return true;}
		else if (sender instanceof BlockCommandSender)
			{System.out.println(ChatColor.RED + "Cette commande ne peut être utilisée par un bloc-commande !"); return true;}
		else
			{System.out.println(ChatColor.RED + "Cette commande ne peut être lancée de cette façon !"); return true;}
		
		if (arg3.length < 2)
		{
			joueur.sendMessage("You must enter image url, and true if you want image to be persistant (false otherwise)");
		    return false;
		}
		
		if (arg3[1].contains("true"))
			imgSvg = true;
		else
			imgSvg = false;
		
		// On crée une map
		Rendu ren = new Rendu(arg3[0], imgSvg);
		MapView carte = Bukkit.createMap(joueur.getWorld());
		Rendu.SupprRendu(carte);
		carte.addRenderer(ren);
		joueur.setItemInHand(new ItemStack(Material.MAP, 1, carte.getId()));
		
		if (imgSvg)
		{
			SvgMap(carte.getId(), "map" + carte.getId(), joueur.getName());
		}
		
		return true;
	}

	void SvgMap(int IdMap, String nomImage, String nomJoueur)
	{
		System.out.println("Sauvegarde de la map..");
		ArrayList<String> liste = new ArrayList<String>();
		liste.add(String.valueOf(IdMap));
		liste.add(nomImage);
		liste.add(nomJoueur);
		ca.getConfig().set("map" + IdMap, liste);
		ca.saveConfig();
	}

}