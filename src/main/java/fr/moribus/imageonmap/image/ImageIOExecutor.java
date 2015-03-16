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
import fr.moribus.imageonmap.worker.Worker;
import fr.moribus.imageonmap.worker.WorkerRunnable;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageIOExecutor extends Worker
{
    static private ImageIOExecutor instance;
    
    static public void start()
    {
        if(instance != null) stop();
        instance = new ImageIOExecutor();
        instance.init();
    }
    
    static public void stop()
    {
        instance.exit();
        instance = null;
    }
    
    private ImageIOExecutor()
    {
        super("Image IO");
    }
    
    static public void loadImage(final File file, final Renderer mapRenderer) 
    {
        instance.submitQuery(new WorkerRunnable()
            {
                @Override
                public void run() throws Exception
                {
                    BufferedImage image = ImageIO.read(file);
                    mapRenderer.setImage(image);
                }
            });
    }
}
