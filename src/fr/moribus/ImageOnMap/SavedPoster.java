package fr.moribus.ImageOnMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SavedPoster 
{
	ImageOnMap plugin;
	short[] ids;
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    String PosName, nomJoueur;
	
	SavedPoster(ImageOnMap p, short[] i, String nom)
	{
		ids = i;
		plugin = p;
		nomJoueur = nom;
		PosName = "";
	}
	
	SavedPoster(ImageOnMap p, short[] i, String nom, String n)
	{
		ids = i;
		plugin = p;
		nomJoueur = nom;
		PosName = n;
		
	}
	
	SavedPoster(ImageOnMap p, String id)
	{
		plugin = p;
		PosName = id;
		ArrayList<String> liste = (ArrayList<String>) getCustomConfig().getStringList(PosName);
		if(!liste.isEmpty() || liste != null)
		{
			nomJoueur = liste.get(0);
			ids = new short[liste.size() - 1];
			for(int i= 0; i< ids.length; i++)
			{
				ids[i] = Short.parseShort(liste.get(i+1));
			}
		}
	}
	
	SavedPoster(ImageOnMap p)
	{
		plugin = p;
	}
	
	
	String Save()
	{
			int increment = increment();
			ArrayList<String> liste = new ArrayList<String>();
			liste.add(nomJoueur);
			for(int i= 0; i< ids.length; i++)
			{
				liste.add(String.valueOf(ids[i]));
			}
			PosName = "poster"+ increment;
			getCustomConfig().set(PosName, liste);
			saveCustomConfig();
			return PosName;
	}
	
	boolean Remove()
	{
		if(!PosName.isEmpty())
		{
			for(int i= 0; i< ids.length; i++)
			{
				ImgUtility.RemoveMap(plugin, ids[i]);
			}
			getCustomConfig().set(PosName, null);
			saveCustomConfig();
			return true;
		}
		else
			return false;
		
	}
	
	int increment()
	{
		int i;
		if(getCustomConfig().get("IdCount") != null)
			i = getCustomConfig().getInt("IdCount");
		else
			i = 0;
		i++;
		this.getCustomConfig().set("IdCount", i);
		return i;
	}
	
	String getId()
	{
		return PosName;
	}
	
	ArrayList<String> getListMapByPlayer(ImageOnMap plugin, String pseudo)
	{
		ArrayList<String> listeMap = new ArrayList<String>();
		Set<String> cle = getCustomConfig().getKeys(false);
		for (String s: cle)
		{
			if(getCustomConfig().getStringList(s).size() > 1 && pseudo.equalsIgnoreCase(getCustomConfig().getStringList(s).get(0)))
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
    
    public void saveCustomConfig() 
    {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getCustomConfig().save(customConfigFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }
    
}
