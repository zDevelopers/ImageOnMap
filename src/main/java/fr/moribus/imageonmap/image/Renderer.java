/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.moribus.imageonmap.image;

import fr.zcraft.zlib.tools.PluginLogger;
import java.awt.image.BufferedImage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class Renderer extends MapRenderer
{
    static public boolean isHandled(MapView map)
    {
        if(map == null) return false;
        for(MapRenderer renderer : map.getRenderers())
        {
            if(renderer instanceof Renderer) return true;
        }
        return false;
    }
    
    static public void installRenderer(PosterImage image, int[] mapsIds)
    {
        for(int i = 0; i < mapsIds.length; i++)
        {
            installRenderer(image.getImageAt(i), mapsIds[i]);
        }
    }
    
    static public void installRenderer(BufferedImage image, int mapID)
    {
        @SuppressWarnings("deprecation")
		MapView map = Bukkit.getMap(mapID > Short.MAX_VALUE ? Short.MAX_VALUE : mapID < Short.MIN_VALUE ? Short.MIN_VALUE : (short)mapID);
        if(map == null)
        {
            PluginLogger.warning("Could not install renderer for map {0} : the Minecraft map does not exist", mapID);
        }
        else
        {
            installRenderer(map).setImage(image);
        }
    }
    
    static public Renderer installRenderer(MapView map)
    {
        Renderer renderer = new Renderer();
        removeRenderers(map);
        map.addRenderer(renderer);
        return renderer;
    }
    
    static public void removeRenderers(MapView map)
    {
        for(MapRenderer renderer : map.getRenderers())
        {
            map.removeRenderer(renderer);
        }
    }
    
    private BufferedImage image;
    
    protected Renderer()
    {
        this(null);
    }
    
    protected Renderer(BufferedImage image)
    {
        this.image = image;
    }

    @Override
    public void render(MapView v, final MapCanvas canvas, Player p)
    {
        //Render only once to avoid overloading the server
        if (image == null) return;
        
        canvas.drawImage(0, 0, image);
        image = null;
    }
    
    public BufferedImage getImage()
    {
        return image;
    }
    
    public void setImage(BufferedImage image)
    {
        this.image = image;
    }
}
