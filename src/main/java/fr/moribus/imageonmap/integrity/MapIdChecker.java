package fr.moribus.imageonmap.integrity;

import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.quartzlib.components.nbt.NBTReadFile;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.reflection.Reflection;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;

public class MapIdChecker {

    public static void check() {
        int idCount = 0;
        int idMax = 0;

        String worldFolder = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();

        NBTReadFile nbtReader = new NBTReadFile();
        nbtReader.read(new File(worldFolder + "/data/idcounts.dat"));

        try {
            Object compound = Reflection.call(nbtReader.tagCompound, "getCompound", "data");
            idCount = (int) Reflection.call(compound, "getInt", "map");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        //I use the term set instead of map to avoid confusion when designing the java map
        // (confusion with minecraft map and java map).
        Map<UUID, List<Integer>> idSet = MapManager.getMapIdSet();
        Map<Integer, List<UUID>> conflict = new HashMap<>();
        Map<Integer, List<UUID>> sane = new HashMap<>();
        for (Map.Entry<UUID, List<Integer>> entry : idSet.entrySet()) {
            //We are searching for the max used id
            for (int id : entry.getValue()) {
                if (idMax < id) {
                    idMax = id;
                }
            }


            //if the ids overlap on the same player that mean the newer id (the last in the set) is not corrupted,
            // each older map that use this id need to be deleted
            List<Integer> idRead = new ArrayList<>();
            List<Integer> idBad = new ArrayList<>();
            for (Integer id : entry.getValue()) {

                if (idRead.contains(id) || idBad.contains(id)) {
                    idRead.remove(id);
                    if (!idBad.contains(id)) {
                        idBad.add(id);
                    }

                } else {
                    idRead.add(id);
                }
            }
            if (!idBad.isEmpty()) {
                PluginLogger
                        .info("Corruption detected, there are some ids that are overlapping for the player's uuid "
                                + entry.getKey());
            }

            //repare
            /*try {

                boolean nextMap = false;
                for (Integer id : idBad) {
                    for (ImageMap map : MapManager.getPlayerMapStore(entry.getKey()).getMapList()) {
                        for (Integer mapId : map.getMapsIDs()) {
                            if (nextMap) {
                                continue;
                            }
                            if (mapId == id) {
                                map.getMapsIDs();
                                MapManager.getPlayerMapStore(entry.getKey()).deleteMap(map);
                                nextMap = true;
                            }
                        }
                        nextMap = false;
                    }
                }
            } catch (MapManagerException e) {
                e.printStackTrace();
            }*/

            

        }

        //We search for a common usage of map id between different player.
        //if an id is used between various player it's possible by looking at the id overlapping to find out the
        // corrupted map, if not possible (same area) we throw a warning. This is not a critical
        // error but this needs an operator to manually delete the corrupted map.

        //REPARATION
        //if the map id used by IoM are used by
        if (idMax > idCount) {
            PluginLogger.info("Corruption detected, idmax > idcount need to create additional maps");
            /*nbtReader = new NBTReadFile();
            nbtReader.read(new File(worldFolder + "/data/idcounts.dat"));

            try {
                Object compound = Reflection.call(nbtReader.tagCompound, "getCompound", "data");
                idCount = (int) Reflection.call(compound, "setInt", "map",idMax);
                nbtReader.write(new File(worldFolder + "/data/idcounts.dat"), compound);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }*/

            //int[] mapids = MapManager.getNewMapsIds(idMax - idCount);
            PluginLogger.info("number of map to create" + (idMax - idCount));
            /*StringBuilder s = new StringBuilder();
            for (int id : mapids) {
                s = s.append(" " + id);
            }
            PluginLogger.info("ids " + s);*/

        }
        PluginLogger.info("id max" + idMax);
        PluginLogger.info("id count" + idCount);
    }
}
