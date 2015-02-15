package fr.moribus.imageonmap;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapType;
import fr.moribus.imageonmap.map.PosterMap;
import fr.moribus.imageonmap.map.SingleMap;
import java.io.IOException;
import java.net.URL;

public class TacheTraitementNouvelleMap extends TacheTraitementMap
{
	private final MapType type;

	public TacheTraitementNouvelleMap(Player player, URL url, MapType type, boolean rs, boolean rn)
	{
		super(player, url, rs, rn);
		this.type = type;
	}

	@Override
	public void traiterMap(BufferedImage img) throws IOException
	{
		ImageMap m;
		
		if(type == MapType.Single)
			m = new SingleMap(img, getJoueur());
		else
			m = new PosterMap(img, getJoueur());
		
		m.load();
		m.give(getJoueur().getInventory());
		m.save();
	}

}
