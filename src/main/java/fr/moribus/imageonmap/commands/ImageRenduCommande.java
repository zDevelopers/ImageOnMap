package fr.moribus.imageonmap.commands;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.ImgUtility;
import fr.moribus.imageonmap.TacheTraitementMap;
import fr.moribus.imageonmap.TacheTraitementNouvelleMap;
import fr.moribus.imageonmap.map.ImageMap;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.net.MalformedURLException;
import java.net.URL;

public class ImageRenduCommande implements CommandExecutor
{
    Player joueur;
    boolean renderName, imgSvg;
    ImageOnMap plugin;
    boolean resize, rename;

    public ImageRenduCommande(ImageOnMap p)
    {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        // On vérifie si celui qui exécute la commande est bien un joueur
        if (!ImgUtility.VerifierIdentite(sender))
        {
            return false;
        }

        joueur = (Player) sender;
        resize = false;
        rename = true;

        if (!joueur.hasPermission("imageonmap.userender"))
        {
            joueur.sendMessage("You are not allowed to use this command ( " + cmd.getName() + " )!");
            return false;
        }

        if (args.length < 1)
        {
            joueur.sendMessage(ChatColor.RED + "You must enter image url.");
            return false;
        }

        URL url;
        try
        {
            url = new URL(args[0]);
        }
        catch (MalformedURLException ex)
        {
            joueur.sendMessage("§cInvalid URL.");
            return false;
        }

        ImageMap.Type type = ImageMap.Type.SINGLE;
        
        if (args.length >= 2)
        {
            try
            {
                type = Enum.valueOf(ImageMap.Type.class, args[1]);
            }
            catch (IllegalArgumentException ex)
            {
                joueur.sendMessage("Specified map type doesn't exist");
            }

        }

        TacheTraitementMap tache = new TacheTraitementNouvelleMap(joueur, url, type, resize, rename);
        tache.runTaskTimer(plugin, 0, 5);

        return true;
    }

}
