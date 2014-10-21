package fr.moribus.ImageOnMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import fr.moribus.ImageOnMap.Map.ImageMap;
import fr.moribus.ImageOnMap.Map.SingleMap;

public final class ImageOnMap extends JavaPlugin
{
	private boolean dossierCree;
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    /* liste contenant les maps ne pouvant être placé dans l'inventaire du joueur. Je le fous ici afin que ce soit
     accessible de partout dans le plugin. */
    private HashMap<String, ArrayList<ItemStack>> cache = new HashMap<String, ArrayList<ItemStack>>();
    
    // Index des maps chargées sur le serveur
    public ArrayList<Short> mapChargee = new ArrayList<Short>();
    
	@Override
	public void onEnable()
	{
		// On crée si besoin le dossier où les images seront stockées
		dossierCree = ImgUtility.CreeRepImg(this);
		
		// On ajoute si besoin les params par défaut du plugin
		ImgUtility.CreeSectionConfig(this);
		if(getConfig().get("map_path") == null)
			getConfig().set("map_path", getServer().getWorlds().get(0).getName());
		else if(getConfig().get("map_path") != getServer().getWorlds().get(0).getName())
			getConfig().set("map_path", getServer().getWorlds().get(0).getName());
		
		if(getConfig().getBoolean("import-maps"))
			ImgUtility.ImporterConfig(this);
		
		if(this.getConfig().getBoolean("collect-data"))
		{
			try 
			{
				MetricsLite metrics = new MetricsLite(this);
				metrics.start();
				getLogger().info("Metrics launched for ImageOnMap");
			} catch (IOException e) 
			{
				// Failed to submit the stats :-(
			}
		}
		
		if(dossierCree)
		{
			getCommand("tomap").setExecutor(new ImageRenduCommande(this));
			getCommand("maptool").setExecutor(new MapToolCommand(this));
			getServer().getPluginManager().registerEvents(new SendMapOnFrameEvent(this), this);
			getServer().getPluginManager().registerEvents(new SendMapOnInvEvent(this), this);
			this.saveDefaultConfig();
			//ChargerMap();
		}
		else
		{
			getLogger().info("[ImageOnMap] An error occured ! Unable to create Image folder. Plugin will NOT work !");
			this.setEnabled(false);
		}
		
		// Disable all sound effects
		ProtocolLibrary.getProtocolManager().addPacketListener(
		  new PacketAdapter(this, ListenerPriority.NORMAL, 
		          PacketType.Play.Server.SPAWN_ENTITY_LIVING)
		  {
			@Override
		    public void onPacketSending(PacketEvent event)
		    {
		    	event.getPacket().getIntegers().write(1, (int) EntityType.BLAZE.getTypeId());
		    }
		});
		
	}
	
	@Override
	public void onDisable()
	{
		getLogger().info("Stopping ImageOnMap");
	}

	public void ChargerMap()
	{
		Set<String> cle = getCustomConfig().getKeys(false);
		int nbMap = 0, nbErr = 0;
		for (String s: cle)
		{
			if(getCustomConfig().getStringList(s).size() >= 3)
			{
				ImageMap map;
				try
				{
					map = new SingleMap(Short.valueOf(getCustomConfig().getStringList(s).get(0)));
					
					if(map.load())
						nbMap++;
					else
						nbErr++;
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
					nbErr++;
				}
				catch (Exception e)
				{
					nbErr++;
				}
			}
			
		}
		getLogger().info(nbMap +" maps was loaded");
		if(nbErr != 0)
			getLogger().info(nbErr +" maps can't be loaded");
	}
	
	/* Méthodes pour charger / recharger / sauvegarder
	 * le fichier conf des maps (map.yml).
	 * Je les ai juste copié depuis un tuto du wiki Bukkit.
	 */
    @SuppressWarnings("deprecation")
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
    
    public ArrayList<ItemStack> getRemainingMaps(String j)
    {
    	return cache.get(j);
    }
    
    public void setRemainingMaps(String j, ArrayList<ItemStack> remaining)
    {
    	cache.put(j, remaining);
    }
    
    public void removeRemaingMaps(String j)
    {
    	cache.remove(j);
    }
}
