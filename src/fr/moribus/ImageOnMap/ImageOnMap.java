package fr.moribus.ImageOnMap;

import java.io.File;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

public final class ImageOnMap extends JavaPlugin
{
	File dossier;
	boolean dossierCree;
	@Override
	public void onEnable()
	{
		dossier = new File(getDataFolder().getPath() + "/Image");
		if (!dossier.exists())
		{
			 dossierCree = dossier.mkdirs();
		}
		else
			dossierCree = true;
		
		if(dossierCree)
		{
		System.out.println("Loading ImageOnMap");
		getCommand("tomap").setExecutor(new ImageRenduCommande(this));
		getCommand("rmmap").setExecutor(new ImageSupprCommande(this));
		this.saveDefaultConfig();
		ChargerMap();
		}
		else
			System.out.println("[ImageOnMap] An error occured ! Unable to create Image folder. Plugin will NOT work !");
	}
	
	@Override
	public void onDisable()
	{
		System.out.println("Stopping ImageOnMap");
	}

	public void ChargerMap()
	{
		Set<String> cle = getConfig().getKeys(false);
		for (String s: cle)
		{
			System.out.println("Loading " + s);
			@SuppressWarnings("deprecation")
			MapView carte = Bukkit.getMap(Short.parseShort(getConfig().getStringList(s).get(0)));
			Rendu.SupprRendu(carte);
			carte.addRenderer(new Rendu("./plugins/ImageOnMap/Image/" + getConfig().getStringList(s).get(1) + ".png", getConfig().getStringList(s).get(2)));
		}
	}

}
