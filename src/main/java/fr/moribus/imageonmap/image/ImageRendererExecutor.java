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
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.worker.Worker;
import fr.zcraft.zlib.components.worker.WorkerAttributes;
import fr.zcraft.zlib.components.worker.WorkerCallback;
import fr.zcraft.zlib.components.worker.WorkerRunnable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;


@WorkerAttributes (name = "Image Renderer", queriesMainThread = true)
public class ImageRendererExecutor extends Worker
{
    static public void render(final URL url, final ImageUtils.ScalingType scaling, final UUID playerUUID, final int width, final int height, WorkerCallback<ImageMap> callback)
    {
        submitQuery(new WorkerRunnable<ImageMap>()
        {
            @Override
            public ImageMap run() throws Throwable
            {
                final URLConnection connection = url.openConnection();
                connection.connect();
                if(connection instanceof HttpURLConnection)
                {
                    final HttpURLConnection  httpConnection = (HttpURLConnection) connection;
                    final int httpCode = httpConnection.getResponseCode();
                    if((httpCode / 100) != 2)
                    {
                        throw new IOException(I.t("HTTP error: {0} {1}", httpCode, httpConnection.getResponseMessage()));
                    }
                }
                final InputStream stream = connection.getInputStream();
                final BufferedImage image = ImageIO.read(stream);
                
                if (image == null) throw new IOException(I.t("The given URL is not a valid image"));

                if(scaling != ImageUtils.ScalingType.NONE && height <= 1 && width <= 1) {
                    return renderSingle(scaling.resize(image, ImageMap.WIDTH, ImageMap.HEIGHT), playerUUID);
                }

                final BufferedImage resizedImage = scaling.resize(image, ImageMap.WIDTH * width, ImageMap.HEIGHT * height);
                return renderPoster(resizedImage, playerUUID);
            }
        }, callback);
    }

    static private ImageMap renderSingle(final BufferedImage image, final UUID playerUUID) throws Throwable
    {
        MapManager.checkMapLimit(1, playerUUID);
        final Future<Short> futureMapID = submitToMainThread(new Callable<Short>()
        {
            @Override
            public Short call() throws Exception
            {
                return MapManager.getNewMapsIds(1)[0];
            }
        });

        final short mapID = futureMapID.get();
        ImageIOExecutor.saveImage(mapID, image);
        
        submitToMainThread(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                Renderer.installRenderer(image, mapID);
                return null;
            }
        });
        
        return MapManager.createMap(playerUUID, mapID);
    }

    static private ImageMap renderPoster(final BufferedImage image, final UUID playerUUID) throws Throwable
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
}
