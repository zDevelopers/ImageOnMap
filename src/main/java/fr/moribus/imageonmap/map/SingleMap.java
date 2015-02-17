package fr.moribus.imageonmap.map;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.ImgUtility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.map.MapView;

import fr.moribus.imageonmap.image.Renderer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.bukkit.map.MapRenderer;

public class SingleMap extends ImageMap
{
    private final short mapID;
    private BufferedImage image;

    public SingleMap(BufferedImage img, Player player)
    {
        this(img, null, player);
    }

    public SingleMap(BufferedImage image, String imageName, Player player)
    {
        super(imageName, player.getName(), player.getWorld().getName());
        this.mapID = Bukkit.createMap(player.getWorld()).getId();
        this.image = ImgUtility.scaleImage(image, WIDTH, HEIGHT);
    }

    public SingleMap(short mapID) throws IOException, IllegalArgumentException
    {
        this.mapID = mapID;

        //Testing if the map id exists
        MapView map = Bukkit.getMap(mapID);
        if(map == null) 
            throw new IllegalArgumentException("Map ID '" + mapID + "' doesn't exist");
        
        List<String> svg = ImageOnMap.getPlugin().getCustomConfig().getStringList("map" + mapID);
        String nomImg = svg.get(1);
        ownerName = svg.get(2);
        image = ImageIO.read(new File("./plugins/ImageOnMap/Image/" + nomImg + ".png"));
    }

    @Override
    public void save() throws IOException
    {
        String mapName = "map" + mapID;
        ImageOnMap plugin = ImageOnMap.getPlugin();
        
        File outputfile = new File("./plugins/ImageOnMap/Image/" + mapName + ".png");
        ImageIO.write(image, "png", outputfile);

        // Enregistrement de la map dans la config
        ArrayList<String> liste = new ArrayList<String>();
        liste.add(String.valueOf(mapID));
        liste.add(mapName);
        liste.add(ownerName);
        liste.add(worldName);
        plugin.getCustomConfig().set(mapName, liste);
        plugin.saveCustomConfig();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void give(Inventory inventory)
    {
        give(inventory, mapID);
    }

    @Override
    public void load()
    {
        MapView map = Bukkit.getMap(mapID);
        SingleMap.SupprRendu(map);
        map.addRenderer(new Renderer(image));
    }

    @Override
    public boolean isNamed()
    {
        return imageName != null;
    }

    public short getId()
    {
        return mapID;
    }

    @Override
    public void setImage(BufferedImage image)
    {
        this.image = image;
        load();
    }

    @Override
    public void send(Player joueur)
    {
        joueur.sendMap(Bukkit.getMap(mapID));
    }

    public static void SupprRendu(MapView map)
    {
        for(MapRenderer renderer : map.getRenderers())
        {
            map.removeRenderer(renderer);
        }
    }

}
