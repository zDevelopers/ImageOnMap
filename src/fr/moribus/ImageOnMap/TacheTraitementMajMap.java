package fr.moribus.ImageOnMap;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;

import fr.moribus.ImageOnMap.Map.ImageMap;

public class TacheTraitementMajMap extends TacheTraitementMap
{
	private ImageMap m;
	
	public TacheTraitementMajMap(ImageMap m, String u, Player joueur)
	{
		super(u);
		setJoueur(joueur);
		this.m = m;
	}

	@Override
	public void traiterMap(BufferedImage img)
	{
		m.setImage(img);
		m.save();
		m.send(getJoueur());
	}
}
