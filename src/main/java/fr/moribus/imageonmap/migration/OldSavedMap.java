/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2022)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2022)
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

package fr.moribus.imageonmap.migration;

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.SingleMap;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;

class OldSavedMap {
    private final short mapId;
    private final String mapName;
    private final String userName;

    public OldSavedMap(Object rawData) throws InvalidConfigurationException {
        List<String> data;
        try {
            data = (List<String>) rawData;
        } catch (ClassCastException ex) {
            throw new InvalidConfigurationException("Invalid map data : " + ex.getMessage());
        }

        if (data.size() < 3) {
            throw new InvalidConfigurationException("Map data too short (given : " + data.size() + ", expected 3)");
        }
        try {
            mapId = Short.parseShort(data.get(0));
        } catch (NumberFormatException ex) {
            throw new InvalidConfigurationException("Invalid map ID : " + ex.getMessage());
        }

        mapName = data.get(1);
        userName = data.get(2);
    }

    public ImageMap toImageMap(UUID userUUID) {
        return new SingleMap(userUUID, mapId, null, mapName);
    }

    public void serialize(Configuration configuration) {
        ArrayList<String> data = new ArrayList<>();
        data.add(Short.toString(mapId));
        data.add(mapName);
        data.add(userName);
        configuration.set(mapName, data);
    }

    public boolean isMapValid() {
        return MapManager.mapIdExists(mapId);
    }

    public short getMapId() {
        return mapId;
    }

    public String getUserName() {
        return userName;
    }
}
