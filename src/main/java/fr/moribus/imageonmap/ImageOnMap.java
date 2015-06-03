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
import fr.moribus.imageonmap.migration.MigratorExecutor;
import fr.moribus.imageonmap.migration.V3Migrator;
import fr.moribus.imageonmap.ui.MapItemManager;
import java.io.File;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

public final class ImageOnMap extends JavaPlugin
{
    static private final String IMAGES_DIRECTORY_NAME = "images";
    static private final String MAPS_DIRECTORY_NAME = "maps";
    static private ImageOnMap plugin;
    
    private File imagesDirectory;
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
        PluginLogger.init(this);
        // Creating the images and maps directories if necessary
        try
        {
            imagesDirectory = checkPluginDirectory(imagesDirectory, V3Migrator.getOldImagesDirectory(this));
            checkPluginDirectory(mapsDirectory);
        }
        catch(IOException ex)
        {
            PluginLogger.error("FATAL : " + ex.getMessage(), null);
            this.setEnabled(false);
            return;
        }
        
        //Init all the things !
        PluginConfiguration.init(this);
        MetricsLite.startMetrics();
        ImageIOExecutor.start();
        ImageRendererExecutor.start();
        MapManager.init();
        Commands.init(this);
        MapInitEvent.init(this);
        MapItemManager.init();
    }

    @Override
    public void onDisable()
    {
        ImageIOExecutor.stop();
        ImageRendererExecutor.stop();
        MapManager.exit();
        MapItemManager.exit();
        MigratorExecutor.waitForMigration();
        PluginLogger.exit();
    }
    
    private File checkPluginDirectory(File primaryFile, File... alternateFiles) throws IOException
    {
        if(primaryFile.exists()) return primaryFile;
        for(File file : alternateFiles)
        {
            if(file.exists()) return file;
        }
        if(!primaryFile.mkdirs()) 
            throw new IOException("Could not create '" + primaryFile.getName() + "' plugin directory.");
        return primaryFile;
    }

}
