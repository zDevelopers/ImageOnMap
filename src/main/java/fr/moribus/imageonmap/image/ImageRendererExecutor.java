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

import fr.moribus.imageonmap.PluginLogger;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.worker.Worker;
import fr.moribus.imageonmap.worker.WorkerCallback;
import fr.moribus.imageonmap.worker.WorkerRunnable;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;

public class ImageRendererExecutor extends Worker
{
    static private ImageRendererExecutor instance;
    
    static public void start()
    {
        if(instance != null) stop();
        instance = new ImageRendererExecutor();
        instance.init();
    }
    
    static public void stop()
    {
        instance.exit();
        instance = null;
    }
    
    private ImageRendererExecutor()
    {
        super("Image IO", true);
    }
    
    static public void Test(WorkerCallback callback)
    {
        instance.submitQuery(new WorkerRunnable<Void>()
        {
            @Override
            public Void run() throws Throwable
            {
                Thread.sleep(5000);
                return null;
            }
        }, callback);
    }
    
    static public void Render(final URL url, final boolean scaling, final UUID playerUUID, WorkerCallback<ImageMap> callback)
    {
        instance.submitQuery(new WorkerRunnable<ImageMap>()
        {
            @Override
            public ImageMap run() throws Throwable
            {
                final BufferedImage image = ImageIO.read(url);
                if(image == null) throw new IOException("The given URL is not a valid image");
                
                if(scaling) return RenderSingle(image, playerUUID);
                else return RenderPoster(image, playerUUID);
            }
        }, callback);
    }
    
    static private ImageMap RenderSingle(final BufferedImage image, final UUID playerUUID) throws Throwable
    {
        final short mapID = instance.submitToMainThread(new Callable<Short>()
        {
            @Override
            public Short call() throws Exception
            {
                return MapManager.getNewMapsIds(1)[0];
            }
        }).get();
        
        final BufferedImage finalImage = ResizeImage(image, ImageMap.WIDTH, ImageMap.HEIGHT);
        
        ImageIOExecutor.saveImage(mapID, finalImage);
        
        final ImageMap newMap = instance.submitToMainThread(new Callable<ImageMap>()
        {
            @Override
            public ImageMap call() throws Exception
            {
                Renderer.installRenderer(finalImage, mapID);
                return MapManager.createMap(playerUUID, mapID);
            }

        }).get();
        
        return newMap;
    }
    
    static private ImageMap RenderPoster(final BufferedImage image, final UUID playerUUID) throws Throwable
    {
        final PosterImage poster = new PosterImage(image);
        final int mapCount = poster.getImagesCount();

        final Future<short[]> futureMapsIds = instance.submitToMainThread(new Callable<short[]>()
        {
            @Override
            public short[] call() throws Exception
            {
                return MapManager.getNewMapsIds(mapCount);
            }
        });

        poster.splitImages();

        final short[] mapsIDs = futureMapsIds.get();
        
        ImageIOExecutor.saveImage(mapsIDs, poster);
        
        final ImageMap newMap = instance.submitToMainThread(new Callable<ImageMap>()
        {
            @Override
            public ImageMap call() throws Exception
            {
                Renderer.installRenderer(poster, mapsIDs);
                return MapManager.createMap(poster, playerUUID, mapsIDs);
            }

        }).get();
        
        return newMap;
    }
    
    static private BufferedImage ResizeImage(BufferedImage source, int destinationW, int destinationH)
    {
        float ratioW = (float)destinationW / (float)source.getWidth();
        float ratioH = (float)destinationH / (float)source.getHeight();
        int finalW, finalH;
        
        if(ratioW < ratioH)
        {
            finalW = destinationW;
            finalH = (int)(source.getHeight() * ratioW);
        }
        else
        {
            finalW = (int)(source.getWidth() * ratioH);
            finalH = destinationH;
        }
        
        int x, y;
        x = (destinationW - finalW) / 2;
        y = (destinationH - finalH) / 2;
        PluginLogger.LogInfo(finalW + " " + finalH + " : " + x + " " + y);
        
        BufferedImage newImage = new BufferedImage(destinationW, destinationH, BufferedImage.TYPE_INT_ARGB);
        
        Graphics graphics = newImage.getGraphics();
        graphics.drawImage(source, x, y, finalW, finalH, null);
        graphics.dispose();
        return newImage;
    }
    
}
