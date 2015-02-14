package fr.moribus.imageonmap.map;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface ImageMap 
{
	public boolean load();
	public boolean save();
	public void give(Inventory inv);
	public boolean isNamed();
	public void setImage(BufferedImage image);
	public void send(Player joueur);
}