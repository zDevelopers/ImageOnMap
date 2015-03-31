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

package fr.moribus.imageonmap;

import fr.moribus.imageonmap.commands.Commands;
import fr.moribus.imageonmap.image.ImageIOExecutor;
import fr.moribus.imageonmap.image.ImageRendererExecutor;
import fr.moribus.imageonmap.image.MapInitEvent;
import fr.moribus.imageonmap.map.MapManager;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public final class ImageOnMap extends JavaPlugin
{
    static private final String IMAGES_DIRECTORY_NAME = "images";
    static private final String MAPS_DIRECTORY_NAME = "maps";
    static private ImageOnMap plugin;
    
    private final File imagesDirectory;
    private final File mapsDirectory;

    public ImageOnMap()
    {
        imagesDirectory = new File(this.getDataFolder(), IMAGES_DIRECTORY_NAME);
        mapsDirectory = new File(this.getDataFolder(), MAPS_DIRECTORY_NAME);
        plugin = this;
    }

    static public ImageOnMap getPlugin()
    {
        return plugin;
    }
    
    public File getImagesDirectory() {return imagesDirectory;}
    public File getMapsDirectory() {return mapsDirectory;}
    public File getImageFile(short mapID)
    {
        return new File(imagesDirectory, "map"+mapID+".png");
    }
    
    @Override
    public void onEnable()
    {
        // Creating the images directory if necessary
        if(!imagesDirectory.exists())
        {
            if(!imagesDirectory.mkdirs())
            {
                PluginLogger.LogError("FATAL : Could not create the images directory.", null);
                this.setEnabled(false);
                return;
            }
        }
        
        if(!mapsDirectory.exists())
        {
            if(!mapsDirectory.mkdirs())
            {
                PluginLogger.LogError("FATAL : Could not create the images directory.", null);
                this.setEnabled(false);
                return;
            }
        }
         
        //Init all the things !
        MetricsLite.startMetrics();
        ImageIOExecutor.start();
        ImageRendererExecutor.start();
        MapManager.init();
        Commands.init(this);
        getServer().getPluginManager().registerEvents(new MapInitEvent(), this);
        MapInitEvent.init();
    }

    @Override
    public void onDisable()
    {
        ImageIOExecutor.stop();
        ImageRendererExecutor.stop();
        MapManager.exit();
    }

}
