package fr.moribus.imageonmap.map;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface ImageMap 
{
	public void load() throws IOException;
	public void save() throws IOException;
	public void give(Inventory inv);
	public boolean isNamed();
	public void setImage(BufferedImage image);
	public void send(Player joueur);
}
