package fr.moribus.ImageOnMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ImageOnMap extends JavaPlugin
{
	int test = 0;
	File dossier;
	boolean dossierCree;
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    
	@Override
	public void onEnable()
	{
		// On crée si besoin le dossier où les images seront stockées
		dossierCree = ImgUtility.CreeRepImg(this);
		
		// On ajoute si besoin les params par défaut du plugin
		ImgUtility.CreeSectionConfig(this);
		
		if(getConfig().getBoolean("import-maps"))
			ImgUtility.ImporterConfig(this);
		
		if(this.getConfig().getBoolean("collect-data"))
		{
			try 
			{
				MetricsLite metrics = new MetricsLite(this);
				metrics.start();
				System.out.println("Metrics launched for ImageOnMap");
			} catch (IOException e) {
				// Failed to submit the stats :-(
			}
		}
		
		if(dossierCree)
		{
			getCommand("tomap").setExecutor(new ImageRenduCommande(this));
			getCommand("maptool").setExecutor(new MapToolCommand(this));
			this.saveDefaultConfig();
			ChargerMap();
		}
		else
		{
			System.out.println("[ImageOnMap] An error occured ! Unable to create Image folder. Plugin will NOT work !");
			this.setEnabled(false);
		}
		
	}
	
	@Override
	public void onDisable()
	{
		System.out.println("Stopping ImageOnMap");
	}

	public void ChargerMap()
	{
		Set<String> cle = getCustomConfig().getKeys(false);
		int nbMap = 0, nbErr = 0;
		for (String s: cle)
		{
			if(getCustomConfig().getStringList(s).size() >= 3)
			{
				SavedMap map = new SavedMap(this, Short.valueOf(getCustomConfig().getStringList(s).get(0)));
				
				if(map.LoadMap())
					nbMap++;
				else
					nbErr++;
			}
			
		}
		System.out.println(nbMap +" maps was loaded");
		if(nbErr != 0)
			System.out.println(nbErr +" maps can't be loaded");
	}
	
	/* Méthodes pour charger / recharger / sauvegarder
	 * le fichier conf des maps (map.yml).
	 * Je les ai juste copié depuis un tuto du wiki Bukkit...
	 */
    public void reloadCustomConfig() 
    {
        if (customConfigFile == null) 
        {
        customConfigFile = new File(getDataFolder(), "map.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = this.getResource("map.yml");
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
            getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

}
