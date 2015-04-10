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

package fr.moribus.imageonmap.migration;

import fr.moribus.imageonmap.ImageOnMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * This class represents and executes the ImageOnMap v3.x migration process
 */
public class V3Migrator
{
    /**
     * The name of the former images directory
     */
    static private final String OLD_IMAGES_DIRECTORY_NAME = "Image";
    
    /**
     * The name of the former file that contained all the maps definitions (including posters)
     */
    static private final String OLD_MAPS_FILE_NAME = "map.yml";
    
    /**
     * The name of the former file that contained all the posters definitions
     */
    static private final String OLD_POSTERS_FILE_NAME = "poster.yml";
    
    /**
     * The name of the backup directory that will contain the pre-v3 files that
     * were present before the migration started
     */
    static private final String BACKUPS_PREV3_DIRECTORY_NAME = "backups_pre-v3";
    
    /**
     * The name of the backup directory that will contain the post-v3 files that
     * were present before the migration started
     */
    static private final String BACKUPS_POSTV3_DIRECTORY_NAME = "backups_post-v3";
    
    /**
     * Returns the former images directory of a given plugin
     * @param plugin The plugin.
     * @return the corresponding 'Image' directory
     */
    static public File getOldImagesDirectory(Plugin plugin)
    {
        return new File(plugin.getDataFolder(), OLD_IMAGES_DIRECTORY_NAME);
    }
    
    /**
     * The plugin that is running the migration
     */
    private final ImageOnMap plugin;
    
    /**
     * The former file that contained all the posters definitions
     */
    private File oldPostersFile;
    
    /**
     * The former file that contained all the maps definitions (including posters)
     */
    private File oldMapsFile;
    
    /**
     * The backup directory that will contain the pre-v3 files that
     * were present before the migration started
     */
    private final File backupsPrev3Directory;
    
    /**
     * The backup directory that will contain the post-v3 files that
     * were present before the migration started
     */
    private final File backupsPostv3Directory;
    
    /**
     * The list of all the posters to migrate
     */
    private final ArrayList<OldSavedPoster> postersToMigrate;
    
    /**
     * The list of all the single maps to migrate
     */
    private final ArrayList<OldSavedMap> mapsToMigrate;
    
    /**
     * The set of all the user names to retreive the UUID from Mojang
     */
    private final HashSet<String> userNamesToFetch;
    
    /**
     * The map of all the usernames and their corresponding UUIDs
     */
    private Map<String, UUID> usersUUIDs;
    
    /**
     * Defines if the migration process is currently running
     */
    private boolean isRunning = false;
    
    public V3Migrator(ImageOnMap plugin)
    {
        this.plugin = plugin;
        
        File dataFolder = plugin.getDataFolder();
        
        oldPostersFile = new File(dataFolder, OLD_POSTERS_FILE_NAME);
        oldMapsFile = new File(dataFolder, OLD_MAPS_FILE_NAME);
        
        backupsPrev3Directory = new File(dataFolder, BACKUPS_PREV3_DIRECTORY_NAME);
        backupsPostv3Directory = new File(dataFolder, BACKUPS_POSTV3_DIRECTORY_NAME);
        
        postersToMigrate = new ArrayList<>();
        mapsToMigrate = new ArrayList<>();
        userNamesToFetch = new HashSet<>();
    }
    
    /**
     * Executes the full migration
     */
    private void migrate()
    {
        try
        {
            if(!spotFilesToMigrate()) return;
            if(checkForExistingBackups()) return;
            if(!loadOldFiles()) return;
            backupMapData();
            fetchUUIDs();
            if(!fetchMissingUUIDs()) return;
        }
        catch(Exception ex)
        {
            logError("Error while preparing migration", ex);
            logError("Aborting migration. No change has been made.");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        try
        {
            
        }
        catch(Exception ex)
        {
            logError("Error while migrating", ex);
            logError("Aborting migration. Some changes may already have been made.");
            logError("Before trying to migrate again, you must recover player files from the backups, and then move the backups away from the plugin directory to avoid overwriting them.");
            
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /* ****** Actions ***** */
    
    /**
     * Checks if there is any of the former files to be migrated
     * @return true if any former map or poster file exists, false otherwise
     */
    private boolean spotFilesToMigrate()
    {
        logInfo("Looking for configuration files to migrate ...");
        
        if(!oldPostersFile.exists()) oldPostersFile = null;
        else logInfo("Detected former posters file " + OLD_POSTERS_FILE_NAME);
        
        if(!oldMapsFile.exists()) oldMapsFile = null;
        else logInfo("Detected former maps file " + OLD_POSTERS_FILE_NAME);
        
        if(oldPostersFile == null && oldMapsFile == null)
        {
            logInfo("There is nothing to migrate. Stopping.");
            return false;
        }
        else
        {
            logInfo("Done.");
            return true;
        }
    }
    
    /**
     * Checks if any existing backup directories exists
     * @return true if a non-empty backup directory exists, false otherwise
     */
    private boolean checkForExistingBackups()
    {
        if((backupsPrev3Directory.exists() && backupsPrev3Directory.list().length == 0)
                || (backupsPostv3Directory.exists() && backupsPostv3Directory.list().length == 0))
        {
            logError("Backup directories already exists.");
            logError("This means that a migration has already been done, or may not have ended well.");
            logError("To start a new migration, you must move away the backup directories so they are not overwritten.");

            return true;
        }
        return false;
    }
    
    /**
     * Creates backups of the former map files, and of the existing map stores
     * @throws IOException 
     */
    private void backupMapData() throws IOException
    {
        logInfo("Backing up map data before migrating ...");
        
        if(!backupsPrev3Directory.exists()) backupsPrev3Directory.mkdirs();
        if(!backupsPostv3Directory.exists()) backupsPostv3Directory.mkdirs();
        
        if(oldMapsFile.exists())
        {
            File oldMapsFileBackup = new File(backupsPrev3Directory, oldMapsFile.getName());
            verifiedBackupCopy(oldMapsFile, oldMapsFileBackup);
        }
        
        if(oldPostersFile.exists())
        {
            File oldPostersFileBackup = new File(backupsPrev3Directory, oldPostersFile.getName());
            verifiedBackupCopy(oldPostersFile, oldPostersFileBackup);
        }
        
        File backupFile;
        for(File mapFile : plugin.getMapsDirectory().listFiles())
        {
            backupFile = new File(backupsPostv3Directory, mapFile.getName());
            verifiedBackupCopy(mapFile, backupFile);
        }
        
        logInfo("Backup complete.");
    }
    
    /**
     * An utility function to check if a map is actually part of a loaded poster
     * @param map The single map.
     * @return true if the map is part of a poster, false otherwise
     */
    private boolean posterContains(OldSavedMap map)
    {
        for(OldSavedPoster poster : postersToMigrate)
        {
            if(poster.contains(map)) return true;
        }
        
        return false;
    }
    
    /**
     * Loads the former files into the corresponding arrays
     * Also fetches the names of all the users that have maps
     * @return true if any of the files contained readable map data, false otherwise
     */
    private boolean loadOldFiles()
    {
        FileConfiguration oldPosters = YamlConfiguration.loadConfiguration(oldPostersFile);
        
        OldSavedPoster oldPoster;
        for(String key : oldPosters.getKeys(false))
        {
            try
            {
                oldPoster = new OldSavedPoster(oldPosters.get(key));
                userNamesToFetch.add(oldPoster.getUserName());
                postersToMigrate.add(oldPoster);
            }
            catch(InvalidConfigurationException ex)
            {
                logWarning("Could not read poster data for key " + key, ex);
            }
        }
        
        FileConfiguration oldMaps = YamlConfiguration.loadConfiguration(oldMapsFile);
        OldSavedMap oldMap;
        
        for(String key : oldMaps.getKeys(false))
        {
            try
            {
                oldMap = new OldSavedMap(oldMaps.get(key));
                
                if(!posterContains(oldMap)) mapsToMigrate.add(oldMap);
            }
            catch(InvalidConfigurationException ex)
            {
                logWarning("Could not read poster data for key " + key, ex);
            }
        }
        
        return (postersToMigrate.size() > 0) || (mapsToMigrate.size() > 0);
    }
    
    /**
     * Fetches all the needed UUIDs from Mojang's UUID conversion service
     * @throws IOException if the fetcher could not connect to Mojang's servers
     * @throws InterruptedException if the thread was interrupted while fetching UUIDs
     */
    private void fetchUUIDs() throws IOException, InterruptedException
    {
        logInfo("Fetching UUIDs from Mojang ...");
        try
        {
            usersUUIDs = UUIDFetcher.fetch(new ArrayList<String>(userNamesToFetch));
        }
        catch(IOException ex)
        {
            logError("An error occured while fetching the UUIDs from Mojang", ex);
            throw ex;
        }
        catch(InterruptedException ex)
        {
            logError("The migration worker has been interrupted", ex);
            throw ex;
        }
        logInfo("Fetching done. " + usersUUIDs.size() + " UUIDs have been retreived.");
    }
    
    /**
     * Fetches the UUIDs that could not be retreived via Mojang's standard API
     * @return true if at least one UUID has been retreived, false otherwise
     */
    private boolean fetchMissingUUIDs() throws IOException, InterruptedException
    {
        if(usersUUIDs.size() == userNamesToFetch.size()) return true;
        int remainingUsersCount = userNamesToFetch.size() - usersUUIDs.size();
        logInfo("Mojang did not find UUIDs for "+remainingUsersCount+" players.");
        logInfo("The Mojang servers limit requests rate at one per second, this may take some time...");
        
        try
        {
            UUIDFetcher.fetchRemaining(userNamesToFetch, usersUUIDs);
        }
        catch(IOException ex)
        {
            logError("An error occured while fetching the UUIDs from Mojang", ex);
            throw ex;
        }
        catch(InterruptedException ex)
        {
            logError("The migration worker has been interrupted", ex);
            throw ex;
        }
        
        if(usersUUIDs.size() <= 0)
        {
            logInfo("Mojang could not find any of the registered players.");
            logInfo("There is nothing to migrate. Stopping.");
            return false;
        }
        
        return true;
    }
    
    /* ****** Utils ***** */
    static public void logInfo(String message)
    {
        System.out.println("[ImageOnMap-Migration][INFO] " + message);
    }
    
    static public void logWarning(String message)
    {
        System.err.println("[ImageOnMap-Migration][WARN] " + message);
    }
    
    static public void logWarning(String message, Exception ex)
    {
        logWarning(message + " : " + ex.getMessage());
    }
    
    static public void logError(String message)
    {
        System.err.println("[ImageOnMap-Migration][ERROR] " + message);
    }
    
    static public void logError(String message, Exception ex)
    {
        logError(message + " : " + ex.getMessage());
    }
    
    public synchronized boolean isRunning()
    {
        return isRunning;
    }
    
    private synchronized void setRunning(boolean running)
    {
        this.isRunning = running;
    }
    
    /**
     * Executes the full migration, and defines the running status of the migration
     */
    public void run()
    {
        setRunning(true);
        migrate();
        setRunning(false);
    }
    
    
    /**
     * Makes a standard file copy, and checks the integrity of the destination 
     * file after the copy
     * @param sourceFile The file to copy
     * @param destinationFile The destination file
     * @throws IOException If the copy failed, if the integrity check failed, or if the destination file already exists
     */
    static private void verifiedBackupCopy(File sourceFile, File destinationFile) throws IOException
    {
        if(destinationFile.exists())
            throw new IOException("Backup copy failed : destination file ("+destinationFile.getName()+") already exists.");
            
        long sourceSize = sourceFile.length();
        String sourceCheckSum = fileCheckSum(sourceFile, "SHA1");
        
        Path sourcePath = Paths.get(sourceFile.getAbsolutePath());
        Path destinationPath = Paths.get(destinationFile.getAbsolutePath());
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        
        long destinationSize = destinationFile.length();
        String destinationCheckSum = fileCheckSum(destinationFile, "SHA1");
        
        if(sourceSize != destinationSize || !sourceCheckSum.equals(destinationCheckSum))
        {
            throw new IOException("Backup copy failed : source and destination files ("+sourceFile.getName()+") differ after copy.");
        }
        
    }
    
    /**
     * Calculates the checksum of a given file
     * @param file The file to calculate the checksum of
     * @param algorithmName The name of the algorithm to use
     * @return The resulting checksum in hexadecimal format
     * @throws IOException 
     */
    static private String fileCheckSum(File file, String algorithmName) throws IOException
    {
        MessageDigest instance;
        try
        {
            instance = MessageDigest.getInstance(algorithmName);
        }
        catch(NoSuchAlgorithmException ex)
        {
            throw new IOException("Could not check file integrity because of NoSuchAlgorithmException : " + ex.getMessage());
        }
        
        FileInputStream inputStream = new FileInputStream(file);
        
        byte[] data = new byte[1024];
        int read = 0;
        
        while((read = inputStream.read(data)) != -1)
        {
            instance.update(data);
        }
        
        byte[] hashBytes = instance.digest();
        
        StringBuilder buffer = new StringBuilder();
        char hexChar;
        for(int i = 0; i < hashBytes.length; i++)
        {
            hexChar = Integer.toHexString((hashBytes[i] & 0xff) + 0x100).charAt(0);
            buffer.append(hexChar);
        }
        
        return buffer.toString();
    }
    
}
