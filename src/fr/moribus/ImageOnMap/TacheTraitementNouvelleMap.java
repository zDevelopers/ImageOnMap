package fr.moribus.ImageOnMap;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;

import fr.moribus.ImageOnMap.Map.ImageMap;
import fr.moribus.ImageOnMap.Map.MapType;
import fr.moribus.ImageOnMap.Map.PosterMap;
import fr.moribus.ImageOnMap.Map.SingleMap;

public class TacheTraitementNouvelleMap extends TacheTraitementMap
{
	private MapType type;

	public TacheTraitementNouvelleMap(Player j, String u, MapType type, boolean rs, boolean rn)
	{
		super(j, u, rs, rn);
		this.type = type;
	}

	@Override
	public void traiterMap(BufferedImage img)
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
