package fr.moribus.ImageOnMap.Map;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.moribus.ImageOnMap.ImageOnMap;

public class PosterData
{
	private String joueur;
	private short[] idMaps;
	private String nomPoster;
	
	private FileConfiguration customConfig = null;
    private File customConfigFile = null;
	
	public PosterData(String joueur, short[] ids)
	{
		setJoueur(joueur);
		setIdMaps(ids);
		nomPoster = "poster"+ increment();
	}
	
	public PosterData(String id) throws Exception
	{
		load(id);
	}
	
	public String getJoueur()
	{
		return joueur;
	}

	private void setJoueur(String joueur)
	{
		this.joueur = joueur;
	}

	public short[] getIdMaps()
	{
		return idMaps;
	}

	private void setIdMaps(short[] idMaps)
	{
		this.idMaps = idMaps;
	}
	
	private void load(String id) throws Exception
	{
		List<String> svg = getCustomConfig().getStringList(id);
		if(svg != null && !svg.isEmpty())
		{
			nomPoster = id;
			setJoueur(svg.get(0));
			
			try
			{
				short[] ids = new short[svg.size()-1];
				for(int i = 0; i < ids.length; i++)
				{
					ids[i] = Short.parseShort(svg.get(i+1));
				}
				setIdMaps(ids);
			}
			catch(NumberFormatException e)
			{
				throw e;
			}
		}
		else
		{
			throw new Exception("Le poster est introuvable.");
		}
		
	}
	
	public void save()
	{
		ArrayList<String> liste = new ArrayList<String>();
		liste.add(joueur);
		for(int i= 0; i< idMaps.length; i++)
		{
			liste.add(String.valueOf(idMaps[i]));
		}
		getCustomConfig().set(nomPoster, liste);
		saveCustomConfig();
}
	
	private int increment()
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
	
	/* Méthodes pour charger / recharger / sauvegarder
	 * le fichier conf des Posters (poster.yml).
	 * Je les ai juste copié depuis un tuto du wiki Bukkit.
	 */
    @SuppressWarnings("deprecation")
	public void reloadCustomConfig() 
    {
    	ImageOnMap plugin = (ImageOnMap)Bukkit.getPluginManager().getPlugin("ImageOnMap");
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
    	ImageOnMap plugin = (ImageOnMap)Bukkit.getPluginManager().getPlugin("ImageOnMap");
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
