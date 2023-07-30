/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2022)
 * Copyright or © or Copr. Vlammar <anais.jabre@gmail.com> (2019 – 2023)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap.map;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.PluginConfiguration;
import fr.moribus.imageonmap.map.MapManagerException.Reason;
import fr.zcraft.quartzlib.tools.PluginLogger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerMapStore implements ConfigurationSerializable {
    private final UUID playerUUID;
    private final ArrayList<ImageMap> mapList = new ArrayList<>();
    private boolean modified = false;
    private int mapCount = 0;
    private FileConfiguration mapConfig = null;
    private File mapsFile = null;

    public PlayerMapStore(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    //TODO maybe usefull to merge with the other manages map
    public synchronized boolean managesMap(int mapID) {
        for (ImageMap map : mapList) {
            if (map.managesMap(mapID)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean managesMap(ItemStack item) {
        if (item == null) {
            return false;
        }
        if (item.getType() != Material.FILLED_MAP) {
            return false;
        }
        return managesMap(MapManager.getMapIdFromItemStack(item));
    }

    public synchronized void addMap(ImageMap map) throws MapManagerException {
        checkMapLimit(map);
        insertMap(map);
    }

    public synchronized void insertMap(ImageMap map) {
        add_Map(map);
        notifyModification();
    }

    private void add_Map(ImageMap map) {
        mapList.add(map);
        mapCount += map.getMapCount();
    }

    public synchronized void deleteMap(ImageMap map) throws MapManagerException {
        delete_Map(map);
        notifyModification();
    }

    private void delete_Map(ImageMap map) throws MapManagerException {
        if (!mapList.remove(map)) {
            throw new MapManagerException(Reason.IMAGEMAP_DOES_NOT_EXIST);
        }
        mapCount -= map.getMapCount();
    }

    public synchronized boolean mapExists(String mapId) {
        return getMap(mapId) != null;
    }

    public String getNextAvailableMapID(String mapId) {
        //TODO check if the value is always greater than the id count
        if (!mapExists(mapId)) {
            return mapId;
        }
        int id = 0;
        do {
            id++;
        } while (mapExists(mapId + "-" + id));

        return mapId + "-" + id;
    }

    public synchronized List<ImageMap> getMapList() {
        return new ArrayList<>(mapList);
    }

    //TODO refactor to arraylist instead of an array
    public synchronized ImageMap[] getMaps() {
        return mapList.toArray(new ImageMap[0]);
    }

    public synchronized ImageMap getMap(String mapId) {
        for (ImageMap map : mapList) {
            if (map.getId().equals(mapId)) {
                return map;
            }
        }
        return null;
    }

    /* ===== Getters & Setters ===== */

    public void checkMapLimit(ImageMap map) throws MapManagerException {
        checkMapLimit(map.getMapCount());
    }

    public void checkMapLimit(int newMapsCount) throws MapManagerException {
        int limit = PluginConfiguration.MAP_PLAYER_LIMIT.get();
        if (limit <= 0) {
            return;
        }

        if (getMapCount() + newMapsCount > limit) {
            throw new MapManagerException(Reason.MAXIMUM_PLAYER_MAPS_EXCEEDED, limit);
        }
    }

    public UUID getUUID() {
        return playerUUID;
    }

    public synchronized boolean isModified() {
        return modified;
    }

    public synchronized void notifyModification() {
        this.modified = true;
    }

    /* ****** Serializing ***** */

    public synchronized int getMapCount() {
        return this.mapCount;
    }

    public synchronized int getImagesCount() {
        return this.mapList.size();
    }

    /* ****** Configuration Files management ***** */

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        ArrayList<Map> list = new ArrayList<>();
        synchronized (this) {
            for (ImageMap tmpMap : mapList) {
                list.add(tmpMap.serialize());
            }
        }
        map.put("mapList", list);
        return map;
    }

    private void loadFromConfig(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) section.getList("mapList");
        if (list == null) {
            return;
        }

        for (Map<String, Object> tmpMap : list) {
            try {
                ImageMap newMap = ImageMap.fromConfig(tmpMap, playerUUID);
                synchronized (this) {
                    add_Map(newMap);
                }
            } catch (InvalidConfigurationException ex) {
                PluginLogger.warning("Could not load map data : ", ex);
            }
        }

        try {
            checkMapLimit(0);
        } catch (MapManagerException ex) {
            PluginLogger.warning("Map limit exceeded for player {0} ({1} maps loaded)",
                    playerUUID.toString(), mapList.size());
        }
    }

    public FileConfiguration getToolConfig() {
        if (mapConfig == null) {
            load();
        }

        return mapConfig;
    }

    public void load() {
        if (mapsFile == null) {
            mapsFile = new File(ImageOnMap.getPlugin().getMapsDirectory(), playerUUID.toString() + ".yml");
            if (!mapsFile.exists()) {
                save();
            }
        }
        mapConfig = YamlConfiguration.loadConfiguration(mapsFile);
        loadFromConfig(getToolConfig().getConfigurationSection("PlayerMapStore"));
    }

    public void save() {
        if (mapsFile == null || mapConfig == null) {
            return;
        }
        getToolConfig().set("PlayerMapStore", this.serialize());
        try {
            getToolConfig().save(mapsFile);

        } catch (IOException ex) {
            PluginLogger.error("Could not save maps file for player '{0}'", ex, playerUUID.toString());
        }
        synchronized (this) {
            modified = false;
        }
    }
}
