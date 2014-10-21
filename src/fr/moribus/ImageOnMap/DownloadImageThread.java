package fr.moribus.ImageOnMap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;


public class DownloadImageThread implements Callable<BufferedImage>
{
	private String stringUrl;
	private BufferedImage imgSrc;
	/*private BufferedImage[] img;
	private Poster poster;
	private boolean resized;*/

	DownloadImageThread(String u/*, boolean r*/)
	{
		stringUrl = u;
		//resized = r;
	}
	
	/*public BufferedImage[] getImg() 
	{
		if (!this.isAlive())
				return img;

		else
			return null;
	}
	
	public HashMap<Integer, String> getNumeroMap()
	{
		return poster.NumeroMap;
	}

	public void run()
	{
		URI uri = null;
		java.net.URL url = null;
		try 
		{
			uri = URI.create(URL);
			url = uri.toURL();
			
			
		}
		catch (IllegalArgumentException | MalformedURLException e) {
			e.printStackTrace();
			
			erreur = true;
			return;
		}
		if(erreur != true)
		{
			try {
				imgSrc = ImageIO.read(url.openStream());
				
			
			} catch (IOException e) {
				// 
				erreur = true;
				e.printStackTrace();
			}
			if(resized)
			{
				img = new BufferedImage[1];
				Image i = imgSrc.getScaledInstance(128, 128, Image.SCALE_SMOOTH);
				BufferedImage imgScaled = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
				imgScaled.getGraphics().drawImage(i, 0, 0 , null);
				img[0] = imgScaled;
				
			}
			else
			{
				int width = imgSrc.getWidth();
				int height = imgSrc.getHeight();
				
				// Fonction qui cherche le multiple de 128 le plus proche
				// de la hauteur / largeur de l'image
				int tmpW = 0, tmpH = 0;
				int i = 1;
				while (tmpW < width)
				{
					
					tmpW = i * 128;
					i++;
				}
				
				i = 0;
				while (tmpH < height)
				{
					
					tmpH = i * 128;
					i++;
				}
				
				// On crée un "canvas" = une image vide qui a une taille multiple de 128
				// dans laquelle on dessinera l'image téléchargées
				BufferedImage canvas = new BufferedImage(tmpW, tmpH, BufferedImage.TYPE_INT_ARGB);
				// On récupère l'objet Grapics2D, servant à dessiner dans notre canvas
				Graphics2D graph = canvas.createGraphics();
				
				// Variable servant à cadrer l'image
				int centerX = 0, centerY = 0;
				centerX = (tmpW - imgSrc.getWidth()) / 2;
				centerY = (tmpH - imgSrc.getHeight()) / 2;
				//On déplace le point d'origine de graph afin que l'image soit dessinée au milieu du canvas
				graph.translate(centerX, centerY);
				//graph.rotate(45);
				// on dessine l'image dans le canvas
				graph.drawImage(imgSrc, null, null);
				// on crée un Poster à partir de notre canvas
				poster = new Poster(canvas);
				img = poster.getPoster();
			}
		}
		
	}*/
	
	@Override
	public BufferedImage call() throws Exception
	{
		URL url = new URL(stringUrl);
		
		try
		{
			imgSrc = ImageIO.read(url.openStream());
		}
		catch (IOException e)
		{
			imgSrc = null;
		}
		return imgSrc;
	}

}
