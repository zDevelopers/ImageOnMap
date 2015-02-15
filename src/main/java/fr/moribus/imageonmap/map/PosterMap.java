package fr.moribus.imageonmap.map;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.moribus.imageonmap.Poster;
import java.io.IOException;

public class PosterMap implements ImageMap
{

    private SingleMap[] posterMap;
    private PosterData data;

    public PosterMap(BufferedImage img, Player joueur)
    {
        Poster poster = new Poster(img);
        BufferedImage[] imgs = poster.getPoster();
        posterMap = new SingleMap[imgs.length];

        short[] idsMap = new short[posterMap.length];
        for (int i = 0; i < posterMap.length; i++)
        {
            SingleMap map = new SingleMap(imgs[i], joueur);
            posterMap[i] = map;
            idsMap[i] = map.getId();
        }

        data = new PosterData(joueur.getName(), idsMap);
    }

    public PosterMap(String id) throws Exception
    {
        data = new PosterData(id);

        short[] ids = data.getIdMaps();
        posterMap = new SingleMap[ids.length];
        for (int i = 0; i < ids.length; i++)
        {
            SingleMap map = new SingleMap(ids[i]);
            posterMap[i] = map;
        }
    }

    @Override
    public void load() 
    {
        for(SingleMap map : posterMap)
        {
            map.load();
        }
    }

    @Override
    public void save() throws IOException
    {
        for(SingleMap map : posterMap)
        {
            map.save();
        }
    }

    @Override
    public void give(Inventory inv)
    {
        for(SingleMap map : posterMap)
        {
            map.give(inv);
        }
    }

    @Override
    public boolean isNamed()
    {
        for(SingleMap map : posterMap)
        {
            if(!map.isNamed()) return false;
        }
        return true;
    }

    @Override
    public void setImage(BufferedImage image)
    {
        Poster poster = new Poster(image);
        BufferedImage[] imgs = poster.getPoster();
        for(int i = 0; i < posterMap.length; i++)
        {
            posterMap[i].setImage(imgs[i]);
        }
    }

    @Override
    public void send(Player player)
    {
        for(SingleMap map : posterMap)
        {
            map.send(player);
        }
    }

}
