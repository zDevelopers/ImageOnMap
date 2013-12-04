package fr.moribus.ImageOnMap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.bukkit.map.MapView;


public class ImageRenderer extends Thread
{
	private String URL;
	private BufferedImage imgSrc;
	private Poster img;
	private boolean estPrete = false;
	boolean erreur = false;
	
	ImageRenderer(String u)
	{
		URL = u;
	}
	
	public BufferedImage[] getImg() 
	{
		if (estPrete)
			return img.getPoster();
		else
			return null;
	}

	public Boolean getStatut()
	{
		return estPrete;
	}

	@Override
	public void run() 
	{
		try 
		{
			imgSrc = ImageIO.read(URI.create(URL).toURL().openStream());
			img = new Poster(imgSrc);
		}
		catch (IOException e) {
			e.printStackTrace();
			erreur = true;
		}
		estPrete = true;
		
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
