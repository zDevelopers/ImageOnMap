package fr.moribus.ImageOnMap;

import java.awt.Image;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;


public class Rendu extends MapRenderer implements Runnable 
{
	
	private boolean estRendu;
	private Image imageARendre;
	private MapCanvas canvas;
	
	public Rendu(Image img)
	{
		estRendu = false;
		setImageARendre(img);
	}
	
	@Override
	public void render(MapView v, final MapCanvas mc, Player p) 
	{
		canvas = mc;


		if (!estRendu) // Si la map a déjà été rendu, on n'entre plus dans la fonction, ce qui évite de surcharger le serveur
		{
			run();
			estRendu = true;
		}
	}

	@Override
	public void run() 
	{
		// on dessine l'image redimensionnée dans le canvas (et donc, sur la map !)
		canvas.drawImage(0, 0, getImageARendre());

	}

	public Image getImageARendre()
	{
		return imageARendre;
	}

	public void setImageARendre(Image imageARendre)
	{
		if(imageARendre != null)
		{
			this.imageARendre = imageARendre;
			estRendu = false;
		}
	}
}