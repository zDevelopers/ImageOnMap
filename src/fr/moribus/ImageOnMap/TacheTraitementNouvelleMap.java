package fr.moribus.ImageOnMap;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;

import fr.moribus.ImageOnMap.Map.PosterMap;

public class TacheTraitementNouvelleMap extends TacheTraitementMap
{

	public TacheTraitementNouvelleMap(Player j, String u, boolean rs, boolean rn)
	{
		super(j, u, rs, rn);
	}

	@Override
	public void traiterMap(BufferedImage img)
	{
		PosterMap m = new PosterMap(img, getJoueur());
		m.load();
		m.give(getJoueur().getInventory());
		m.save();
	}

}
