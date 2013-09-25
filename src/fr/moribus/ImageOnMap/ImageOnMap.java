package fr.moribus.ImageOnMap;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

public final class ImageOnMap extends JavaPlugin
{

	@Override
	public void onEnable()
	{
		System.out.println("Loading ImageOnMap");
		getCommand("tomap").setExecutor(new ImageCommande(this));
		this.saveDefaultConfig();
		ChargerMap();
	}
	
	@Override
	public void onDisable()
	{
		System.out.println("Stopping ImageOnMap");
	}

	void ChargerMap()
	{
		Set<String> cle = getConfig().getKeys(false);
		for (String s: cle)
		{
			System.out.println("Loading" + s);
			@SuppressWarnings("deprecation")
			MapView carte = Bukkit.getMap(Short.parseShort(getConfig().getStringList(s).get(0)));
			Rendu.SupprRendu(carte);
			carte.addRenderer(new Rendu("./plugins/ImageOnMap/" + getConfig().getStringList(s).get(1) + ".png", getConfig().getStringList(s).get(2)));
		}
	}

}
