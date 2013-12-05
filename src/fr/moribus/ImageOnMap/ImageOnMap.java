package fr.moribus.ImageOnMap;

import java.io.File;

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
		
		if(dossierCree)
		{
			getCommand("tomap").setExecutor(new ImageRenduCommande(this));
			getCommand("rmmap").setExecutor(new ImageSupprCommande(this));
			this.saveDefaultConfig();
			//ChargerMap();
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

	/*public void ChargerMap()
	{
		Set<String> cle = getConfig().getKeys(false);
		for (String s: cle)
		{
			if(getConfig().getStringList(s).size() >= 3)
			{
				System.out.println("Loading " + s);
				@SuppressWarnings("deprecation")
				MapView carte = Bukkit.getMap(Short.parseShort(getConfig().getStringList(s).get(0)));
				ImageRenderer.SupprRendu(carte);
				if (getConfig().getStringList(s).size() == 4)
					carte.addRenderer(new Rendu("./plugins/ImageOnMap/Image/" + getConfig().getStringList(s).get(1) + ".png", getConfig().getStringList(s).get(2), getConfig().getStringList(s).get(3)));
				else if (getConfig().getStringList(s).size() == 3)
					carte.addRenderer(new Rendu("./plugins/ImageOnMap/Image/" + getConfig().getStringList(s).get(1) + ".png", getConfig().getStringList(s).get(2), "True"));
			}
		}
	}*/

}
