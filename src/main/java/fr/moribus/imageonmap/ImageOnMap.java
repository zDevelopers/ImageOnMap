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

import fr.moribus.imageonmap.commands.maptool.DeleteConfirmCommand;
import fr.moribus.imageonmap.commands.maptool.DeleteNoConfirmCommand;
import fr.moribus.imageonmap.commands.maptool.ExploreCommand;
import fr.moribus.imageonmap.commands.maptool.GetCommand;
import fr.moribus.imageonmap.commands.maptool.GetRemainingCommand;
import fr.moribus.imageonmap.commands.maptool.ListCommand;
import fr.moribus.imageonmap.commands.maptool.MigrateCommand;
import fr.moribus.imageonmap.commands.maptool.NewCommand;
import fr.moribus.imageonmap.image.ImageIOExecutor;
import fr.moribus.imageonmap.image.ImageRendererExecutor;
import fr.moribus.imageonmap.image.MapInitEvent;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.migration.MigratorExecutor;
import fr.moribus.imageonmap.migration.V3Migrator;
import fr.moribus.imageonmap.ui.MapItemManager;
import fr.zcraft.zlib.components.commands.Commands;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.core.ZPlugin;
import fr.zcraft.zlib.tools.PluginLogger;

import java.io.File;
import java.io.IOException;

public final class ImageOnMap extends ZPlugin
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
    
    @SuppressWarnings ("unchecked")
    @Override
    public void onEnable()
    {
        // Creating the images and maps directories if necessary
        try
        {
            imagesDirectory = checkPluginDirectory(imagesDirectory, V3Migrator.getOldImagesDirectory(this));
            checkPluginDirectory(mapsDirectory);
        }
        catch(IOException ex)
        {
            PluginLogger.error("FATAL : " + ex.getMessage());
            this.setEnabled(false);
            return;
        }

        loadComponents(Gui.class, Commands.class, ImageIOExecutor.class, ImageRendererExecutor.class);
        
        //Init all the things !
        PluginConfiguration.init(this);
        MetricsLite.startMetrics();
        MapManager.init();
        MapInitEvent.init(this);
        MapItemManager.init();

        Commands.register(
                "maptool",
                NewCommand.class,
                ListCommand.class,
                GetCommand.class,
                DeleteConfirmCommand.class,
                DeleteNoConfirmCommand.class,
                GetRemainingCommand.class,
                ExploreCommand.class,
                MigrateCommand.class
        );

        Commands.registerShortcut("maptool", NewCommand.class, "tomap");
        Commands.registerShortcut("maptool", ExploreCommand.class, "maps");
    }

    @Override
    public void onDisable()
    {
        MapManager.exit();
        MapItemManager.exit();
        MigratorExecutor.waitForMigration();

        super.onDisable();
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
