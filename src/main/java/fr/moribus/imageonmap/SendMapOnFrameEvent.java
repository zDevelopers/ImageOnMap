package fr.moribus.imageonmap;

import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import fr.moribus.imageonmap.map.SingleMap;

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
					try
					{
						new SingleMap(stack.getDurability()).load();
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				
			}
		}
	}
}
