package fr.moribus.ImageOnMap.Map;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AnimatedMap implements ImageObserver
{
	private ImageMap map;
	private ImageIcon icon;
	private BufferedImage img;
	private Player joueur;

	public AnimatedMap(ImageMap map, Player j) throws MalformedURLException
	{
		this.map = map;
		icon = new ImageIcon(new URL("http://upload.wikimedia.org/wikipedia/commons/5/55/Tesseract.gif"));
		img = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
		joueur = j;
		icon.setImageObserver(this);
	}
	
	public void animer()
	{
		icon.paintIcon(null, img.getGraphics(), 0, 0);
	}

	@Override
	public boolean imageUpdate(Image gif, int infoflags, int x, int y,
			int width, int height)
	{
		if ((infoflags & ImageObserver.FRAMEBITS) != 0)
		{
			Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("ImageOnMap"), new DessinerAnimation(gif));
			
			try
			{
				Thread.sleep(300);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return true;
	}
	
	class DessinerAnimation implements Runnable
	{
		private Image image;
		public DessinerAnimation(Image image)
		{
			this.image = image;
		}
		
		@Override
		public void run()
		{
			img.getGraphics().clearRect(0, 0, 128, 128);
			img.getGraphics().drawImage(image, 0, 0, null);
			map.setImage(img);
			map.send(joueur);
		}
	}

}
