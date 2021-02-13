package fr.moribus.imageonmap.integrity;

import fr.zcraft.quartzlib.tools.PluginLogger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.bukkit.Bukkit;

public class MapIdChecker {

    public static void check() {
        int idcount;
        int idmax;
        String worldFolder = Bukkit.getWorlds().get(0).getWorldFolder().getAbsolutePath();
        try (BufferedReader reader = new BufferedReader(new FileReader(worldFolder + "/data/idcounts.dat"))) {
            //test
            PluginLogger.info(reader.readLine());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // MapManager.getNextAvailableMapID()

        // if (idmax > idcount) {
        //Not good
        //}

    }
}
