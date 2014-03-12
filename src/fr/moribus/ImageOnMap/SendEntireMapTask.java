package fr.moribus.ImageOnMap;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

public class SendEntireMapTask extends BukkitRunnable
{
	ImageOnMap plugin;
	Player joueur;
	ArrayList<Short> listeMap;
	List<Short> partListe;
	boolean allsent;
	int index, toIndex, nbSend;

	SendEntireMapTask(ImageOnMap p, Player j)
	{
		plugin = p;
		joueur = j;
		allsent = false;
		index = 0;
		nbSend = plugin.getConfig().getInt("send-entire-maps");
		toIndex = nbSend;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() 
	{
		if(nbSend > 0)
		{
			if(listeMap == null)
			{
				//plugin.getLogger().info("test de nullité");
				listeMap = new ArrayList<Short>();
				listeMap.addAll(plugin.mapChargee);
			}
			else if(listeMap.equals(plugin.mapChargee) != true)
			{
				//plugin.getLogger().info("taille des maps : " + listeMap.size()+ " ; "+ plugin.mapChargee.size());
				//plugin.getLogger().info("test d'égalité");
				listeMap.clear();
				listeMap.addAll(plugin.mapChargee);
				allsent = false;
			}
			if(!allsent)
			{
				//plugin.getLogger().info("test 1");
				if(toIndex > listeMap.size())
				{
					//plugin.getLogger().info("test 3");
					partListe = listeMap.subList(index, listeMap.size());
					index = listeMap.size();
					toIndex = listeMap.size() + nbSend;
					allsent = true;
				}
				else
				{
					//plugin.getLogger().info("taille des index : " + index+ " ; "+ toIndex+ ". taille de la map : "
					//		+ listeMap.size());
					//plugin.getLogger().info("test 2");
					partListe = listeMap.subList(index, toIndex);
					index = toIndex;
					toIndex += nbSend;
				}
				for(int i= 0; i< partListe.size(); i++)
				{
					//plugin.getLogger().info("test 4");
					MapView map = Bukkit.getMap(partListe.get(i));
					joueur.sendMap(map);
				}
			}
		}
		
	}

}
