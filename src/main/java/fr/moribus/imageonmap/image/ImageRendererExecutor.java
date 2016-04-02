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

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.zlib.components.worker.Worker;
import fr.zcraft.zlib.components.worker.WorkerAttributes;
import fr.zcraft.zlib.components.worker.WorkerCallback;
import fr.zcraft.zlib.components.worker.WorkerRunnable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;


@WorkerAttributes (name = "Image Renderer", queriesMainThread = true)
public class ImageRendererExecutor extends Worker
{
    static public void Test(WorkerCallback callback)
    {
        submitQuery(new WorkerRunnable<Void>()
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
        submitQuery(new WorkerRunnable<ImageMap>()
        {
            @Override
            public ImageMap run() throws Throwable
            {
                final BufferedImage image = ImageIO.read(url);
                if (image == null) throw new IOException("The given URL is not a valid image");

                if (scaling) return RenderSingle(image, playerUUID);
                else return RenderPoster(image, playerUUID);
            }
        }, callback);
    }
    
    static private ImageMap RenderSingle(final BufferedImage image, final UUID playerUUID) throws Throwable
    {
        MapManager.checkMapLimit(1, playerUUID);
        Future<Short> futureMapID = submitToMainThread(new Callable<Short>()
        {
            @Override
            public Short call() throws Exception
            {
                return MapManager.getNewMapsIds(1)[0];
            }
        });
        
        final BufferedImage finalImage = ResizeImage(image, ImageMap.WIDTH, ImageMap.HEIGHT);
        
        final short mapID = futureMapID.get();
        ImageIOExecutor.saveImage(mapID, finalImage);
        
        submitToMainThread(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                Renderer.installRenderer(finalImage, mapID);
                return null;
            }

        });
        
        return MapManager.createMap(playerUUID, mapID);
    }
    
    static private ImageMap RenderPoster(final BufferedImage image, final UUID playerUUID) throws Throwable
    {
        final PosterImage poster = new PosterImage(image);
        final int mapCount = poster.getImagesCount();
        
        MapManager.checkMapLimit(mapCount, playerUUID);
        final Future<short[]> futureMapsIds = submitToMainThread(new Callable<short[]>()
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
        
        submitToMainThread(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                Renderer.installRenderer(poster, mapsIDs);
                return null;
            }

        });
        
        return MapManager.createMap(poster, playerUUID, mapsIDs);
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
        
        BufferedImage newImage = new BufferedImage(destinationW, destinationH, BufferedImage.TYPE_INT_ARGB);
        
        Graphics graphics = newImage.getGraphics();
        graphics.drawImage(source, x, y, finalW, finalH, null);
        graphics.dispose();
        return newImage;
    }
}
