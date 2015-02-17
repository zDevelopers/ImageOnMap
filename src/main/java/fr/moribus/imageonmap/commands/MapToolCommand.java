package fr.moribus.imageonmap.commands;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.ImgUtility;
import fr.moribus.imageonmap.PluginLogger;
import fr.moribus.imageonmap.SavedPoster;
import fr.moribus.imageonmap.TacheTraitementMajMap;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MapToolCommand implements CommandExecutor
{
    short id;
    ImageOnMap plugin;
    MapView map;
    Player joueur;
    Inventory inv;

    public MapToolCommand(ImageOnMap p)
    {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!ImgUtility.VerifierIdentite(sender))
        {
            return false;
        }

        joueur = (Player) sender;
        inv = joueur.getInventory();

        if (args.length < 1)
        {
            joueur.sendMessage("Map tools usage:"
                + "\n/" + ChatColor.GOLD + label + ChatColor.RESET + " get [id]: get the map corresponding to this id"
                + "\n/" + ChatColor.GOLD + label + ChatColor.RESET + " delete [id]: remove the map corresponding to this id"
                + "\n/" + ChatColor.GOLD + label + ChatColor.RESET + " list: show all ids of maps in your possession");
            return true;
        }

        if (args[0].equalsIgnoreCase("get"))
        {
            try
            {
                id = Short.parseShort(args[1]);
            }
            catch (NumberFormatException err)
            {
                joueur.sendMessage("you must enter a number !");
                return true;
            }

            SingleMap smap;
            try
            {
                smap = new SingleMap(id);
                smap.load();
                if (inv.firstEmpty() == -1)
                {
                    joueur.sendMessage("Your inventory is full, you can't take the map !");
                    return true;
                }

                smap.give(joueur.getInventory());
                joueur.sendMessage("Map " + ChatColor.ITALIC + id + ChatColor.RESET + " was added in your inventory.");
                
            }
            catch (IllegalArgumentException ex)
            {
                joueur.sendMessage(ChatColor.RED + "Invalid argument : " + ex.getMessage());
            }
            catch(IOException ex)
            {
                joueur.sendMessage(ChatColor.RED + "Unable to load the map. Check server console for details.");
                PluginLogger.LogError("Could not load the map", ex);
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("set"))
        {
            ImageMap smap;
            try
            {
                if (args[1].startsWith("poster"))
                {
                    smap = new PosterMap(args[1]);
                }
                else
                {
                    id = Short.parseShort(args[1]);
                    smap = new SingleMap(id);
                }
            }
            catch (NumberFormatException err)
            {
                joueur.sendMessage("you must enter a number !");
                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                joueur.sendMessage(ChatColor.RED + "ERROR while loading maps");
                return true;
            }
            
            if(args.length < 3)
            {
                joueur.sendMessage("§cYou must enter a valid URL.");
                return true;
            }
            
            URL url;
            try 
            {
                url = new URL(args[2]);
            }
            catch (MalformedURLException ex) 
            {
                joueur.sendMessage("§Invalid URL.");
                return true;
            }

            TacheTraitementMajMap tache = new TacheTraitementMajMap(smap, url, joueur);
            tache.runTaskTimer(plugin, 0, 5);

            return true;
        }

        else if (args[0].equalsIgnoreCase("delete"))
        {
            if (!joueur.hasPermission("imageonmap.usermmap"))
            {
                joueur.sendMessage("You are not allowed to delete map !");
                return true;
            }

            if (args.length == 2 && args[1].startsWith("poster"))
            {
                SavedPoster poster = new SavedPoster(plugin, args[1]);
                try
                {
                    poster.Remove();
                }
                catch (IOException ex)
                {
                    joueur.sendMessage("Unable to remove the entire poster, check the server log for more information");
                    PluginLogger.LogError("Unable to remove the entire poster", ex);
                }
                    
                return true;
            }

            if (args.length <= 1)
            {
                if (joueur.getItemInHand().getType() == Material.MAP)
                {
                    id = joueur.getItemInHand().getDurability();
                }
                else
                {
                    joueur.sendMessage(ChatColor.RED + "You must hold a map or enter an id");
                }
            }
            else
            {
                try
                {
                    id = Short.parseShort(args[1]);
                }
                catch (NumberFormatException err)
                {
                    joueur.sendMessage("you must enter a number !");
                    return true;
                }
            }

            boolean success = ImgUtility.RemoveMap(plugin, id);

            if (success)
            {
                joueur.sendMessage("Map#" + id + " was deleted");
                return true;
            }
            else
            {
                joueur.sendMessage(ChatColor.RED + "Can't delete delete Map#" + id + ": check the server log");
                return true;
            }
        }

        else if (args[0].equalsIgnoreCase("list"))
        {
            String msg = "", msg2 = "";
            int compteur = 0;
            ArrayList<String> liste = new ArrayList<String>();

            liste = ImgUtility.getListMapByPlayer(plugin, joueur.getName());

            for (; compteur < liste.size(); compteur++)
            {
                msg += liste.get(compteur) + " ";
            }

            SavedPoster tmp = new SavedPoster(plugin);
            ArrayList<String> listePoster = tmp.getListMapByPlayer(plugin, joueur.getName());
            for (int i = 0; i < listePoster.size(); i++)
            {
                msg2 += listePoster.get(i) + " ";
            }
            joueur.sendMessage(msg
                    + "\nYou have rendered " + ChatColor.DARK_PURPLE + (compteur + 1) + ChatColor.RESET + " pictures");
            joueur.sendMessage("Your posters: \n" + msg2);

        }

        else if (args[0].equalsIgnoreCase("getrest"))
        {
            if (plugin.getRemainingMaps(joueur.getName()) == null)
            {
                joueur.sendMessage("All maps have already be placed in your inventory");
                return true;
            }
            ArrayList<ItemStack> reste = plugin.getRemainingMaps(joueur.getName());
            ArrayList<ItemStack> restant = new ArrayList<ItemStack>();
            for (int i = 0; i < reste.size(); i++)
            {
                ImgUtility.AddMap(reste.get(i), inv, restant);
            }
            if (restant.isEmpty())
            {
                plugin.removeRemaingMaps(joueur.getName());
                joueur.sendMessage("All maps have been placed in your inventory");
            }
            else
            {
                plugin.setRemainingMaps(joueur.getName(), restant);
                joueur.sendMessage(restant.size() + " maps can't be placed in your inventory. Please run " + ChatColor.GOLD + "/maptool getrest again");
            }
        }

        return true;
    }

}
