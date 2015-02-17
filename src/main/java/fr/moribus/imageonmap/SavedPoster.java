package fr.moribus.imageonmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SavedPoster
{
    ImageOnMap plugin;
    short[] ids;
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    String posterName, playerName;
    
    public SavedPoster(ImageOnMap plugin, short[] ids, String playerName, String posterName)
    {
        this.ids = ids;
        this.plugin = plugin;
        this.playerName = playerName;
        this.posterName = posterName;
    }
    
    public SavedPoster(ImageOnMap plugin, short[] ids, String playerName)
    {
        this(plugin, ids, playerName, null);
    }
    
    public SavedPoster(ImageOnMap plugin)
    {
        this(plugin, null, null, null);
    }

    public SavedPoster(ImageOnMap p, String id)
    {
        plugin = p;
        posterName = id;
        ArrayList<String> liste = (ArrayList<String>) getCustomConfig().getStringList(posterName);
        if (!liste.isEmpty() || liste != null)
        {
            playerName = liste.get(0);
            ids = new short[liste.size() - 1];
            for (int i = 0; i < ids.length; i++)
            {
                ids[i] = Short.parseShort(liste.get(i + 1));
            }
        }
    }

    public String Save() throws IOException
    {
        int increment = increment();
        ArrayList<String> liste = new ArrayList<String>();
        liste.add(playerName);
        for (int i = 0; i < ids.length; i++)
        {
            liste.add(String.valueOf(ids[i]));
        }
        posterName = "poster" + increment;
        getCustomConfig().set(posterName, liste);
        saveCustomConfig();
        return posterName;
    }

    public void Remove() throws IOException
    {
        if (posterName == null || posterName.isEmpty()) return;
        for (int i = 0; i < ids.length; i++)
        {
            ImgUtility.RemoveMap(plugin, ids[i]);
        }
        getCustomConfig().set(posterName, null);
        saveCustomConfig();
    }

    int increment()
    {
        int i;
        if (getCustomConfig().get("IdCount") != null)
        {
            i = getCustomConfig().getInt("IdCount");
        }
        else
        {
            i = 0;
        }
        i++;
        this.getCustomConfig().set("IdCount", i);
        return i;
    }

    String getId()
    {
        return posterName;
    }

    public ArrayList<String> getListMapByPlayer(ImageOnMap plugin, String pseudo)
    {
        ArrayList<String> listeMap = new ArrayList<String>();
        Set<String> cle = getCustomConfig().getKeys(false);
        for (String s : cle)
        {
            if (getCustomConfig().getStringList(s).size() > 1 && pseudo.equalsIgnoreCase(getCustomConfig().getStringList(s).get(0)))
            {
                listeMap.add(s);
            }
        }
        return listeMap;
    }

    /* Méthodes pour charger / recharger / sauvegarder
     * le fichier conf des Posters (poster.yml).
     * Je les ai juste copié depuis un tuto du wiki Bukkit...
     */
    @SuppressWarnings("deprecation")
    public void reloadCustomConfig()
    {
        if (customConfigFile == null)
        {
            customConfigFile = new File(plugin.getDataFolder(), "poster.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("poster.yml");
        if (defConfigStream != null)
        {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getCustomConfig()
    {
        if (customConfig == null)
        {
            reloadCustomConfig();
        }
        return customConfig;
    }

    public void saveCustomConfig() throws IOException
    {
        if (customConfig == null || customConfigFile == null)
        {
            return;
        }
        getCustomConfig().save(customConfigFile);
    }

}
