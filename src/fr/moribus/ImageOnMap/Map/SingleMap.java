package fr.moribus.ImageOnMap.Map;

import java.awt.Image;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;

import fr.moribus.ImageOnMap.Rendu;

public class SingleMap implements ImageMap
{
	private MapData data;
	private MapView map;
	private boolean named;
	
	@SuppressWarnings("deprecation")
	public SingleMap(Image img, Player joueur)
	{
		map = Bukkit.createMap(joueur.getWorld());
		this.named = false;
		
		data = new MapData(map.getId(), joueur.getName(), img, joueur.getWorld().getName());
	}
	
	@SuppressWarnings("deprecation")
	public SingleMap(Image img, Player joueur, String name)
	{
		map = Bukkit.createMap(joueur.getWorld());
		
		data = new MapData(map.getId(), joueur.getName(), img, joueur.getWorld().getName(), name);
	}
	
	@SuppressWarnings("deprecation")
	public SingleMap(short id) throws Exception
	{
		try
		{
			data = new MapData(id);
			data.load();
			map = Bukkit.getMap(id);
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	@Override
	public boolean save()
	{
		return data.save();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void give(Inventory inv)
	{
		ItemStack itemMap = new ItemStack(Material.MAP, 1, map.getId());
		if(isNamed())
		{
			ItemMeta meta = itemMap.getItemMeta();
			meta.setDisplayName(data.getNom());
			itemMap.setItemMeta(meta);
		}
		inv.addItem(itemMap);
	}

	@Override
	public boolean load()
	{
		if(map != null)
		{
			SingleMap.SupprRendu(map);
			map.addRenderer(new Rendu(data.getImage()));
			return true;
		}
		else
			return false;
	}

	@Override
	public boolean isNamed()
	{
		return named;
	}
	
	@SuppressWarnings("deprecation")
	public short getId()
	{
		return map.getId();
	}

	@Override
	public void setImage(Image image)
	{
		data.setImage(image);
		load();
	}

	@Override
	public void send(Player joueur)
	{
		joueur.sendMap(map);
	}

	public static void SupprRendu(MapView map)
	{
		if (map.getRenderers().size() > 0)
		{
			int i = 0, t = map.getRenderers().size();
			while (i < t)
			{
				map.removeRenderer(map.getRenderers().get(i));
				i++;
			}
		}
	}

}
