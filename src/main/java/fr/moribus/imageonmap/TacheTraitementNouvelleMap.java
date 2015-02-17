package fr.moribus.imageonmap;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.ImageMap.Type;
import java.io.IOException;
import java.net.URL;

public class TacheTraitementNouvelleMap extends TacheTraitementMap
{
	private final Type type;

	public TacheTraitementNouvelleMap(Player player, URL url, Type type, boolean rs, boolean rn)
	{
		super(player, url, rs, rn);
		this.type = type;
	}

	@Override
	public void traiterMap(BufferedImage img) throws IOException
	{
		ImageMap m = ImageMap.Type.createNewMap(type, img, getJoueur());
		
		m.load();
		m.give(getJoueur().getInventory());
		m.save();
	}

}
