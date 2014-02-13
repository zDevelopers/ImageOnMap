package fr.moribus.ImageOnMap;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

public class SendMapOnFrameEvent implements Listener
{
	ImageOnMap plugin;
	Chunk chunk;
	Entity[] entites;
	ItemFrame frame;
	
	SendMapOnFrameEvent(ImageOnMap plug)
	{
		plugin = plug;
		plugin.getLogger().info("Loading event");
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event)
	{
		chunk = event.getChunk();
		//plugin.getLogger().info("Loading entities..");
		entites = chunk.getEntities().clone();
		for(int i = 0; i < entites.length; i++)
		{
			Entity entite = entites[i];
			//plugin.getLogger().info("entrée dans la boucle");
			if(entite instanceof ItemFrame)
			{
				ArrayList<Short> ListeId = plugin.mapChargee;
				//plugin.getLogger().info("entrée dans la condition");
				frame = (ItemFrame) entite;
				ItemStack stack = frame.getItem();
				if(stack.getType() == Material.MAP && !ListeId.contains(stack.getDurability()))
				{
					/* c'est dans la méthode LoadMap() qu'on vérifie si la map est bien
					 une map custom et non une map normale*/
					SavedMap map = new SavedMap(plugin, stack.getDurability());
					map.LoadMap();

				}
				
				
			}
		}
	}
}
