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

package fr.moribus.imageonmap.migration;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.mojang.UUIDFetcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * This class represents and executes the ImageOnMap v3.x migration process
 */
public class V3Migrator implements Runnable {
    /**
     * The name of the former images directory
     */
    private static final String OLD_IMAGES_DIRECTORY_NAME = "Image";

    /**
     * The name of the former file that contained all the maps definitions (including posters)
     */
    private static final String OLD_MAPS_FILE_NAME = "map.yml";

    /**
     * The name of the former file that contained all the posters definitions
     */
    private static final String OLD_POSTERS_FILE_NAME = "poster.yml";

    /**
     * The name of the backup directory that will contain the pre-v3 files that
     * were present before the migration started
     */
    private static final String BACKUPS_PREV3_DIRECTORY_NAME = "backups_pre-v3";

    /**
     * The name of the backup directory that will contain the post-v3 files that
     * were present before the migration started
     */
    private static final String BACKUPS_POSTV3_DIRECTORY_NAME = "backups_post-v3";
    /**
     * The plugin that is running the migration
     */
    private final ImageOnMap plugin;
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
    private final ArrayDeque<OldSavedPoster> postersToMigrate;
    /**
     * The list of all the single maps to migrate
     */
    private final ArrayDeque<OldSavedMap> mapsToMigrate;
    /**
     * The set of all the user names to retreive the UUID from Mojang
     */
    private final HashSet<String> userNamesToFetch;
    /**
     * The former file that contained all the posters definitions
     */
    private File oldPostersFile;
    /**
     * The former file that contained all the maps definitions (including posters)
     */
    private File oldMapsFile;
    /**
     * The map of all the usernames and their corresponding UUIDs
     */
    private Map<String, UUID> usersUUIDs;
    /**
     * Defines if the migration process is currently running
     */
    private boolean isRunning = false;

    public V3Migrator(ImageOnMap plugin) {
        this.plugin = plugin;

        File dataFolder = plugin.getDataFolder();

        oldPostersFile = new File(dataFolder, OLD_POSTERS_FILE_NAME);
        oldMapsFile = new File(dataFolder, OLD_MAPS_FILE_NAME);

        backupsPrev3Directory = new File(dataFolder, BACKUPS_PREV3_DIRECTORY_NAME);
        backupsPostv3Directory = new File(dataFolder, BACKUPS_POSTV3_DIRECTORY_NAME);

        postersToMigrate = new ArrayDeque<>();
        mapsToMigrate = new ArrayDeque<>();
        userNamesToFetch = new HashSet<>();
    }

    /**
     * Returns the former images directory of a given plugin
     *
     * @param plugin The plugin.
     * @return the corresponding 'Image' directory
     */
    public static File getOldImagesDirectory(Plugin plugin) {
        return new File(plugin.getDataFolder(), OLD_IMAGES_DIRECTORY_NAME);
    }

    /**
     * Makes a standard file copy, and checks the integrity of the destination
     * file after the copy
     *
     * @param sourceFile      The file to copy
     * @param destinationFile The destination file
     * @throws IOException If the copy failed, if the integrity check failed, or if the destination file already exists
     */
    private static void verifiedBackupCopy(File sourceFile, File destinationFile) throws IOException {
        if (destinationFile.exists()) {
            throw new IOException(
                    "Backup copy failed : destination file (" + destinationFile.getName() + ") already exists.");
        }

        long sourceSize = sourceFile.length();
        String sourceCheckSum = fileCheckSum(sourceFile, "SHA1");

        Path sourcePath = Paths.get(sourceFile.getAbsolutePath());
        Path destinationPath = Paths.get(destinationFile.getAbsolutePath());
        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

        long destinationSize = destinationFile.length();
        String destinationCheckSum = fileCheckSum(destinationFile, "SHA1");

        if (sourceSize != destinationSize || !sourceCheckSum.equals(destinationCheckSum)) {
            throw new IOException("Backup copy failed : source and destination files ("
                    + sourceFile.getName()
                    + ") differ after copy.");
        }

    }

    /* ****** Actions ***** */

    /**
     * Calculates the checksum of a given file
     *
     * @param file          The file to calculate the checksum of
     * @param algorithmName The name of the algorithm to use
     * @return The resulting checksum in hexadecimal format
     * @throws IOException
     **/
    private static String fileCheckSum(File file, String algorithmName) throws IOException {
        MessageDigest instance;
        try {
            instance = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(
                    "Could not check file integrity because of NoSuchAlgorithmException : " + ex.getMessage());
        }

        FileInputStream inputStream = new FileInputStream(file);

        byte[] data = new byte[1024];
        int read = 0;

        while ((read = inputStream.read(data)) != -1) {
            instance.update(data);
        }

        byte[] hashBytes = instance.digest();

        StringBuilder buffer = new StringBuilder();
        char hexChar;
        for (int i = 0; i < hashBytes.length; i++) {
            hexChar = Integer.toHexString((hashBytes[i] & 0xff) + 0x100).charAt(0);
            buffer.append(hexChar);
        }

        return buffer.toString();
    }

    /**
     * Executes the full migration
     */
    private void migrate() {
        try {
            if (!spotFilesToMigrate()) {
                return;
            }
            if (checkForExistingBackups()) {
                return;
            }
            if (!loadOldFiles()) {
                return;
            }
            backupMapData();
            fetchUUIDs();
            if (!fetchMissingUUIDs()) {
                return;
            }
        } catch (Exception ex) {
            PluginLogger.error(I.t("Error while preparing migration"));
            PluginLogger.error(I.t("Aborting migration. No change has been made."), ex);
            return;
        }

        try {
            mergeMapData();
            saveChanges();
            cleanup();
        } catch (Exception ex) {
            PluginLogger.error(I.t("Error while migrating"), ex);
            PluginLogger.error(I.t("Aborting migration. Some changes may already have been made."));
            PluginLogger.error(I.t(
                    "Before trying to migrate again, you must recover player files from the backups,"
                            + " and then move the backups away from the plugin directory to avoid overwriting them."));
        }
    }

    /**
     * Checks if there is any of the former files to be migrated
     *
     * @return true if any former map or poster file exists, false otherwise
     */
    private boolean spotFilesToMigrate() {
        PluginLogger.info(I.t("Looking for configuration files to migrate..."));

        if (!oldPostersFile.exists()) {
            oldPostersFile = null;
        } else {
            PluginLogger.info(I.t("Detected former posters file {0}", OLD_POSTERS_FILE_NAME));
        }

        if (!oldMapsFile.exists()) {
            oldMapsFile = null;
        } else {
            PluginLogger.info(I.t("Detected former maps file {0}", OLD_MAPS_FILE_NAME));
        }

        if (oldPostersFile == null && oldMapsFile == null) {
            PluginLogger.info(I.t("There is nothing to migrate. Stopping."));
            return false;
        } else {
            PluginLogger.info(I.t("Done."));
            return true;
        }
    }

    /**
     * Checks if any existing backup directories exists
     *
     * @return true if a non-empty backup directory exists, false otherwise
     */
    private boolean checkForExistingBackups() {
        if ((backupsPrev3Directory.exists() && backupsPrev3Directory.list().length == 0)
                || (backupsPostv3Directory.exists() && backupsPostv3Directory.list().length == 0)) {
            PluginLogger.error(I.t("Backup directories already exists."));
            PluginLogger.error(I.t("This means that a migration has already been done,"
                    + " or may not have ended well."));
            PluginLogger.error(I.t(
                    "To start a new migration,"
                            + " you must move away the backup directories so they are not overwritten."));

            return true;
        }
        return false;
    }

    /**
     * Creates backups of the former map files, and of the existing map stores
     *
     * @throws IOException
     **/
    private void backupMapData() throws IOException {
        PluginLogger.info(I.t("Backing up map data before migrating..."));

        if (!backupsPrev3Directory.exists()) {
            backupsPrev3Directory.mkdirs();
        }
        if (!backupsPostv3Directory.exists()) {
            backupsPostv3Directory.mkdirs();
        }

        if (oldMapsFile != null && oldMapsFile.exists()) {
            File oldMapsFileBackup = new File(backupsPrev3Directory, oldMapsFile.getName());
            verifiedBackupCopy(oldMapsFile, oldMapsFileBackup);
        }

        if (oldPostersFile != null && oldPostersFile.exists()) {
            File oldPostersFileBackup = new File(backupsPrev3Directory, oldPostersFile.getName());
            verifiedBackupCopy(oldPostersFile, oldPostersFileBackup);
        }

        File backupFile;
        for (File mapFile : plugin.getMapsDirectory().listFiles()) {
            backupFile = new File(backupsPostv3Directory, mapFile.getName());
            verifiedBackupCopy(mapFile, backupFile);
        }

        PluginLogger.info(I.t("Backup complete."));
    }

    /**
     * An utility function to check if a map is actually part of a loaded poster
     *
     * @param map The single map.
     * @return true if the map is part of a poster, false otherwise
     */
    private boolean posterContains(OldSavedMap map) {
        for (OldSavedPoster poster : postersToMigrate) {
            if (poster.contains(map)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Loads the former files into the corresponding arrays
     * Also fetches the names of all the users that have maps
     *
     * @return true if any of the files contained readable map data, false otherwise
     */
    private boolean loadOldFiles() {
        if (oldPostersFile != null) {
            FileConfiguration oldPosters = YamlConfiguration.loadConfiguration(oldPostersFile);

            OldSavedPoster oldPoster;
            for (String key : oldPosters.getKeys(false)) {
                if ("IdCount".equals(key)) {
                    continue;
                }
                try {
                    oldPoster = new OldSavedPoster(oldPosters.get(key), key);
                    postersToMigrate.add(oldPoster);
                    if (!userNamesToFetch.contains(oldPoster.getUserName())) {
                        userNamesToFetch.add(oldPoster.getUserName());
                    }
                } catch (InvalidConfigurationException ex) {
                    PluginLogger.warning("Could not read poster data for key {0}", ex, key);
                }
            }
        }

        if (oldMapsFile != null) {
            FileConfiguration oldMaps = YamlConfiguration.loadConfiguration(oldMapsFile);
            OldSavedMap oldMap;

            for (String key : oldMaps.getKeys(false)) {
                try {
                    if ("IdCount".equals(key)) {
                        continue;
                    }
                    oldMap = new OldSavedMap(oldMaps.get(key));

                    if (!posterContains(oldMap)) {
                        mapsToMigrate.add(oldMap);
                    }

                    if (!userNamesToFetch.contains(oldMap.getUserName())) {
                        userNamesToFetch.add(oldMap.getUserName());
                    }
                } catch (InvalidConfigurationException ex) {
                    PluginLogger.warning("Could not read poster data for key '{0}'", ex, key);
                }
            }
        }

        return (postersToMigrate.size() > 0) || (mapsToMigrate.size() > 0);
    }

    /**
     * Fetches all the needed UUIDs from Mojang's UUID conversion service
     *
     * @throws IOException          if the fetcher could not connect to Mojang's servers
     * @throws InterruptedException if the thread was interrupted while fetching UUIDs
     */
    private void fetchUUIDs() throws IOException, InterruptedException {
        PluginLogger.info(I.t("Fetching UUIDs from Mojang..."));
        try {
            usersUUIDs = UUIDFetcher.fetch(new ArrayList<String>(userNamesToFetch));
        } catch (IOException ex) {
            PluginLogger.error(I.t("An error occurred while fetching the UUIDs from Mojang"), ex);
            throw ex;
        } catch (InterruptedException ex) {
            PluginLogger.error(I.t("The migration worker has been interrupted"), ex);
            throw ex;
        }
        PluginLogger.info(I.tn("Fetching done. {0} UUID have been retrieved.",
                "Fetching done. {0} UUIDs have been retrieved.", usersUUIDs.size()));
    }

    /**
     * Fetches the UUIDs that could not be retrieved via Mojang's standard API
     *
     * @return true if at least one UUID has been retrieved, false otherwise
     */
    private boolean fetchMissingUUIDs() throws IOException, InterruptedException {
        if (usersUUIDs.size() == userNamesToFetch.size()) {
            return true;
        }
        int remainingUsersCount = userNamesToFetch.size() - usersUUIDs.size();
        PluginLogger.info(I.tn("Mojang did not find UUIDs for {0} player at the current time.",
                "Mojang did not find UUIDs for {0} players at the current time.", remainingUsersCount));
        PluginLogger.info(I.t("The Mojang servers limit requests rate at one per second, this may take some time..."));

        try {
            UUIDFetcher.fetchRemaining(userNamesToFetch, usersUUIDs);
        } catch (IOException ex) {
            PluginLogger.error(I.t("An error occurred while fetching the UUIDs from Mojang"));
            throw ex;
        } catch (InterruptedException ex) {
            PluginLogger.error(I.t("The migration worker has been interrupted"));
            throw ex;
        }

        if (usersUUIDs.size() != userNamesToFetch.size()) {
            PluginLogger.warning(I.tn("Mojang did not find player data for {0} player",
                    "Mojang did not find player data for {0} players",
                    userNamesToFetch.size() - usersUUIDs.size()));
            PluginLogger.warning(I.t("The following players do not exist or do not have paid accounts :"));

            String missingUsersList = "";

            for (String user : userNamesToFetch) {
                if (!usersUUIDs.containsKey(user)) {
                    missingUsersList += user + ", ";
                }
            }
            missingUsersList = missingUsersList.substring(0, missingUsersList.length());

            PluginLogger.info(missingUsersList);
        }

        if (usersUUIDs.size() <= 0) {
            PluginLogger.info(I.t("Mojang could not find any of the registered players."));
            PluginLogger.info(I.t("There is nothing to migrate. Stopping."));
            return false;
        }

        return true;
    }

    private void mergeMapData() {
        PluginLogger.info(I.t("Merging map data..."));

        ArrayDeque<OldSavedMap> remainingMaps = new ArrayDeque<>();
        ArrayDeque<OldSavedPoster> remainingPosters = new ArrayDeque<>();

        ArrayDeque<Integer> missingMapIds = new ArrayDeque<>();

        UUID playerUUID;
        OldSavedMap map;
        while (!mapsToMigrate.isEmpty()) {
            map = mapsToMigrate.pop();
            playerUUID = usersUUIDs.get(map.getUserName());
            if (playerUUID == null) {
                remainingMaps.add(map);
            } else if (!map.isMapValid()) {
                missingMapIds.add((int) map.getMapId());
            } else {
                MapManager.insertMap(map.toImageMap(playerUUID));
            }
        }
        mapsToMigrate.addAll(remainingMaps);

        OldSavedPoster poster;
        while (!postersToMigrate.isEmpty()) {
            poster = postersToMigrate.pop();
            playerUUID = usersUUIDs.get(poster.getUserName());
            if (playerUUID == null) {
                remainingPosters.add(poster);
            } else if (!poster.isMapValid()) {
                missingMapIds.addAll(Arrays.stream(ArrayUtils.toObject(poster.getMapsIds())).map(id -> (int) id)
                        .collect(Collectors.toList()));
            } else {
                MapManager.insertMap(poster.toImageMap(playerUUID));
            }
        }
        postersToMigrate.addAll(remainingPosters);

        if (!missingMapIds.isEmpty()) {
            PluginLogger.warning(I.tn("{0} registered minecraft map is missing from the save.",
                    "{0} registered minecraft maps are missing from the save.", missingMapIds.size()));
            PluginLogger.warning(
                    I.t("These maps will not be migrated,"
                            + " but this could mean the save has been altered or corrupted."));
            PluginLogger.warning(I.t("The following maps are missing : {0} ",
                    StringUtils.join(missingMapIds, ',')));
        }
    }

    /* ****** Utils ***** */

    private void saveChanges() {
        PluginLogger.info(I.t("Saving changes..."));
        MapManager.save();
    }

    private void cleanup() throws IOException {
        PluginLogger.info(I.t("Cleaning up old data files..."));

        //Cleaning maps file
        if (oldMapsFile != null) {
            if (mapsToMigrate.isEmpty()) {
                PluginLogger.info(I.t("Deleting old map data file..."));
                oldMapsFile.delete();
            } else {
                PluginLogger.info(I.tn("{0} map could not be migrated.", "{0} maps could not be migrated.",
                        mapsToMigrate.size()));
                YamlConfiguration mapConfig = new YamlConfiguration();
                mapConfig.set("IdCount", mapsToMigrate.size());

                for (OldSavedMap map : mapsToMigrate) {
                    map.serialize(mapConfig);
                }

                mapConfig.save(oldMapsFile);
            }
        }

        //Cleaning posters file
        if (oldPostersFile != null) {
            if (postersToMigrate.isEmpty()) {
                PluginLogger.info(I.t("Deleting old poster data file..."));
                oldPostersFile.delete();
            } else {
                PluginLogger.info(I.tn("{0} poster could not be migrated.", "{0} posters could not be migrated.",
                        postersToMigrate.size()));
                YamlConfiguration posterConfig = new YamlConfiguration();
                posterConfig.set("IdCount", postersToMigrate.size());

                for (OldSavedPoster poster : postersToMigrate) {
                    poster.serialize(posterConfig);
                }

                posterConfig.save(oldPostersFile);
            }
        }

        PluginLogger.info(I.t("Data that has not been migrated will be kept in the old data files."));
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    private synchronized void setRunning(boolean running) {
        this.isRunning = running;
    }

    /**
     * Executes the full migration, and defines the running status of the migration
     */
    @Override
    public void run() {
        setRunning(true);
        migrate();
        setRunning(false);
    }

}
