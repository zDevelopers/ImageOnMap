package fr.moribus.imageonmap.map;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.image.Renderer;
import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.moribus.imageonmap.image.PosterImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.map.MapView;

public class PosterMap extends ImageMap
{
    private PosterImage image;
    private final short[] mapsIDs;
    
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    public PosterMap(PosterImage image, Player player)
    {
        super(null, player.getName(), player.getWorld().getName());
        this.image = image;

        mapsIDs = new short[image.getImagesCount()];
        for (int i = 0; i < mapsIDs.length; i++)
        {
            mapsIDs[i] = Bukkit.createMap(player.getWorld()).getId();
        }
    }

    public PosterMap(String id) throws Exception
    {
        this.imageName = id;
        List<String> svg = getCustomConfig().getStringList(imageName);
        if(svg != null && !svg.isEmpty())
        {
                this.ownerName = svg.get(0);
                mapsIDs = new short[svg.size()-1];
                for(int i = 0; i < mapsIDs.length; i++)
                {
                        mapsIDs[i] = Short.parseShort(svg.get(i+1));
                }
        }
        else
        {
                throw new Exception("Le poster est introuvable.");
        }
    }

    @Override
    public void load() 
    {
        for(int i = 0; i < mapsIDs.length; i++)
        {
            MapView map = Bukkit.getMap(mapsIDs[i]);
            SingleMap.SupprRendu(map);
            map.addRenderer(new Renderer(image.getImageAt(i)));
        }
    }

    @Override
    public void save() throws IOException
    {
        ImageOnMap plugin = ImageOnMap.getPlugin();
        
        for(int i = 0; i < mapsIDs.length; i++)
        {
            short mapID = mapsIDs[i];
            String mapName = "map" + mapID;
            File outputfile = new File("./plugins/ImageOnMap/Image/" + mapName + ".png");
            ImageIO.write(image.getImageAt(i), "png", outputfile);

            // Enregistrement de la map dans la config
            ArrayList<String> liste = new ArrayList<String>();
            liste.add(String.valueOf(mapID));
            liste.add(mapName);
            liste.add(ownerName);
            liste.add(worldName);
            plugin.getCustomConfig().set(mapName, liste);
        }
       
        plugin.saveCustomConfig();
    }

    @Override
    public void give(Inventory inv)
    {
        for(short mapID : mapsIDs)
        {
            give(inv, mapID);
        }
    }

    @Override
    public void setImage(BufferedImage image)
    {
        
    }

    @Override
    public void send(Player player)
    {
        for(short mapID: mapsIDs)
        {
            player.sendMap(Bukkit.getMap(mapID));
        }
    }
    
    private void reloadCustomConfig() 
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
    
    private FileConfiguration getCustomConfig() 
    {
        if (customConfig == null) 
        {
            reloadCustomConfig();
        }
        return customConfig;
    }
    
    private void saveCustomConfig() throws IOException
    {
    	ImageOnMap plugin = (ImageOnMap)Bukkit.getPluginManager().getPlugin("ImageOnMap");
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        getCustomConfig().save(customConfigFile);
    }

}
