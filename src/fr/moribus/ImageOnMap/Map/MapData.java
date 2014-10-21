package fr.moribus.ImageOnMap.Map;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;

import fr.moribus.ImageOnMap.ImageOnMap;

public class MapData
{
	private short id;
	private String joueur;
	private String monde;
	private String nom;
	private Image image;
	
	
	public Image getImage()
	{
		return image;
	}
	
	public void setImage(Image image)
	{
		this.image = image;
	}

	public MapData(short id, String joueur, Image image, String monde)
	{
		this.id = id;
		this.joueur = joueur;
		this.monde = monde;
		this.image = image;
	}
	
	public MapData(short id, String joueur, Image image, String monde, String nom)
	{
		this.id = id;
		this.joueur = joueur;
		this.monde = monde;
		this.setNom(nom);
	}
	
	public MapData(short id) throws Exception
	{
		this.id = id;
	}
	
	public boolean save()
	{
		boolean ok = true;
		String nomImg = "map"+ id;
		ImageOnMap plugin = (ImageOnMap)Bukkit.getPluginManager().getPlugin("ImageOnMap");
		try
        {
            File outputfile = new File("./plugins/ImageOnMap/Image/"+ nomImg + ".png");
            ImageIO.write((BufferedImage)image, "png", outputfile);
                
             // Enregistrement de la map dans la config
    		ArrayList<String> liste = new ArrayList<String>();
    		liste.add(String.valueOf(id));
    		liste.add(nomImg);
    		liste.add(joueur);
    		liste.add(monde);
    		plugin.getCustomConfig().set(nomImg, liste);
    		plugin.saveCustomConfig();
        }
		catch (IOException e)
        {
                e.printStackTrace();
                ok = false;
        }
		
		return ok;
	}
	
	public void load() throws IOException
	{
		ImageOnMap plugin = (ImageOnMap)Bukkit.getPluginManager().getPlugin("ImageOnMap");
		List<String> svg = plugin.getCustomConfig().getStringList("map"+id);
		String nomImg = svg.get(1);
		joueur = svg.get(2);
		try
		{
			image = ImageIO.read(new File("./plugins/ImageOnMap/Image/"+ nomImg + ".png"));
		}
		catch (IOException e)
		{
			plugin.getLogger().log(Level.WARNING, "Image "+ nomImg +".png doesn't exists in Image directory.");
			throw e;
		}
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}
}
