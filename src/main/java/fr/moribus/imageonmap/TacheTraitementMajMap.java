package fr.moribus.imageonmap;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;

import fr.moribus.imageonmap.map.ImageMap;
import java.io.IOException;
import java.net.URL;

public class TacheTraitementMajMap extends TacheTraitementMap
{
	private ImageMap m;
	
	public TacheTraitementMajMap(ImageMap m, URL url, Player joueur)
	{
		super(url);
		setJoueur(joueur);
		this.m = m;
	}

	@Override
	public void traiterMap(BufferedImage img) throws IOException
	{
		m.setImage(img);
		m.save();
		m.send(getJoueur());
	}
}
