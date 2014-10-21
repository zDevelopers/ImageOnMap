package fr.moribus.ImageOnMap.Map;

import java.awt.Image;
import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.moribus.ImageOnMap.Poster;

public class PosterMap implements ImageMap
{
	private SingleMap[] posterMap;
	private PosterData data;
	
	public PosterMap(Image img, Player joueur)
	{
		Poster poster = new Poster((BufferedImage)img);
		BufferedImage[] imgs = poster.getPoster();
		posterMap = new SingleMap[imgs.length];
		
		short[] idsMap = new short[posterMap.length];
		for(int i = 0; i < posterMap.length; i++)
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
		for(int i = 0; i < ids.length; i++)
		{
			SingleMap map = new SingleMap(ids[i]);
			posterMap[i] = map;
		}
	}

	@Override
	public boolean load()
	{
		boolean ok;
		
		int i = 0;
		do
		{
			ok = posterMap[i].load();
			i++;
		}
		while(ok && i < posterMap.length);
		
		return ok;
	}

	@Override
	public boolean save()
	{
		boolean ok;
		
		int i = 0;
		do
		{
			ok = posterMap[i].save();
			i++;
		}
		while(ok && i < posterMap.length);
		
		data.save();
		
		return ok;
	}

	@Override
	public void give(Inventory inv)
	{
		int i = 0;
		do
		{
			posterMap[i].give(inv);
			i++;
		}
		while(i < posterMap.length);
	}

	@Override
	public boolean isNamed()
	{
		boolean ok;
		
		int i = 0;
		do
		{
			ok = posterMap[i].isNamed();
			i++;
		}
		while(ok && i < posterMap.length);
		
		return ok;
	}

	@Override
	public void setImage(Image image)
	{
		Poster poster = new Poster((BufferedImage)image);
		BufferedImage[] imgs = poster.getPoster();
		int i = 0;
		do
		{
			posterMap[i].setImage(imgs[i]);
			i++;
		}
		while(i < imgs.length);
	}

	@Override
	public void send(Player joueur)
	{
		int i = 0;
		do
		{
			posterMap[i].send(joueur);
			i++;
		}
		while(i < posterMap.length);
	}

}
