package fr.moribus.imageonmap.map;

import fr.moribus.imageonmap.ImgUtility;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;

import fr.moribus.imageonmap.Rendu;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SingleMap implements ImageMap
{
    private final MapData data;
    private final MapView map;

    public final int LARGEUR = 128;
    public final int HAUTEUR = 128;

    public SingleMap(BufferedImage img, Player player)
    {
        this(img, player, null);
    }

    public SingleMap(BufferedImage img, Player player, String imageName)
    {
        map = Bukkit.createMap(player.getWorld());

        data = new MapData(map.getId(), player.getName(), ImgUtility.scaleImage(img, LARGEUR, HAUTEUR), player.getWorld().getName(), imageName);
    }

    @SuppressWarnings("deprecation")
    public SingleMap(short id) throws IOException, IllegalArgumentException
    {
        data = new MapData(id);
        data.load();
        map = Bukkit.getMap(id);
        if(map == null) 
            throw new IllegalArgumentException("Map ID '"+id+"' doesn't exist");
    }

    @Override
    public void save() throws IOException
    {
        data.save();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void give(Inventory inv)
    {
        ItemStack itemMap = new ItemStack(Material.MAP, 1, map.getId());
        if (isNamed())
        {
            ItemMeta meta = itemMap.getItemMeta();
            meta.setDisplayName(data.getName());
            itemMap.setItemMeta(meta);
        }
        inv.addItem(itemMap);
    }

    @Override
    public void load()
    {
        SingleMap.SupprRendu(map);
        map.addRenderer(new Rendu(data.getImage()));
    }

    @Override
    public boolean isNamed()
    {
        return data.getName() != null;
    }

    @SuppressWarnings("deprecation")
    public short getId()
    {
        return map.getId();
    }

    @Override
    public void setImage(BufferedImage image)
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
