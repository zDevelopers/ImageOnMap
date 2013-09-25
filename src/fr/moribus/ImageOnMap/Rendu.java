package fr.moribus.ImageOnMap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MinecraftFont;

public class Rendu extends MapRenderer implements Runnable 
{
	
	boolean estRendu, estSVGee, local = false;
	BufferedImage touhou;
	BufferedImage resizedImage;
	private Thread TRendu;
	public MapCanvas canvas;
	MapView carte;
	String URLImage, nomJoueur;
	Player joueur;

	public Rendu(String url, boolean svg)
	{
		estRendu = true;
		estSVGee = svg;
		URLImage = url;
	}
	
	public Rendu(String url, String pseudo)
	{
		estRendu = true;
		estSVGee = false;
		URLImage = url;
		local = true; // Sert à indiquer que le rendu a été lancé par le plugin au démarrage, et non par un joueur en jeu.
		nomJoueur = pseudo;
	}
	@Override
	public void render(MapView v, final MapCanvas mc, Player p) 
	{
		canvas = mc;
		carte = v;
		joueur = p;


		if (estRendu) // Si la map a déjà été rendu, on n'entre plus dans la fonction, ce qui évite de surcharger le serveur
		{
			// On instancie et démarre le thread de rendu
			TRendu = new Thread(this);
			TRendu.start();
			estRendu = false;
		}
	}

	// Le chargement et le rendu de l'image se font dans un thread afin d'éviter le lag..
	@Override
	public void run() 
	{
		// chargement de l'image seulement si elle n'est pas déjà chargée
		if (touhou == null  && local == false)
		{
			try 
			{
				touhou = ImageIO.read(URI.create(URLImage).toURL().openStream());
				//System.out.println("chargement de l'image");
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (touhou == null  && local == true)
		{
			try 
			{
				touhou = ImageIO.read(new File(URLImage));
				//System.out.println("chargement de l'image");
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		if (estSVGee)
		{
			try 
			{
				@SuppressWarnings("deprecation")
				File outputfile = new File("./plugins/ImageOnMap/map" + carte.getId() + ".png");
				ImageIO.write(MapPalette.resizeImage(touhou), "png", outputfile);
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		//System.out.println("Rendu de l'image..");
		
		// on dessine l'image redimensionnée dans le canvas (et donc, sur la map !)
		canvas.drawImage(0, 0, MapPalette.resizeImage(touhou));
		
		// On écrit le pseudo du joueur qui a téléchargé l'image
		if(!local)
			canvas.drawText(2, 120, MinecraftFont.Font, new String("(" + joueur.getName() + ")"));
		else
			canvas.drawText(2, 120, MinecraftFont.Font, new String("(" + nomJoueur + ")"));
		
		//System.out.println("Rendu de l'image fini");

	}
	
	static void SupprRendu(MapView map)
	{
		if (map.getRenderers().size() != 0)
		{
			int i = 0, t = map.getRenderers().size();
			while (i < t )
			{
				map.removeRenderer(map.getRenderers().get(i));
				i++;
			}
		}
	}

}