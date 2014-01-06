package fr.moribus.ImageOnMap;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

public final class ImageOnMap extends JavaPlugin
{
	int test = 0;
	File dossier;
	boolean dossierCree;
	@Override
	public void onEnable()
	{
		// On crée si besoin le dossier où les images seront stockées
		dossierCree = ImgUtility.CreeRepImg(this);
		
		// On ajoute si besoin les params par défaut du plugin
		ImgUtility.CreeSectionConfig(this);
		
		if(this.getConfig().getBoolean("collect-data"))
		{
			try 
			{
				MetricsLite metrics = new MetricsLite(this);
				metrics.start();
				System.out.println("Metrics launched for ImageOnMap");
			} catch (IOException e) {
				// Failed to submit the stats :-(
			}
		}
		
		if(dossierCree)
		{
			getCommand("tomap").setExecutor(new ImageRenduCommande(this));
			//getCommand("rmmap").setExecutor(new ImageSupprCommande(this));
			this.saveDefaultConfig();
			ChargerMap();
		}
		else
		{
			System.out.println("[ImageOnMap] An error occured ! Unable to create Image folder. Plugin will NOT work !");
			this.setEnabled(false);
		}
	}
	
	@Override
	public void onDisable()
	{
		System.out.println("Stopping ImageOnMap");
	}

	public void ChargerMap()
	{
		Set<String> cle = getConfig().getKeys(false);
		int nbMap = 0, nbErr = 0;
		for (String s: cle)
		{
			if(getConfig().getStringList(s).size() >= 3)
			{
				SavedMap map = new SavedMap(this, Short.valueOf(getConfig().getStringList(s).get(0)));
				
				if(map.LoadMap())
					nbMap++;
				else
					nbErr++;
			}
			
		}
		System.out.println(nbMap +" maps was loaded");
		if(nbErr != 0)
			System.out.println(nbErr +" maps can't be loaded");
	}

}
