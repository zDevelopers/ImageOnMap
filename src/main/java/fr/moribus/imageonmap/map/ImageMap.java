package fr.moribus.imageonmap.map;

import fr.moribus.imageonmap.image.PosterImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.bukkit.Material;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ImageMap 
{
    static public enum Type
    {
        SINGLE, POSTER;
        
        static public ImageMap createNewMap(Type type, BufferedImage image, Player player)
        {
            switch(type)
            {
                case POSTER:
                    return new PosterMap(new PosterImage(image), player);
                default:
                    return new SingleMap(image, player);
            }
        }
        
        static public Type fromString(String string)
        { 
            switch(string.toLowerCase())
            {
                case "poster":
                case "multi":
                    return POSTER;
                default:
                    return SINGLE;
            }
        }
    }
    
    static public final int WIDTH = 128;
    static public final int HEIGHT = 128;
    
    protected String imageName;
    protected String ownerName;
    protected String worldName;
    
    public abstract void load() throws IOException;
    public abstract void save() throws IOException;
    public abstract void give(Inventory inv);
    public abstract void setImage(BufferedImage image);
    public abstract void send(Player joueur);
    
    public ImageMap()
    {
        this(null, null, null);
    }
    
    public ImageMap(String imageName, String ownerName, String worldName)
    {
        this.imageName = imageName;
        this.ownerName = ownerName;
        this.worldName = worldName;
    }
    
    
    
    protected void give(Inventory inventory, short mapID)
    {
        ItemStack itemMap = new ItemStack(Material.MAP, 1, mapID);
        if(isNamed())
        {
            ItemMeta meta = itemMap.getItemMeta();
            meta.setDisplayName(imageName);
            itemMap.setItemMeta(meta);
        }
        inventory.addItem(itemMap);
    }
    
    // Getters & Setters

    public String getImageName()
    {
        return imageName;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }
    
    public boolean isNamed()
    {
        return imageName != null;
    }
    
    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName(String ownerName)
    {
        this.ownerName = ownerName;
    }
    
    public String getWorldName()
    {
        return worldName;
    }

    public void setWorldName(String worldName)
    {
        this.worldName = worldName;
    }
}
