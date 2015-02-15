package fr.moribus.imageonmap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

public class SavedMap
{

    ImageOnMap plugin;
    String nomImg, nomJoueur, nomMonde;
    short idMap;
    BufferedImage image;
    boolean loaded = false;

    SavedMap(ImageOnMap plug, String nomJ, short id, BufferedImage img, String nomM)
    {
        plugin = plug;
        nomJoueur = nomJ;
        idMap = id;
        image = img;
        nomImg = "map" + id;
        nomMonde = nomM;
        loaded = true;
    }

    SavedMap(ImageOnMap plug, short id) throws IOException
    {
        idMap = id;
        plugin = plug;
        Set<String> cle = plugin.getCustomConfig().getKeys(false);
        for (String s : cle)
        {
            if (plugin.getCustomConfig().getStringList(s).size() >= 3 && Short.valueOf(plugin.getCustomConfig().getStringList(s).get(0)) == id)
            {
				//System.out.println(tmp);
                //MapView carte = Bukkit.getMap(Short.parseShort(plugin.getConfig().getStringList(s).get(0)));
                nomImg = plugin.getCustomConfig().getStringList(s).get(1);
                nomJoueur = plugin.getCustomConfig().getStringList(s).get(2);
                image = ImageIO.read(new File("./plugins/ImageOnMap/Image/" + nomImg + ".png"));
                loaded = true;
                break;
            }
        }
        if (!loaded)
        {
            //plugin.getLogger().info("No map was loaded");
        }
    }

    public void saveMap() throws IOException
    {
        if (!loaded)
        {
            PluginLogger.LogWarning("Tried to save a map that wasn't loaded. ID:" + idMap);
            return;
        }
        plugin.getLogger().info("Saving map " + idMap);

        // Enregistrement de l'image sur le disque dur
        File outputfile = new File("./plugins/ImageOnMap/Image/" + nomImg + ".png");
        ImageIO.write(MapPalette.resizeImage(image), "png", outputfile);
        // Enregistrement de la map dans la config
        ArrayList<String> liste = new ArrayList<String>();
        liste.add(String.valueOf(idMap));
        liste.add(nomImg);
        liste.add(nomJoueur);
        liste.add(nomMonde);
        plugin.getCustomConfig().set(nomImg, liste);
        plugin.saveCustomConfig();
        if (!plugin.mapChargee.contains(idMap))
        {
            plugin.mapChargee.add(idMap);
        }
    }

    @SuppressWarnings("deprecation")
    Boolean LoadMap()
    {
        MapView carte = Bukkit.getMap(idMap);
        if (carte != null && loaded)
        {
            ImageRendererThread.SupprRendu(carte);
            carte.addRenderer(new Rendu(image));
            if (!plugin.mapChargee.contains(idMap))
            {
                plugin.mapChargee.add(idMap);
            }
            return true;
        }
        else
        {
            return false;
        }
    }

}
