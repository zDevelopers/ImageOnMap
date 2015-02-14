package fr.moribus.imageonmap;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener
{
	ImageOnMap plugin;
	HashMap<String, SendEntireMapTask> hmap;
	int send;
	
	PlayerListener(ImageOnMap p)
	{
		plugin = p;
		hmap = new HashMap<String, SendEntireMapTask>();
		send = plugin.getConfig().getInt("send-entire-maps");
	}
	@EventHandler
	public void OnPlayerLogin(PlayerLoginEvent event)
	{
		if(send > 0)
		{
			Player joueur = event.getPlayer();
			hmap.put(joueur.getName(), new SendEntireMapTask(plugin, event.getPlayer()));
			hmap.get(joueur.getName()).runTaskTimer(plugin, 40, 20);
		}
	}
	
	@EventHandler
	public void OnPlayerLeft(PlayerQuitEvent event)
	{
		if(send > 0)
		{
			Player joueur = event.getPlayer();
			hmap.get(joueur.getName()).cancel();
		}
	}
}
