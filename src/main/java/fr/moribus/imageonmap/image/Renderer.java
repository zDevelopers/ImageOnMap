/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2020)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2020)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.moribus.imageonmap.image;

import fr.zcraft.quartzlib.tools.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

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
        MapView map = Bukkit.getMap(mapID);
        if(map == null)
        {
            PluginLogger.warning("Could not install renderer for map {0}: the Minecraft map does not exist", mapID);
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
