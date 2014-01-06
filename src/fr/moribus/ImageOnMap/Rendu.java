package fr.moribus.ImageOnMap;

import java.awt.Image;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;


public class Rendu extends MapRenderer implements Runnable 
{
	
	boolean estRendu;
	Image touhou;
	private Thread TRendu;
	public MapCanvas canvas;
	
	public Rendu(Image img)
	{
		estRendu = false;
		touhou = img;
	}
	
	@Override
	public void render(MapView v, final MapCanvas mc, Player p) 
	{
		canvas = mc;


		if (!estRendu) // Si la map a déjà été rendu, on n'entre plus dans la fonction, ce qui évite de surcharger le serveur
		{
			// On instancie et démarre le thread de rendu
			TRendu = new Thread(this);
			TRendu.start();
			estRendu = true;
		}
	}

	// Le chargement et le rendu de l'image se font dans un thread afin d'éviter le lag..
	@Override
	public void run() 
	{
		// on dessine l'image redimensionnée dans le canvas (et donc, sur la map !)
		canvas.drawImage(0, 0, touhou);

	}
}