package fr.moribus.imageonmap.map;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;

import fr.moribus.imageonmap.ImageOnMap;

public class MapData
{
    private final short id;
    private String playerName;
    private String worldName;
    private String name;
    private BufferedImage image;
    
    public MapData(short id, String playerName, BufferedImage image, String worldName, String name)
    {
        this.id = id;
        this.playerName = playerName;
        this.image = image;
        this.worldName = worldName;
        this.name = name;
    }
    
    public MapData(short id)
    {
        this(id, null, null, null, null);
    }

    public MapData(short id, String playerName, BufferedImage image, String worldName)
    {
        this(id, playerName, image, worldName, null);
    }

    public Image getImage()
    {
        return image;
    }

    public void setImage(BufferedImage image)
    {
        this.image = image;
    }

    public void save() throws IOException
    {
        String nomImg = "map" + id;
        ImageOnMap plugin = (ImageOnMap) Bukkit.getPluginManager().getPlugin("ImageOnMap");
        
        File outputfile = new File("./plugins/ImageOnMap/Image/" + nomImg + ".png");
        ImageIO.write(image, "png", outputfile);

        // Enregistrement de la map dans la config
        ArrayList<String> liste = new ArrayList<String>();
        liste.add(String.valueOf(id));
        liste.add(nomImg);
        liste.add(playerName);
        liste.add(worldName);
        plugin.getCustomConfig().set(nomImg, liste);
        plugin.saveCustomConfig();
    }

    public void load() throws IOException
    {
        ImageOnMap plugin = (ImageOnMap) Bukkit.getPluginManager().getPlugin("ImageOnMap");
        List<String> svg = plugin.getCustomConfig().getStringList("map" + id);
        String nomImg = svg.get(1);
        playerName = svg.get(2);
        image = ImageIO.read(new File("./plugins/ImageOnMap/Image/" + nomImg + ".png"));
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
