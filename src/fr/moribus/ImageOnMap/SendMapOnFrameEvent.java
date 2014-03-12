package fr.moribus.ImageOnMap;

import java.util.ArrayList;

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
	}
	
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event)
	{
		chunk = event.getChunk();
		entites = chunk.getEntities().clone();
		for(int i = 0; i < entites.length; i++)
		{
			Entity entite = entites[i];
			if(entite instanceof ItemFrame)
			{
				ArrayList<Short> ListeId = plugin.mapChargee;
				frame = (ItemFrame) entite;
				ItemStack stack = frame.getItem();
				if(stack.getType() == Material.MAP && !ListeId.contains(stack.getDurability()))
				{
					SavedMap map = new SavedMap(plugin, stack.getDurability());
					map.LoadMap();

				}
				
				
			}
		}
	}
}
